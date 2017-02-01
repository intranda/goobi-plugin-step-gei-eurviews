package de.intranda.goobi.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
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

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.DatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;
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

    private List<SelectItem> possibleTypes = new ArrayList<>(38);
    private List<String> possibleImageDocStructs;
    private List<String> possibleLanguages;
    private List<String> possiblePersons;
    private List<String> possiblePublisher;
    private List<String> possiblePlaceholder;

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

    private List<Topic> topicList = new ArrayList<>();

    private boolean edition = false;


    private String displayMode = "";

    private String german;
    private String english;
    private String french;

    private String database;
    protected List<List<NormData>> dataList;

    private String searchOption;
    private String searchValue;
    private String index;
    private String rowType;

    private List<Toponym> resultList;
    private int totalResults;
    private String gndSearchValue;

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
        try {
            data = DatabaseManager.getResouceMetadata(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        // TODO tempoary fix, remove after creation works
        if (data == null) {
            data = new ResouceMetadata(process.getId());
        }

        topicList = KeywordHelper.getInstance().initializeKeywords();

        initializeResourceTypes();

        //        possibleTypes = ConfigPlugins.getPluginConfig(this).getList("elements.docstruct");
        possibleImageDocStructs = ConfigPlugins.getPluginConfig(this).getList("images.docstruct");
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

            List<StringPair> keyowrdList = DatabaseManager.getKeywordList(process.getId());
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
            createImage(currentImage);
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
            descriptionList.add(new Context(process.getId(), getPossibleLanguages().get(0)));
        }
        this.currentDescription = descriptionList.get(0);
        if(this.descriptionList.size() > 1) {
        	this.referenceDescription = this.descriptionList.get(1);
        } else {
        	this.referenceDescription = this.currentDescription;
        }

        try {
            transcriptionList = DatabaseManager.getTransciptionList(process.getId());
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
        
        if(transcriptionList.size() > 1) {
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
                bm = DatabaseManager.getBibliographicData(data.getBibliographicDataId());

                if (!bm.getPersonList().isEmpty()) {
                    for (Person author : bm.getPersonList()) {
                        Person per = new Person();
                        per.setFirstName(author.getFirstName());
                        per.setLastName(author.getLastName());
                        per.setNormdataAuthority(author.getNormdataAuthority());
                        per.setNormdataValue(author.getNormdataValue());
                        per.setRole(author.getRole());

                        data.addToResourceAuthorList(per);
                    }
                }
            } catch (SQLException e) {
                logger.error(e);
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
        possibleTypes.add(new SelectItem("Abbildungen - Sammelbild", "TODO Auto-generated method stub   -   Sammelbild"));

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
            DatabaseManager.saveResouceMetadata(data);
            DatabaseManager.saveImages(currentImages);
            DatabaseManager.saveDesciptionList(descriptionList);
            DatabaseManager.saveTranscriptionList(transcriptionList);
            DatabaseManager.saveKeywordList(topicList, process.getId());
            Helper.setMeldung("dataSavedSuccessfully");
        } catch (SQLException e) {
            logger.error(e);
            Helper.setFehlerMeldung("dataCouldNotBeSaved", e);
        }
    }

    private void createImage(Image image) {
    	String fileName = image.getFileName();
        //                    /* Pages-Verzeichnis ermitteln */
        String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

        //                    /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String mySession = session.getId() + "_" + fileName + ".png";

        try {
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
            im = new ImageManager(new File(inFileName).toURI().toURL());
            Dimension dim = new Dimension();
            dim.setSize(size, size);
            float originalHeight = im.getMyInterpreter().getHeight();
            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
            pi = new JpegInterpreter(ri);
            outputFileStream = new FileOutputStream(outFileName);
            pi.writeToStream(null, outputFileStream);
            return originalHeight/(float)size;
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
            String currentImageURL = baseUrl + ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + image.getFileName() + "_large_" + ".png";
            return currentImageURL;
        }
    }
    
    public String getBildIIIFUrl() throws IOException, InterruptedException, SwapException, DAOException {
    	if(image != null) {    		
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
    	if(image != null) {
    		return image.getScale();
    	} else {
    		return 1f;
    	}
    }

    public boolean isImageHasOcr() {
    	if(image != null) {    		
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
    
    public String getOcrForAllSources(){
    	String ocrResult = "";
    	for (Image myimage : currentImages) {
    		if (myimage.getStructType().equals("Quelle")){
        		String ocrFile = myimage.getFileName().substring(0, myimage.getFileName().lastIndexOf(".")) + ".txt";
        		ocrResult += FilesystemHelper.getOcrFileContent(process, ocrFile);
        		ocrResult+="<br/>";
    		}
		}
    	return ocrResult;
    }

    public int getSizeOfTranscriptionList() {
    	return transcriptionList.size();
    }
    
//    public void addTranscription() {
//        transcriptionList.add(new Transcription(process.getId()));
//    }
//
//
//    public void deleteTranscription() {
//        if (transcriptionList.contains(currentTranscription)) {
//            transcriptionList.remove(currentTranscription);
//        }
//        try {
//            DatabaseManager.deleteTranscription(currentTranscription);
//        } catch (SQLException e) {
//            logger.error(e);
//        }
//        if(transcriptionList.isEmpty()) {
//        	transcriptionList.add(new Transcription(process.getId()));
//        }
//        currentTranscription = transcriptionList.get(0);
//    }

    public void resetValues() {
        german = "";
        french = "";
        english = "";
    }

    public String search() {
        String val = "";
        if (searchOption.isEmpty()) {
            val = searchValue;
        } else {
            val = searchValue + " and BBG=" + searchOption;
        }
        URL url = convertToURLEscapingIllegalCharacters("http://normdata.intranda.com/normdata/gnd/woe/" + val);
        String string = url.toString().replace("Ä", "%C3%84").replace("Ö", "%C3%96").replace("Ü", "%C3%9C").replace("ä", "%C3%A4").replace("ö",
                "%C3%B6").replace("ü", "%C3%BC").replace("ß", "%C3%9F");
        dataList = NormDataImporter.importNormDataList(string);
        return "";
    }

    private URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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

        ComplexMetadataObject metadata = null;
        if (rowType.equals("resourceAuthor")) {
            metadata = data.getResourceAuthorList().get(Integer.parseInt(index));
        }

        if (metadata instanceof Person) {
            Person person = (Person) metadata;
            for (NormData normdata : currentData) {
                if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                    person.setNormdataAuthority("gnd");
                    person.setNormdataValue(normdata.getValues().get(0).getText());
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
        } else if (metadata instanceof Publisher) {
            Publisher person = (Publisher) metadata;
            getPublisherData(person, currentData);
        }
        return "";
    }

    public String getPublisherData(Publisher person, List<NormData> currentData) {
        for (NormData normdata : currentData) {
            if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                person.setNormdataAuthority("gnd");
                person.setNormdataValue(normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("NORM_NAME")) {
                person.setName(filter(normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "")));
            }
        }

        return "";
    }

    public String searchGeonames() {
        String credentials = ConfigurationHelper.getInstance().getGeonamesCredentials();
        if (credentials != null) {
            WebService.setUserName(credentials);
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setNameEquals(gndSearchValue);
            searchCriteria.setStyle(Style.FULL);
            try {
                ToponymSearchResult searchResult = WebService.search(searchCriteria);
                resultList = searchResult.getToponyms();
                totalResults = searchResult.getTotalResultsCount();
            } catch (Exception e) {

            }

        } else {
            // deaktiviert 
            Helper.setFehlerMeldung("geonamesDeactivated");
        }
        return "";
    }

    //    public String getGeonamesData(Toponym currentToponym) {
    //        Location loc = data.getCountryList().get(Integer.parseInt(index));
    //        loc.setName(currentToponym.getName());
    //        loc.setNormdataAuthority("geonames");
    //        loc.setNormdataValue("" + currentToponym.getGeoNameId());
    //        return "";
    //    }

    public String getGeonamesUrl(Location loc) {
        if (StringUtils.isBlank(loc.getNormdataValue())) {
            return null;
        } else {
            return "http://www.geonames.org/" + loc.getNormdataValue();
        }
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
    	if(!language.equals(IMAGE_REFERENCE)) {
    		setReferenceTranscription(getTranscription(language));
    	}
    }
    
    public void setCurrentTranscriptionLanguage(String language) {
    	this.currentTranscriptionLanguage = language;
    	this.currentTranscription = getTranscription(language);
    	if(this.currentTranscription == null) {
    		this.currentTranscription = new Transcription(getProcess().getId());
    		this.currentTranscription.setLanguage(language);
    		this.transcriptionList.add(this.currentTranscription);
    		setDefaultValues(this.currentTranscription);
    	}
    }
    
    private void setDefaultValues(Transcription transcription) {
    	String keyAvailability = "default.{lang}.availability".replace("{lang}", transcription.getLanguage());
		transcription.setAvailability(ConfigPlugins.getPluginConfig(this).getString(keyAvailability, TeiExportPlugin.DEFAULT_TEXT_AVAILABILITY));
		
		String keyProjectDesc = "default.{lang}.projectDesc".replace("{lang}", transcription.getLanguage());
		transcription.setProjectContext(ConfigPlugins.getPluginConfig(this).getString(keyProjectDesc, TeiExportPlugin.DEFAULT_TEXT_CONTEXT));
		
		String keySamplingDecl = "default.{lang}.sampling".replace("{lang}", transcription.getLanguage());
		transcription.setSelectionMethod(ConfigPlugins.getPluginConfig(this).getString(keySamplingDecl, TeiExportPlugin.DEFAULT_TEXT_SAMPLING));
		
	}

	public void setCurrentDescriptionLanguage(String language) {
    	this.currentDescription = getDescription(language);
    	if(this.currentDescription == null) {
    		this.currentDescription = new Context(getProcess().getId(), language);
    		this.descriptionList.add(this.currentDescription);
    	}
    }
    
    public String getCurrentDescriptionLanguage() {
    	return this.currentDescription.getLanguage();
    }
    
    public void setReferenceDescriptionLanguage(String language) {
    	this.referenceDescription = getDescription(language);
    	if(this.referenceDescription == null) {
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
			if(context.getLanguage().equals(language)) {
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
			if(transcription.getLanguage().equals(language)) {
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
}
