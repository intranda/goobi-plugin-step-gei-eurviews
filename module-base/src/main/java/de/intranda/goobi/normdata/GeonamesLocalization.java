package de.intranda.goobi.normdata;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.intranda.goobi.model.GeonamesLocale;

public class GeonamesLocalization {
    
    public static final String BASE_URL = "http://sws.geonames.org/{identifier}/about.rdf";
    public static final String OFFICIAL_NAME_XPATH = "//gn:officialName[@xml:lang='{language}']/text()";
    public static final String ALTERNATE_NAME_XPATH = "//gn:alternateName[@xml:lang='{language}']/text()";
    
    public static final Namespace NAMESPACE_GN = Namespace.getNamespace("gn", "http://www.geonames.org/ontology#");
    public static final Namespace NAMESPACE_XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
    
    public static GeonamesLocale getLocalNames(String language, String identifier) throws IOException, JDOMException {
        
        GeonamesLocalization instance = new GeonamesLocalization();
        return instance.search(language, identifier);
    }

    private GeonamesLocale search(String language, String identifier) throws IOException, JDOMException {
        
        GeonamesLocale locale = new GeonamesLocale(language);
        
        URL url = getURL(identifier);
        Document doc = retrieveDocument(url);
        
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Text> xp = xpfac.compile(getOfficialNameXPath(language), Filters.text(), null, NAMESPACE_GN, NAMESPACE_XML);
        Text text = xp.evaluateFirst(doc);
        if(text != null) {
            locale.setOfficialName(text.getTextTrim());
        }
        
        xp = xpfac.compile(getAlternateNameXPath(language), Filters.text(), null, NAMESPACE_GN, NAMESPACE_XML);
        List<Text> texts = xp.evaluate(doc);
        for (Text text2 : texts) {
            locale.getAlternateNames().add(text2.getTextTrim());
        }
        
        return locale;
    }

    private String getOfficialNameXPath(String language) {
        return OFFICIAL_NAME_XPATH.replace("{language}", language);
    }
    
    private String getAlternateNameXPath(String language) {
        return ALTERNATE_NAME_XPATH.replace("{language}", language);
    }

    private Document retrieveDocument(URL url) throws IOException, JDOMException {
        Document doc = new SAXBuilder().build(url);
        return doc;
//        HttpURLConnection connection = null;
//        InputStream inputStream = null;
//        try {            
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.connect();
//            int responseCode = connection.getResponseCode();
//            if (responseCode != 200) {
//                inputStream = connection.getInputStream();
//                String error = StringUtils.join(IOUtils.readLines(inputStream, connection.getContentEncoding()), "\n");
//                throw new IOException("Geonames call \"" + url + "\" returned status code " + responseCode + "\n Response = " + error);
//            } else {
//                inputStream = connection.getInputStream();
//                String lines = StringUtils.join(IOUtils.readLines(inputStream, connection.getContentEncoding()), "\n");
//                Document doc = new SAXBuilder().build(inputStream);
//                return doc;
//            }
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }
    }

    private URL getURL(String identifier) {
        try {
            return new URL(BASE_URL.replace("{identifier}", identifier));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Something is wrong with the identifier: " + identifier);
        }
    }
    
}
