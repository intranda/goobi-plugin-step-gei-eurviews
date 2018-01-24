package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.intranda.goobi.model.annotation.AnnotationMetadata;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.conversion.CMDIBuilder;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.util.Filters;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@PluginImplementation
public class ViewerTEIExportPlugin implements IExportPlugin {

    private static final Logger logger = Logger.getLogger(ViewerTEIExportPlugin.class);
    private static final String TITLE = "Gei_WorldViews_ViewerExport";

    private String destination = ConfigPlugins.getPluginConfig(this)
            .getString("targetFolder", "/opt/digiverso/viewer/hotfolder");

    private boolean exportOCR = true;
    private boolean exportImages = true;
    private Process process = null;

    private List<String> problems = new ArrayList<>();

    @Override
    public PluginType getType() {
        return PluginType.Export;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean startExport(Process process) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
            WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
            TypeNotAllowedForParentException {
        return startExport(process, this.destination);
    }

    @Override
    public boolean startExport(Process process, String destination) throws IOException, InterruptedException, DocStructHasNoTypeException,
            PreferencesException, WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
            SwapException, DAOException, TypeNotAllowedForParentException {
        this.process = process;
        Path destPath = Paths.get(destination);
        if (!destPath.toFile()
                .isDirectory()) {
            reportProblem("Destination path does not exist: " + destination);
            return false;
        }
        Path exportFilePath = destPath.resolve(process.getTitel() + ".xml");
        Path exportTeiPath = destPath.resolve(process.getTitel() + "_tei");
        Path exportCmdiPath = destPath.resolve(process.getTitel() + "_cmdi");
        Path exportImagesPath = destPath.resolve(process.getTitel() + "_tif");

        Path sourceTeiPath = Paths.get(process.getExportDirectory(), process.getTitel() + "_tei");
        if ((!sourceTeiPath.toFile()
                .isDirectory()
                || sourceTeiPath.toFile()
                        .listFiles(Filters.XmlFilter).length == 0)
                && exportOCR) {
            reportProblem("No TEI files found in " + sourceTeiPath);
            return false;
        }

        try {
            Document exportDoc = null;
            ResouceMetadata resourceMetadata = WorldViewsDatabaseManager.getResourceMetadata(process.getId());
            AnnotationMetadata annotationMetadata = WorldViewsDatabaseManager.getContributionDescription(process.getId());
            if (resourceMetadata != null) {
                //resource export
                List<Image> images = WorldViewsDatabaseManager.getImages(process.getId());
                BibliographicMetadata bibData = WorldViewsDatabaseManager.getBibliographicData(resourceMetadata.getBibliographicDataId());
                if (bibData == null) {
                    reportProblem("Not bibliographic data found for resource process " + resourceMetadata.getProcessId());
                    return false;
                }
                exportDoc = createResourceDocument(resourceMetadata, bibData, images);
            } else if (annotationMetadata != null) {
                //Annotation export
                List<Source> sources = WorldViewsDatabaseManager.getSourceList(process.getId());
                exportDoc = createAnnotationDocument(annotationMetadata, sources);
            } else {
                reportProblem("No WorldViews dataset associated with process " + process.getId());
                return false;
            }
            StringBuilder sb = new StringBuilder();
            try {
                writeDocument(exportDoc, exportFilePath);
                sb.append("Successfully copied main data file to " + exportFilePath)
                        .append("\n");
                if (exportOCR && sourceTeiPath.toFile()
                        .isDirectory()
                        && sourceTeiPath.toFile()
                                .list().length > 0) {
                    copyTEIAndCreateCMDI(sourceTeiPath, exportTeiPath, exportCmdiPath);
                    sb.append("Successfully copied TEI data to ")
                            .append(exportTeiPath)
                            .append("\n");
                }

                Path sourceImagesPath = Paths.get(process.getImagesTifDirectory(false));
                if (exportImages && sourceImagesPath.toFile()
                        .isDirectory()
                        && sourceImagesPath.toFile()
                                .list().length > 0) {
                    copyImages(sourceImagesPath, exportImagesPath);
                    sb.append("Successfully copied image data to ")
                            .append(exportImagesPath)
                            .append("\n");

                }
                sb.append("Export to viewer completed. Please check the viewer itself for the indexing results.\n");

                // Export to Fedora
                String fedoraUrl = ConfigPlugins.getPluginConfig(this)
                        .getString("fedoraUrl");
                String resourcePath = ConfigPlugins.getPluginConfig(this)
                        .getString("fedoraResourcePath");
                boolean useVersioning = ConfigPlugins.getPluginConfig(this)
                        .getBoolean("useVersioning", true);
                Map<String, Path> dataFolders = new HashMap<>();
                dataFolders.put("tei", exportTeiPath);
                dataFolders.put("cmdi", exportCmdiPath);
                FedoraExport fe = new FedoraExport(fedoraUrl, resourcePath);
                if (fe.ingestData(process.getId(), process.getTitel(), process.getTitel(), useVersioning, exportFilePath, dataFolders)) {
                    sb.append("Export to Fedora repository '")
                            .append(fedoraUrl)
                            .append("' completed.");
                } else {
                    reportProblem("Export to Fedora repository '" + fedoraUrl + "' failed.");
                }

                writeToGoobiLog(sb.toString(), LogType.INFO);
                return true;
            } catch (IOException e) {
                reportProblem("Error creating export files: " + e.getMessage());
                if (exportFilePath.toFile()
                        .isFile()) {
                    exportFilePath.toFile()
                            .delete();
                }
                if (exportTeiPath.toFile()
                        .isDirectory()) {
                    FileUtils.deleteDirectory(exportTeiPath.toFile());
                }
                if (exportImagesPath.toFile()
                        .isDirectory()) {
                    FileUtils.deleteDirectory(exportImagesPath.toFile());
                }
                return false;
            }

        } catch (SQLException e) {
            reportProblem("Error accessing database: " + e.getMessage());
            return false;
        }

    }

    private void writeToGoobiLog(String message, LogType logType) {
        if (this.process != null) {
            message = message.replace("\n", "<br />");
            LogEntry errorEntry = new LogEntry();
            errorEntry.setContent(message);
            errorEntry.setType(logType);
            this.process.getProcessLog()
                    .add(errorEntry);
            errorEntry.setCreationDate(new Date());
            errorEntry.setProcessId(this.process.getId());
            ProcessManager.saveLogEntry(errorEntry);
        }
    }

    /**
     * 
     * @param sourceTeiPath
     * @param exportTeiPath
     * @param exportCmdiPath
     * @throws IOException
     */
    private void copyTEIAndCreateCMDI(Path sourceTeiPath, Path exportTeiPath, Path exportCmdiPath) throws IOException {
        if (!exportTeiPath.toFile()
                .isDirectory()
                && !exportTeiPath.toFile()
                        .mkdir()) {
            throw new IOException("Unable to create directory " + exportTeiPath);
        }
        if (!exportCmdiPath.toFile()
                .isDirectory()
                && !exportCmdiPath.toFile()
                        .mkdir()) {
            throw new IOException("Unable to create directory " + exportCmdiPath);
        }
        File[] teiFiles = sourceTeiPath.toFile()
                .listFiles(Filters.XmlFilter);
        for (File file : teiFiles) {
            Files.copy(Paths.get(file.getAbsolutePath()), exportTeiPath.resolve(file.getName()));

            // Create CMDI
            try {
                Document teiDoc = readXmlFileToDoc(file);
                Document cmdiDoc = CMDIBuilder.convertToCMDI(process.getTitel(), teiDoc);
                if (cmdiDoc != null) {
                    // logger.debug(CMDIBuilder.getStringFromElement(cmdiDoc, null));
                    String fileNameSuffix = file.getName()
                            .substring(file.getName()
                                    .length() - 7, file.getName()
                                            .length());
                    Path cmdiFilePath = Paths.get(exportCmdiPath.toAbsolutePath()
                            .toString(), process.getTitel() + fileNameSuffix);
                    logger.debug(cmdiFilePath.toAbsolutePath()
                            .toString());
                    writeDocument(cmdiDoc, cmdiFilePath);
                    logger.info("CMDI file written: " + cmdiFilePath.getFileName()
                            .toString());
                } else {
                    logger.error("Could not create CMDI");
                }
            } catch (JDOMException e) {
                throw new IOException(e);
            }

        }
    }

    private static void copyImages(Path sourceImagePath, Path exportImagePath) throws IOException {
        if (!exportImagePath.toFile()
                .isDirectory()
                && !exportImagePath.toFile()
                        .mkdir()) {
            throw new IOException("Unable to create directory " + exportImagePath);
        }
        File[] imageFiles = sourceImagePath.toFile()
                .listFiles(new ResourceDescriptionPlugin.ImageFilter());
        for (File file : imageFiles) {
            Files.copy(Paths.get(file.getAbsolutePath()), exportImagePath.resolve(file.getName()));
        }
    }

    private static void writeDocument(Document exportDoc, Path exportFilePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(exportFilePath.toFile())) {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(exportDoc, fileWriter);
        }
    }

