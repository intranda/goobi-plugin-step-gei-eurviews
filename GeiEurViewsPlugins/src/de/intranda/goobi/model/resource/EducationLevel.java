package de.intranda.goobi.model.resource;

import java.util.Arrays;
import java.util.List;

public enum EducationLevel  {
    primary("Primärstufe", "Primarstufe"),
    secondary1("Sekundarstufe 1"),
    secondary2("Sekundarstufe 2"),
    tertiary("Tertiärbereich"),
    otherEducationLevel("nicht zuzuordnen");
    
    private List<String> aliases;
    
    private EducationLevel(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }
    
    public boolean matches(String alias) {
        return aliases.contains(alias);
    }
    
    public static EducationLevel getEducationLevel(String alias) {
        for (EducationLevel level : EducationLevel.values()) {
            if(level.matches(alias)) {
                return level;
            }
        }
        return null;
    }
    
}
