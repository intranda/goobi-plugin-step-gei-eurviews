package de.intranda.goobi.model.resource;

public class Category {

    private Integer categoryId;
    private Integer processId;
    private String externalId;
    private String label;
    
    public Category(Integer processId) {
        this.processId = processId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Integer getProcessId() {
        return processId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getLabel() {
        return label;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
   
    
    /* 
    CREATE TABLE `goobi`.`category` (
    `categoryId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `externalID` varchar(255) DEFAULT NULL,
    `label` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`categoryId`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */
    
    
}
