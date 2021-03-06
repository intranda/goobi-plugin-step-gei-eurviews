package de.intranda.goobi.model.resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.intranda.goobi.model.ComplexMetadataContainer;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import lombok.Data;

@Data
public class ResouceMetadata implements ComplexMetadataContainer {

    private Integer id;
    private Integer processId;
    private Integer bibliographicDataId;
    private BibliographicMetadata bibliographicData = null;

    // Quellentyp
    private List<SimpleMetadataObject> resourceTypes = new ArrayList<>();

    //    Titel
    private TitleInfo resourceTitle = new TitleInfo();
//    private String resourceTitleOriginal;
//    private String resourceTitleLanguage;
//    //    Übersetzung (de)
//    private String resourceTitleGerman;
//    //    Übersetzung (en)
//    private String resourceTitleEnglish;

    private List<Person> resourceAuthorList = new ArrayList<>();
    
    //Datum (Jahr?) der digitalen Veröffentlichung
    private String publicationYearDigital;

    //    Seitenzahl Seitenbereich
    //    - Von
    private String startPage;
    //    - Bis
    private String endPage;

    //    Zulieferung durch
    private String supplier;
    
    private List<String> digitalCollections = new ArrayList<String>();

    private ComplexMetadataObject currentNormdataObject;
    private SimpleMetadataObject currentObject;

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

    public void addResourceType() {
        this.resourceTypes.add(new SimpleMetadataObject(""));
    }
    
    public void addResourceType(SimpleMetadataObject type) {
        this.resourceTypes.add(type);
    }
    
    public void deleteResourceType() {
        if (currentObject != null && resourceTypes.contains(currentObject)) {
            resourceTypes.remove(currentObject);
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
    
    public List<Person> getResourceAuthorList() {
        return resourceAuthorList;
    }

    @Override
    public void deleteMetadata(ComplexMetadataObject metadata) {
        resourceAuthorList.remove(metadata);
        
    }

    @Override
    public ComplexMetadataObject getCurrentMetadata() {
       return currentNormdataObject;
    }

    @Override
    public void setCurrentMetadata(ComplexMetadataObject metadata) {
        this.currentNormdataObject = metadata;
        
    }
 }
