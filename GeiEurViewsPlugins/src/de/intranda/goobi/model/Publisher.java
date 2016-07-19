package de.intranda.goobi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Publisher extends ComplexMetadataObject {

    private String name;
}
