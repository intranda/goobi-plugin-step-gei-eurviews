package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.model.SelectItem;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;

public class SourceTypeHelper {
    
    private static XMLConfiguration config;

    private static SourceTypeHelper helper;

    public static SourceTypeHelper getInstance() {
        if (helper == null) {
            helper = new SourceTypeHelper();
            String file = "plugin_sourceTypes.xml";
            try {
                config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + file);
            } catch (ConfigurationException e) {
                config = new XMLConfiguration();
            }
            config.setListDelimiter('&');
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setExpressionEngine(new XPathExpressionEngine());
        }

        return helper;
    }
    
    public static Locale getCurrentLocale() {
        try {
            return FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
            return Locale.ENGLISH;
        }
        
    }
    
    public static String getCurrentLanguage() {
        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            if(desiredLanguage.getLanguage().equals(Locale.GERMAN.getLanguage())) {
                return "ger";
            } else {
                return "eng";
            }
        } else {
            return "eng";
        }
    }
    

    public List<SourceType> initializeResourceTypes() {
        
        List<HierarchicalConfiguration> types = config.configurationsAt("sourceType");
        return initSourceType(types, null, 0);

    }

    /**
     * @param types
     * @param locale 
     * @param parentValue
     */
    public List<SourceType> initSourceType(List<HierarchicalConfiguration> types, SourceType parent, int level) {
        List<SourceType> possibleTypes = new ArrayList<>();
        for (HierarchicalConfiguration type : types) {
            
            String valueGer = (parent != null ? (parent.getValueGer() + " - ") : "") + type.getString("value[@lang=\"ger\"]");
            String valueEng = (parent != null ? (parent.getValueEng() + " - ") : "") + type.getString("value[@lang=\"eng\"]");
            String label = "";
            for (int i = 0; i < level; i++) {
                label += "\t-\t";
            }
            String labelGer = label + type.getString("value[@lang=\"ger\"]");
            String labelEng = label + type.getString("value[@lang=\"eng\"]");
            SourceType sourceType = new SourceType(valueGer, labelGer, valueEng, labelEng);
            possibleTypes.add(sourceType);
            List<HierarchicalConfiguration> subTypes = type.configurationsAt("sourceType");
            possibleTypes.addAll(initSourceType(subTypes, sourceType, level+1));
        }
        return possibleTypes;
    }

    private String getLanguageCode(Locale locale) {
        if(locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
            return "ger";
        } else {
            return "eng";
        }
    }
    
    /**
     * Find a sourceType by its German name. If there is no sourceType with a matching German name, null is returned
     * 
     * @param value
     * @return
     */
    public SourceType findSourceType(String value) {
        
        List<SourceType> types = initializeResourceTypes();
        for (SourceType sourceType : types) {
            if(sourceType.getValue().equals(value)) {
                return sourceType;
            }
        }
        return null;
    }
}
