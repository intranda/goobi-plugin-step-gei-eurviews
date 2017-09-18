package de.intranda.goobi.model.annotation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.model.ComplexMetadataContainer;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Person;
import lombok.Data;

@Data
public class AnnotationMetadata implements ComplexMetadataContainer{

    private Integer id = null;
    private int processId;

    private Person currentPerson;
    private List<Person> authorList = new ArrayList<>();

    private String contributionType = "";
    private String edition;
    private String publisher = "Georg-Eckert-Institut";
    private String project = "WorldViews";
    private String availability;
    private String licence = "CC BY-NC-ND 3.0 DE";
    private String publicationYearDigital;
    
    private List<String> digitalCollections = new ArrayList<String>();

 
    @Override
    public void deleteMetadata(ComplexMetadataObject metadata) {
        if (authorList.contains(metadata)) {
            authorList.remove(metadata);
        }
    }
    
    @Override
    public ComplexMetadataObject getCurrentMetadata() {
        return getCurrentPerson();
    }
    
    @Override
    public void setCurrentMetadata(ComplexMetadataObject metadata) {
        if(metadata instanceof Person) {            
            setCurrentPerson((Person) metadata);
        } else {
            throw new IllegalArgumentException("Metadata must be of type Person");
        }
    }
    
    public void addAuthor() {
        authorList.add(new Person());
    }

    public void deletePerson() {
        if (authorList.contains(currentPerson)) {
            authorList.remove(currentPerson);
        }
    }

}
