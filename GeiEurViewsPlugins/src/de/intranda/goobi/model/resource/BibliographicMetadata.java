package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sun.faces.util.CollectionsUtils;

import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.SimpleMetadataObject;
import lombok.Data;

public @Data class BibliographicMetadata {
    
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
    private String schoolSubject;
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
    private ComplexMetadataObject currentIdentity;
    private SimpleMetadataObject currentObject;
    private Location currentLocation;

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

    public void deleteBookAuthor() {
        if (currentIdentity != null && personList.contains(currentIdentity)) {
            personList.remove(currentIdentity);
        }
    }

    public void addNewVolumeAuthor() {
        Person per = new Person();
        volumePersonList.add(per);

    }

    public void deleteVolumeAuthor() {
        if (currentIdentity != null && volumePersonList.contains(currentIdentity)) {
            volumePersonList.remove(currentIdentity);
        }
    }
    
    public void deletePublisher() {
        if (currentIdentity != null && publisherList.contains(currentIdentity)) {
            publisherList.remove(currentIdentity);
        }
    }

    public void addNewPublisher() {
        Corporation pub = new Corporation();
        publisherList.add(pub);
    }

    public void addCorporation(Corporation pub) {
        this.corporationList.add(pub);
    }
    
    public void deleteCorporation() {
        if (currentIdentity != null && corporationList.contains(currentIdentity)) {
            corporationList.remove(currentIdentity);
        }
    }

    public void addNewVolumeCorporation() {
        Corporation pub = new Corporation();
        volumeCorporationList.add(pub);
    }
    
    public void addVolumeCorporation(Corporation pub) {
        this.volumeCorporationList.add(pub);
    }
    
    public void deleteVolumeCorporation() {
        if (currentIdentity != null && volumeCorporationList.contains(currentIdentity)) {
        	volumeCorporationList.remove(currentIdentity);
        }
    }

    public void addNewCorporation() {
        Corporation pub = new Corporation();
        corporationList.add(pub);
    }
    
    public void deleteSeriesResponsibility() {
        if (currentIdentity != null && seriesResponsibilityList.contains(currentIdentity)) {
            seriesResponsibilityList.remove(currentIdentity);
        }
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

    public void deleteCountry() {
        if (currentLocation != null && countryList.contains(currentLocation)) {
            countryList.remove(currentLocation);
        }
    }

    public void addCountry() {
        countryList.add(new Location("country"));
    }

    public void deleteState() {
        if (currentLocation != null && stateList.contains(currentLocation)) {
            stateList.remove(currentLocation);
        }
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

    public void deletePlaceOfPublication() {
        if (currentLocation != null && placeOfPublicationList.contains(currentLocation)) {
            placeOfPublicationList.remove(currentLocation);
        }
    }
    
    public String getPlaceOfPublicationNames() {
    	return StringUtils.join(getPlaceOfPublicationList(), ", ");
    }
    
    public boolean isMultivolume() {
        return MULTIVOLUME.equals(getDocumentType());
    }
    
    public boolean isSeriesVolume() {
        return !getSeriesTitle().isEmpty();
    }

}
