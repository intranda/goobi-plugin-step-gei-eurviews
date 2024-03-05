package de.intranda.goobi.model;

import lombok.Data;

@Data
public class Language {

    private String isoCode_639_2;
    private String isoCode_639_1;
    private String englishName;
    private String frenchName;
    private String germanName;
    
    @Deprecated
    public String getIsoCode() {
        return getIsoCode_639_2();
    }
    
    @Deprecated
    public String getIsoCodeOld() {
        return getIsoCode_639_1();
    }
}
