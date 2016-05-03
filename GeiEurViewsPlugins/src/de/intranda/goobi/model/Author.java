package de.intranda.goobi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Author extends ComplexMetadataObject {

    private String firstName;
    private String lastName;
}
