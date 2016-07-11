package de.intranda.goobi.model.annotation;

import lombok.Data;
import de.intranda.goobi.model.resource.BibliographicData;

import de.intranda.goobi.plugins.ResourceAnnotationPlugin;
import de.sub.goobi.helper.Helper;

public @Data class Source {

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
        if (data != null && (this.data == null || this.data.getProzesseID() != data.getProzesseID())) {
            this.data = data;

            ResourceAnnotationPlugin plugin = (ResourceAnnotationPlugin) Helper.getManagedBeanValue("#{AktuelleSchritteForm.myPlugin}");
            if (plugin != null) {
                plugin.updateKeywordList(data.getProzesseID());
            }
        }

    }

    public void setMainSource(boolean isMainSource) {
        this.isMainSource = isMainSource;
    }

}
