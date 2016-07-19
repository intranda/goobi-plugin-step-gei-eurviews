package de.intranda.goobi.model;

import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    private String role;
    protected String normdataAuthority;
    protected String normdataValue;

    private String searchValue;
    private String searchOption;
}
