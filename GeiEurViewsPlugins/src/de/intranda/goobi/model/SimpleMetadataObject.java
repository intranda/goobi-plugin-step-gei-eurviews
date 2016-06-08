package de.intranda.goobi.model;

import lombok.Data;

public @Data class SimpleMetadataObject {

    private String value;
    
    public SimpleMetadataObject (String value) {
        this.value = value;
    }
    
}
