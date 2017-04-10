package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.SimpleMetadataObject;
import lombok.Data;

public @Data class BibliographicMetadata {

    private Integer resourceID = null;
    private Integer prozesseID;

    private String documentType;

    //  Titel
    private String maintitleOriginal;
    //  Untertitel
    private String subtitleOriginal;
    
    private String languageMainTitle;

    //    Übersetzung (de)
    private String maintitleGerman;

    //  Übersetzung (en)
    private String maintitleEnglish;

    //    Person (Schulbuch)
    //    - Vorname
    //    - Nachname
    //    - Rolle
    //    - Normdaten
    // TODO validieren: nicht leer
    private List<Person> personList = new ArrayList<Person>();

    //    Erscheinungsort
    private List<Location> placeOfPublicationList = new ArrayList<>();
    private Location currentLocation;
    //    Verlag
    //    - Name
    //    - Rolle
    //    - Normdaten
    // TODO validieren: nicht leer
    private List<Corporation> corporationList = new ArrayList<>();
    private List<Corporation> publisherList = new ArrayList<>();
    //  Erscheinungsjahr
    private String publicationYear;

    //  Sprache
    private List<SimpleMetadataObject> languageList = new ArrayList<>();

    //    Bandtitel
    private String volumeTitleOriginal;
    //    Übersetzung (de)
    private String volumeTitleGerman;

    //  Übersetzung (en)
    private String volumeTitleEnglish;
    
    private String languageVolumeTitle;
    
    //    Bandnummer
    private String volumeNumber;

    // TODO validieren: nicht leer
    private List<Person> volumePersonList = new ArrayList<Person>();
    
    private List<Corporation> volumeCorporationList = new ArrayList<Corporation>();

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

  

    private Person currentPerson;
    private Corporation currentCorporation;
    private Corporation currentPublisher;
    private SimpleMetadataObject currentObject;

    public BibliographicMetadata(Integer prozesseID) {
        this.prozesseID = prozesseID;
    }

    public String getLabel() {
        String label = maintitleGerman;
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
        if (currentPerson != null && personList.contains(currentPerson)) {
            personList.remove(currentPerson);
        }
    }

    public void addNewVolumeAuthor() {
        Person per = new Person();
        volumePersonList.add(per);

    }

    public void deleteVolumeAuthor() {
        if (currentPerson != null && volumePersonList.contains(currentPerson)) {
            volumePersonList.remove(currentPerson);
        }
    }
    
    public void deletePublisher() {
        if (currentPublisher != null && publisherList.contains(currentPublisher)) {
            publisherList.remove(currentPublisher);
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
        if (currentCorporation != null && corporationList.contains(currentCorporation)) {
            corporationList.remove(currentCorporation);
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
        if (currentCorporation != null && volumeCorporationList.contains(currentCorporation)) {
        	volumeCorporationList.remove(currentCorporation);
        }
    }

    public void addNewCorporation() {
        Corporation pub = new Corporation();
        corporationList.add(pub);
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
}
