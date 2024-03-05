package de.intranda.goobi.model;

public interface ComplexMetadataContainer {

    public void deleteMetadata(ComplexMetadataObject metadata);
    
    public ComplexMetadataObject getCurrentMetadata();
    
    public void setCurrentMetadata(ComplexMetadataObject metadata);
}
