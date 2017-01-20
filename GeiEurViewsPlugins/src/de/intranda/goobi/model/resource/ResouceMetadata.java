package de.intranda.goobi.model.resource;

import java.util.ArrayList;
import java.util.List;


import de.intranda.goobi.model.Person;
import lombok.Data;

@Data
public class ResouceMetadata {

    
    private Integer id;
    private Integer processId;
    private Integer bibliographicDataId;
    
    // Quellentyp
    private String resourceType;

    //    Titel
    private String resourceTitleOriginal;
    //    Übersetzung (de)
    private String resourceTitleGerman;
    //    Übersetzung (en)
    private String resourceTitleEnglish;

    private List<Person> resourceAuthorList = new ArrayList<>();

    //    Seitenzahl Seitenbereich
    //    - Von
    private String startPage;
    //    - Bis
    private String endPage;

    //    Zulieferung durch
    private String supplier;
    
    private Person currentPerson;


    public void addToResourceAuthorList(Person per) {
        resourceAuthorList.add(per);
    }

    public void addNewResourceAuthor() {
        Person per = new Person();
        resourceAuthorList.add(per);
    }

    public void deleteResourceAuthor() {
        if (currentPerson != null && resourceAuthorList.contains(currentPerson)) {
            resourceAuthorList.remove(currentPerson);
        }
    }
    
    
//    authorList = data.getResourceAuthorList();
//    for (Person author : authorList) {
//        insertMetadata(run, connection, data.getResourceID(), data.getProzesseID(), "resource", author);
//    }

    
    
//    List<Person> res = new QueryRunner().query(connection, metadata, DatabaseManager.resultSetToPersonListHandler, resourceAuthor);
//    data.setResourceAuthorList(res);

}
