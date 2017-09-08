package de.intranda.goobi.model.resource;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;

import de.intranda.goobi.model.EurViewsRecord;
public class BibliographicMetadataBuilderTest {

    private static final File digiSourceFile = new File("test/resources/digiSource.xml");

    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws IOException {
        EurViewsRecord record = new EurViewsRecord();
        record.setData(FileUtils.readFileToString(digiSourceFile, "utf-8"));
        
        
        
        Process process = new Process();
        process.setId(1008);
        process.setRegelsatz(new Ruleset());
        process.getRegelsatz().setDatei("WorldViews.xml");
        process.getRegelsatz().getPreferences();
        
        BibliographicMetadata data = BibliographicMetadataBuilder.build(process, record);
        
        Assert.assertEquals("Sekundarstufe 2", data.getEducationLevel());
    }

}
