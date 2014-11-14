package de.intranda.goobi.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.persistence.ResourceBibliographicManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibImageException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;

@PluginImplementation
public class ResourceDescriptionPlugin implements IStepPlugin, IPlugin {

    private static final Logger logger = Logger.getLogger(ResourceDescriptionPlugin.class);

    private Step step;
    private Process process;
    private String returnPath = "/ui/task_edit.xhtml";
    private static final String PLUGIN_NAME = "ResourceDescription";
    private static final String GUI_PATH = "/ui/ResourceDescriptionPlugin.xhtml";
    private int imageSizeInPixel = 200;

    private String imageFolder;
    
    private List<String> possibleDocStructs;
    private List<String> possibleImageDocStructs;
    private List<String> possibleLicences;
    
    private List<Image> currentImages;

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

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        this.process = step.getProzess();
        // import bibliographic data
        try {
            data = ResourceBibliographicManager.getBibliographicData(process.getId());
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
        possibleDocStructs = ConfigPlugins.getPluginConfig(this).getList("elements.docstruct");
        possibleImageDocStructs = ConfigPlugins.getPluginConfig(this).getList("images.docstruct");
        possibleLicences = ConfigPlugins.getPluginConfig(this).getList("licences.licence");
       
        try {
            imageFolder = process.getImagesTifDirectory(true);
       
        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            logger.error(e);
        }

        try {
            currentImages = ResourceBibliographicManager.getImages(process.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        if ( currentImages == null || currentImages.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new image set");
            }
            currentImages = new ArrayList<Image>();
            String[] imageNameArray = new File(imageFolder).list();
            List<String> imageNameList = Arrays.asList(imageNameArray);
            Collections.sort(imageNameList);
            int order = 1;
            for (String imagename : imageNameList) {
                Image currentImage = new Image(process.getId());
                currentImage.setFileName(imagename);
                currentImage.setOrder(order++);
                currentImages.add(currentImage);
            }
        }
        

        // create thumbnail images
        for (Image currentImage : currentImages) {
            createImage(currentImage.getFileName());
        }
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public String cancel() {
        return returnPath;
    }

    @Override
    public String finish() {
        return returnPath;
    }

    public void save() {
        try {
            ResourceBibliographicManager.saveBibliographicData(data);
            ResourceBibliographicManager.saveImages(currentImages);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    private void createImage(String fileName) {

        //                    /* Pages-Verzeichnis ermitteln */
        String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

        //                    /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String mySession = session.getId() + "_" + fileName + ".png";

        try {
            scaleFile(imageFolder + fileName, myPfad + mySession);
        } catch (ContentLibImageException | IOException e) {
            logger.error(e);
        }

    }

    private void scaleFile(String inFileName, String outFileName) throws IOException, ContentLibImageException {

        ImageManager im = new ImageManager(new File(inFileName).toURI().toURL());
        Dimension dim = new Dimension();
        dim.setSize(imageSizeInPixel, imageSizeInPixel);
        RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
        JpegInterpreter pi = new JpegInterpreter(ri);
        FileOutputStream outputFileStream = new FileOutputStream(outFileName);
        pi.writeToStream(null, outputFileStream);
        outputFileStream.close();

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

    public BibliographicData getData() {
        return data;
    }

    public void setData(BibliographicData data) {
        this.data = data;
    }

    public List<String> getPossibleDocumentTypes() {

        return possibleDocStructs;
    }

    
    public List<Image> getCurrentImages() {
        return currentImages;
    }
    
    
    public void setCurrentImages(List<Image> currentImages) {
        this.currentImages = currentImages;
    }
    
    public List<String> getPossibleImageDocStructs() {
        return possibleImageDocStructs;
    }
    
    
    public List<String> getPossibleLicences() {
        return possibleLicences;
    }
}
