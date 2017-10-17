package de.intranda.goobi.normdata;

public class GeonamesDatabase extends NormDatabase {

    public static final String GEONAMES_URL = "http://sws.geonames.org/";
    
    @Override
    public String getSearchTerm(String searchValue, String searchOption) {
        return searchValue;
    }

    @Override
    public String getCatalogue(String searchOption) {
        return "";
    }

    @Override
    public String getName() {
        return "geonames";
    }

}
