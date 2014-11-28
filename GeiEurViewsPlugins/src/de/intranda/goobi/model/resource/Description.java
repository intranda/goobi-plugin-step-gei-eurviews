package de.intranda.goobi.model.resource;

public class Description {

    private Integer prozesseID;
    private Integer descriptionID;
    private String language;
    private String title;
    private String shortDescription;
    private String longDescription;
    private boolean originalLanguage = false;

    public Description(Integer processID) {
        this.prozesseID = processID;
    }

    public Integer getDescriptionID() {
        return descriptionID;
    }

    public Integer getProcessID() {
        return prozesseID;
    }

    public String getLanguage() {
        return language;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setDescriptionID(Integer descriptionID) {
        this.descriptionID = descriptionID;
    }

    public void setProcessID(Integer processID) {
        this.prozesseID = processID;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public boolean isOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(boolean originalLanguage) {
        this.originalLanguage = originalLanguage;
    }
}
