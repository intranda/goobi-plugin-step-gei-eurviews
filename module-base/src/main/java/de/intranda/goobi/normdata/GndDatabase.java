package de.intranda.goobi.normdata;

import org.apache.commons.lang.StringUtils;

public class GndDatabase extends NormDatabase {
    
    public static final String GND_URI_FIELD = "URI";
    public static final String GND_URL = "http://d-nb.info/gnd/";
    
    @Override
    public String getSearchTerm(String searchValue, String searchOption) {
        return searchValue;
//        if (StringUtils.isBlank(searchOption)) {
//            return searchValue;
//        } else {
//            return searchValue + " and BBG=" + searchOption;
//        }

    }

    @Override
    public String getCatalogue(String searchOption) {
        String catalog = "idn";
        if (StringUtils.isNotBlank(searchOption)) {
            switch (searchOption) {
                case "Tp*":
                    catalog = "per";
                    break;
                case "Tb*":
                    catalog = "koe";
                    break;
                case "Ts*":
                    catalog = "sw";
                    break;
                case "Tg*":
                    catalog = "geo";
                default:
                    catalog = "woe";
            }
        }
        return catalog;
    }

    @Override
    public String getName() {
        return "gnd";
    }

}
