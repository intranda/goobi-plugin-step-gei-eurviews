package de.intranda.goobi.model.resource;

import java.util.List;

public class Description {

    private Integer prozesseID;
    private Integer descriptionID;
    private String language;
    private String title;
    private String shortDescription;
    private String longDescription;
    private List<String> categoryList;
    private List<String> keywordList;
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

    public List<String> getKeywordList() {
        return keywordList;
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

    public void setKeywordList(List<String> keywordList) {
        this.keywordList = keywordList;
    }

    public List<String> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<String> categoryList) {
        this.categoryList = categoryList;
    }

    public boolean isOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(boolean originalLanguage) {
        this.originalLanguage = originalLanguage;
    }
}
