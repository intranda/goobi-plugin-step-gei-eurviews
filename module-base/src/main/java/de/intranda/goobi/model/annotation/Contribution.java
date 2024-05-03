package de.intranda.goobi.model.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.SimpleMetadataObject;
import lombok.Data;

public @Data class Contribution {

    private Integer contributionId = null;

    private Integer processId;
    

    //    Titel   1,1 Der Titel des Beitrages.    Freitextfeld        
    private String title;

    //    Übersetzer  0,* Falls der Text übersetzt wurde, dann wird hier der Übersetzer in freier Form eingetragen.   Freitextfeld        Ein weiteres Feld kann mit einem Pluszeichen erzeugt und mit einem Mülleimerzeichen entfernt werden.
    private List<SimpleMetadataObject> translatorList = new ArrayList<>();
    private SimpleMetadataObject currentObject;

    //    Sprache 1,1 Die Sprache des Beitragstexts.  Drop-Down-Liste     Gibt es standardisierte Listen, die hier eingebunden werden können?Bei Abby-OCR-Daten automatischer Eintrag der Sprache.
    private String language;
    private String languageCode;

    //    Abstract    0,1 Kurzer Abstract zum Beitrag.    einfacher RT-Editor     standardmäßig ausgeblendet                   
    private String abstrakt;

    //    Inhalt  1,1 Der Text des Beitrags   einfacher RT-Editor     
    private String content;

    //    Anmerkungen 0,1 Anmerkungen zum Beitrag.    einfacher RT-Editor     Anmerkungen können später in verschiedenen Varianten formatiert werden, also als Fußnoten, Endnoten, Randnotizen etc.
    private String context;
    
    public Contribution(int processId) {
        this.processId = processId;
    }

    public void addTranslator(SimpleMetadataObject translator) {
        this.translatorList.add(translator);
    }

    public void deleteTranslator() {
        if (currentObject != null && translatorList.contains(currentObject)) {
            translatorList.remove(currentObject);
        }
    }

    public void addTranslator() {
        translatorList.add(new SimpleMetadataObject(""));
    }

    public String getLanguageCode() {
 	   if(isOriginalLanguage()) {
 		   if(StringUtils.isNotBlank(languageCode)) { 			   
  			   return languageCode;
  		   } else {
  			   return "";
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
