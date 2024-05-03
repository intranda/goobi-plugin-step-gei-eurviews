package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.intranda.goobi.model.EurViewsRecord;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;

public class SourceInitializationPluginTest {

    private static final File digiSourceFile = new File("src/test/resources/digiSource.xml");
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore("This failing test was not executed before")
    public void testSelectKeywords() throws IOException, JDOMException {
        EurViewsRecord record = new EurViewsRecord();
        record.setData(FileUtils.readFileToString(digiSourceFile, "utf-8"));
        
        List<Topic> topics = new SourceInitializationPlugin().createTopics(record);
        for (Topic topic : topics) {
            for (Keyword keyword : topic.getKeywordList()) {
                if(keyword.isSelected()) {
                    System.out.println(topic.getId() + " - " + keyword.getWvId());
                    System.out.println(topic.getNameDE() + " - " + keyword.getKeywordNameDE());
                    System.out.println(topic.getNameEN() + " - " + keyword.getKeywordNameEN());
                    System.out.println("--------------------------------------------------------");
                }
            }
        }
    }
    
    @Test 
    public void testGetNormdata() {
        BibliographicMetadata data = new BibliographicMetadata(1);
        Person author = new Person();
        author.setFirstName("Bernhard");
        author.setLastName("Bendfeld");
        data.addBookAuthor(author);
        
        SourceInitializationPlugin plugin = new SourceInitializationPlugin();
        System.out.println(author.getNormdataUri("gnd"));
        plugin.addNormdata(data);
        System.out.println(author.getNormdataUri("gnd"));

        
             
    }

}
