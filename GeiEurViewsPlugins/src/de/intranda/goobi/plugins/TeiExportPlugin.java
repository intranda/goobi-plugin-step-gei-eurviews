package de.intranda.goobi.plugins;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.DatabaseManager;
import jdk.nashorn.internal.ir.ForNode;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@Log4j
public class TeiExportPlugin implements IStepPlugin, IPlugin {

    private static final String PLUGIN_NAME = "RtfToTeiExport";

    private static final Namespace TEI = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
    private static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    private Step step;
    private Process process;
    private String returnPath;

    private BibliographicData bibliographicData;
    private List<Context> descriptionList;
    private List<Transcription> transcriptionList;
    private List<Image> currentImages;

    private java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

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
            bibliographicData = DatabaseManager.getBibliographicData(process.getId());
            descriptionList = DatabaseManager.getDescriptionList(process.getId());
            transcriptionList = DatabaseManager.getTransciptionList(process.getId());
            currentImages = DatabaseManager.getImages(process.getId());
        } catch (SQLException e) {
            log.error(e);
        }

    }

    @Override
    public boolean execute() {
        Document teiDocument = new Document();
        Element teiRoot = new Element("TEI", TEI);
        teiDocument.setRootElement(teiRoot);
        teiRoot.setAttribute("id", "dtabf", XML);
        teiRoot.setAttribute("version", "5.0");

        Element teiHeader = createHeader();
        teiRoot.addContent(teiHeader);

        return false;
    }

    private Element createTitleStmt() {
        Element titleStmt = new Element("titleStmt", TEI);

        if (StringUtils.isNotBlank(bibliographicData.getMaintitleGerman())) {
            Element title = new Element("title", TEI);
            //            title.setAttribute("lang", "ger", XML);
            title.setText(bibliographicData.getMaintitleGerman());
            titleStmt.addContent(title);
        }
        //        if (StringUtils.isNotBlank(bibliographicData.getMaintitleEnglish())) {
        //            Element title = new Element("title", TEI);
        //            title.setAttribute("lang", "eng", XML);
        //            title.setText(bibliographicData.getMaintitleEnglish());
        //        }

        for (Transcription transcription : transcriptionList) {
            for (SimpleMetadataObject person : transcription.getTranslatorList()) {

                Element editor = new Element("editor", TEI);
                titleStmt.addContent(editor);
                Element persName = new Element("persName", TEI);
                // TODO
                persName.setAttribute("ref", "");
                editor.addContent(persName);
                persName.setAttribute("role", "translator");
                persName.setText(person.getValue());
            }
        }
        return titleStmt;
    }

    private Element createEditionStmt() {
        Element editionStmt = new Element("editionStmt", TEI);

        Element edition = new Element("edition", TEI);
        editionStmt.addContent(edition);
        // TODO
        edition.setAttribute("n", bibliographicData.getEdition());
        edition.setText(bibliographicData.getEdition());

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
            Element author = new Element("author", TEI);
            seriesStmt.addContent(author);
            Element persName = new Element("persName", TEI);
            author.addContent(persName);
            // TODO
            persName.setAttribute("ref", "");
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
        //       orgName1.setAttribute("ref", "edu.experts.id");
        orgName1.setAttribute("role", "hostingInstitution");
        orgName1.setText("Georg-Eckert-Institut");
        authority.addContent(orgName1);
        Element orgName2 = new Element("orgName", TEI);
        //     orgName2.setAttribute("ref", "edu.experts.id");
        orgName2.setAttribute("role", "project");
        orgName2.setText("WorldViews");
        authority.addContent(orgName2);

        Element date = new Element("date", TEI);
        String dateString = formatter.format(new Date());
        date.setAttribute("when", dateString);
        date.setAttribute("type", "publication");
        date.setText(dateString);
        publicationStmt.addContent(date);

        Element availability = new Element("availability", TEI);
        publicationStmt.addContent(availability);
        Element p = new Element("p", TEI);
        // TODO Text aus Konfiguration nehmen?
        p.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam");
        availability.addContent(p);

        Element licence = new Element("licence", TEI);
        // TODO which licence?
        licence.setAttribute("target", "http://creativecommons.org/licenses/by-sa/3.0/");
        licence.setText("Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)");
        availability.addContent(licence);

        Element idnoUrn = new Element("idno", TEI);
        idnoUrn.setAttribute("type", "URN");
        // TODO 
        //        idnoUrn.setText("");
        publicationStmt.addContent(idnoUrn);
        Element idnoUPIDCMDI = new Element("idno", TEI);
        idnoUPIDCMDI.setAttribute("type", "PIDCMDI");
        //        idnoUPIDCMDI.setText("");
        publicationStmt.addContent(idnoUPIDCMDI);

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
            Element author = new Element("author", TEI);
            titleStmt.addContent(author);
            Element persName = new Element("persName", TEI);
            author.addContent(persName);
            // TODO
            persName.setAttribute("ref", "");
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

        return titleStmt;
    }

    private Element createSourceDesc() {
        Element sourceDesc = new Element("sourceDesc", TEI);

        Element biblFull = new Element("biblFull", TEI);

        sourceDesc.addContent(biblFull);
        Element titleStmt = createBbiliographicTitleStmt();
        biblFull.addContent(titleStmt);

        Element editionStmt = createEditionStmt();
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
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
        projectDesc.addContent(p);

        Element samplingDecl = new Element("samplingDecl", TEI);
        Element p2 = new Element("p", TEI);
        p2.setText("consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua");
        samplingDecl.addContent(p2);
        encodingDesc.addContent(samplingDecl);

        return encodingDesc;
    }

    private Element createHeader() {
        Element teiHeader = new Element("teiHeader", TEI);
        Element fileDesc = new Element("fileDesc", TEI);
        teiHeader.addContent(fileDesc);

        Element titleStmt = createTitleStmt();
        fileDesc.addContent(titleStmt);

        Element editionStmt = createEditionStmt();
        fileDesc.addContent(editionStmt);

        Element extent = createExtent(null);
        fileDesc.addContent(extent);

        Element publicationStmt = createPublicationStmt();
        fileDesc.addContent(publicationStmt);

        Element sourceDesc = createSourceDesc();
        fileDesc.addContent(sourceDesc);

        Element encodingDesc = createEncodingDesc();
        teiHeader.addContent(encodingDesc);

        Element profileDesc = createProfileDesc();
        teiHeader.addContent(profileDesc);

        Element revisionDesc = createRevisionDesc();
        teiHeader.addContent(revisionDesc);

        return teiHeader;
    }

    private Element createProfileDesc() {
        Element profileDesc = new Element("profileDesc", TEI);
        Element langUsage = new Element("langUsage", TEI);
        profileDesc.addContent(langUsage);

        for (SimpleMetadataObject currentLanguage : bibliographicData.getLanguageList()) {
            Element language = new Element("language", TEI);
            language.setAttribute("ident", currentLanguage.getValue());
            language.setText(currentLanguage.getValue());
            langUsage.addContent(language);
        }
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

        for (Context context : descriptionList) {
            if (!context.getShortDescription().isEmpty()) {
                Element teiContent = parseData(context.getShortDescription());

                Element abstractElement = new Element("abstract", TEI);
                abstractElement.setAttribute("lang", context.getLanguage(), XML);
                abstractElement.addContent(teiContent);
                profileDesc.addContent(abstractElement);
            }
        }

        Element textClass = new Element("textClass", TEI);
        profileDesc.addContent(textClass);
//        <textClass>
//        <!-- markierte Themenfelder -->
//            <keywords scheme="WV.topics" xml:lang="ger">
//                <term>[Themenfeld] - [Schlagwort]</term>
//                <term>[Themenfeld] - [Schlagwort]</term>
//            </keywords>
//            <!-- Quellentyp - Feld ist zur Zeit nicht wiederholbar -->
//            <classCode scheme="WV.sourceType">[Quellenart]</classCode>
//            <classCode scheme="WV.sourceType">[Quellenart]</classCode>
//            <!-- ??? -->
//            <classCode scheme="WV.textType">[Textart]</classCode>
//            <classCode scheme="WV.textType">[Textart]</classCode>
//        </textClass>
        
        return profileDesc;
    }

    private Element parseData(String shortDescription) {
        Element p = new Element("p", TEI);
        // TODO
        p.setText(shortDescription);
        return p;
    }

    private Element createRevisionDesc() {
        Element revisionDesc = new Element("revisionDesc", TEI);

        Element change = new Element("change", TEI);
        revisionDesc.addContent(change);
        // TODO
        change.setAttribute("when", "20161124");
        change.setText("initialer export");

        return revisionDesc;
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

}
