package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Content.CType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TeiAnnotationExportPluginTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTeiConformance() throws JDOMException, IOException {
        Element root = new Element("div");
        Element head = new Element("head");
        root.addContent(head);
        head.addContent("ein Text ");
        Element hi = new Element("hi");
        hi.setText("zwei Text ");
        head.addContent(hi);
        head.addContent("drei Text");
        Assert.assertEquals("ein Text ", head.getContent(0).getValue());
        Assert.assertEquals(CType.Element, head.getContent(1).getCType());
        Assert.assertEquals("zwei Text ", head.getContent(1).getValue());
        Assert.assertEquals("drei Text", head.getContent(2).getValue());
        
        new TeiAnnotationExportPlugin().teiConformance(root);
        Assert.assertEquals("ein Text ", head.getContent(0).getValue());
        Assert.assertEquals(CType.Text, head.getContent(1).getCType());
        Assert.assertEquals("zwei Text ", head.getContent(1).getValue());
        Assert.assertEquals("drei Text", head.getContent(2).getValue());
    
        File testFile = new File("src/test/resources/reference/tei-conformance.xml");
        File referenceFile = new File("src/test/resources/reference/tei-conformance_reference.xml");
        File outFile = new File("src/test/resources/reference/tei-conformance_output.xml");
        
        Document testDoc = getDocument(testFile);   
        Document referenceDoc = getDocument(referenceFile);
        
        new TeiAnnotationExportPlugin().teiConformance(testDoc.getRootElement());
        writeDocument(testDoc, outFile);
        
        IteratorIterable<Element> figureElements = testDoc.getRootElement().getDescendants(org.jdom2.filter.Filters.element("figure", null));
        int counter = 0;
        while(figureElements.hasNext()) {
            counter++;
            Assert.assertEquals("div",figureElements.next().getParentElement().getName());
        }
        Assert.assertEquals(2, counter);
    }

    private void writeDocument(Document testDoc, File outFile) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.output(testDoc, new FileWriter(outFile));
        
    }

    private Document getDocument(File file) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(file);
    }

}
