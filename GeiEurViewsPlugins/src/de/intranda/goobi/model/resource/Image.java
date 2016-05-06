package de.intranda.goobi.model.resource;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import lombok.Data;
import de.sub.goobi.config.ConfigurationHelper;

public @Data class Image {

    private Integer processId;
    private Integer imageId;
    private String fileName;
    private int order;
    
    private String structType;
    private boolean mainImage = false;
    private List<String> licenceList;
    private String copyright;
    private String resolution;
    private boolean displayImage = false;
    
    private String representative;

    public Image(Integer processId) {
        this.processId = processId;
    }

    

    public String getImagename() {
        /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        String currentImageURL = ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + fileName + ".png";

        return currentImageURL;
    }
}
