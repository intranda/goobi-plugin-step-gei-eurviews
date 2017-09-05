package de.intranda.goobi.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.properties.ImportProperty;

import de.intranda.goobi.model.EurViewsRecord;
import de.sub.goobi.forms.MassImportForm;
import de.sub.goobi.helper.exceptions.ImportPluginException;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

public class EurViewsImportPlugin implements IImportPlugin{

    private static final Logger logger = Logger.getLogger(EurViewsImportPlugin.class);
    private static final String TITLE = "EurViewsImport";
    
    private Prefs prefs;    //ruleset for querying database
    private EurViewsRecord record;  //the current record from importFile
    private File importFolder;  //do we need this?
    private File importFile;    //the xml-File containing <digiSource> records
    
    @Override
    public PluginType getType() {
        return PluginType.Import;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }

    @Override
    public void setData(Record r) {
        if(r instanceof EurViewsRecord) {            
            this.record = (EurViewsRecord) r;
        } else {
            throw new IllegalArgumentException("Plugin only accepts record of type EurViewsRecord");
        }
        
    }

    @Override
    public String getImportFolder() {
        return this.importFolder.getAbsolutePath();
    }

    @Override
    public String getProcessTitle() {
        if(record != null) {
            return record.getProcess();
        } else {
            throw new IllegalStateException("Cannot get process title without selected record");
        }
    }
    

    @Override
    public Fileformat convertData() throws ImportPluginException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ImportObject> generateFiles(List<Record> records) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setForm(MassImportForm form) {
    }

    @Override
    public void setImportFolder(String folder) {
        this.importFolder = new File(folder);
    }

    @Override
    public List<Record> splitRecords(String records) {
        return null;
    }

    @Override
    public List<Record> generateRecordsFromFile() {
        if(this.importFile == null) {
            throw new IllegalStateException("Must provide an import file before generating records");
        } else if(!this.importFile.isFile()) {
            throw new IllegalArgumentException("Import file " + this.importFile + " not found");
        }
        
        List<Record> records = new ArrayList<>();
        records.add(new EurViewsRecord());
        return records;
    }

    @Override
    public List<Record> generateRecordsFromFilenames(List<String> filenames) {
        return null;
    }

    @Override
    public void setFile(File importFile) {
        this.importFile = importFile;
        
    }

    @Override
    public List<String> splitIds(String ids) {
        return null;
    }

    @Override
    public List<ImportType> getImportTypes() {
        return Collections.singletonList(ImportType.FILE);
    }

    @Override
    public List<ImportProperty> getProperties() {
        return null;
    }

    @Override
    public List<String> getAllFilenames() {
        return null;
    }

    @Override
    public void deleteFiles(List<String> selectedFilenames) {
    }

    @Override
    public List<? extends DocstructElement> getCurrentDocStructs() {
        return null;
    }

    @Override
    public String deleteDocstruct() {
        return null;
    }

    @Override
    public String addDocstruct() {
        return null;
    }

    @Override
    public List<String> getPossibleDocstructs() {
        return null;
    }

    @Override
    public DocstructElement getDocstruct() {
        return null;
    }

    @Override
    public void setDocstruct(DocstructElement dse) {
    }


}
