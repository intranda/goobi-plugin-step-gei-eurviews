package de.intranda.goobi.model;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.normdata.EduExpertsDatabase;
import de.intranda.goobi.normdata.GeonamesDatabase;
import de.intranda.goobi.normdata.GndDatabase;
import lombok.Data;

@Data
public class NormdataEntity {

    private final String authority;
    private String uri = "";
    private String id = "";
    
    public NormdataEntity(String authority) {
        this.authority = authority;
    }
    
    public String getAuthorityURL() {
        if(StringUtils.isNotBlank(authority)) {
            switch(authority.toLowerCase()) {
                case "gnd":
                    return GndDatabase.GND_URL;
                case "edu.experts":
                    return EduExpertsDatabase.WVEXPERTS_DATABASE_URL;
                case "geonames":
                    return GeonamesDatabase.GEONAMES_URL;
            }
        }
        return "";
    }
    
    public String getUri() {
        if (StringUtils.isBlank(uri) && StringUtils.isNotBlank(id)) {
            uri = getAuthorityURL() + id;
        }
        return uri;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        if (StringUtils.isBlank(id) && StringUtils.isNotBlank(uri)) {
            id = uri.replace(getAuthorityURL(), "");
        }
        return this.id;
    }
    
    public boolean isEmpty() {
        return StringUtils.isBlank(uri) && StringUtils.isBlank(id);
    }
    
    public boolean hasUrl() {
        return StringUtils.isNotBlank(uri);
    }
}
