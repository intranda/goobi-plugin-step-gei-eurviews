package de.intranda.goobi.normdata;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.geonames.FeatureClass;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.Language;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Log4j
public class NormdataSearch {

    public static final String INTRANDA_NORMDATA_SERVICE_URL = "http://normdata.intranda.com/normdata/";
    private String searchOption;
    private String searchValue;

    private int totalResults;
    protected List<List<NormData>> dataList;
    private List<Toponym> resultList;
    private List<Language> searchedLanguages;

    private XMLConfiguration config;

    private String createRecordResult;
    private boolean createRecordSuccess = true;

    public NormdataSearch(XMLConfiguration config) {
        this.config = config;
    }

    public String search(String database) {

        dataList = new ArrayList<List<NormData>>();

        NormDatabase ndb = NormDatabase.get(database);
        dataList = search(ndb, searchValue, searchOption);

        //for gnd also search edu.experts
        //        getEduExpertsId(ndb);

        //        for (List<NormData> list : dataList) {
        //            System.out.println("\n");
        //            for (NormData normData : list) {
        //                System.out.println(normData.getKey() + "\t\t" + StringUtils.join(normData.getValues(), "; "));
        //            }
        //        }

        List<String> keyList = new ArrayList<>();
        if (config != null) {
            keyList = config.getList("normdata.keys.key");
        }

        dataList = filterNormdata(dataList, keyList);
        return "";
    }

    public List<List<NormData>> getDataList() {
        return this.dataList;
    }
    
    /**
     * @param ndb
     * @param foundEERecord
     */
    public void getEduExpertsId(NormDatabase ndb) {
        boolean foundEERecord = false;
        if (ndb instanceof GndDatabase) {
            for (List<NormData> gndRecord : dataList) {
                String gndIdentifier = null;
                int gndIdentifierIndex = -1;
                int gndUriIndex = -1;
                for (NormData normData : gndRecord) {
                    if ("NORM_IDENTIFIER".equals(normData.getKey()) && !normData.getValues().isEmpty()) {
                        gndIdentifier = normData.getValues().iterator().next().getText();
                        gndIdentifierIndex = gndRecord.indexOf(normData);
                    }
                    if ("URI".equals(normData.getKey()) && !normData.getValues().isEmpty()) {
                        gndUriIndex = gndRecord.indexOf(normData);
                    }
                }
                if (gndIdentifier != null) {
                    List<List<NormData>> eduExpertsData = search(new EduExpertsDatabase(), gndIdentifier, "gnduid");
                    if (!eduExpertsData.isEmpty()) {
                        foundEERecord = true;
                        for (NormData normData : eduExpertsData.iterator().next()) {
                            if (EduExpertsDatabase.OUTPUT_IDENTIFIER.equals(normData.getKey())) {
                                gndRecord.add(gndIdentifierIndex + 1, normData);
                            }
                            if (EduExpertsDatabase.OUTPUT_URI.equals(normData.getKey())) {
                                gndRecord.add(gndUriIndex + 1, normData);
                            }
                        }
                    }
                    //                    mergeDataSets(gndRecord, eduExpertsData);
                }
                if (foundEERecord) {
                    break;
                }
            }
        }
    }

    /**
     * Writes all values of the second normdata list into the first one
     * 
     * @param gndRecord
     * @param eduExpertsData
     */
    private void mergeDataSets(List<NormData> gndRecord, List<List<NormData>> eduExpertsData) {
        for (List<NormData> eduExpertsRecord : eduExpertsData) {
            for (NormData normDataEE : eduExpertsRecord) {
                boolean written = false;
                for (NormData normData : gndRecord) {
                    if (normData.getKey().equals(normDataEE.getKey())) {
                        normData.getValues().addAll(normDataEE.getValues());
                        written = true;
                        break;
                    }
                }
                if (!written) {
                    gndRecord.add(normDataEE);
                }
            }
        }
    }

