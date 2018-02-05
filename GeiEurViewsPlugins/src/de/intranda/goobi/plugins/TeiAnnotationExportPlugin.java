package de.intranda.goobi.plugins;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Step;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;

import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.annotation.Contribution;
import de.intranda.goobi.model.conversion.HtmlToTEIConvert.ConverterMode;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import org.goobi.beans.Process;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j
public class TeiAnnotationExportPlugin extends TeiExportPlugin {

    public static final String DEFAULT_TEXT_CONTEXT =
            "Kommentare dienen der Erläuterung und Interpretation der ausgesuchten Quelle, insbesondere wenn diese nicht selbsterklärend ist. Essays dienen der vertieften Interpretation von Quellen in ihrem Entstehungskontext (thematisch, räumlich, zeitlich, disziplinenspezifisch). Bildungsgeschichten liefern den nationalen bildungshistorischen Hintergrund für die Fächer Geschichte und Geographie (sowie möglichst auch Staatsbürgerkunde und Werteerziehung o.ä.) von um 1870 bis in die Gegenwart.";

    private List<Contribution> contributionList;
    private ResourceAnnotationPlugin dataPlugin;

    private static final String PLUGIN_NAME = "Gei_WorldViews_Annotation_RtfToTeiExport";

    @Override
    public void initialize(Step step, String returnPath) {
        super.initialize(step, returnPath);
        try {
            this.contributionList = WorldViewsDatabaseManager.getContributions(getProcess().getId());
            this.dataPlugin = new ResourceAnnotationPlugin();
            this.dataPlugin.initialize(getStep(), "");
            WorldViewsDatabaseManager.getContributionDescription(dataPlugin.getData());
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public void initialize(Process process, String returnPath) {
        super.initialize(process);
        try {
            this.contributionList = WorldViewsDatabaseManager.getContributions(getProcess().getId());
            this.dataPlugin = new ResourceAnnotationPlugin();
            this.dataPlugin.initialize(getStep(), "");
            WorldViewsDatabaseManager.getContributionDescription(dataPlugin.getData());
        } catch (SQLException e) {
            log.error(e);
        }
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    @Override
    protected Element createHeader(LanguageEnum language) throws JDOMException, IOException {
        Element teiHeader = new Element("teiHeader", TeiExportPlugin.TEI);
        Element fileDesc = new Element("fileDesc", TeiExportPlugin.TEI);
        teiHeader.addContent(fileDesc);

        Element titleStmt = createTitleStmt(language);
        if (titleStmt != null) {
            fileDesc.addContent(titleStmt);
        }

        Element editionStmt = createEditionStmt(language, getDataPlugin().getData().getEdition());
        if (editionStmt != null) {
            fileDesc.addContent(editionStmt);
        }
        Element publicationStmt = createPublicationStmt(language);
        fileDesc.addContent(publicationStmt);

        if (!LanguageEnum.ORIGINAL.equals(language) && getContribution(LanguageEnum.ORIGINAL) != null) {
            Element notesStmt = new Element("notesStmt", TEI);
            fileDesc.addContent(notesStmt);
            Element translationNote = new Element("note", TEI);
            notesStmt.addContent(translationNote);
            translationNote.setAttribute("type", "translationNote");
            translationNote.setText("translated from " + getLanguageCodeFromContribution(LanguageEnum.ORIGINAL));
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

    @Override
    protected Element createTitleStmt(LanguageEnum language) {
        Element titleStmt = new Element("titleStmt", TEI);

        if (StringUtils.isNotBlank(getTitle(language))) {
            Element title = new Element("title", TEI);
            title.setAttribute("lang", getLanguageCodeFromContribution(language), TeiExportPlugin.XML);
            title.setAttribute("level", "m");
            title.setAttribute("type", "main");
            title.setText(getTitle(language));
            titleStmt.addContent(title);
        }

        for (Person person : getDataPlugin().getData().getAuthorList()) {
            Element author = new Element("author", TEI);
            Element persName = createPersonName(person);
            if (persName != null) {
                addNormdata(person, persName);
                author.addContent(persName);
                titleStmt.addContent(author);
            }
        }

        Contribution contribution = getContribution(language);
        if (contribution != null) {

            List<SimpleMetadataObject> translatorList = getContribution(language).getTranslatorList();

            for (SimpleMetadataObject person : translatorList) {
                Element editor = new Element("editor", TEI);
                titleStmt.addContent(editor);
                Element persName = new Element("persName", TEI);
                // persName.setAttribute("ref", "edu.experts.id");
                editor.addContent(persName);
                editor.setAttribute("role", "translator");
                persName.setText(person.getValue());
            }

        }

        if (titleStmt.getContentSize() > 0) {
            return titleStmt;
        } else {
            return null;
        }
    }

    public Contribution getContribution(LanguageEnum language) {
        for (Contribution contribution : contributionList) {
            if (contribution.getLanguage().equals(language.getLanguage())) {
                return contribution;
            }
        }
        return null;
    }

    protected Element createPublicationStmt(LanguageEnum language) {
        Element publicationStmt = new Element("publicationStmt", TEI);

        Element publisher = new Element("publisher", TEI);

        if (StringUtils.isNotBlank(getDataPlugin().getData().getPublisher())) {
            Element hostOrg = new Element("orgName", TEI);
            hostOrg.setAttribute("role", "hostingInstitution");
            hostOrg.setText(getDataPlugin().getData().getPublisher());
            publisher.addContent(hostOrg);
        }

        if (StringUtils.isNotBlank(getDataPlugin().getData().getProject())) {
            Element project = new Element("orgName", TEI);
            project.setAttribute("role", "project");
            project.setText(getDataPlugin().getData().getProject());
            publisher.addContent(project);
        }
        if (publisher.getContentSize() > 0) {
            publicationStmt.addContent(publisher);
        }

        if (StringUtils.isNotBlank(getDataPlugin().getData().getPublicationYearDigital())) {
            Element date = new Element("date", TEI);
            String year = getYear(getDataPlugin().getData().getPublicationYearDigital());
            if (StringUtils.isNotBlank(year)) {
                date.setAttribute("when", year);
            }
            date.setAttribute("type", "publication");
            //    		date.setText(df.format(currentDate));
            date.setText(getDataPlugin().getData().getPublicationYearDigital());
            publicationStmt.addContent(date);
        }
        
        addIdnos(publicationStmt, getProcess().getTitel(), getLanguageCodeFromContribution(language));

        Element availability = new Element("availability", TEI);
        publicationStmt.addContent(availability);

        Element p = new Element("p", TEI);
        p.setText(getDataPlugin().getData().getAvailability());
        if (StringUtils.isNotBlank((getDataPlugin().getData().getAvailability()))) {
            availability.addContent(p);
        }

        if (StringUtils.isNotBlank(getDataPlugin().getData().getLicence())) {
            Element licence = new Element("licence", TEI);
            licence.setAttribute("target", getDataPlugin().getData().getLicence());
            licence.setText(Helper.getString(Locale.ENGLISH, getDataPlugin().getData().getLicence()));
            availability.addContent(licence);
        }
        
        if (publicationStmt.getContentSize() > 0) {
            return publicationStmt;
        } else {
            return null;
        }
    }

    protected Element createEncodingDesc(LanguageEnum language) throws JDOMException, IOException {
        Element encodingDesc = new Element("encodingDesc", TEI);

        String context = "";
        String languageCode = getLanguageCodeFromContribution(language);
        Element projectDesc = new Element("projectDesc", TEI);
        if (!languageCode.equals("ger") && !languageCode.equals("eng")) {
            languageCode = "eng";
            language = LanguageEnum.ENGLISH;
        }
        context = getContext(language);
        if (StringUtils.isBlank(context)) {
            context = getDefaultContext(languageCode);
        }
        if (StringUtils.isNotBlank(context)) {
            encodingDesc.addContent(projectDesc);
            projectDesc.setAttribute("lang", languageCode, XML);
            createTextElement(context, projectDesc);
        }

        return encodingDesc;
    }

    private String getDefaultContext(String language) {
        String key = "default.{lang}.projectDesc".replace("{lang}", language);
        return ConfigPlugins.getPluginConfig(this).getString(key, "");
    }

    @Override
    protected Element createProfileDesc(LanguageEnum currentLang) throws JDOMException, IOException {
        Element profileDesc = new Element("profileDesc", TEI);

        if (StringUtils.isNotBlank(currentLang.getLanguage())) {
            Element langUsage = new Element("langUsage", TEI);
            Element language = new Element("language", TEI);
            language.setAttribute("ident", getLanguageCodeFromContribution(currentLang));
            language.setText(getLanguageCodeFromContribution(currentLang));
            langUsage.addContent(language);
            profileDesc.addContent(langUsage);
        }

        String languageCode = getLanguageCodeFromDescription(currentLang);
        LanguageEnum language = currentLang;
        if (!languageCode.equals("ger") && !languageCode.equals("eng")) {
            languageCode = "eng";
            language = LanguageEnum.ENGLISH;
        }
        String abstractText = getAbstrakt(language);
        if (StringUtils.isBlank(abstractText)) {
            abstractText = getAbstrakt(LanguageEnum.ENGLISH);
        }

        if (StringUtils.isNotBlank(abstractText)) {
            Element abstr = new Element("abstract", TEI);
            abstr.setAttribute("lang", languageCode, XML);
            profileDesc.addContent(abstr);
            createTextElement(abstractText, abstr);
            //			abstr.addContent(p);
        }
        Element textClass = new Element("textClass", TEI);
        profileDesc.addContent(textClass);

        Element keywords = new Element("keywords", TEI);
        keywords.setAttribute("scheme", "WV.topics");
        if (getLanguageCodeFromContribution(currentLang).equals("ger")) {
            keywords.setAttribute("lang", "ger", XML);
        } else {
            keywords.setAttribute("lang", "eng", XML);
        }
        List<Topic> topics = getDataPlugin().getTopicList();
        for (Topic topic : topics) {
            for (Keyword currentKeyword : topic.getKeywordList()) {
                if (currentKeyword.isSelected()) {
                    Element term = new Element("term", TEI);

                    Element rsTopic = new Element("rs", TEI);
                    rsTopic.setAttribute("type", "topic");
                    rsTopic.setAttribute("key", topic.getId());
                    rsTopic.setText(getLanguageCodeFromContribution(currentLang).equals("ger") ? topic.getNameDE() : topic.getNameEN());
                    term.addContent(rsTopic);

                    term.addContent("-");

                    Element rsKeyword = new Element("rs", TEI);
                    rsKeyword.setAttribute("type", "keyword");
                    rsKeyword.setAttribute("key", currentKeyword.getWvId());
                    rsKeyword.setText(getLanguageCodeFromContribution(currentLang).equals("ger") ? currentKeyword.getKeywordNameDE() : currentKeyword
                            .getKeywordNameEN());
                    term.addContent(rsKeyword);

                    keywords.addContent(term);
                }
            }
        }
        if (!keywords.getChildren().isEmpty()) {
            textClass.addContent(keywords);
        }

        Element classCode = new Element("classCode", TEI);
        classCode.setAttribute("scheme", "WV.textType");
        if (getLanguageCodeFromContribution(currentLang).equals("ger")) {
            classCode.setAttribute("lang", "ger", XML);
            classCode.setText(Helper.getString(Locale.GERMAN, getDataPlugin().getData().getContributionType()));
        } else {
            classCode.setAttribute("lang", "eng", XML);
            classCode.setText(Helper.getString(Locale.ENGLISH, getDataPlugin().getData().getContributionType()));
        }
        textClass.addContent(classCode);

        return profileDesc;
    }

    public String getContext(LanguageEnum language) {
        if (getContribution(language) != null) {
            return getContribution(language).getContext();
        } else {            
            return null;
        }
    }

    public String getAbstrakt(LanguageEnum language) {
        if (getContribution(language) != null) {
            return getContribution(language).getAbstrakt();
        } else {            
            return null;
        }
    }

    public String getContent(LanguageEnum language) {
        if (getContribution(language) != null) {
            return getContribution(language).getContent();
        } else {
            return null;            
        }
    }

    protected String getLanguageCodeFromContribution(LanguageEnum language) {
        try {
            return getContribution(language).getLanguageCode();
        } catch (Throwable e) {
            throw new IllegalStateException("No language code set for contribution");
            //			log.warn("Cannot identify language code of " + language.getLanguage());
            //			return language.getLanguage();
        }
    }

    protected String getLanguageCodeFromDescription(LanguageEnum language) {
        return getLanguageCodeFromContribution(language);
    }

    protected String getLanguageCodeFromTranscription(LanguageEnum language) {
        return getLanguageCodeFromContribution(language);
    }

    /**
     * @param language
     * @return
     */
    private String getTitle(LanguageEnum language) {
        return getContribution(language).getTitle();
    }

    @Override
    protected Element createSourceDesc(LanguageEnum language) {
        Element sourceDesc = new Element("sourceDesc", TeiExportPlugin.TEI);
        Element p = new Element("p", TEI);
        p.addContent("born digital");
        sourceDesc.addContent(p);
        return sourceDesc;
    }

    @Override
    protected Element createBody(LanguageEnum language) throws JDOMException, IOException {

        Element body = new Element("body", TEI);

        Contribution contribution = getContribution(language);

        String content = contribution.getContent();

        Element div = new Element("div", TEI);
        createTextElement(content, div, ConverterMode.annotation);
        for (Element child : div.getChildren()) {
            removeExtraElements(child);
        }
        teiConformance(div);
        body.addContent(div);

        return body;
    }

    protected void teiConformance(Element div) {
        //no <hi> in head
        IteratorIterable<Element> headElements = div.getDescendants(org.jdom2.filter.Filters.element("head", null));
        List<Element> headList = new ArrayList<>();
        while (headElements.hasNext()) {
            Element head = headElements.next();
            headList.add(head);
        }
        for (Element head : headList) {
            List<Element> his = head.getChildren("hi", null);
            for (Element hi : his) {
                head.addContent(head.indexOf(hi), new Text(hi.getText()));
                hi.detach();
            }
        }

        //parent of <figure> must be <div>
        makeChildOfDiv(div, "figure");
        makeChildOfDiv(div, "list");
        makeChildOfDiv(div, "table");

        addMissingHeads(div);

    }

    /**
     * Create a head element as first element for all <div>s that don't already start with <head>
     * 
     * @param div
     */
    private void addMissingHeads(Element div) {
        addHeadIfMissing(div);
        IteratorIterable<Element> divs = div.getDescendants(Filters.element("div"));
        List<Element> divList = new ArrayList<>();
        while (divs.hasNext()) {
            divList.add(divs.next());
        }
        for (Element element : divList) {
            addHeadIfMissing(element);
        }

    }

    private void addHeadIfMissing(Element div) {
        if (div.getName().equals("div") && div.getChildren() != null && !div.getChildren().isEmpty() && !div.getChildren().get(0).getName().equals(
                "head")) {
            div.addContent(0, new Element("head", TEI));
        }
    }

    /**
     * @param div
     * @param elementName
     */
    public void makeChildOfDiv(Element div, String elementName) {
        IteratorIterable<Element> figureElements = div.getDescendants(org.jdom2.filter.Filters.element(elementName, null));
        List<Element> figureList = new ArrayList<>();
        while (figureElements.hasNext()) {
            Element figure = figureElements.next();
            figureList.add(figure);
        }
        for (Element figure : figureList) {
            Element parent = figure.getParentElement();
            while (parent != null && parent.getParentElement() != null && parent.getName().equals("p")) {
                int index = parent.indexOf(figure);
                figure.detach();
                if (isEmpty(parent)) {
                    Element grandParent = parent.getParentElement();
                    grandParent.addContent(grandParent.indexOf(parent), figure);
                    grandParent.removeContent(parent);
                    parent = grandParent;
                } else {
                    Element newDiv = new Element("div", TEI);
                    newDiv.addContent(figure);
                    parent.addContent(index, newDiv);
                    break;
                }
            }
        }
    }

    private boolean isEmpty(Element element) {
        return element.getTextTrim().isEmpty() && element.getChildren().isEmpty();
    }

    @Override
    protected boolean teiExistsForLanguage(LanguageEnum language) {
        Contribution contribution = getContribution(language);
        return contribution != null && StringUtils.isNotBlank(contribution.getContent());
    }

    @Override
    protected boolean isGerman(LanguageEnum language) {
        if (LanguageEnum.GERMAN.equals(language)) {
            return true;
        } else if (LanguageEnum.ORIGINAL.equals(language)) {
            return "ger".equals(getLanguageCodeFromContribution(language));
        } else {
            return false;
        }
    }

    protected String getTeiId() {
        return "GEI-contributions";
    }

}
