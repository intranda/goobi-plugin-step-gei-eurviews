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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.BibliographicMetadataBuilder;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.ResourceMetadataBuilder;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class SourceInitializationPlugin implements IStepPlugin {

    private static final Logger logger = Logger.getLogger(SourceInitializationPlugin.class);
    private static final String TITLE = "SourceInitialization";

    private Step step;
    private String returnPath;

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
                return exitWithError("Data file " + digiSourceFile + " not found");
            }
            EurViewsRecord record = new EurViewsRecord();
            record.setData(FileUtils.readFileToString(digiSourceFile, "utf-8"));
            record.setId(record.get("vorgang"));
            record.setPPN(record.get("ppn", ""));

            Process bookProcess = ProcessManager.getProcessByTitle(getProcessTitleSchoolbook(record));
            if (bookProcess == null) {
                return exitWithError("No schoolbook process found for initialization");
            }

            BibliographicMetadata biblData = BibliographicMetadataBuilder.build(bookProcess, record);
            WorldViewsDatabaseManager.saveBibliographicData(biblData);

            ResouceMetadata data = ResourceMetadataBuilder.build(sourceProcess, record);

            List<Image> images = createImages(record, sourceProcess);
            List<Context> descriptions = createDescriptions(record, sourceProcess);
            List<Transcription> transcriptions = createTranscriptions(record, sourceProcess);
            List<Topic> topics = createTopics(record);

            WorldViewsDatabaseManager.saveResouceMetadata(data);
            WorldViewsDatabaseManager.saveImages(images);
            WorldViewsDatabaseManager.saveDesciptionList(descriptions);
            WorldViewsDatabaseManager.saveTranscriptionList(transcriptions);
            WorldViewsDatabaseManager.saveKeywordList(topics, sourceProcess.getId());

            downloadImages(images, sourceProcess);

        } catch (IOException | InterruptedException | SwapException | DAOException | JDOMException | SQLException | URISyntaxException e) {
            return exitWithError(e.getMessage());
        }
        return true;
    }

    private void downloadImages(List<Image> images, Process sourceProcess) throws IOException, InterruptedException, SwapException, DAOException,
            URISyntaxException {
        Path imagesFolder = Paths.get(sourceProcess.getImagesOrigDirectory(false));
        for (Image image : images) {
            URL url = new URL(image.getFileName());
            Path imageFile = imagesFolder.resolve(Paths.get(url.getFile()).getFileName());
            try (InputStream in = url.openStream()) {
                Files.copy(in, imageFile);
            }
            if (!imageFile.toFile().isFile() || imageFile.toFile().length() == 0) {
                throw new IOException("Failed to download file from " + url);
            }
            image.setFileName(imageFile.toString());
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

        for (Topic topic : topics) {
            for (Keyword keyword : topic.getKeywordList()) {
                if(keywordList.contains(cleaned(keyword.getKeywordNameDE()))) {
                    keyword.setSelected(true);
                }
            }
        }
        return topics;
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
            if (lang.equals(originalLanguage) || (originalLanguage.isEmpty() && !lang.equals("de") && !lang.equals("en"))) {
                trans.setOriginalLanguage(true);
            }
            trans.setLanguage(getLanguageCode(lang));
            trans.setTranscription(fulltext);
        }

        return transcriptions;
    }

    private List<Context> createDescriptions(EurViewsRecord record, Process sourceProcess) throws JDOMException, IOException {
        List<Context> descriptions = new ArrayList<>();

        Context eng = new Context(sourceProcess.getId());
        eng.setLanguage("eng");
        eng.setLongDescription(record.get("descriptions/description[@xml:lang=\"en\"]", ""));
        descriptions.add(eng);

        Context ger = new Context(sourceProcess.getId());
        eng.setLanguage("ger");
        eng.setLongDescription(record.get("descriptions/description[@xml:lang=\"de\"]", ""));
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
        String linkLabel = record.get("bibRef/link/@label");
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
