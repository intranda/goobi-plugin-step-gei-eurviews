package de.intranda.goobi.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;

import de.intranda.goobi.model.process.Page;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.BibliographicMetadataBuilder;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.ResourceMetadataBuilder;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import lombok.Data;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.ContentFile;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Reference;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@PluginImplementation
public @Data class SourceCreationPlugin implements IWorkflowPlugin {

    private static final Logger logger = Logger.getLogger(SourceCreationPlugin.class);
    private static final String PLUGIN_NAME = "Gei_WorldViews_SourceCreation";
    private static final String GUI_PATH = "/Gei_WorldViews_SourceCreationPlugin.xhtml";

    private Process schoolbookProcess = null;
    private Project project = null;
    private List<Page> pages;
    private Page startPage = null;
    private Page endPage = null;

    @Override
    public PluginType getType() {
        return PluginType.Workflow;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    @Override
    public String getGui() {
        return "/" + Helper.getTheme() + GUI_PATH;
    }

    public List<Process> getAllProcesses() {
        String filter = "IstTemplate=false";
        if (this.project != null) {
            filter += " AND prozesse.ProjekteID=" + this.project.getId();
        }
        List<Process> processes = ProcessManager.getProcesses("prozesse.Titel", filter);
        return processes;
    }

    public List<Project> getAllProjects() {
        return ProjectManager.getAllProjects();
    }

    public List<Page> readPages()
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        List<Page> pages = new ArrayList<>();
        if (getSchoolbookProcess() != null) {
            Fileformat ff = getSchoolbookProcess().readMetadataFile();
            List<DocStruct> pageDocs = ff.getDigitalDocument()
                    .getAllDocStructsByType("page");
            if (pageDocs != null) {
                for (DocStruct docStruct : pageDocs) {
                    pages.add(new Page(docStruct));
                }
            }
        }
        Collections.sort(pages);
        return pages;
    }

    public void setSchoolbookProcessId(Integer id)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        if(id != null) {            
            this.schoolbookProcess = ProcessManager.getProcessById(id);
        } else {
            this.schoolbookProcess = null;
        }
        this.pages = readPages();
//        startPage = null;
//        endPage = null;
    }

    public Integer getSchoolbookProcessId() {
        if (this.schoolbookProcess != null) {
            return this.schoolbookProcess.getId();
        } else {
            return null;
        }
    }

    public void setProjectId(Integer id) throws DAOException {
        if(id != null) {            
            this.project = ProjectManager.getProjectById(id);
        } else {
            this.project = null;
        }
        this.schoolbookProcess = null;
        this.pages = null;
//        this.startPage = null;
//        this.endPage = null;
    }

    public Integer getProjectId() {
        if (this.project != null) {
            return this.project.getId();
        } else {
            return null;
        }
    }

    public Integer getStartPageId() {
        if (startPage != null) {
            return startPage.getId();
        } else {
            return null;
        }
    }

    public void setStartPageId(Integer id) {
        for (Page page : pages) {
            if (page.getId()
                    .equals(id)) {
                startPage = page;
            }
        }
        logger.debug("Start page is now " + startPage);
    }

    public Integer getEndPageId() {
        if (endPage != null) {
            return endPage.getId();
        } else {
            return null;
        }
    }

    public void setEndPageId(Integer id) {
        for (Page page : pages) {
            if (page.getId()
                    .equals(id)) {
                endPage = page;
            }
        }
        logger.debug("End page is now " + endPage);
    }

    public List<Page> getPagesAfter(Page page) {
        if (page != null) {
            int index = getPages().indexOf(page);
            if (index > -1) {
                return getPages().subList(index, getPages().size() - 1);
            }
        }
        return getPages();
    }

    public void createData() {
        
        BibliographicMetadata biblData = null;
        Process sourceProcess = null;
        ResouceMetadata sourceData = null;
        try {
            sourceProcess = createSourceProcess(getSourceProcessTitle(), getSourceTemplate(), getSchoolbookProcess());
            
            biblData = getBibliographicData(getSchoolbookProcess());
            sourceData = getResourceData(sourceProcess, biblData);
            sourceData.setDigitalCollections(getDigitalCollectionsForSource());

            List<Page> imagePaths = getImageFilesToCopy();
            List<Page> copiedPaths = copyImageFiles(imagePaths, sourceProcess);
            List<Image> images = createImages(copiedPaths, sourceProcess);

            WorldViewsDatabaseManager.saveBibliographicData(biblData);
            WorldViewsDatabaseManager.saveResouceMetadata(sourceData);
            WorldViewsDatabaseManager.deleteImages(sourceData);
            WorldViewsDatabaseManager.saveImages(images);
            
            Helper.setMeldung(Helper.getTranslation("success_creating_data", sourceProcess.getTitel(), sourceProcess.getId().toString()));
            
        } catch (Throwable e) {
            logger.error("Error creating data", e);
            Helper.setFehlerMeldung(Helper.getTranslation("error_creating_data", e.toString()));
            if(sourceProcess != null) {
                ProcessManager.deleteProcess(sourceProcess);
            }
        }
    }

    private List<Image> createImages(List<Page> pages, Process sourceProcess) {

        List<Image> images = new ArrayList<>();
        int order = 0;
        for (Page page : pages) {
            String type = page.getDocType();
            Image image = new Image(sourceProcess.getId());
            image.setOrder(order + 1);
            image.setFileName(page.getFilePath()
                    .toString());
            image.setStructType(getStructType(type));
            images.add(image);
            order++;
        }
        return images;
    }

    private static String getStructType(String type) {
        switch (type) {
            case "TitlePage":
                return "Titel";
            case "TableOfContents":
                return "Inhaltsverzeichnis";
            case "Imprint":
                return "Impressum";
            case "Preface":
                return "Vorwort";
            default:
                return "Quelle";
        }
    }

    /**
     * 
     * @param imagePaths
     * @param sourceProcess
     * @return
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     * @throws IOException  If the target directory could not be created or any image could not be copied. Any already copied files are removed
     */
    private List<Page> copyImageFiles(List<Page> imagePaths, Process sourceProcess)
            throws InterruptedException, SwapException, DAOException, IOException {
        Path masterFolder = Paths.get(sourceProcess.getImagesOrigDirectory(false));
        if (!Files.isDirectory(masterFolder)) {
            Files.createDirectory(masterFolder);
        }
        List<Page> targetPaths = new ArrayList<>();
        for (Page image : imagePaths) {
            if (image.getFilePath() == null) {
                throw new IllegalStateException("Pages must have filenames before they can be copied");
            }
            Path targetPath = masterFolder.resolve(image.getFilePath()
                    .getFileName());
            try {                
                Files.copy(image.getFilePath(), targetPath);
            } catch(IOException e) {
                //revert all copied files
                for (Page page : targetPaths) {
                    Files.delete(page.getFilePath());
                }
                throw new IOException("Error copying file " + image.getFilePath() + " to " + targetPath, e);
            }
            targetPaths.add(new Page(image, targetPath.getFileName()));
        }
        return targetPaths;
    }

    private List<Page> getImageFilesToCopy() {
        int startPageIndex = getPages().indexOf(getStartPage());
        int endPageIndex = getPages().indexOf(getEndPage());
        List<Page> pagesOfInterest = new ArrayList<>(getPages().subList(Math.max(0, startPageIndex), Math.max(0, Math.min(endPageIndex + 1, getPages().size()))));
        
        List<String> additionalFileDocStructTypes = getAdditionalFileDocStructTypes();   
        Iterator<Page> pages = getPages().iterator();
        while(pages.hasNext()) {
            Page page = pages.next();
            if(!pagesOfInterest.contains(page)) {
                List<Reference> logicalReferences = page.getDs().getAllFromReferences();
                for (Reference reference : logicalReferences) {
                    if(additionalFileDocStructTypes.contains(reference.getSource().getType().getName())) {
                        page.setDocType(reference.getSource().getType().getName());
                        pagesOfInterest.add(page);
                    }
                }
            }
        }
        
        Collections.sort(pagesOfInterest);
        return pagesOfInterest;
    }


    private XMLConfiguration getConfig() {
        return ConfigPlugins.getPluginConfig(this);
    }
    
    private List<String> getAdditionalFileDocStructTypes() {
        return getConfig().getList("images.docStructType", Collections.EMPTY_LIST);
    }

    private String getSourceTemplate() {
        return getConfig().getString("source.template", "GEI_WorldViews_Resources");
    }

    private List<String> getDigitalCollectionsForSource() {
        return getConfig().getList("source.digitalCollection", Collections.singletonList("WorldViews"));
    }
    

    private List<String> getUserGroups() {
        return getConfig().getList("schoolbook.bibDataStep.userGroup", Collections.EMPTY_LIST);
//        List<Integer> ids = new ArrayList<>();
//        for (String idString : idList) {
//            try {
//                ids.add(Integer.parseInt(idString));
//            } catch(NullPointerException | NumberFormatException e) {
//                logger.error("Unable to parse config setting at 'schoolbook.bibDataStep.userGroup': " + idString);
//            }
//        }
//        return ids;
    }
    

    private String getSchoolbookEditStepTitle() {
        return getConfig().getString("schoolbook.bibDataStep.title", "Als Schulbuch erfassen");
    }


    private String getSourceProcessTitle() {
        return getSchoolbookProcess().getTitel() + "_" + getStartPage().getOrder() + "_" + getEndPage().getOrder();
    }

    private Process createSourceProcess(String title, String templateName, Process SchoolbookProcess) {

        BeanHelper bhelp = new BeanHelper();
        Process template = ProcessManager.getProcessByTitle(templateName);

        Process newProcess = new Process();
        newProcess.setTitel(title);
        newProcess.setIstTemplate(false);
        newProcess.setInAuswahllisteAnzeigen(false);
        newProcess.setProjekt(template.getProjekt());
        newProcess.setRegelsatz(template.getRegelsatz());
        newProcess.setDocket(template.getDocket());

        bhelp.SchritteKopieren(template, newProcess);
        bhelp.ScanvorlagenKopieren(template, newProcess);
        bhelp.WerkstueckeKopieren(template, newProcess);
        bhelp.EigenschaftenKopieren(template, newProcess);

        try {
            ProcessManager.saveProcess(newProcess);
        } catch (DAOException e) {
            logger.error(e);
        }

        {
            Processproperty pp = new Processproperty();
            pp.setTitel("Schulbuch");
            pp.setWert(schoolbookProcess.getId() + "");
            pp.setProzess(newProcess);
            PropertyManager.saveProcessProperty(pp);
        }

        {
            Processproperty pp = new Processproperty();
            pp.setTitel("Template");
            pp.setWert(template.getTitel());
            pp.setProzess(newProcess);
            PropertyManager.saveProcessProperty(pp);
        }

        {
            Processproperty pp = new Processproperty();
            pp.setTitel("TemplateID");
            pp.setWert(template.getId() + "");
            pp.setProzess(newProcess);
            PropertyManager.saveProcessProperty(pp);
        }

        return newProcess;

    }

    public BibliographicMetadata getBibliographicData(Process bookProcess) throws SQLException, DAOException {
        BibliographicMetadata biblData = null;
        try {
            biblData = WorldViewsDatabaseManager.getBibliographicData(bookProcess.getId());
        } catch (Throwable e) {
            logger.error(e);
        }
        if (biblData == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new bibliographic record");
            }
            biblData = BibliographicMetadataBuilder.build(bookProcess, null);
            addBibDataPluginStep(bookProcess);
        }
        return biblData;
    }

    public ResouceMetadata getResourceData(Process sourceProcess, BibliographicMetadata bibData) throws SQLException {
        ResouceMetadata data = null;
        try {
            data = WorldViewsDatabaseManager.getResourceMetadata(sourceProcess.getId());
        } catch (Throwable e) {
            logger.error(e);
        }
        if (data == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new resource record");
            }
            data = ResourceMetadataBuilder.build(sourceProcess, null, bibData);
        } else {
            ResourceMetadataBuilder.resetData(data);
            ResourceMetadataBuilder.init(data, null, bibData);
        }
        return data;
    }

    private void addBibDataPluginStep(Process bookProcess) throws DAOException {
        String stepTitle = getSchoolbookEditStepTitle();
        
        for(Step step : bookProcess.getSchritte()) {
            if(step.getTitel().equals(stepTitle) || BibliographicDataPlugin.PLUGIN_NAME.equals(step.getStepPlugin())) {
                return;
            }
        }
        
        int lastStepOrder = getLastStepOrder(bookProcess);
        Step step = new Step();
        step.setTitel(stepTitle);
        step.setReihenfolge(lastStepOrder + 1);
        List<String> userGroupNames = getUserGroups();
        String filter = "titel = '" + StringUtils.join(userGroupNames, "' OR titel = '") + "'";
        List<Usergroup> userGroups = UsergroupManager.getUsergroups(null, filter, null, null);
        step.getBenutzergruppen().addAll(userGroups);
        step.setStepPlugin(BibliographicDataPlugin.PLUGIN_NAME);
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        step.setProzess(bookProcess);
        bookProcess.getSchritte()
                .add(step);
        ProcessManager.saveProcess(bookProcess);

    }

    /**
     * @param bookProcess
     * @return
     */
    public int getLastStepOrder(Process bookProcess) {
        List<Step> schritte = bookProcess.getSchritte();
        int lastStepOrder = 0;
        for (Step step : schritte) {
            lastStepOrder = Math.max(lastStepOrder, step.getReihenfolge());
        }
        return lastStepOrder;
    }
    
    public boolean isDataValid() {
        return getSchoolbookProcess() != null && getStartPage() != null && getEndPage() != null && getStartPage().getOrder() <= getEndPage().getOrder();
    }
}
