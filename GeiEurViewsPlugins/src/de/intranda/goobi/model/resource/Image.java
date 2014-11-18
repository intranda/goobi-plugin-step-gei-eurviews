package de.intranda.goobi.model.resource;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.sub.goobi.config.ConfigurationHelper;

public class Image {


    private Integer processId;
    private Integer imageId;
    private String fileName;
    private int order;
    private String structType;
    private boolean displayImage = false;
    private String licence;
    private boolean representative = false;

    public Image(Integer processId) {
        this.processId = processId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getOrder() {
        return order;
    }

    public String getStructType() {
        return structType;
    }

    public boolean isDisplayImage() {
        return displayImage;
    }

    public String getLicence() {
        return licence;
    }

    public boolean isRepresentative() {
        return representative;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setStructType(String structType) {
        this.structType = structType;
    }

    public void setDisplayImage(boolean displayImage) {
        this.displayImage = displayImage;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    public void setRepresentative(boolean representative) {
        this.representative = representative;
    }

    public String getImagename() {
        /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        String currentImageURL = ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + fileName + ".png";

        return currentImageURL;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
}
