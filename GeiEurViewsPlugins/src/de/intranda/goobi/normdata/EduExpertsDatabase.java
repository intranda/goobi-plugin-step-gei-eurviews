package de.intranda.goobi.normdata;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;

public class EduExpertsDatabase extends NormDatabase {

    public static final String INPUT_IDENTIFIER = "NORM_IDENTIFIER_ZLB";
    public static final String OUTPUT_IDENTIFIER = "NORM_IDENTIFIER_EDU_EXPERTS";
    
    public static final String INPUT_URI = "URI";
    public static final String OUTPUT_URI = "URI_EDU_EXPERTS";

    @Override
    public String getSearchTerm(String searchValue, String searchOption) {
        return searchValue.replace(" ", "_");
    }

    @Override
    public String getCatalogue(String searchOption) {
        String catalog = "wvexpertsid";
        if (StringUtils.isNotBlank(searchOption)) {
            switch (searchOption) {
                case "Tp*":
                    catalog = "expert";
                    break;
                case "Tb*":
                    catalog = "corporatebody";
                    break;
                default:
                    catalog = searchOption;
            }
        }
        return catalog;
    }

    @Override
    public List internalMappings(List<List<NormData>> list) {
        if (list != null) {
            for (List<NormData> ndList : list) {
                if (ndList != null) {
                    for (NormData normData : ndList) {
                        if (normData != null) {
                            if (normData.getKey().equals(INPUT_IDENTIFIER)) {
                                normData.setKey(OUTPUT_IDENTIFIER);
                            }
                            if (normData.getKey().equals(OUTPUT_URI)) {
                                for (NormDataValue value : normData.getValues()) {
                                    if(StringUtils.isBlank(value.getUrl()) && value.getText().startsWith("http")) {
                                        value.setUrl(value.getText());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String getName() {
        return "edu.experts";
    }

}
