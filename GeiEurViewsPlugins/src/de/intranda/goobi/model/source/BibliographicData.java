package de.intranda.goobi.model.source;

import java.util.ArrayList;
import java.util.List;

public class BibliographicData {

    private String documentType;
    private String maintitle;
    private String subtitle;
    private String authorFirstname;
    private String authorLastname;
    private String language;
    private String publisher;
    private String placeOfPublication;
    private String publicationYear;
    private String numberOfPages;
    private String shelfmark;
    private String copyright;
    
    public String getDocumentType() {
        return documentType;
    }
    public String getLanguage() {
        return language;
    }
    public String getAuthorFirstname() {
        return authorFirstname;
    }
    public String getAuthorLastname() {
        return authorLastname;
    }
    public String getMaintitle() {
        return maintitle;
    }
    public String getSubtitle() {
        return subtitle;
    }
    public String getPublisher() {
        return publisher;
    }
    public String getPlaceOfPublication() {
        return placeOfPublication;
    }
    public String getPublicationYear() {
        return publicationYear;
    }
    public String getNumberOfPages() {
        return numberOfPages;
    }
    public String getShelfmark() {
        return shelfmark;
    }
    public String getCopyright() {
        return copyright;
    }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public void setAuthorFirstname(String authorFirstname) {
        this.authorFirstname = authorFirstname;
    }
    public void setAuthorLastname(String authorLastname) {
        this.authorLastname = authorLastname;
    }
    public void setMaintitle(String maintitle) {
        this.maintitle = maintitle;
    }
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    public void setPlaceOfPublication(String placeOfPublication) {
        this.placeOfPublication = placeOfPublication;
    }
    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }
    public void setNumberOfPages(String numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
    public void setShelfmark(String shelfmark) {
        this.shelfmark = shelfmark;
    }
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    
   public List<String> getPossibleDocumentTypes() {
       // TODO read from configuration
       List<String> answer = new ArrayList<String>();
       answer.add("Artikel");
       answer.add("Bild");
       answer.add("Kapitel");
       
       return answer;
   }
    
}
