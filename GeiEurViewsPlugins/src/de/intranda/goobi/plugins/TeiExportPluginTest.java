package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TeiExportPluginTest {

	@Before
	public void setUp() throws Exception {
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
		String teiString = plugin.convertBody(htmlString);
		FileUtils.write(sampleTeiFile, teiString, "utf-8");
		System.out.println("HTML:");
		System.out.println(htmlString);
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("TEI");
		System.out.println(teiString);
	
	}

}
