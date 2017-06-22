package de.intranda.goobi.normdata;

import java.util.List;

import de.intranda.digiverso.normdataimporter.model.NormData;

public abstract class NormDatabase {
    
    public static final String GND_URL = "http://d-nb.info/gnd/";
    public static final String EDU_EXPERTS_URL = "http://wvexperts.gei.de/middleware/";
    public static final String GEONAMES_URL = "http://sws.geonames.org/";

    public static NormDatabase get(String database) {
        switch (database.toLowerCase()) {
            case "edu.experts":
                return new EduExpertsDatabase();
            default:
                return new GndDatabase();
        }
    }

    public abstract String getSearchTerm(String searchValue, String searchOption);

    public abstract String getCatalogue(String searchOption);

    /**
     * Optionally filter or remap result values by normdatabase
     * 
     * @param list
     * @return
     */
    public List<List<NormData>> internalMappings(List<List<NormData>> list) {
        return list;
    }

    public abstract String getName();



}
