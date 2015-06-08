package de.intranda.goobi.model.annotation;

import de.intranda.goobi.model.resource.BibliographicData;


public class Source {

    // foreign key resource table
    private Integer resourceId;
    // foreign key process table
    private Integer processId;
    // main title resource table
    private BibliographicData data;
    
    private boolean isMainSource;
    
    
    public Source(int processid) {
        this.processId = processid; 
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public Integer getProcessId() {
        return processId;
    }

    public BibliographicData getData() {
        return data;
    }

    public boolean isMainSource() {
        return isMainSource;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public void setData(BibliographicData data) {
        this.data = data;
    }

    public void setMainSource(boolean isMainSource) {
        this.isMainSource = isMainSource;
    }

    
    
}
