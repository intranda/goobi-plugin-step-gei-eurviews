package de.intranda.goobi.plugins;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ViewerTEIExportPluginTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    
//    @Test
    public void testReadTEI() throws FileNotFoundException, IOException, JDOMException {
        File file = new File("/home/florian/GEI/FR_2006_CheungEtAl_HistoireGeographie_57_tei_ger.xml");
        Document doc = ViewerTEIExportPlugin.readXmlFileToDoc(file);
        Assert.assertNotNull(doc);
    }

}
