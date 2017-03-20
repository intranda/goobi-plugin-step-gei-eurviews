package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.SimpleMetadataObject;
import lombok.Data;

public @Data class Transcription {

    private Integer transcriptionID;
    private Integer prozesseID;
    private String language;
    private String languageCode;
    private String transcription;
    private String publisher = "Georg-Eckert-Institut";
    private String project = "WorldViews";
    private List<SimpleMetadataObject> translatorList = new ArrayList<SimpleMetadataObject>();
    
    private String approval = "Georg-Eckert-Institut";
    private String availability;
    private String licence;
    private SimpleMetadataObject currentObject;
    
    public Transcription(int processId) {
        this.prozesseID = processId;
    }
    
    public void addNewTranslator(SimpleMetadataObject trans) {
        translatorList.add(trans);
    }
    
   public void deleteTranslator() {
       if (currentObject != null && translatorList.contains(currentObject)) {
           translatorList.remove(currentObject);
       }
   }
    
   public void addTranslator() {
       translatorList.add(new SimpleMetadataObject(""));
   }
   
   public void setTranscription(String trancription) {
	   this.transcription = trancription;
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
