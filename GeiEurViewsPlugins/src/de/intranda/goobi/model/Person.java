package de.intranda.goobi.model;

import org.apache.commons.lang.StringUtils;

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
    public String getNameForSearch() {
        return getFirstName() + "+" + getLastName();
    }

    @Override
    public void setName(String name) {
        setName(name, true);
    }
    
    public void setName(String name, boolean allowFirstNameFirst) {
       if(!StringUtils.isBlank(name)) {
           int commaIndex = name.lastIndexOf(",");
           if(commaIndex > -1) {
               firstName = name.substring(commaIndex+1).trim();
               lastName = name.substring(0, commaIndex).trim();
           } else if(allowFirstNameFirst) {
               int spaceIndex = name.lastIndexOf(" ");
               if(spaceIndex > -1) {                   
                   firstName = name.substring(spaceIndex+1).trim();
                   lastName = name.substring(0, spaceIndex).trim();
               } else {
                   firstName = "";
                   lastName = name.trim();
               }
           } else {
               firstName = "";
               lastName = name.trim();
           }
       }
        
    }

        @Override
        public boolean isPerson() {
            return true;
        }

}
