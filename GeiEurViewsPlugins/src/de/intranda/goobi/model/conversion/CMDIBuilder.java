package de.intranda.goobi.model.conversion;

import java.text.SimpleDateFormat;
import java.time.Instant;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

public class CMDIBuilder {

    static final String VIEWER_URL = "http://gei-worldviews.gei.de";
    static final Namespace CMDI = Namespace.getNamespace("cmdi", "http://www.clarin.eu/cmd/");
    static final Namespace CMDI_NOPREFIX = Namespace.getNamespace("http://www.clarin.eu/cmd/");
    static final Namespace TEI = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");
    //    static final Namespace TEI_NOPREFIX = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
    static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
    static final Namespace XSI = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    /**
     * 
     * @param pi
     * @param teiDoc
     * @param englishTeiDoc
     * @return
     * @should generate root element correctly
     */
    public static Document convertToCMDI(String pi, Document teiDoc, Document englishTeiDoc) {
        if (teiDoc == null) {
            throw new IllegalArgumentException("teiDoc may not be null");
        }

        Document doc = new Document();
        Element eleRoot = new Element("CMD", CMDI_NOPREFIX);
        eleRoot.addNamespaceDeclaration(XSI);
        eleRoot.setAttribute("CMDVersion", "1.1");
        eleRoot.setAttribute(new Attribute("schemaLocation",
                "http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1380106710826/xsd",
                XSI));
        doc.setRootElement(eleRoot);
        eleRoot.addContent(generateHeader(pi, teiDoc));
        eleRoot.addContent(generateResources(pi, teiDoc));
        eleRoot.addContent(generateComponents(teiDoc, englishTeiDoc));

        // System.out.println(CMDIBuilder.getStringFromElement(doc, null));
        return doc;
    }

    /**
     * 
     * @param pi
     * @param teiDoc
     * @return
     * @should create header correctly
     */
    static Element generateHeader(String pi, Document teiDoc) {
        Element eleHeader = new Element("Header", CMDI_NOPREFIX);
        {
            Element ele = new Element("MdCreator", CMDI_NOPREFIX);
            ele.setText("GEI - WorldViews");
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdCreationDate", CMDI_NOPREFIX);
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            ele.setText(dateFormat.format(now));
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdSelfLink", CMDI_NOPREFIX);
            String language = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:profileDesc/tei:langUsage/tei:language/text()", "");
            ele.setText(VIEWER_URL + "/rest/content/cmdi/" + pi + '/' + language + '/');
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdProfile", CMDI_NOPREFIX);
            ele.setText("clarin.eu:cr1:p_1380106710826");
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdCollectionDisplayName", CMDI_NOPREFIX);
            String value =
                    getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:authority/tei:orgName[@role='project']", "");
            ele.setText(value);
            eleHeader.addContent(ele);
        }

        return eleHeader;
    }

    /**
     * 
     * @param pi
     * @param teiDoc
     * @return
     * @should create resources correctly
     */
    static Element generateResources(String pi, Document teiDoc) {
        Element eleResources = new Element("Resources", CMDI_NOPREFIX);
        Element eleResourceProxyList = new Element("ResourceProxyList", CMDI_NOPREFIX);
        {
            Element eleResourceProxy = new Element("ResourceProxy", CMDI_NOPREFIX);
            eleResourceProxy.setAttribute("id", "rp_lp");

            Element eleResourceType = new Element("ResourceType", CMDI_NOPREFIX);
            eleResourceType.setAttribute("mimetype", "application/xhtml+xml");
            eleResourceType.setText("LandingPage");
            eleResourceProxy.addContent(eleResourceType);

            Element eleResourceRef = new Element("ResourceRef", CMDI_NOPREFIX);
            eleResourceRef.setText(VIEWER_URL + "/image/" + pi + "/1/");
            eleResourceProxy.addContent(eleResourceRef);

            eleResourceProxyList.addContent(eleResourceProxy);
        }
        {
            Element eleResourceProxy = new Element("ResourceProxy", CMDI_NOPREFIX);
            eleResourceProxy.setAttribute("id", "rp_tei");

            Element eleResourceType = new Element("ResourceType", CMDI_NOPREFIX);
            eleResourceType.setAttribute("mimetype", "application/tei+xml");
            eleResourceType.setText("Resource");
            eleResourceProxy.addContent(eleResourceType);

            Element eleResourceRef = new Element("ResourceRef", CMDI_NOPREFIX);
            String language = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:profileDesc/tei:langUsage/tei:language/text()", "");
            eleResourceRef.setText(VIEWER_URL + "/rest/content/tei/" + pi + '/' + language + '/');
            eleResourceProxy.addContent(eleResourceRef);

            eleResourceProxyList.addContent(eleResourceProxy);
        }
        eleResources.addContent(eleResourceProxyList);

        eleResources.addContent(new Element("JournalFileProxyList", CMDI_NOPREFIX));
        eleResources.addContent(new Element("ResourceRelationList", CMDI_NOPREFIX));
        eleResources.addContent(new Element("IsPartOfList", CMDI_NOPREFIX));

        return eleResources;
    }

