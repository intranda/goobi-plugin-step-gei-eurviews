package de.intranda.goobi.model;

import org.apache.commons.lang.StringUtils;

import lombok.Data;

public @Data class SimpleMetadataObject {

    private String value;
    
    public SimpleMetadataObject (String value) {
        this.value = value;
    }

    public boolean hasValue() {
        return StringUtils.isNotBlank(value);
    }
    
}
