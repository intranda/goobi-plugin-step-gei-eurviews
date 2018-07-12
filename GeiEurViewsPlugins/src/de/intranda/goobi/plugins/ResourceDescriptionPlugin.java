package de.intranda.goobi.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.JDOMException;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.EurViewsRecord;
import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.Language;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.NormdataEntity;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SourceType;
import de.intranda.goobi.model.SourceTypeHelper;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.ResourceMetadataBuilder;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.normdata.NormdataSearch;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;
import de.unigoettingen.sub.commons.util.Filters;
import lombok.Data;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public @Data class ResourceDescriptionPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceDescriptionPlugin.class);

    private Step step;
    private Process process;
    private String returnPath;
    private static final String PLUGIN_NAME = "Gei_WorldViews_ResourceDescription";
    private static final String GUI_PATH = "/Gei_WorldViews_ResourceDescriptionPlugin.xhtml";
    private static final String USER_GROUP_NAME = "Schlagworterfassung";
    private static final String IMAGE_REFERENCE = "image-view";

    private int imageSizeInPixel = 300;

    private String imageFolder;

    private List<SourceType> possibleTypes = new ArrayList<>(38);
    private List<String> possibleImageDocStructs;
    private List<String> possibleLanguages;
    private List<String> possiblePersons;
    private List<String> possiblePublisher;
    private List<String> possiblePlaceholder;
    private List<String> possibleLicences;

    private List<Image> currentImages;
    private Image image = null;
    private int imageIndex = 0;

    private ResouceMetadata data;

    private List<Context> descriptionList;
    private Context currentDescription;
    private Context referenceDescription;

    private List<Transcription> transcriptionList;
    private Transcription currentTranscription;
    private String currentTranscriptionLanguage;
    private Transcription referenceTranscription;
    private String referenceTranscriptionLanguage = IMAGE_REFERENCE;

    private List<Language> searchedLanguages;

    private List<Topic> topicList = new ArrayList<>();
    private NormdataSearch search;

    private boolean edition = false;

    private String displayMode = "";

    private String german;
    private String english;
    private String french;

    private String index;
    private String rowType;
    private String searchDatabase;

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    public String getDescription() {
        return PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Step step, String returnPath) {

        this.step = step;
        this.process = step.getProzess();
        this.returnPath = returnPath;
        this.search = new NormdataSearch(ConfigPlugins.getPluginConfig(this));
        try {
            data = WorldViewsDatabaseManager.getResourceMetadata(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        // TODO tempoary fix, remove after creation works
        if (data == null) {
            data = new ResouceMetadata(process.getId());
        }

        topicList = KeywordHelper.getInstance().initializeKeywords();
        possibleTypes = SourceTypeHelper.getInstance().initializeResourceTypes();

        // possibleTypes =
        // ConfigPlugins.getPluginConfig(this).getList("elements.docstruct");
        possibleImageDocStructs = ConfigPlugins.getPluginConfig(this).getList("images.docstruct");
        possibleLanguages = ConfigPlugins.getPluginConfig(this).getList("elements.language");

        possiblePersons = ConfigPlugins.getPluginConfig(this).getList("elements.person");
        possiblePublisher = ConfigPlugins.getPluginConfig(this).getList("elements.publisher");
        possiblePlaceholder = ConfigPlugins.getPluginConfig(this).getList("elements.placeholder");
        possibleLicences = ConfigPlugins.getPluginConfig(this).getList("licences.licence");

        try {
            imageFolder = process.getImagesTifDirectory(true);

        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            logger.error(e);
        }

        try {
            currentImages = WorldViewsDatabaseManager.getImages(process.getId());

            List<StringPair> keyowrdList = WorldViewsDatabaseManager.getKeywordList(process.getId());
            for (StringPair sp : keyowrdList) {
                for (Topic topic : topicList) {
                    if (topic.getNameDE().equals(sp.getOne())) {
                        for (Keyword keyword : topic.getKeywordList()) {
                            if (keyword.getKeywordNameDE().equals(sp.getTwo())) {
                                keyword.setSelected(true);
                                break;
                            }
                        }

                    }
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        if (currentImages == null || currentImages.isEmpty()) {
            readImages();
        }

        // create thumbnail images
        for (Image currentImage : currentImages) {
            createImage(currentImage);
        }
        if (!currentImages.isEmpty()) {
            setImage(currentImages.get(0));
        }

        try {
            descriptionList = WorldViewsDatabaseManager.getDescriptionList(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if (descriptionList.isEmpty()) {
            descriptionList.add(new Context(process.getId(), getPossibleLanguages().get(0)));
        }
        this.currentDescription = descriptionList.get(0);
        if (this.descriptionList.size() > 1) {
            this.referenceDescription = this.descriptionList.get(1);
        } else {
            this.referenceDescription = this.currentDescription;
        }
        for (Context description : descriptionList) {
            setDefaultValues(description);
        }

        try {
            transcriptionList = WorldViewsDatabaseManager.getTransciptionList(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if (transcriptionList.isEmpty()) {
            transcriptionList.add(new Transcription(process.getId()));
            transcriptionList.get(0).setLanguage(getPossibleLanguages().get(0));
        }
        this.currentTranscription = transcriptionList.get(0);
        this.currentTranscriptionLanguage = transcriptionList.get(0).getLanguage();
        for (Transcription transcription : transcriptionList) {
            setDefaultValues(transcription);
        }

        if (transcriptionList.size() > 1) {
            this.referenceTranscription = transcriptionList.get(1);
        } else {
            this.referenceTranscription = new Transcription(getProcess().getId());
        }

        User user = Helper.getCurrentUser();
        for (Usergroup ug : user.getBenutzergruppen()) {
            if (ug.getTitel().equalsIgnoreCase(USER_GROUP_NAME)) {
                edition = true;
            }
        }

        if (data.getResourceAuthorList().isEmpty() && data.getBibliographicDataId() != null) {
            BibliographicMetadata bm;
            try {
                bm = WorldViewsDatabaseManager.getBibliographicData(data.getBibliographicDataId());

                if (!bm.getPersonList().isEmpty()) {
                    for (Person author : bm.getPersonList()) {
                        Person per = new Person();
                        per.setFirstName(author.getFirstName());
                        per.setLastName(author.getLastName());
                        per.setNormdata(author.getNormdata());
                        per.setRole(author.getRole());

                        data.addToResourceAuthorList(per);
                    }
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        }

        if (StringUtils.isBlank(data.getPublicationYearDigital())) {
            data.setPublicationYearDigital(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        }

    }

    /**
     * 
     */
    public void readImages() {
        if (logger.isDebugEnabled()) {
            logger.debug("create new image set from folder " + imageFolder);
        }
        currentImages = new ArrayList<Image>();
        String[] imageNameArray = new File(imageFolder).list(new ImageFilter());
        if (imageNameArray != null && imageNameArray.length > 0) {
            List<String> imageNameList = Arrays.asList(imageNameArray);
            Collections.sort(imageNameList);
            int order = 1;
            for (String imagename : imageNameList) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Add image to image set: " + imagename);
                }
                Image currentImage = new Image(process.getId());
                currentImage.setFileName(imagename);
                currentImage.setOrder(order++);
                currentImages.add(currentImage);
            }
        }
    }

    public void associateImages() throws IOException, InterruptedException, SwapException, DAOException, URISyntaxException {
        if (logger.isDebugEnabled()) {
            logger.debug("Associating image list with image files");
        }
        if (currentImages != null && !currentImages.isEmpty()) {
            String[] imageNameArray = new File(imageFolder).list(new ImageFilter());
            if (imageNameArray != null && imageNameArray.length > 0) {
                List<String> imageNameList = Arrays.asList(imageNameArray);
                Collections.sort(imageNameList);
                Iterator<Image> imageIterator = this.currentImages.iterator();
                for (String imagename : imageNameList) {
                    if (imageIterator.hasNext()) {
                        Image image = imageIterator.next();
                        image.setFileName(imagename);
                    }
                }
                while(imageIterator.hasNext()) {
                    imageIterator.next();
                    imageIterator.remove();
                }
            } else {
                throw new IOException("No images found in image folder");
//                SourceInitializationPlugin.downloadImages(currentImages, getProcess());
            }
        }
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public String cancel() {
        return "/" + Helper.getTheme() + returnPath;
    }

    @Override
    public String finish() {
        return "/" + Helper.getTheme() + returnPath;
    }

    public void save() {
        try {
            for (Context context : getDescriptionList()) {
                setDefaultValues(context);
            }
            for (Transcription transcription : getTranscriptionList()) {
                setDefaultValues(transcription);
            }
            WorldViewsDatabaseManager.saveResouceMetadata(data);
            if(unsavedImagesExist(currentImages)) {
                unsaveAllImages(currentImages);
                WorldViewsDatabaseManager.deleteImages(data);
            }
            WorldViewsDatabaseManager.saveImages(currentImages);
            WorldViewsDatabaseManager.saveDesciptionList(descriptionList);
            WorldViewsDatabaseManager.saveTranscriptionList(transcriptionList);
            WorldViewsDatabaseManager.saveKeywordList(topicList, process.getId());
            Helper.setMeldung("dataSavedSuccessfully");
        } catch (SQLException e) {
            logger.error(e);
            Helper.setFehlerMeldung("dataCouldNotBeSaved", e);
        }
    }

    private void unsaveAllImages(List<Image> images) {
        for (Image image : images) {
            image.setImageId(null);
        }
        
    }

    private boolean unsavedImagesExist(List<Image> images) {
        for (Image image : images) {
            if(image.getImageId() == null) {
                return true;
            }
        }
        return false;
    }

    private void createImage(Image image) {
        String fileName = image.getFileName();
        // /* Pages-Verzeichnis ermitteln */
        String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

        // /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String mySession = session.getId() + "_" + getProcess().getId() + "_" + fileName + ".png";

        try {
            System.out.println("Creating thumbnail image " + myPfad + mySession);
            float scale = scaleFile(imageFolder + fileName, myPfad + mySession, imageSizeInPixel);
            image.setScale(scale);
        } catch (ContentLibException | IOException e) {
            logger.error(e);
        }

    }


    /**
     * 
     * @param inFileName
     * @param outFileName
     * @param size
     * @return the factor by which the image was scaled
     * @throws IOException
     * @throws ContentLibException
     */
    private float scaleFile(String inFileName, String outFileName, int size) throws IOException, ContentLibException {
        ImageManager im = null;
        JpegInterpreter pi = null;
        FileOutputStream outputFileStream = null;
        try {
            im = new ImageManager(new File(inFileName).toURI());
            Dimension dim = new Dimension();
            dim.setSize(size, size);
            float originalHeight = im.getMyInterpreter().getHeight();
            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
            pi = new JpegInterpreter(ri);
            outputFileStream = new FileOutputStream(outFileName);
            pi.writeToStream(null, outputFileStream);
            return originalHeight / (float) size;
        } finally {
            if (im != null) {
                im.close();
            }
            if (pi != null) {
                pi.close();
            }
            if (outputFileStream != null) {
                outputFileStream.close();
            }
        }
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public Step getStep() {
        return step;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
    }

    @Override
    public String getPagePath() {
        return "/" + Helper.getTheme() + GUI_PATH;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
        if (this.imageIndex < 0) {
            this.imageIndex = 0;
        }
        if (this.imageIndex > getSizeOfImageList()) {
            this.imageIndex = getSizeOfImageList();
        }
        setImage(currentImages.get(this.imageIndex));
    }

    public int getSizeOfImageList() {
        if (currentImages.isEmpty()) {
            return 0;
        }
        return currentImages.size() - 1;
    }

    public void setImage(Image image) {
        this.image = image;
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String currentImageURL = ConfigurationHelper.getTempImagesPathAsCompleteDirectory() + session.getId() + "_" + image.getFileName() + "_large_"
                + ".png";
        try {
            if (currentImageURL != null) {
                float scale = scaleFile(imageFolder + image.getFileName(), currentImageURL, 800);
                image.setScale(scale);
            }
        } catch (ContentLibException | IOException e) {
            logger.error(e);
        }
    }

    public String getBild() {
        if (image == null) {
            return null;
        } else {
            FacesContext context = FacesContextHelper.getCurrentFacesContext();
            String baseUrl = getServletPathWithHostAsUrlFromJsfContext();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            String currentImageURL = baseUrl + ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + image.getFileName() + "_large_"
                    + ".png";
            return currentImageURL;
        }
    }

    public String getBildIIIFUrl() throws IOException, InterruptedException, SwapException, DAOException {
        if (image != null) {
            StringBuilder sb = new StringBuilder(ConfigPlugins.getPluginConfig(this).getString("presentation.url", "http://localhost:8080/viewer"));
            sb.append("/iiif/image/");
            sb.append(getProcess().getTitel()).append("/");
            sb.append(image.getFileName());
            sb.append("/{region}/full/0/default.jpg");
            return sb.toString();
        } else {
            return null;
        }
    }

    public float getImageScale() {
        if (image != null) {
            return image.getScale();
        } else {
            return 1f;
        }
    }

    public boolean isImageHasOcr() {
        if (image != null) {
            String ocrFile = image.getFileName().substring(0, image.getFileName().lastIndexOf(".")) + ".txt";
            return FilesystemHelper.isOcrFileExists(process, ocrFile);
        } else {
            return false;
        }
    }

    public String getOcrForImage() {
        String ocrFile = image.getFileName().substring(0, image.getFileName().lastIndexOf(".")) + ".txt";
        return FilesystemHelper.getOcrFileContent(process, ocrFile);
    }

    public String getOcrForAllSources() {
        String ocrResult = "";
        for (Image myimage : currentImages) {
            if (myimage.getStructType().equals("Quelle")) {
                String ocrFile = myimage.getFileName().substring(0, myimage.getFileName().lastIndexOf(".")) + ".txt";
                ocrResult += FilesystemHelper.getOcrFileContent(process, ocrFile);
                ocrResult += "<br/>";
            }
        }
        return ocrResult;
    }

    public int getSizeOfTranscriptionList() {
        return transcriptionList.size();
    }

    // public void addTranscription() {
    // transcriptionList.add(new Transcription(process.getId()));
    // }
    //
    //
    // public void deleteTranscription() {
    // if (transcriptionList.contains(currentTranscription)) {
    // transcriptionList.remove(currentTranscription);
    // }
    // try {
    // DatabaseManager.deleteTranscription(currentTranscription);
    // } catch (SQLException e) {
    // logger.error(e);
    // }
    // if(transcriptionList.isEmpty()) {
    // transcriptionList.add(new Transcription(process.getId()));
    // }
    // currentTranscription = transcriptionList.get(0);
    // }

    public void resetValues() {
        german = "";
        french = "";
        english = "";
    }

    public String search() {
        //        String database = "gnd";
        //        ComplexMetadataObject object = getSelectedObject();
        //        if (object != null && StringUtils.isNotBlank(object.getNormdataAuthority())) {
        //            database = object.getNormdataAuthority();
        //        }
        return search.search(searchDatabase);
    }

    protected String filter(String str) {
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            // current != 0x152 && current != 0x156
            if (current != 0x98 && current != 0x9C) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }

    public String getData(List<NormData> currentData) {

        ComplexMetadataObject metadata = getSelectedObject();
        metadata.resetNormdataValues();

        if (metadata instanceof Person) {
            Person person = (Person) metadata;
            for (NormData normdata : currentData) {
                if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                    person.setNormdataId("gnd", normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("NORM_IDENTIFIER_EDU_EXPERTS")) {
                    person.setNormdataId("edu.experts", normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("URI")) {
                    person.setNormdataUri("gnd", normdata.getValues().get(0).getText());
                    if (StringUtils.isBlank(person.getNormdataValue("gnd"))) {
                        String uri = normdata.getValues().get(0).getText();
                        int idIndex = uri.lastIndexOf("/");
                        if (idIndex > -1 && idIndex < uri.length() - 1) {
                            person.setNormdataId("gnd", uri.substring(idIndex + 1));
                        }
                    }
                } else if (normdata.getKey().equals("URI_EDU_EXPERTS")) {
                    person.setNormdataUri("edu.experts", normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("NORM_NAME")) {
                    String value = normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "");
                    value = filter(value);
                    if (value.contains(",")) {
                        person.setLastName(value.substring(0, value.indexOf(",")).trim());
                        person.setFirstName(value.substring(value.indexOf(",") + 1).trim());
                    } else if (value.contains(" ")) {
                        String[] nameParts = value.split(" ");
                        String first = "";
                        String last = "";
                        if (nameParts.length == 1) {
                            last = nameParts[0];
                        } else if (nameParts.length == 2) {
                            first = nameParts[0];
                            last = nameParts[1];
                        } else {
                            int counter = nameParts.length;
                            for (int i = 0; i < counter; i++) {
                                if (i == counter - 1) {
                                    last = nameParts[i];
                                } else {
                                    first += " " + nameParts[i];
                                }
                            }
                        }
                        person.setLastName(last);
                        person.setFirstName(first);
                    } else {
                        person.setLastName(value);
                    }
                }
            }
        } else if (metadata instanceof Corporation) {
            Corporation person = (Corporation) metadata;
            getPublisherData(person, currentData);
        }
        if (search.addEduExpertsNormdata(metadata)) {
            logger.debug("Added edu.experts normdata");
        }
        return "";
    }

    /**
     * @return
     */
    private ComplexMetadataObject getSelectedObject() {
        ComplexMetadataObject metadata = null;
        if (rowType.equals("resourceAuthor")) {
            metadata = data.getResourceAuthorList().get(Integer.parseInt(index));
        }
        return metadata;
    }

    public String getPublisherData(Corporation person, List<NormData> currentData) {
        for (NormData normdata : currentData) {
            if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                person.setNormdataId("gnd", normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("NORM_NAME")) {
                person.setName(filter(normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "")));
            } else if (normdata.getKey().equals("NORM_IDENTIFIER_EDU_EXPERTS")) {
                person.setNormdataId("edu.experts", normdata.getValues().get(0).getText());
            }
        }

        return "";
    }

    public String searchGeonames() {
        return search.searchGeonames();
    }

    // public String getGeonamesData(Toponym currentToponym) {
    // Location loc = data.getCountryList().get(Integer.parseInt(index));
    // loc.setName(currentToponym.getName());
    // loc.setNormdataAuthority("geonames");
    // loc.setNormdataValue("" + currentToponym.getGeoNameId());
    // return "";
    // }

    public String getGeonamesUrl(Location loc) {
        return loc.getNormdataUri("geonames");
        //        if (StringUtils.isBlank(loc.getNormdataValue())) {
        //            return null;
        //        } else {
        //            return "http://www.geonames.org/" + loc.getNormdataValue();
        //        }
    }

    public Context getFirstContext() {
        return descriptionList.get(0);
    }

    public Context getSecondContext() {
        return descriptionList.get(1);
    }

    /**
     * 
     * 
     * @return a list of language Strings of transcriptions as well as the string "image" to represent the image display
     */
    public List<String> getPossibleTranscriptionReferences() {
        List<String> list = new ArrayList<>();
        for (Transcription transcription : transcriptionList) {
            list.add(transcription.getLanguage());
        }
        list.add(IMAGE_REFERENCE);
        return list;
    }

    public void setReferenceTranscriptionLanguage(String language) {
        this.referenceTranscriptionLanguage = language;
        if (!language.equals(IMAGE_REFERENCE)) {
            setReferenceTranscription(getTranscription(language));
        }
    }

    public void setCurrentTranscriptionLanguage(String language) {
        this.currentTranscriptionLanguage = language;
        this.currentTranscription = getTranscription(language);
        if (this.currentTranscription == null) {
            this.currentTranscription = new Transcription(getProcess().getId());
            this.currentTranscription.setLanguage(language);
            this.transcriptionList.add(this.currentTranscription);
            setDefaultValues(this.currentTranscription);
        }
    }

    private void setDefaultValues(Transcription transcription) {
        if (StringUtils.isBlank(transcription.getAvailability())) {
            String keyAvailability = "default.{lang}.availability".replace("{lang}", transcription.getLanguageCode());
            transcription.setAvailability(ConfigPlugins.getPluginConfig(this).getString(keyAvailability, ""));
        }
    }

    private void setDefaultValues(Context description) {

        String keyProjectDesc = "default.{lang}.projectDesc".replace("{lang}", description.getLanguageCode());
        if (StringUtils.isBlank(description.getProjectContext())) {
            description.setProjectContext(ConfigPlugins.getPluginConfig(this).getString(keyProjectDesc, ""));
        }

        String keySamplingDecl = "default.{lang}.sampling".replace("{lang}", description.getLanguageCode());
        if (StringUtils.isBlank(description.getSelectionMethod())) {
            description.setSelectionMethod(ConfigPlugins.getPluginConfig(this).getString(keySamplingDecl, ""));
        }
    }

    public void setCurrentDescriptionLanguage(String language) {
        this.currentDescription = getDescription(language);
        if (this.currentDescription == null) {
            this.currentDescription = new Context(getProcess().getId(), language);
            this.descriptionList.add(this.currentDescription);
            setDefaultValues(this.currentDescription);
        }
    }

    public String getCurrentDescriptionLanguage() {
        return this.currentDescription.getLanguage();
    }

    public void setReferenceDescriptionLanguage(String language) {
        this.referenceDescription = getDescription(language);
        if (this.referenceDescription == null) {
            throw new IllegalStateException("Set reference description to a non-existing language");
        }
    }

    public String getReferenceDescriptionLanguage() {
        return this.referenceDescription.getLanguage();
    }

    public List<String> getPossibleDescriptionReferences() {
        List<String> list = new ArrayList<>();
        for (Context context : this.descriptionList) {
            list.add(context.getLanguage());
        }
        return list;
    }

    /**
     * 
     * @param language
     * @return the first context/description with the given language. Or null if no such context exists
     */
    private Context getDescription(String language) {
        for (Context context : this.descriptionList) {
            if (context.getLanguage().equals(language)) {
                return context;
            }
        }
        return null;
    }

    /**
     * 
     * @param language
     * @return the first transcription with the given language. Or null if no such transcription exists
     */
    private Transcription getTranscription(String language) {
        for (Transcription transcription : this.transcriptionList) {
            if (transcription.getLanguage().equals(language)) {
                return transcription;
            }
        }
        return null;
    }

    public static String getServletPathWithHostAsUrlFromJsfContext() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request != null) {
                return getServletPathWithHostAsUrlFromRequest(request);
            }
        }

        return "";
    }

    public String searchLanguage() {
        return search.searchLanguage();
    }

    public String getLanguageData(Language currentLanguage) {

        switch (rowType) {
            case "languageTranscription":
                currentTranscription.setLanguageCode(currentLanguage.getIsoCode_639_2());
                break;
            case "languageContext":
                currentDescription.setLanguageCode(currentLanguage.getIsoCode_639_2());
                break;
            case "languageResourceTitle":
                data.getResourceTitle().setLanguage(currentLanguage.getIsoCode_639_2());
        }

        return "";
    }

    public static String getServletPathWithHostAsUrlFromRequest(HttpServletRequest request) {
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath(); // /mywebapp
        if (serverPort != 80) {
            return scheme + "://" + serverName + ":" + serverPort + contextPath;
        }
        return scheme + "://" + serverName + contextPath;
    }

    public List<String> getPossibleDigitalCollections() {
        try {
            return DigitalCollections.possibleDigitalCollectionsForProcess(getProcess());
        } catch (JDOMException | IOException e) {
            logger.error(e);
            return Collections.singletonList(getDefaultDigitalCollection());
        }
    }

    public String getDefaultDigitalCollection() {
        return ConfigPlugins.getPluginConfig(this).getString("default.digitalCollection", "WorldViews");
    }

    public void createEduExpertsEntry(ComplexMetadataObject metadata) {
        NormdataEntity entity = metadata.getNormdata("edu.experts");
        if (StringUtils.isBlank(entity.getId())) {

        }
    }

    public boolean isNotBlank(String string) {
        return StringUtils.isNotBlank(string);
    }

    public void resetImages() {
        EurViewsRecord record;
        try {
            record = SourceInitializationPlugin.createRecord(getProcess());
            if (record != null) {
                this.currentImages = SourceInitializationPlugin.createImages(record, getProcess());
                associateImages();
            } else {
                readImages();
            }
        } catch (IOException | InterruptedException | SwapException | DAOException | JDOMException | URISyntaxException e) {
            logger.error("Error reading original record");
            readImages();
        }
        for (Image image : currentImages) {
            createImage(image);
        }
    }
    
    public static class ImageFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().matches(".*\\.(jpe?g|tiff?|png|jp2)");
        }
        
    }
}
