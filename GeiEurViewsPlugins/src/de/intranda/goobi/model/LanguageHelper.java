package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;

import com.sun.org.apache.xml.internal.utils.SuballocatedByteVector;

import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;

public class LanguageHelper {
    private static XMLConfiguration config;

    private static LanguageHelper helper;

    private Locale locale;

    public static LanguageHelper getInstance() {
        if (helper == null) {
            helper = new LanguageHelper();
            String file = "plugin_languages.xml";
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
    
    public Language getLanguage(String isoCode) {
        SubnodeConfiguration languageConfig = null;
        if(isoCode.length() == 3) {
            languageConfig = config.configurationAt("language[iso_639-2=\"" + isoCode + "\"]");
        } else if(isoCode.length() == 2) {
            languageConfig = config.configurationAt("language[iso_639-1=\"" + isoCode + "\"]");
        }
        if(languageConfig == null) {            
            throw new IllegalArgumentException("No matching language found for " + isoCode);
        }
        Language language = new Language();
        language.setIsoCode(languageConfig.getString("iso_639-2"));
        language.setIsoCodeOld(languageConfig.getString("iso_639-1"));
        language.setEnglishName(languageConfig.getString("eng"));
        language.setGermanName(languageConfig.getString("ger"));
        language.setFrenchName(languageConfig.getString("fre"));
        
        return language;
    }

}
