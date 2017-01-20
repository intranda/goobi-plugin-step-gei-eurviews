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
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.DatabaseManager;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@Log4j
public class TeiExportPlugin implements IStepPlugin, IPlugin {

    private static final int HEADER_HIERARCHY_DEPTH = 9;
    private static final String HEADER_DIV_REGEX = "(<hx[\\S\\s]*?)(?=((<h\\d)|$))"; //replace x with the hierarchy level

    public enum LanguageEnum {

        GERMAN("ger"),
        ENGLISH("eng"),
        ORIGINAL("original");

        @Getter
        private String language;

        private LanguageEnum(String language) {
            this.language = language;
        }
    }

    private static final String PLUGIN_NAME = "Gei_WorldViews_RtfToTeiExport";

    private static final Namespace TEI = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
    private static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    private Step step;
    private Process process;
    private String returnPath;

    private BibliographicMetadata bibliographicData;
    private ResouceMetadata resouceMetadata;
    private List<Context> descriptionList;
    private List<Transcription> transcriptionList;
    private List<Image> currentImages;
    private List<Topic> topicList;

    private java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    private DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);

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
        this.process = step.getProzess();

        try {
            resouceMetadata = DatabaseManager.getResouceMetadata(process.getId());
            bibliographicData = DatabaseManager.getBibliographicData(resouceMetadata.getBibliographicDataId());
            descriptionList = DatabaseManager.getDescriptionList(process.getId());
            transcriptionList = DatabaseManager.getTransciptionList(process.getId());
            currentImages = DatabaseManager.getImages(process.getId());

            topicList = KeywordHelper.getInstance().initializeKeywords();

            List<StringPair> keywordList = DatabaseManager.getKeywordList(process.getId());
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
            log.error(e);
        }

    }

    @Override
    public boolean execute() {
        File teiDirectory = getTeiDirectory();
        if (teiDirectory == null) {
            return false;
        }
        for (LanguageEnum language : EnumSet.allOf(LanguageEnum.class)) {
            if (teiExistsForLanguage(language)) {
                File teiFile = new File(teiDirectory, getStep().getProzess().getTitel() + "_" + language.getLanguage() + ".xml");
                Document teiDocument = createTEiDocForLanguage(language);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                try {
                    xmlOutput.output(teiDocument, new FileWriter(teiFile));
                } catch (IOException e) {
                    log.error(e);
                    return false;
                }
            }
        }

        try {
            Files.createSymbolicLink(new File(getStep().getProzess().getImagesDirectory(), teiDirectory.getName()).toPath(), teiDirectory.toPath());
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            return false;
        }

        return true;
    }

    /**
     * 
     * 
     * @return the process directory for tei transcription, creating it if it doesn't exist
     */
    private File getTeiDirectory() {
        try {
            File dir = new File(getStep().getProzess().getOcrDirectory(), getStep().getProzess().getTitel() + "_tei");
            if (!dir.isDirectory() && !dir.mkdirs()) {
                log.error("Failed to create ocr-directory for process " + getStep().getProcessId());
                return null;
            }
            return dir;
        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            log.error("Failed to get ocr-directory for process " + getStep().getProcessId());
            return null;
        }
    }

    private boolean teiExistsForLanguage(LanguageEnum language) {
        for (Transcription transcription : transcriptionList) {
            if (transcription.getLanguage().equals(language.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    protected Document createTEiDocForLanguage(LanguageEnum language) {
        Document teiDocument = new Document();
        Element teiRoot = new Element("TEI", TEI);
        teiDocument.setRootElement(teiRoot);
        teiRoot.setAttribute("id", "GEI-textbooks", XML);
        teiRoot.setAttribute("version", "5.0");

        Element teiHeader = createHeader(language);
        teiRoot.addContent(teiHeader);

        Element text = new Element("text", TEI);
        teiRoot.addContent(text);

        Element body = new Element("body", TEI);
        text.addContent(body);

        for (Transcription transcription : transcriptionList) {
            if (transcription.getLanguage().equals(language.getLanguage())) {
                String fulltext = "<div>" + convertBody(transcription.getTranscription()) + "</div>";
                try {
                    StringReader reader = new StringReader(fulltext);
                    Document teiBody = new SAXBuilder().build(reader);
                    Element root = teiBody.getRootElement();
                    Element div = root.getChild("div", TEI);

                    div.detach();

                    body.addContent(div);
                } catch (JDOMException | IOException e) {
                    log.error(e);
                }

            }
        }

        return teiDocument;
    }

    protected String convertBody(String text) {
        text = removeUrlEncoding(text);
        text = "<div xmlns=\"http://www.tei-c.org/ns/1.0\">" + text + "</div>";

        for (int i = HEADER_HIERARCHY_DEPTH; i > 0; i--) {
            String regex = HEADER_DIV_REGEX.replace("x", Integer.toString(i));
            for (MatchResult r : findRegexMatches(regex, text)) {
                text = text.replace(r.group(), "<div>" + r.group() + "</div>");
            }
            // replace header
            for (MatchResult r : findRegexMatches("<h" + i + ".*?>(.*?)</h" + i + ">", text)) {
                text = text.replace(r.group(), "<head>" + r.group(1) + "</head>");
            }
        }

        // replace bold

        for (MatchResult r : findRegexMatches("<strong>(.*?)</strong>", text)) {
            text = text.replace(r.group(), "<hi rend=\"bold\">" + r.group(1) + "</hi>");
        }
        // replace italic
        for (MatchResult r : findRegexMatches("<em>(.*?)</em>", text)) {
            text = text.replace(r.group(), "<hi rend=\"italic\">" + r.group(1) + "</hi>");
        }
        // replace underline
        for (MatchResult r : findRegexMatches("<span style=\"text-decoration: underline;\">(.*?)</span>", text)) {
            text = text.replace(r.group(), "<hi rend=\"underline\">" + r.group(1) + "</hi>");
        }

        // replace anm
        for (MatchResult r : findRegexMatches("\\[anm\\](.*?)\\[/anm\\]", text)) {
            text = text.replace(r.group(), "<note type=\"editorial\"><p>" + r.group(1) + "</p></note>");
        }

        // tables
        text = text.replaceAll("<table.*?>", "<table>").replace("<tbody>", "").replace("</tbody>", "");
        text = text.replace("<caption>", "<head>").replace("</caption>", "</head>");
        text = text.replaceAll("<tr style=.*?>", "<row>").replace("<tr>", "<row>").replace("</tr>", "</row>");
        text = text.replaceAll("<td style=\".*?\">", "<cell>").replace("</td>", "</cell>");

        // lists
        text = text.replace("<ul>", "<list>").replace("</ul>", "</list>");
        text = text.replace("<li>", "<item>").replace("</li>", "</item>");
        text = text.replace("<ol>", "<list>").replace("</ol>", "</list>");
        text = text.replace("<ol style=\"list-style-type: lower-alpha;\">", "<list>").replace("</ol>", "</list>");

        // images
        //        <img src="none" alt="Bildbeschriftung" />
        for (MatchResult r : findRegexMatches("<img src=\"(.*?)\" alt=\"(.*?)\" />", text)) {
            text = text.replace(r.group(), "<figure><head>" + r.group(2) + "</head><graphic url=\"" + r.group(1) + "\"/></figure>");
        }
        // Blockquote
        for (MatchResult r : findRegexMatches("<blockquote>\\s*<p>\\[Q=(.*?)\\](.*?)\\[/Q\\]</p>\\s*</blockquote>", text)) {
            text = text.replace(r.group(), "<cit><q source=\"#" + r.group(1) + "\">" + r.group(2) + "</q></cit>");
        }

        for (MatchResult r : findRegexMatches("\\[Q=(.*?)\\](.*?)\\[/Q\\]", text)) {
            text = text.replace(r.group(), "<q source=\"#" + r.group(1) + "\">" + r.group(2) + "</q>");
        }

        for (MatchResult r : findRegexMatches("\\[q\\](.*?)\\[/q\\]", text)) {
            text = text.replace(r.group(), "<q>" + r.group(1) + "</q>");
        }

        for (MatchResult r : findRegexMatches("<a href=\"(.*?)\">(.*?)</a>", text)) {
            text = text.replace(r.group(), "<ref target=\"" + r.group(1) + "\" type=\"url\">" + r.group(2) + "</ref>");
        }

        text = text.replace("<br />", "");
        text = text.replace("<p />", "");
        return text;
    }
    //
    //    public static void main(String[] args) {
    //        convertBody(null,
    //                "<h1>Einleitung</h1>\n<p>Erlaubt sind <strong>fett</strong> und <em>kursiv</em> und <span style=\"text-decoration: underline;\">unterstrichen</span> und in <strong><em>allen</em> </strong><span style=\"text-decoration: underline;\"><em><strong>Kombinationen</strong> </em><em>aber</em> <strong>sonst</strong> </span>nichts.</p>\n<p>Eine freie Anmerkung im laufenden Text <span style=\"color: #ff0000;\">[anm]</span>freie Anmerkung<span style=\"color: #ff0000;\">[/anm]</span> kann sp&auml;ter beliebig dargestellt werden, bspw. als eine Fussnote.</p>\n<p>&nbsp;</p>\n<h2>Verweise und Zitate</h2>\n<p>Es gibt interne Verweise auf Abschnitte:</p>\n<p>Hier verweisen wir auf einen Abschnitt, bspw. auf die <a href=\"#einleitung\">Einleitung</a> oder auf das <a href=\"#literaturverzeichnis\">Literaturverzeichnis</a>.</p>\n<p>Es gibt Verweise auf externe Ressourcen:</p>\n<p>Ein Link auf die&nbsp;<a href=\"http://www.gei.de\">GEI</a> Homepage.</p>\n<p>Es gibt interne Verweise im Zusammenhang mit Zitaten, dabei werden nur Direktzitate markiert:</p>\n<p>Ein nachgewiesenes Direktzitat, markiert mit gro&szlig;em [Q]: <span style=\"color: #ff0000;\">[Q=m&uuml;ller2000_14]</span>das direkte nachgewiesene Zitat<span style=\"color: #ff0000;\">[/Q]</span> (<a href=\"#m&uuml;ller2000\">M&uuml;ller 2000, 14</a>)</p>\n<p>Ein nicht nachgewiesene Direktzitat, &nbsp;markiert mit kleinem [q]: Die Studie&nbsp;schreibt <span style=\"color: #ff0000;\">[q]</span>keine einfache Sache<span style=\"color: #ff0000;\">[/q]</span> in diesem Zusammenhang.</p>\n<p>L&auml;ngere Zitate k&ouml;nnen als Blockquote dargestellt werden:</p>\n<blockquote>\n<p><span style=\"color: #ff0000;\">[Q=maier2010_3-9]</span>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.<span style=\"color: #ff0000;\">[/Q]</span> (<a href=\"#maier2010\">Maier 2010, 3-9</a>)</p>\n</blockquote>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<h1>Hauptteil</h1>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<p>&nbsp;<a id=\"tabelle_1\"></a></p>\n<table style=\"height: 34px;\" width=\"580\"><caption>Tabellenbeschriftung</caption>\n<tbody>\n<tr style=\"height: 13px;\">\n<td style=\"width: 186px; height: 13px;\">qq</td>\n<td style=\"width: 186px; height: 13px;\">qq</td>\n<td style=\"width: 186px; height: 13px;\">qq</td>\n</tr>\n<tr style=\"height: 13.9375px;\">\n<td style=\"width: 186px; height: 13.9375px;\">qq</td>\n<td style=\"width: 186px; height: 13.9375px;\">qq</td>\n<td style=\"width: 186px; height: 13.9375px;\">qq</td>\n</tr>\n</tbody>\n</table>\n<p>Hier verweisen wir auf Tabelle<a href=\"#tabelle_1\">1</a> oben.</p>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<p>&nbsp;</p>\n<h2>&Uuml;berschrift-1</h2>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<ul>\n<li>element 1</li>\n<li>element 2</li>\n</ul>\n<p>&nbsp;</p>\n<ol>\n<li>1.element 1</li>\n<li>2.element 2</li>\n</ol>\n<ol style=\"list-style-type: lower-alpha;\">\n<li>a.element 1</li>\n<li>b.element 2</li>\n</ol>\n<p>&nbsp;</p>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<p>&nbsp;</p>\n<h3>&Uuml;berschrift-2</h3>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<p>&nbsp;</p>\n<p><img src=\"none\" alt=\"Bildbeschriftung\" /></p>\n<p>&nbsp;</p>\n<p>Hier verweisen wir auf Bild <a href=\"#bild_1\">1</a> oben.</p>\n<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>\n<p>&nbsp;</p>\n<h1>Literaturverzeichnis</h1>\n<p>M&uuml;ller&nbsp;(2000): Titel. Verlag: Ort.</p>\n<p>Maier&nbsp;(2010): Titel. Verlag: Ort.</p>\n");
    //    }

    /**
     * @param text
     * @return
     */
    private String removeUrlEncoding(String text) {
        text = text.replace("&amp;", "&");
        text = text.replace("&Auml;", "Ä");
        text = text.replace("&Ouml;", "Ö");
        text = text.replace("&Uuml;", "Ü");

        text = text.replace("&auml;", "ä");
        text = text.replace("&ouml;", "ö");
        text = text.replace("&uuml;", "ü");

        text = text.replace("&szlig;", "ß");
        text = text.replace("&nbsp;", "");
        text = text.replace("&shy;", "-");
        return text;
    }

    public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<MatchResult>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }

    private Element createTitleStmt(LanguageEnum language) {
        Element titleStmt = new Element("titleStmt", TEI);

        if (StringUtils.isNotBlank(bibliographicData.getMaintitleOriginal())) {
            Element title = new Element("title", TEI);
            // TODO originalsprache
            //            title.setAttribute("lang", "ger", XML);
            title.setText(bibliographicData.getMaintitleOriginal());
            titleStmt.addContent(title);
        }

        if (StringUtils.isNotBlank(bibliographicData.getMaintitleGerman())) {
            Element title = new Element("title", TEI);
            title.setAttribute("lang", "ger", XML);
            title.setText(bibliographicData.getMaintitleGerman());
            titleStmt.addContent(title);
        }
        if (StringUtils.isNotBlank(bibliographicData.getMaintitleEnglish())) {
            Element title = new Element("title", TEI);
            title.setAttribute("lang", "eng", XML);
            title.setText(bibliographicData.getMaintitleEnglish());
        }

        for (Transcription transcription : transcriptionList) {
            if (transcription.getLanguage().equals(language.getLanguage())) {
                for (SimpleMetadataObject person : transcription.getTranslatorList()) {
                    Element editor = new Element("editor", TEI);
                    titleStmt.addContent(editor);
                    Element persName = new Element("persName", TEI);
                    persName.setAttribute("ref", "edu.experts.id");
                    editor.addContent(persName);
                    editor.setAttribute("role", "translator");
                    persName.setText(person.getValue());
                }
            }
        }
        if (!resouceMetadata.getResourceAuthorList().isEmpty()) {
            for (Person person : resouceMetadata.getResourceAuthorList()) {
                Element author = new Element("author", TEI);
                Element persName = new Element("persName", TEI);
                Element forename = new Element("forename", TEI);
                Element surname = new Element("surname", TEI);
                surname.setText(person.getLastName());
                persName.addContent(surname);
                forename.setText(person.getFirstName());
                persName.addContent(forename);
                if (StringUtils.isNotBlank(person.getNormdataValue())) {
                    persName.setAttribute("ref", person.getNormdataValue());
                }
                author.addContent(persName);
                titleStmt.addContent(author);
            }
        } else {
            for (Person person : bibliographicData.getPersonList()) {
                Element author = new Element("author", TEI);
                Element persName = new Element("persName", TEI);
                Element forename = new Element("forename", TEI);
                Element surname = new Element("surname", TEI);
                surname.setText(person.getLastName());
                persName.addContent(surname);
                forename.setText(person.getFirstName());
                persName.addContent(forename);
                if (StringUtils.isNotBlank(person.getNormdataValue())) {
                    persName.setAttribute("ref", person.getNormdataValue());
                }
                author.addContent(persName);
                titleStmt.addContent(author);
            }
        }

        return titleStmt;
    }

    private Element createEditionStmt(LanguageEnum language) {
        Element editionStmt = null;
        if (StringUtils.isNotBlank(bibliographicData.getEdition())) {
            editionStmt = new Element("editionStmt", TEI);

            Element edition = new Element("edition", TEI);
            editionStmt.addContent(edition);
            edition.setAttribute("n", bibliographicData.getEdition());
            edition.setText(bibliographicData.getEdition());
        }
        return editionStmt;
    }

    private Element createExtent(String number) {
        Element extent = new Element("extent", TEI);
        Element measure = new Element("measure", TEI);

        if (number == null) {
            int images = 0;
            for (Image img : currentImages) {
                if (img.getStructType().equals("Quelle")) {
                    images++;
                }
            }
            number = images + "";
        }
        measure.setAttribute("unit", "pages");
        measure.setAttribute("quantity", number);
        if (number.equals("1")) {
            measure.setText(number + " Seite");
        } else {
            measure.setText(number + " Seiten");
        }
        extent.addContent(measure);
        return extent;
    }

    private Element createSeriesStmt() {
        Element seriesStmt = new Element("seriesStmt", TEI);
        if (StringUtils.isNotBlank(bibliographicData.getVolumeTitleGerman())) {
            Element title = new Element("title", TEI);
            title.setText(bibliographicData.getVolumeTitleGerman());
            title.setAttribute("lang", "ger", XML);
            seriesStmt.addContent(title);
        }

        if (StringUtils.isNotBlank(bibliographicData.getVolumeTitleEnglish())) {
            Element title = new Element("title", TEI);
            title.setText(bibliographicData.getVolumeTitleEnglish());
            title.setAttribute("lang", "eng", XML);
            seriesStmt.addContent(title);
        }

        for (Person person : bibliographicData.getVolumePersonList()) {
            Element editor = new Element("editor", TEI);
            seriesStmt.addContent(editor);
            Element persName = new Element("persName", TEI);
            editor.addContent(persName);

            persName.setAttribute("ref", "edu.experts.id");
            if (StringUtils.isNotBlank(person.getFirstName())) {
                Element forename = new Element("forename", TEI);
                forename.setText(person.getFirstName());
                persName.addContent(forename);
            }
            if (StringUtils.isNotBlank(person.getLastName())) {
                Element surname = new Element("surname", TEI);
                surname.setText(person.getLastName());
                persName.addContent(surname);
            }
        }
        Element biblScope = new Element("biblScope", TEI);
        biblScope.setAttribute("unit", "volume");
        biblScope.setText(bibliographicData.getVolumeNumber());

        seriesStmt.addContent(biblScope);
        return seriesStmt;
    }

    private Element createPublicationStmt() {
        Element publicationStmt = new Element("publicationStmt", TEI);
        Element authority = new Element("authority", TEI);
        publicationStmt.addContent(authority);
        Element orgName1 = new Element("orgName", TEI);
        orgName1.setAttribute("ref", "edu.experts.id");
        orgName1.setAttribute("role", "hostingInstitution");
        orgName1.setText("Georg-Eckert-Institut");
        authority.addContent(orgName1);
        Element orgName2 = new Element("orgName", TEI);
        orgName2.setAttribute("ref", "edu.experts.id");
        orgName2.setAttribute("role", "project");
        orgName2.setText("WorldViews");
        authority.addContent(orgName2);

        Date currentDate = new Date();

        Element date = new Element("date", TEI);
        String dateString = formatter.format(currentDate);
        date.setAttribute("when", dateString);
        date.setAttribute("type", "publication");

        date.setText(df.format(currentDate));

        publicationStmt.addContent(date);

        //        Element idnoPid = new Element("idno", TEI);
        //        idnoPid.setAttribute("type", "CHANGEME");
        //        publicationStmt.addContent(idnoPid);
        //        idnoPid.setText("1234567890");
        Element idnoUPIDCMDI = new Element("idno", TEI);
        idnoUPIDCMDI.setAttribute("type", "PIDCMDI");
        publicationStmt.addContent(idnoUPIDCMDI);
        idnoUPIDCMDI.setText("0987654321");

        Element availability = new Element("availability", TEI);
        publicationStmt.addContent(availability);
        Element p = new Element("p", TEI);
        // TODO Weiteres p für weitere Sprachen?
        p.setText("Available with prior consent of depositor (GEI) for purposes of academic research and teaching only.");
        availability.addContent(p);

        Element licence = new Element("licence", TEI);
        licence.setAttribute("target", "http://creativecommons.org/licenses/by-sa/3.0/");
        licence.setText("Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)");
        availability.addContent(licence);

        return publicationStmt;
    }

    private Element createBibliographicPublicationStmt() {
        Element publicationStmt = new Element("publicationStmt", TEI);
        Element publisherElement = new Element("publisher", TEI);
        publicationStmt.addContent(publisherElement);

        for (Publisher publisher : bibliographicData.getPublisherList()) {
            Element orgName = new Element("orgName", TEI);
            if (!publisher.getNormdataValue().isEmpty()) {
                orgName.setAttribute("ref", publisher.getNormdataValue());
            }
            orgName.setText(publisher.getName());
            publisherElement.addContent(orgName);
        }

        for (Location loc : bibliographicData.getPlaceOfPublicationList()) {
            Element pubPlace = new Element("pubPlace", TEI);
            if (!loc.getNormdataValue().isEmpty()) {
                pubPlace.setAttribute("ref", loc.getNormdataValue());
            }
            pubPlace.setText(loc.getName());
            publicationStmt.addContent(pubPlace);
        }

        Element date = new Element("date", TEI);
        date.setAttribute("when", bibliographicData.getPublicationYear());
        date.setText(bibliographicData.getPublicationYear());
        publicationStmt.addContent(date);
        return publicationStmt;
    }

    private Element createBbiliographicTitleStmt() {
        Element titleStmt = new Element("titleStmt", TEI);

        if (StringUtils.isNotBlank(bibliographicData.getMaintitleOriginal())) {
            Element title = new Element("title", TEI);
            // TODO originalsprache
            //            title.setAttribute("lang", "ger", XML);
            title.setText(bibliographicData.getMaintitleOriginal());
            titleStmt.addContent(title);
        }

        if (StringUtils.isNotBlank(bibliographicData.getMaintitleGerman())) {
            Element title = new Element("title", TEI);
            title.setAttribute("lang", "ger", XML);
            title.setText(bibliographicData.getMaintitleGerman());
            titleStmt.addContent(title);
        }
        if (StringUtils.isNotBlank(bibliographicData.getMaintitleEnglish())) {
            Element title = new Element("title", TEI);
            title.setAttribute("lang", "eng", XML);
            title.setText(bibliographicData.getMaintitleEnglish());
        }
        for (Person person : bibliographicData.getPersonList()) {
            Element editor = new Element("author", TEI);
            titleStmt.addContent(editor);
            Element persName = new Element("persName", TEI);
            editor.addContent(persName);
            persName.setAttribute("ref", "edu.experts.id");
            if (StringUtils.isNotBlank(person.getFirstName())) {
                Element forename = new Element("forename", TEI);
                forename.setText(person.getFirstName());
                persName.addContent(forename);
            }
            if (StringUtils.isNotBlank(person.getLastName())) {
                Element surname = new Element("surname", TEI);
                surname.setText(person.getLastName());
                persName.addContent(surname);
            }
        }
        for (Publisher publisher : bibliographicData.getPublisherList()) {
            Element editor = new Element("author", TEI);
            titleStmt.addContent(editor);
            Element persName = new Element("persName", TEI);
            editor.addContent(persName);

            if (!publisher.getNormdataValue().isEmpty()) {
                persName.setAttribute("ref", publisher.getNormdataValue());
            }
            persName.setText(publisher.getName());
        }

        return titleStmt;

    }

    private Element createSourceDesc(LanguageEnum language) {
        Element sourceDesc = new Element("sourceDesc", TEI);

        Element biblFull = new Element("biblFull", TEI);

        sourceDesc.addContent(biblFull);
        Element titleStmt = createBbiliographicTitleStmt();
        biblFull.addContent(titleStmt);
        Element editionStmt = createEditionStmt(language);
        biblFull.addContent(editionStmt);

        Element extent = createExtent(bibliographicData.getNumberOfPages());

        biblFull.addContent(extent);

        Element publicationStmt = createBibliographicPublicationStmt();
        biblFull.addContent(publicationStmt);

        if (bibliographicData.getDocumentType().equals("multivolume")) {
            Element seriesStmt = createSeriesStmt();
            biblFull.addContent(seriesStmt);
        }

        Element msDesc = createMsDesc();
        sourceDesc.addContent(msDesc);
        return sourceDesc;
    }

    private Element createMsDesc() {
        Element msDesc = new Element("msDesc", TEI);
        Element msIdentifier = new Element("msIdentifier", TEI);
        msDesc.addContent(msIdentifier);

        Element repository = new Element("repository", TEI);
        repository.setText(bibliographicData.getPhysicalLocation());
        msIdentifier.addContent(repository);

        if (!bibliographicData.getShelfmark().isEmpty()) {
            Element idno = new Element("idno", TEI);
            msIdentifier.addContent(idno);

            Element shelfmark = new Element("idno", TEI);
            shelfmark.setText(bibliographicData.getShelfmark());
            shelfmark.setAttribute("type", "shelfmark");
            idno.addContent(shelfmark);
        }

        return msDesc;
    }

    private Element createEncodingDesc() {
        Element encodingDesc = new Element("encodingDesc", TEI);

        Element projectDesc = new Element("projectDesc", TEI);
        encodingDesc.addContent(projectDesc);

        Element p = new Element("p", TEI);
        p.setText(
                "Ziel ist es, Selbstverortungen und Alteritätskonzept zu erheben sowie Auszüge aus Schulbücher aus aller Welt im Hinblick auf Vorstellungen von übernationalen Zugehörigkeiten und Teilhabe an historisch prägenden Ereignissen und Prozessen abzubilden. Mit dem Quellenmaterial wird es NutzerInnen ermöglicht, transnationale, regionale und interkulturelle Verflechtungen zu erschließen. Wir fokussieren in der Projektphase 2016-22 vor allem auf Vorstellungen von Europäizität sowie alternativen Sinnstiftungsangeboten, auf Gesellschaftskonzepte und Modernitätsverständnisse.");
        projectDesc.addContent(p);

        Element samplingDecl = new Element("samplingDecl", TEI);
        Element p2 = new Element("p", TEI);
        p2.setText(
                "Quellenauszüge sind im Hinblick auf Repräsentation, Deutungsmuster und/ oder Perspektive der Darstellung möglichst markant. Es sind Darstellungen, die in besonders weit verbreiteten und genutzten Schulbüchern vermittelt werden oder aber als Sonderpositionierungen (inhaltlich oder z.B. auch didaktisch motiviert) gekennzeichnet werden können. Damit den NutzerInnen der Edition die Einordnung der jeweiligen Auszüge erleichtert wird, werden die Textanteile durch Kooperationspartner und/ oder Redaktion (mit wissenschaftlicher und Regionalexpertise) kontextualisiert und kommentiert sowie nah am Ausgangstext ins Deutsche und Englische übersetzt.");
        samplingDecl.addContent(p2);
        encodingDesc.addContent(samplingDecl);

        return encodingDesc;
    }

    private Element createHeader(LanguageEnum language) {
        Element teiHeader = new Element("teiHeader", TEI);
        Element fileDesc = new Element("fileDesc", TEI);
        teiHeader.addContent(fileDesc);

        Element titleStmt = createTitleStmt(language);
        fileDesc.addContent(titleStmt);

        Element editionStmt = createEditionStmt(language);
        if (editionStmt != null) {
            fileDesc.addContent(editionStmt);
        }
        Element extent = createExtent(null);
        if (extent != null) {
            fileDesc.addContent(extent);
        }
        Element publicationStmt = createPublicationStmt();
        fileDesc.addContent(publicationStmt);

        Element sourceDesc = createSourceDesc(language);
        fileDesc.addContent(sourceDesc);

        Element encodingDesc = createEncodingDesc();
        teiHeader.addContent(encodingDesc);

        Element profileDesc = createProfileDesc(language);
        teiHeader.addContent(profileDesc);

        Element revisionDesc = createRevisionDesc();
        teiHeader.addContent(revisionDesc);

        return teiHeader;
    }

    private Element createProfileDesc(LanguageEnum currentLang) {
        Element profileDesc = new Element("profileDesc", TEI);
        Element langUsage = new Element("langUsage", TEI);
        profileDesc.addContent(langUsage);

        for (SimpleMetadataObject currentLanguage : bibliographicData.getLanguageList()) {
            Element language = new Element("language", TEI);
            language.setAttribute("ident", currentLanguage.getValue());
            language.setText(currentLanguage.getValue());
            langUsage.addContent(language);
        }
        List<Element> abstractList = new ArrayList<>();

        getAbstracts(currentLang, abstractList);
        if (abstractList.isEmpty()) {
            getAbstracts(LanguageEnum.ENGLISH, abstractList);
        }
        profileDesc.addContent(abstractList);

        Element textDesc = new Element("textDesc", TEI);
        profileDesc.addContent(textDesc);
        textDesc.setAttribute("n", "schoolbook");

        if (StringUtils.isNotBlank(bibliographicData.getEducationLevel())) {
            Element domainEducationalLevel = new Element("domain", TEI);
            domainEducationalLevel.setAttribute("type", "educationalLevel");
            domainEducationalLevel.setText(bibliographicData.getEducationLevel());
            textDesc.addContent(domainEducationalLevel);
        }
        if (StringUtils.isNotBlank(bibliographicData.getSchoolSubject())) {
            Element domainEducationalSubject = new Element("domain", TEI);
            domainEducationalSubject.setAttribute("type", "educationalSubject");
            domainEducationalSubject.setText(bibliographicData.getSchoolSubject());
            textDesc.addContent(domainEducationalSubject);
        }

        for (Location loc : bibliographicData.getCountryList()) {
            Element domainLocation = new Element("domain", TEI);
            domainLocation.setAttribute("type", "placeOfUse");
            domainLocation.setText(loc.getName());
            textDesc.addContent(domainLocation);
        }

        for (SimpleMetadataObject loc : bibliographicData.getStateList()) {
            Element domainLocation = new Element("domain", TEI);
            domainLocation.setAttribute("type", "placeOfUse");
            domainLocation.setText(loc.getValue());
            textDesc.addContent(domainLocation);
        }

        Element textClass = new Element("textClass", TEI);
        profileDesc.addContent(textClass);

        // TODO richtige Sprache
        Element keywords = new Element("keywords", TEI);
        keywords.setAttribute("scheme", "WV.topics");
        if (currentLang.getLanguage().equals("ger")) {
            keywords.setAttribute("lang", "ger", XML);
        } else {
            keywords.setAttribute("lang", "eng", XML);
        }
        for (Topic topic : topicList) {
            for (Keyword currentKeyword : topic.getKeywordList()) {
                if (currentKeyword.isSelected()) {
                    Element term = new Element("term", TEI);
                    if (currentLang.getLanguage().equals("ger")) {
                        term.setText(topic.getNameDE() + " - " + currentKeyword.getKeywordNameDE());
                    } else {
                        term.setText(topic.getNameEN() + " - " + currentKeyword.getKeywordNameEN());
                    }

                    keywords.addContent(term);
                }
            }
        }
        textClass.addContent(keywords);
        Element classCode2 = new Element("classCode", TEI);
        classCode2.setAttribute("scheme", "WV.textType");
        classCode2.setText("Schulbuchquelle");
        textClass.addContent(classCode2);

        Element classCode = new Element("classCode", TEI);
        classCode.setAttribute("scheme", "WV.sourceType");
        classCode.setText("Autorentext");
        textClass.addContent(classCode);

        return profileDesc;
    }

    private void getAbstracts(LanguageEnum currentLang, List<Element> abstractList) {
        for (Context context : descriptionList) {
            if (context.getLanguage().equals(currentLang.getLanguage())) {

                if (!context.getBookInformation().isEmpty()) {
                    Element abstractElement = new Element("abstract", TEI);
                    abstractElement.setAttribute("lang", context.getLanguage(), XML);
                    abstractElement.setAttribute("id", "ProfileDescAbstractSchoolbook", XML);
                    //                    Element p = new Element("p", TEI);
                    //                    abstractElement.addContent(p);

                    String fulltext = convertBody(context.getBookInformation());
                    try {
                        StringReader reader = new StringReader(fulltext);
                        Document teiBody = new SAXBuilder().build(reader);
                        Element root = teiBody.getRootElement();
                        Element div = root.getChild("p", TEI);

                        div.detach();

                        abstractElement.addContent(div);
                    } catch (JDOMException | IOException e) {
                        log.error(e);
                    }
                    abstractList.add(abstractElement);
                }

                if (!context.getShortDescription().isEmpty()) {
                    Element abstractElement = new Element("abstract", TEI);
                    abstractElement.setAttribute("lang", context.getLanguage(), XML);
                    abstractElement.setAttribute("id", "ProfileDescAbstractShort", XML);
                    //                    Element p = new Element("p", TEI);
                    //                    abstractElement.addContent(p);
                    String fulltext = convertBody(context.getShortDescription());
                    try {
                        StringReader reader = new StringReader(fulltext);
                        Document teiBody = new SAXBuilder().build(reader);
                        Element root = teiBody.getRootElement();
                        Element div = root.getChild("p", TEI);

                        div.detach();

                        abstractElement.addContent(div);
                    } catch (JDOMException | IOException e) {
                        log.error(e);
                    }
                    abstractList.add(abstractElement);
                }

                if (!context.getLongDescription().isEmpty()) {
                    Element abstractElement = new Element("abstract", TEI);
                    abstractElement.setAttribute("lang", context.getLanguage(), XML);
                    abstractElement.setAttribute("id", "ProfileDescAbstractLong", XML);
                    //                    Element p = new Element("p", TEI);
                    //                    abstractElement.addContent(p);
                    String fulltext = convertBody(context.getLongDescription());
                    try {
                        StringReader reader = new StringReader(fulltext);
                        Document teiBody = new SAXBuilder().build(reader);
                        Element root = teiBody.getRootElement();
                        Element div = root.getChild("p", TEI);

                        div.detach();

                        abstractElement.addContent(div);
                    } catch (JDOMException | IOException e) {
                        log.error(e);
                    }
                    abstractList.add(abstractElement);
                }
            }
        }
    }

    private Element createRevisionDesc() {
        Element revisionDesc = new Element("revisionDesc", TEI);

        for (LogEntry logEntry : getProcessLog()) {
            if (StringUtils.isNotBlank(logEntry.getSecondContent())) {
                Element change = new Element("change", TEI);
                revisionDesc.addContent(change);
                change.setAttribute("when", logEntry.getDate());
                change.setText(logEntry.getContent());
            }
        }

        return revisionDesc;
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

}
