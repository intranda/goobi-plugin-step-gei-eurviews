
    CREATE TABLE `goobi`.`plugin_gei_eurviews_source` (
    `resourceId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `data` varchar(255) DEFAULT NULL,
    `mainsource` bit(1) DEFAULT false,
    PRIMARY KEY (`resourceId`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_author` (
    `authorID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `name` varchar(255) DEFAULT NULL,
    `organization` varchar(255) DEFAULT NULL,
    `mail` varchar(255) DEFAULT NULL,
    `url` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`authorID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_annotation` (
    `annotationID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `title` varchar(255) DEFAULT NULL,
    `language` varchar(255) DEFAULT NULL,
    `content` text DEFAULT NULL,
    `translator` varchar(255) DEFAULT NULL,
    `reference` text DEFAULT NULL,
    PRIMARY KEY (`annotationID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_categories` (
    `catId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `german` varchar(255) DEFAULT NULL,
    `english` varchar(255) DEFAULT NULL,
    `french` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`catId`)
     ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_keywords` (
    `keyId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `german` varchar(255) DEFAULT NULL,
    `english` varchar(255) DEFAULT NULL,
    `french` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`keyId`)
     ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_category` (
    `categoryId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`categoryId`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_keyword` (
    `keywordID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`keywordID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_image` (
    `imageID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `fileName` varchar(255) DEFAULT NULL,
    `sequence` int(10) unsigned NULL DEFAULT NULL,
    `structType` varchar(255) DEFAULT NULL,
    `displayImage` bit(1) DEFAULT false,
    `licence` varchar(255) DEFAULT NULL,
    `representative` bit(1) DEFAULT false,
    PRIMARY KEY (`imageID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_description` (
    `descriptionID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `language` varchar(255) DEFAULT NULL,
    `title` varchar(255) DEFAULT NULL,
    `shortDescription` text DEFAULT NULL,
    `longDescription` text DEFAULT NULL,
    `originalLanguage` bit(1) DEFAULT false,
    PRIMARY KEY (`descriptionID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_resource` (
    `resourceID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `documentType` varchar(255) DEFAULT NULL,
    `maintitle` varchar(255) DEFAULT NULL,
    `subtitle` varchar(255) DEFAULT NULL,
    `authorFirstname` varchar(255) DEFAULT NULL,
    `authorLastname` varchar(255) DEFAULT NULL,
    `language` varchar(255) DEFAULT NULL,
    `publisher` varchar(255) DEFAULT NULL,
    `placeOfPublication` varchar(255) DEFAULT NULL,
    `publicationYear` varchar(255) DEFAULT NULL,
    `numberOfPages` varchar(255) DEFAULT NULL,
    `shelfmark` varchar(255) DEFAULT NULL,
    `copyright` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`resourceID`),
    KEY `prozesseID` (`prozesseID`)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_transcription` (
    `transcriptionID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `language` varchar(255) DEFAULT NULL,
    `transcription` text DEFAULT NULL,
    `fileName` varchar(255) DEFAULT NULL,
    `author` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`transcriptionID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

    alter table `goobi`.`plugin_gei_eurviews_author` add column mail2 varchar(255) default null;
    alter table `goobi`.`plugin_gei_eurviews_author` add column mail3 varchar(255) default null;
    
    alter table `goobi`.`plugin_gei_eurviews_source` MODIFY data INTEGER;
    
    alter table `goobi`.`plugin_gei_eurviews_annotation` add column classification varchar(255) default null;
    alter table `goobi`.`plugin_gei_eurviews_annotation` add column footnote text default null;
     
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` CHANGE maintitle maintitleOriginal varchar(255);
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` CHANGE subtitle subtitleOriginal varchar(255);          
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` CHANGE authorFirstname authorFirstnameOriginal varchar(255);
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` CHANGE authorLastname authorLastnameOriginal varchar(255);
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` CHANGE placeOfPublication placeOfPublicationOriginal varchar(255);
    
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column maintitleGerman varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column subtitleGerman varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column authorFirstnameGerman varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column authorLastnameGerman varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column placeOfPublicationGerman varchar(255) default null;
    
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column maintitleEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column subtitleEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column authorFirstnameEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column authorLastnameEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column placeOfPublicationEnglish varchar(255) default null;
    
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column maintitleTransliterated varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column subtitleTransliterated varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column authorFirstnameTransliterated varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column authorLastnameTransliterated varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column placeOfPublicationTransliterated varchar(255) default null;
    
     ALTER TABLE `goobi`.`plugin_gei_eurviews_keyword` add column category varchar(255) default null;

    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorFirstnameOriginal;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorLastnameOriginal;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN placeOfPublicationOriginal;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN publisher;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN language;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorFirstnameGerman;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorLastnameGerman;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN placeOfPublicationGerman;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN maintitleEnglish;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN subtitleEnglish;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorFirstnameEnglish;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorLastnameEnglish;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN placeOfPublicationEnglish;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN maintitleTransliterated;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN subtitleTransliterated;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorFirstnameTransliterated;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN authorLastnameTransliterated;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN placeOfPublicationTransliterated;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN subtitleGerman;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` DROP COLUMN copyright;
    
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column maintitleEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column placeOfPublication varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column volumeTitleOriginal varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column volumeTitleGerman varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column volumeTitleEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column volumeNumber varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column schoolSubject varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column educationLevel varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column edition varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column isbn varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column physicalLocation varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column resourceType varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column resourceTitleOriginal varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column resourceTitleGerman varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column resourceTitleEnglish varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column startPage varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column endPage varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_resource` add column supplier varchar(255) default null;
    
    CREATE TABLE `goobi`.`plugin_gei_eurviews_resource_stringlist` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `resourceID` int(10) unsigned NOT NULL DEFAULT '0',
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `type` varchar(255) DEFAULT NULL,
    `data` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `resourceID` (`resourceID`),
    KEY `prozesseID` (`prozesseID`)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
 
    CREATE TABLE `goobi`.`plugin_gei_eurviews_resource_metadatalist` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `resourceID` int(10) unsigned NOT NULL DEFAULT '0',
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `type` varchar(255) DEFAULT NULL,
    `role` varchar(255) DEFAULT NULL,
    `normdataAuthority` varchar(255) DEFAULT NULL,
    `normdataValue` varchar(255) DEFAULT NULL,
    `firstValue` varchar(255) DEFAULT NULL,
    `secondValue` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `resourceID` (`resourceID`),
    KEY `prozesseID` (`prozesseID`)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;

    ALTER TABLE `goobi`.`plugin_gei_eurviews_image` add column copyright varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_image` add column resolution varchar(255) default null;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_image` add column placeholder varchar(255) default null;
    
    ALTER TABLE `goobi`.`plugin_gei_eurviews_description` DROP COLUMN title;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_description` DROP COLUMN originalLanguage;
    ALTER TABLE `goobi`.`plugin_gei_eurviews_description` add column bookInformation text default null;
    RENAME TABLE `goobi`.`plugin_gei_eurviews_description` TO `goobi`.`plugin_gei_eurviews_context`;
    
    drop table plugin_gei_eurviews_keyword;
    drop table plugin_gei_eurviews_keywords;
    drop table plugin_gei_eurviews_category;
    drop table plugin_gei_eurviews_categories;
    
    
     CREATE TABLE `goobi`.`plugin_gei_eurviews_keyword` (
    `keywordID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `topic` varchar(255) DEFAULT NULL,
    `keyword` varchar(255) DEFAULT false,
    PRIMARY KEY (`keywordID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
    
    ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` DROP COLUMN fileName;
    
     ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column publisher varchar(255) default null;
     ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column project varchar(255) default null;
     ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column approval varchar(255) default null;
     ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column availability varchar(255) default null;
     ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column licence varchar(255) default null;
    
    
    
       drop table plugin_gei_eurviews_annotation;   
    
    CREATE TABLE `goobi`.`plugin_gei_eurviews_contributiondescription` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `contributionType` varchar(255) DEFAULT NULL,
    `edition` varchar(255) DEFAULT NULL,
    `publisher` varchar(255) DEFAULT NULL,
    `project` varchar(255) DEFAULT NULL,
    `availability` varchar(255) DEFAULT NULL,
    `licence` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `prozesseID` (`prozesseID`)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
    
    
    CREATE TABLE `goobi`.`plugin_gei_eurviews_contribution` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `processId` int(10) unsigned NOT NULL DEFAULT '0',
    `titleOriginal` varchar(255) DEFAULT NULL,
    `titleTranslation` varchar(255) DEFAULT NULL,
    `languageOriginal` varchar(255) DEFAULT NULL,
    `languageTranslation` varchar(255) DEFAULT NULL, 
     `abstractOriginal` text DEFAULT NULL,
    `abstractTranslation` text DEFAULT NULL,
    `contentOriginal` text DEFAULT NULL,
    `contentTranslation` text DEFAULT NULL,
    `noteOriginal` text DEFAULT NULL,
    `noteTranslation` text DEFAULT NULL,
    `referenceOriginal` text DEFAULT NULL,
    `referenceTranslation` text DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `prozesseID` (`processId`)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;

    CREATE TABLE `goobi`.`plugin_gei_eurviews_languages` (
    `isoCode` char(2) NOT NULL,
    `englishName` varchar(255) DEFAULT NULL,
    `frenchName` varchar(255) DEFAULT NULL,
    `germanName` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`isoCode`)
    )ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
    
    insert into plugin_gei_eurviews_languages (isoCode,englishName,frenchName,germanName) VALUES ("aa", "Afar", "afar", "Danakil-Sprache"),
("ab", "Abkhazian", "abkhaze", "Abchasisch"),
("af", "Afrikaans", "afrikaans", "Afrikaans"),
("ak", "Akan", "akan", "Akan-Sprache"),
("sq", "Albanian", "albanais", "Albanisch"),
("am", "Amharic", "amharique", "Amharisch"),
("ar", "Arabic", "arabe", "Arabisch"),
("an", "Aragonese", "aragonais", "Aragonesisch"),
("hy", "Armenian", "arménien", "Armenisch"),
("as", "Assamese", "assamais", "Assamesisch"),
("av", "Avaric", "avar", "Awarisch"),
("ae", "Avestan", "avestique", "Avestisch"),
("ay", "Aymara", "aymara", "Aymará-Sprache"),
("az", "Azerbaijani", "azéri", "Aserbeidschanisch"),
("ba", "Bashkir", "bachkir", "Baschkirisch"),
("bm", "Bambara", "bambara", "Bambara-Sprache"),
("eu", "Basque", "basque", "Baskisch"),
("be", "Belarusian", "biélorusse", "Weißrussisch"),
("bn", "Bengali", "bengali", "Bengali"),
("bh", "Bihari languages", "langues biharis", "Bihari (Andere)"),
("bi", "Bislama", "bichlamar", "Beach-la-mar"),
("bo", "Tibetan", "tibétain", "Tibetisch"),
("bs", "Bosnian", "bosniaque", "Bosnisch"),
("br", "Breton", "breton", "Bretonisch"),
("bg", "Bulgarian", "bulgare", "Bulgarisch"),
("my", "Burmese", "birman", "Birmanisch"),
("ca", "Catalan, Valencian", "catalan, valencien", "Katalanisch"),
("cs", "Czech", "tchèque", "Tschechisch"),
("ch", "Chamorro", "chamorro", "Chamorro-Sprache"),
("ce", "Chechen", "tchétchène", "Tschetschenisch"),
("zh", "Chinese", "chinois", "Chinesisch"),
("cu", "Church Slavic, Old Slavonic, Church Slavonic, Old Bulgarian, Old Church Slavonic", "slavon d\'église, vieux slave, slavon liturgique, vieux bulgare", "Kirchenslawisch"),
("cv", "Chuvash", "tchouvache", "Tschuwaschisch"),
("kw", "Cornish", "cornique", "Kornisch"),
("co", "Corsican", "corse", "Korsisch"),
("cr", "Cree", "cree", "Cree-Sprache"),
("cy", "Welsh", "gallois", "Kymrisch"),
("da", "Danish", "danois", "Dänisch"),
("de", "German", "allemand", "Deutsch"),
("dv", "Divehi, Dhivehi, Maldivian", "maldivien", "Maledivisch"),
("nl", "Dutch, Flemish", "néerlandais, flamand", "Niederländisch"),
("dz", "Dzongkha", "dzongkha", "Dzongkha"),
("el", "Greek, Modern (1453-)", "grec moderne (après 1453)", "Neugriechisch"),
("en", "English", "anglais", "Englisch"),
("eo", "Esperanto", "espéranto", "Esperanto"),
("et", "Estonian", "estonien", "Estnisch"),
("ee", "Ewe", "éwé", "Ewe-Sprache"),
("fo", "Faroese", "féroïen", "Färöisch"),
("fa", "Persian", "persan", "Persisch"),
("fj", "Fijian", "fidjien", "Fidschi-Sprache"),
("fi", "Finnish", "finnois", "Finnisch"),
("fr", "French", "français", "Französisch"),
("fy", "Western Frisian", "frison occidental", "Friesisch"),
("ff", "Fulah", "peul", "Ful"),
("ka", "Georgian", "géorgien", "Georgisch"),
("gd", "Gaelic, Scottish Gaelic", "gaélique, gaélique écossais", "Gälisch-Schottisch"),
("ga", "Irish", "irlandais", "Irisch"),
("gl", "Galician", "galicien", "Galicisch"),
("gv", "Manx", "manx, mannois", "Manx"),
("gn", "Guarani", "guarani", "Guaraní-Sprache"),
("gu", "Gujarati", "goudjrati", "Gujarati-Sprache"),
("ht", "Haitian, Haitian Creole", "haïtien, créole haïtien", "Haïtien (Haiti-Kreolisch)"),
("ha", "Hausa", "haoussa", "Haussa-Sprache"),
("he", "Hebrew", "hébreu", "Hebräisch"),
("hz", "Herero", "herero", "Herero-Sprache"),
("hi", "Hindi", "hindi", "Hindi"),
("ho", "Hiri Motu", "hiri motu", "Hiri-Motu"),
("hr", "Croatian", "croate", "Kroatisch"),
("hu", "Hungarian", "hongrois", "Ungarisch"),
("ig", "Igbo", "igbo", "Ibo-Sprache"),
("is", "Icelandic", "islandais", "Isländisch"),
("io", "Ido", "ido", "Ido"),
("ii", "Sichuan Yi, Nuosu", "yi de Sichuan", "Lalo-Sprache"),
("iu", "Inuktitut", "inuktitut", "Inuktitut"),
("ie", "Interlingue, Occidental", "interlingue", "Interlingue"),
("ia", "Interlingua (International Auxiliary Language Association)", "interlingua (langue auxiliaire internationale)", "Interlingua"),
("id", "Indonesian", "indonésien", "Bahasa Indonesia"),
("ik", "Inupiaq", "inupiaq", "Inupik"),
("it", "Italian", "italien", "Italienisch"),
("jv", "Javanese", "javanais", "Javanisch"),
("ja", "Japanese", "japonais", "Japanisch"),
("kl", "Kalaallisut, Greenlandic", "groenlandais", "Grönländisch"),
("kn", "Kannada", "kannada", "Kannada"),
("ks", "Kashmiri", "kashmiri", "Kaschmiri"),
("kr", "Kanuri", "kanouri", "Kanuri-Sprache"),
("kk", "Kazakh", "kazakh", "Kasachisch"),
("km", "Central Khmer", "khmer central", "Kambodschanisch"),
("ki", "Kikuyu, Gikuyu", "kikuyu", "Kikuyu-Sprache"),
("rw", "Kinyarwanda", "rwanda", "Rwanda-Sprache"),
("ky", "Kirghiz, Kyrgyz", "kirghiz", "Kirgisisch"),
("kv", "Komi", "kom", "Komi-Sprache"),
("kg", "Kongo", "kongo", "Kongo-Sprache"),
("ko", "Korean", "coréen", "Koreanisch"),
("kj", "Kuanyama, Kwanyama", "kuanyama, kwanyama", "Kwanyama-Sprache"),
("ku", "Kurdish", "kurde", "Kurdisch"),
("lo", "Lao", "lao", "Laotisch"),
("la", "Latin", "latin", "Latein"),
("lv", "Latvian", "letton", "Lettisch"),
("li", "Limburgan, Limburger, Limburgish", "limbourgeois", "Limburgisch"),
("ln", "Lingala", "lingala", "Lingala"),
("lt", "Lithuanian", "lituanien", "Litauisch"),
("lb", "Luxembourgish, Letzeburgesch", "luxembourgeois", "Luxemburgisch"),
("lu", "Luba-Katanga", "luba-katanga", "Luba-Katanga-Sprache"),
("lg", "Ganda", "ganda", "Ganda-Sprache"),
("mk", "Macedonian", "macédonien", "Makedonisch"),
("mh", "Marshallese", "marshall", "Marschallesisch"),
("ml", "Malayalam", "malayalam", "Malayalam"),
("mi", "Maori", "maori", "Maori-Sprache"),
("mr", "Marathi", "marathe", "Marathi"),
("ms", "Malay", "malais", "Malaiisch"),
("mg", "Malagasy", "malgache", "Malagassi-Sprache"),
("mt", "Maltese", "maltais", "Maltesisch"),
("mn", "Mongolian", "mongol", "Mongolisch"),
("na", "Nauru", "nauruan", "Nauruanisch"),
("nv", "Navajo, Navaho", "navaho", "Navajo-Sprache"),
("nr", "Ndebele, South, South Ndebele", "ndébélé du Sud", "Ndebele-Sprache (Transvaal)"),
("nd", "Ndebele, North, North Ndebele", "ndébélé du Nord", "Ndebele-Sprache (Simbabwe)"),
("ng", "Ndonga", "ndonga", "Ndonga"),
("ne", "Nepali", "népalais", "Nepali"),
("nn", "Norwegian Nynorsk, Nynorsk, Norwegian", "norvégien nynorsk, nynorsk, norvégien", "Nynorsk"),
("nb", "Bokmål, Norwegian, Norwegian Bokmål", "norvégien bokmål", "Bokmål"),
("no", "Norwegian", "norvégien", "Norwegisch"),
("ny", "Chichewa, Chewa, Nyanja", "chichewa, chewa, nyanja", "Nyanja-Sprache"),
("oc", "Occitan (post 1500)", "occitan (après 1500)", "Okzitanisch"),
("oj", "Ojibwa", "ojibwa", "Ojibwa-Sprache"),
("or", "Oriya", "oriya", "Oriya-Sprache"),
("om", "Oromo", "galla", "Galla-Sprache"),
("os", "Ossetian, Ossetic", "ossète", "Ossetisch"),
("pa", "Panjabi, Punjabi", "pendjabi", "Pandschabi-Sprache"),
("pi", "Pali", "pali", "Pali"),
("pl", "Polish", "polonais", "Polnisch"),
("pt", "Portuguese", "portugais", "Portugiesisch"),
("ps", "Pushto, Pashto", "pachto", "Paschtu"),
("qu", "Quechua", "quechua", "Quechua-Sprache"),
("rm", "Romansh", "romanche", "Rätoromanisch"),
("ro", "Romanian, Moldavian, Moldovan", "roumain, moldave", "Rumänisch"),
("rn", "Rundi", "rundi", "Rundi-Sprache"),
("ru", "Russian", "russe", "Russisch"),
("sg", "Sango", "sango", "Sango-Sprache"),
("sa", "Sanskrit", "sanskrit", "Sanskrit"),
("si", "Sinhala, Sinhalese", "singhalais", "Singhalesisch"),
("sk", "Slovak", "slovaque", "Slowakisch"),
("sl", "Slovenian", "slovène", "Slowenisch"),
("se", "Northern Sami", "sami du Nord", "Nordsaamisch"),
("sm", "Samoan", "samoan", "Samoanisch"),
("sn", "Shona", "shona", "Schona-Sprache"),
("sd", "Sindhi", "sindhi", "Sindhi-Sprache"),
("so", "Somali", "somali", "Somali"),
("st", "Sotho, Southern", "sotho du Sud", "Süd-Sotho-Sprache"),
("es", "Spanish, Castilian", "espagnol, castillan", "Spanisch"),
("sc", "Sardinian", "sarde", "Sardisch"),
("sr", "Serbian", "serbe", "Serbisch"),
("ss", "Swati", "swati", "Swasi-Sprache"),
("su", "Sundanese", "soundanais", "Sundanesisch"),
("sw", "Swahili", "swahili", "Swahili"),
("sv", "Swedish", "suédois", "Schwedisch"),
("ty", "Tahitian", "tahitien", "Tahitisch"),
("ta", "Tamil", "tamoul", "Tamil"),
("tt", "Tatar", "tatar", "Tatarisch"),
("te", "Telugu", "télougou", "Telugu-Sprache"),
("tg", "Tajik", "tadjik", "Tadschikisch"),
("tl", "Tagalog", "tagalog", "Tagalog"),
("th", "Thai", "thaï", "Thailändisch"),
("ti", "Tigrinya", "tigrigna", "Tigrinja-Sprache"),
("to", "Tonga (Tonga Islands)", "tongan (Îles Tonga)", "Tongaisch"),
("tn", "Tswana", "tswana", "Tswana-Sprache"),
("ts", "Tsonga", "tsonga", "Tsonga-Sprache"),
("tk", "Turkmen", "turkmène", "Turkmenisch"),
("tr", "Turkish", "turc", "Türkisch"),
("tw", "Twi", "twi", "Twi-Sprache"),
("ug", "Uighur, Uyghur", "ouïgour", "Uigurisch"),
("uk", "Ukrainian", "ukrainien", "Ukrainisch"),
("ur", "Urdu", "ourdou", "Urdu"),
("uz", "Uzbek", "ouszbek", "Usbekisch"),
("ve", "Venda", "venda", "Venda-Sprache"),
("vi", "Vietnamese", "vietnamien", "Vietnamesisch"),
("vo", "Volapük", "volapük", "Volapük"),
("wa", "Walloon", "wallon", "Wallonisch"),
("wo", "Wolof", "wolof", "Wolof-Sprache"),
("xh", "Xhosa", "xhosa", "Xhosa-Sprache"),
("yi", "Yiddish", "yiddish", "Jiddisch"),
("yo", "Yoruba", "yoruba", "Yoruba-Sprache"),
("za", "Zhuang, Chuang", "zhuang, chuang", "Zhuang"),
("zu", "Zulu", "zoulou", "Zulu-Sprache");