package de.intranda.goobi.plugins;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import ugh.dl.DigitalDocument;
import ugh.dl.ExportFileformat;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.UGHException;

public class FedoraExport {

    private static final Logger log = Logger.getLogger(FedoraExport.class);

    public static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH.mm.ss.SSS");

    private final String fedoraUrl;
    private final String rootUrl;
    private final String repositoryPath;
    private final List<String> imageDataList = new ArrayList<>();
    private final List<String> teiList = new ArrayList<>();
    private final List<String> cmdiList = new ArrayList<>();

    public FedoraExport(String fedoraUrl, String repositoryPath) {
        if (fedoraUrl == null) {
            throw new IllegalArgumentException("fedoraUrl may not be null");
        }
        if (repositoryPath == null) {
            throw new IllegalArgumentException("repositoryPath may not be null");
        }
        this.fedoraUrl = fedoraUrl;
        this.rootUrl = fedoraUrl;
        if (!repositoryPath.startsWith("/")) {
            repositoryPath = '/' + repositoryPath;
        }
        if (!repositoryPath.endsWith("/") && repositoryPath.length() > 1) {
            repositoryPath += '/';
        }
        this.repositoryPath = repositoryPath;
    }

    /**
     * @param folder
     * @param processId
     * @param processTitle
     * @param pi
     * @param useVersioning If true, new versions of the existing resource will be added; if false, the resource will be deleted and created anew (all
     *            previous versions will be deleted).
     * @param dataFile
     * @param dataFolders
     */
    public boolean ingestData(int processId, String processTitle, String identifier, boolean useVersioning, Path dataFile,
            Map<String, Path> dataFolders) {
        imageDataList.clear();
        teiList.clear();
        cmdiList.clear();

        // Bsp.: http://middleware.gei.de/fcrepo/rest/WorldViews/sthe_B_01/

        Client client = ClientBuilder.newClient();
        WebTarget fedoraBase = client.target(fedoraUrl);
        // Create a new transaction in Fedora (POST operation)
        Response transactionResponse = fedoraBase.path("fcr:tx")
                .request()
                .post(null);
        if (transactionResponse.getStatus() < 400) {
            // The base URL to work with (contains the transaction ID)
            String transactionUrl = transactionResponse.getHeaderString("location");
            WebTarget ingestLocation = client.target(transactionUrl);

            // If not using versioning remove resource prior to ingesting to speed things up
            if (!useVersioning) {
                WebTarget recordContainer = ingestLocation.path(repositoryPath + identifier);
                if (!deleteResource(processId, processTitle, recordContainer)) {
                    return false;
                }
            }
            // Create the required container hierarchy for the process identifier
            String containerUrl = transactionUrl + repositoryPath + identifier;
            boolean containerCreated = createContainer(containerUrl);
            if (!containerCreated) {
                Helper.addMessageToProcessLog(processId, LogType.ERROR,
                        "The ingest into Fedora was not successful (container creation for " + containerUrl + ")");
                Helper.setFehlerMeldung(null, processTitle + ": ",
                        "The ingest into Fedora was not successful as the container could not be created for " + containerUrl);
                return false;
            }

            // Name for the new version, if using versioning
            String version = useVersioning ? "goobi-export." + formatter.print(System.currentTimeMillis()) : null;

            try {
                WebTarget recordUrl = ingestLocation.path(repositoryPath)
                        .path(identifier); // URL for the record folder

                // ingest data folders
                for (String label : dataFolders.keySet()) {
                    containerCreated = createContainer(containerUrl + "/" + label);
                    if (!containerCreated) {
                        Helper.addMessageToProcessLog(processId, LogType.ERROR,
                                "The ingest into Fedora was not successful (container creation for " + containerUrl + "media)");
                        Helper.setFehlerMeldung(null, processTitle + ": ",
                                "The ingest into Fedora was not successful as the container could not be created for " + containerUrl + "/media");
                        return false;
                    }
                    addFolderContent(dataFolders.get(label), label, transactionUrl, ingestLocation, version, recordUrl);
                }

                // ingest  "METS" file
                addFileResource(dataFile, recordUrl.path(dataFile.getFileName()
                        .toString()), version, transactionUrl);

                // Finish the entire ingest by committing the transaction
                ingestLocation.path("fcr:tx")
                        .path("fcr:commit")
                        .request()
                        .post(null);
            } catch (IOException | DAOException | InterruptedException | SwapException e) {
                // Roll back transaction, if anything fails
                log.error(e.getMessage(), e);
                ingestLocation.path("fcr:tx")
                        .path("fcr:rollback")
                        .request()
                        .post(null);
                Helper.addMessageToProcessLog(processId, LogType.ERROR,
                        "The ingest into Fedora was not successful and the transaction got rolled back: " + e.getMessage());
                Helper.setFehlerMeldung(null, processTitle + ": ",
                        "The ingest into Fedora was not successful and the transaction got rolled back: " + e.getMessage());
                return false;
            }
        }
        Helper.addMessageToProcessLog(processId, LogType.INFO, "Ingest into Fedora successfully finished.");
        Helper.setMeldung(null, processTitle + ": ", "ExportFinished");
        return true;
    }

