package de.intranda.goobi.model.conversion;

import java.text.SimpleDateFormat;
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

public class CMDIBuilder {

    static final String VIEWER_URL = "http://worldviews.gei.de";
    static final Namespace CMDI = Namespace.getNamespace("cmd", "http://www.clarin.eu/cmd/1");
    //    static final Namespace CMDI_NOPREFIX = Namespace.getNamespace("http://www.clarin.eu/cmd/1");
    static final Namespace TEI = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");
    static final Namespace COMPONENTS = Namespace.getNamespace("cmdp", "http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1380106710826");
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
        if (englishTeiDoc == null) {
            throw new IllegalArgumentException("englishTeiDoc may not be null");
        }

        Document doc = new Document();
        Element eleRoot = new Element("CMD", CMDI);
        eleRoot.addNamespaceDeclaration(XSI);
        eleRoot.addNamespaceDeclaration(COMPONENTS);
        eleRoot.setAttribute("CMDVersion", "1.2");
        eleRoot.setAttribute(new Attribute("schemaLocation",
                "http://www.clarin.eu/cmd/1 https://infra.clarin.eu/CMDI/1.x/xsd/cmd-envelop.xsd http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1380106710826 https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1380106710826/xsd",
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
        Element eleHeader = new Element("Header", CMDI);
        String language = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:profileDesc/tei:langUsage/tei:language/text()", "");
        {
            Element ele = new Element("MdCreator", CMDI);
            ele.setText("GEI - WorldViews");
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdCreationDate", CMDI);
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            ele.setText(dateFormat.format(now));
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdSelfLink", CMDI);
            ele.setText(VIEWER_URL + "/rest/content/cmdi/" + pi + '/' + language + '/');
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdProfile", CMDI);
            ele.setText("clarin.eu:cr1:p_1380106710826");
            eleHeader.addContent(ele);
        }
        {
            Element ele = new Element("MdCollectionDisplayName", CMDI);
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
        Element eleResources = new Element("Resources", CMDI);
        Element eleResourceProxyList = new Element("ResourceProxyList", CMDI);
        String language = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:profileDesc/tei:langUsage/tei:language/text()", "");
        {
            Element eleResourceProxy = new Element("ResourceProxy", CMDI);
            eleResourceProxy.setAttribute("id", "rp_lp");

            Element eleResourceType = new Element("ResourceType", CMDI);
            eleResourceType.setAttribute("mimetype", "application/xhtml+xml");
            eleResourceType.setText("LandingPage");
            eleResourceProxy.addContent(eleResourceType);

            Element eleResourceRef = new Element("ResourceRef", CMDI);
            eleResourceRef.setText(VIEWER_URL + "/open/" + pi + "/" + language + "/");
            eleResourceProxy.addContent(eleResourceRef);

            eleResourceProxyList.addContent(eleResourceProxy);
        }
        {
            Element eleResourceProxy = new Element("ResourceProxy", CMDI);
            eleResourceProxy.setAttribute("id", "rp_tei");

            Element eleResourceType = new Element("ResourceType", CMDI);
            eleResourceType.setAttribute("mimetype", "application/tei+xml");
            eleResourceType.setText("Resource");
            eleResourceProxy.addContent(eleResourceType);

            Element eleResourceRef = new Element("ResourceRef", CMDI);
            eleResourceRef.setText(VIEWER_URL + "/rest/content/tei/" + pi + '/' + language + '/');
            eleResourceProxy.addContent(eleResourceRef);

            eleResourceProxyList.addContent(eleResourceProxy);
        }
        eleResources.addContent(eleResourceProxyList);

        eleResources.addContent(new Element("JournalFileProxyList", CMDI));
        eleResources.addContent(new Element("ResourceRelationList", CMDI));
        // eleResources.addContent(new Element("IsPartOfList", CMDI));

        return eleResources;
    }

    /**
     * 
     * @param teiDoc
     * @return
     * @should create components correctly
     */
    static Element generateComponents(Document teiDoc, Document englishTeiDoc) {
        Element eleComponents = new Element("Components", CMDI);

        if (teiDoc != null && teiDoc.getRootElement() != null && teiDoc.getRootElement().getChild("teiHeader", TEI) != null) {
            String docLanguage = getFirstValue(teiDoc, "tei:TEI/tei:text/@xml:lang", null);
            String origLanguage = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[not(@type)]/@xml:lang",
                    getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msContents/tei:textLang/@mainLang", ""));
            String level = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[not(@type='translated')]/@level", "");
            Element eleOrigTitle = null;

            Element eleTeiHeader = teiDoc.getRootElement().getChild("teiHeader", TEI).clone();
            //            eleTeiHeader.setNamespace(CMDI);

            // type
            Element eleType = new Element("type", COMPONENTS);
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
                eleFileDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315561", CMDI);
                // titleStmt
                Element eleTitleStmt = eleFileDesc.getChild("titleStmt", TEI);
                if (eleTitleStmt != null) {
                    eleTitleStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372983", CMDI);
                    // Add original source title
                    String origSourceTitle = getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[not(@type)]", null);
                    eleTitleStmt.removeChildren("title", TEI);
                    if (origSourceTitle != null) {
                        eleOrigTitle = new Element("title", COMPONENTS);
                        if (StringUtils.isNotBlank(level)) {
                            eleOrigTitle.setAttribute("level", level);
                        }
                        eleOrigTitle.setAttribute("lang", origLanguage);
                        eleOrigTitle.setText(origSourceTitle);
                        eleTitleStmt.addContent(0, eleOrigTitle);
                    }
                    // Add English translation, if main source title is not English
                    String englishSourceTitle =
                            getFirstValue(teiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@xml:lang='eng']", null);
                    if (englishSourceTitle == null) {
                        englishSourceTitle =
                                getFirstValue(englishTeiDoc, "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@xml:lang='eng']", null);
                    }
                    if (!"eng".equals(origLanguage) && englishSourceTitle != null) {
                        Element eleEnglishTitle = new Element("title", COMPONENTS);
                        if (StringUtils.isNotBlank(level)) {
                            eleEnglishTitle.setAttribute("level", level);
                        }
                        eleEnglishTitle.setAttribute("lang", "eng");
                        eleEnglishTitle.setAttribute("type", "translated");
                        eleEnglishTitle.setText(englishSourceTitle);
                        eleTitleStmt.addContent(1, eleEnglishTitle);
                    }
                    // author
                    List<Element> eleListAuthor = eleTitleStmt.getChildren("author", TEI);
                    if (eleListAuthor != null && !eleListAuthor.isEmpty()) {
                        for (Element eleAuthor : eleListAuthor) {
                            Element elePersName = eleAuthor.getChild("persName", TEI);
                            if (elePersName != null) {
                                if (elePersName.getChild("surname", TEI) != null) {
                                    String name = elePersName.getChildText("surname", TEI);
                                    if (elePersName.getChild("forename", TEI) != null) {
                                        name += ", " + elePersName.getChildText("forename", TEI);
                                    }
                                    eleAuthor.setText(name);
                                } else if (StringUtils.isNotEmpty(elePersName.getText())) {
                                    eleAuthor.setText(elePersName.getText());
                                }
                                eleAuthor.removeChild("persName", TEI);
                            }
                        }
                    }
                    // editor (translator)
                    List<Element> eleListEditor = eleTitleStmt.getChildren("editor", TEI);
                    if (eleListEditor != null && !eleListEditor.isEmpty()) {
                        for (Element eleEditor : eleListEditor) {
                            String persName = eleEditor.getChildText("persName", TEI);
                            String orgName = eleEditor.getChildText("orgName", TEI);
                            if (persName != null) {
                                eleEditor.removeChild("persName", TEI);
                                eleEditor.setText(persName);
                            } else if (orgName != null) {
                                eleEditor.removeChild("orgName", TEI);
                                eleEditor.setText(persName);
                            }
                        }
                    }
                }
                // editionStmt
                Element eleEditionStmt = eleFileDesc.getChild("editionStmt", TEI);
                if (eleEditionStmt != null) {
                    eleEditionStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1381926654590", CMDI);
                    // edition
                    Element eleEdition = eleEditionStmt.getChild("edition", TEI);
                    if (eleEdition != null) {
                        Element eleNote = new Element("note", COMPONENTS);
                        eleNote.setText(eleEdition.getText());
                        eleEdition.setText("");
                        eleEdition.removeAttribute("n");
                        eleEdition.addContent(eleNote);
                    }
                }
                // extent
                Element eleExtent = eleFileDesc.getChild("extent", TEI);
                if (eleExtent != null) {
                    eleExtent.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372984", CMDI);
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
                    elePublicationStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372991", CMDI);
                    // publisher
                    String publisherValue = getFirstValue(teiDoc,
                            "tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:authority/tei:orgName[@role='hostingInstitution']", null);
                    if (publisherValue == null) {
                        // Contributions have a different path
                        publisherValue = getFirstValue(teiDoc,
                                "tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:publisher/tei:orgName[@role='hostingInstitution']", null);
                    }
                    elePublicationStmt.removeChild("publisher", TEI);
                    if (publisherValue != null) {
                        Element elePublisher = new Element("publisher", COMPONENTS);
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
                        eleAvailability.removeAttribute("lang", XML);
                        // Remove <p>
                        eleAvailability.removeChild("p", TEI);
                    } else {
                        eleAvailability = new Element("availability", COMPONENTS);
                        elePublicationStmt.addContent(eleAvailability);
                    }
                    eleAvailability.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372986", CMDI);
                    eleAvailability.setAttribute("status", "restricted");

                    // ab
                    Element eleAb = new Element("ab", COMPONENTS);
                    eleAb.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372985", CMDI);
                    eleAvailability.addContent(eleAb);
                    // license
                    Element eleLicense = eleAvailability.getChild("licence", TEI);
                    if (eleLicense != null && eleLicense.getAttribute("target") != null) {
                        eleAb.setAttribute("type", eleLicense.getAttributeValue("target"));
                        eleLicense.removeAttribute("target");
                    }
                }
                // notesStmt
                Element eleNotesStmt = eleFileDesc.getChild("notesStmt", TEI);
                if (eleNotesStmt != null) {
                    eleNotesStmt.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372992", CMDI);
                    // Remove @type
                    Element eleNote = eleNotesStmt.getChild("note", TEI);
                    if (eleNote != null) {
                        eleNote.removeAttribute("type");
                    }
                }
                // sourceDesc
                Element eleSourceDesc = eleFileDesc.getChild("sourceDesc", TEI);
                if (eleSourceDesc != null) {
                    eleSourceDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880373029", CMDI);
                    // Remove <p>born digital</p>
                    if (getFirstValue(eleSourceDesc, "tei:p[text() = 'born digital']/text()", null) != null) {
                        eleSourceDesc.removeChild("p", TEI);
                    }
                    // biblStruct
                    Element eleBiblStruct = new Element("biblStruct", COMPONENTS);
                    eleBiblStruct.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315559", CMDI);
                    eleSourceDesc.addContent(eleBiblStruct);
                    // monogr
                    Element eleMonogr = new Element("monogr", COMPONENTS);
                    eleMonogr.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315552", CMDI);
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
                        // Add monogr after idno
                        eleBiblStruct.addContent(eleMonogr);
                        {
                            Element eleBiblFullTitleStmt = eleBiblFull.getChild("titleStmt", TEI);
                            if (eleBiblFullTitleStmt != null) {
                                //  title
                                boolean translatedTitleAdded = false;
                                List<Element> eleListSchoolbookTitle = evaluateToElements(eleBiblFullTitleStmt, "tei:title[@level='m']");
                                if (eleListSchoolbookTitle != null && !eleListSchoolbookTitle.isEmpty()) {
                                    Element eleNewMainSchoolbookTitle = null;
                                    String volNumber = null;
                                    String mainTitle = null;
                                    String subTitle = null;
                                    for (Element eleSchoolbookTitle : eleListSchoolbookTitle) {
                                        String type = eleSchoolbookTitle.getAttributeValue("type");
                                        String lang = eleSchoolbookTitle.getAttributeValue("lang", XML);
                                        // Add original title and translated title if original is not English
                                        if (type != null) {
                                            switch (type) {
                                                case "volume":
                                                    volNumber = eleSchoolbookTitle.getValue();
                                                    if (eleNewMainSchoolbookTitle == null) {
                                                        eleNewMainSchoolbookTitle = eleSchoolbookTitle.clone();
                                                        eleNewMainSchoolbookTitle.removeAttribute("level");
                                                        eleNewMainSchoolbookTitle.removeAttribute("type");
                                                        eleNewMainSchoolbookTitle.setAttribute("lang", origLanguage);
                                                        eleNewMainSchoolbookTitle.setText("");
                                                        eleMonogr.addContent(eleNewMainSchoolbookTitle);
                                                    }
                                                    break;
                                                case "main":
                                                    mainTitle = eleSchoolbookTitle.getValue();
                                                    if (eleNewMainSchoolbookTitle == null) {
                                                        eleNewMainSchoolbookTitle = eleSchoolbookTitle.clone();
                                                        eleNewMainSchoolbookTitle.removeAttribute("level");
                                                        eleNewMainSchoolbookTitle.removeAttribute("type");
                                                        eleNewMainSchoolbookTitle.setAttribute("lang", origLanguage);
                                                        eleNewMainSchoolbookTitle.setText("");
                                                        eleMonogr.addContent(eleNewMainSchoolbookTitle);
                                                    }
                                                    break;
                                                case "sub":
                                                    subTitle = eleSchoolbookTitle.getValue();
                                                    break;
                                                case "translated":
                                                    if ("eng".equals(lang)) {
                                                        Element eleNewEngTranslatedTitle = eleSchoolbookTitle.clone();
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
                                        sb.append(volNumber).append(" : ");
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
                                    if (eleNewMainSchoolbookTitle != null && sb.length() > 0) {
                                        if (sb.toString().endsWith(" : ")) {
                                            sb.delete(sb.length() - 3, sb.length());
                                        }
                                        eleNewMainSchoolbookTitle.setText(sb.toString());
                                        // replace @xml:lang with @lang
                                        String language = eleNewMainSchoolbookTitle.getAttributeValue("lang", XML);
                                        if (language != null) {
                                            eleNewMainSchoolbookTitle.removeAttribute("lang", XML);
                                            eleNewMainSchoolbookTitle.setAttribute("lang", language);
                                        }
                                    }
                                }
                                if (!translatedTitleAdded && !"eng".equals(origLanguage)) {
                                    String translatedTitle = getFirstValue(englishTeiDoc.getRootElement(),
                                            "tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:titleStmt/tei:title[@level='m'][@type='translated'][@xml:lang='eng']",
                                            null);
                                    if (eleListSchoolbookTitle != null) {
                                        Element eleNewEngTranslatedTitle = new Element("title", COMPONENTS);
                                        eleNewEngTranslatedTitle.setText(translatedTitle);
                                        eleNewEngTranslatedTitle.setAttribute("lang", "eng");
                                        eleMonogr.addContent(eleNewEngTranslatedTitle);
                                        translatedTitleAdded = true;
                                    }
                                }

                                // edition
                                String edition = getFirstValue(eleBiblFull, "tei:editionStmt/tei:edition", null);
                                if (edition != null) {
                                    Element eleEdition = new Element("edition", COMPONENTS);
                                    eleEdition.setText(edition);
                                    eleMonogr.addContent(eleEdition);
                                }
                                // author
                                List<Element> eleListAuthor = eleBiblFullTitleStmt.getChildren("author", TEI);
                                if (eleListAuthor != null && !eleListAuthor.isEmpty()) {
                                    Element eleAuthors = new Element("author", COMPONENTS);
                                    eleAuthors.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315551", CMDI);
                                    eleMonogr.addContent(eleAuthors);
                                    for (Element eleAuthor : eleListAuthor) {
                                        Element elePersName = eleAuthor.getChild("persName", TEI);
                                        if (elePersName != null) {
                                            String name = null;
                                            if (elePersName.getChild("surname", TEI) != null) {
                                                name = elePersName.getChildText("surname", TEI);
                                                if (elePersName.getChild("forename", TEI) != null) {
                                                    name += ", " + elePersName.getChildText("forename", TEI);
                                                }

                                            } else if (StringUtils.isNotEmpty(elePersName.getText())) {
                                                name = elePersName.getText();
                                            }
                                            Element eleName = new Element("name", COMPONENTS);
                                            eleName.setText(name);
                                            eleAuthors.addContent(eleName);
                                        }
                                    }
                                }
                                // editor
                                List<Element> eleListEditor = eleBiblFullTitleStmt.getChildren("editor", TEI);
                                if (eleListEditor != null && !eleListEditor.isEmpty()) {
                                    Element eleEditors = new Element("editor", COMPONENTS);
                                    eleEditors.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315553", CMDI);
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
                                            Element eleName = new Element("name", COMPONENTS);
                                            eleName.setText(name);
                                            eleEditors.addContent(eleName);
                                        } else if (eleOrgName != null) {
                                            // Corporate
                                            Element eleName = new Element("name", COMPONENTS);
                                            eleName.setText(eleOrgName.getText());
                                            eleEditors.addContent(eleName);
                                        }
                                    }
                                }
                            }
                        }
                        // imprint
                        Element eleImprint = new Element("imprint", COMPONENTS);
                        eleImprint.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315555", CMDI);
                        eleMonogr.addContent(eleImprint);
                        {
                            // pubPlace
                            String pubPlace = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:pubPlace", null);
                            if (pubPlace != null) {
                                Element elePubPlace = new Element("pubPlace", COMPONENTS);
                                elePubPlace.setText(pubPlace);
                                eleImprint.addContent(elePubPlace);
                            }
                            // publisher
                            String publisher = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:publisher/tei:orgName", null);
                            if (publisher == null) {
                                publisher = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:publisher", null);
                            }
                            if (publisher != null) {
                                Element elePublisher = new Element("publisher", COMPONENTS);
                                elePublisher.setText(publisher);
                                eleImprint.addContent(elePublisher);
                            }
                            // date
                            String date = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:date", null);
                            String dateWhen = getFirstValue(eleBiblFull, "tei:publicationStmt/tei:date/@when", null);
                            Element eleDate = new Element("date", COMPONENTS);
                            eleImprint.addContent(eleDate);
                            if (date != null) {
                                eleDate.setAttribute("cert", "high");
                                eleDate.setAttribute("when", dateWhen);
                                eleDate.setText(date);
                            }
                        }
                        // extent
                        String pages = getFirstValue(eleBiblFull, "tei:extent/tei:measure[@unit='pages']", null);
                        if (pages != null) {
                            Element eleMonogrExtent = new Element("extent", COMPONENTS);
                            eleMonogrExtent.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372984", CMDI);
                            eleMonogr.addContent(eleMonogrExtent);
                            {
                                // num
                                Element eleNum = new Element("num", COMPONENTS);
                                eleNum.setAttribute("type", "pages");
                                eleNum.setText(pages);
                                eleMonogrExtent.addContent(eleNum);
                            }
                        }
                        // relatedItem
                        Element eleSeriesStmt = eleBiblFull.getChild("seriesStmt", TEI);
                        if (eleSeriesStmt != null) {
                            Element eleRelatedItem = new Element("relatedItem", COMPONENTS);
                            eleRelatedItem.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315557", CMDI);
                            eleRelatedItem.setAttribute("type", "series");
                            eleBiblStruct.addContent(eleRelatedItem);
                            {
                                // bibl
                                Element eleBibl = new Element("bibl", COMPONENTS);
                                eleBibl.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315556", CMDI);
                                eleRelatedItem.addContent(eleBibl);
                                // title
                                String biblScope = eleSeriesStmt.getChildText("biblScope", TEI);
                                List<Element> eleListTitle = eleSeriesStmt.getChildren("title", TEI);
                                if (eleListTitle != null && !eleListTitle.isEmpty()) {
                                    // String origLanguage = getFirstValue(eleSeriesStmt, "tei:title[@level='s' and @type='main']/@xml:lang", null);
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
                    } else if ("m".equals(level)) {
                        // Contributions have no biblFull
                        // Add monogr after idno
                        eleBiblStruct.addContent(eleMonogr);
                        String sourceTitle = getFirstValue(teiDoc,
                                "tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@level='m' and @xml:lang='" + docLanguage + "']", null);
                        if (sourceTitle != null) {
                            Element eleSourceTitle = new Element("title", COMPONENTS);
                            eleSourceTitle.setAttribute("lang", docLanguage);
                            eleSourceTitle.setText(sourceTitle);
                            eleMonogr.addContent(eleSourceTitle);
                        }
                    }
                }
                // msDesc
                if (eleSourceDesc.getChild("msDesc", TEI) != null) {
                    Element eleMsDesc = eleSourceDesc.getChild("msDesc", TEI).clone();
                    eleMsDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712054", CMDI);
                    // msIdentifier
                    Element eleMsIdentifier = eleMsDesc.getChild("msIdentifier", TEI);
                    if (eleMsIdentifier != null) {
                        eleMsIdentifier.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712039", CMDI);
                        // idno (shelfmark)
                        Element eleIdno = eleMsIdentifier.getChild("idno", TEI);
                        if (eleIdno != null && eleIdno.getChild("idno", TEI) != null) {
                            Element eleIdnoShelfmark = eleIdno.getChild("idno", TEI).clone();
                            eleMsIdentifier.removeChild("idno", TEI);
                            eleMsIdentifier.addContent(eleIdnoShelfmark);
                        }
                    }
                    // msContents
                    Element eleMsContents = eleMsDesc.getChild("msContents", TEI);
                    if (eleMsContents != null) {
                        eleMsContents.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712038", CMDI);
                        //  msItem
                        Element eleTextLang = eleMsContents.getChild("textLang", TEI);
                        if (eleTextLang != null && eleTextLang.getAttributeValue("mainLang") != null) {
                            Element eleMsItem = new Element("msItem", COMPONENTS);
                            eleMsItem.setAttribute("ComponentId", "clarin.eu:cr1:c_1407745712037", CMDI);
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
                eleEncodingDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1379939315562", CMDI);
                // samplingDecl
                Element eleSamplingDecl = eleEncodingDesc.getChild("samplingDecl", TEI);
                if (eleSamplingDecl != null) {
                    eleSamplingDecl.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372982", CMDI);
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
                    Element eleProjectDesc = eleEncodingDesc.getChild("projectDesc", TEI).clone();
                    eleProjectDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880372987", CMDI);
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
                eleProfileDesc.setAttribute("ComponentId", "clarin.eu:cr1:c_1379925925653", CMDI);
                // langUsage
                Element eleLangUsage = eleProfileDesc.getChild("langUsage", TEI);
                if (eleLangUsage != null) {
                    eleLangUsage.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880373021", CMDI);
                }
                // textClass
                Element eleTextClass = eleProfileDesc.getChild("textClass", TEI);
                if (eleTextClass != null) {
                    eleTextClass.setAttribute("ComponentId", "clarin.eu:cr1:c_1375880373027", CMDI);
                    // classCode
                    List<Element> eleListClassCode = eleTextClass.getChildren("classCode", TEI);
                    eleTextClass.removeChildren("classCode", TEI);
                    // Make sure eleListClassCode comes from the English language file
                    try {
                        eleListClassCode = englishTeiDoc.getRootElement()
                                .getChild("teiHeader", TEI)
                                .getChild("profileDesc", TEI)
                                .getChild("textClass", TEI)
                                .getChildren("classCode", TEI);
                    } catch (NullPointerException e) {
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
                    List<Element> eleListKeywords =
                            evaluateToElements(englishTeiDoc.getRootElement(), "tei:teiHeader/tei:profileDesc/tei:textClass/tei:keywords");
                    if (eleListKeywords != null && !eleListKeywords.isEmpty()) {
                        Element eleKeywords = eleListKeywords.get(0).clone();
                        eleKeywords.setAttribute("ComponentId", "clarin.eu:cr1:c_1380613302381", CMDI);
                        eleKeywords.setAttribute("scheme", "");
                        eleKeywords.removeAttribute("lang", XML);
                        Element eleList = new Element("list", COMPONENTS);
                        eleList.setAttribute("ComponentId", "clarin.eu:cr1:c_1380613302392", CMDI);
                        eleList.setAttribute("type", "");
                        eleKeywords.addContent(eleList);

                        Set<String> usedKeywords = new HashSet<>();
                        List<Element> eleListRs = evaluateToElements(eleKeywords, "tei:term/tei:rs[@type='keyword']");
                        if (eleListRs != null && !eleListRs.isEmpty()) {
                            for (Element eleRs : eleListRs) {
                                String key = eleRs.getAttributeValue("key");
                                if (!usedKeywords.contains(key)) {
                                    Element eleItem = new Element("item", COMPONENTS);
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

            changeNamespaceTo(eleTeiHeader, COMPONENTS);
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

        XPathExpression<? extends Object> expr = XPathFactory.instance().compile(xpath, Filters.element(), null, CMDI, COMPONENTS, TEI, XML);
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
        XPathExpression<? extends Object> expr =
                XPathFactory.instance().compile(xpath, Filters.fpassthrough(), null, CMDI, COMPONENTS, TEI, XML, XSI);
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
