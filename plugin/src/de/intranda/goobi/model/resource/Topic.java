package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.sub.goobi.helper.FacesContextHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Topic {

    private String id;
    
    private String nameDE;

    private String nameEN;
    
    private String display;

    private List<Keyword> keywordList = new ArrayList<>();

    public void addKeyword(Keyword keyword) {
        keywordList.add(keyword);
    }

    public String getName() {
        if (FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
            return nameDE;
        }
        return nameEN;
    }
}
