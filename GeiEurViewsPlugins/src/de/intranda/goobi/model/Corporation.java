package de.intranda.goobi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Corporation extends ComplexMetadataObject {

    private String name;
}
