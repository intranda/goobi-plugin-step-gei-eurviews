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

public class CMDIConverterTest {

    /**
     * @see CMDIConverter#generateHeader(Document)
     * @verifies create header correctly
     */
    @Test
    public void generateHeader_shouldCreateHeaderCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Element eleHeader = CMDIConverter.generateHeader("AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8", teiDoc);
        Assert.assertNotNull(eleHeader);
        Assert.assertEquals("GEI - WorldViews", eleHeader.getChildText("MdCreator", CMDIConverter.CMDI));
        Assert.assertNotNull(eleHeader.getChildText("MdCreationDate", CMDIConverter.CMDI));
        Assert.assertEquals(10, eleHeader.getChildText("MdCreationDate", CMDIConverter.CMDI).length());
        Assert.assertEquals(CMDIConverter.VIEWER_URL + "/rest/content/cmdi/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/", eleHeader
                .getChildText("MdSelfLink", CMDIConverter.CMDI));
        Assert.assertEquals("clarin.eu:cr1:p_1380106710826", eleHeader.getChildText("MdProfile", CMDIConverter.CMDI));
        Assert.assertEquals("WorldViews", eleHeader.getChildText("MdCollectionDisplayName", CMDIConverter.CMDI));
    }

    /**
     * @see CMDIConverter#generateResources(String,Document)
     * @verifies create resources correctly
     */
    @Test
    public void generateResources_shouldCreateResourcesCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Element eleResources = CMDIConverter.generateResources("AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8", teiDoc);
        Assert.assertNotNull(eleResources);
        Assert.assertNotNull(eleResources.getChild("ResourceProxyList", CMDIConverter.CMDI));
        List<Element> eleListResourceProxy = eleResources.getChild("ResourceProxyList", CMDIConverter.CMDI).getChildren("ResourceProxy",
                CMDIConverter.CMDI);
        Assert.assertEquals(2, eleListResourceProxy.size());
        {
            Element eleResourceProxy = eleListResourceProxy.get(0);
            Assert.assertEquals("rp_lp", eleResourceProxy.getAttributeValue("id"));

            Element eleResourceType = eleResourceProxy.getChild("ResourceType", CMDIConverter.CMDI);
            Assert.assertNotNull(eleResourceType);
            Assert.assertEquals("application/xhtml+xml", eleResourceType.getAttributeValue("mimetype"));
            Assert.assertEquals("LandingPage", eleResourceType.getText());

            Assert.assertEquals(CMDIConverter.VIEWER_URL + "/image/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/1/", eleResourceProxy
                    .getChildText("ResourceRef", CMDIConverter.CMDI));
        }
        {
            Element eleResourceProxy = eleListResourceProxy.get(1);
            Assert.assertEquals("rp_tei", eleResourceProxy.getAttributeValue("id"));

            Element eleResourceType = eleResourceProxy.getChild("ResourceType", CMDIConverter.CMDI);
            Assert.assertNotNull(eleResourceType);
            Assert.assertEquals("application/tei+xml", eleResourceType.getAttributeValue("mimetype"));
            Assert.assertEquals("Resource", eleResourceType.getText());

            Assert.assertEquals(CMDIConverter.VIEWER_URL + "/rest/content/tei/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8/eng/",
                    eleResourceProxy.getChildText("ResourceRef", CMDIConverter.CMDI));

            Assert.assertNotNull(eleResources.getChild("JournalFileProxyList", CMDIConverter.CMDI));
            Assert.assertNotNull(eleResources.getChild("ResourceRelationList", CMDIConverter.CMDI));
            Assert.assertNotNull(eleResources.getChild("IsPartOfList", CMDIConverter.CMDI));
        }
    }

    /**
     * @see CMDIConverter#generateComponents(Document)
     * @verifies create components correctly
     */
    @Test
    public void generateComponents_shouldCreateComponentsCorrectly() throws Exception {
        Document teiDoc = getDocumentFromFile(new File("test/xml/AR_1884_Cambon_BrevesLeccionesDeHistoriaArgentina_7_8_eng.xml"));
        Assert.assertNotNull(teiDoc);

        Element eleComponents = CMDIConverter.generateComponents(teiDoc);
        Assert.assertNotNull(eleComponents);
        Element eleTeiHeader = eleComponents.getChild("teiHeader", CMDIConverter.TEI);
        Assert.assertNotNull(eleTeiHeader);
        Assert.assertEquals("textbook source", CMDIConverter.getFirstValue(eleTeiHeader, "tei:type", null));
        // fileDesc/titleStmt
        Assert.assertEquals("Fin de la Colonia Sancti-Spíritus", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc[@ComponentId='clarin.eu:cr1:c_1379939315561']/tei:titleStmt[@ComponentId='clarin.eu:cr1:c_1375880372983']/tei:title[@level='a' and @xml:lang='spa' and not(@type)]",
                null));
        Assert.assertEquals("End of the colony of Sancti Spiritus", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:titleStmt/tei:title[@level='a' and @xml:lang='eng' and @type='translated']", null));
        Assert.assertNull(CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:title[@level='a' and @xml:lang='ger']", null));
        Assert.assertNull(CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:author/tei:persName/@source", null));
        Assert.assertEquals("Cambón, Ramón", CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:author", null));
        Assert.assertNull(CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:editor[@role='translator']/tei:persName", null));
        Assert.assertEquals("Friedl, Sophie", CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:titleStmt/tei:editor[@role='translator']",
                null));
        // fileDesc/editionStmt
        Assert.assertEquals("Version 1", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:editionStmt[@ComponentId='clarin.eu:cr1:c_1381926654590']/tei:edition/tei:note", null));
        // fileDesc/extent
        Assert.assertEquals("pages", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/tei:num/@type", null));
        Assert.assertEquals("2", CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:extent/tei:num/@n", null));
        Assert.assertEquals("7 - 8", CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:extent/tei:num", null));
        // fileDesc/publicationStmt
        Assert.assertEquals("Georg-Eckert-Institut", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:publicationStmt[@ComponentId='clarin.eu:cr1:c_1375880372991']/tei:publisher", null));
        Assert.assertEquals("2017", CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:publicationStmt/tei:date", null));
        Assert.assertEquals("2017", CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:publicationStmt/tei:date/@when", null));
        Assert.assertEquals("CC BY-NC-SA 3.0 DE", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:publicationStmt/tei:availability[@ComponentId='clarin.eu:cr1:c_1375880372986']/tei:licence", null));
        Assert.assertEquals("https://creativecommons.org/licenses/by-nc-sa/3.0/de/", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:publicationStmt/tei:availability/tei:ab[@ComponentId='clarin.eu:cr1:c_1375880372985']/@type", null));
        // TODO fileDesc/noteStmt
        // fileDesc/sourceDesc/idno
        Assert.assertEquals("123-4-5678-9101-1", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc[@ComponentId='clarin.eu:cr1:c_1375880373029']/tei:biblStruct/tei:idno[@type='ISBN']", null));
        Assert.assertEquals("000000000", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:idno[@type='PPNa']", null));
        Assert.assertEquals("111111111", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:idno[@type='PPNc']", null));
        Assert.assertEquals("222222222", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:idno[@type='PPNf']", null));
        // fileDesc/sourceDesc/biblStruct/monogr
        // TODO title
        Assert.assertEquals("test edition", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct[@ComponentId='clarin.eu:cr1:c_1379939315559']/tei:monogr[@ComponentId='clarin.eu:cr1:c_1379939315552']/tei:edition",
                null));
        // TODO author
        // TODO editor
        Assert.assertEquals("Buenos Aires", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint[@ComponentId='clarin.eu:cr1:c_1379939315555']/tei:pubPlace",
                null));
        Assert.assertEquals("Pablo E. Coni", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:publisher", null));
        Assert.assertEquals("1884", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:date[@cert='high' and @when='1884']", null));
        Assert.assertEquals("35", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:extent[@ComponentId='clarin.eu:cr1:c_1375880372984']/tei:num[@type='pages']",
                null));
        // fileDesc/sourceDesc/biblStruct/relatedItem
        Assert.assertEquals("series title spanish (scope)", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:relatedItem[@ComponentId='clarin.eu:cr1:c_1379939315557']/tei:bibl[@ComponentId='clarin.eu:cr1:c_1379939315556']/tei:title[@xml:lang='spa']",
                null));
        Assert.assertEquals("series title (scope)", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:relatedItem/tei:bibl/tei:title[@xml:lang='eng']", null));
        // fileDesc/sourceDesc/msDesc
        Assert.assertEquals("RA H-13 (1,1884)", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:msDesc[@ComponentId='clarin.eu:cr1:c_1407745712054']/tei:msIdentifier[@ComponentId='clarin.eu:cr1:c_1407745712039']/tei:idno[@type='shelfmark']",
                null));
        Assert.assertEquals("spa", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msContents[@ComponentId='clarin.eu:cr1:c_1407745712038']/tei:msItem[@ComponentId='clarin.eu:cr1:c_1407745712037']/@n",
                null));
        Assert.assertNull(CMDIConverter.getFirstValue(eleTeiHeader, "tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msContents/tei:textLang/@n", null));
        // encodingDesc
        Assert.assertNotNull(CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:encodingDesc[@ComponentId='clarin.eu:cr1:c_1379939315562']/tei:samplingDecl[@ComponentId='clarin.eu:cr1:c_1375880372982']/tei:ab",
                null));
        Assert.assertNotNull(CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:encodingDesc/tei:projectDesc[@ComponentId='clarin.eu:cr1:c_1375880372987']/tei:ab", null));
        // profileDesc
        Assert.assertEquals("eng", CMDIConverter.getFirstValue(eleTeiHeader,
                "tei:profileDesc/tei:langUsage[@ComponentId='clarin.eu:cr1:c_1375880373021']/tei:language[@ident='eng']", null));
        // TODO textClass

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