    private static Document createAnnotationDocument(AnnotationMetadata annotationMetadata, List<Source> sourceList) {
        Document doc = new Document();
        Element root = new Element("worldviews");
        doc.setRootElement(root);

        Element annotation = new Element("annotation");
        root.addContent(annotation);

        Element type = new Element("docType");
        if (annotationMetadata.getContributionType()
                .equals("Bildungsgeschichte")) {
            type.setText("FormationHistory");
        } else {
            type.setText("Comment");
        }
        annotation.addContent(type);

        Element identifier = new Element("identifier");
        identifier.setText(ProcessManager.getProcessTitle(annotationMetadata.getProcessId()));
        annotation.addContent(identifier);

        for (String dc : annotationMetadata.getDigitalCollections()) {
            Element collection = new Element("collection");
            collection.setText(dc);
            annotation.addContent(collection);
        }

        for (Source source : sourceList) {
            if (source.getData() != null && source.getData()
                    .getProcessId() != null) {
                String sourceProcessTitle = ProcessManager.getProcessTitle(source.getData()
                        .getProcessId());
                if (StringUtils.isNotBlank(sourceProcessTitle)) {
                    Element relatedItem = new Element("relatedItem");
                    if (source.isMainSource()) {
                        relatedItem.setAttribute("type", "primarySource");
                    } else {
                        relatedItem.setAttribute("type", "secondarySource");
                    }
                    Element relatedIdentifier = new Element("identifier");
                    relatedIdentifier.setText(ProcessManager.getProcessTitle(source.getData()
                            .getProcessId()));
                    relatedItem.addContent(relatedIdentifier);
                    annotation.addContent(relatedItem);
                }
            }
        }

        return doc;
    }

