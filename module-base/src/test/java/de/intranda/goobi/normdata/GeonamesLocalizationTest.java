package de.intranda.goobi.normdata;

import java.io.IOException;
import java.util.Locale;

import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.intranda.goobi.model.GeonamesLocale;

public class GeonamesLocalizationTest {
    
    public static final String IDENTIFIER_GERMANY = "2921044";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore("This failing test was not executed before")
    public void testGetLocalNames() throws IOException, JDOMException {
        Locale german = Locale.GERMAN;
        String language = german.getLanguage();
        System.out.println("Searching for names of " + IDENTIFIER_GERMANY + " in " + language);
        GeonamesLocale names = GeonamesLocalization.getLocalNames(language, IDENTIFIER_GERMANY);
        System.out.println(names);
        Assert.assertEquals(names.getOfficialName(), "Deutschland");
        Assert.assertTrue(names.getAlternateNames().contains("Bundesrepublik Deutschland"));
    }

}
