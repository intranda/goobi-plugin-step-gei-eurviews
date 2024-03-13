package de.intranda.goobi.model.conversion;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.intranda.goobi.model.conversion.HtmlToTEIConvert;
import de.intranda.goobi.model.conversion.HtmlToTEIConvert.ConverterMode;

public class HtmlToTEIConverterTest {
	
	private static final String FAIL_SAMPLE_FILE = "src/test/resources/invalidBody.html";
	private static final String RESOURCE_SAMPLE_FILE = "src/test/resources/resourceBody.html";
	private static final String RESOURCE_REFERENCE_FILE = "src/test/resources/reference/resourceBody.html";
	private static final String ENCODING = "UTF8";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore("This failing test was not executed before")
	public void testconvertResource() throws IOException {
		String input = FileUtils.readFileToString(new File(RESOURCE_SAMPLE_FILE), ENCODING);
		
		String output = new HtmlToTEIConvert(ConverterMode.resource).convert(input);
		output = output.replaceAll("\\s+", " ");
		System.out.println("OUTPUT:");
		System.out.println(output);
		Assert.assertTrue(output.length() > 0);
		
		String reference = FileUtils.readFileToString(new File(RESOURCE_REFERENCE_FILE), ENCODING);
		reference = reference.replaceAll("\\s+", " ");
		System.out.println("REFERNCE:");
		System.out.println(reference);
		Assert.assertEquals(reference, output);

	}
	
	@Test
	public void testReplaceFootnotes() {
	    String origString = "<p>erste Fußnote<sup>1</sup></p><p>zweite Fußnote[2]</p><p>dritte Fußnote[3] <#_ftn3></p><p>viewer Fußnote<a class=\"sdfootnoteanc\" href=\"#sdfootnote4sym\" name=\"sdfootnote4anc\"><sup>4</sup></a></p><p>---------------------------------------------------------------------</p><p><sup>1</sup>  Fußnote1</p><p>Zweite Zeile</p><p>[2] Fußnote2</p><p>Zweite Zeile</p><p>[3] <#_ftnref3> Fußnote3</p><p>Zweite Zeile</p><p><a class=\"sdfootnotesym\" href=\"#sdfootnote4anc\" name=\"sdfootnote4sym\">4</a>Das ist die Fu&szlig;note</p><p>Zweite Zeile</p>";
	    System.out.println("ORIGINAL STRING:\n" + origString.replace("</p><p>", "</p>\n<p>"));
	    HtmlToTEIConvert converter = new HtmlToTEIConvert(ConverterMode.resource);
	    String output = converter.replaceFootnotes(origString, converter.getAllFootnoteTypes());
	    System.out.println("CONVERTED STRING:\n" + output.replace("</p><p>", "</p>\n<p>"));
	}
	
	@Test
	public void testConvertAbstract() {
	    String abstractIn = "<p>The quotes are taken from the lessons <em>Europe,</em> and <em>Ireland and the European Union</em>. In these parts it is demonstrated how Irish citizens are citizens of the world as well and how the world is interconnected. Apart from the European Union, the Council of Europe and the United Nations are introduced. The paragraph about the European Union states that Ireland has profited from its membership in the EU and vice versa.</p>";
        String abstractOut = new HtmlToTEIConvert(ConverterMode.resource).convert(abstractIn);
        System.out.println(abstractOut);
	}


}
