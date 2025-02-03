package de.intranda.goobi.model.resource;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

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

        String currentImageURL = ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + getProcessId() + "_" + fileName + ".png";

        return currentImageURL;
    }
}
