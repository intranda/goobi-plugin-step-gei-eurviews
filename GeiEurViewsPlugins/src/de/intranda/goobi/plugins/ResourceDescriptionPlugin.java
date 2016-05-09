package de.intranda.goobi.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import lombok.Data;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.configuration.HierarchicalConfiguration;
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

import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Description;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.KeywordCategory;
import de.intranda.goobi.model.resource.KeywordEntry;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.DatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibImageException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;

@PluginImplementation
public @Data class ResourceDescriptionPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceDescriptionPlugin.class);

    private Step step;
    private Process process;
    private String returnPath = "/task_edit.xhtml";
    private static final String PLUGIN_NAME = "ResourceDescription";
    private static final String GUI_PATH = "/ResourceDescriptionPlugin.xhtml";
    private int imageSizeInPixel = 300;

    private String imageFolder;

    private List<String> possibleTypes;
    private List<String> possibleImageDocStructs;
    private List<String> possibleLicences;
    private List<String> possibleLanguages;
    private List<String> possiblePersons;
    private List<String> possiblePublisher;
    private List<String> possiblePlaceholder;

    private List<Image> currentImages;
    private Image image = null;
    private int imageIndex = 0;

    private BibliographicData data;

    private List<Description> descriptionList;
    
    
    private Description currentDescription;

    private List<Transcription> transcriptionList;
    private Transcription currentTranscription;

    //    private List<String> categoryList;
    //    private List<String> keywordList;

    private List<KeywordCategory> possibleKeywords = new ArrayList<>();

    private boolean edition = false;
    private static final String USER_GROUP_NAME = "Schlagworterfassung";

    private String displayMode = "";

    private String german;
    private String english;
    private String french;

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

        try {
            data = DatabaseManager.getBibliographicData(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        // import bibliographic data from mets file
        if (data == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new bibliographic record");
            }
            data = new BibliographicData(process.getId());
            // TODO check if document type is MMO
            data.setDocumentType("multivolume");
            
            // TODO get from meta.xml
            List<StringPair> metadataList = MetadataManager.getMetadata(process.getId());
            for (StringPair sp : metadataList) {
                if (sp.getOne().equals("TitleDocMain")) {
                    data.setMaintitleOriginal(sp.getTwo());
                } else if (sp.getOne().equals("TitleDocSub1")) {
                    data.setSubtitleOriginal(sp.getTwo());
                } else if (sp.getOne().equals("Author")) {
                    Person aut = new Person();
                    aut.setRole("Author");

                    String value = sp.getTwo();
                    if (value.contains(" ")) {
                        aut.setFirstName(value.substring(value.indexOf(" ") + 1));
                        aut.setLastName(value.substring(0, value.indexOf(" ")));
                    } else {
                        aut.setLastName(value);
                    }
                    data.addBookAuthor(aut);
                    
                    data.addVolumeAuthor(aut);
                } else if (sp.getOne().equals("DocLanguage")) {
                    data.addLanguage(sp.getTwo());
                } else if (sp.getOne().equals("PublisherName")) {
                    Publisher pub = new Publisher();
                    pub.setRole("Verlag");
                    pub.setName(sp.getTwo());

                    data.addPublisher(pub);
                } else if (sp.getOne().equals("PlaceOfPublication")) {
                    data.setPlaceOfPublication(sp.getTwo());
                } else if (sp.getOne().equals("PublicationYear")) {
                    data.setPublicationYear(sp.getTwo());
                } else if (sp.getOne().equals("shelfmarksource")) {
                    data.setShelfmark(sp.getTwo());
                }

            }

            // Testdaten
            data.addLanguage("de");
            data.addLanguage("en");
            data.addCountry("Deutschland");
            data.addCountry("Schweiz");
            data.addState("Niedersachsen");
            data.addState("Bremen");
        }

        initializeKeywords();

        possibleTypes = ConfigPlugins.getPluginConfig(this).getList("elements.docstruct");
        possibleImageDocStructs = ConfigPlugins.getPluginConfig(this).getList("images.docstruct");
        possibleLicences = ConfigPlugins.getPluginConfig(this).getList("licences.licence");
        possibleLanguages = ConfigPlugins.getPluginConfig(this).getList("elements.language");

        possiblePersons = ConfigPlugins.getPluginConfig(this).getList("elements.person");
        possiblePublisher = ConfigPlugins.getPluginConfig(this).getList("elements.publisher");
        possiblePlaceholder = ConfigPlugins.getPluginConfig(this).getList("elements.placeholder");
        
        try {
            imageFolder = process.getImagesTifDirectory(true);

        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            logger.error(e);
        }

        try {
            currentImages = DatabaseManager.getImages(process.getId());

            List<String> keywordList = DatabaseManager.getKeywordList(process.getId());
            if (keywordList != null && !keywordList.isEmpty()) {
                for (String listItem : keywordList) {
                    String[] data = listItem.split("---");
                    String categoryName = data[0];
                    String entryName = data[1];
                    for (KeywordCategory category : possibleKeywords) {
                        if (category.getCategoryName().equals(categoryName)) {
                            for (KeywordEntry field : category.getKeywordList()) {
                                if (field.getKeyword().equals(entryName)) {
                                    field.setSelected(true);
                                    break;
                                }
                            }
                        }
                    }
                }

            }
            //            categoryList = DatabaseManager.getCategoryList(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if (currentImages == null || currentImages.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new image set");
            }
            currentImages = new ArrayList<Image>();
            String[] imageNameArray = new File(imageFolder).list();
            if (imageNameArray != null && imageNameArray.length > 0) {
                List<String> imageNameList = Arrays.asList(imageNameArray);
                Collections.sort(imageNameList);
                int order = 1;
                for (String imagename : imageNameList) {
                    Image currentImage = new Image(process.getId());
                    currentImage.setFileName(imagename);
                    currentImage.setOrder(order++);
                    currentImages.add(currentImage);
                }
            }
        }

        // create thumbnail images
        for (Image currentImage : currentImages) {
            createImage(currentImage.getFileName());
        }
        if (!currentImages.isEmpty()) {
            setImage(currentImages.get(0));
        }

        try {
            descriptionList = DatabaseManager.getDescriptionList(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if (descriptionList.isEmpty()) {
            descriptionList.add(new Description(process.getId()));
        }

        try {
            transcriptionList = DatabaseManager.getTransciptionList(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if (transcriptionList.isEmpty()) {
            transcriptionList.add(new Transcription(process.getId()));
        }

        User user = Helper.getCurrentUser();
        for (Usergroup ug : user.getBenutzergruppen()) {
            if (ug.getTitel().equalsIgnoreCase(USER_GROUP_NAME)) {
                edition = true;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void initializeKeywords() {

        List elements = ConfigPlugins.getPluginConfig(this).configurationsAt("keywordList.mainEntry");
        if (elements != null) {
            for (Iterator it = elements.iterator(); it.hasNext();) {
                HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
                String value = sub.getString("[@value]");
                KeywordCategory keyword = new KeywordCategory();
                keyword.setCategoryName(value);

                List entries = sub.getList("entry");

                for (Object entry : entries) {
                    String keywordValue = (String) entry;
                    keyword.addKeyword(keywordValue);
                }

                possibleKeywords.add(keyword);
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
            DatabaseManager.saveBibliographicData(data);
            DatabaseManager.saveImages(currentImages);
            DatabaseManager.saveDesciptionList(descriptionList);
            DatabaseManager.saveTranscriptionList(transcriptionList);
            DatabaseManager.saveKeywordList(possibleKeywords, process.getId());
            //            DatabaseManager.saveCategoryList(categoryList, process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    private void createImage(String fileName) {

        //                    /* Pages-Verzeichnis ermitteln */
        String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

        //                    /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String mySession = session.getId() + "_" + fileName + ".png";

        try {
            scaleFile(imageFolder + fileName, myPfad + mySession, imageSizeInPixel);
        } catch (ContentLibImageException | IOException e) {
            logger.error(e);
        }

    }

    private void scaleFile(String inFileName, String outFileName, int size) throws IOException, ContentLibImageException {

        ImageManager im = new ImageManager(new File(inFileName).toURI().toURL());
        Dimension dim = new Dimension();
        dim.setSize(size, size);
        RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
        JpegInterpreter pi = new JpegInterpreter(ri);
        FileOutputStream outputFileStream = new FileOutputStream(outFileName);
        pi.writeToStream(null, outputFileStream);
        outputFileStream.close();

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

    public void addDescription() {
        descriptionList.add(new Description(process.getId()));
    }

    public int getSizeOfDescriptionList() {
        return descriptionList.size();
    }

    public void deleteDescription() {
        if (descriptionList.contains(currentDescription)) {
            descriptionList.remove(currentDescription);
        }
        try {
            DatabaseManager.deleteDescription(currentDescription);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public List<String> completeCategory(String query) {

        try {
            return DatabaseManager.getCategories(query);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    public List<String> completeKeyword(String query) {
        try {
            return DatabaseManager.getKeywords(query);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
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
        String currentImageURL =
                ConfigurationHelper.getTempImagesPathAsCompleteDirectory() + session.getId() + "_" + image.getFileName() + "_large_" + ".png";
        try {
            if (currentImageURL != null) {
                scaleFile(imageFolder + image.getFileName(), currentImageURL, 800);
            }
        } catch (ContentLibImageException | IOException e) {
            logger.error(e);
        }
    }

    public String getBild() {
        if (image == null) {
            return null;
        } else {
            FacesContext context = FacesContextHelper.getCurrentFacesContext();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            String currentImageURL = ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + image.getFileName() + "_large_" + ".png";
            return currentImageURL;
        }
    }

    public void addTranscription() {
        transcriptionList.add(new Transcription(process.getId()));
    }

    public int getSizeOfTranscriptionList() {
        return transcriptionList.size();
    }

    public void deleteTranscription() {
        if (transcriptionList.contains(currentTranscription)) {
            transcriptionList.remove(currentTranscription);
        }
        try {
            DatabaseManager.deleteTranscription(currentTranscription);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void resetValues() {
        german = "";
        french = "";
        english = "";
    }

    public void saveCategory() {
        try {
            DatabaseManager.addCategory(german, english, french);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void saveKeyword() {
        try {
            DatabaseManager.addKeyword(german, english, french);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

}
