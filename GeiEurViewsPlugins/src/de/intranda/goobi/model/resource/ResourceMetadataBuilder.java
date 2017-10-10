package de.intranda.goobi.model.resource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.jdom2.JDOMException;

import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.EurViewsRecord;
import de.intranda.goobi.model.LanguageHelper;
import de.intranda.goobi.model.NormdataEntity;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SourceType;
import de.intranda.goobi.model.SourceTypeHelper;
import de.intranda.goobi.normdata.NormdataSearch;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;

public class ResourceMetadataBuilder {

    private static final Logger logger = Logger.getLogger(ResourceMetadataBuilder.class);

    public static ResouceMetadata build(Process sourceProcess, EurViewsRecord record, BibliographicMetadata bibData) {

        ResouceMetadata data = new ResouceMetadata(sourceProcess.getId());
        init(data, record, bibData);
        return data;
    }
    

    public static void init(ResouceMetadata data, EurViewsRecord record, BibliographicMetadata bibData) {
        try{
            if(bibData != null) {
                addDataFromBook(data, bibData);
                data.setBibliographicData(bibData);
                data.setBibliographicDataId(bibData.getProzesseID());
            }
            if(record != null) {
                addDataFromRecord(data, record);
            }
            addEduExpertsNormdata(data);
        } catch (JDOMException | IOException | IllegalArgumentException e) {
            logger.error(e);
        }
    }

    public static void resetData(ResouceMetadata data) {
       data.setResourceAuthorList(new ArrayList<Person>());
        
    }

    private static void addDataFromRecord(ResouceMetadata data, EurViewsRecord record) throws JDOMException, IOException, IllegalArgumentException {

        String pages = record.get("bibRef/pages", "");
        if (pages.matches("(S.?\\s*)?[0-9]+-(\\s*S.?\\s*)?[0-9]+")) {
            int toIndex = pages.indexOf("-");
            String firstPage = pages.substring(0, toIndex).replaceAll("S\\.", "").trim();
            String lastPage = pages.substring(toIndex + 1).trim();
            data.setStartPage(firstPage);
            data.setEndPage(lastPage);
        } else {
            data.setStartPage(pages);
        }

        data.getResourceTitle().setLanguage(LanguageHelper.getInstance().getLanguage(record.get("bibRef/source/@xml:lang", "ger")).getIsoCode());
        data.getResourceTitle().setTitle(record.get("bibRef/source", ""));
        data.getResourceTitle().setTranslationENG(record.get("bibRef/titles/title[@xml:lang=\"en\"]", ""));
        data.getResourceTitle().setTranslationGER(record.get("bibRef/titles/title[@xml:lang=\"de\"]", ""));

        List<String> categories = record.getAll("categories/categorieslist[@xml:lang=\"de\"]/category");
        for (String category : categories) {
            SourceType sourceType = SourceTypeHelper.getInstance().findSourceType(category);
            if (sourceType != null) {
                data.addResourceType(sourceType);
            }

        }
        List<String> types = record.getAll("type");
        for (String type : types) {
            SourceType sourceType = SourceTypeHelper.getInstance().findSourceType(type);
            if (sourceType != null && !data.getResourceTypes().contains(sourceType)) {
                data.addResourceType(sourceType);
            }
        }

    }

    private static void addDataFromBook(ResouceMetadata data, BibliographicMetadata bm) {
        if (data.getResourceAuthorList().isEmpty() && data.getBibliographicDataId() != null) {

            if (!bm.getPersonList().isEmpty()) {
                for (Person author : bm.getPersonList()) {
                    Person per = new Person();
                    per.setFirstName(author.getFirstName());
                    per.setLastName(author.getLastName());
                    per.setNormdata(author.getNormdata());
                    per.setRole(author.getRole());

                    data.addToResourceAuthorList(per);
                }
            }
        }

        if (StringUtils.isBlank(data.getPublicationYearDigital())) {
            data.setPublicationYearDigital(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        }

    }
    
    public static void addEduExpertsNormdata(ResouceMetadata data) {
        List<ComplexMetadataObject> objects = new ArrayList<>();
        objects.addAll(data.getResourceAuthorList());

        NormdataSearch search = new NormdataSearch(null);
        for (ComplexMetadataObject object : objects) {
            NormdataEntity gnd = object.getNormdata("gnd");
            NormdataEntity eduexperts = object.getNormdata("edu.experts");
            if (eduexperts.isEmpty() && !gnd.isEmpty()) {
                search.addEduExpertsNormdata(object);
            }
        }
    }

}
