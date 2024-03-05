package de.intranda.goobi.model;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class EurViewsRecordTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws JDOMException, IOException {
        EurViewsRecord record = new EurViewsRecord();
        record.setData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><bibRef><authors><author xml:lang='l1'>a1</author><author>a2</author><author role='r3'>a3</author></authors></bibRef></xml>");
        String xPath = "bibRef/authors/author[@role]";
        Assert.assertEquals(xPath + " --> " + record.get(xPath), "a3", record.get(xPath));
        
        String xPath2 = "bibRef/authors/author/@xml:lang";
        Assert.assertEquals(xPath2 + " --> " + record.get(xPath2), "l1", record.get(xPath2));
    }

}
