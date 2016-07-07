package de.intranda.goobi.model.annotation;

import java.util.ArrayList;
import java.util.List;

import de.intranda.goobi.model.resource.Topic;
import lombok.Data;

public @Data class Contribution {

    private Integer annotationId = null;

    private Integer processId;

    //    Titel   1,1 Der Titel des Beitrages.    Freitextfeld        
    private String title;
    //    Übersetzer  0,* Falls der Text übersetzt wurde, dann wird hier der Übersetzer in freier Form eingetragen.   Freitextfeld        Ein weiteres Feld kann mit einem Pluszeichen erzeugt und mit einem Mülleimerzeichen entfernt werden.
    private String translator;
    //    Sprache 1,1 Die Sprache des Beitragstexts.  Drop-Down-Liste     Gibt es standardisierte Listen, die hier eingebunden werden können?Bei Abby-OCR-Daten automatischer Eintrag der Sprache.
    private String language;
    //    Abstract    0,1 Kurzer Abstract zum Beitrag.    einfacher RT-Editor     standardmäßig ausgeblendet                   
    private String abstrakt;
    //    Inhalt  1,1 Der Text des Beitrags   einfacher RT-Editor     
    private String content;
    //    Anmerkungen 0,1 Anmerkungen zum Beitrag.    einfacher RT-Editor     Anmerkungen können später in verschiedenen Varianten formatiert werden, also als Fußnoten, Endnoten, Randnotizen etc.
    private String reference;
    //    Literaturangaben    0,1 Literaturangaben zum Beitrag.   einfacher RT-Editor     
    private String footnote;
    //    Themenfelder / Schlagwörter 1,* Themenfelder mit zugeordneten Schlagworten mit denen der Quellenauszug kontextualisiert wird.   ein Tab pro ThemenfeldSchlagworte können markiert werden    Im Fall von Essays und Kommentaren sollen alle Schlagworte aus den referenzierten Quellen hier vorausgewählt sein! Bildungsgeschichten "erben" nichts.  Entspricht den Themenfeldern/Schlagwörtern im Reiter "Kontext" im Modul "Quellenbeschreibung".
    private List<Topic> topicList = new ArrayList<>();
    // TODO 1.) initialisieren, 2.) Topics aus 

    public Contribution(int processId) {
        this.processId = processId;
    }

}
