package de.intranda.goobi.model;

import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    private String role;
    private String normdataAuthority;
    private String normdataValue;
}
