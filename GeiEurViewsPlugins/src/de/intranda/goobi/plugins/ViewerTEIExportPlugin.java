package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.intranda.goobi.model.annotation.AnnotationMetadata;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.util.Filters;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@PluginImplementation
public class ViewerTEIExportPlugin implements IExportPlugin {

    private static final Logger logger = Logger.getLogger(ViewerTEIExportPlugin.class);
    private static final String TITLE = "Gei_WorldViews_ViewerExport";

    private String destination = ConfigPlugins.getPluginConfig(this).getString("targetFolder", "/opt/digiverso/viewer/hotfolder");

    private boolean exportOCR = true;
    private boolean exportImages = true;

    private List<String> problems = new ArrayList<String>();

    @Override
    public PluginType getType() {
        return PluginType.Export;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean startExport(Process process) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
            WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
            TypeNotAllowedForParentException {
        return startExport(process, this.destination);
    }

    @Override
    public boolean startExport(Process process, String destination) throws IOException, InterruptedException, DocStructHasNoTypeException,
            PreferencesException, WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
            SwapException, DAOException, TypeNotAllowedForParentException {
        Path destPath = Paths.get(destination);
        if (!destPath.toFile().isDirectory()) {
            reportProblem("Destination path does not exist: " + destination);
            return false;
        }
        Path exportFilePath = destPath.resolve(process.getTitel() + ".xml");
        Path exportTeiPath = destPath.resolve(process.getTitel() + "_tei");
        Path exportImagesPath = destPath.resolve(process.getTitel() + "_tif");

        Path sourceTeiPath = Paths.get(process.getExportDirectory(), process.getTitel() + "_tei");
        if ((!sourceTeiPath.toFile().isDirectory() || sourceTeiPath.toFile().listFiles(Filters.XmlFilter).length == 0) && exportOCR) {
            reportProblem("No TEI files found in " + sourceTeiPath);
            return false;
        }

        try {
            Document exportDoc = null;
            ResouceMetadata resourceMetadata = WorldViewsDatabaseManager.getResourceMetadata(process.getId());
            AnnotationMetadata annotationMetadata = WorldViewsDatabaseManager.getContributionDescription(process.getId());
            if (resourceMetadata != null) {
                //resource export
                List<Image> images = WorldViewsDatabaseManager.getImages(process.getId());
                BibliographicMetadata bibData = WorldViewsDatabaseManager.getBibliographicData(resourceMetadata.getBibliographicDataId());
                if (bibData == null) {
                    reportProblem("Not bibliographic data found for resource process " + resourceMetadata.getProcessId());
                    return false;
                }
                exportDoc = createResourceDocument(resourceMetadata, bibData, images);
            } else if (annotationMetadata != null) {
                //Annotation export
                List<Source> sources = WorldViewsDatabaseManager.getSourceList(process.getId());
                exportDoc = createAnnotationDocument(annotationMetadata, sources);
            } else {
                reportProblem("No WorldViews dataset associated with process " + process.getId());
                return false;
            }
            
            try {                
                writeDocument(exportDoc, exportFilePath);
                if (exportOCR && sourceTeiPath.toFile().isDirectory() && sourceTeiPath.toFile().list().length > 0) {
                    copyTEI(sourceTeiPath, exportTeiPath);
                }
                Path sourceImagesPath = Paths.get(process.getImagesTifDirectory(false));
                if (exportImages && sourceImagesPath.toFile().isDirectory() && sourceImagesPath.toFile().list().length > 0) {
                    copyImages(sourceImagesPath, exportImagesPath);
                }
                return true;
            } catch(IOException e) {
                reportProblem("Error creating export files: " + e.getMessage());
                if(exportFilePath.toFile().isFile()) {
                    exportFilePath.toFile().delete();
                }
                if(exportTeiPath.toFile().isDirectory()) {
                    FileUtils.deleteDirectory(exportTeiPath.toFile());
                }
                if(exportImagesPath.toFile().isDirectory()) {
                    FileUtils.deleteDirectory(exportImagesPath.toFile());
                }
                return false;
            }

        } catch (SQLException e) {
            reportProblem("Error accessing database: " + e.getMessage());
            return false;
        }

    }

    private void copyTEI(Path sourceTeiPath, Path exportTeiPath) throws IOException {
        if(!exportTeiPath.toFile().isDirectory() && !exportTeiPath.toFile().mkdir()) {
            throw new IOException("Unable to create directory " + exportTeiPath);
        }
        File[] teiFiles = sourceTeiPath.toFile().listFiles(Filters.XmlFilter);
        for (File file : teiFiles) {
            Files.copy(Paths.get(file.getAbsolutePath()), exportTeiPath.resolve(file.getName()));
        }   
    }
    
    private void copyImages(Path sourceImagePath, Path exportImagePath) throws IOException {
        if(!exportImagePath.toFile().isDirectory() && !exportImagePath.toFile().mkdir()) {
            throw new IOException("Unable to create directory " + exportImagePath);
        }
        File[] imageFiles = sourceImagePath.toFile().listFiles(Filters.ImageFilter);
        for (File file : imageFiles) {
            Files.copy(Paths.get(file.getAbsolutePath()), exportImagePath.resolve(file.getName()));
        }   
    }

    private void writeDocument(Document exportDoc, Path exportFilePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(exportFilePath.toFile())) {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(exportDoc, fileWriter);
        }
    }

