package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.List;

import de.intranda.digiverso.normdataimporter.model.NormData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Person extends ComplexMetadataObject {

    private String firstName;
    private String lastName;

    private List<String> possibleDatabases = new ArrayList<>();

    public Person() {
        possibleDatabases.add("gnd");
        possibleDatabases.add("eduexperts");
    }
}
