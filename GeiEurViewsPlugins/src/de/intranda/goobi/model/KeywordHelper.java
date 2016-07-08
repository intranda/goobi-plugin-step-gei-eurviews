package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.sub.goobi.helper.Helper;

public class KeywordHelper {
    private static XMLConfiguration config;

    private static KeywordHelper helper;

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
                t.setNameDE(topic.getString("name[@language='de']"));
                t.setNameEN(topic.getString("name[@language='en']"));
                answer.add(t);

                List<HierarchicalConfiguration> keywordList = topic.configurationsAt("keyword");

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

                        List<String> synonymListDe = keyword.getList("synonym[@language='de']");
                        List<String> synonymListEn = keyword.getList("synonym[@language='en']");

                        k.setSynonymListDE(synonymListDe);

                        k.setSynonymListEN(synonymListEn);

                        t.addKeyword(k);
                    }
                }

            }
        }
        HashMap<String, String> uiStatus = (HashMap<String, String>) Helper.getManagedBeanValue("#{NavigationForm.uiStatus}");
        uiStatus.put("gei_topic", answer.get(0).getNameDE());

        return answer;
    }

}
