package de.intranda.goobi.model.resource;

import java.util.LinkedList;
import java.util.List;

public class KeywordCategory {

    private String categoryName;

    private List<KeywordEntry> keywordList = new LinkedList<KeywordEntry>();

    public List<KeywordEntry> getKeywordList() {
        return keywordList;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setKeywordList(List<KeywordEntry> keywordList) {
        this.keywordList = keywordList;
    }

    public void setCategoryName(String name) {
        this.categoryName = name;
    }

    public void addKeyword(String keyword) {
        KeywordEntry sub = new KeywordEntry();
        sub.setKeyword(keyword);
        keywordList.add(sub);
    }
}
