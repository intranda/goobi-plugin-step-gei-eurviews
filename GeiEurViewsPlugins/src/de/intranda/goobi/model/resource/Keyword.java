package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.sub.goobi.helper.FacesContextHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Keyword {

    private String keywordNameDE;

    private String keywordNameEN;

    private String gndId;

    private String wvId;

    private List<String> synonymListDE = new ArrayList<>();

    private List<String> synonymListEN = new ArrayList<>();

    private boolean selected = false;

    public String getKeywordName() {
        if (FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
            return keywordNameDE;
        }
        return keywordNameEN;
    }

    //    public List<String> getSynonymList() {
    //        if (FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
    //            return synonymListDE;
    //        }
    //        return synonymListEN;
    //    }

    public String getSynonymList() {
        StringBuilder sb = new StringBuilder();
        if (FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
            for (String string : synonymListDE) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(string);
            }
        } else {
            for (String string : synonymListEN) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(string);
            }
        }

        return sb.toString();
    }

}
