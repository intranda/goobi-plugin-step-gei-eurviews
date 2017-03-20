package de.intranda.goobi.model.resource;

import org.apache.commons.lang.StringUtils;

import lombok.Data;

public @Data class Context {

    private Integer processID;
    private Integer descriptionID;
    private String language;
    private String languageCode;
    private String bookInformation;
    private String shortDescription;
    private String longDescription;
    private String projectContext;
    private String selectionMethod;

    
    public Context(int processId) {
        this.processID = processId;
    }
    
    public Context(int processId, String language) {
        this.processID = processId;
        this.language = language;
    }
    
    public String getLanguageCode() {
 	   if(isOriginalLanguage()) {
 		   if(StringUtils.isNotBlank(languageCode)) { 			   
 			   return languageCode;
 		   } else {
 			   return "original";
 		   }
 	   } else {
 		   return language;
 	   }
    }
    
    public boolean isOriginalLanguage() {
 	   return "original".equalsIgnoreCase(language);
    }
    
    public void setOriginalLanguage(boolean original) {
 	   if(original) {		   
 		   if(StringUtils.isBlank(languageCode)) {
 			   languageCode = language;
 		   }
 		   language = "original";
 	   } else {
 		   if(!StringUtils.isBlank(languageCode)) {
 			   language = languageCode;
 		   }
 		   languageCode = null;
 	   }
    }

}
