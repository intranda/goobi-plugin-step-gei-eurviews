package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import lombok.Data;

@Data
public class GeonamesLocale {
    
    String language;
    String officialName;
    List<String> alternateNames = new ArrayList<>();
    
    public GeonamesLocale(String language) {
        this.language = language;
    }

    public GeonamesLocale(String language, String officialName) {
        this.language = language;
        this.officialName = officialName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(language);
        if(StringUtils.isNotBlank(officialName)) {            
            sb.append(": ").append(officialName);
        }
        if(!alternateNames.isEmpty()) {
            sb.append(" (").append(StringUtils.join(alternateNames, ", ")).append(")");
        }
        return sb.toString();
    }
    
    public boolean hasName() {
        return StringUtils.isNotBlank(this.officialName) || !alternateNames.isEmpty();
    }
    
    public String getOfficialName(boolean useFallback) {
        if(StringUtils.isNotBlank(officialName)) {
            return officialName;
        } else if(!alternateNames.isEmpty()) {
            return alternateNames.get(0);
        } else {
            return null;
        }
    }
}