    private static Document createResourceDocument(ResouceMetadata resourceMetadata, BibliographicMetadata bibData, List<Image> imageList) {
        Document doc = new Document();
        Element root = new Element("worldviews");
        doc.setRootElement(root);

        Element resource = new Element("resource");
        root.addContent(resource);

        resource.addContent(createElement("docType", "Source"));

        Element identifier = new Element("identifier");
        identifier.setText(ProcessManager.getProcessTitle(resourceMetadata.getProcessId()));
        resource.addContent(identifier);

        Element relatedItem = new Element("relatedItem");
        relatedItem.setAttribute("type", "schoolbook");
        resource.addContent(relatedItem);

        Element relatedIdentifier = new Element("identifier");
        relatedIdentifier.setText(ProcessManager.getProcessTitle(bibData.getProzesseID()));
        relatedItem.addContent(relatedIdentifier);

        for (String dc : resourceMetadata.getDigitalCollections()) {
            Element collection = new Element("collection");
            collection.setText(dc);
            resource.addContent(collection);
        }

        Element images = new Element("images");
        resource.addContent(images);
        for (Image digiImage : imageList) {
            Element image = new Element("image");
            image.addContent(createElement("fileName", digiImage.getFileName()));
            image.addContent(createElement("sequence", Integer.toString(digiImage.getOrder())));
            image.addContent(createElement("structType", digiImage.getStructType()));
            image.addContent(createElement("displayImage", Boolean.toString(digiImage.isDisplayImage())));
            image.addContent(createElement("licence", digiImage.getLicence()));
            image.addContent(createElement("representative", Boolean.toString(digiImage.isRepresentative())));
            image.addContent(createElement("copyright", digiImage.getCopyright()));
            image.addContent(createElement("placeholder", digiImage.getPlaceholder()));
            images.addContent(image);
        }

        return doc;
    }

    /**
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JDOMException
     * @should read XML file correctly
     * @should throw IOException if file not found
     */
    public static Document readXmlFileToDoc(File file) throws FileNotFoundException, IOException, JDOMException {
        try (FileInputStream fis = new FileInputStream(file)) {
            org.jdom2.Document doc = new SAXBuilder().build(fis);
            return doc;
        }
    }

    private static Element createElement(String name, String text) {
        Element ele = new Element(name);
        ele.setText(text);
        return ele;
    }

    private void reportProblem(String string) {
        this.problems.add(string);
        writeToGoobiLog(string, LogType.ERROR);
    }

    @Override
    public void setExportFulltext(boolean exportFulltext) {
        this.exportOCR = exportFulltext;

    }

    @Override
    public void setExportImages(boolean exportImages) {
        this.exportImages = exportImages;

    }

    @Override
    public List<String> getProblems() {
        return problems;
    }
}
