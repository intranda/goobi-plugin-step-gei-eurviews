package de.intranda.goobi.model.resource;

import lombok.Data;

public @Data class Context {

    private Integer processID;
    private Integer descriptionID;
    private String language;
    private String bookInformation;
    private String shortDescription;
    private String longDescription;

    
    public Context(int processId) {
        this.processID = processId;
    }
    
    public Context(int processId, String language) {
        this.processID = processId;
        this.language = language;
    }

}
