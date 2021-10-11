package de.intranda.goobi.model.resource;

import java.util.Arrays;
import java.util.List;

public enum SchoolSubject {
    history("Geschichte"),
    geography("Erdkunde"),
    socialstudies("Sozialkunde/Politik"),
    other("Sonstige");
    
    private List<String> aliases;
    
    private SchoolSubject(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }
    
    public boolean matches(String alias) {
        return aliases.contains(alias);
    }
    
    public static SchoolSubject getSchoolSubject(String alias) {
        for (SchoolSubject subject : SchoolSubject.values()) {
            if(subject.matches(alias)) {
                return subject;
            }
        }
        return null;
    }
}
