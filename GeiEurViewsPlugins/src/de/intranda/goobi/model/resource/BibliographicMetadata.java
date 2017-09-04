package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.ComplexMetadataContainer;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import lombok.Data;

public @Data class BibliographicMetadata implements ComplexMetadataContainer{
    
    public static final String MULTIVOLUME = "multivolume";
    public static final String MONOGRAPH = "book";

    private Integer resourceID = null;
    private Integer prozesseID;
    
    private String documentType;

    //Werktitel (Monographie/MBW)
    private TitleInfo mainTitle = new TitleInfo();
    //Bandtitel
    private TitleInfo volumeTitle = new TitleInfo();
    //Serientitel
    private TitleInfo seriesTitle = new TitleInfo();
    
    //analog PPNs
    //PPNa (Monograph) oder PPNc (MBW)
    private String mainIdentifier = null;
    //PPNf (MBW)
    private String volumeIdentifier = null;
    
    //    Person (Schulbuch)
    //    - Vorname
    //    - Nachname
    //    - Rolle
    //    - Normdaten
    // TODO validieren: nicht leer
    private List<Person> personList = new ArrayList<Person>();
    private List<Person> volumePersonList = new ArrayList<Person>();
    private List<Corporation> corporationList = new ArrayList<>();
    private List<Corporation> volumeCorporationList = new ArrayList<Corporation>();
    //Personen oder Körperschaften "Verantwortlichkeit (Reihe)"
    private List<ComplexMetadataObject> seriesResponsibilityList = new ArrayList<>();
    //    Verlag
    //    - Name
    //    - Rolle
    //    - Normdaten
    // TODO validieren: nicht leer
    private List<Corporation> publisherList = new ArrayList<>();

    //    Erscheinungsort
    private List<Location> placeOfPublicationList = new ArrayList<>();
    //  Erscheinungsjahr
    private String publicationYear;

    //  Sprache
    private List<SimpleMetadataObject> languageList = new ArrayList<>();
    //    Einsatzland
    private List<Location> countryList = new ArrayList<>();
    //    Einsatzbundesland
    private List<Location> stateList = new ArrayList<>();

    //    Schulfach
    private List<SimpleMetadataObject> schoolSubjects = new ArrayList<>();
    //    Schulstufe
    private String educationLevel;
    //    Auflage
    private String edition;
    //    ISBN
    private String isbn;
    //    Bibliothek
    private String physicalLocation;
    //  Signatur
    private String shelfmark;
    //    Seiten
    private String numberOfPages;

  

//    private Person currentPerson;
//    private Corporation currentCorporation;
//    private Corporation currentPublisher;
    private ComplexMetadataObject currentComplexObject;
    private SimpleMetadataObject currentObject;

    public BibliographicMetadata(Integer prozesseID) {
        this.prozesseID = prozesseID;
    }

    public String getLabel() {
        String label = getMainTitle().getTranslationGER();
        if(StringUtils.isBlank(label)) {
        	label = getMainTitle().getTitle();
        }
        if (resourceID != null) {
            label = label + " (" + resourceID + ")";
        }
        return label;
    }

   

    public void addBookAuthor(Person aut) {
        this.personList.add(aut);
    }

    public void addVolumeAuthor(Person aut) {
        this.volumePersonList.add(aut);
    }

    public void addLanguage(SimpleMetadataObject lang) {
        this.languageList.add(lang);
    }

    public void addPublisher(Corporation pub) {
        this.publisherList.add(pub);
    }

    public void addCountry(Location country) {
        this.countryList.add(country);
    }

    public void addState(Location state) {
        this.stateList.add(state);
    }

    public void addNewBookAuthor() {
        Person per = new Person();

        personList.add(per);
    }


    public void addNewVolumeAuthor() {
        Person per = new Person();
        volumePersonList.add(per);

    }


    public void addNewPublisher() {
        Corporation pub = new Corporation();
        publisherList.add(pub);
    }

    public void addCorporation(Corporation pub) {
        this.corporationList.add(pub);
    }
 

    public void addNewVolumeCorporation() {
        Corporation pub = new Corporation();
        volumeCorporationList.add(pub);
    }
    
    public void addVolumeCorporation(Corporation pub) {
        this.volumeCorporationList.add(pub);
    }


    public void addNewCorporation() {
        Corporation pub = new Corporation();
        corporationList.add(pub);
    }

    
    public void addNewSeriesResponsibility() {
        Corporation pub = new Corporation();
        seriesResponsibilityList.add(pub);
    }

    public void deleteLanguage() {
        if (currentObject != null && languageList.contains(currentObject)) {
            languageList.remove(currentObject);
        }
    }

    public void addLanguage() {
        languageList.add(new SimpleMetadataObject(""));
    }


    public void addCountry() {
        countryList.add(new Location("country"));
    }


    public void addState() {
        stateList.add(new Location("state"));
    }

    public void addPlaceOfPublication(Location aut) {
        this.placeOfPublicationList.add(aut);
    }

    public void addNewPlaceOfPublication() {
        Location placeOfPublication = new Location();
        placeOfPublication.setRole("PlaceOfPublication");
        placeOfPublicationList.add(placeOfPublication);
    }

    
    public String getPlaceOfPublicationNames() {
    	return StringUtils.join(getPlaceOfPublicationList(), ", ");
    }
    

    public void addSchoolSubject(SimpleMetadataObject simpleMetadataObject) {
        this.schoolSubjects.add(simpleMetadataObject);
        
    }
    
    public void addSchoolSubject() {
        this.schoolSubjects.add(new SimpleMetadataObject(""));
    }
    
    public void deleteSchoolSubject() {
        if (currentObject != null && schoolSubjects.contains(currentObject)) {
            schoolSubjects.remove(currentObject);
        }
    }
    
    public boolean isMultivolume() {
        return MULTIVOLUME.equals(getDocumentType());
    }
    
    public boolean isSeriesVolume() {
        return !getSeriesTitle().isEmpty();
    }

    @Override
    public void deleteMetadata(ComplexMetadataObject metadata) {
        personList.remove(metadata);
        corporationList.remove(metadata);
        countryList.remove(metadata);
        placeOfPublicationList.remove(metadata);
        stateList.remove(metadata);
        publisherList.remove(metadata);
        seriesResponsibilityList.remove(metadata);
        volumeCorporationList.remove(metadata);
        volumePersonList.remove(metadata);
        
    }

    @Override
    public ComplexMetadataObject getCurrentMetadata() {
        return currentComplexObject;
    }

    @Override
    public void setCurrentMetadata(ComplexMetadataObject metadata) {
        this.currentComplexObject = metadata;
        
    }


}
