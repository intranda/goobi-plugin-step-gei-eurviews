package de.intranda.goobi.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    protected String role;
    protected String normdataAuthority;
    protected Map<String, String> normdataValues = new HashMap<String, String>();
    public abstract String getName();
    public abstract void setName(String name);

    public String getNormdataValue() {
        return normdataValues.get(normdataAuthority);
    }
    
    public void setNormdataValue(String value) {
        this.normdataValues.put(normdataAuthority, value);
    }
    

    public void resetNormdataValues() {
        normdataValues = new HashMap<String, String>();
    }
    
}
