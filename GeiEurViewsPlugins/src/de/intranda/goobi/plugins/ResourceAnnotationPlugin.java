package de.intranda.goobi.plugins;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.goobi.model.annotation.Annotation;
import de.intranda.goobi.model.annotation.Author;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.persistence.DatabaseManager;
import de.sub.goobi.config.ConfigPlugins;

@PluginImplementation
public class ResourceAnnotationPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceAnnotationPlugin.class);
    private Step step;
    private String returnPath = "/ui/task_edit.xhtml";
    private static final String PLUGIN_NAME = "ResourceAnnotation";
    private static final String GUI_PATH = "/ui/ResourceAnnotationPlugin.xhtml";

    private int processId;
    private List<String> possibleLanguages;

    private Author currentAuthor;
    private List<Author> authorList = new ArrayList<Author>();

    private Source currentSource;
    private List<Source> sourceList = new ArrayList<Source>();

    private Annotation currentAnnotation;
    private List<Annotation> annotationList = new ArrayList<Annotation>();

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        processId = step.getProzess().getId();
        possibleLanguages = ConfigPlugins.getPluginConfig(this).getList("elements.language");

        try {
            annotationList = DatabaseManager.getAnnotationList(processId);
            authorList = DatabaseManager.getAuthorList(processId);
            sourceList = DatabaseManager.getSourceList(processId);
        } catch (SQLException e) {
            logger.error(e);
        }
        if (annotationList.isEmpty()) {
            annotationList.add(new Annotation(processId));
        }
        if (authorList.isEmpty()) {
            authorList.add(new Author(processId));
        }
        if (sourceList.isEmpty()) {
            sourceList.add(new Source(processId));
        }
        
    }

    @Override
    public boolean execute() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String cancel() {
        return returnPath;
    }

    public void save() {
        System.out.println("save");

        try {
            DatabaseManager.saveAnnotationList(annotationList, processId);
            DatabaseManager.saveAuthorList(authorList, processId);
            DatabaseManager.saveSourceList(sourceList, processId);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    @Override
    public String finish() {
        // TODO Auto-generated method stub
        return returnPath;
    }

    public List<Author> getAuthorList() {
        return authorList;
    }

    public void addAuthor() {
        authorList.add(new Author(processId));
    }

    public void deleteAuthor() {
        if (authorList.contains(currentAuthor)) {
            authorList.remove(currentAuthor);
        }
        try {
            DatabaseManager.saveAuthorList(authorList, processId);
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
    public Step getStep() {
        return step;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
    }

    @Override
    public String getPagePath() {
        return GUI_PATH;
    }

    public Author getCurrentAuthor() {
        return currentAuthor;
    }

    public void setCurrentAuthor(Author currentAuthor) {
        this.currentAuthor = currentAuthor;
    }

    public Source getCurrentSource() {
        return currentSource;
    }

    public void setCurrentSource(Source currentSource) {
        this.currentSource = currentSource;
    }

    public List<Source> getSourceList() {
        return sourceList;
    }

    public List<BibliographicData> completeSource(String query) {

        try {
            return DatabaseManager.getBibliographicData(query);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
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

    public int getSizeOfSourceList() {
        return sourceList.size();
    }

    public Annotation getCurrentAnnotation() {
        return currentAnnotation;
    }

    public void setCurrentAnnotation(Annotation currentAnnotation) {
        this.currentAnnotation = currentAnnotation;
    }

    public List<Annotation> getAnnotationList() {
        return annotationList;
    }

    public void addAnnotation() {
        annotationList.add(new Annotation(processId));
    }

    public void deleteAnnotation() {
        if (annotationList.contains(currentAnnotation)) {
            annotationList.remove(currentAnnotation);
        }
        try {
            DatabaseManager.saveAnnotationList(annotationList, processId);
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    public int getSizeOfAnnotationList() {
        return annotationList.size();
    }

    public List<String> getPossibleLanguages() {
        return possibleLanguages;
    }

}
