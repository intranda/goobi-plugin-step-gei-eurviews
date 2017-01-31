package de.intranda.goobi.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.log4j.Log4j;

@EqualsAndHashCode(callSuper = false)
@Log4j
public @Data class Location extends ComplexMetadataObject {

    private List<Toponym> resultList;
    private Toponym currentToponym;
    private int totalResults;
    private String searchValue;

    private String name;

    public Location() {
    }

    public Location(String role) {
        this.role = role;
    }

    public String search() {
        String credentials = ConfigurationHelper.getInstance().getGeonamesCredentials();
        if (credentials != null) {
            WebService.setUserName(credentials);
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setNameEquals(searchValue);
            searchCriteria.setStyle(Style.FULL);
            try {
                ToponymSearchResult searchResult = WebService.search(searchCriteria);
                resultList = searchResult.getToponyms();
                totalResults = searchResult.getTotalResultsCount();
            } catch (Exception e) {
                log.error(e);
            }

        } else {
            // deaktiviert 
            Helper.setFehlerMeldung("geonamesDeactivated");
        }
        return "";
    }

    public String getData() {
        name = currentToponym.getName();
        normdataAuthority = "geonames";
        normdataValue = "" + currentToponym.getGeoNameId();
        return "";
    }

    public String getUrl() {
        if (StringUtils.isBlank(normdataValue)) {
            return null;
        } else {
            return "http://www.geonames.org/" + normdataValue;
        }
    }
    
    @Override
    public String toString() {
    	return name ;
    }
}
