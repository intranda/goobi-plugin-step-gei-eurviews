package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    private Integer id = null;
    protected String role;
    protected List<NormdataEntity> normdata = new ArrayList<>();

    //    protected String normdataAuthority;
    //    protected Map<String, String> normdataValues = new HashMap<String, String>();
    //    protected Map<String, String> normdataUris = new HashMap<String, String>();
    public abstract String getName();

    public abstract void setName(String name);

    public NormdataEntity getNormdata(String authority) {
        for (NormdataEntity normdataEntity : normdata) {
            if(normdataEntity.getAuthority().equals(authority)) {
                return normdataEntity;
            }
        }
        NormdataEntity normdata = new NormdataEntity(authority);
        this.normdata.add(normdata);
        return normdata;
    }

    public String getNormdataValue(String authority) {
        return getNormdata(authority).getId();
    }
    
    public String getNormdataValue() {
        return getNormdata("gnd").getId();
    }

    public String getNormdataUri(String authority) {
        return getNormdata(authority).getUri();
    }

    public void resetNormdataValues() {
        normdata = new ArrayList<>();
    }
    
    public void setNormdata(String authority, String id, String uri) {
        NormdataEntity entity = getNormdata(authority);
        entity.setId(id);
        entity.setUri(uri);
    }
    
    public void setNormdataId(String authority, String id) {
        NormdataEntity entity = getNormdata(authority);
        entity.setId(id);
    }
    
    public void setNormdataUri(String authority, String uri) {
        NormdataEntity entity = getNormdata(authority);
        entity.setUri(uri);
    }
    
    public void setNormdataAuthority(String authority) {
        
    }
    
    public String getNormdataAuthority() {
        return "";
    }

    public void setNormdata(List list) {
        resetNormdataValues();
        for (Object o : list) {
                normdata.add((NormdataEntity)o);
        }
    }

    public String getNameForSearch() {
        return getName();
    }
    
    public boolean isPerson() {
        return false;
    }
}