    /**
     * Add the entire content of a given folder into fedora and put it all under a name that is passed over as parameter label
     * 
     * @param folder
     * @param label
     * @param transactionUrl
     * @param ingestLocation
     * @param version
     * @param recordUrl
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */
    private void addFolderContent(Path folder, String label, String transactionUrl, WebTarget ingestLocation, String version, WebTarget recordUrl)
            throws IOException, InterruptedException, SwapException, DAOException {
        WebTarget mediaUrl = recordUrl.path(label); // URL for the folder with the correct label
        List<Path> filesToIngest = StorageProvider.getInstance().listFiles(folder.toString());
        for (Path file : filesToIngest) {
            String fileUrl = addFileResource(file, mediaUrl.path(file.getFileName()
                    .toString()), version, transactionUrl);
            if (fileUrl != null) {
                imageDataList.add(fileUrl.replace(transactionUrl, fedoraUrl));
            }
            // Refresh transaction after each file to prevent timeouts
            ingestLocation.path("fcr:tx")
                    .request()
                    .post(null);
        }
    }

    /**
     * Delete a resource form fedora based on the container name
     * 
     * @param processId
     * @param processTitle
     * @param recordContainer the container name to delete
     * @return
     */
    public boolean deleteResource(int processId, String processTitle, WebTarget recordContainer) {
        // Check whether the container for this record already exists (GET operation; returns 200 if exists)
        Response response = recordContainer.request()
                .get();
        if (response.getStatus() == 200) {
            log.debug("Record container already exists: " + recordContainer.getUri()
                    .toString());
            // Delete the container (DELETE operation)
            response = recordContainer.request()
                    .delete();
            switch (response.getStatus()) {
                case 204:
                    // Each deleted resource leaves a tombstone which prevents a resource with the same name
                    // from being created, so the tombstone has to be deleted as well (DELETE operation)
                    response = recordContainer.path("fcr:tombstone")
                            .request()
                            .delete();
                    switch (response.getStatus()) {
                        case 204:
                            // Deleted successfully
                            log.debug("Record container deleted");
                            break;
                        default:
                            // Error occured while deleting the tombstone
                            String body = response.readEntity(String.class);
                            String msg = response.getStatus() + ": " + response.getStatusInfo()
                                    .getReasonPhrase() + " - " + body;
                            log.error(msg);
                            Helper.addMessageToProcessLog(processId, LogType.ERROR, "The ingest into Fedora was not successful: " + msg);
                            Helper.setFehlerMeldung(null, processTitle + ": ", "The ingest into Fedora was not successful: " + msg);
                            return false;
                    }
                    break;
                default:
                    // a general error occurred and gets logged
                    String body = response.readEntity(String.class);
                    String msg = response.getStatus() + ": " + response.getStatusInfo()
                            .getReasonPhrase() + " - " + body;
                    log.error(msg);
                    Helper.addMessageToProcessLog(processId, LogType.ERROR, "The ingest into Fedora was not successful: " + msg);
                    Helper.setFehlerMeldung(null, processTitle + ": ", "The ingest into Fedora was not successful: " + msg);
                    return false;
            }
        }
        return true;
    }

