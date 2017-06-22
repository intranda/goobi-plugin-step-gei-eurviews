package de.intranda.goobi.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.normdata.NormData;
import de.intranda.goobi.normdata.NormDatabase;
import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    private Integer id = null;
    protected String role;
    protected String normdataAuthority;
    protected Map<String, String> normdataValues = new HashMap<String, String>();
    protected Map<String, String> normdataUris = new HashMap<String, String>();
    public abstract String getName();
    public abstract void setName(String name);
    
    public void setNormdataAuthority(String authority) {
        this.normdataAuthority = authority;
    }

    public String getNormdataValue() {
        return getNormdataValue(getNormdataAuthority());
    }
    
    public String getNormdataValue(String authority) {
        String value = normdataValues.get(authority);
        if(StringUtils.isBlank(value) && StringUtils.isNotBlank(normdataUris.get(authority))) {
            value = getNormdataUri(authority).replace(getAuthorityURL(authority), "");
        }
        return value;
    }
    
    public void setNormdataValue(String value) {
        this.normdataValues.put(normdataAuthority, value);
    }
    
    public String getNormdataUri() {
        return getNormdataUri(getNormdataAuthority());
    }
    
    public String getNormdataUri(String authority) {
        String uri = normdataUris.get(authority);
        if(StringUtils.isBlank(uri) && StringUtils.isNotBlank(normdataValues.get(authority))) {
            uri = getAuthorityURL(authority) + getNormdataValue(authority);
        }
        return uri;
    }
    
    private String getAuthorityURL(String authority) {
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
    public void setNormdataUri(String value) {
        this.normdataUris.put(normdataAuthority, value);
    }
    

    public void resetNormdataValues() {
        normdataValues = new HashMap<String, String>();
        normdataUris = new HashMap<String, String>();
    }

    public void setNormdata(List<NormData> normData) {
        for (NormData nd : normData) {
            if(StringUtils.isBlank(getNormdataAuthority())) {
                setNormdataAuthority(nd.getAuthority());
            }
            normdataValues.put(nd.getAuthority(), nd.getValue());
            normdataUris.put(nd.getAuthority(), nd.getURI());
        }
        
    }
}
