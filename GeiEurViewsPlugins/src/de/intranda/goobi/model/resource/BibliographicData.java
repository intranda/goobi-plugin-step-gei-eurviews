package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;

import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import lombok.Data;

public @Data class BibliographicData {

    private Integer resourceID = null;
    private Integer prozesseID;

    private String documentType;

    //  Titel
    private String maintitleOriginal;
    //  Untertitel
    private String subtitleOriginal;

    //    Übersetzung (de)
    private String maintitleGerman;

    //  Übersetzung (en)
    private String maintitleEnglish;

    //    Person (Schulbuch)
    //    - Vorname
    //    - Nachname
    //    - Rolle
    //    - Normdaten
    private List<Person> personList = new ArrayList<Person>();

    //    Erscheinungsort
    private String placeOfPublication;

    //    Verlag
    //    - Name
    //    - Rolle
    //    - Normdaten
    private List<Publisher> publisherList = new ArrayList<>();

    //  Erscheinungsjahr
    private String publicationYear;

    //  Sprache
    private List<String> languageList = new ArrayList<>();

    //    Bandtitel
    private String volumeTitleOriginal;
    //    Übersetzung (de)
    private String volumeTitleGerman;

    //  Übersetzung (en)
    private String volumeTitleEnglish;
    //    Bandnummer
    private String volumeNumber;

    private List<Person> volumePersonList = new ArrayList<Person>();

    
    //    Einsatzland
    private List<String> countryList = new ArrayList<>();
    //    Einsatzbundesland
    private List<String> stateList = new ArrayList<>();

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

    // Quellentyp
    private String resourceType;

    //    Titel
    private String resourceTitleOriginal;
    //    Übersetzung (de)
    private String resourceTitleGerman;
    //    Übersetzung (en)
    private String resourceTitleEnglish;

    //    Seitenzahl Seitenbereich
    //    - Von
    private String startPage;
    //    - Bis
    private String endPage;

    //    Zulieferung durch
    private String supplier;

    
    
    
    
    
    
    
    
    public BibliographicData(Integer prozesseID) {
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
    
    public void addLanguage(String lang) {
        this.languageList.add(lang);
    }

    public void addPublisher(Publisher pub) {
        this.publisherList.add(pub);
    }

    public void addCountry(String country) {
        this.countryList.add(country);
    }

    public void addState(String state) {
        this.stateList.add(state);
    }

}
