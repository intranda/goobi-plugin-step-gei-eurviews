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
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;

public class KeywordHelper {
    private static XMLConfiguration config;
    private static XMLConfiguration mapping;

    private static KeywordHelper helper;

    private Locale locale;

    public static KeywordHelper getInstance() {
        if (helper == null) {
            helper = new KeywordHelper();
            String file = "plugin_keywords.xml";
            try {
                config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + file);
            } catch (ConfigurationException e) {
                config = new XMLConfiguration();
            }
            config.setListDelimiter('&');
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setExpressionEngine(new XPathExpressionEngine());
            
            String mappingFile = "plugin_keyword_mappings.xml";
            try {
                mapping = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + mappingFile);
            } catch (ConfigurationException e) {
                mapping = new XMLConfiguration();
            }
            mapping.setListDelimiter('&');
            mapping.setReloadingStrategy(new FileChangedReloadingStrategy());
            mapping.setExpressionEngine(new XPathExpressionEngine());
        }

        return helper;
    }

    @SuppressWarnings("unchecked")
    public List<Topic> initializeKeywords() {
        List<Topic> answer = new ArrayList<Topic>();

        List<HierarchicalConfiguration> topicList = config.configurationsAt("topicList/topic");
        if (topicList != null) {
            for (HierarchicalConfiguration topic : topicList) {
                Topic t = new Topic();
                t.setId(topic.getString("@wv"));
                t.setNameDE(topic.getString("name[@language='de']"));
                t.setNameEN(topic.getString("name[@language='en']"));
                t.setDisplay(topic.getString("display"));
                answer.add(t);
            }
        }

        List<HierarchicalConfiguration> keywordList = config.configurationsAt("keywordList/keyword");

        if (keywordList != null) {
            for (HierarchicalConfiguration keyword : keywordList) {
                Keyword k = new Keyword();
                String gndid = keyword.getString("@gnd");
                String wvid = keyword.getString("@wv");
                if (StringUtils.isNotBlank(gndid)) {
                    k.setGndId(gndid);
                }
                if (StringUtils.isNotBlank(wvid)) {
                    k.setWvId(wvid);
                }
                k.setKeywordNameDE(keyword.getString("name[@language='de']"));
                k.setKeywordNameEN(keyword.getString("name[@language='en']"));
                //
                List<String> synonymListDe = keyword.getList("synonym[@language='de']");
                List<String> synonymListEn = keyword.getList("synonym[@language='en']");
                k.setSynonymListDE(synonymListDe);

                k.setSynonymListEN(synonymListEn);
                List<String> associatedTopics = keyword.getList("topic");

                for (Topic topic : answer) {
                    if (associatedTopics.contains(topic.getId())) {
                        topic.addKeyword(k);
                    }
                }

            }
        }

        try {        	
        	locale = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch(NullPointerException e) {
        	//No faces context. Probably an automatic task
        	locale = Locale.GERMANY;
        }

        for (Topic topic : answer) {
            List<Keyword> keywords = topic.getKeywordList();
            Collections.sort(keywords, keywordComparator);
        }

        Map<String, String> uiStatus = (HashMap<String, String>) Helper.getManagedBeanValue("#{NavigationForm.uiStatus}");

        if(uiStatus != null) {        	
        	for (Topic topic : answer ) {
        		if (topic.getDisplay().equals("first")  && StringUtils.isBlank(uiStatus.get("gei_topic"))) {
        			uiStatus.put("gei_topic", topic.getNameDE());
        		} else if (topic.getDisplay().equals("second")  && StringUtils.isBlank(uiStatus.get("gei_secondTopic"))) {
        			uiStatus.put("gei_secondTopic", topic.getNameDE());
        		}
        	}
        }
        return answer;
    }

    private Comparator<Keyword> keywordComparator = new Comparator<Keyword>() {
        @Override
        public int compare(Keyword o1, Keyword o2) {
//            System.out.println( o1.getKeywordNameDE() + " - " +  o2.getKeywordNameDE());
            if (locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
                return o1.getKeywordNameDE().compareTo(o2.getKeywordNameDE());
            } else {
                return o1.getKeywordNameEN().compareTo(o2.getKeywordNameEN());
            }
        }
    };
    
    public List<String> getWorldViewsKeywords(String eurViewsKeyword) {
        return mapping.getList("keyword[EV-SW_de=\""+ eurViewsKeyword +"\"]/WV-SW_de");
    }

}
