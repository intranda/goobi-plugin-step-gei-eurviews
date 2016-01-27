package de.intranda.goobi.model.resource;

public class KeywordEntry {

    
    private String keyword;
    private boolean selected = false;
    
    
    public String getKeyword() {
        return keyword;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
}
