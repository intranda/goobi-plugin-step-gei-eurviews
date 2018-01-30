package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.conversion.HtmlToTEIConvert.ConverterMode;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.plugins.TeiExportPlugin.LanguageEnum;

public class TeiExportPluginTest {

    private static final String LOREM_IPSUM =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    private BibliographicMetadata bibliographicData;
    private ResouceMetadata resourceData;
    private List<Context> descriptionList;
    private List<Transcription> transcriptionList;
    private List<Image> currentImages;
    private List<Topic> topicList;
    TeiExportPlugin plugin;
    LanguageEnum language = LanguageEnum.GERMAN;
    String transcriptionHtml;
    Process process;

    File sampleHtmlFile = new File("test/resources/transcription.html");

    @Before
    public void setUp() throws Exception {
        plugin = new TeiExportPlugin();
        transcriptionHtml = FileUtils.readFileToString(sampleHtmlFile, "utf-8");
        bibliographicData = createBibliographicData();
        resourceData = createResourceMetadata();
        descriptionList = createDescriptions();
        transcriptionList = createTranscriptions();
        currentImages = createImages();
        topicList = createKeywords();
        LogEntry entry = new LogEntry();
        entry.setContent("Log message");
        entry.setCreationDate(new Date());
        entry.setSecondContent("Previous message");
        process = new Process();
        process.setProcessLog(Collections.singletonList(entry));
    }

    private List<Topic> createKeywords() {
        Topic topic = new Topic();
        Keyword keyword = new Keyword();
        keyword.setGndId("gnd01");
        keyword.setKeywordNameDE("Schlüssel");
        keyword.setKeywordNameEN("key");
        keyword.setWvId("WV01");
        topic.addKeyword(keyword);
        return Collections.singletonList(topic);
    }

    private List<Image> createImages() {
        Image image = new Image(1);
        image.setStructType("Quelle");
        return Collections.singletonList(image);
    }

    private List<Transcription> createTranscriptions() {
        Transcription transcription = new Transcription(1);
        transcription.setLanguage(language.getLanguage());
        transcription.setTranscription(transcriptionHtml);
        SimpleMetadataObject smo = new SimpleMetadataObject("Der Alte vom Wandernden Berge");
        transcription.setTranslatorList(Collections.singletonList(smo));
        return Collections.singletonList(transcription);
    }

    private List<Context> createDescriptions() {
        Context context = new Context(1);
        context.setLanguage(language.getLanguage());
        context.setBookInformation("<p>" + LOREM_IPSUM + "</p>");
        context.setShortDescription("<p>" + LOREM_IPSUM + "</p>");
        context.setLongDescription("<p>" + LOREM_IPSUM + "</p>");
        return Collections.singletonList(context);
    }

    private BibliographicMetadata createBibliographicData() {
        BibliographicMetadata data = new BibliographicMetadata(1);
        List<Person> persons = createPersonList();
        data.setPersonList(persons);
        Corporation publisher = new Corporation();
        publisher.setName("Bastian Balthasar Bux");
        publisher.setNormdataId("gnd", "1234");
        data.setCorporationList(Collections.singletonList(publisher));
        data.setEdition("erste Ausgabe");
        data.setPublicationYear("1977");
        data.setDocumentType("monograph");
        data.setShelfmark("MD-1 1977");
        data.setPhysicalLocation("Göttingen, intranda");
        data.setLanguageList(Collections.singletonList(new SimpleMetadataObject(language.getLanguage())));
        data.setEducationLevel("Secundarstufe");
        data.addSchoolSubject(new SimpleMetadataObject("Physik"));
        Location country = new Location("country");
        country.setName("Aotearoa");
        data.setCountryList(Collections.singletonList(country));
        data.setStateList(Collections.singletonList(new Location("Otago")));
        return data;
    }

    private ResouceMetadata createResourceMetadata() {
        ResouceMetadata rm = new ResouceMetadata(1);
        rm.getResourceTitle().setTranslationGER("Beispieltitel");

        return rm;
    }

    /**
     * @return
     */
    private List<Person> createPersonList() {
        List<Person> persons = new ArrayList<Person>();
        Person author = new Person();
        author.setFirstName("Anna");
        author.setLastName("Blume");
        author.setRole("aut");
        persons.add(author);
        Person editor = new Person();
        editor.setFirstName("Karl Konrad");
        editor.setLastName("Koreander");
        editor.setRole("edt");
        persons.add(editor);
        return persons;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConvertBody() throws IOException {
        File sampleHtmlFile = new File("test/resources/transcription.html");
        File sampleTeiFile = new File("test/resources/transcription.xml");
        String htmlString = FileUtils.readFileToString(sampleHtmlFile, "utf-8");
        TeiExportPlugin plugin = new TeiExportPlugin();
        String teiString = plugin.convertBody(htmlString, ConverterMode.resource);
        FileUtils.write(sampleTeiFile, teiString, "utf-8");
        System.out.println("HTML:");
        System.out.println(htmlString);
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("TEI");
        System.out.println(teiString);

    }

    @Test
    public void testCreateTEiDocForLanguage() throws IOException, JDOMException {
        plugin.setBibliographicData(bibliographicData);
        plugin.setResouceMetadata(resourceData);
        plugin.setCurrentImages(currentImages);
        plugin.setDescriptionList(descriptionList);
        plugin.setTopicList(topicList);
        plugin.setTranscriptionList(transcriptionList);
        plugin.setProcess(process);

        Document teiDoc = plugin.createTEiDocForLanguage(language);
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(teiDoc, new FileWriter(new File("test/resources/tei.xml")));
    }

    @Test
    public void testRemoveExtraElements() throws JDOMException, IOException {
    	File sampleFile = new File("test/resources/testExtraElements.xml");
    	FileReader reader = new FileReader(sampleFile);
		Document doc = new SAXBuilder().build(reader);
		Element root = doc.getRootElement();
		TeiExportPlugin plugin = new TeiExportPlugin();
		plugin.removeExtraElements(root);
		String output = new XMLOutputter().outputString(doc);
		System.out.println(output);


		
    }
    
    @Test
    public void testSymlinks() throws IOException {
        File source = new File("test/reference/symlink.test");
        File symLinkFolder = new File("test/temp");
        if(!source.isFile() && !source.createNewFile()) {
            Assert.fail("Unable to create source file");
        }
        
        File symlink = new File(symLinkFolder, source.getName());
        try {
            
        
        Assert.assertFalse("Symlink file must not exists prior to test", Files.isSymbolicLink(symlink.toPath()));
        
        Files.createSymbolicLink(symlink.toPath(), source.toPath());
        Assert.assertTrue("Symlink file must  exists after creation", Files.isSymbolicLink(symlink.toPath()));
        } finally {
            symlink.delete();
            Assert.assertFalse("Symlink file must not exists prior to test", Files.isSymbolicLink(symlink.toPath()));
        }

        
    }
    
}
