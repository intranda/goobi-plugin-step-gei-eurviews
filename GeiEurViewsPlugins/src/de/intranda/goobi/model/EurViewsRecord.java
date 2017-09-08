package de.intranda.goobi.model;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.importer.Record;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class EurViewsRecord extends Record {
    
    public static final Namespace TEI = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
    protected static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    private String ppn;
    private Document doc;
    
    public void setPPN(String pPN) {
        ppn = pPN;
    }
    
    public String getPPN() {
        return ppn;
    }
    
    public boolean hasPPN() {
        return StringUtils.isNotBlank(ppn);
    }
    
    public String get(String path) throws JDOMException, IOException {
        return get(path, null);
    }
    
    public String get(String path, String defaultValue) throws JDOMException, IOException {
        if(!path.startsWith(getDocument().getRootElement().getName())) {
            path = getDocument().getRootElement().getName() + "/" + path;
        }
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<? extends Object> xp = xpfac.compile(path, Filters.fpassthrough() , null, XML);
        Object result = xp.evaluateFirst(getDocument());
        if(result != null) {            
            if (result instanceof Attribute) {
                return ((Attribute)result).getValue();
            }
            if (result instanceof Content) {
                return ((Content)result).getValue();
            }
            return String.valueOf(result);
        } else if(defaultValue != null) {
            return defaultValue;
        } else throw new IllegalArgumentException("No value found at " + path);
    }
    
    public List<String> getAll(String path) throws JDOMException, IOException {
        if(!path.startsWith(getDocument().getRootElement().getName())) {
            path = getDocument().getRootElement().getName() + "/" + path;
        }
        List<String> values = new ArrayList<>();
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Object> xp = xpfac.compile(path, Filters.fpassthrough(), null, XML);
        List<Object> results = xp.evaluate(getDocument());
        for (Object result : results) {
            if(result != null) {            
                if (result instanceof Attribute) {
                    values.add(((Attribute)result).getValue());
                } else if (result instanceof Content) {
                    values.add(((Content)result).getValue());
                } else {                    
                    values.add(String.valueOf(result));
                }
                
            }
        }
        return values;
    }
    
    public Document getDocument() throws JDOMException, IOException {
        if(doc == null) {
            createDocument();
        }
        return doc;
    }

    private void createDocument() throws JDOMException, IOException {
       if(StringUtils.isBlank(getData()))  {
           throw new IllegalStateException("Data must be set bedore creating document");
       }
       SAXBuilder builder = new SAXBuilder();
       try(StringReader reader = new StringReader(getData())) {           
           doc = builder.build(reader);
       }
    }

}
