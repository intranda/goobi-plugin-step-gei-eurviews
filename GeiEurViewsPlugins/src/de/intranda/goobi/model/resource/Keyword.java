package de.intranda.goobi.model.resource;

public class Keyword {

    private Integer processId;
    private Integer keywordId;
    private String externalId;
    private String label;

    public Keyword(Integer processId) {
        this.processId = processId;
    }
    
    public Integer getKeywordId() {
        return keywordId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getLabel() {
        return label;
    }

    public void setKeywordId(Integer keywordId) {
        this.keywordId = keywordId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getProcessId() {
        return processId;
    }
    
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }
    
    /* 
    CREATE TABLE `goobi`.`keyword` (
    `keywordID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `externalID` varchar(255) DEFAULT NULL,
    `label` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`keywordID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */
    
    
}
