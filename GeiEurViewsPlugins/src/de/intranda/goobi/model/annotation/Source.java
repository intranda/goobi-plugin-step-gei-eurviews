package de.intranda.goobi.model.annotation;

public class Source {

    // foreign key resource table
    private Integer resourceId;
    // foreign key process table
    private Integer processId;
    // main title resource table
    private String title;

    private boolean isMainSource;

    public Integer getResourceId() {
        return resourceId;
    }

    public Integer getProcessId() {
        return processId;
    }

    public String getTitle() {
        return title;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMainSource(boolean isMainSource) {
        this.isMainSource = isMainSource;
    }

    
    
}
