package de.intranda.goobi.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.normdata.NormData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class Person extends ComplexMetadataObject {

    private String firstName;
    private String lastName;

    public Person() {
    }

    @Override
    public String getName() {
        return lastName + ", " + firstName;
    }

    @Override
    public void setName(String name) {
       if(!StringUtils.isBlank(name)) {
           int commaIndex = name.lastIndexOf(",");
           if(commaIndex > -1) {
               firstName = name.substring(commaIndex+1).trim();
               lastName = name.substring(0, commaIndex).trim();
           } else {
               int spaceIndex = name.lastIndexOf(" ");
               if(spaceIndex > -1) {                   
                   firstName = name.substring(spaceIndex+1).trim();
                   lastName = name.substring(0, spaceIndex).trim();
               } else {
                   firstName = "";
                   lastName = name.trim();
               }
           }
       }
        
    }



}
