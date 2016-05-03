package de.intranda.goobi.model;

import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    private Integer id;

    private String role;
    private String normdataAuthority;
    private String normdataValue;
}
