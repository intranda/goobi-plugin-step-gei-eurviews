package de.intranda.goobi.model.annotation;

import lombok.Data;

import org.goobi.production.plugin.interfaces.IPlugin;

import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.plugins.ResourceAnnotationPlugin;
import de.sub.goobi.helper.Helper;

public @Data class Source {

    // foreign key resource table
    private Integer resourceId;
    // foreign key process table
    private Integer processId;
    // main title resource table
//    private BibliographicMetadata data;
    // source description resource table
    private ResouceMetadata data;

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

    public ResouceMetadata getData() {
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
    
    public void setData(ResouceMetadata data) {
        if (data != null && (this.data == null || this.data.getProcessId() != data.getProcessId())) {
            this.data = data;

            IPlugin plugin =  (IPlugin) Helper.getManagedBeanValue("#{AktuelleSchritteForm.myPlugin}");
            if(plugin != null && plugin instanceof ResourceAnnotationPlugin) {
            	((ResourceAnnotationPlugin) plugin).updateKeywordList(data.getProcessId());
            }
        }

    }

    public void setMainSource(boolean isMainSource) {
        this.isMainSource = isMainSource;
    }

}
