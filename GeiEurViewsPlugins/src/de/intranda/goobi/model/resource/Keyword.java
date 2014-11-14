package de.intranda.goobi.model.resource;

public class Keyword {

    
    private Integer keywordId;
    private String categoryId;
    private String label;
    
    public Integer getKeywordId() {
        return keywordId;
    }
    public String getCategoryId() {
        return categoryId;
    }
    public String getLabel() {
        return label;
    }
    public void setKeywordId(Integer keywordId) {
        this.keywordId = keywordId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    public void setLabel(String label) {
        this.label = label;
    }

}
