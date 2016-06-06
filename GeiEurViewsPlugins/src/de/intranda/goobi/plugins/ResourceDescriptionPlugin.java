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
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import lombok.Data;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
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

import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
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

    private List<SelectItem> possibleTypes = new ArrayList<>(38);
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

    private List<Context> descriptionList;

    private Context currentDescription;

    private List<Transcription> transcriptionList;
    private Transcription currentTranscription;

    private List<Topic> topicList = new ArrayList<>();

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
            // TODO check if document type is MMO or monograph
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

            if (!data.getPersonList().isEmpty()) {
                for (Person author : data.getPersonList()) {
                    Person per = new Person();
                    per.setFirstName(author.getFirstName());
                    per.setLastName(author.getLastName());
                    per.setNormdataAuthority(author.getNormdataAuthority());
                    per.setNormdataValue(author.getNormdataValue());
                    per.setRole(author.getRole());

                    data.addToResourceAuthorList(per);
                }
            }
        }

        initializeKeywords();

        initializeResourceTypes();

        //        possibleTypes = ConfigPlugins.getPluginConfig(this).getList("elements.docstruct");
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
            // TODO

            //            List<String> keywordList = DatabaseManager.getKeywordList(process.getId());
            //            if (keywordList != null && !keywordList.isEmpty()) {
            //                for (String listItem : keywordList) {
            //                    String[] data = listItem.split("---");
            //                    String categoryName = data[0];
            //                    String entryName = data[1];
            //                    for (Topic category : possibleKeywords) {
            //                        if (category.getCategoryName().equals(categoryName)) {
            //                            for (KeywordEntry field : category.getKeywordList()) {
            //                                if (field.getKeyword().equals(entryName)) {
            //                                    field.setSelected(true);
            //                                    break;
            //                                }
            //                            }
            //                        }
            //                    }
            //                }
            //
            //            }
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
            descriptionList.add(new Context(process.getId()));
            descriptionList.add(new Context(process.getId()));
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

    private void initializeResourceTypes() {
        possibleTypes.add(new SelectItem("Autorentext", "Autorentext"));
        possibleTypes.add(new SelectItem("Abbildungen", "Abbildungen"));
        possibleTypes.add(new SelectItem("Abbildungen - Objekt", "   -   Objekt"));
        possibleTypes.add(new SelectItem("Abbildungen - Bauwerk", "   -   Bauwerk"));
        possibleTypes.add(new SelectItem("Abbildungen - Relief", "   -   Relief"));
        possibleTypes.add(new SelectItem("Abbildungen - Skulptur", "   -   Skulptur"));
        possibleTypes.add(new SelectItem("Abbildungen - Fotografie", "   -   Fotografie"));
        possibleTypes.add(new SelectItem("Abbildungen - Wandmalerei", "   -   Wandmalerei"));
        possibleTypes.add(new SelectItem("Abbildungen - Gemälde", "   -   Gemälde"));
        possibleTypes.add(new SelectItem("Abbildungen - Zeichnung", "   -   Zeichnung"));
        possibleTypes.add(new SelectItem("Abbildungen - Grafik", "   -   Grafik"));
        possibleTypes.add(new SelectItem("Abbildungen - Comic", "   -   Comic"));
        possibleTypes.add(new SelectItem("Abbildungen - Karikatur", "   -   Karikatur"));
        possibleTypes.add(new SelectItem("Abbildungen - Plakat", "   -   Plakat"));
        possibleTypes.add(new SelectItem("Abbildungen - Postkarte", "   -   Postkarte"));
        possibleTypes.add(new SelectItem("Abbildungen - Sammelbild", "   -   Sammelbild"));

        possibleTypes.add(new SelectItem("Infografik", "Infografik"));
        possibleTypes.add(new SelectItem("Infografik - Karte", "   -   Karte"));

        possibleTypes.add(new SelectItem("Infografik - Karte - politische Karte", "   -     -   politische Karte"));
        possibleTypes.add(new SelectItem("Infografik - Karte - topologische Karte", "   -     -   topologische Karte"));
        possibleTypes.add(new SelectItem("Infografik - Karte - geologische Karte", "   -     -   geologische Karte"));
        possibleTypes.add(new SelectItem("Infografik - Karte - geografische Karte", "   -     -   geografische Karte"));
        possibleTypes.add(new SelectItem("Infografik - Karte - Geschichtskarte", "   -     -   Geschichtskarte"));
        possibleTypes.add(new SelectItem("Infografik - Karte - historische Karte", "   -     -   historische Karte"));

        possibleTypes.add(new SelectItem("Infografik - Struktogramm", "   -   Struktogramm"));
        possibleTypes.add(new SelectItem("Infografik - Tabelle", "   -   Tabelle"));
        possibleTypes.add(new SelectItem("Infografik - Zeitstrahl", "   -   Zeitstrahl"));
        possibleTypes.add(new SelectItem("Infografik - Illustrative Zeichnung", "   -   Illustrative Zeichnung"));
        possibleTypes.add(new SelectItem("Infografik - Piktogramm", "   -   Piktogramm"));
        possibleTypes.add(new SelectItem("Infografik - Diagramm", "   -   Diagramm"));
        possibleTypes.add(new SelectItem("Infografik - Organigramm", "   -   Organigramm"));

        possibleTypes.add(new SelectItem("Schriftquelle", "Schriftquelle"));
        possibleTypes.add(new SelectItem("Schriftquelle - Fachliteratur", "   -   Fachliteratur"));
        possibleTypes.add(new SelectItem("Schriftquelle - Rede", "   -   Rede"));
        possibleTypes.add(new SelectItem("Schriftquelle - Zeitungsartikel", "   -   Zeitungsartikel"));
        possibleTypes.add(new SelectItem("Schriftquelle - Literatur", "   -   Literatur"));
        possibleTypes.add(new SelectItem("Schriftquelle - Interview", "   -   Interview"));
        possibleTypes.add(new SelectItem("Schriftquelle - Tagebücher", "   -   Tagebücher"));

    }

    @SuppressWarnings("unchecked")
    private void initializeKeywords() {

        XMLConfiguration config = ConfigPlugins.getPluginConfig(this);
        config.setExpressionEngine(new XPathExpressionEngine());

        List<HierarchicalConfiguration> topicList = config.configurationsAt("topicList/topic");
        if (topicList != null) {
            for (HierarchicalConfiguration topic : topicList) {
                Topic t = new Topic();
                t.setNameDE(topic.getString("name[@language='de']"));
                t.setNameEN(topic.getString("name[@language='en']"));
                this.topicList.add(t);

                List<HierarchicalConfiguration> keywordList = topic.configurationsAt("keyword");

                if (keywordList != null) {
                    for (HierarchicalConfiguration keyword : keywordList) {
                        Keyword k = new Keyword();
                        String gndid = keyword.getString("@gnd");
                        String wvid = keyword.getString("@wv");
                        if (StringUtils.isNotBlank(gndid)) {
                            k.setGndId(gndid);
                        }
                        if (StringUtils.isNotBlank(wvid)) {
                            k.setWvId(wvid);
                        }
                        k.setKeywordNameDE(keyword.getString("name[@language='de']"));
                        k.setKeywordNameEN(keyword.getString("name[@language='en']"));

                        List<String> synonymListDe = keyword.getList("synonym[@language='de']");
                        List<String> synonymListEn = keyword.getList("synonym[@language='en']");

                        k.setSynonymListDE(synonymListDe);

                        k.setSynonymListEN(synonymListEn);

                        t.addKeyword(k);
                    }
                }

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
            DatabaseManager.saveKeywordList(topicList, process.getId());
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
        ImageManager im = null;
        JpegInterpreter pi = null;
        FileOutputStream outputFileStream = null;
        try {
            im = new ImageManager(new File(inFileName).toURI().toURL());
            Dimension dim = new Dimension();
            dim.setSize(size, size);
            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
            pi = new JpegInterpreter(ri);
            outputFileStream = new FileOutputStream(outFileName);
            pi.writeToStream(null, outputFileStream);
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
