package de.intranda.goobi.model.resource;

public class Category {

    private Integer descriptionId;
    private String categoryId;
    private String label;
    
    public Integer getDescriptionId() {
        return descriptionId;
    }
    public String getCategoryId() {
        return categoryId;
    }
    public String getLabel() {
        return label;
    }
    public void setDescriptionId(Integer descriptionId) {
        this.descriptionId = descriptionId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    public void setLabel(String label) {
        this.label = label;
    }

}
