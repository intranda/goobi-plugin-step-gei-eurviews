package de.intranda.goobi.model.annotation;


public class Source {

    // foreign key resource table
    private Integer resourceId;
    // foreign key process table
    private Integer processId;
    // main title resource table
    private String data;

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

    public String getData() {
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

    public void setData(String data) {
        this.data = data;
    }

    public void setMainSource(boolean isMainSource) {
        this.isMainSource = isMainSource;
    }

    
    
}