    /**
     * @param ndb
     * @return
     */
    private List<List<NormData>> search(NormDatabase ndb, String searchValue, String searchOption) {
        String val = ndb.getSearchTerm(searchValue, searchOption);
        String catalog = ndb.getCatalogue(searchOption);
        List<List<NormData>> list = queryDatabase(ndb, catalog, val);
        if (list != null) {
            //list is null if connection did not succeed
            list = ndb.internalMappings(list);
        } else {
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * @param database
     * @param catalog
     * @param val
     * @return
     */
    private List<List<NormData>> queryDatabase(NormDatabase database, String catalog, String val) {
        val = val.replace(".", "");
        URL url = convertToURLEscapingIllegalCharacters(
                INTRANDA_NORMDATA_SERVICE_URL + database.getName() + "/" + catalog + "/" + val,
                database);
        String string = url.toString().replace("Ä", "%C3%84").replace("Ö", "%C3%96").replace("Ü", "%C3%9C").replace("ä", "%C3%A4").replace(
                "ö",
                "%C3%B6").replace("ü", "%C3%BC").replace("ß", "%C3%9F");
        log.debug("Retrieve normdata from " + string);
        List list = NormDataImporter.importNormDataList(string);
        list = createURLForIndentifier(list);
        return list;

    }

    private List<List<NormData>> createURLForIndentifier(List<List<NormData>> list) {
        if (list != null) {
            for (List<NormData> normDataList : list) {
                for (NormData normData : normDataList) {
                    if ("URI".equals(normData.getKey())) {
                        List<NormDataValue> values = normData.getValues();
                        for (NormDataValue value : values) {
                            if (value.getUrl() == null && value.getText().startsWith("http")) {
                                value.setUrl(value.getText());
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    private List<List<NormData>> filterNormdata(List<List<NormData>> data, List<String> keys) {
        if (keys == null || keys.isEmpty() || data == null) {
            return data;
        }
        for (List<NormData> normdataList : data) {
            ListIterator<NormData> i = normdataList.listIterator();
            while (i.hasNext()) {
                NormData nd = i.next();
                if (!keys.contains(nd.getKey())) {
                    i.remove();
                }
            }
        }
        return data;
    }

    private URL convertToURLEscapingIllegalCharacters(String string, NormDatabase database) {
        try {
            String decodedURL = database.getSearchTerm(URLDecoder.decode(string, "UTF-8"), null);
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String searchGeonames() {
        String credentials = ConfigurationHelper.getInstance().getGeonamesCredentials();
        if (credentials != null) {
            WebService.setUserName(credentials);
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setNameEquals(searchValue);
            searchCriteria.setFeatureClass(FeatureClass.A);
            searchCriteria.setStyle(Style.FULL);
            try {
                ToponymSearchResult searchResult = WebService.search(searchCriteria);
                resultList = searchResult.getToponyms();
                totalResults = searchResult.getTotalResultsCount();

                searchCriteria.setFeatureClass(FeatureClass.P);
                searchResult = WebService.search(searchCriteria);
                resultList.addAll(searchResult.getToponyms());
                totalResults += searchResult.getTotalResultsCount();
            } catch (Exception e) {

            }

        } else {
            // deaktiviert
            Helper.setFehlerMeldung("geonamesDeactivated");
        }
        return "";
    }

    public String searchLanguage() {

        try {
            searchedLanguages = WorldViewsDatabaseManager.getLanguageList(searchValue);
        } catch (SQLException e) {
            log.error(e);
        }
        return "";
    }

    public void setSearchValue(String value) {
        this.searchValue = value;
    }

    public String getLocationMap(Toponym location) {
        //		String url = "http://www.geonames.org/maps/google_{latitude}_{longitude}.html";
        //		url = url.replace("{latitude}", Double.toString(latitude)).replace("{longitude}", Double.toString(longitude));
        String url = "http://www.geonames.org/{identifier}";
        url = url.replace("{identifier}", Integer.toString(location.getGeoNameId()));
        return url;
    }

    /**
     * 
     * @param metadata
     * @return true if the metadata has a name (or a first and last name for person)
     */
    public boolean mayCreateEduExpertsRecord(ComplexMetadataObject metadata) {
        if (metadata instanceof Person) {
            return StringUtils.isNotBlank(((Person) metadata).getLastName());
        } else if (metadata instanceof Corporation) {
            return StringUtils.isNotBlank(((Corporation) metadata).getName());
        } else {
            return false;
        }
    }

    /**
     * Adds normdata id and uri for edu.experts to metadata if an entry can be found
     * in the edu.experts normdatabase by searching for the gnd uid or the person/corporation name
     * 
     * @param metadata
     * @return
     */
    public boolean addEduExpertsNormdata(ComplexMetadataObject metadata) {
        List<List<NormData>> eduExpertsData = new ArrayList<>();
        if (StringUtils.isNotBlank(metadata.getNormdata("gnd").getId())) {
            eduExpertsData.addAll(search(new EduExpertsDatabase(), metadata.getNormdata("gnd").getId(), "gnduid"));
        } else if (StringUtils.isNotBlank(metadata.getNameForSearch())) {
            eduExpertsData.addAll(
                    search(new EduExpertsDatabase(), metadata.getNameForSearch(), metadata instanceof Person ? "expert" : "corporatebody"));
        } else {
            //no basis for search
            return false;
        }

        if (!eduExpertsData.isEmpty()) {
            for (NormData normData : eduExpertsData.iterator().next()) {
                if (EduExpertsDatabase.OUTPUT_IDENTIFIER.equals(normData.getKey()) && !normData.getValues().isEmpty()) {
                    metadata.getNormdata("edu.experts").setId(normData.getValues().get(0).getText());
                }
                if (EduExpertsDatabase.OUTPUT_URI.equals(normData.getKey())) {
                    metadata.getNormdata("edu.experts").setUri(normData.getValues().get(0).getText());
                }
                if (GndDatabase.GND_URI_FIELD.equals(normData.getKey())) {
                    metadata.getNormdata("gnd").setUri(normData.getValues().get(0).getText());
                }

            }
            return true;
        } else {
            return false;
        }
    }

    public void createEduExpertsRecord(ComplexMetadataObject metadata) throws IOException {

        try {
            if (addEduExpertsNormdata(metadata)) {
                setCreateRecordResult(Helper.getTranslation("create_ee_record_exists", metadata.getName(), metadata.getNormdataUri("edu.experts")));
                setCreateRecordSuccess(true);
            } else {
                new EduExpertsDatabase().createRecord(metadata);
                if (addEduExpertsNormdata(metadata)) {
                    setCreateRecordResult(
                            Helper.getTranslation("create_ee_record_created", metadata.getName(), metadata.getNormdataUri("edu.experts")));
                    setCreateRecordSuccess(true);
                } else {
                    throw new IOException("Created record not found in database");
                }
            }
        } catch (IllegalArgumentException e) {
            setCreateRecordResult(Helper.getTranslation("create_ee_record_fail_illegal_argument", metadata.getName(), e.getMessage()));
            setCreateRecordSuccess(false);
        } catch (IOException e) {
            setCreateRecordResult(Helper.getTranslation("create_ee_record_fail_io", metadata.getName(), e.getMessage()));
            setCreateRecordSuccess(false);
        }

    }
    
    public void cleanSearchList() {
        this.dataList = new ArrayList<>();
        this.resultList = new ArrayList<>();
        this.searchedLanguages = new ArrayList<>();
    }

}
