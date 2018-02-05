package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;

import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.GeonamesLocale;
import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.LanguageHelper;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.SourceType;
import de.intranda.goobi.model.SourceTypeHelper;
import de.intranda.goobi.model.conversion.HtmlToTEIConvert;
import de.intranda.goobi.model.conversion.HtmlToTEIConvert.ConverterMode;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.BibliographicMetadataBuilder;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.ResourceMetadataBuilder;
import de.intranda.goobi.model.resource.TitleInfo;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.normdata.EduExpertsDatabase;
import de.intranda.goobi.normdata.GeonamesLocalization;
import de.intranda.goobi.normdata.NormdataSearch;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@Log4j
public class TeiExportPlugin implements IStepPlugin, IPlugin {

    public static final String DEFAULT_TEXT_CONTEXT =
            "Ziel ist es, Selbstverortungen und Alteritätskonzept zu erheben sowie Auszüge aus Schulbücher aus aller Welt im Hinblick auf Vorstellungen von übernationalen Zugehörigkeiten und Teilhabe an historisch prägenden Ereignissen und Prozessen abzubilden. Mit dem Quellenmaterial wird es NutzerInnen ermöglicht, transnationale, regionale und interkulturelle Verflechtungen zu erschließen. Wir fokussieren in der Projektphase 2016-22 vor allem auf Vorstellungen von Europäizität sowie alternativen Sinnstiftungsangeboten, auf Gesellschaftskonzepte und Modernitätsverständnisse.";
    public static final String DEFAULT_TEXT_AVAILABILITY =
            "Available with prior consent of depositor (GEI) for purposes of academic research and teaching only.";
    public static final String DEFAULT_TEXT_SAMPLING =
            "Quellenauszüge sind im Hinblick auf Repräsentation, Deutungsmuster und/ oder Perspektive der Darstellung möglichst markant. Es sind Darstellungen, die in besonders weit verbreiteten und genutzten Schulbüchern vermittelt werden oder aber als Sonderpositionierungen (inhaltlich oder z.B. auch didaktisch motiviert) gekennzeichnet werden können. Damit den NutzerInnen der Edition die Einordnung der jeweiligen Auszüge erleichtert wird, werden die Textanteile durch Kooperationspartner und/ oder Redaktion (mit wissenschaftlicher und Regionalexpertise) kontextualisiert und kommentiert sowie nah am Ausgangstext ins Deutsche und Englische übersetzt.";

    //    public static final String GND_URL = "http://d-nb.info/gnd/";
    //    public static final String GEONAMES_URL = "http://sws.geonames.org/";

    public enum LanguageEnum {

        GERMAN("ger", Locale.GERMAN),
        ENGLISH("eng", Locale.ENGLISH),
        ORIGINAL("original", Locale.ENGLISH);

        @Getter
        private String language;
        @Getter
        private Locale locale;

        private LanguageEnum(String language, Locale locale) {
            this.language = language;
            this.locale = locale;
        }
    }

    private static final String PLUGIN_NAME = "Gei_WorldViews_RtfToTeiExport";

    public static final Namespace TEI = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
    protected static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    private Step step;
    private Process process;
    private String returnPath;

    private BibliographicMetadata bibliographicData;
    private ResouceMetadata resouceMetadata;
    private List<Context> descriptionList;
    private List<Transcription> transcriptionList;
    private List<Image> currentImages;
    private List<Topic> topicList;