    /**
     * Adds the given binary file to Fedora
     * 
     * @param file File to add
     * @param target Target URL containing the transaction ID
     * @param version Version name, if using versioning; otherwise null
     * @param Transaction URL prefix used to remove transaction IDs from the final file location URL
     * @return File location URL in Fedora
     * @throws IOException
     */
    private String addFileResource(Path file, WebTarget target, String version, String transactionUrl) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file may not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("target may not be null");
        }

        // Check resource existence
        boolean exists = false;
        Response response = target.request()
                .get();
        if (response.getStatus() == 200) {
            exists = true;
            log.debug("Resource already exists: " + target.getUri()
                    .toURL()
                    .toString()
                    .replace(transactionUrl, fedoraUrl));
        }

        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            // Determine mime type using Java NIO
            String mimeType = Files.probeContentType(file);
            // If mime type could not be determined, use alternate method
            if (mimeType == null) {
                mimeType = URLConnection.guessContentTypeFromStream(inputStream);
            }
            // Manual fallback for Macs (using the file extension)
            if (mimeType == null) {
                String extension = FilenameUtils.getExtension(file.getFileName()
                        .toString());
                if (extension != null) {
                    switch (extension.toLowerCase()) {
                        case "tif":
                        case "tiff":
                            mimeType = "image/tiff";
                            break;
                        case "jpg":
                        case "jpeg":
                            mimeType = "image/jpeg";
                            break;
                        case "png":
                            mimeType = "image/png";
                            break;
                        case "xml":
                            mimeType = "text/xml";
                            break;
                        default:
                            mimeType = "text/html";
                            break;
                    }
                    log.debug("Manually determined mime type: " + mimeType);
                }
            }
            // Create HTTP entity from the file
            Entity<InputStream> fileEntity = Entity.entity(inputStream, mimeType);
            if (exists) {
                if (version != null) {
                    // Add new version (POST operation)
                    // "Slug" is the version name attribute
                    // "Content-Disposition" attribute contains the file name
                    response = target.path("fcr:versions")
                            .request()
                            .header("Slug", version)
                            .header("Content-Disposition", "attachment; filename=\"" + file.getFileName()
                                    .toString() + "\"")
                            .post(Entity.entity(inputStream, mimeType));
                } else {
                    // No versioning: Delete file so it can be replaced (DELETE operation)
                    // TODO This part is obsolete because the entire container is now deleted if it already exists (much faster)
                    response = target.request()
                            .delete();
                    if (response.getStatus() != 204) {
                        // Error
                        String body = response.readEntity(String.class);
                        String msg = response.getStatus() + ": " + response.getStatusInfo()
                                .getReasonPhrase() + " - " + body;
                        log.error(msg);
                        throw new IOException(msg);
                    }
                    // Delete tombstone (DELETE operation)
                    response = target.path("fcr:tombstone")
                            .request()
                            .delete();
                    if (response.getStatus() == 204) {
                        // Add file again (PUT operation)
                        // "Content-Disposition" attribute contains the file name
                        response = target.request()
                                .header("Content-Disposition", "attachment; filename=\"" + file.getFileName()
                                        .toString() + "\"")
                                .put(fileEntity);
                    } else {
                        // Error
                        String body = response.readEntity(String.class);
                        String msg = response.getStatus() + ": " + response.getStatusInfo()
                                .getReasonPhrase() + " - " + body;
                        log.error(msg);
                        throw new IOException(msg);
                    }
                }
            } else {
                // File does not exist yet, so just add it (PUT operation)
                // "Content-Disposition" attribute contains the file name
                response = target.request()
                        .header("Content-Disposition", "attachment; filename=\"" + file.getFileName()
                                .toString() + "\"")
                        .put(fileEntity);
            }
            // Handle response to the file adding operation (both versioned or not)
            switch (response.getStatus()) {
                case 201:
                    if (exists) {
                        if (version != null) {
                            // Successfully added new version
                            log.debug("New resource version " + version + " added: " + response.getHeaderString("location")
                                    .replace(transactionUrl, fedoraUrl));
                        } else {
                            // Successfully deleted and re-added file
                            log.debug("Resource updated: " + response.getHeaderString("location")
                                    .replace(transactionUrl, fedoraUrl));
                        }
                    } else {
                        // Added completely new file
                        log.debug("New resource added: " + response.getHeaderString("location")
                                .replace(transactionUrl, fedoraUrl));
                    }
                    break;
                default:
                    // Error
                    String body = response.readEntity(String.class);
                    log.error(response.getStatus() + ": " + response.getStatusInfo()
                            .getReasonPhrase() + " - " + body);
                    break;
            }

            return response.getHeaderString("location");
        }

    }

    /**
     * Creates the container hierarchy for the record (which is .../records/<record identifier>/media/). Containers along the path can be created
     * implicitly (i.e. creating "records/PPN123/media" will also create "/records" and "/records/PPN123"), but implicitly created containers have the
     * "pairtree" type and cannot contain binary documents. Therefore the containers for the record identifier and the media folder are created
     * explicitly here. Apache HTTP client is used here because it supports PUT operations without an entity.
     * 
     * @param rootUrl
     * @param identifier
     * @return
     */
    private static boolean createContainer(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
            // Create proper (non-pairtree) container for the record identifier
            HttpPut put = new HttpPut(url);
            // Create container (PUT operation with no entity - an empty entity will create an empty file instead)
            try (CloseableHttpResponse httpResponse = httpClient.execute(put); StringWriter writer = new StringWriter()) {
                switch (httpResponse.getStatusLine()
                        .getStatusCode()) {
                    case 201:
                        // Container created
                        log.info("Container created: " + url);
                        break;
                    case 204:
                    case 409:
                        // Container already exists
                        log.debug("Container already exists: " + url);
                        break;
                    default:
                        // Error
                        String body = IOUtils.toString(httpResponse.getEntity()
                                .getContent(), "UTF-8");
                        log.error(httpResponse.getStatusLine()
                                .getStatusCode() + ": "
                                + httpResponse.getStatusLine()
                                        .getReasonPhrase()
                                + " - " + body);
                        return false;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * Generates METS file from the given process.
     * 
     * @param process
     * @param destination Target path
     * @return
     * @throws UGHException
     * @throws DAOException
     * @throws InterruptedException
     * @throws IOException
     * @throws SwapException
     */
    private Path createMetsFile(Process process, String destination)
            throws UGHException, DAOException, InterruptedException, IOException, SwapException {
        Prefs prefs = process.getRegelsatz()
                .getPreferences();
        Fileformat fileformat = process.readMetadataFile();

        ExportFileformat mm = MetadatenHelper.getExportFileformatByName(process.getProjekt()
                .getFileFormatDmsExport(), process.getRegelsatz());
        mm.setWriteLocal(false);

        DigitalDocument dd = fileformat.getDigitalDocument();

        MetadatenImagesHelper mih = new MetadatenImagesHelper(prefs, dd);

        if (dd.getFileSet() == null || dd.getFileSet()
                .getAllFiles()
                .isEmpty()) {
            Helper.setMeldung(process.getTitel() + ": Digital document does not contain images; temporarily adding them for mets file creation");
            mih.createPagination(process, null);
        } else {
            mih.checkImageNames(process, null);
        }

        mm.setDigitalDocument(dd);

        VariableReplacer vp = new VariableReplacer(mm.getDigitalDocument(), prefs, process, null);

        VirtualFileGroup v = new VirtualFileGroup();
        v.setName("PRESENTATION");
        v.setPathToFiles(rootUrl);
        v.setMimetype("image/html-sandboxed"); // mime type used by Goobi viewer to identify external image resources
        //        v.setMimetype("image/tiff");
        v.setFileSuffix("tif"); // TODO File suffix as a method argument?
        mm.getDigitalDocument()
                .getFileSet()
                .addVirtualFileGroup(v);

        // Replace rights and digiprov entries.
        mm.setRightsOwner(vp.replace(process.getProjekt()
                .getMetsRightsOwner()));
        mm.setRightsOwnerLogo(vp.replace(process.getProjekt()
                .getMetsRightsOwnerLogo()));
        mm.setRightsOwnerSiteURL(vp.replace(process.getProjekt()
                .getMetsRightsOwnerSite()));
        mm.setRightsOwnerContact(vp.replace(process.getProjekt()
                .getMetsRightsOwnerMail()));
        mm.setDigiprovPresentation(vp.replace(process.getProjekt()
                .getMetsDigiprovPresentation()));
        mm.setDigiprovReference(vp.replace(process.getProjekt()
                .getMetsDigiprovReference()));
        mm.setDigiprovPresentationAnchor(vp.replace(process.getProjekt()
                .getMetsDigiprovPresentationAnchor()));
        mm.setDigiprovReferenceAnchor(vp.replace(process.getProjekt()
                .getMetsDigiprovReferenceAnchor()));

        mm.setMetsRightsLicense(vp.replace(process.getProjekt()
                .getMetsRightsLicense()));
        mm.setMetsRightsSponsor(vp.replace(process.getProjekt()
                .getMetsRightsSponsor()));
        mm.setMetsRightsSponsorLogo(vp.replace(process.getProjekt()
                .getMetsRightsSponsorLogo()));
        mm.setMetsRightsSponsorSiteURL(vp.replace(process.getProjekt()
                .getMetsRightsSponsorSiteURL()));

        mm.setPurlUrl(vp.replace(process.getProjekt()
                .getMetsPurl()));
        mm.setContentIDs(vp.replace(process.getProjekt()
                .getMetsContentIDs()));

        String pointer = process.getProjekt()
                .getMetsPointerPath();
        pointer = vp.replace(pointer);
        mm.setMptrUrl(pointer);

        String anchor = process.getProjekt()
                .getMetsPointerPathAnchor();
        pointer = vp.replace(anchor);
        mm.setMptrAnchorUrl(pointer);

        mm.setGoobiID(String.valueOf(process.getId()));
        Path tempFile = Files.createTempFile(process.getTitel(), ".xml");

        mm.write(tempFile.toString());

        overwriteUrls(tempFile.toString());
        Path metsFilePath = Paths.get(destination, process.getTitel() + ".xml");
        Files.copy(tempFile, metsFilePath, NIOFileUtils.STANDARD_COPY_OPTIONS);
        return metsFilePath;
    }

    /**
     * Replaces Goobi-generated file URLs in PRESENATATION (and FEDORA) METS file groups with URLs generated in this profile.
     * 
     * @param metsfile
     */
    private void overwriteUrls(String metsfile) {
        Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        SAXBuilder parser = new SAXBuilder();
        try {
            Document metsDoc = parser.build(metsfile);
            Element fileSec = metsDoc.getRootElement()
                    .getChild("fileSec", mets);

            for (Element fileGrp : fileSec.getChildren()) {
                if ("PRESENTATION"
                        .equals(fileGrp.getAttributeValue("USE"))
                        || "FEDORA"
                                .equals(fileGrp.getAttributeValue("USE"))) {
                    List<Element> fileList = fileGrp.getChildren();
                    for (int i = 0; i < fileList.size(); i++) {
                        Element file = fileList.get(i);
                        Element flocat = file.getChild("FLocat", mets);

                        // cut off the version path of the url
                        String myurl = imageDataList.get(i);
                        if (myurl.contains("/fcr:versions")) {
                            myurl = myurl.substring(0, myurl.indexOf("/fcr:versions"));
                        }
                        flocat.setAttribute("href", myurl, xlink);
                    }
                }
            }
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream(metsfile);
            outputter.output(metsDoc, output);
        } catch (JDOMException | IOException e) {
            log.error(e);
        }
    }

    public static void main(String[] args) {
        String fedoraUrl = "http://localhost:8088/rest";
        String processTitle = "DE_1953_Bendfeld_AbendlandStaatensystem_186";
        Map<String, Path> dataFolders = new HashMap<>();
        dataFolders.put("tei", Paths.get("c:/digiverso/viewer/hotfolder/" + processTitle + "_tei"));
        dataFolders.put("cmdi", Paths.get("c:/digiverso/viewer/hotfolder/" + processTitle + "_cmdi"));
        FedoraExport fe = new FedoraExport(fedoraUrl, "WorldViews/resources");
        fe.ingestData(0, processTitle, processTitle, false, Paths.get("c:/digiverso/viewer/hotfolder/" + processTitle + ".xml"), dataFolders);
    }
}
