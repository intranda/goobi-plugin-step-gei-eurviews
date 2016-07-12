package de.intranda.goobi.model.annotation;

import java.util.ArrayList;
import java.util.List;

import de.intranda.goobi.model.SimpleMetadataObject;
import lombok.Data;

public @Data class Contribution {

    private Integer contributionId = null;

    private Integer processId;

    //    Titel   1,1 Der Titel des Beitrages.    Freitextfeld        
    private String titleOriginal;
    private String titleTranslation;

    //    Übersetzer  0,* Falls der Text übersetzt wurde, dann wird hier der Übersetzer in freier Form eingetragen.   Freitextfeld        Ein weiteres Feld kann mit einem Pluszeichen erzeugt und mit einem Mülleimerzeichen entfernt werden.
    private List<SimpleMetadataObject> translatorListOriginal = new ArrayList<>();
    private List<SimpleMetadataObject> translatorListTranslation = new ArrayList<>();
    private SimpleMetadataObject currentObject;

    //    Sprache 1,1 Die Sprache des Beitragstexts.  Drop-Down-Liste     Gibt es standardisierte Listen, die hier eingebunden werden können?Bei Abby-OCR-Daten automatischer Eintrag der Sprache.
    private String languageOriginal;
    private String languageTranslation;

    //    Abstract    0,1 Kurzer Abstract zum Beitrag.    einfacher RT-Editor     standardmäßig ausgeblendet                   
    private String abstractOriginal;
    private String abstractTranslation;

    //    Inhalt  1,1 Der Text des Beitrags   einfacher RT-Editor     
    private String contentOriginal;
    private String contentTranslation;

    //    Anmerkungen 0,1 Anmerkungen zum Beitrag.    einfacher RT-Editor     Anmerkungen können später in verschiedenen Varianten formatiert werden, also als Fußnoten, Endnoten, Randnotizen etc.
    private String noteOriginal;
    private String noteTranslation;

    //    Literaturangaben    0,1 Literaturangaben zum Beitrag.   einfacher RT-Editor     
    private String referenceOriginal;
    private String referenceTranslation;

    public Contribution(int processId) {
        this.processId = processId;
    }

    public void addTranslatorOriginal(SimpleMetadataObject translator) {
        this.translatorListOriginal.add(translator);
    }

    public void deleteTranslatorOriginal() {
        if (currentObject != null && translatorListOriginal.contains(currentObject)) {
            translatorListOriginal.remove(currentObject);
        }
    }

    public void addTranslatorOriginal() {
        translatorListOriginal.add(new SimpleMetadataObject(""));
    }

    public void addTranslatorTranslation(SimpleMetadataObject translator) {
        this.translatorListTranslation.add(translator);
    }

    public void deleteTranslatorTranslation() {
        if (currentObject != null && translatorListTranslation.contains(currentObject)) {
            translatorListTranslation.remove(currentObject);
        }
    }

    public void addTranslatorTranslation() {
        translatorListTranslation.add(new SimpleMetadataObject(""));
    }

}
