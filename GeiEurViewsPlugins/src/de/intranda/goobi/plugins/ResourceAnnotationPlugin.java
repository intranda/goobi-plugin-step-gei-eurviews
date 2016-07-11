package de.intranda.goobi.plugins;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.annotation.Contribution;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.persistence.DatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;

@PluginImplementation
public @Data class ResourceAnnotationPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceAnnotationPlugin.class);
    private Step step;
    private String returnPath = "/task_edit.xhtml";
    private static final String PLUGIN_NAME = "ResourceAnnotation";
    private static final String GUI_PATH = "/ResourceAnnotationPlugin.xhtml";

    private int processId;
    private List<String> possibleLanguages;
    private List<String> possiblePersons;

    private List<String> possibleClassifications;
    private List<String> possibleLicences;

    private Person currentPerson;
    private List<Person> authorList = new ArrayList<>();

    private String contributionType;
    private String edition;
    private String publisher = "Georg-Eckert-Institut";
    private String project = "WorldViews";
    private String availability;
    private String licence = "CC BY-NC-ND 3.0 DE";

    private Contribution contribution;

    private Source currentSource;

    private List<Source> sourceList = new ArrayList<>();

    private List<Topic> topicList = new ArrayList<>();

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
        processId = step.getProzess().getId();
        possibleLanguages = ConfigPlugins.getPluginConfig(this).getList("elements.language");
        possiblePersons = ConfigPlugins.getPluginConfig(this).getList("elements.person");
        possibleLicences = ConfigPlugins.getPluginConfig(this).getList("elements.licence");
        possibleClassifications = ConfigPlugins.getPluginConfig(this).getList("classification.value");
        topicList = KeywordHelper.getInstance().initializeKeywords();
        try {
            contribution = DatabaseManager.getContribution(processId);
            authorList = DatabaseManager.getAuthorList(processId);
            sourceList = DatabaseManager.getSourceList(processId);

            List<StringPair> keyowrdList = DatabaseManager.getKeywordList(processId);
            for (StringPair sp : keyowrdList) {
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
        if (contribution == null) {
            contribution = new Contribution(processId);
        }
        if (authorList.isEmpty()) {
            authorList.add(new Person());
        }
        if (sourceList.isEmpty()) {
            sourceList.add(new Source(processId));
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

    public void save() {
        try {
            DatabaseManager.saveContribution(contribution, processId);
            DatabaseManager.saveAuthorList(authorList, processId);
            DatabaseManager.saveSourceList(sourceList, processId);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    @Override
    public String finish() {
        return "/" + Helper.getTheme() + returnPath;
    }

    public void addAuthor() {
        authorList.add(new Person());
    }

    public void deletePerson() {
        if (authorList.contains(currentPerson)) {
            authorList.remove(currentPerson);
        }
        try {
            DatabaseManager.saveAuthorList(authorList, processId);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void addSource() {
        sourceList.add(new Source(processId));
    }

    public void deleteSource() {
        if (sourceList.contains(currentSource)) {
            sourceList.remove(currentSource);
        }
        try {
            DatabaseManager.saveSourceList(sourceList, processId);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public int getSizeOfAuthorList() {
        return authorList.size();
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

    public List<BibliographicData> completeSource(String query) {

        try {
            return DatabaseManager.getBibliographicData(query);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    public void updateKeywordList(Integer prozesseID) {
        if (!contributionType.equals("Bildungsgeschichte")) {
            try {
                List<StringPair> keyowrdList = DatabaseManager.getKeywordList(prozesseID);

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

}
