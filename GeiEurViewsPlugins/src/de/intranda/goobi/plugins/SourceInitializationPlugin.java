package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.JDOMException;

import de.intranda.goobi.model.EurViewsRecord;
import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.LanguageHelper;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.BibliographicMetadataBuilder;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.ResourceMetadataBuilder;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.normdata.NormDatabase;
import de.intranda.goobi.normdata.NormdataSearch;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import sun.security.krb5.internal.crypto.DesCbcCrcEType;

@PluginImplementation
public class SourceInitializationPlugin implements IStepPlugin {

    private static final Logger logger = Logger.getLogger(SourceInitializationPlugin.class);
    private static final String TITLE = "Gei_WorldViews_SourceInitialization";

    private static final List<String> DIGITAL_COLLECTIONS = Arrays.asList(new String[] { "WorldViews", "EurViews" });
    private static final NumberFormat filenameFormat = new DecimalFormat("00000000");

    private Step step;
    private String returnPath;
    private NormdataSearch search = new NormdataSearch(null);;

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        this.returnPath = returnPath;

    }

    @Override
    public boolean execute() {
        Process sourceProcess = getStep().getProzess();
        File digiSourceFile;
        try {
            digiSourceFile = new File(sourceProcess.getSourceDirectory(), "digiSource.xml");
            if (!digiSourceFile.isFile()) {
                return exitWithInfo("No EurViews import data available. Skipping step");
                //                return exitWithError("Data file " + digiSourceFile + " not found");
            }
            EurViewsRecord record = new EurViewsRecord();
            record.setData(FileUtils.readFileToString(digiSourceFile, "utf-8"));
            record.setId(sourceProcess.getTitel());
            record.setPPN(record.get("ppn", ""));

            Process bookProcess = ProcessManager.getProcessByTitle(getProcessTitleSchoolbook(record));
            if (bookProcess == null) {
                return exitWithError("No schoolbook process found for initialization");
            }
            
            BibliographicMetadata biblData = getBibliographicData(record, bookProcess);
            WorldViewsDatabaseManager.saveBibliographicData(biblData);


            ResouceMetadata data = getResourceData(record, sourceProcess, biblData);

            List<Image> images = createImages(record, sourceProcess);
            List<Context> descriptions = createDescriptions(record, sourceProcess);
            List<Transcription> transcriptions = createTranscriptions(record, sourceProcess);
            List<Topic> topics = createTopics(record);
            selectKeywords(topics, Collections.singletonList("Europa"));

            data.setDigitalCollections(DIGITAL_COLLECTIONS);

            downloadImages(images, sourceProcess);

            List<Context> oldDescriptions = WorldViewsDatabaseManager.getDescriptionList(data.getProcessId());
            for (Context context : oldDescriptions) {
                WorldViewsDatabaseManager.deleteDescription(context);
            }
            List<Transcription> oldTranscriptions = WorldViewsDatabaseManager.getTransciptionList(data.getProcessId());
            for (Transcription transcription : oldTranscriptions) {
                WorldViewsDatabaseManager.deleteTranscription(transcription);
            }

            WorldViewsDatabaseManager.saveResouceMetadata(data);
            WorldViewsDatabaseManager.saveDesciptionList(descriptions);
            WorldViewsDatabaseManager.saveTranscriptionList(transcriptions);
            WorldViewsDatabaseManager.saveKeywordList(topics, sourceProcess.getId());
            WorldViewsDatabaseManager.deleteImages(data);
            WorldViewsDatabaseManager.saveImages(images);

        } catch (IOException | InterruptedException | SwapException | DAOException | JDOMException | SQLException | URISyntaxException e) {
            return exitWithError(e.getMessage());
        }
        return true;
    }

    /**
     * @param record
     * @param bookProcess
     * @return
     * @throws SQLException
     */
    public BibliographicMetadata getBibliographicData(EurViewsRecord record, Process bookProcess) throws SQLException {
        BibliographicMetadata biblData = null;
        try {
            biblData = WorldViewsDatabaseManager.getBibliographicData(bookProcess.getId());
        } catch (Throwable e) {
            logger.error(e);
        }
        if (biblData == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new bibliographic record");
            }
            biblData = BibliographicMetadataBuilder.build(bookProcess, record);
        } else {
            BibliographicMetadataBuilder.resetData(biblData);
            BibliographicMetadataBuilder.init(biblData, bookProcess, record);
        }
        return biblData;
    }
    
    public ResouceMetadata getResourceData(EurViewsRecord record, Process sourceProcess, BibliographicMetadata bibData) throws SQLException {
        ResouceMetadata data = null;
        try {
            data = WorldViewsDatabaseManager.getResourceMetadata(sourceProcess.getId());
        } catch (Throwable e) {
            logger.error(e);
        }
        if (data == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new resource record");
            }
            data = ResourceMetadataBuilder.build(sourceProcess, record, bibData);
        } else {
            ResourceMetadataBuilder.resetData(data);
            ResourceMetadataBuilder.init(data, record, bibData);
        }
        return data;
    }

    protected void addNormdata(BibliographicMetadata biblData) {
        for (Person person : biblData.getPersonList()) {
            search.setSearchValue(person.getName());
            search.setSearchOption("Tp*");
            search.search("gnd");
        }

    }

    private void downloadImages(List<Image> images, Process sourceProcess) throws IOException, InterruptedException, SwapException, DAOException,
            URISyntaxException {
        Path imagesFolder = Paths.get(sourceProcess.getImagesOrigDirectory(false));
        int filenameCounter = 1;
        for (Image image : images) {
            URL url = new URL(image.getFileName());
            String filename = filenameFormat.format(filenameCounter) + "." + FilenameUtils.getExtension(
                    Paths.get(url.getFile()).getFileName().toString());
            Path imageFile = imagesFolder.resolve(filename);
            if (!imageFile.toFile().isFile()) {
                try (InputStream in = url.openStream()) {
                    Files.copy(in, imageFile);
                }
                if (!imageFile.toFile().isFile() || imageFile.toFile().length() == 0) {
                    throw new IOException("Failed to download file from " + url);
                }
            }
            image.setFileName(imageFile.getFileName().toString());
            filenameCounter++;
        }
    }

    protected List<Topic> createTopics(EurViewsRecord record) throws JDOMException, IOException {
        List<Topic> topics = KeywordHelper.getInstance().initializeKeywords();

        List<String> eurViewsKeywords = record.getAll("keywords/keywordslist[@xml:lang=\"de\"]/keyword");
        List<String> keywordList = new ArrayList<>();
        for (String string : eurViewsKeywords) {
            List<String> keyWordNames = KeywordHelper.getInstance().getWorldViewsKeywords(string);
            for (String keywordName : keyWordNames) {
                if (StringUtils.isNotBlank(keywordName) && !keywordList.contains(keywordName)) {
                    keywordList.add(keywordName);
                }
            }
        }

        List<String> categories = record.getAll("categories/categorieslist[@xml:lang=\"de\"]/category");
        for (String categoryName : categories) {
            List<String> keyWordNames = KeywordHelper.getInstance().getWorldViewsKeywords(categoryName);
            for (String keywordName : keyWordNames) {
                if (StringUtils.isNotBlank(keywordName) && !keywordList.contains(keywordName)) {
                    keywordList.add(keywordName);
                }
            }
        }

        selectKeywords(topics, keywordList);

        return topics;
    }

    /**
     * @param topics
     * @param keywordList
     */
    public void selectKeywords(List<Topic> topics, List<String> keywordList) {
        for (Topic topic : topics) {
            for (Keyword keyword : topic.getKeywordList()) {
                if (keywordList.contains(cleaned(keyword.getKeywordNameDE()))) {
                    keyword.setSelected(true);
                }
            }
        }
    }

    /**
     * Returns the given keyword, cleaned from any text within brackets ()
     * 
     * @param keyoword
     * @return
     */
    private String cleaned(String keyword) {
        return keyword.replaceAll("\\s*\\(.*\\)\\s*", "");
    }

    private List<Transcription> createTranscriptions(EurViewsRecord record, Process sourceProcess) throws JDOMException, IOException {
        List<Transcription> transcriptions = new ArrayList<>();

        String originalLanguage = record.get("bibRef/source/@xml:lang", "");
        List<String> fulltexts = record.getAll("fulltexts/fulltext");
        List<String> languages = record.getAll("fulltexts/fulltext/@xml:lang");

        int counter = 0;
        for (String fulltext : fulltexts) {
            String lang = languages.get(counter);
            Transcription trans = new Transcription(sourceProcess.getId());
            trans.setLanguage(getLanguageCode(lang));
            if (lang.equals(originalLanguage) || (originalLanguage.isEmpty() && !lang.equals("de") && !lang.equals("en"))) {
                trans.setOriginalLanguage(true);
            }
            trans.setTranscription(fulltext);
            transcriptions.add(trans);
            counter++;
        }

        return transcriptions;
    }

    private List<Context> createDescriptions(EurViewsRecord record, Process sourceProcess) throws JDOMException, IOException {
        List<Context> descriptions = new ArrayList<>();

//        String originalLanguage = record.get("bibRef/source/@xml:lang", "");

        Context eng = new Context(sourceProcess.getId());
        eng.setLanguage("eng");
//        if (originalLanguage.equals("en")) {
//            eng.setOriginalLanguage(true);
//        }
        eng.setShortDescription(record.get("descriptions/description[@xml:lang=\"en\"]", ""));
        descriptions.add(eng);

        Context ger = new Context(sourceProcess.getId());
        ger.setLanguage("ger");
//        if (originalLanguage.equals("de")) {
//            ger.setOriginalLanguage(true);
//        }
        ger.setShortDescription(record.get("descriptions/description[@xml:lang=\"de\"]", ""));
        descriptions.add(ger);

        return descriptions;
    }

    private List<Image> createImages(EurViewsRecord record, Process sourceProcess) throws JDOMException, IOException {

        String copyright = getCopyright(record);

        List<Image> images = new ArrayList<>();
        List<String> daos = record.getAll("daos/dao");
        List<String> types = record.getAll("daos/dao/@type");
        int order = 0;
        for (String imagePath : daos) {
            String type = types.get(order);
            Image image = new Image(sourceProcess.getId());
            image.setOrder(order + 1);
            image.setLicence(copyright);
            image.setFileName(imagePath);
            image.setStructType(getStructType(type));
            images.add(image);
            order++;
        }
        return images;
    }

    private String getStructType(String type) {
        switch (type) {
            case "T":
                return "Titel";
            case "In":
                return "Inhaltsverzeichnis";
            case "Im":
                return "Impressum";
            case "Q":
                return "Quelle";
            case "P":
                return "Vorwort";
            default:
                return "";
        }
    }

    private String getCopyright(EurViewsRecord record) throws JDOMException, IOException {
        String copyright = record.get("bibRef/copyrightText", "");
        String linkLabel = record.get("bibRef/link/@label", "");
        if (StringUtils.isNotBlank(linkLabel) && StringUtils.isNotBlank(copyright)) {
            copyright += "; ";
        }
        copyright += linkLabel;
        return copyright;
    }

    @Override
    public String cancel() {
        return returnPath;
    }

    @Override
    public String finish() {
        return returnPath;
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
        return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return null;
    }

    private boolean exitWithError(String message) {
        Helper.setFehlerMeldung(message);
        Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.ERROR, message);
        return false;
    }

    private boolean exitWithInfo(String message) {
        Helper.addMessageToProcessLog(getStep().getProcessId(), LogType.INFO, message);
        return true;
    }

    public String getProcessTitleSchoolbook(Record record) {
        String sourceTitle = record.getId();
        String suffix = getTitleSuffix(sourceTitle);
        while (suffix.matches("_?\\d+_?")) {
            sourceTitle = sourceTitle.substring(0, sourceTitle.length() - suffix.length());
            suffix = getTitleSuffix(sourceTitle);
        }
        return sourceTitle;
    }

    private String getTitleSuffix(String sourceTitle) {
        int index = sourceTitle.lastIndexOf("_");
        if (index > -1) {
            return sourceTitle.substring(index);
        }
        return "";
    }

    private String getLanguageCode(String string) {
        try {
            return LanguageHelper.getInstance().getLanguage(string).getIsoCode();
        } catch (IllegalArgumentException e) {
            logger.warn("Did not find language '" + string + "' in language list");
            return string;
        }
    }

}
