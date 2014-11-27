package de.intranda.goobi.model.resource;

public class Transcription {

    private Integer prozesseID;
    private Integer transcriptionID;
    private String language;
    private String transcription;
    private String author;
    private String imageName;
    
    public Transcription(int processId) {
        this.prozesseID = processId;
    }
    
    public Integer getProzesseID() {
        return prozesseID;
    }
    public Integer getTranscriptionID() {
        return transcriptionID;
    }
    public String getLanguage() {
        return language;
    }
    public String getTranscription() {
        return transcription;
    }
    public String getAuthor() {
        return author;
    }
    public void setProzesseID(Integer prozesseID) {
        this.prozesseID = prozesseID;
    }
    public void setTranscriptionID(Integer transcriptionID) {
        this.transcriptionID = transcriptionID;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    
}
