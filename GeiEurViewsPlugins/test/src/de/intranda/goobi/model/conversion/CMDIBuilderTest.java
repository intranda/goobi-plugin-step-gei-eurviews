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
        String pi = "K_2017_Herfordt_FriedenskonferenzMuenster";
        String lang = "ger";
        Document teiDoc = getDocumentFromFile(new File("test/xml/" + pi + "_tei_" + lang + ".xml"));
        Assert.assertNotNull(teiDoc);
        Document englishTeiDoc = getDocumentFromFile(new File("test/xml/" + pi + "_tei_eng.xml"));
        Assert.assertNotNull(englishTeiDoc);

        Document cmdiDoc = CMDIBuilder.convertToCMDI(pi, teiDoc, englishTeiDoc);
        Assert.assertNotNull(cmdiDoc);
        Assert.assertNotNull(cmdiDoc.getRootElement());
        Assert.assertEquals("1.2", CMDIBuilder.getFirstValue(cmdiDoc.getRootElement(), "@CMDVersion", null));
        Assert.assertEquals(
                "http://www.clarin.eu/cmd/1 https://infra.clarin.eu/CMDI/1.x/xsd/cmd-envelop.xsd http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1380106710826 https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1380106710826/xsd",
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
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_tei_eng.xml"));
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
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_tei_eng.xml"));
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

            Assert.assertEquals(CMDIBuilder.VIEWER_URL + "/open/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/",
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
            // Assert.assertNotNull(eleResources.getChild("IsPartOfList", CMDIBuilder.CMDI));
        }
    }

    /**
     * @see CMDIBuilder#generateComponents(Document)
     * @verifies create components correctly
     */
    @Test
    public void generateComponents_shouldCreateComponentsCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_tei_spa.xml"));
        Assert.assertNotNull(teiDoc);
        Document englishTeiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_tei_eng.xml"));
        Assert.assertNotNull(englishTeiDoc);

        Element eleComponents = CMDIBuilder.generateComponents(teiDoc, englishTeiDoc);
        Assert.assertNotNull(eleComponents);
        Element eleTeiHeader = eleComponents.getChild("teiHeader", CMDIBuilder.COMPONENTS);
        Assert.assertNotNull(eleTeiHeader);
        Assert.assertEquals("textbook source", CMDIBuilder.getFirstValue(eleTeiHeader, "type", null));
        // fileDesc/titleStmt
        Assert.assertEquals("Fin de la Colonia Sancti-Spíritus", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc[@ComponentId='clarin.eu:cr1:c_1379939315561']/cmd:titleStmt[@ComponentId='clarin.eu:cr1:c_1375880372983']/cmd:title[@level='a' and @lang='spa' and not(@type)]",
                null));
        Assert.assertEquals("End of the colony of Sancti Spiritus", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:titleStmt/cmd:title[@level='a' and @lang='eng' and @type='translated']", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:titleStmt/cmd:title[@level='a' and @lang='ger']", null));
        Assert.assertNull(CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:titleStmt/cmd:author/cmd:persName/@source", null));
        Assert.assertEquals("Cambón, Ramón", CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:titleStmt/cmd:author", null));
        Assert.assertNull(
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:titleStmt/cmd:editor[@role='translator']/cmd:persName", null));
        //        Assert.assertEquals("Friedl, Sophie",
        //                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:titleStmt/cmd:editor[@role='translator']", null));
        // fileDesc/editionStmt
        Assert.assertEquals("Version 1", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:editionStmt[@ComponentId='clarin.eu:cr1:c_1381926654590']/cmd:edition/cmd:note", null));
        // fileDesc/extent
        Assert.assertEquals("pages", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/cmd:num/@type", null));
        Assert.assertEquals("2", CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:extent/cmd:num/@n", null));
        Assert.assertEquals("7 - 8", CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:extent/cmd:num", null));
        // fileDesc/publicationStmt
        Assert.assertEquals("Georg-Eckert-Institut", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:publicationStmt[@ComponentId='clarin.eu:cr1:c_1375880372991']/cmd:publisher", null));
        Assert.assertEquals("2017", CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:publicationStmt/cmd:date", null));
        Assert.assertEquals("2017", CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:publicationStmt/cmd:date/@when", null));
        Assert.assertEquals("CC BY-NC-SA 3.0 DE", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:publicationStmt/cmd:availability[@ComponentId='clarin.eu:cr1:c_1375880372986']/cmd:licence", null));
        Assert.assertEquals("https://creativecommons.org/licenses/by-nc-sa/3.0/de/", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:publicationStmt/cmd:availability/cmd:ab[@ComponentId='clarin.eu:cr1:c_1375880372985']/@type", null));
        // fileDesc/noteStmt
        //        Assert.assertEquals("translated from spa", CMDIBuilder.getFirstValue(eleTeiHeader,
        //                "cmd:fileDesc/cmd:notesStmt[@ComponentId='clarin.eu:cr1:c_1375880372992']/cmd:note[not(@type)]", null));
        // fileDesc/sourceDesc/idno
        Assert.assertEquals("123-4-5678-9101-1", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc[@ComponentId='clarin.eu:cr1:c_1375880373029']/cmd:biblStruct[@ComponentId='clarin.eu:cr1:c_1379939315559']/cmd:idno[@type='ISBN']",
                null));
        Assert.assertEquals("000000000",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:idno[@type='PPNa']", null));
        Assert.assertEquals("111111111",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:idno[@type='PPNc']", null));
        Assert.assertEquals("222222222",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:idno[@type='PPNf']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/title
        Assert.assertEquals(
                "Breves lecciones de historia argentina : Para servir exclusivamente a niños de escuela elemental. Arregladas al programa oficial",
                CMDIBuilder.getFirstValue(eleTeiHeader,
                        "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct[@ComponentId='clarin.eu:cr1:c_1379939315559']/cmd:monogr[@ComponentId='clarin.eu:cr1:c_1379939315552']/cmd:title[@lang='spa']",
                        null));
        Assert.assertEquals(
                "Short lessons in Argentinian history. Exclusively for elementary school children. Corresponding to the official curriculum.",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:title[@lang='eng']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/edition
        Assert.assertEquals("test edition",
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:edition", null));
        // fileDesc/sourceDesc/biblStruct/monogr/author
        Assert.assertEquals("Cambón, Ramón", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:author[@ComponentId='clarin.eu:cr1:c_1379939315551']/cmd:name",
                null));
        // fileDesc/sourceDesc/biblStruct/monogr/editor
        Assert.assertEquals("Editor, Mr.", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:editor[@ComponentId='clarin.eu:cr1:c_1379939315553']/cmd:name",
                null));
        // fileDesc/sourceDesc/biblStruct/monogr/imprint
        Assert.assertEquals("Buenos Aires", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:imprint[@ComponentId='clarin.eu:cr1:c_1379939315555']/cmd:pubPlace",
                null));
        Assert.assertEquals("Pablo E. Coni", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:imprint/cmd:publisher", null));
        Assert.assertEquals("1884", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:imprint/cmd:date[@cert='high' and @when='1884']", null));
        // fileDesc/sourceDesc/biblStruct/monogr/extent
        Assert.assertEquals("35", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:monogr/cmd:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/cmd:num[@type='pages']",
                null));
        // fileDesc/sourceDesc/biblStruct/relatedItem
        Assert.assertEquals("series title spanish (scope)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:relatedItem[@ComponentId='clarin.eu:cr1:c_1379939315557']/cmd:bibl[@ComponentId='clarin.eu:cr1:c_1379939315556']/cmd:title[@lang='spa']",
                null));
        Assert.assertEquals("series title (scope)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:biblStruct/cmd:relatedItem/cmd:bibl/cmd:title[@lang='eng']", null));
        // fileDesc/sourceDesc/msDesc
        Assert.assertEquals("RA H-13 (1,1884)", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:msDesc[@ComponentId='clarin.eu:cr1:c_1407745712054']/cmd:msIdentifier[@ComponentId='clarin.eu:cr1:c_1407745712039']/cmd:idno[@type='shelfmark']",
                null));
        Assert.assertEquals("spa", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:fileDesc/cmd:sourceDesc/cmd:msDesc/cmd:msContents[@ComponentId='clarin.eu:cr1:c_1407745712038']/cmd:msItem[@ComponentId='clarin.eu:cr1:c_1407745712037']/@n",
                null));
        Assert.assertNull(
                CMDIBuilder.getFirstValue(eleTeiHeader, "cmd:fileDesc/cmd:sourceDesc/cmd:msDesc/cmd:msContents/cmd:textLang/@n", null));
        // encodingDesc
        Assert.assertNotNull(CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:encodingDesc[@ComponentId='clarin.eu:cr1:c_1379939315562']/cmd:samplingDecl[@ComponentId='clarin.eu:cr1:c_1375880372982']/cmd:ab",
                null));
        Assert.assertNotNull(CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:encodingDesc/cmd:projectDesc[@ComponentId='clarin.eu:cr1:c_1375880372987']/cmd:ab", null));
        // profileDesc/language
        Assert.assertEquals("spa", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:profileDesc/cmd:langUsage[@ComponentId='clarin.eu:cr1:c_1375880373021']/cmd:language[@ident='spa']", null));
        // profileDesc/textClass/classCode
        Assert.assertEquals("textbook source", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:profileDesc/cmd:textClass[@ComponentId='clarin.eu:cr1:c_1375880373027']/cmd:classCode[@scheme='WV.textType' and not(@lang)]",
                null));
        Assert.assertEquals("Argentine Republic", CMDIBuilder.getFirstValue(eleTeiHeader,
                "cmd:profileDesc/cmd:textClass[@ComponentId='clarin.eu:cr1:c_1375880373027']/cmd:classCode[@scheme='WV.placeOfUse' and not(@lang)]",
                null));
        //  profileDesc/textClass/keywords
        List<Element> eleListKeyword = CMDIBuilder.evaluateToElements(eleTeiHeader,
                "cmd:profileDesc/cmd:textClass/cmd:keywords[@ComponentId='clarin.eu:cr1:c_1380613302381' and @scheme='']/cmd:list[@ComponentId='clarin.eu:cr1:c_1380613302392' and @type='']/cmd:item");
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
