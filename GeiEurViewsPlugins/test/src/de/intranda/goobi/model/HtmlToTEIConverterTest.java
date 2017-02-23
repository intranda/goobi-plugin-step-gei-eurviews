package de.intranda.goobi.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.intranda.goobi.model.HtmlToTEIConverter.ConverterMode;

public class HtmlToTEIConverterTest {
	
	private static final String RESOURCE_SAMPLE_FILE = "test/resources/resourceBody.html";
	private static final String RESOURCE_REFERENCE_FILE = "test/reference/resourceBody.html";
	private static final String ENCODING = "UTF8";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testconvertResource() throws IOException {
		String input = FileUtils.readFileToString(new File(RESOURCE_SAMPLE_FILE), ENCODING);
		
		String output = new HtmlToTEIConverter(ConverterMode.resource).convert(input);
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

}
