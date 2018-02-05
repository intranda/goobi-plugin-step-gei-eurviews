package de.intranda.goobi.model.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Test;

public class CMDIBuilderTest {

    /**
     * @see CMDIBuilder#convertToCMDI(String,Document)
     * @verifies generate root element correctly
     */
    @Test
    public void convertToCMDI_shouldGenerateRootElementCorrectly() throws Exception {
        String pi = "DE_1953_Bendfeld_AbendlandStaatensystem_186";
        String lang = "eng";
        Document teiDoc = getDocumentFromFile(new File("test/xml/" + pi + "_tei_" + lang + ".xml"));
        Assert.assertNotNull(teiDoc);
        Document englishTeiDoc = getDocumentFromFile(new File("test/xml/" + pi + "_tei_eng.xml"));
        Assert.assertNotNull(englishTeiDoc);

        Document cmdiDoc = CMDIBuilder.convertToCMDI(pi, teiDoc, englishTeiDoc);
        Assert.assertNotNull(cmdiDoc);
        Assert.assertNotNull(cmdiDoc.getRootElement());
        Assert.assertEquals("1.1", CMDIBuilder.getFirstValue(cmdiDoc.getRootElement(), "@CMDVersion", null));
        Assert.assertEquals(
                "http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1380106710826/xsd",
                CMDIBuilder.getFirstValue(cmdiDoc.getRootElement(), "@xsi:schemaLocation", null));

        Path cmdiFilePath = Paths.get("test/xml/" + pi + "_cmdi_" + lang + ".xml");
        try (FileWriter fileWriter = new FileWriter(cmdiFilePath.toFile())) {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(cmdiDoc, fileWriter);
        }
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
        Assert.assertEquals("GEI - WorldViews", eleHeader.getChildText("MdCreator", CMDIBuilder.CMDI_NOPREFIX));
        Assert.assertNotNull(eleHeader.getChildText("MdCreationDate", CMDIBuilder.CMDI_NOPREFIX));
        Assert.assertEquals(10, eleHeader.getChildText("MdCreationDate", CMDIBuilder.CMDI_NOPREFIX)
                .length());
        Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/rest/content/cmdi/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/",
                eleHeader.getChildText("MdSelfLink", CMDIBuilder.CMDI_NOPREFIX));
        Assert.assertEquals("clarin.eu:cr1:p_1380106710826", eleHeader.getChildText("MdProfile", CMDIBuilder.CMDI_NOPREFIX));
        Assert.assertEquals("WorldViews", eleHeader.getChildText("MdCollectionDisplayName", CMDIBuilder.CMDI_NOPREFIX));
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
        Assert.assertNotNull(eleResources.getChild("ResourceProxyList", CMDIBuilder.CMDI_NOPREFIX));
        List<Element> eleListResourceProxy = eleResources.getChild("ResourceProxyList", CMDIBuilder.CMDI_NOPREFIX)
                .getChildren("ResourceProxy", CMDIBuilder.CMDI_NOPREFIX);
        Assert.assertEquals(2, eleListResourceProxy.size());
        {
            Element eleResourceProxy = eleListResourceProxy.get(0);
            Assert.assertEquals("rp_lp", eleResourceProxy.getAttributeValue("id"));

            Element eleResourceType = eleResourceProxy.getChild("ResourceType", CMDIBuilder.CMDI_NOPREFIX);
            Assert.assertNotNull(eleResourceType);
            Assert.assertEquals("application/xhtml+xml", eleResourceType.getAttributeValue("mimetype"));
            Assert.assertEquals("LandingPage", eleResourceType.getText());

            Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/image/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/1/",
                    eleResourceProxy.getChildText("ResourceRef", CMDIBuilder.CMDI_NOPREFIX));
        }
        {
            Element eleResourceProxy = eleListResourceProxy.get(1);
            Assert.assertEquals("rp_tei", eleResourceProxy.getAttributeValue("id"));

            Element eleResourceType = eleResourceProxy.getChild("ResourceType", CMDIBuilder.CMDI_NOPREFIX);
            Assert.assertNotNull(eleResourceType);
            Assert.assertEquals("application/tei+xml", eleResourceType.getAttributeValue("mimetype"));
            Assert.assertEquals("Resource", eleResourceType.getText());

            Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/rest/content/tei/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/",
                    eleResourceProxy.getChildText("ResourceRef", CMDIBuilder.CMDI_NOPREFIX));

            Assert.assertNotNull(eleResources.getChild("JournalFileProxyList", CMDIBuilder.CMDI_NOPREFIX));
            Assert.assertNotNull(eleResources.getChild("ResourceRelationList", CMDIBuilder.CMDI_NOPREFIX));
            Assert.assertNotNull(eleResources.getChild("IsPartOfList", CMDIBuilder.CMDI_NOPREFIX));
        }
    }

    /**
     * @see CMDIBuilder#generateComponents(Document)
     * @verifies create components correctly
     */
    @Test
    public void generateComponents_shouldCreateComponentsCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_spa.xml"));
        Assert.assertNotNull(teiDoc);
        Document englishTeiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(englishTeiDoc);

        Element eleComponents = CMDIBuilder.generateComponents(teiDoc, englishTeiDoc);
        Assert.assertNotNull(eleComponents);
        Element eleTeiHeader = eleComponents.getChild("teiHeader", CMDIBuilder.CMDI_NOPREFIX);
        Assert.assertNotNull(eleTeiHeader);
        Assert.assertEquals("textbook source", CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:type", null));
        // fileDesc/titleStmt
        Assert.assertEquals("Fin de la Colonia Sancti-Spíritus", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc[@ComponentId='clarin.eu:cr1:c_1379939315561']/cmdi:titleStmt[@ComponentId='clarin.eu:cr1:c_1375880372983']/cmdi:title[@level='a' and @lang='spa' and not(@type)]",
                null));
        Assert.assertEquals("End of the colony of Sancti Spiritus", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:titleStmt/cmdi:title[@level='a' and @lang='eng' and @type='translated']", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:titleStmt/cmdi:title[@level='a' and @lang='ger']", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:titleStmt/cmdi:author/cmdi:persName/@source", null));
        Assert.assertEquals("Cambón, Ramón", CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:titleStmt/cmdi:author", null));
        Assert.assertNull(
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:titleStmt/cmdi:editor[@role='translator']/cmdi:persName", null));
        //        Assert.assertEquals("Friedl, Sophie",
        //                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:titleStmt/cmdi:editor[@role='translator']", null));
        // fileDesc/editionStmt
        Assert.assertEquals("Version 1", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:editionStmt[@ComponentId='clarin.eu:cr1:c_1381926654590']/cmdi:edition/cmdi:note", null));
        // fileDesc/extent
        Assert.assertEquals("pages", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/cmdi:num/@type", null));
        Assert.assertEquals("2", CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:extent/cmdi:num/@n", null));
        Assert.assertEquals("7 - 8", CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:extent/cmdi:num", null));
        // fileDesc/publicationStmt
        Assert.assertEquals("Georg-Eckert-Institut", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:publicationStmt[@ComponentId='clarin.eu:cr1:c_1375880372991']/cmdi:publisher", null));
        Assert.assertEquals("2017", CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:publicationStmt/cmdi:date", null));
        Assert.assertEquals("2017", CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:publicationStmt/cmdi:date/@when", null));
        Assert.assertEquals("CC BY-NC-SA 3.0 DE", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:publicationStmt/cmdi:availability[@ComponentId='clarin.eu:cr1:c_1375880372986']/cmdi:licence", null));
        Assert.assertEquals("https://creativecommons.org/licenses/by-nc-sa/3.0/de/", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:publicationStmt/cmdi:availability/cmdi:ab[@ComponentId='clarin.eu:cr1:c_1375880372985']/@type", null));
        // fileDesc/noteStmt
        //        Assert.assertEquals("translated from spa", CMDIBuilder.getFirstValue(eleTeiHeader,
        //                "cmdi:fileDesc/cmdi:notesStmt[@ComponentId='clarin.eu:cr1:c_1375880372992']/cmdi:note[not(@type)]", null));
        // fileDesc/sourceDesc/idno
        Assert.assertEquals("123-4-5678-9101-1", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc[@ComponentId='clarin.eu:cr1:c_1375880373029']/cmdi:biblStruct[@ComponentId='clarin.eu:cr1:c_1379939315559']/cmdi:idno[@type='ISBN']",
                null));
        Assert.assertEquals("000000000",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:idno[@type='PPNa']", null));
        Assert.assertEquals("111111111",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:idno[@type='PPNc']", null));
        Assert.assertEquals("222222222",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:idno[@type='PPNf']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/title
        Assert.assertEquals(
                "Breves lecciones de historia argentina : Para servir exclusivamente a niños de escuela elemental. Arregladas al programa oficial",
                CMDIBuilder.getFirstValue(eleTeiHeader,
                        "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct[@ComponentId='clarin.eu:cr1:c_1379939315559']/cmdi:monogr[@ComponentId='clarin.eu:cr1:c_1379939315552']/cmdi:title[@lang='spa']",
                        null));
        Assert.assertEquals(
                "Short lessons in Argentinian history. Exclusively for elementary school children. Corresponding to the official curriculum.",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:title[@lang='eng']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/edition
        Assert.assertEquals("test edition",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:edition", null));
        // fileDesc/sourceDesc/biblStruct/monogr/author
        Assert.assertEquals("Cambón, Ramón", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:author[@ComponentId='clarin.eu:cr1:c_1379939315551']/cmdi:name",
                null));
        // fileDesc/sourceDesc/biblStruct/monogr/editor
        Assert.assertEquals("Editor, Mr.", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:editor[@ComponentId='clarin.eu:cr1:c_1379939315553']/cmdi:name",
                null));
        // fileDesc/sourceDesc/biblStruct/monogr/imprint
        Assert.assertEquals("Buenos Aires", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:imprint[@ComponentId='clarin.eu:cr1:c_1379939315555']/cmdi:pubPlace",
                null));
        Assert.assertEquals("Pablo E. Coni", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:imprint/cmdi:publisher", null));
        Assert.assertEquals("1884", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:imprint/cmdi:date[@cert='high' and @when='1884']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/extent
        Assert.assertEquals("35", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:monogr/cmdi:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/cmdi:num[@type='pages']",
                null));
        // fileDesc/sourceDesc/biblStruct/relatedItem
        Assert.assertEquals("series title spanish (scope)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:relatedItem[@ComponentId='clarin.eu:cr1:c_1379939315557']/cmdi:bibl[@ComponentId='clarin.eu:cr1:c_1379939315556']/cmdi:title[@lang='spa']",
                null));
        Assert.assertEquals("series title (scope)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:biblStruct/cmdi:relatedItem/cmdi:bibl/cmdi:title[@lang='eng']", null));
        // fileDesc/sourceDesc/msDesc
        Assert.assertEquals("RA H-13 (1,1884)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:msDesc[@ComponentId='clarin.eu:cr1:c_1407745712054']/cmdi:msIdentifier[@ComponentId='clarin.eu:cr1:c_1407745712039']/cmdi:idno[@type='shelfmark']",
                null));
        Assert.assertEquals("spa", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:fileDesc/cmdi:sourceDesc/cmdi:msDesc/cmdi:msContents[@ComponentId='clarin.eu:cr1:c_1407745712038']/cmdi:msItem[@ComponentId='clarin.eu:cr1:c_1407745712037']/@n",
                null));
        Assert.assertNull(
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmdi:fileDesc/cmdi:sourceDesc/cmdi:msDesc/cmdi:msContents/cmdi:textLang/@n", null));
        // encodingDesc
        Assert.assertNotNull(CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:encodingDesc[@ComponentId='clarin.eu:cr1:c_1379939315562']/cmdi:samplingDecl[@ComponentId='clarin.eu:cr1:c_1375880372982']/cmdi:ab",
                null));
        Assert.assertNotNull(CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:encodingDesc/cmdi:projectDesc[@ComponentId='clarin.eu:cr1:c_1375880372987']/cmdi:ab", null));
        // profileDesc/language
        Assert.assertEquals("spa", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:profileDesc/cmdi:langUsage[@ComponentId='clarin.eu:cr1:c_1375880373021']/cmdi:language[@ident='spa']", null));
        // profileDesc/textClass/classCode
        Assert.assertEquals("textbook source", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:profileDesc/cmdi:textClass[@ComponentId='clarin.eu:cr1:c_1375880373027']/cmdi:classCode[@scheme='WV.textType' and not(@lang)]",
                null));
        Assert.assertEquals("Argentine Republic", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmdi:profileDesc/cmdi:textClass[@ComponentId='clarin.eu:cr1:c_1375880373027']/cmdi:classCode[@scheme='WV.placeOfUse' and not(@lang)]",
                null));
        //  profileDesc/textClass/keywords
        List<Element> eleListKeyword = CMDIBuilder.evaluateToElements(eleTeiHeader,
                "cmdi:profileDesc/cmdi:textClass/cmdi:keywords[@ComponentId='clarin.eu:cr1:c_1380613302381' and @scheme='']/cmdi:list[@ComponentId='clarin.eu:cr1:c_1380613302392' and @type='']/cmdi:item");
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
