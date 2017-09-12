package de.intranda.goobi.model.resource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.jdom2.JDOMException;

import de.intranda.goobi.model.ComplexMetadataContainer;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.EurViewsRecord;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.SourceType;
import de.intranda.goobi.model.SourceTypeHelper;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.intranda.goobi.plugins.SourceInitializationPlugin;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class BibliographicMetadataBuilder {

    private static final Logger logger = Logger.getLogger(SourceInitializationPlugin.class);
    
//    private static final List<String> POSSIBLE_SUBJECTS = Arrays.asList(new String[]{"Geschichte", "Erdkunde", "Sozialkunde/Politik"});
//    private static final List<String> POSSIBLE_EDUCATION_LEVELS = Arrays.asList(new String[]{"Primärstufe", "Primarstufe", "Sekundarstufe 1", "Sekundarstufe 2", "Tertiärbereich"});

    public static BibliographicMetadata build(Process bookProcess, EurViewsRecord record) {
        BibliographicMetadata data = null;
        try {
            data = WorldViewsDatabaseManager.getBibliographicData(bookProcess.getId());
        } catch (Throwable e) {
            logger.error(e);
        }
        if (data == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("create new bibliographic record");
            }
            data = new BibliographicMetadata(bookProcess.getId());
        } else {
            resetData(data);
        }

            try {
                Fileformat ff = bookProcess.readMetadataFile();
                DigitalDocument dd = ff.getDigitalDocument();

                DocStruct volume = null;
                DocStruct logical = dd.getLogicalDocStruct();
                if (logical.getType().isAnchor()) {
                    data.setDocumentType(BibliographicMetadata.MULTIVOLUME);
                    // anchor = logical;
                    // logical = logical.getAllChildren().get(0);
                    volume = logical.getAllChildren().get(0);
                } else {
                    data.setDocumentType(BibliographicMetadata.MONOGRAPH);
                }

                //                readMetsMetadata(data, volume, logical);
                readIdentifierFromMets(data, volume, logical);
                readRecordMetadata(data, record, logical.getType().isAnchor());
                

            } catch (ReadException | PreferencesException | WriteException | IOException | InterruptedException | SwapException | DAOException | JDOMException e) {
                logger.error(e);
                return null;
            }
        return data;
    }

    private static void readRecordMetadata(BibliographicMetadata data, EurViewsRecord record, boolean multivolume) throws JDOMException, IOException {
        
        if(multivolume) {
            data.getVolumeTitle().setTitle(record.get("bibRef/publishedIn", ""));
            if(StringUtils.isNotBlank(record.get("bibRef/part", ""))) {
                data.getVolumeTitle().setTitle(data.getVolumeTitle().getTitle() + "; " + record.get("bibRef/part"));
            }
        } else {            
            data.getMainTitle().setTitle(record.get("bibRef/publishedIn", ""));
            if(StringUtils.isNotBlank(record.get("bibRef/part", ""))) {
                data.getMainTitle().setTitle(data.getMainTitle().getTitle() + "; " + record.get("bibRef/part"));
            }
        }
        
        String shelfmarkString = record.get("bibRef/shelfMark", "");
        if(StringUtils.isNotBlank(shelfmarkString)) {
            int separatorIndex = shelfmarkString.indexOf(";");
            if(separatorIndex > -1) {
                data.setPhysicalLocation(shelfmarkString.substring(0, separatorIndex).trim());
                data.setShelfmark(shelfmarkString.substring(separatorIndex+1).trim());
            } else {
                data.setPhysicalLocation(shelfmarkString);
            }
        }
        
        data.setIsbn(record.get("bibRef/number[@type=\"isbn\"]", ""));
        
        data.setEdition(record.get("bibRef/edition", ""));
        
        if(StringUtils.isNotBlank(record.get("bibRef/pubDate", ""))) {
            data.setPublicationYear(record.get("bibRef/pubDate"));
        } else {
            data.setPublicationYear(record.get("bibRef/year"));
        }
                
        List<String> places = record.getAll("bibRef/place");
        for (String placeName : places) {
            Location place = new Location("PlaceOfPublication");
            place.setName(placeName);
            data.addPlaceOfPublication(place);
        }
        
        List<String> publisherList = record.getAll("bibRef/publisher");
        for (String publisherName : publisherList) {
            Corporation publisher = new Corporation();
            publisher.setRole("Editor");
            publisher.setName(publisherName);
            data.addPublisher(publisher);
        }
        
        List<String> countries = record.getAll("bibRef/country");
        for (String placeName : countries) {
            Location place = new Location("country");
            place.setName(placeName);
            data.addCountry(place);
        }
        
        data.setNumberOfPages(record.get("bibRef/extent", ""));
        
        List<String> authorStrings = record.getAll("bibRef/authors/author[not(@role)]");
        for (String authorName : authorStrings) {
            Person author = new Person();
            author.setRole("Author");
            author.setName(authorName, false);
            data.addBookAuthor(author);
        }
        
        List<String> editorStrings = record.getAll("bibRef/authors/author[@role]");
        for (String authorName : editorStrings) {
            Person author = new Person();
            author.setRole("Editor");
            author.setName(authorName, false);
            data.addBookAuthor(author);
        }
        
        List<String> categories = record.getAll("categories/categorieslist[@xml:lang=\"de\"]/category");
        for (String category : categories) {
            SchoolSubject subject = SchoolSubject.getSchoolSubject(category);
            if(subject != null) {
                data.addSchoolSubject(new SimpleMetadataObject(subject.name()));
            }
            EducationLevel level = EducationLevel.getEducationLevel(category);
            if(level != null) {
                data.setEducationLevel(level.name());
            }
        }

        
        
//        String originalLanguage = getLanguageCode(record.get(record.get("bibRef/source[@lang]", "ger")));
//        data.getMainTitle().setTitle(record.get("bibRef/publishedIn"));
//        data.getMainTitle().setLanguage(originalLanguage);
//        data.getMainTitle().setTranslationENG(record.get("bibRef/titles/title[@lang=\"en\"]", ""));
//        data.getMainTitle().setTranslationGER(record.get("bibRef/titles/title[@lang=\"de\"]", ""));
//
//        if (data.getDocumentType().equals(BibliographicMetadata.MULTIVOLUME)) {
//            data.getVolumeTitle().setTitle(record.get("bibRef/part"));
//            data.getVolumeTitle().setLanguage(originalLanguage);
//            data.getVolumeTitle().setTranslationENG(record.get("bibRef/titles/title[@lang=\"en\"]", ""));
//            data.getVolumeTitle().setTranslationGER(record.get("bibRef/titles/title[@lang=\"de\"]", ""));
//        }

    }

    /**
     * @param data
     */
    public static void resetData(BibliographicMetadata data) {
        data.setPlaceOfPublicationList(new ArrayList<Location>());
        data.setPublisherList(new ArrayList<Corporation>());
        data.setCorporationList(new ArrayList<Corporation>());
        data.setCountryList(new ArrayList<Location>());
        data.setLanguageList(new ArrayList<SimpleMetadataObject>());
        data.setPersonList(new ArrayList<Person>());
        data.setSeriesResponsibilityList(new ArrayList<ComplexMetadataObject>());
        data.setStateList(new ArrayList<Location>());
        data.setVolumeCorporationList(new ArrayList<Corporation>());
        data.setVolumePersonList(new ArrayList<Person>());
        data.setSchoolSubjects(new ArrayList<SimpleMetadataObject>());
    }

    public static void readIdentifierFromMets(BibliographicMetadata data, DocStruct volume, DocStruct logical) {
        for (Metadata metadata : logical.getAllMetadata()) {

            if (metadata.getType().getName().equals("CatalogIDSource")) {
                data.setMainIdentifier(metadata.getValue());
            }
        }

        if (volume != null) {
            for (Metadata metadata : volume.getAllMetadata()) {

                if (metadata.getType().getName().equals("CatalogIDSource")) {
                    data.setVolumeIdentifier(metadata.getValue());
                }
            }
        }
    }

    /**
     * @param data
     * @param volume
     * @param logical
     */
    public static void readMetsMetadata(BibliographicMetadata data, DocStruct volume, DocStruct logical) {
        for (Metadata metadata : logical.getAllMetadata()) {

            if (metadata.getType().getName().equals("TitleDocMain")) {
                data.getMainTitle().setTitle(metadata.getValue());
            } else if (metadata.getType().getName().equals("TitleDocSub1")) {
                data.getMainTitle().setSubTitle(metadata.getValue());
            } else if (metadata.getType().getName().equals("DocLanguage")) {
                data.addLanguage(new SimpleMetadataObject(metadata.getValue()));
            } else if (metadata.getType().getName().equals("PublisherName")) {
                Corporation pub = new Corporation();
                pub.setRole("Editor");
                pub.setName(metadata.getValue());
                data.addPublisher(pub);
            } else if (metadata.getType().getName().equals("PlaceOfPublication")) {
                Location loc = new Location();
                loc.setRole("PlaceOfPublication");
                loc.setName(metadata.getValue());
                data.addPlaceOfPublication(loc);
            } else if (metadata.getType().getName().equals("PublicationYear")) {
                data.setPublicationYear(metadata.getValue());
            } else if (metadata.getType().getName().equals("shelfmarksource")) {
                data.setShelfmark(metadata.getValue());
            } else if (metadata.getType().getName().equals("SeriesTitle")) {
                data.getSeriesTitle().setTitle(metadata.getValue());
            } else if (metadata.getType().getName().equals("SeriesOrder")) {
                data.getSeriesTitle().setNumbering(metadata.getValue());
            } else if (metadata.getType().getName().equals("CatalogIDSource")) {
                data.setMainIdentifier(metadata.getValue());
            }
        }
        if (logical.getAllPersons() != null) {
            for (ugh.dl.Person per : logical.getAllPersons()) {

                if (per.getType().getName().equals("Author") || per.getType().getName().equals("Editor")) {
                    Person aut = new Person();
                    aut.setRole("Author");
                    aut.setFirstName(per.getFirstname());
                    aut.setLastName(per.getLastname());
                    if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
                        aut.setNormdataId("gnd", per.getAuthorityValue());
                    }
                    data.addBookAuthor(aut);
                }
            }
        }

        if (volume != null) {
            for (Metadata metadata : volume.getAllMetadata()) {

                if (metadata.getType().getName().equals("TitleDocMain")) {
                    data.getVolumeTitle().setTitle(metadata.getValue());
                } else if (metadata.getType().getName().equals("TitleDocSub1")) {
                    data.getVolumeTitle().setSubTitle(metadata.getValue());

                } else if (metadata.getType().getName().equals("CurrentNo")) {
                    data.getVolumeTitle().setNumbering(metadata.getValue());
                } else if (metadata.getType().getName().equals("DocLanguage")) {
                    SimpleMetadataObject object = new SimpleMetadataObject(metadata.getValue());
                    if (!data.getLanguageList().contains(object)) {
                        data.addLanguage(object);
                    }
                } else if (metadata.getType().getName().equals("PublisherName")) {
                    Corporation pub = new Corporation();
                    pub.setRole("Editor");
                    pub.setName(metadata.getValue());
                    if (!data.getPublisherList().contains(pub)) {
                        data.addPublisher(pub);
                    }
                } else if (metadata.getType().getName().equals("PlaceOfPublication")) {
                    Location loc = new Location();
                    loc.setRole("PlaceOfPublication");
                    loc.setName(metadata.getValue());
                    if (!data.getPlaceOfPublicationList().contains(loc)) {
                        data.addPlaceOfPublication(loc);
                    }
                } else if (metadata.getType().getName().equals("PublicationYear")) {
                    data.setPublicationYear(metadata.getValue());
                } else if (metadata.getType().getName().equals("shelfmarksource")) {
                    data.setShelfmark(metadata.getValue());
                } else if (metadata.getType().getName().equals("SeriesTitle")) {
                    data.getSeriesTitle().setTitle(metadata.getValue());
                } else if (metadata.getType().getName().equals("SeriesOrder")) {
                    data.getSeriesTitle().setNumbering(metadata.getValue());
                } else if (metadata.getType().getName().equals("CatalogIDSource")) {
                    data.setVolumeIdentifier(metadata.getValue());
                }
            }
            if (volume.getAllPersons() != null) {
                for (ugh.dl.Person per : volume.getAllPersons()) {
                    if (per.getType().getName().equals("Author") || per.getType().getName().equals("Editor")) {
                        Person aut = new Person();
                        aut.setRole("Author");
                        aut.setFirstName(per.getFirstname());
                        aut.setLastName(per.getLastname());
                        if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
                            aut.setNormdataId("gnd", per.getAuthorityValue());
                        }
                        data.addVolumeAuthor(aut);
                    }
                }
            }
        }
    }
}
