package de.intranda.goobi.model.resource;

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
    private boolean representative;
//    private String licence = "alle Rechte beim Verlag";
    private String licence = "";

    private String copyright;
    private boolean displayImage = false;
    float scale = 1f;

    private String placeholder;

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
