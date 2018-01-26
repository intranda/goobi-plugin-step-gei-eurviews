package de.intranda.goobi.model.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;

public class CMDIBuilderTest {

    /**
     * @see CMDIBuilder#convertToCMDI(String,Document)
     * @verifies generate root element correctly
     */
    @Test
    public void convertToCMDI_shouldGenerateRootElementCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Document cmdiDoc = CMDIBuilder.convertToCMDI("AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8", teiDoc);
        Assert.assertNotNull(cmdiDoc);
        Assert.assertNotNull(cmdiDoc.getRootElement());
        Assert.assertEquals("1.1", CMDIBuilder.getFirstValue(cmdiDoc.getRootElement(), "@CMDVersion", null));
        Assert.assertEquals(
                "http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1380106710826/xsd",
                CMDIBuilder.getFirstValue(cmdiDoc.getRootElement(), "@xsi:schemaLocation", null));
    }

    /**
     * @see CMDIBuilder#generateHeader(Document)
     * @verifies create header correctly
     */
    @Test
    public void generateHeader_shouldCreateHeaderCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Element eleHeader = CMDIBuilder.generateHeader("AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8", teiDoc);
        Assert.assertNotNull(eleHeader);
        Assert.assertEquals("GEI - WorldViews", eleHeader.getChildText("MdCreator", CMDIBuilder.CMDI));
        Assert.assertNotNull(eleHeader.getChildText("MdCreationDate", CMDIBuilder.CMDI));
        Assert.assertEquals(10, eleHeader.getChildText("MdCreationDate", CMDIBuilder.CMDI)
                .length());
        Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/rest/content/cmdi/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/",
                eleHeader.getChildText("MdSelfLink", CMDIBuilder.CMDI));
        Assert.assertEquals("clarin.eu:cr1:p_1380106710826", eleHeader.getChildText("MdProfile", CMDIBuilder.CMDI));
        Assert.assertEquals("WorldViews", eleHeader.getChildText("MdCollectionDisplayName", CMDIBuilder.CMDI));
    }

    /**
     * @see CMDIBuilder#generateResources(String,Document)
     * @verifies create resources correctly
     */
    @Test
    public void generateResources_shouldCreateResourcesCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Element eleResources = CMDIBuilder.generateResources("AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8", teiDoc);
        Assert.assertNotNull(eleResources);
        Assert.assertNotNull(eleResources.getChild("ResourceProxyList", CMDIBuilder.CMDI));
        List<Element> eleListResourceProxy = eleResources.getChild("ResourceProxyList", CMDIBuilder.CMDI)
                .getChildren("ResourceProxy", CMDIBuilder.CMDI);
        Assert.assertEquals(2, eleListResourceProxy.size());
        {
            Element eleResourceProxy = eleListResourceProxy.get(0);
            Assert.assertEquals("rp_lp", eleResourceProxy.getAttributeValue("id"));

            Element eleResourceType = eleResourceProxy.getChild("ResourceType", CMDIBuilder.CMDI);
            Assert.assertNotNull(eleResourceType);
            Assert.assertEquals("application/xhtml+xml", eleResourceType.getAttributeValue("mimetype"));
            Assert.assertEquals("LandingPage", eleResourceType.getText());

            Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/image/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/1/",
                    eleResourceProxy.getChildText("ResourceRef", CMDIBuilder.CMDI));
        }
        {
            Element eleResourceProxy = eleListResourceProxy.get(1);
            Assert.assertEquals("rp_tei", eleResourceProxy.getAttributeValue("id"));

            Element eleResourceType = eleResourceProxy.getChild("ResourceType", CMDIBuilder.CMDI);
            Assert.assertNotNull(eleResourceType);
            Assert.assertEquals("application/tei+xml", eleResourceType.getAttributeValue("mimetype"));
            Assert.assertEquals("Resource", eleResourceType.getText());

            Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/rest/content/tei/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/",
                    eleResourceProxy.getChildText("ResourceRef", CMDIBuilder.CMDI));

            Assert.assertNotNull(eleResources.getChild("JournalFileProxyList", CMDIBuilder.CMDI));
            Assert.assertNotNull(eleResources.getChild("ResourceRelationList", CMDIBuilder.CMDI));
            Assert.assertNotNull(eleResources.getChild("IsPartOfList", CMDIBuilder.CMDI));
        }
    }

    /**
     * @see CMDIBuilder#generateComponents(Document)
     * @verifies create components correctly
     */
    @Test
    public void generateComponents_shouldCreateComponentsCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Element eleComponents = CMDIBuilder.generateComponents(teiDoc);
        Assert.assertNotNull(eleComponents);
        Element eleTeiHeader = eleComponents.getChild("teiHeader", CMDIBuilder.TEI);
        Assert.assertNotNull(eleTeiHeader);
        Assert.assertEquals("textbook source", CMDIBuilder.getFirstValue(eleTeiHeader, "tei:type", null));
        // fileDesc/titleStmt
        Assert.assertEquals("Fin de la Colonia Sancti-Spíritus", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc[@ComponentId='clarin.eu:cr1:c_1379939315561']/tei:titleStmt[@ComponentId='clarin.eu:cr1:c_1375880372983']/tei:title[@level='a' and @lang='spa' and not(@type)]",
                null));
        Assert.assertEquals("End of the colony of Sancti Spiritus", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:titleStmt/tei:title[@level='a' and @lang='eng' and @type='translated']", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:title[@level='a' and @lang='ger']", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:author/tei:persName/@source", null));
        Assert.assertEquals("Cambón, Ramón", CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:author", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:editor[@role='translator']/tei:persName", null));
        Assert.assertEquals("Friedl, Sophie",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:editor[@role='translator']", null));
        // fileDesc/editionStmt
        Assert.assertEquals("Version 1", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:editionStmt[@ComponentId='clarin.eu:cr1:c_1381926654590']/tei:edition/tei:note", null));
        // fileDesc/extent
        Assert.assertEquals("pages",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/tei:num/@type", null));
        Assert.assertEquals("2", CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:extent/tei:num/@n", null));
        Assert.assertEquals("7 - 8", CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:extent/tei:num", null));
        // fileDesc/publicationStmt
        Assert.assertEquals("Georg-Eckert-Institut", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:publicationStmt[@ComponentId='clarin.eu:cr1:c_1375880372991']/tei:publisher", null));
        Assert.assertEquals("2017", CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:publicationStmt/tei:date", null));
        Assert.assertEquals("2017", CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:publicationStmt/tei:date/@when", null));
        Assert.assertEquals("CC BY-NC-SA 3.0 DE", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:publicationStmt/tei:availability[@ComponentId='clarin.eu:cr1:c_1375880372986']/tei:licence", null));
        Assert.assertEquals("https://creativecommons.org/licenses/by-nc-sa/3.0/de/", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:publicationStmt/tei:availability/tei:ab[@ComponentId='clarin.eu:cr1:c_1375880372985']/@type", null));
        // fileDesc/noteStmt
        Assert.assertEquals("translated from spa", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:notesStmt[@ComponentId='clarin.eu:cr1:c_1375880372992']/tei:note[not(@type)]", null));
        // fileDesc/sourceDesc/idno
        Assert.assertEquals("123-4-5678-9101-1", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc[@ComponentId='clarin.eu:cr1:c_1375880373029']/tei:biblStruct/tei:idno[@type='ISBN']", null));
        Assert.assertEquals("000000000",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:idno[@type='PPNa']", null));
        Assert.assertEquals("111111111",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:idno[@type='PPNc']", null));
        Assert.assertEquals("222222222",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:idno[@type='PPNf']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/title
        Assert.assertEquals(
                "Breves lecciones de historia argentina : Para servir exclusivamente a niños de escuela elemental. Arregladas al programa oficial",
                CMDIBuilder.getFirstValue(eleTeiHeader,
                        "tei:fileDesc/tei:sourceDesc/tei:biblStruct[@ComponentId='clarin.eu:cr1:c_1379939315559']/tei:monogr[@ComponentId='clarin.eu:cr1:c_1379939315552']/tei:title[@lang='spa']",
                        null));
        Assert.assertEquals(
                "Short lessons in Argentinian history. Exclusively for elementary school children. Corresponding to the official curriculum.",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:title[@lang='eng']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/edition
        Assert.assertEquals("test edition",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:edition", null));
        // fileDesc/sourceDesc/biblStruct/monogr/author
        Assert.assertEquals("Cambón, Ramón", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:author[@ComponentId='clarin.eu:cr1:c_1379939315551']/tei:name", null));
        // fileDesc/sourceDesc/biblStruct/monogr/editor
        Assert.assertEquals("Editor, Mr.", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:editor[@ComponentId='clarin.eu:cr1:c_1379939315553']/tei:name", null));
        // fileDesc/sourceDesc/biblStruct/monogr/imprint
        Assert.assertEquals("Buenos Aires", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint[@ComponentId='clarin.eu:cr1:c_1379939315555']/tei:pubPlace",
                null));
        Assert.assertEquals("Pablo E. Coni",
                CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:publisher", null));
        Assert.assertEquals("1884", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:date[@cert='high' and @when='1884']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/extent
        Assert.assertEquals("35", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/tei:num[@type='pages']",
                null));
        // fileDesc/sourceDesc/biblStruct/relatedItem
        Assert.assertEquals("series title spanish (scope)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:relatedItem[@ComponentId='clarin.eu:cr1:c_1379939315557']/tei:bibl[@ComponentId='clarin.eu:cr1:c_1379939315556']/tei:title[@lang='spa']",
                null));
        Assert.assertEquals("series title (scope)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:relatedItem/tei:bibl/tei:title[@lang='eng']", null));
        // fileDesc/sourceDesc/msDesc
        Assert.assertEquals("RA H-13 (1,1884)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:msDesc[@ComponentId='clarin.eu:cr1:c_1407745712054']/tei:msIdentifier[@ComponentId='clarin.eu:cr1:c_1407745712039']/tei:idno[@type='shelfmark']",
                null));
        Assert.assertEquals("spa", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msContents[@ComponentId='clarin.eu:cr1:c_1407745712038']/tei:msItem[@ComponentId='clarin.eu:cr1:c_1407745712037']/@n",
                null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msContents/tei:textLang/@n", null));
        // encodingDesc
        Assert.assertNotNull(CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:encodingDesc[@ComponentId='clarin.eu:cr1:c_1379939315562']/tei:samplingDecl[@ComponentId='clarin.eu:cr1:c_1375880372982']/tei:ab",
                null));
        Assert.assertNotNull(CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:encodingDesc/tei:projectDesc[@ComponentId='clarin.eu:cr1:c_1375880372987']/tei:ab", null));
        // profileDesc/langusage
        Assert.assertEquals("eng", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:profileDesc/tei:langUsage[@ComponentId='clarin.eu:cr1:c_1375880373021']/tei:language[@ident='eng']", null));
        // profileDesc/textClass/classCode
        Assert.assertEquals("textbook source", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:profileDesc/tei:textClass[@ComponentId='clarin.eu:cr1:c_1375880373027']/tei:classCode[@scheme='WV.textType' and not(@lang)]",
                null));
        Assert.assertEquals("Argentine Republic", CMDIBuilder.getFirstValue(eleTeiHeader,
                "tei:profileDesc/tei:textClass[@ComponentId='clarin.eu:cr1:c_1375880373027']/tei:classCode[@scheme='WV.placeOfUse' and not(@lang)]",
                null));
        //  profileDesc/textClass/keywords
        List<Element> eleListKeyword = CMDIBuilder.evaluateToElements(eleTeiHeader,
                "tei:profileDesc/tei:textClass/tei:keywords[@ComponentId='clarin.eu:cr1:c_1380613302381' and @scheme='']/tei:list[@ComponentId='clarin.eu:cr1:c_1380613302392' and @type='']/tei:item");
        Assert.assertNotNull(eleListKeyword);
        Assert.assertEquals(6, eleListKeyword.size());
    }

    /**
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    private static Document getDocumentFromFile(File file) throws FileNotFoundException, IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            return builder.build(fis);
        }
    }
}
