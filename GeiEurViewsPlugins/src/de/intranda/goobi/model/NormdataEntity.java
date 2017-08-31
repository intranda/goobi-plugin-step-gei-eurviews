package de.intranda.goobi.model;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.normdata.NormDatabase;
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
                    return NormDatabase.GND_URL;
                case "edu.experts":
                    return NormDatabase.EDU_EXPERTS_URL;
                case "geonames":
                    return NormDatabase.GEONAMES_URL;
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
        return this.id;
    }
    
    public boolean isEmpty() {
        return StringUtils.isBlank(uri) && StringUtils.isBlank(id);
    }
    
    public boolean hasUrl() {
        return StringUtils.isNotBlank(uri);
    }
}