    /**
     * 
     * @param teiDoc
     * @return
     * @should create components correctly
     */
    static Element generateComponents(Document teiDoc, Document englishTeiDoc) {
        Element eleComponents = new Element("Components", CMDI_NOPREFIX);

        if (teiDoc != null && teiDoc.getRootElement() != null && teiDoc.getRootElement()
                .getChild("teiHeader", TEI) != null) {
            String docLanguage = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[not(@type)]/@xml:lang", null);
            String level = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[not(@type)]/@level", null);
            Element eleMainTitle = null;

            Element eleTeiHeader = teiDoc.getRootElement()
                    .getChild("teiHeader", TEI)
                    .clone();
            //            eleTeiHeader.setNamespace(CMDI);

            // type
            Element eleType = new Element("type", CMDI_NOPREFIX);
            String typeValue = getFirstValue(teiDoc,
                    "tei:TEI/tei:teiHeader/tei:profileDesc/tei:textClass/tei:classCode[@scheme='WV.textType'][@xml:lang='eng']", null);
            if (typeValue == null && !"eng".equals(docLanguage)) {
                typeValue = getFirstValue(englishTeiDoc,
                        "tei:TEI/tei:teiHeader/tei:profileDesc/tei:textClass/tei:classCode[@scheme='WV.textType'][@xml:lang='eng']", null);
            }
            eleType.setText(typeValue);
            eleTeiHeader.addContent(0, eleType);

            // fileDesc
            Element eleFileDesc = eleTeiHeader.getChild("fileDesc", TEI);
            if (eleFileDesc != null) {
                eleFileDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315561");
                // titleStmt
                Element eleTitleStmt = eleFileDesc.getChild("titleStmt", TEI);
                if (eleTitleStmt != null) {
                    eleTitleStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372983");
                    // title
                    List<Element> eleListTitle = eleTitleStmt.getChildren("title", TEI);
                    if (eleListTitle != null && !eleListTitle.isEmpty()) {
                        List<Element> eleListTitleRemove = new ArrayList<>();
                        String title = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[not(type)]", null);
                        String englishTitle =
                                getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@xml:lang='eng']", null);
                        if (englishTitle == null) {
                            englishTitle =
                                    getFirstValue(englishTeiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@xml:lang='eng']", null);
                        }
                        eleTitleStmt.removeChildren("title", TEI);
                        // Add main title
                        if (title != null) {
                            eleMainTitle = new Element("title", CMDI_NOPREFIX);
                            eleMainTitle.setAttribute("level", level);
                            eleMainTitle.setAttribute("lang", docLanguage);
                            eleMainTitle.setText(title);
                            eleTitleStmt.addContent(0, eleMainTitle);
                            // Add English translation, if main title is not English
                            if (!"eng".equals(docLanguage) && englishTitle != null) {
                                Element eleEnglishTitle = new Element("title", CMDI_NOPREFIX);
                                eleEnglishTitle.setAttribute("level", level);
                                eleEnglishTitle.setAttribute("lang", "eng");
                                eleEnglishTitle.setAttribute("type", "translated");
                                eleEnglishTitle.setText(englishTitle);
                                eleTitleStmt.addContent(1, eleEnglishTitle);
                            }
                        }
                    }
                    // author
                    List<Element> eleListAuthor = eleTitleStmt.getChildren("author", TEI);
                    if (eleListAuthor != null && !eleListAuthor.isEmpty()) {
                        for (Element eleAuthor : eleListAuthor) {
                            Element elePersName = eleAuthor.getChild("persName", TEI);
                            if (elePersName != null && elePersName.getChild("surname", TEI) != null) {
                                String name = elePersName.getChildText("surname", TEI);
                                if (elePersName.getChild("forename", TEI) != null) {
                                    name += ", " + elePersName.getChildText("forename", TEI);
                                }
                                eleAuthor.removeChild("persName", TEI);
                                eleAuthor.setText(name);
                            }
                        }
                    }
                    // editor (translator)
                    List<Element> eleListEditor = eleTitleStmt.getChildren("editor", TEI);
                    if (eleListEditor != null && !eleListEditor.isEmpty()) {
                        for (Element eleEditor : eleListEditor) {
                            String persName = eleEditor.getChildText("persName", TEI);
                            if (persName != null) {
                                eleEditor.removeChild("persName", TEI);
                                eleEditor.setText(persName);
                            }
                        }
                    }
                }
                // editionStmt
                Element eleEditionStmt = eleFileDesc.getChild("editionStmt", TEI);
                if (eleEditionStmt != null) {
                    eleEditionStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1381926654590");
                    // edition
                    Element eleEdition = eleEditionStmt.getChild("edition", TEI);
                    if (eleEdition != null) {
                        Element eleNote = new Element("note", CMDI_NOPREFIX);
                        eleNote.setText(eleEdition.getText());
                        eleEdition.setText("");
                        eleEdition.removeAttribute("n");
                        eleEdition.addContent(eleNote);
                    }
                }
                // extent
                Element eleExtent = eleFileDesc.getChild("extent", TEI);
                if (eleExtent != null) {
                    eleExtent.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372984");
                    // measure -> num
                    Element eleMeasure = eleExtent.getChild("measure", TEI);
                    if (eleMeasure != null) {
                        eleMeasure.setName("num");
                        // @unit -> @type
                        Attribute attUnit = eleMeasure.getAttribute("unit");
                        if (attUnit != null) {
                            attUnit.setName("type");
                        }
                        // @quantity -> @n
                        Attribute attQuantity = eleMeasure.getAttribute("quantity");
                        if (attQuantity != null) {
                            attQuantity.setName("n");
                        }
                    }
                }
                // publicationStmt
                Element elePublicationStmt = eleFileDesc.getChild("publicationStmt", TEI);
                if (elePublicationStmt != null) {
                    elePublicationStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372991");
                    // publisher
                    String publisherValue = getFirstValue(teiDoc,
                            "tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:authority/tei:orgName[@role='hostingInstitution']", null);
                    if (publisherValue != null) {
                        Element elePublisher = new Element("publisher", CMDI_NOPREFIX);
                        elePublisher.setText(publisherValue);
                        elePublicationStmt.addContent(0, elePublisher);
                    }
                    elePublicationStmt.removeChild("authority", TEI);
                    // date
                    Element eleDate = elePublicationStmt.getChild("date", TEI);
                    // Remove @type
                    if (eleDate != null) {
                        eleDate.removeAttribute("type");
                    }
                    // availability
                    Element eleAvailability = elePublicationStmt.getChild("availability", TEI);
                    if (eleAvailability != null) {
                        eleAvailability.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372986");
                        eleAvailability.setAttribute("status", "restricted");
                        // ab
                        Element eleLicense = eleAvailability.getChild("licence", TEI);
                        if (eleLicense != null && eleLicense.getAttribute("target") != null) {
                            Element eleAb = new Element("ab", CMDI_NOPREFIX);
                            eleAb.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372985");
                            eleAb.setAttribute("type", eleLicense.getAttributeValue("target"));
                            eleAvailability.addContent(eleAb);
                            eleLicense.removeAttribute("target");
                        }
                    }
                }
                // notesStmt
                Element eleNotesStmt = eleFileDesc.getChild("notesStmt", TEI);
                if (eleNotesStmt != null) {
                    eleNotesStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372992");
                    // Remove @type
                    Element eleNote = eleNotesStmt.getChild("note", TEI);
                    if (eleNote != null) {
                        eleNote.removeAttribute("type");
                    }
                }
                // sourceDesc
                Element eleSourceDesc = eleFileDesc.getChild("sourceDesc", TEI);
                if (eleSourceDesc != null) {
                    eleSourceDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880373029");
                    // biblStruct
                    Element eleBiblStruct = new Element("biblStruct", CMDI_NOPREFIX);
                    eleBiblStruct.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315559");
                    eleSourceDesc.addContent(eleBiblStruct);
                    Element eleBiblFull = eleSourceDesc.getChild("biblFull", TEI);
                    if (eleBiblFull != null) {
                        // idno
                        Element eleBiblStructPublicationStmt = eleBiblFull.getChild("publicationStmt", TEI);
                        if (eleBiblStructPublicationStmt != null) {
                            List<Element> eleListIdno = eleBiblStructPublicationStmt.getChildren("idno", TEI);
                            if (eleListIdno != null && !eleListIdno.isEmpty()) {
                                for (Element eleIdno : eleListIdno) {
                                    String type = eleIdno.getAttributeValue("type");
                                    if (type != null) {
                                        switch (type) {
                                            case "ISBN":
                                            case "PPNa":
                                            case "PPNf":
                                            case "PPNc":
                                                eleBiblStruct.addContent(eleIdno.clone());
                                                break;
                                        }
                                    }
                                }
                            }

                        }
                        // monogr
                        Element eleMonogr = new Element("monogr", CMDI_NOPREFIX);
                        eleMonogr.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315552");
                        eleBiblStruct.addContent(eleMonogr);
                        {
                            Element eleBiblFullTitleStmt = eleBiblFull.getChild("titleStmt", TEI);
                            if (eleBiblFullTitleStmt != null) {
                                //  title
                                if ("m".equals(level)) {
                                    // Contribution
                                    if (eleMainTitle != null) {
                                        Element eleContributionTitle = eleMainTitle.clone();
                                        eleContributionTitle.removeAttribute("level");
                                        eleMonogr.addContent(eleContributionTitle);
                                    }
                                } else {
                                    // Source
                                    boolean translatedTitleAdded = false;
                                    List<Element> eleListTitle = eleBiblFullTitleStmt.getChildren("title", TEI);
                                    if (eleListTitle != null && !eleListTitle.isEmpty()) {
                                        Element eleNewMainTitle = null;
                                        String volNumber = null;
                                        String mainTitle = null;
                                        String subTitle = null;
                                        for (Element eleTitle : eleListTitle) {
                                            String type = eleTitle.getAttributeValue("type");
                                            String lang = eleTitle.getAttributeValue("lang", XML);
                                            if (!docLanguage.equals(lang)) {
                                                continue;
                                            }
                                            // Add original title and translated title if original is not English
                                            if (type != null) {
                                                switch (type) {
                                                    case "volume":
                                                        volNumber = eleTitle.getValue();
                                                        if (eleNewMainTitle == null) {
                                                            eleNewMainTitle = eleTitle.clone();
                                                            eleNewMainTitle.removeAttribute("level");
                                                            eleNewMainTitle.removeAttribute("type");
                                                            eleNewMainTitle.setAttribute("lang", docLanguage);
                                                            eleNewMainTitle.setText("");
                                                            eleMonogr.addContent(eleNewMainTitle);
                                                        }
                                                        break;
                                                    case "main":
                                                        mainTitle = eleTitle.getValue();
                                                        if (eleNewMainTitle == null) {
                                                            eleNewMainTitle = eleTitle.clone();
                                                            eleNewMainTitle.removeAttribute("level");
                                                            eleNewMainTitle.removeAttribute("type");
                                                            eleNewMainTitle.setAttribute("lang", docLanguage);
                                                            eleNewMainTitle.setText("");
                                                            eleMonogr.addContent(eleNewMainTitle);
                                                        }
                                                        break;
                                                    case "sub":
                                                        if (docLanguage.equals(lang)) {
                                                            subTitle = eleTitle.getValue();
                                                        }
                                                        break;
                                                    case "translated":
                                                        if ("eng".equals(lang)) {
                                                            Element eleNewEngTranslatedTitle = eleTitle.clone();
                                                            eleNewEngTranslatedTitle.removeAttribute("level");
                                                            eleNewEngTranslatedTitle.removeAttribute("type");
                                                            // replace @xml:lang with @lang
                                                            String language = eleNewEngTranslatedTitle.getAttributeValue("lang", XML);
                                                            if (language != null) {
                                                                eleNewEngTranslatedTitle.removeAttribute("lang", XML);
                                                                eleNewEngTranslatedTitle.setAttribute("lang", language);
                                                            }
                                                            eleMonogr.addContent(eleNewEngTranslatedTitle);
                                                            translatedTitleAdded = true;
                                                        }
                                                        break;
                                                }
                                            }
                                        }
                                        StringBuilder sb = new StringBuilder();
                                        if (volNumber != null) {
                                            sb.append(volNumber)
                                                    .append(" : ");
                                        }
                                        if (mainTitle != null) {
                                            sb.append(mainTitle);
                                        }
                                        if (subTitle != null) {
                                            if (sb.length() > 0) {
                                                sb.append(" : ");
                                            }
                                            sb.append(subTitle);
                                        }
                                        if (eleNewMainTitle != null && sb.length() > 0) {
                                            if (sb.toString()
                                                    .endsWith(" : ")) {
                                                sb.delete(sb.length() - 3, sb.length());
                                            }
                                            eleNewMainTitle.setText(sb.toString());
                                            // replace @xml:lang with @lang
                                            String language = eleNewMainTitle.getAttributeValue("lang", XML);
                                            if (language != null) {
                                                eleNewMainTitle.removeAttribute("lang", XML);
                                                eleNewMainTitle.setAttribute("lang", language);
                                            }
                                        }
                                    }

                                    if (!translatedTitleAdded) {
                                        String translatedTitle = getFirstValue(englishTeiDoc.getRootElement(),
                                                "tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:titleStmt/tei:title[@level='m'][@type='translated'][@xml:lang='eng']",
                                                null);
                                        if (eleListTitle != null) {
                                            Element eleNewEngTranslatedTitle = new Element("title", CMDI_NOPREFIX);
                                            eleNewEngTranslatedTitle.setText(translatedTitle);
                                            eleNewEngTranslatedTitle.setAttribute("lang", "eng");
                                            eleMonogr.addContent(eleNewEngTranslatedTitle);
                                            translatedTitleAdded = true;
                                        }
                                    }

                                }
                                // author
                                List<Element> eleListAuthor = eleBiblFullTitleStmt.getChildren("author", TEI);
                                if (eleListAuthor != null && !eleListAuthor.isEmpty()) {
                                    Element eleAuthors = new Element("author", CMDI_NOPREFIX);
                                    eleAuthors.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315551");
                                    eleMonogr.addContent(eleAuthors);
                                    for (Element eleAuthor : eleListAuthor) {
                                        Element elePersName = eleAuthor.getChild("persName", TEI);
                                        if (elePersName != null && elePersName.getChild("surname", TEI) != null) {
                                            String name = elePersName.getChildText("surname", TEI);
                                            if (elePersName.getChild("forename", TEI) != null) {
                                                name += ", " + elePersName.getChildText("forename", TEI);
                                            }
                                            Element eleName = new Element("name", CMDI_NOPREFIX);
                                            eleName.setText(name);
                                            eleAuthors.addContent(eleName);
                                        }
                                    }
                                }
                                // editor
                                List<Element> eleListEditor = eleBiblFullTitleStmt.getChildren("editor", TEI);
                                if (eleListEditor != null && !eleListEditor.isEmpty()) {
                                    Element eleEditors = new Element("editor", CMDI_NOPREFIX);
                                    eleEditors.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315553");
                                    eleMonogr.addContent(eleEditors);
                                    for (Element eleEditor : eleListEditor) {
                                        Element elePersName = eleEditor.getChild("persName", TEI);
                                        Element eleOrgName = eleEditor.getChild("orgName", TEI);
                                        if (elePersName != null && elePersName.getChild("surname", TEI) != null) {
                                            // Person
                                            String name = elePersName.getChildText("surname", TEI);
                                            if (elePersName.getChild("forename", TEI) != null) {
                                                name += ", " + elePersName.getChildText("forename", TEI);
                                            }
                                            Element eleName = new Element("name", CMDI_NOPREFIX);
                                            eleName.setText(name);
                                            eleEditors.addContent(eleName);
                                        } else if (eleOrgName != null) {
                                            // Corporate
                                            Element eleName = new Element("name", CMDI_NOPREFIX);
                                            eleName.setText(eleOrgName.getText());
                                            eleEditors.addContent(eleName);
                                        }
                                    }
                                }
                            }
                        }
                        // edition
                        String edition = getFirstValue(eleBiblFull, "tei:editionStmt/tei:edition", null);
                        if (edition != null) {
                            Element eleEdition = new Element("edition", CMDI_NOPREFIX);
                            eleEdition.setText(edition);
                            eleMonogr.addContent(eleEdition);
                        }
                        // imprint
                        Element eleImprint = new Element("imprint", CMDI_NOPREFIX);
                        eleImprint.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315555");
                        eleMonogr.addContent(eleImprint);
                        {
                            // pubPlace
                            String pubPlace = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:pubPlace", null);
                            if (pubPlace != null) {
                                Element elePubPlace = new Element("pubPlace", CMDI_NOPREFIX);
                                elePubPlace.setText(pubPlace);
                                eleImprint.addContent(elePubPlace);
                            }
                            // publisher
                            String publisher = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:publisher/tei:orgName", null);
                            if (publisher != null) {
                                Element elePublisher = new Element("publisher", CMDI_NOPREFIX);
                                elePublisher.setText(publisher);
                                eleImprint.addContent(elePublisher);
                            }
                            // date
                            String date = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:date", null);
                            String dateWhen = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:date/@when", null);
                            if (date != null) {
                                Element eleDate = new Element("date", CMDI_NOPREFIX);
                                eleDate.setAttribute("cert", "high");
                                eleDate.setAttribute("when", dateWhen);
                                eleDate.setText(date);
                                eleImprint.addContent(eleDate);
                            }
                        }
                        // extent
                        String pages = getFirstValue(eleBiblFull, "tei:extent/tei:measure[@unit='pages']", null);
                        if (pages != null) {
                            Element eleMonogrExtent = new Element("extent", CMDI_NOPREFIX);
                            eleMonogrExtent.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372984");
                            eleMonogr.addContent(eleMonogrExtent);
                            {
                                // num
                                Element eleNum = new Element("num", CMDI_NOPREFIX);
                                eleNum.setAttribute("type", "pages");
                                eleNum.setText(pages);
                                eleMonogrExtent.addContent(eleNum);
                            }
                        }
                    }
                    // relatedItem
                    Element eleSeriesStmt = eleBiblFull.getChild("seriesStmt", TEI);
                    if (eleSeriesStmt != null) {
                        Element eleRelatedItem = new Element("relatedItem", CMDI_NOPREFIX);
                        eleRelatedItem.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315557");
                        eleRelatedItem.setAttribute("type", "series");
                        eleBiblStruct.addContent(eleRelatedItem);
                        {
                            // bibl
                            Element eleBibl = new Element("bibl", CMDI_NOPREFIX);
                            eleBibl.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315556");
                            eleRelatedItem.addContent(eleBibl);
                            // title
                            String biblScope = eleSeriesStmt.getChildText("biblScope", TEI);
                            List<Element> eleListTitle = eleSeriesStmt.getChildren("title", TEI);
                            if (eleListTitle != null && !eleListTitle.isEmpty()) {
                                String origLanguage = getFirstValue(eleSeriesStmt, "tei:title[@level='s' and @type='main']/@xml:lang", null);
                                for (Element eleTitle : eleListTitle) {
                                    String type = eleTitle.getAttributeValue("type");
                                    String lang = eleTitle.getAttributeValue("lang", XML);
                                    // Add original title and translated title if original is not English
                                    if ("main".equals(type) || ("translated".equals(type) && "eng".equals(lang))) {
                                        Element eleNewTitle = eleTitle.clone();
                                        eleNewTitle.removeAttribute("level");
                                        eleNewTitle.removeAttribute("type");
                                        // replace @xml:lang with @lang
                                        String language = eleNewTitle.getAttributeValue("lang", XML);
                                        if (language != null) {
                                            eleNewTitle.removeAttribute("lang", XML);
                                            eleNewTitle.setAttribute("lang", language);
                                        }

                                        if (biblScope != null) {
                                            eleNewTitle.setText(eleNewTitle.getText() + " (" + biblScope + ")");
                                        }
                                        eleBibl.addContent(eleNewTitle);
                                    }
                                }
                            }
                        }
                    }
                    // Remove biblFull
                    eleSourceDesc.removeChild("biblFull", TEI);
                }
                // msDesc
                if (eleSourceDesc.getChild("msDesc", TEI) != null) {
                    Element eleMsDesc = eleSourceDesc.getChild("msDesc", TEI)
                            .clone();
                    eleMsDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712054");
                    // msIdentifier
                    Element eleMsIdentifier = eleMsDesc.getChild("msIdentifier", TEI);
                    if (eleMsIdentifier != null) {
                        eleMsIdentifier.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712039");
                        // idno (shelfmark)
                        Element eleIdno = eleMsIdentifier.getChild("idno", TEI);
                        if (eleIdno != null && eleIdno.getChild("idno", TEI) != null) {
                            Element eleIdnoShelfmark = eleIdno.getChild("idno", TEI)
                                    .clone();
                            eleMsIdentifier.removeChild("idno", TEI);
                            eleMsIdentifier.addContent(eleIdnoShelfmark);
                        }
                    }
                    // msContents
                    Element eleMsContents = eleMsDesc.getChild("msContents", TEI);
                    if (eleMsContents != null) {
                        eleMsContents.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712038");
                        //  msItem
                        Element eleTextLang = eleMsContents.getChild("textLang", TEI);
                        if (eleTextLang != null && eleTextLang.getAttributeValue("mainLang") != null) {
                            Element eleMsItem = new Element("msItem", CMDI_NOPREFIX);
                            eleMsItem.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712037");
                            eleMsItem.setAttribute("n", eleTextLang.getAttributeValue("mainLang"));
                            eleMsContents.addContent(eleMsItem);
                            eleMsContents.removeChild("textLang", TEI);
                        }
                    }
                    // Remove old msDesc element and add new, so that it comes after bibStruct
                    eleSourceDesc.removeChild("msDesc", TEI);
                    eleSourceDesc.addContent(eleMsDesc);
                }
            }

            // encodingDesc
            Element eleEncodingDesc = eleTeiHeader.getChild("encodingDesc", TEI);
            if (eleEncodingDesc != null) {
                eleEncodingDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315562");
                // samplingDecl
                Element eleSamplingDecl = eleEncodingDesc.getChild("samplingDecl", TEI);
                if (eleSamplingDecl != null) {
                    eleSamplingDecl.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372982");
                    // rename <p> to <ab>
                    Element eleP = eleSamplingDecl.getChild("p", TEI);
                    if (eleP != null) {
                        eleP.setName("ab");
                    }
                    eleSamplingDecl.removeAttribute("lang", XML);
                }
                // projectDesc
                //                Element eleProjectDesc = ;
                if (eleEncodingDesc.getChild("projectDesc", TEI) != null) {
                    Element eleProjectDesc = eleEncodingDesc.getChild("projectDesc", TEI)
                            .clone();
                    eleProjectDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372987");
                    // p -> ab
                    Element eleP = eleProjectDesc.getChild("p", TEI);
                    if (eleP != null) {
                        eleP.setName("ab");
                    }
                    eleProjectDesc.removeAttribute("lang", XML);
                    // remove and re-attach to make sure projectDesc comes after samplingDecl
                    eleEncodingDesc.removeChild("projectDesc", TEI);
                    eleEncodingDesc.addContent(eleProjectDesc);
                }
            }
            // profileDesc
            Element eleProfileDesc = eleTeiHeader.getChild("profileDesc", TEI);
            if (eleProfileDesc != null) {
                eleProfileDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1379925925653");
                // langUsage
                Element eleLangUsage = eleProfileDesc.getChild("langUsage", TEI);
                if (eleLangUsage != null) {
                    eleLangUsage.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880373021");
                }
                // textClass
                Element eleTextClass = eleProfileDesc.getChild("textClass", TEI);
                if (eleTextClass != null) {
                    eleTextClass.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880373027");
                    // classCode
                    List<Element> eleListClassCode = eleTextClass.getChildren("classCode", TEI);
                    eleTextClass.removeChildren("classCode", TEI);
                    // Make sure eleListClassCode comes from the English language file
                    if (!"eng".equals(docLanguage)) {
                        try {
                            eleListClassCode = englishTeiDoc.getRootElement()
                                    .getChild("teiHeader", TEI)
                                    .getChild("profileDesc", TEI)
                                    .getChild("textClass", TEI)
                                    .getChildren("classCode", TEI);
                        } catch (NullPointerException e) {
                        }
                    }
                    if (eleListClassCode != null && !eleListClassCode.isEmpty()) {
                        for (Element eleClassCode : eleListClassCode) {
                            if ("WV.placeOfUse".equals(eleClassCode.getAttributeValue("scheme"))) {
                                String value = eleClassCode.getChildText("rs", TEI);
                                eleClassCode.removeChild("rs", TEI);
                                eleClassCode.setText(value);
                            }
                            eleClassCode.removeAttribute("lang", XML);
                            eleTextClass.addContent(eleClassCode.clone());
                        }
                    }
                    // keywords
                    if (eleTextClass.getChild("keywords", TEI) != null) {
                        Element eleKeywords = eleTextClass.getChild("keywords", TEI)
                                .clone();
                        eleKeywords.setAttribute("ComponentId", "clarin.eu:cr1:c_1380613302381");
                        eleKeywords.setAttribute("scheme", "");
                        eleKeywords.removeAttribute("lang", XML);
                        Element eleList = new Element("list", CMDI_NOPREFIX);
                        eleList.setAttribute("ComponentId", "clarin.eu:cr1:c_1380613302392");
                        eleList.setAttribute("type", "");
                        eleKeywords.addContent(eleList);

                        Set<String> usedKeywords = new HashSet<>();
                        List<Element> eleListRs = evaluateToElements(eleKeywords, "tei:term/tei:rs[@type='keyword']");
                        if (eleListRs != null && !eleListRs.isEmpty()) {
                            for (Element eleRs : eleListRs) {
                                String key = eleRs.getAttributeValue("key");
                                if (!usedKeywords.contains(key)) {
                                    Element eleItem = new Element("item", CMDI_NOPREFIX);
                                    eleItem.setAttribute("n", key);
                                    eleItem.setText(eleRs.getText());
                                    eleList.addContent(eleItem);
                                    usedKeywords.add(key);
                                }
                            }
                        }
                        // remove term elements
                        eleKeywords.removeChildren("term", TEI);
                        // remove and re-attach keywords to make sure it comes after classCode
                        eleTextClass.removeChild("keywords", TEI);
                        eleTextClass.addContent(eleKeywords);
                    }
                }

                // remove abstracts
                eleProfileDesc.removeChildren("abstract", TEI);
            }

            changeNamespaceTo(eleTeiHeader, CMDI_NOPREFIX);
            eleComponents.addContent(eleTeiHeader);
        }

