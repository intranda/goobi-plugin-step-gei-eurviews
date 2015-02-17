CREATE TABLE `goobi`.`plugin_gei_eurviews_source`
  (
     `resourceid` INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid` INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `data`       VARCHAR(255) DEFAULT NULL,
     `mainsource` BIT(1) DEFAULT false,
     PRIMARY KEY (`resourceid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  
    
CREATE TABLE `goobi`.`plugin_gei_eurviews_author`
  (
     `authorid`     INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid`   INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `name`         VARCHAR(255) DEFAULT NULL,
     `organization` VARCHAR(255) DEFAULT NULL,
     `mail`         VARCHAR(255) DEFAULT NULL,
     `url`          VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`authorid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  


CREATE TABLE `goobi`.`plugin_gei_eurviews_annotation`
  (
     `annotationid` INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid`   INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `title`        VARCHAR(255) DEFAULT NULL,
     `language`     VARCHAR(255) DEFAULT NULL,
     `content`      TEXT DEFAULT NULL,
     `translator`   VARCHAR(255) DEFAULT NULL,
     `reference`    TEXT DEFAULT NULL,
     PRIMARY KEY (`annotationid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8; 
     
CREATE TABLE `goobi`.`plugin_gei_eurviews_categories`
  (
     `catid`   INT(10) UNSIGNED NOT NULL auto_increment,
     `german`  VARCHAR(255) DEFAULT NULL,
     `english` VARCHAR(255) DEFAULT NULL,
     `french`  VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`catid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  
    
CREATE TABLE `goobi`.`plugin_gei_eurviews_keywords`
  (
     `keyid`   INT(10) UNSIGNED NOT NULL auto_increment,
     `german`  VARCHAR(255) DEFAULT NULL,
     `english` VARCHAR(255) DEFAULT NULL,
     `french`  VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`keyid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  
    
CREATE TABLE `goobi`.`plugin_gei_eurviews_category`
  (
     `categoryid` INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid` INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `value`      VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`categoryid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  
    
CREATE TABLE `goobi`.`plugin_gei_eurviews_keyword`
  (
     `keywordid`  INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid` INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `value`      VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`keywordid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  
    
CREATE TABLE `goobi`.`plugin_gei_eurviews_image`
  (
     `imageid`        INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid`     INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `filename`       VARCHAR(255) DEFAULT NULL,
     `sequence`       INT(10) UNSIGNED NULL DEFAULT NULL,
     `structtype`     VARCHAR(255) DEFAULT NULL,
     `displayimage`   BIT(1) DEFAULT false,
     `licence`        VARCHAR(255) DEFAULT NULL,
     `representative` BIT(1) DEFAULT false,
     PRIMARY KEY (`imageid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;  

    
CREATE TABLE `goobi`.`plugin_gei_eurviews_description`
  (
     `descriptionid`    INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid`       INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `language`         VARCHAR(255) DEFAULT NULL,
     `title`            VARCHAR(255) DEFAULT NULL,
     `shortdescription` TEXT DEFAULT NULL,
     `longdescription`  TEXT DEFAULT NULL,
     `originallanguage` BIT(1) DEFAULT false,
     PRIMARY KEY (`descriptionid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;

CREATE TABLE `goobi`.`plugin_gei_eurviews_resource`
  (
     `resourceid`         INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid`         INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `documenttype`       VARCHAR(255) DEFAULT NULL,
     `maintitle`          VARCHAR(255) DEFAULT NULL,
     `subtitle`           VARCHAR(255) DEFAULT NULL,
     `authorfirstname`    VARCHAR(255) DEFAULT NULL,
     `authorlastname`     VARCHAR(255) DEFAULT NULL,
     `language`           VARCHAR(255) DEFAULT NULL,
     `publisher`          VARCHAR(255) DEFAULT NULL,
     `placeofpublication` VARCHAR(255) DEFAULT NULL,
     `publicationyear`    VARCHAR(255) DEFAULT NULL,
     `numberofpages`      VARCHAR(255) DEFAULT NULL,
     `shelfmark`          VARCHAR(255) DEFAULT NULL,
     `copyright`          VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`resourceid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;

CREATE TABLE `goobi`.`plugin_gei_eurviews_transcription`
  (
     `transcriptionid` INT(10) UNSIGNED NOT NULL auto_increment,
     `prozesseid`      INT(10) UNSIGNED NOT NULL DEFAULT '0',
     `language`        VARCHAR(255) DEFAULT NULL,
     `transcription`   TEXT DEFAULT NULL,
     `filename`        VARCHAR(255) DEFAULT NULL,
     `author`          VARCHAR(255) DEFAULT NULL,
     PRIMARY KEY (`transcriptionid`),
     KEY `prozesseid` (`prozesseid`)
  )
engine = innodb
DEFAULT CHARACTER SET = utf8;   