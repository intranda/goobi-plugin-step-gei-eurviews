package de.intranda.goobi.plugins;

import java.sql.SQLException;
import java.util.HashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.persistence.ResourceBibliographicManager;

@PluginImplementation
public class ResourceDescriptionPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceDescriptionPlugin.class);

    private Step step;
    private String returnPath = "/ui/task_edit.xhtml";
    private static final String PLUGIN_NAME = "ResourceDescription";
    private static final String GUI_PATH = "/ui/ResourceDescriptionPlugin.xhtml";

    //    private Integer annotationID = null;

    private BibliographicData data;

    //    private Author currentAuthor;   
    //    private List<Author> authorList = new ArrayList<Author>();

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

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        //        this.returnPath = returnPath;
        try {
            data = ResourceBibliographicManager.getBibliographicData(step.getProzess().getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if (data == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new bibliographic record");
            }
            // TODO get information from mets file?
            data = new BibliographicData(step.getProzess().getId());
        }
        //        authorList.add(new Author());
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

    @Override
    public String finish() {
        // TODO Auto-generated method stub
        return returnPath;
    }

    public void save() {
        try {
            ResourceBibliographicManager.saveBibliographicData(data);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    //    public List<Author> getAuthorList() {
    //        return authorList;
    //    }
    //    
    //    public void addAuthor()  {
    //        authorList.add(new Author());
    //    }
    //    
    //    public void deleteAuthor()  {
    //        if (authorList.contains(currentAuthor)) {
    //            authorList.remove(currentAuthor);
    //        }
    //    }
    //    
    //    public Author getCurrentAuthor() {
    //        return currentAuthor;
    //    }
    //
    //    public void setCurrentAuthor(Author currentAuthor) {
    //        this.currentAuthor = currentAuthor;
    //    }

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

    public BibliographicData getData() {
        return data;
    }

    public void setData(BibliographicData data) {
        this.data = data;
    }

}