        // System.out.println(CMDIBuilder.getStringFromElement(eleComponents, null));
        return eleComponents;
    }

    /**
     * 
     * @param ele
     * @param xpath
     * @return
     */
    public static List<Element> evaluateToElements(Element ele, String xpath) {
        List<Element> retList = new ArrayList<>();

        XPathExpression<? extends Object> expr = XPathFactory.instance()
                .compile(xpath, Filters.element(), null, CMDI, TEI, XML);
        List<? extends Object> list = expr.evaluate(ele);
        if (list == null) {
            return null;
        }
        for (Object object : list) {
            if (object instanceof Element) {
                Element element = (Element) object;
                retList.add(element);
            }
        }

        return retList;
    }

    /**
     * 
     * @param ele
     * @param xpath
     * @param defaultValue
     * @return
     */
    public static String getFirstValue(Object ele, String xpath, String defaultValue) {
        XPathExpression<? extends Object> expr = XPathFactory.instance()
                .compile(xpath, Filters.fpassthrough(), null, CMDI, TEI, XML, XSI);
        Object object = expr.evaluateFirst(ele);
        if (object != null) {
            String text = getText(object);
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
        }
        return defaultValue;
    }

    /**
     * @param object
     * @return
     */
    private static String getText(Object object) {
        String text;
        if (object instanceof Content) {
            text = ((Content) object).getValue();
        } else if (object instanceof Attribute) {
            text = ((Attribute) object).getValue();
        } else if (object instanceof Text) {
            text = ((Text) object).getTextTrim();
        } else {
            text = object.toString();
        }
        return text;
    }

    /**
     * @param element
     * @param encoding
     * @return
     * @should return XML string correctly for documents
     * @should return XML string correctly for elements
     */
    public static String getStringFromElement(Object element, String encoding) {
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        if (encoding == null) {
            encoding = "UTF-8";
        }
        Format format = Format.getRawFormat();
        XMLOutputter outputter = new XMLOutputter(format);
        Format xmlFormat = outputter.getFormat();
        if (StringUtils.isNotEmpty(encoding)) {
            xmlFormat.setEncoding(encoding);
        }
        xmlFormat.setExpandEmptyElements(true);
        outputter.setFormat(xmlFormat);

        String docString = null;
        if (element instanceof Document) {
            docString = outputter.outputString((Document) element);
        } else if (element instanceof Element) {
            docString = outputter.outputString((Element) element);
        }
        return docString;
    }

    /**
     * Recursively replaces the namespace of the given element and all its children to the given namespace.
     * 
     * @param element
     * @param namespace
     */
    public static void changeNamespaceTo(Element element, Namespace namespace) {
        if (element != null) {
            element.setNamespace(namespace);
            for (Element child : element.getChildren()) {
                changeNamespaceTo(child, namespace);
            }
        }
    }
}
