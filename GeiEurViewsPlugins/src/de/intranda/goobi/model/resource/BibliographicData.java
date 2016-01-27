package de.intranda.goobi.model.resource;

public class BibliographicData {

    private Integer resourceID = null;
    private Integer prozesseID;

    private String documentType;
    private String maintitleGerman;
    private String subtitleGerman;
    
    private String maintitleEnglish;
    private String subtitleEnglish;
    
    private String maintitleOriginal;
    private String subtitleOriginal;
    
    private String maintitleTransliterated;
    private String subtitleTransliterated;
    
    private String authorFirstnameGerman;
    private String authorLastnameGerman;
    
    private String authorFirstnameEnglish;
    private String authorLastnameEnglish;
    
    private String authorFirstnameOriginal;
    private String authorLastnameOriginal;
    
    private String authorFirstnameTransliterated;
    private String authorLastnameTransliterated;
    
    private String placeOfPublicationGerman;
    private String placeOfPublicationEnglish;
    private String placeOfPublicationOriginal;
    private String placeOfPublicationTransliterated;

    
    private String language;
    private String publisher;
    private String publicationYear;
    private String numberOfPages;
    private String shelfmark;
    private String copyright;

    public BibliographicData(int processId) {
        prozesseID = processId;
    }
    
    public String getLabel() {
        String label = maintitleGerman;
        if (resourceID != null) {
            label = label + " (" + resourceID + ")"; 
        }
        return label;
    }

    public Integer getResourceID() {
        return resourceID;
    }

    public void setResourceID(Integer resourceID) {
        this.resourceID = resourceID;
    }

    public Integer getProzesseID() {
        return prozesseID;
    }

    public void setProzesseID(Integer prozesseID) {
        this.prozesseID = prozesseID;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getMaintitleGerman() {
        return maintitleGerman;
    }

    public void setMaintitleGerman(String maintitleGerman) {
        this.maintitleGerman = maintitleGerman;
    }

    public String getSubtitleGerman() {
        return subtitleGerman;
    }

    public void setSubtitleGerman(String subtitleGerman) {
        this.subtitleGerman = subtitleGerman;
    }

    public String getMaintitleEnglish() {
        return maintitleEnglish;
    }

    public void setMaintitleEnglish(String maintitleEnglish) {
        this.maintitleEnglish = maintitleEnglish;
    }

    public String getSubtitleEnglish() {
        return subtitleEnglish;
    }

    public void setSubtitleEnglish(String subtitleEnglish) {
        this.subtitleEnglish = subtitleEnglish;
    }

    public String getMaintitleOriginal() {
        return maintitleOriginal;
    }

    public void setMaintitleOriginal(String maintitleOriginal) {
        this.maintitleOriginal = maintitleOriginal;
    }

    public String getSubtitleOriginal() {
        return subtitleOriginal;
    }

    public void setSubtitleOriginal(String subtitleOriginal) {
        this.subtitleOriginal = subtitleOriginal;
    }

    public String getMaintitleTransliterated() {
        return maintitleTransliterated;
    }

    public void setMaintitleTransliterated(String maintitleTransliterated) {
        this.maintitleTransliterated = maintitleTransliterated;
    }

    public String getSubtitleTransliterated() {
        return subtitleTransliterated;
    }

    public void setSubtitleTransliterated(String subtitleTransliterated) {
        this.subtitleTransliterated = subtitleTransliterated;
    }

    public String getAuthorFirstnameGerman() {
        return authorFirstnameGerman;
    }

    public void setAuthorFirstnameGerman(String authorFirstnameGerman) {
        this.authorFirstnameGerman = authorFirstnameGerman;
    }

    public String getAuthorLastnameGerman() {
        return authorLastnameGerman;
    }

    public void setAuthorLastnameGerman(String authorLastnameGerman) {
        this.authorLastnameGerman = authorLastnameGerman;
    }

    public String getAuthorFirstnameEnglish() {
        return authorFirstnameEnglish;
    }

    public void setAuthorFirstnameEnglish(String authorFirstnameEnglish) {
        this.authorFirstnameEnglish = authorFirstnameEnglish;
    }

    public String getAuthorLastnameEnglish() {
        return authorLastnameEnglish;
    }

    public void setAuthorLastnameEnglish(String authorLastnameEnglish) {
        this.authorLastnameEnglish = authorLastnameEnglish;
    }

    public String getAuthorFirstnameOriginal() {
        return authorFirstnameOriginal;
    }

    public void setAuthorFirstnameOriginal(String authorFirstnameOriginal) {
        this.authorFirstnameOriginal = authorFirstnameOriginal;
    }

    public String getAuthorLastnameOriginal() {
        return authorLastnameOriginal;
    }

    public void setAuthorLastnameOriginal(String authorLastnameOriginal) {
        this.authorLastnameOriginal = authorLastnameOriginal;
    }

    public String getAuthorFirstnameTransliterated() {
        return authorFirstnameTransliterated;
    }

    public void setAuthorFirstnameTransliterated(String authorFirstnameTransliterated) {
        this.authorFirstnameTransliterated = authorFirstnameTransliterated;
    }

    public String getAuthorLastnameTransliterated() {
        return authorLastnameTransliterated;
    }

    public void setAuthorLastnameTransliterated(String authorLastnameTransliterated) {
        this.authorLastnameTransliterated = authorLastnameTransliterated;
    }

    public String getPlaceOfPublicationGerman() {
        return placeOfPublicationGerman;
    }

    public void setPlaceOfPublicationGerman(String placeOfPublicationGerman) {
        this.placeOfPublicationGerman = placeOfPublicationGerman;
    }

    public String getPlaceOfPublicationEnglish() {
        return placeOfPublicationEnglish;
    }

    public void setPlaceOfPublicationEnglish(String placeOfPublicationEnglish) {
        this.placeOfPublicationEnglish = placeOfPublicationEnglish;
    }

    public String getPlaceOfPublicationOriginal() {
        return placeOfPublicationOriginal;
    }

    public void setPlaceOfPublicationOriginal(String placeOfPublicationOriginal) {
        this.placeOfPublicationOriginal = placeOfPublicationOriginal;
    }

    public String getPlaceOfPublicationTransliterated() {
        return placeOfPublicationTransliterated;
    }

    public void setPlaceOfPublicationTransliterated(String placeOfPublicationTransliterated) {
        this.placeOfPublicationTransliterated = placeOfPublicationTransliterated;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(String numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public String getShelfmark() {
        return shelfmark;
    }

    public void setShelfmark(String shelfmark) {
        this.shelfmark = shelfmark;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
}