    private Document createAnnotationDocument(AnnotationMetadata annotationMetadata, List<Source> sourceList) {
        Document doc = new Document();
        Element root = new Element("worldviews");
        doc.setRootElement(root);

        Element annotation = new Element("annotation");
        root.addContent(annotation);

        Element type = new Element("docType");
        if(annotationMetadata.getContributionType().equals("Bildungsgeschichte")) {
            type.setText("FormationHistory");
        } else {
            type.setText("Comment");
        }
        annotation.addContent(type);
        
        Element identifier = new Element("identifier");
        identifier.setText(ProcessManager.getProcessTitle(annotationMetadata.getProcessId()));
        annotation.addContent(identifier);

        for (Source source : sourceList) {
            Element relatedItem = new Element("relatedItem");
            if (source.isMainSource()) {
                relatedItem.setAttribute("type", "primarySource");
            } else {
                relatedItem.setAttribute("type", "secondarySource");
            }
            Element relatedIdentifier = new Element("identifier");
            relatedIdentifier.setText(ProcessManager.getProcessTitle(source.getData().getProcessId()));
            relatedItem.addContent(relatedIdentifier);
            annotation.addContent(relatedItem);
        }

        return doc;
    }

    private Document createResourceDocument(ResouceMetadata resourceMetadata, BibliographicMetadata bibData, List<Image> imageList) {
        Document doc = new Document();
        Element root = new Element("worldviews");
        doc.setRootElement(root);

        Element resource = new Element("resource");
        root.addContent(resource);
        
        resource.addContent(createElement("docType", "Source"));

        Element identifier = new Element("identifier");
        identifier.setText(ProcessManager.getProcessTitle(resourceMetadata.getProcessId()));
        resource.addContent(identifier);

        Element relatedItem = new Element("relatedItem");
        relatedItem.setAttribute("type", "schoolbook");
        resource.addContent(relatedItem);

        Element relatedIdentifier = new Element("identifier");
        relatedIdentifier.setText(ProcessManager.getProcessTitle(bibData.getProzesseID()));
        relatedItem.addContent(relatedIdentifier);

        for (String dc : resourceMetadata.getDigitalCollections()) {
            Element collection = new Element("collection");
            collection.setText(dc);
            resource.addContent(collection);
        }

        Element images = new Element("images");
        resource.addContent(images);
        for (Image digiImage : imageList) {
            Element image = new Element("image");
            image.addContent(createElement("fileName", digiImage.getFileName()));
            image.addContent(createElement("sequence", Integer.toString(digiImage.getOrder())));
            image.addContent(createElement("structType", digiImage.getStructType()));
            image.addContent(createElement("displayImage", Boolean.toString(digiImage.isDisplayImage())));
            image.addContent(createElement("licence", digiImage.getLicence()));
            image.addContent(createElement("representative", Boolean.toString(digiImage.isRepresentative())));
            image.addContent(createElement("copyright", digiImage.getCopyright()));
            image.addContent(createElement("placeholder", digiImage.getPlaceholder()));
            images.addContent(image);
        }

        return doc;
    }

    private Element createElement(String name, String text) {
        Element ele = new Element(name);
        ele.setText(text);
        return ele;
    }

    private void reportProblem(String string) {
        this.problems.add(string);
    }

    @Override
    public void setExportFulltext(boolean exportFulltext) {
        this.exportOCR = exportFulltext;

    }

    @Override
    public void setExportImages(boolean exportImages) {
        this.exportImages = exportImages;

    }

    @Override
    public List<String> getProblems() {
        return problems;
    }

}
