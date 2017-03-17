package de.intranda.goobi.model;

import lombok.Data;

public abstract @Data class ComplexMetadataObject {

    protected String role;
    protected String normdataAuthority;
    protected String normdataValue;

}