    protected java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    protected DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);

    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        this.returnPath = returnPath;
        initialize(ProcessManager.getProcessById(step.getProzess().getId()));

    }

    /**
     * @param step
     */
    public void initialize(Process process) {
        this.process = process;

        try {
            resouceMetadata = WorldViewsDatabaseManager.getResourceMetadata(process.getId());
            if (resouceMetadata != null) {
                bibliographicData = WorldViewsDatabaseManager.getBibliographicData(resouceMetadata.getBibliographicDataId());

                if (StringUtils.isBlank(bibliographicData.getMainIdentifier())) {
                    log.info("Missing identifier, adding from METS");
                    Process bookProcess = ProcessManager.getProcessById(bibliographicData.getProzesseID());
                    if (bookProcess != null) {
                        BibliographicMetadata md = BibliographicMetadataBuilder.build(bookProcess, null);
                        if (md != null) {
                            bibliographicData.setMainIdentifier(md.getMainIdentifier());
                            if (StringUtils.isNotBlank(md.getVolumeIdentifier())) {
                                bibliographicData.setVolumeIdentifier(md.getVolumeIdentifier());
                            }
                        }
                    }

                }
                ResourceMetadataBuilder.addEduExpertsNormdata(resouceMetadata);
                BibliographicMetadataBuilder.addEduExpertsNormdata(bibliographicData);
            }
            descriptionList = WorldViewsDatabaseManager.getDescriptionList(process.getId());
            transcriptionList = WorldViewsDatabaseManager.getTransciptionList(process.getId());
            currentImages = WorldViewsDatabaseManager.getImages(process.getId());

            topicList = KeywordHelper.getInstance().initializeKeywords();

            List<StringPair> keywordList = WorldViewsDatabaseManager.getKeywordList(process.getId());
            for (StringPair sp : keywordList) {
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
            log.error(e.toString(), e);
        }
    }

    @Override
    public boolean execute() {
        return execute(getTeiDirectory());
    }

    public boolean execute(File teiDirectory) {
        if (teiDirectory == null) {
            logError("Unable to create directory for TEI");
            return false;
        }
        boolean fileCreated = false;
        List<String> languagesWritten = new ArrayList<>();
        for (LanguageEnum language : EnumSet.allOf(LanguageEnum.class)) {
            if (teiExistsForLanguage(language) && StringUtils.isNotBlank(getLanguageCodeFromTranscription(language))) {
                File teiFile = new File(teiDirectory, getProcess().getTitel() + "_tei_" + getLanguageCodeFromTranscription(language) + ".xml");
                File teiFileDeprecated = new File(teiDirectory, getProcess().getTitel() + "_" + getLanguageCodeFromTranscription(language) + ".xml");

                try {
                    Document oldTeiDocument = null;
                    try {
                        oldTeiDocument = getDocumentFromFile(teiFile);
                        //if no tei file exists, look for a file in the old name scheme
                        if(oldTeiDocument == null) {
                            oldTeiDocument = getDocumentFromFile(teiFileDeprecated);
                        }
                        teiFileDeprecated.delete();
                    } catch (IOException | JDOMException e) {
                        log.error(e);
                        logError("Error reading existing tei file " + teiFile);
                        return false;
                    }
                    Document teiDocument = createTEiDocForLanguage(language);
                    if (oldTeiDocument != null) {
                        Element text = oldTeiDocument.getRootElement().getChild("text", null);
                        text.detach();
                        teiDocument.getRootElement().removeChild("text", null);
                        teiDocument.getRootElement().addContent(text);
                    }

                    XMLOutputter xmlOutput = new XMLOutputter();
                    xmlOutput.setFormat(Format.getPrettyFormat());
                    xmlOutput.output(teiDocument, new FileWriter(teiFile));
                    fileCreated = true;
                    languagesWritten.add(getLanguageCodeFromTranscription(language));
                } catch (JDOMException e) {
                    log.error(e.toString(), e);
                    logError("Invalid xml in rich text field for language " + language + ". Reason: " + e.getMessage());
                    return false;
                } catch (IOException e) {
                    log.error(e);
                    logError("Error writing TEI to file");
                    return false;
                }
            }
        }

        String symLinkPath = ConfigPlugins.getPluginConfig(this).getString("linkFilesTo");
        if (StringUtils.isNotBlank(symLinkPath)) {
            try {
                File symLink = new File(symLinkPath, teiDirectory.getName());
                //delete an existing symlink since it may lead to a deleted process by the same name                
                if (Files.isSymbolicLink(symLink.toPath())) {
                    symLink.delete();
                }
                Files.createSymbolicLink(symLink.toPath(), teiDirectory.toPath());
            } catch (IOException e) {
                log.error(e);
                logError("Error creating symlink at " + symLinkPath + ". The folder may not exist or have limited access.");
                return false;
            }
        }

        if (!fileCreated) {
            logError("Missing content for all languages");
            return false;
        }

        String languagesWrittenMessage = StringUtils.join(languagesWritten, ", ");
        handleSuccess(languagesWrittenMessage);
        return true;
    }

    private Document getDocumentFromFile(File file) throws JDOMException, IOException {
        if (file.isFile()) {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(file);
            return document;
        } else {
            return null;
        }
    }

    private void handleSuccess(String message) {
        LogEntry entry = new LogEntry();
        entry.setContent("TEI file(s) written");
        entry.setType(LogType.INFO);
        getProcessLog().add(entry);
        entry.setCreationDate(new Date());
        entry.setProcessId(getProcess().getId());
        ProcessManager.saveLogEntry(entry);
        Helper.setMeldung(Helper.getTranslation("success_writing_tei", message));
    }

    /**
     * @param errorMessage
     */
    private void logError(String errorMessage) {
        LogEntry errorEntry = new LogEntry();
        errorEntry.setContent("Failed to create TEI documents: " + errorMessage);
        errorEntry.setType(LogType.ERROR);
        getProcessLog().add(errorEntry);
        errorEntry.setCreationDate(new Date());
        errorEntry.setProcessId(getProcess().getId());
        ProcessManager.saveLogEntry(errorEntry);
        Helper.setFehlerMeldung(Helper.getTranslation("error_writing_tei", errorMessage));
    }

    /**
     * 
     * 
     * @return the process directory for tei transcription, creating it if it doesn't exist
     */
    private File getTeiDirectory() {
        try {
            File dir = new File(getProcess().getExportDirectory(), getProcess().getTitel() + "_tei");
            if (!dir.isDirectory() && !dir.mkdirs()) {
                log.error("Failed to create ocr-directory for process " + getProcess().getId());
                return null;
            }
            return dir;
        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            log.error("Failed to get ocr-directory for process " + getProcess().getId());
            return null;
        }
    }

    protected boolean teiExistsForLanguage(LanguageEnum language) {
        for (Transcription transcription : transcriptionList) {
            if (transcription.getLanguage().equals(language.getLanguage()) && StringUtils.isNotBlank(transcription.getTranscription())) {
                return true;
            }
        }
        return false;
    }

    protected Document createTEiDocForLanguage(LanguageEnum language) throws JDOMException, IOException {
        Document teiDocument = new Document();
        Element teiRoot = new Element("TEI", TEI);
        teiDocument.setRootElement(teiRoot);
        teiRoot.setAttribute("id", getTeiId(), XML);
        teiRoot.setAttribute("version", "5.0");

        Element teiHeader = createHeader(language);
        teiRoot.addContent(teiHeader);

        Element text = new Element("text", TEI);
        text.setAttribute("lang", getLanguageCodeFromTranscription(language), XML);
        teiRoot.addContent(text);

        Element body = createBody(language);
        text.addContent(body);

        return teiDocument;
    }

    protected String getTeiId() {
        return "GEI-textbooks";
    }

    /**
     * @param language
     * @return
     */
    protected String getLanguageCodeFromDescription(LanguageEnum language) {
        try {
            return getDescription(language).getLanguageCode();
        } catch (NullPointerException e) {
            return getLanguageCodeFromTranscription(language);
        }
    }

    protected String getLanguageCodeFromTranscription(LanguageEnum language) {
        try {
            return getTranscription(language).getLanguageCode();
        } catch (NullPointerException e) {
            throw new IllegalStateException("No language code set for transcription");
        }
    }

    /**
     * @param language
     * @throws IOException
     * @throws JDOMException
     */
    protected Element createBody(LanguageEnum language) throws JDOMException, IOException {

        Element body = new Element("body", TEI);

        for (Transcription transcription : transcriptionList) {
            if (transcription.getLanguage().equals(language.getLanguage())) {
                Element div = new Element("div", TEI);
                log.debug("Creating body for " + language.getLanguage());
                createTextElement(transcription.getTranscription(), div);
                for (Element child : div.getChildren()) {
                    removeExtraElements(child);
                }
                removeEmptyElements(div);
                body.addContent(div);

            }
        }

        return body;
    }

    protected void removeExtraElements(Element div) {
        Element parent = div;
        while (parent.getChildren() != null && !parent.getChildren().isEmpty()) {
            List<Element> children = parent.getChildren();
            if (StringUtils.isBlank(parent.getText()) && children.size() == 1 && (children.get(0).getName().equals(parent.getName()) || children.get(
                    0).getName().equalsIgnoreCase("p") && !parent.getName().equals("note"))) {
                List<Content> content = children.get(0).removeContent();
                parent.removeContent(children.get(0));
                parent.addContent(content);
            } else {
                for (Element element : children) {
                    removeExtraElements(element);
                }
                break;
            }
        }

    }

    protected void removeEmptyElements(Element div) {
        IteratorIterable<Element> elements = div.getDescendants(Filters.element());
        while (elements.hasNext()) {
            Element ele = elements.next();
            if ((ele.getName().equals("div") || ele.getName().equals("p")) && ele.getChildren().isEmpty() && StringUtils.isBlank(ele.getText())) {
                elements.remove();
                ele.detach();
            }
        }
    }

    protected Element createTextElement(String text, Element wrapper) throws JDOMException, IOException {
        return createTextElement(text, wrapper, ConverterMode.resource);
    }

    protected Element createTextElement(String text, Element wrapper, ConverterMode mode) throws JDOMException, IOException {
        //        text = HtmlToTEIConvert.removeUrlEncoding(text);
        //        text = HtmlToTEIConvert.removeComments(text);
        text = convertBody(text, mode);
        log.debug("Create text element from \n" + text);
//        if(!text.matches("<div\\s+.*?<\\/div>")) {            
//            text = "<div>" + text + "</div>";
//        }
//        StringReader reader = new StringReader(text);
        StringReader reader = new StringReader("<div>" + text + "</div>");
        Document doc = new SAXBuilder().build(reader);
        Element root = doc.getRootElement();
        List<Content> content = root.removeContent((Filter<Content>) (Filters.element().or(Filters.text())));
        String parentName = null;
        while (content.size() == 1 && content.get(0) instanceof Element) {
            String elementName = ((Element)content.get(0)).getName();
            boolean sameAsParent = elementName.equalsIgnoreCase(parentName);
            parentName = elementName;
            if(elementName.equalsIgnoreCase("p") && !sameAsParent) {
                break;
            }
            content = ((Element) content.get(0)).removeContent((Filter<Content>) (Filters.element().or(Filters.text())));
        }
        removeEmptyText(content);
        setNamespace(content, TEI);
        boolean needPWrapper = true;
        ListIterator<Content> iter = content.listIterator();
        while (iter.hasNext()) {
            Content c = iter.next();

            if (c instanceof Element) {
                if (((Element) c).getName().equals("p")) {
                    needPWrapper = false;
                }
            } else if (c instanceof Text && StringUtils.isNotBlank(((Text) c).getText())) {
                Element p = new Element("p", TEI);
                p.setText(((Text) c).getText());
                iter.remove();
                iter.add(p);
                needPWrapper = false;
            }
        }
        if (needPWrapper) {
            Element p = new Element("p", TEI);
            p.addContent(content);
            wrapper.addContent(p);
        } else {
            wrapper.addContent(content);
        }
        return wrapper;
    }

    private void setNamespace(List<? extends Content> content, Namespace ns) {
        if (content != null) {
            for (Content c : content) {
                if (c instanceof Element) {
                    ((Element) c).setNamespace(ns);
                    setNamespace(((Element) c).getChildren(), ns);
                }
            }
        }

    }

    /**
     * @param content
     */
    private void removeEmptyText(List<Content> content) {
        ListIterator<Content> iterator = content.listIterator();
        while (iterator.hasNext()) {
            Content c = iterator.next();
            if (c instanceof Text) {
                if (StringUtils.isBlank(((Text) c).getText())) {
                    iterator.remove();
                }
            }
        }
    }

    protected String convertBody(String text, ConverterMode mode) {
        return new HtmlToTEIConvert(mode).convert(text);
    }

    protected Element createTitleStmt(LanguageEnum language) {
        Element titleStmt = new Element("titleStmt", TEI);

        createFullTitle(resouceMetadata.getResourceTitle(), language, titleStmt, "a", "");

        if (!resouceMetadata.getResourceAuthorList().isEmpty()) {
            for (Person person : resouceMetadata.getResourceAuthorList()) {
                Element author = new Element("author", TEI);
                Element persName = createPersonName(person);
                if (persName != null) {
                    addNormdata(person, persName);
                    author.addContent(persName);
                    titleStmt.addContent(author);
                }
            }
        } else if(!bibliographicData.getPersonList().isEmpty()) {
            for (Person person : bibliographicData.getPersonList()) {
                Element author = new Element("author", TEI);
                Element persName = createPersonName(person);
                if (persName != null) {
                    addNormdata(person, persName);
                    author.addContent(persName);
                    titleStmt.addContent(author);
                }
            }
        } else {
            for (Corporation corporation : bibliographicData.getCorporationList()) {
                if(corporation.getRole().equalsIgnoreCase("editor")) {
                    Element editor = new Element("editor", TEI);
                    titleStmt.addContent(editor);
                    Element orgname = new Element("orgName", TEI);
                    editor.addContent(orgname);
//                    editor.setAttribute("role", corporation.getRole());
                    addNormdata(corporation, orgname);
                    orgname.setText(corporation.getName());
                }
            }
        }
        for (Transcription transcription : transcriptionList) {
            if (transcription.getLanguage().equals(language.getLanguage())) {
                for (SimpleMetadataObject person : transcription.getTranslatorList()) {
                    Element editor = new Element("editor", TEI);
                    titleStmt.addContent(editor);
                    Element persName = new Element("persName", TEI);
                    editor.addContent(persName);
                    editor.setAttribute("role", "translator");
                    persName.setText(person.getValue());
                }
            }
        }

        return titleStmt;
    }

    public static boolean isGermanOrEnglish(String language) {
        return "de".equalsIgnoreCase(language) || "ger".equalsIgnoreCase(language) || "en".equalsIgnoreCase(language) || "eng".equalsIgnoreCase(
                language);
    }

    /**
     * @param person
     * @return
     */
    protected Element createPersonName(Person person) {
        Element persName = new Element("persName", TEI);
        if (StringUtils.isNotBlank(person.getLastName()) && StringUtils.isNotBlank(person.getFirstName())) {
            Element surname = new Element("surname", TEI);
            surname.setText(person.getLastName());
            persName.addContent(surname);

            Element forename = new Element("forename", TEI);
            forename.setText(person.getFirstName());
            persName.addContent(forename);
        } else if (StringUtils.isNotBlank(person.getLastName())) {
            persName.setText(person.getLastName());
        } else if (StringUtils.isNotBlank(person.getFirstName())) {
            persName.setText(person.getFirstName());
        } else {
            persName = null;
        }
        return persName;
    }

    protected Element createBibDataEditionStmt(LanguageEnum language) {
        Element editionStmt = null;
        if (StringUtils.isNotBlank(bibliographicData.getEdition())) {
            editionStmt = new Element("editionStmt", TEI);

            Element edition = new Element("edition", TEI);
            editionStmt.addContent(edition);
            // edition.setAttribute("n", bibliographicData.getEdition());
            edition.setText(bibliographicData.getEdition());
            return editionStmt;
        }
        return null;
    }

    private Element createExtent(String number) {
        Element extent = new Element("extent", TEI);
        Element measure = new Element("measure", TEI);

        String text = number;

        if (number == null) {
            int images = 0;
            for (Image img : currentImages) {
                if ("Quelle".equals(img.getStructType())) {
                    images++;
                }
            }

            number = Integer.toString(images);

            if (this.resouceMetadata != null && this.resouceMetadata.getStartPage() != null) {
                text = resouceMetadata.getStartPage().trim();
                if (StringUtils.isNotBlank(resouceMetadata.getEndPage()) && !resouceMetadata.getStartPage().trim().equals(resouceMetadata.getEndPage()
                        .trim())) {
                    text = text + " - " + resouceMetadata.getEndPage().trim();
                }
            }

        }

        if(StringUtils.isNotBlank(number) || StringUtils.isNotBlank(text)) {            
            measure.setAttribute("unit", "pages");
            if(StringUtils.isNotBlank(number)) {                
                measure.setAttribute("quantity", number);
            }
            measure.setText(text);
            extent.addContent(measure);
            return extent;
        } else {
            return null;
        }
    }

    private Element createSeriesStmt(LanguageEnum language) {
        Element seriesStmt = new Element("seriesStmt", TEI);

        TitleInfo seriesTitle = bibliographicData.getSeriesTitle();
        createFullTitle(seriesTitle, language, seriesStmt, "s", "main");

        Collections.sort(bibliographicData.getSeriesResponsibilityList());
        for (ComplexMetadataObject identity : bibliographicData.getSeriesResponsibilityList()) {
            if(StringUtils.isNotBlank(identity.getName())) {                
                String role = identity.getRole().toLowerCase();
                if (StringUtils.isBlank(role)) {
                    role = "editor";
                }
                Element editor = new Element(role, TEI);
                editor.setText(identity.getName());
                addNormdata(identity, editor);
                seriesStmt.addContent(editor);
            }
        }

        String seriesNumbering = seriesTitle.getNumbering();
        if (StringUtils.isNotBlank(seriesNumbering)) {
            Element biblScope = new Element("biblScope", TEI);
            biblScope.setAttribute("unit", "volume");
            biblScope.setText(seriesNumbering);
            seriesStmt.addContent(biblScope);
        }

        if (seriesStmt.getContentSize() > 0) {
            return seriesStmt;
        } else {
            return null;
        }
    }

    protected Element createPublicationStmt(LanguageEnum language) throws JDOMException, IOException {
        Element publicationStmt = new Element("publicationStmt", TEI);
        Element authority = new Element("authority", TEI);
        publicationStmt.addContent(authority);
        Element orgName1 = new Element("orgName", TEI);
        //        orgName1.setAttribute("ref", "edu.experts.id");
        orgName1.setAttribute("role", "hostingInstitution");
        orgName1.setText("Georg-Eckert-Institut");
        authority.addContent(orgName1);
        Element orgName2 = new Element("orgName", TEI);
        //        orgName2.setAttribute("ref", "edu.experts.id");
        orgName2.setAttribute("role", "project");
        orgName2.setText("WorldViews");
        authority.addContent(orgName2);

        if (StringUtils.isNotBlank(resouceMetadata.getPublicationYearDigital())) {
            Element date = new Element("date", TEI);
            String year = getYear(resouceMetadata.getPublicationYearDigital());
            if (StringUtils.isNotBlank(year)) {
                date.setAttribute("when", year);
            }
            date.setAttribute("type", "publication");
            date.setText(resouceMetadata.getPublicationYearDigital());
            publicationStmt.addContent(date);
        }

        // Element idnoPid = new Element("idno", TEI);
        // idnoPid.setAttribute("type", "CHANGEME");
        // publicationStmt.addContent(idnoPid);
        // idnoPid.setText("1234567890");

        //        Element idnoUPIDCMDI = new Element("idno", TEI);
        //        idnoUPIDCMDI.setAttribute("type", "PIDCMDI");
        //        publicationStmt.addContent(idnoUPIDCMDI);
        //        idnoUPIDCMDI.setText("0987654321");

        String context = "";
        String languageCode = "";
        

        if (getTranscription(language) != null) {
            context = getTranscription(language).getAvailability();
            if (StringUtils.isBlank(context)) {
                context = getDefaultAvailability(getLanguageCodeFromTranscription(language));
            }
            languageCode = getLanguageCodeFromTranscription(language);
        }
        if (StringUtils.isBlank(context)) {
            if (getTranscription(LanguageEnum.ENGLISH) != null) {
                context = getTranscription(LanguageEnum.ENGLISH).getAvailability();
                if (StringUtils.isBlank(context)) {
                    context = getDefaultAvailability(getLanguageCodeFromTranscription(LanguageEnum.ENGLISH));
                }
                languageCode = LanguageEnum.ENGLISH.getLanguage();
            }
        }

        addIdnos(publicationStmt, getProcess().getTitel(), languageCode);
        
        Element availability = new Element("availability", TEI);
        if (StringUtils.isNotBlank(context)) {
            availability.setAttribute("lang", languageCode, XML);
            createTextElement(context, availability);
        }

        if (StringUtils.isNotBlank(getTranscription(language).getLicence())) {
            Element licence = new Element("licence", TEI);
            licence.setAttribute("target", getTranscription(language).getLicence());
            licence.setText(Helper.getString(Locale.ENGLISH, getTranscription(language).getLicence()));
            availability.addContent(licence);
        }
        if (!availability.getChildren().isEmpty()) {
            publicationStmt.addContent(availability);
        }
        

        return publicationStmt;
    }

    /**
     * Create {@code<idno>} Elements for PURL_TEI, PURL_HTML and WV_ID
     * 
     * @param publicationStmt
     * @param resourceTitle
     * @param languageCode
     */
    protected void addIdnos(Element parent, String title, String lang) {
        String purlTEI = ConfigPlugins.getPluginConfig(this).getString("urls.tei", "http://worldviews.gei.de/rest/content/tei/");
        String purlHTML = ConfigPlugins.getPluginConfig(this).getString("urls.html", "http://worldviews.gei.de/open/");
        
        String lang_639_1 = lang;
        try {
            lang_639_1 = LanguageHelper.getInstance().getLanguage(lang).getIsoCode_639_1();
        } catch(IllegalArgumentException | NullPointerException e) {
            log.warn("No language code found for " + lang);
        }
        
        Element idnoTEI = new Element("idno", TEI);
        idnoTEI.setAttribute("type", "PURL_TEI");
        idnoTEI.setText(purlTEI + title + "/" + lang_639_1 + "/");
        parent.addContent(idnoTEI);
        
        Element idnoHTML = new Element("idno", TEI);
        idnoHTML.setAttribute("type", "PURL_HTML");
        idnoHTML.setText(purlHTML + title + "/" + lang_639_1 + "/");
        parent.addContent(idnoHTML);
        
        Element idnoWV = new Element("idno", TEI);
        idnoWV.setAttribute("type", "WV_ID");
        idnoWV.setText(title + "_" + lang);
        parent.addContent(idnoWV);
    }

    protected String getYear(String dateString) {
        if (dateString == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\d{4}");
        Matcher matcher = pattern.matcher(dateString);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "";
        }
    }

    private Element createBibliographicPublicationStmt(LanguageEnum language) {
        Element publicationStmt = new Element("publicationStmt", TEI);

        if(bibliographicData.getPublisherList() == null || bibliographicData.getPublisherList().isEmpty()) {
            Element publisherElement = new Element("publisher", TEI);
            publisherElement.setText(getUnknownValue(language));
            publicationStmt.addContent(publisherElement);
        } else {            
            for (Corporation publisher : bibliographicData.getPublisherList()) {
                Element publisherElement = new Element("publisher", TEI);
                Element orgName = new Element("orgName", TEI);
                addNormdata(publisher, orgName);
                orgName.setText(publisher.getName());
                publisherElement.addContent(orgName);
                publicationStmt.addContent(publisherElement);
            }
        }

        for (Location loc : bibliographicData.getPlaceOfPublicationList()) {
            Element pubPlace = new Element("pubPlace", TEI);
            addNormdata(loc, pubPlace);
            GeonamesLocale geo = getLocalName(language, loc);
            if (StringUtils.isNotBlank(geo.getLanguage())) {
                pubPlace.setAttribute("lang", geo.getLanguage(), XML);
            }
            pubPlace.setText(geo.getOfficialName(true));
            publicationStmt.addContent(pubPlace);
        }

        if (StringUtils.isNotBlank(bibliographicData.getPublicationYear()))

        {
            Element date = new Element("date", TEI);
            String year = getYear(bibliographicData.getPublicationYear());
            if (StringUtils.isNotBlank(year)) {
                date.setAttribute("when", year);
            }
            date.setText(bibliographicData.getPublicationYear());
            publicationStmt.addContent(date);
        }

        if (StringUtils.isNotBlank(bibliographicData.getIsbn())) {
            Element idno = new Element("idno", TEI);
            idno.setAttribute("type", "ISBN");
            idno.setText(bibliographicData.getIsbn());
            publicationStmt.addContent(idno);
        }

        if (StringUtils.isNotBlank(bibliographicData.getMainIdentifier())) {
            if (StringUtils.isNotBlank(bibliographicData.getVolumeIdentifier())) {
                Element ppnC = new Element("idno", TEI);
                ppnC.setAttribute("type", "PPNc");
                ppnC.setText(bibliographicData.getMainIdentifier());
                publicationStmt.addContent(ppnC);

                Element ppnF = new Element("idno", TEI);
                ppnF.setAttribute("type", "PPNf");
                ppnF.setText(bibliographicData.getVolumeIdentifier());
                publicationStmt.addContent(ppnF);
            } else {
                Element ppn = new Element("idno", TEI);
                ppn.setAttribute("type", "PPNa");
                ppn.setText(bibliographicData.getMainIdentifier());
                publicationStmt.addContent(ppn);
            }
        }

        if (publicationStmt.getContentSize() > 0) {
            return publicationStmt;
        } else {
            return null;
        }
    }

    private String getUnknownValue(LanguageEnum language) {
        if(isGerman(language)) {
            return "unbekannt";
        } else {
            return "unknown";
        }
    }

    private GeonamesLocale getLocalName(LanguageEnum language, Location loc) {
        String identifier = loc.getNormdata("geonames").getId();
        if (StringUtils.isNotBlank(identifier)) {

            try {
                GeonamesLocale de = GeonamesLocalization.getLocalNames("de", identifier);
                if (!de.hasName()) {
                    de = GeonamesLocalization.getLocalNames("ger", identifier);
                }
                GeonamesLocale en = GeonamesLocalization.getLocalNames("en", identifier);
                if (!en.hasName()) {
                    en = GeonamesLocalization.getLocalNames("eng", identifier);
                }

                if (de.hasName() && en.hasName()) {

                    if (isGerman(language)) {
                        return new GeonamesLocale("ger", de.getOfficialName(true));
                    } else {
                        return new GeonamesLocale("eng", en.getOfficialName(true));
                    }

                }

            } catch (IOException | JDOMException e) {
                log.warn("Unable to get geoname translation because of " + e.toString());
            }
        }
        return new GeonamesLocale("", loc.getName());
    }

    protected boolean isGerman(LanguageEnum language) {
        if (LanguageEnum.GERMAN.equals(language)) {
            return true;
        } else if (LanguageEnum.ORIGINAL.equals(language)) {
            return "ger".equals(getLanguageCodeFromTranscription(language));
        } else {
            return false;
        }
    }

    private Element createBbiliographicTitleStmt(LanguageEnum language) {
        Element titleStmt = new Element("titleStmt", TEI);

        TitleInfo volumeTitle = bibliographicData.getVolumeTitle();
        createShortTitle(volumeTitle, language, titleStmt, "m", "volume");
        TitleInfo mainTitle = bibliographicData.getMainTitle();
        createFullTitle(mainTitle, language, titleStmt, "m", "main");
        
        Collections.sort(bibliographicData.getPersonList());    
        for (Person person : bibliographicData.getPersonList()) {
            if (StringUtils.isNotBlank(person.getFirstName()) || StringUtils.isNotBlank(person.getLastName())) {
                String role = person.getRole().toLowerCase();
                if (StringUtils.isBlank(role)) {
                    role = "author";
                }
                Element editor = new Element(role, TEI);
                titleStmt.addContent(editor);
                Element persName = createPersonName(person);
                if (persName != null) {
                    addNormdata(person, persName);
                    editor.addContent(persName);
                }
            }
        }
        Collections.sort(bibliographicData.getCorporationList());
        for (Corporation publisher : bibliographicData.getCorporationList()) {
            if (StringUtils.isNotBlank(publisher.getName())) {
                String role = publisher.getRole().toLowerCase();
                if (StringUtils.isBlank(role)) {
                    role = "editor";
                }
                Element editor = new Element(role, TEI);
                titleStmt.addContent(editor);
                Element corpName = new Element("orgName", TEI);
                editor.addContent(corpName);
                addNormdata(publisher, corpName);
                corpName.setText(publisher.getName());
            }
        }

        if (titleStmt.getContentSize() > 0) {
            return titleStmt;
        } else {
            return null;
        }

    }

    /**
     * @param language
     * @param titleStmt
     * @return
     */
    private void createFullTitle(TitleInfo mainTitle, LanguageEnum language, Element titleStmt, String level, String type) {
        if (!mainTitle.isEmpty()) {
            Element title = new Element("title", TEI);
            if (StringUtils.isNotBlank(level)) {
                title.setAttribute("level", level);
            }
            if (StringUtils.isNotBlank(type)) {
                title.setAttribute("type", type);
            }
            if (mainTitle.hasLanguage()) {
                title.setAttribute("lang", mainTitle.getLanguage(), XML);
            }
            title.setText(mainTitle.getTitle());
            titleStmt.addContent(title);
        }

        if (mainTitle.hasSubTitle()) {
            Element title = new Element("title", TEI);
            if (StringUtils.isNotBlank(level)) {
                title.setAttribute("level", level);
            }
            title.setAttribute("type", "sub");
            if (mainTitle.hasLanguage()) {
                title.setAttribute("lang", mainTitle.getLanguage(), XML);
            }
            title.setText(mainTitle.getSubTitle());
            titleStmt.addContent(title);
        }

        if (LanguageEnum.ORIGINAL.equals(language) && mainTitle.isEmpty() && !mainTitle.hasSubTitle() && !mainTitle.hasNumbering()) {
            if (mainTitle.hasEnglishTranslation()) {
                Element title = new Element("title", TEI);
                if (StringUtils.isNotBlank(level)) {
                    title.setAttribute("level", level);
                }
                title.setAttribute("lang", "eng", XML);
                title.setAttribute("type", "translated");
                title.setText(mainTitle.getTranslationENG());
                titleStmt.addContent(title);
            } else if (mainTitle.hasGermanTranslation()) {
                Element title = new Element("title", TEI);
                if (StringUtils.isNotBlank(level)) {
                    title.setAttribute("level", level);
                }
                title.setAttribute("lang", "ger", XML);
                title.setAttribute("type", "translated");
                title.setText(mainTitle.getTranslationGER());
                titleStmt.addContent(title);
            }
        }

        if (LanguageEnum.GERMAN.equals(language) && !mainTitle.isGerman() && mainTitle.hasGermanTranslation()) {
            Element title = new Element("title", TEI);
            if (StringUtils.isNotBlank(level)) {
                title.setAttribute("level", level);
            }
            title.setAttribute("lang", "ger", XML);
            title.setAttribute("type", "translated");
            title.setText(mainTitle.getTranslationGER());
            titleStmt.addContent(title);
        }

        if (LanguageEnum.ENGLISH.equals(language) && !mainTitle.isEnglish() && mainTitle.hasEnglishTranslation()) {
            Element title = new Element("title", TEI);
            if (StringUtils.isNotBlank(level)) {
                title.setAttribute("level", level);
            }
            title.setAttribute("lang", "eng", XML);
            title.setAttribute("type", "translated");
            title.setText(mainTitle.getTranslationENG());
            titleStmt.addContent(title);
        }

        if (mainTitle.hasNumbering()) {
            Element scope = new Element("biblScope", TEI);
            scope.setAttribute("unit", "volume");
            scope.setText(mainTitle.getNumbering());
            titleStmt.addContent(scope);
        }
    }

    private void createShortTitle(TitleInfo mainTitle, LanguageEnum language, Element titleStmt, String level, String type) {
        if (!mainTitle.isEmpty()) {
            Element title = new Element("title", TEI);
            title.setAttribute("level", level);
            title.setAttribute("type", type);
            if (mainTitle.hasLanguage()) {
                title.setAttribute("lang", mainTitle.getLanguage(), XML);
            }
            title.setText(mainTitle.toString());
            titleStmt.addContent(title);
        }

        if (LanguageEnum.GERMAN.equals(language) && !mainTitle.isGerman() && mainTitle.hasGermanTranslation()) {
            Element title = new Element("title", TEI);
            title.setAttribute("level", level);
            title.setAttribute("lang", "ger", XML);
            title.setText(mainTitle.getTranslationGER());
            titleStmt.addContent(title);
        }

        if (LanguageEnum.ENGLISH.equals(language) && !mainTitle.isEnglish() && mainTitle.hasEnglishTranslation()) {
            Element title = new Element("title", TEI);
            title.setAttribute("level", level);
            title.setAttribute("lang", "eng", XML);
            title.setText(mainTitle.getTranslationENG());
            titleStmt.addContent(title);
        }
    }

    protected Element createSourceDesc(LanguageEnum language) {
        Element sourceDesc = new Element("sourceDesc", TEI);

        Element biblFull = new Element("biblFull", TEI);

        sourceDesc.addContent(biblFull);
        Element titleStmt = createBbiliographicTitleStmt(language);
        if (titleStmt != null) {
            biblFull.addContent(titleStmt);
        }
        Element editionStmt = createBibDataEditionStmt(language);
        if (editionStmt != null) {
            biblFull.addContent(editionStmt);
        }

        Element extent = createExtent(bibliographicData.getNumberOfPages());

        if(extent != null) {            
            biblFull.addContent(extent);
        }

        Element publicationStmt = createBibliographicPublicationStmt(language);
        if (publicationStmt != null) {
            biblFull.addContent(publicationStmt);
        }

        Element seriesStmt = createSeriesStmt(language);
        if (seriesStmt != null) {
            biblFull.addContent(seriesStmt);
        }

        Element msDesc = createMsDesc();
        if (msDesc != null) {
            sourceDesc.addContent(msDesc);
        }
        return sourceDesc;
    }

    private Element createMsDesc() {
        Element msDesc = new Element("msDesc", TEI);
        Element msIdentifier = new Element("msIdentifier", TEI);

        if (StringUtils.isNotBlank(bibliographicData.getPhysicalLocation())) {
            Element repository = new Element("repository", TEI);
            repository.setText(bibliographicData.getPhysicalLocation());
            msIdentifier.addContent(repository);
        }

        if (!StringUtils.isBlank(bibliographicData.getShelfmark())) {
            Element idno = new Element("idno", TEI);
            msIdentifier.addContent(idno);

            Element shelfmark = new Element("idno", TEI);
            shelfmark.setText(bibliographicData.getShelfmark());
            shelfmark.setAttribute("type", "shelfmark");
            idno.addContent(shelfmark);
        }

//        if (msIdentifier.getContentSize() > 0) {
            msDesc.addContent(msIdentifier);
//        }

        if (!bibliographicData.getLanguageList().isEmpty()) {
            Element msContents = new Element("msContents", TEI);
            msDesc.addContent(msContents);
            Element language = new Element("textLang", TEI);
            msContents.addContent(language);
            Iterator<SimpleMetadataObject> languages = bibliographicData.getLanguageList().iterator();
            language.setAttribute("mainLang", languages.next().getValue());
            StringBuilder otherLangs = new StringBuilder();
            while (languages.hasNext()) {
                otherLangs.append(" ").append(languages.next().getValue());
            }
            if (StringUtils.isNotBlank(otherLangs.toString())) {
                language.setAttribute("otherLangs", otherLangs.toString().trim());
            }
        }

        if (msDesc.getContentSize() > 0) {
            return msDesc;
        } else {
            return null;
        }

    }

    protected Element createEncodingDesc(LanguageEnum language) throws JDOMException, IOException {
        Element encodingDesc = new Element("encodingDesc", TEI);

        String context = "";
        String languageCode = "";

        Element projectDesc = new Element("projectDesc", TEI);

        try {
            languageCode = getLanguageCodeFromDescription(language);
        } catch (IllegalStateException e) {
            log.warn("No description language found for " + language);
        }

        if (!languageCode.equals("ger")) {
            languageCode = "eng";
            language = LanguageEnum.ENGLISH;
        }

        context = "";
        if (getDescription(language) != null) {
            context = getDescription(language).getProjectContext();
        }
        if (StringUtils.isBlank(context)) {
            context = getDefaultProjectDesc(languageCode);
        }

        if (StringUtils.isNotBlank(context)) {
            encodingDesc.addContent(projectDesc);
            projectDesc.setAttribute("lang", languageCode, XML);
            createTextElement(context, projectDesc);
        }

        context = "";
        if (getDescription(language) != null) {
            context = getDescription(language).getSelectionMethod();
        }
        if (StringUtils.isBlank(context)) {
            context = getDefaultSamplingDecl(languageCode);
        }
        if (StringUtils.isNotBlank(context)) {
            Element samplingDecl = new Element("samplingDecl", TEI);
            encodingDesc.addContent(samplingDecl);
            samplingDecl.setAttribute("lang", languageCode, XML);
            createTextElement(context, samplingDecl);
        }

        return encodingDesc;
    }

    private String getDefaultProjectDesc(String language) {
        String key = "default.{lang}.projectDesc".replace("{lang}", language);
        return ConfigPlugins.getPluginConfig(this).getString(key, "");
    }

    private String getDefaultSamplingDecl(String language) {
        String key = "default.{lang}.sampling".replace("{lang}", language);
        return ConfigPlugins.getPluginConfig(this).getString(key, "");
    }

    private String getDefaultAvailability(String language) {
        String key = "default.{lang}.availability".replace("{lang}", language);
        return ConfigPlugins.getPluginConfig(this).getString(key, "");
    }

    private Transcription getTranscription(LanguageEnum language) {
        for (Transcription transcription : getTranscriptionList()) {
            if (transcription.getLanguage().equals(language.getLanguage())) {
                return transcription;
            }
        }
        return null;
    }

    private Context getDescription(LanguageEnum language) {

        String originalLanguageCode = getOriginalLanguage();

        for (Context description : getDescriptionList()) {
            String descriptionLanguageCode = description.getLanguageCode();
            if (descriptionLanguageCode.equals(language.language)) {
                return description;
            } else if (language.equals(LanguageEnum.ORIGINAL) && originalLanguageCode.equals(description.getLanguageCode())) {
                return description;
            }
        }
        return null;
    }

    private String getOriginalLanguage() {
        List<Transcription> transcriptions = getTranscriptionList();
        if (transcriptions != null) {
            for (Transcription transcription : transcriptions) {
                if (transcription.isOriginalLanguage()) {
                    return transcription.getLanguageCode();
                }
            }
        }
        return null;
    }

    protected Element createHeader(LanguageEnum language) throws JDOMException, IOException {
        Element teiHeader = new Element("teiHeader", TEI);
        Element fileDesc = new Element("fileDesc", TEI);
        teiHeader.addContent(fileDesc);

        Element titleStmt = createTitleStmt(language);
        if (titleStmt != null) {
            fileDesc.addContent(titleStmt);
        }

        Element editionStmt = createEditionStmt(language, "");
        if (editionStmt != null) {
            fileDesc.addContent(editionStmt);
        }
        Element extent = createExtent(null);
        if (extent != null) {
            fileDesc.addContent(extent);
        }
        Element publicationStmt = createPublicationStmt(language);
        if (publicationStmt != null) {
            fileDesc.addContent(publicationStmt);
        }

        if (!LanguageEnum.ORIGINAL.equals(language) && getTranscription(LanguageEnum.ORIGINAL) != null) {
            Element notesStmt = new Element("notesStmt", TEI);
            fileDesc.addContent(notesStmt);
            Element translationNote = new Element("note", TEI);
            notesStmt.addContent(translationNote);
            translationNote.setAttribute("type", "translationNote");
            translationNote.setText("translated from " + getLanguageCodeFromTranscription(LanguageEnum.ORIGINAL));
        }

        Element sourceDesc = createSourceDesc(language);
        if (sourceDesc != null) {
            fileDesc.addContent(sourceDesc);
        }

        Element encodingDesc = createEncodingDesc(language);
        if (encodingDesc != null) {
            teiHeader.addContent(encodingDesc);
        }

        Element profileDesc = createProfileDesc(language);
        if (profileDesc != null) {
            teiHeader.addContent(profileDesc);
        }

        Element revisionDesc = createRevisionDesc();
        if (revisionDesc != null) {
            teiHeader.addContent(revisionDesc);
        }

        return teiHeader;
    }

    protected Element createProfileDesc(LanguageEnum currentLang) throws JDOMException, IOException {
        Element profileDesc = new Element("profileDesc", TEI);
        Element langUsage = new Element("langUsage", TEI);

        String languageString = getLanguageCodeFromTranscription(currentLang);
        if (StringUtils.isNotBlank(languageString)) {
            Element language = new Element("language", TEI);
            language.setAttribute("ident", languageString);
            language.setText(languageString);
            langUsage.addContent(language);
        }

        if (langUsage.getContentSize() > 0) {
            profileDesc.addContent(langUsage);
        }

        List<Element> abstractList = new ArrayList<>();
        String languageCode = "";
        try {
            languageCode = getLanguageCodeFromDescription(currentLang);
        } catch (IllegalStateException e) {
            log.warn("No description language found for " + currentLang);
        }
        LanguageEnum language = currentLang;
        if (!languageCode.equals("ger")) {
            languageCode = "eng";
            language = LanguageEnum.ENGLISH;
        }

        getAbstracts(language, abstractList);
        if (abstractList.isEmpty()) {
            getAbstracts(LanguageEnum.ENGLISH, abstractList);
        }
        if (abstractList != null && !abstractList.isEmpty()) {
            profileDesc.addContent(abstractList);
        }

        // Element textDesc = new Element("textDesc", TEI);
        // profileDesc.addContent(textDesc);
        // textDesc.setAttribute("n", "schoolbook");
        Element textClass = new Element("textClass", TEI);
        profileDesc.addContent(textClass);

        if (!topicList.isEmpty()) {
            // TODO richtige Sprache
            Element keywords = new Element("keywords", TEI);
            keywords.setAttribute("scheme", "WV.topics");
            if (getLanguageCodeFromTranscription(currentLang).equals("ger")) {
                keywords.setAttribute("lang", "ger", XML);
            } else {
                keywords.setAttribute("lang", "eng", XML);
            }
            for (Topic topic : topicList) {
                for (Keyword currentKeyword : topic.getKeywordList()) {
                    if (currentKeyword.isSelected()) {
                        Element term = new Element("term", TEI);

                        Element rsTopic = new Element("rs", TEI);
                        rsTopic.setAttribute("type", "topic");
                        rsTopic.setAttribute("key", topic.getId());
                        rsTopic.setText(getLanguageCodeFromTranscription(currentLang).equals("ger") ? topic.getNameDE() : topic.getNameEN());
                        term.addContent(rsTopic);

                        term.addContent("-");

                        Element rsKeyword = new Element("rs", TEI);
                        rsKeyword.setAttribute("type", "keyword");
                        rsKeyword.setAttribute("key", currentKeyword.getWvId());
                        rsKeyword.setText(getLanguageCodeFromTranscription(currentLang).equals("ger") ? currentKeyword.getKeywordNameDE()
                                : currentKeyword.getKeywordNameEN());
                        term.addContent(rsKeyword);

                        keywords.addContent(term);
                    }
                }
            }
            if (!keywords.getChildren().isEmpty()) {
                textClass.addContent(keywords);
            }
        }

        Element classCode2 = new Element("classCode", TEI);
        classCode2.setAttribute("scheme", "WV.textType");
        if (getLanguageCodeFromTranscription(currentLang).equals("ger")) {
            classCode2.setText("Schulbuchquelle");
            classCode2.setAttribute("lang", "ger", XML);
        } else {
            classCode2.setText("textbook source");
            classCode2.setAttribute("lang", "eng", XML);
        }
        textClass.addContent(classCode2);

        for (SimpleMetadataObject simpleType : resouceMetadata.getResourceTypes()) {
            SourceType sourceType = SourceTypeHelper.getInstance().findSourceType(simpleType.getValue());
            if (sourceType != null && sourceType.hasValue()) {
                Element classCode = new Element("classCode", TEI);
                classCode.setAttribute("scheme", "WV.sourceType");
                if (getLanguageCodeFromTranscription(currentLang).equals("ger")) {
                    //                    classCode.setText(Helper.getString(Locale.GERMAN, resourceType.getValue()));
                    classCode.setText(sourceType.getValueGer());
                    classCode.setAttribute("lang", "ger", XML);
                } else {
                    classCode.setText(Helper.getString(Locale.ENGLISH, sourceType.getValueEng()));
                    classCode.setAttribute("lang", "eng", XML);
                }
                textClass.addContent(classCode);
            }
        }

        if (StringUtils.isNotBlank(bibliographicData.getEducationLevel())) {
            Element domainEducationalLevel = new Element("classCode", TEI);
            domainEducationalLevel.setAttribute("scheme", "WV.educationalLevel");
            if (getLanguageCodeFromTranscription(currentLang).equals("ger")) {
                domainEducationalLevel.setAttribute("lang", "ger", XML);
                domainEducationalLevel.setText(Helper.getString(Locale.GERMAN, bibliographicData.getEducationLevel()));
            } else {
                domainEducationalLevel.setAttribute("lang", "eng", XML);
                domainEducationalLevel.setText(Helper.getString(Locale.ENGLISH, bibliographicData.getEducationLevel()));
            }
            textClass.addContent(domainEducationalLevel);
        }
        for (SimpleMetadataObject subject : bibliographicData.getSchoolSubjects()) {
            if (subject.hasValue()) {
                Element domainEducationalSubject = new Element("classCode", TEI);
                domainEducationalSubject.setAttribute("scheme", "WV.educationalSubject");
                if (getLanguageCodeFromTranscription(currentLang).equals("ger")) {
                    domainEducationalSubject.setAttribute("lang", "ger", XML);
                    domainEducationalSubject.setText(Helper.getString(Locale.GERMAN, subject.getValue()));
                } else {
                    domainEducationalSubject.setAttribute("lang", "eng", XML);
                    domainEducationalSubject.setText(Helper.getString(Locale.ENGLISH, subject.getValue()));
                }
                textClass.addContent(domainEducationalSubject);
            }
        }

        for (Location loc : bibliographicData.getCountryList()) {
            Element domainLocation = new Element("classCode", TEI);
            domainLocation.setAttribute("scheme", "WV.placeOfUse");

            Element rs = new Element("rs", TEI);
            rs.setAttribute("type", "place");
            if (StringUtils.isNotBlank(loc.getNormdata("geonames").getId())) {
                GeonamesLocale locale = getLocalName(currentLang, loc);
                if (StringUtils.isNotBlank(locale.getLanguage())) {
                    domainLocation.setAttribute("lang", locale.getLanguage(), XML);
                }
                addNormdata(loc, rs);
                rs.setText(locale.getOfficialName(true));
            } else {
                rs.setText(loc.getName());
            }
            domainLocation.addContent(rs);
            textClass.addContent(domainLocation);
        }

        for (Location loc : bibliographicData.getStateList()) {
            Element domainLocation = new Element("classCode", TEI);
            domainLocation.setAttribute("scheme", "WV.placeOfUse");

            Element rs = new Element("rs", TEI);
            rs.setAttribute("type", "place");
            if (StringUtils.isNotBlank(loc.getNormdata("geonames").getId())) {
                GeonamesLocale locale = getLocalName(currentLang, loc);
                if (StringUtils.isNotBlank(locale.getLanguage())) {
                    domainLocation.setAttribute("lang", locale.getLanguage(), XML);
                }
                addNormdata(loc, rs);
                rs.setText(locale.getOfficialName(true));
            } else {
                rs.setText(loc.getName());
            }
            domainLocation.addContent(rs);
            textClass.addContent(domainLocation);
        }

        return profileDesc;
    }

    private void getAbstracts(LanguageEnum currentLang, List<Element> abstractList) throws JDOMException, IOException {
        Context englishContext = getDescription(LanguageEnum.ENGLISH);
        Context context = getDescription(currentLang);
        if (context != null && StringUtils.isNotBlank(context.getBookInformation())) {
            Element abstractElement = new Element("abstract", TEI);
            abstractElement.setAttribute("lang", context.getLanguageCode(), XML);
            abstractElement.setAttribute("id", "ProfileDescAbstractSchoolbook", XML);
            Element p = new Element("p", TEI);
            createTextElement(context.getBookInformation(), abstractElement);
            //					abstractElement.addContent(p);
            abstractList.add(abstractElement);
        } else if (englishContext != null && StringUtils.isNotBlank(englishContext.getBookInformation())) {
            Element abstractElement = new Element("abstract", TEI);
            abstractElement.setAttribute("lang", englishContext.getLanguageCode(), XML);
            abstractElement.setAttribute("id", "ProfileDescAbstractSchoolbook", XML);
            Element p = new Element("p", TEI);
            createTextElement(englishContext.getBookInformation(), p);
            abstractElement.addContent(p);
            abstractList.add(abstractElement);
        }

        if (context != null && StringUtils.isNotBlank(context.getShortDescription())) {
            Element abstractElement = new Element("abstract", TEI);
            abstractElement.setAttribute("lang", context.getLanguageCode(), XML);
            abstractElement.setAttribute("id", "ProfileDescAbstractShort", XML);
            Element p = new Element("p", TEI);
            createTextElement(context.getShortDescription(), abstractElement);
            //					abstractElement.addContent(p);
            abstractList.add(abstractElement);
        } else if (englishContext != null && StringUtils.isNotBlank(englishContext.getShortDescription())) {
            Element abstractElement = new Element("abstract", TEI);
            abstractElement.setAttribute("lang", englishContext.getLanguageCode(), XML);
            abstractElement.setAttribute("id", "ProfileDescAbstractSchoolbook", XML);
            Element p = new Element("p", TEI);
            createTextElement(englishContext.getShortDescription(), abstractElement);
            //					abstractElement.addContent(p);
            abstractList.add(abstractElement);
        }

        if (context != null && StringUtils.isNotBlank(context.getLongDescription())) {
            Element abstractElement = new Element("abstract", TEI);
            abstractElement.setAttribute("lang", context.getLanguageCode(), XML);
            abstractElement.setAttribute("id", "ProfileDescAbstractLong", XML);
            Element p = new Element("p", TEI);
            createTextElement(context.getLongDescription(), abstractElement);
            //					abstractElement.addContent(p);
            abstractList.add(abstractElement);
        } else if (englishContext != null && StringUtils.isNotBlank(englishContext.getLongDescription())) {
            Element abstractElement = new Element("abstract", TEI);
            abstractElement.setAttribute("lang", englishContext.getLanguageCode(), XML);
            abstractElement.setAttribute("id", "ProfileDescAbstractSchoolbook", XML);
            Element p = new Element("p", TEI);
            createTextElement(englishContext.getLongDescription(), abstractElement);
            //					abstractElement.addContent(p);
            abstractList.add(abstractElement);
        }
    }

    protected Element createRevisionDesc() {
        Element revisionDesc = new Element("revisionDesc", TEI);

        for (LogEntry logEntry : getProcessLog()) {
            if (StringUtils.isNotBlank(logEntry.getSecondContent())) {
                Element change = new Element("change", TEI);
                revisionDesc.addContent(change);
                Date date = logEntry.getCreationDate();
                change.setAttribute("when", formatter.format(date));
                change.setAttribute("n", logEntry.getSecondContent());
                change.setText(logEntry.getContent());
            }
        }

        if (revisionDesc.getContentSize() > 0) {
            return revisionDesc;
        } else {
            return null;
        }
    }

    protected Element createEditionStmt(LanguageEnum language, String editionStr) {
        Element editionStmt = new Element("editionStmt", TEI);

        Element edition = new Element("edition", TEI);
        editionStmt.addContent(edition);
        String versionNo = getLatestRevision();
        edition.setAttribute("n", versionNo);
        if (StringUtils.isNotBlank(editionStr)) {
            editionStr += ", ";
        }
        edition.setText((editionStr != null ? editionStr : "") + "Version " + versionNo);
        return editionStmt;
    }

    protected String getLatestRevision() {
        if (!getProcessLog().isEmpty()) {
            LogEntry logEntry = getProcessLog().get(getProcessLog().size() - 1);
            if (StringUtils.isNotBlank(logEntry.getSecondContent())) {
                return logEntry.getSecondContent();
            }
        }
        return "1";
    }

    /**
     * @param person
     * @param persName
     */
    public void addNormdata(ComplexMetadataObject metadata, Element nameElement) {
        if (metadata == null || nameElement == null) {
            return;
        }
        if (StringUtils.isNotBlank(metadata.getNormdataUri("gnd"))) {
            nameElement.setAttribute("ref", metadata.getNormdataUri("gnd"));
        } else if (StringUtils.isNotBlank(metadata.getNormdataUri("geonames"))) {
            nameElement.setAttribute("ref", metadata.getNormdataUri("geonames"));
        }
        if (StringUtils.isNotBlank(metadata.getNormdataUri("edu.experts"))) {
//            nameElement.setAttribute("source", NormdataSearch.INTRANDA_NORMDATA_SERVICE_URL + "edu.experts/wvexpertsid/" + metadata.getNormdataValue(
//                    "edu.experts"));
            nameElement.setAttribute("source", EduExpertsDatabase.WVEXPERTS_DATABASE_URL + metadata.getNormdataValue("edu.experts"));
        }
    }

    /**
     * @return
     */
    private List<LogEntry> getProcessLog() {
        try {
            return process.getProcessLog();
        } catch (NoSuchMethodError e) {
            log.warn("Unable to get ProcessLog; Not implemented");
            return new ArrayList<LogEntry>();
        } catch (NullPointerException e) {
            log.warn("No process log found");
            return new ArrayList<LogEntry>();
        }
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

    public String getDescription() {
        return getTitle();
    }

    public Process getProcess() {
        return this.process;
    }

}
