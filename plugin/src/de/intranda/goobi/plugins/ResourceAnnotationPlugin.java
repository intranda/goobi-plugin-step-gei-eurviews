package de.intranda.goobi.plugins;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.JDOMException;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.Language;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.annotation.AnnotationMetadata;
import de.intranda.goobi.model.annotation.Contribution;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.normdata.NormdataSearch;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public @Data class ResourceAnnotationPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceAnnotationPlugin.class);
    private Step step;
    private String returnPath;
    private static final String PLUGIN_NAME = "Gei_WorldViews_ResourceAnnotation";
    private static final String GUI_PATH = "/Gei_WorldViews_ResourceAnnotationPlugin.xhtml";

    private AnnotationMetadata data;

    private List<Contribution> contributionList;
    private Contribution currentContribution;
    private Contribution referenceContribution;

    private Source currentSource;

    private List<Source> sourceList = new ArrayList<>();

    private List<Topic> topicList = new ArrayList<>();

    private NormdataSearch search;
    private List<Map<String, String>> resourceDataList;

    private List<String> possibleLanguages;
    private List<String> possiblePersons;
    private List<String> possibleClassifications;
    private List<String> possibleLicences;

    private String searchValue;
    private String index;
    private String rowType;
    private String searchDatabase;

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    public String getDescription() {
        return PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        this.returnPath = returnPath;
        this.search = new NormdataSearch(ConfigPlugins.getPluginConfig(this));
        int processId = step.getProzess().getId();

        XMLConfiguration config =  ConfigPlugins.getPluginConfig(this);
        possibleLanguages = Arrays.asList(config.getStringArray("elements.language"));
        possiblePersons = Arrays.asList(config.getStringArray("elements.person"));
        possibleLicences = Arrays.asList(config.getStringArray("elements.licence"));
        possibleClassifications = Arrays.asList(config.getStringArray("classification.value"));
        topicList = KeywordHelper.getInstance().initializeKeywords();
        try {
            data = WorldViewsDatabaseManager.getContributionDescription(processId);
            if (data.getAuthorList().isEmpty()) {
                data.getAuthorList().add(new Person());
            }
            if(data.getDigitalCollections().isEmpty()) {
                data.getDigitalCollections().add(getDefaultDigitalCollection());
            }

            if(StringUtils.isBlank(data.getPublicationYearDigital())) {
                data.setPublicationYearDigital(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
            }


            contributionList = WorldViewsDatabaseManager.getContributions(processId);
            sourceList = WorldViewsDatabaseManager.getSourceList(processId);

            List<StringPair> keywordList = WorldViewsDatabaseManager.getKeywordList(processId);
            for (StringPair sp : keywordList) {
                for (Topic topic : topicList) {
                    if (topic.getNameDE().equals(sp.getOne())) {
                        for (Keyword keyword : topic.getKeywordList()) {
                            if (keyword.getKeywordNameDE().equals(sp.getTwo())) {
                                keyword.setSelected(true);
                                break;
                            }
                        }

                    }
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        if (contributionList.isEmpty()) {
            Contribution contribution = new Contribution(processId);
            contribution.setLanguage(getPossibleLanguages().get(0));
            contributionList.add(contribution);
        }

        if (sourceList.isEmpty()) {
            sourceList.add(new Source(processId));
        }

        this.currentContribution = contributionList.get(0);
        if (this.contributionList.size() > 1) {
            this.referenceContribution = contributionList.get(1);
        } else {
            this.referenceContribution = currentContribution;
        }

        for (Contribution contribution : this.contributionList) {
            setDefaultText(contribution);
        }


    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public String cancel() {
        return "/" + Helper.getTheme() + returnPath;
    }

    public int getProcessId() {
        return data.getProcessId();
    }

    public void save() {
        try {
            for (Contribution contribution : this.contributionList) {
                setDefaultText(contribution);
            }
            WorldViewsDatabaseManager.saveContribtutionDescription(data);
            for (Contribution contribution : contributionList) {
                WorldViewsDatabaseManager.saveContribution(contribution, getProcessId());
            }
            WorldViewsDatabaseManager.saveSourceList(sourceList, getProcessId());
            WorldViewsDatabaseManager.saveKeywordList(topicList, getProcessId());
            Helper.setMeldung("dataSavedSuccessfully");
        } catch (SQLException e) {
            logger.error(e);
            Helper.setFehlerMeldung("dataCouldNotBeSaved", e);
        }
    }

    @Override
    public String finish() {
        return "/" + Helper.getTheme() + returnPath;
    }



    public void addSource() {
        sourceList.add(new Source(getProcessId()));
    }

    public void deleteSource() {
        if (sourceList.contains(currentSource)) {
            sourceList.remove(currentSource);
        }
        try {
            WorldViewsDatabaseManager.saveSourceList(sourceList, getProcessId());
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public int getSizeOfAuthorList() {
        return data.getAuthorList().size();
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
    }

    @Override
    public String getPagePath() {
        return "/" + Helper.getTheme() + GUI_PATH;
    }

    public void searchResources() {
        try {
            resourceDataList = WorldViewsDatabaseManager.getResource(searchValue);
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    public void updateKeywordList(Integer prozesseID) {
        if (!"Bildungsgeschichte".equals(data.getContributionType())) {
            try {
                List<StringPair> keyowrdList = WorldViewsDatabaseManager.getKeywordList(prozesseID);

                for (StringPair sp : keyowrdList) {
                    for (Topic topic : getTopicList()) {
                        if (topic.getNameDE().equals(sp.getOne())) {
                            for (Keyword keyword : topic.getKeywordList()) {
                                if (keyword.getKeywordNameDE().equals(sp.getTwo())) {
                                    keyword.setSelected(true);
                                    break;
                                }
                            }

                        }
                    }
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        }
    }

    public List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        for (Contribution contribution : contributionList) {
            languages.add(contribution.getLanguage());
        }
        return languages;
    }

    public String getCurrentContributionLanguage() {
        return getCurrentContribution().getLanguage();
    }

    public void setCurrentContributionLanguage(String language) {
        Contribution selected = getContribution(language);
        if (selected == null) {
            selected = new Contribution(getProcessId());
            selected.setLanguage(language);
            this.contributionList.add(selected);
            setDefaultText(selected);
        }
        this.currentContribution = selected;
    }

    private void setDefaultText(Contribution contribution) {
        String keyProjectDesc = "default.{lang}.projectDesc".replace("{lang}", contribution.getLanguage());
        if(StringUtils.isBlank(contribution.getContext())){
            contribution.setContext(ConfigPlugins.getPluginConfig(this).getString(keyProjectDesc, ""));
        }
    }

    public String getReferenceContributionLanguage() {
        return getReferenceContribution().getLanguage();
    }

    public void setReferenceContributionLanguage(String language) {
        Contribution selected = getContribution(language);
        if (selected == null) {
            throw new IllegalStateException("Try to get reference contribution for nonexistent language " + language);
        }
        this.referenceContribution = selected;
    }

    public Contribution getContribution(String language) {
        for (Contribution contribution : contributionList) {
            if (contribution.getLanguage().equals(language)) {
                return contribution;
            }
        }
        return null;
    }

    public String search() {
        //        String database = "gnd";
        //        ComplexMetadataObject object = getSelectedPerson();
        //        if (object != null && StringUtils.isNotBlank(object.getNormdataAuthority())) {
        //            database = object.getNormdataAuthority();
        //        }
        return search.search(searchDatabase);
    }

    protected String filter(String str) {
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            // current != 0x152 && current != 0x156
            if (current != 0x98 && current != 0x9C) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }

    public String setSoruceData(String processId) {
        Source currentSource = sourceList.get(Integer.parseInt(index));
        currentSource.setData(Integer.parseInt(processId));

        return "";
    }

    public String getData(List<NormData> currentData) {
        Person person = getSelectedPerson();

        person.resetNormdataValues();

        for (NormData normdata : currentData) {
            if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                person.setNormdataId("gnd", normdata.getValues().get(0).getText());
            }else if (normdata.getKey().equals("NORM_IDENTIFIER_EDU_EXPERTS")) {
                person.setNormdataId("edu.experts", normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("URI")) {
                person.setNormdataUri("gnd", normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("URI_EDU_EXPERTS")) {
                person.setNormdataUri("edu.experts", normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("NORM_NAME")) {
                String value = normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "");
                value = filter(value);
                if (value.contains(",")) {
                    person.setLastName(value.substring(0, value.indexOf(",")).trim());
                    person.setFirstName(value.substring(value.indexOf(",") + 1).trim());
                } else if (value.contains(" ")) {
                    String[] nameParts = value.split(" ");
                    String first = "";
                    String last = "";
                    if (nameParts.length == 1) {
                        last = nameParts[0];
                    } else if (nameParts.length == 2) {
                        first = nameParts[0];
                        last = nameParts[1];
                    } else {
                        int counter = nameParts.length;
                        for (int i = 0; i < counter; i++) {
                            if (i == counter - 1) {
                                last = nameParts[i];
                            } else {
                                first += " " + nameParts[i];
                            }
                        }
                    }
                    person.setLastName(last);
                    person.setFirstName(first);
                } else {
                    person.setLastName(value);
                }
            }
        }
        if(search.addEduExpertsNormdata(person)) {
            logger.debug("Added edu.experts normdata");
        }
        return "";
    }

    /**
     * @return
     */
    private Person getSelectedPerson() {
        Person person = data.getAuthorList().get(Integer.parseInt(index));
        return person;
    }

    public List<String> getPossibleDigitalCollections() {
        try {
            return DigitalCollections.possibleDigitalCollectionsForProcess(getStep().getProzess());
        } catch (JDOMException | IOException e) {
            logger.error(e);
            return Collections.singletonList(getDefaultDigitalCollection());
        }
    }

    public String getDefaultDigitalCollection() {
        return ConfigPlugins.getPluginConfig(this).getString("default.digitalCollection", "WorldViews");
    }

    public String getLanguageData(Language currentLanguage) {

        switch(rowType) {
            case "languageContribution":
                currentContribution.setLanguageCode(currentLanguage.getIsoCode_639_2());
                break;
        }

        return "";
    }

    public String searchLanguage() {
        return search.searchLanguage();
    }

    public boolean isNotBlank(String string) {
        return StringUtils.isNotBlank(string);
    }
}
