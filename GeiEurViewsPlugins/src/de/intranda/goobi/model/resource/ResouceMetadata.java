package de.intranda.goobi.model.resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.intranda.goobi.model.Person;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import lombok.Data;

@Data
public class ResouceMetadata {

    private Integer id;
    private Integer processId;
    private Integer bibliographicDataId;
    private BibliographicMetadata bibliographicData = null;

    // Quellentyp
    private String resourceType;

    //    Titel
    private String resourceTitleOriginal;
    private String resourceTitleLanguage;
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
    
    private List<String> digitalCollections = new ArrayList<String>();

    private Person currentPerson;

    public ResouceMetadata(Integer processId) {
        this.processId = processId;
    }

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
    
    public BibliographicMetadata getBibliographicData() {
    	if(this.bibliographicData == null) {
    		try {
				this.bibliographicData = WorldViewsDatabaseManager.getBibliographicData(getBibliographicDataId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	return this.bibliographicData;
    }
}
