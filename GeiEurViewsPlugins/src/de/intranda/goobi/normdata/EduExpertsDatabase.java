package de.intranda.goobi.normdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.Person;

public class EduExpertsDatabase extends NormDatabase {

    private static final Logger logger = Logger.getLogger(EduExpertsDatabase.class);
    
    public static final String INPUT_IDENTIFIER = "NORM_IDENTIFIER_ZLB";
    public static final String OUTPUT_IDENTIFIER = "NORM_IDENTIFIER_EDU_EXPERTS";
    
    public static final String INPUT_URI = "URI";
    public static final String OUTPUT_URI = "URI_EDU_EXPERTS";
    
    public static final String WVEXPERTS_DATABASE_URL = "http://wvexperts.gei.de/middleware/";
    private static final String URL_CHARSET = "utf-8";
    private static final String CREATE_RECORD_SUCCESS_STRING = "created and moved";

    @Override
    public String getSearchTerm(String searchValue, String searchOption) {
        return searchValue.replace(" ", "+");
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

    public boolean createRecord(ComplexMetadataObject metadata) throws IOException {
        
        if(StringUtils.isBlank(metadata.getName())) {
            return false;
        }
        
        URL createUrl = buildCreateUrl(metadata);
        logger.debug("Calling url " + createUrl);
        
        URLConnection connection = createUrl.openConnection();
        connection.setRequestProperty("Accept-Charset", URL_CHARSET);
        try(InputStream responseStream = connection.getInputStream(); Scanner scanner = new Scanner(responseStream)) {
            String response = scanner.useDelimiter("\\A").next();
            return response.contains(CREATE_RECORD_SUCCESS_STRING);
        }


    }

    /**
     * @param metadata
     * @return
     * @throws MalformedURLException
     */
    public URL buildCreateUrl(ComplexMetadataObject metadata) throws MalformedURLException {
        StringBuilder sb = new StringBuilder(WVEXPERTS_DATABASE_URL);
        sb.append("?action=create");
        if(StringUtils.isNotBlank(metadata.getNormdataValue(NormDatabase.Database.GND.getLabel()))) {
            sb.append("&gnduid=").append(metadata.getNormdataValue(NormDatabase.Database.GND.getLabel()));
        }
        if(metadata instanceof Person) {
            sb.append("&type=expert")
            .append("&firstName=").append(((Person) metadata).getFirstName())
            .append("&lastName=").append(((Person) metadata).getLastName());
        } else if(metadata instanceof Corporation){
            sb.append("&type=corporatebody")
            .append("&name=").append(metadata.getName());
        }
        
        URL createUrl = new URL(sb.toString());
//        try {
//            createUrl = new URL(URLEncoder.encode(sb.toString(), URL_CHARSET));
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalArgumentException(URL_CHARSET + " is not supported by URLEncoder");
//        }
        return createUrl;
    }

}
