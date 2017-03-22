
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
    `isoCode` char(3) NOT NULL,
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


/* 2017-01-20 */

ALTER TABLE `goobi`.`plugin_gei_eurviews_contribution` DROP COLUMN noteOriginal;
ALTER TABLE `goobi`.`plugin_gei_eurviews_contribution` DROP COLUMN noteTranslation;
ALTER TABLE `goobi`.`plugin_gei_eurviews_contribution` DROP COLUMN referenceOriginal;
ALTER TABLE `goobi`.`plugin_gei_eurviews_contribution` DROP COLUMN referenceTranslation;
    
ALTER TABLE `goobi`.`plugin_gei_eurviews_contribution` add column contextOriginal text default null;
ALTER TABLE `goobi`.`plugin_gei_eurviews_contribution` add column contextTranslation text default null;

ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column projectContext text default null;
ALTER TABLE `goobi`.`plugin_gei_eurviews_transcription` add column selectionMethod text default null;

ALTER TABLE plugin_gei_eurviews_resource RENAME plugin_gei_eurviews_bibliographic_data;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN resourceType;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN resourceTitleOriginal;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN resourceTitleGerman;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN resourceTitleEnglish;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN startPage;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN endPage;
ALTER TABLE plugin_gei_eurviews_bibliographic_data DROP COLUMN supplier;


CREATE TABLE `goobi`.`plugin_gei_eurviews_resource` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `bibliographicDataID` int(10) unsigned NOT NULL DEFAULT '0',
    `resourceType` varchar(255) DEFAULT NULL,
    `resourceTitleOriginal` varchar(255) DEFAULT NULL,
    `resourceTitleGerman` varchar(255) DEFAULT NULL,
    `resourceTitleEnglish` varchar(255) DEFAULT NULL,
    `startPage` varchar(255) DEFAULT NULL,
    `endPage` varchar(255) DEFAULT NULL,
    `supplier` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `prozesseID` (`prozesseID`)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
    
    /* 2017-01-25 ?? */
    
ALTER TABLE `plugin_gei_eurviews_contribution` CHANGE COLUMN `titleOriginal` `title` VARCHAR(255);
ALTER TABLE `plugin_gei_eurviews_contribution` DROP COLUMN `titleTranslation`;
ALTER TABLE `plugin_gei_eurviews_contribution` CHANGE COLUMN `languageOriginal` `language` VARCHAR(255);
ALTER TABLE `plugin_gei_eurviews_contribution` DROP COLUMN `languageTranslation`;
ALTER TABLE `plugin_gei_eurviews_contribution` CHANGE COLUMN `abstractOriginal` `abstract` text;
ALTER TABLE `plugin_gei_eurviews_contribution` DROP COLUMN `abstractTranslation`;
ALTER TABLE `plugin_gei_eurviews_contribution` CHANGE COLUMN `contentOriginal` `content` text;
ALTER TABLE `plugin_gei_eurviews_contribution` DROP COLUMN `contentTranslation`;
ALTER TABLE `plugin_gei_eurviews_contribution` CHANGE COLUMN `contextOriginal` `context` text;
ALTER TABLE `plugin_gei_eurviews_contribution` DROP COLUMN `contextTranslation`;

/* 
ALTER TABLE `plugin_gei_eurviews_contribution` DROP COLUMN `noteOriginal`;
*/

/* 2017-02-10 */

ALTER TABLE `plugin_gei_eurviews_transcription` DROP COLUMN `projectContext`;
ALTER TABLE `plugin_gei_eurviews_transcription` DROP COLUMN `selectionMethod`;

ALTER TABLE `plugin_gei_eurviews_context` add column projectContext text default null;
ALTER TABLE `plugin_gei_eurviews_context` add column selectionMethod text default null;

ALTER TABLE `plugin_gei_eurviews_transcription` ADD COLUMN `originalLanguage` BOOLEAN default FALSE;
ALTER TABLE `plugin_gei_eurviews_context` ADD COLUMN `originalLanguage` BOOLEAN default FALSE;

/* 2017-03-22 */

delete from plugin_gei_eurviews_languages;
ALTER TABLE `plugin_gei_eurviews_languages` CHANGE COLUMN `isoCode` `isoCode` char(3) NOT NULL;

insert into plugin_gei_eurviews_languages (isoCode,englishName,frenchName,germanName) VALUES 
("aar","Afar","afar","Danakil-Sprache"),
("abk","Abkhazian","abkhaze","Abchasisch"),
("ace","Achinese","aceh","Aceh-Sprache"),
("ach","Acoli","acoli","Acholi-Sprache"),
("ada","Adangme","adangme","Adangme-Sprache"),
("ady","Adyghe; Adygei","adyghé","Adygisch"),
("afh","Afrihili","afrihili","Afrihili"),
("afr","Afrikaans","afrikaans","Afrikaans"),
("egy","Egyptian (Ancient)","égyptien","Ägyptisch"),
("ain","Ainu","aïnou","Ainu-Sprache"),
("aka","Akan","akan","Akan-Sprache"),
("akk","Akkadian","akkadien","Akkadisch"),
("alb","Albanian","albanais","Albanisch"),
("ale","Aleut","aléoute","Aleutisch"),
("alg","Algonquian languages","algonquines, langues","Algonkin-Sprachen (Andere)"),
("nwc","Classical Newari; Old Newari; Classical Nepal Bhasa","newari classique","Alt-Newari"),
("alt","Southern Altai","altai du Sud","Altaisch"),
("tut","Altaic languages","altaïques, langues","Altaische Sprachen (Andere)"),
("gez","Geez","guèze","Altäthiopisch"),
("ang","English, Old (ca.450-1100)","anglo-saxon (ca.450-1100)","Altenglisch"),
("fro","French, Old (842-ca.1400)","français ancien (842-ca.1400)","Altfranzösisch"),
("goh","German, Old High (ca.750-1050)","allemand, vieux haut (ca. 750-1050)","Althochdeutsch"),
("sga","Irish, Old (to 900)","irlandais ancien (jusqu'à 900)","Altirisch"),
("non","Norse, Old","norrois, vieux","Altnorwegisch"),
("pro","Provençal, Old (to 1500);Occitan, Old (to 1500)","provençal ancien (jusqu'à 1500); occitan ancien (jusqu'à 1500)","Altokzitanisch"),
("peo","Persian, Old (ca.600-400 B.C.)","perse, vieux (ca. 600-400 av. J.-C.)","Altpersisch"),
("amh","Amharic","amharique","Amharisch"),
("anp","Angika","angika","Anga-Sprache"),
("apa","Apache languages","apaches, langues","Apachen-Sprachen"),
("ara","Arabic","arabe","Arabisch"),
("arg","Aragonese","aragonais","Aragonesisch"),
("arc","Official Aramaic (700-300 BCE); Imperial Aramaic (700-300 BCE)","araméen d'empire (700-300 BCE)","Aramäisch"),
("arp","Arapaho","arapaho","Arapaho-Sprache"),
("arn","Mapudungun; Mapuche","mapudungun; mapuche; mapuce","Arauka-Sprachen"),
("arw","Arawak","arawak","Arawak-Sprachen"),
("arm","Armenian","arménien","Armenisch"),
("rup","Aromanian; Arumanian; Macedo-Romanian","aroumain; macédo-roumain","Aromunisch"),
("aze","Azerbaijani","azéri","Aserbeidschanisch"),
("asm","Assamese","assamais","Assamesisch"),
("ast","Asturian; Bable; Leonese; Asturleonese","asturien; bable; léonais; asturoléonais","Asturisch"),
("ath","Athapascan languages","athapascanes, langues","Athapaskische Sprachen (Andere)"),
("aus","Australian languages","australiennes, langues","Australische Sprachen"),
("map","Austronesian languages","austronésiennes, langues","Austronesische Sprachen (Andere)"),
("ave","Avestan","avestique","Avestisch"),
("awa","Awadhi","awadhi","Awadhi"),
("ava","Avaric","avar","Awarisch"),
("aym","Aymara","aymara","Aymará-Sprache"),
("ind","Indonesian","indonésien","Bahasa Indonesia"),
("ban","Balinese","balinais","Balinesisch"),
("bat","Baltic languages","baltes, langues","Baltische Sprachen (Andere)"),
("bam","Bambara","bambara","Bambara-Sprache"),
("bai","Bamileke languages","bamiléké, langues","Bamileke-Sprachen"),
("bad","Banda languages","banda, langues","Banda-Sprachen (Ubangi-Sprachen)"),
("bnt","Bantu languages","bantou, langues","Bantusprachen (Andere)"),
("bas","Basa","basa","Basaa-Sprache"),
("bak","Bashkir","bachkir","Baschkirisch"),
("baq","Basque","basque","Baskisch"),
("btk","Batak languages","batak, langues","Batak-Sprache"),
("bis","Bislama","bichlamar","Beach-la-mar"),
("bej","Beja; Bedawiyet","bedja","Bedauye"),
("bal","Baluchi","baloutchi","Belutschisch"),
("bem","Bemba","bemba","Bemba-Sprache"),
("ben","Bengali","bengali","Bengali"),
("ber","Berber languages","berbères, langues","Berbersprachen (Andere)"),
("bho","Bhojpuri","bhojpuri","Bhojpuri"),
("bih","Bihari languages","langues biharis","Bihari (Andere)"),
("bik","Bikol","bikol","Bikol-Sprache"),
("byn","Blin; Bilin","blin; bilen","Bilin-Sprache"),
("bur","Burmese","birman","Birmanisch"),
("bla","Siksika","blackfoot","Blackfoot-Sprache"),
("zbl","Blissymbols; Blissymbolics; Bliss","symboles Bliss; Bliss","Bliss-Symbol"),
("nob","Bokmål, Norwegian; Norwegian Bokmål","norvégien bokmål","Bokmål"),
("bos","Bosnian","bosniaque","Bosnisch"),
("bra","Braj","braj","Braj-Bhakha"),
("bre","Breton","breton","Bretonisch"),
("bug","Buginese","bugi","Bugi-Sprache"),
("bul","Bulgarian","bulgare","Bulgarisch"),
("bua","Buriat","bouriate","Burjatisch"),
("cad","Caddo","caddo","Caddo-Sprachen"),
("ceb","Cebuano","cebuano","Cebuano"),
("cmc","Chamic languages","chames, langues","Cham-Sprachen"),
("cha","Chamorro","chamorro","Chamorro-Sprache"),
("chr","Cherokee","cherokee","Cherokee-Sprache"),
("chy","Cheyenne","cheyenne","Cheyenne-Sprache"),
("chb","Chibcha","chibcha","Chibcha-Sprachen"),
("chi","Chinese","chinois","Chinesisch"),
("chn","Chinook jargon","chinook, jargon","Chinook-Jargon"),
("chp","Chipewyan; Dene Suline","chipewyan","Chipewyan-Sprache"),
("cho","Choctaw","choctaw","Choctaw-Sprache"),
("cre","Cree","cree","Cree-Sprache"),
("day","Land Dayak languages","dayak, langues","Dajakisch"),
("dak","Dakota","dakota","Dakota-Sprache"),
("dan","Danish","danois","Dänisch"),
("dar","Dargwa","dargwa","Darginisch"),
("del","Delaware","delaware","Delaware-Sprache"),
("ger","German","allemand","Deutsch"),
("din","Dinka","dinka","Dinka-Sprache"),
("doi","Dogri","dogri","Dogri"),
("dgr","Dogrib","dogrib","Dogrib-Sprache"),
("dra","Dravidian languages","dravidiennes, langues","Drawidische Sprachen (Andere)"),
("dua","Duala","douala","Duala-Sprachen"),
("dyu","Dyula","dioula","Dyula-Sprache"),
("dzo","Dzongkha","dzongkha","Dzongkha"),
("bin","Bini; Edo","bini; edo","Edo-Sprache"),
("efi","Efik","efik","Efik"),
("mis","Uncoded languages","langues non codées","Einzelne andere Sprachen"),
("eka","Ekajuk","ekajuk","Ekajuk"),
("elx","Elamite","élamite","Elamisch"),
("tvl","Tuvalu","tuvalu","Elliceanisch"),
("eng","English","anglais","Englisch"),
("myv","Erzya","erza","Erza-Mordwinisch"),
("epo","Esperanto","espéranto","Esperanto"),
("est","Estonian","estonien","Estnisch"),
("ewe","Ewe","éwé","Ewe-Sprache"),
("ewo","Ewondo","éwondo","Ewondo"),
("fat","Fanti","fanti","Fante-Sprache"),
("fao","Faroese","féroïen","Färöisch"),
("fij","Fijian","fidjien","Fidschi-Sprache"),
("fin","Finnish","finnois","Finnisch"),
("fiu","Finno-Ugrian languages","finno-ougriennes, langues","Finnougrische Sprachen (Andere)"),
("fon","Fon","fon","Fon-Sprache"),
("fre","French","français","Französisch"),
("fry","Western Frisian","frison occidental","Friesisch"),
("fur","Friulian","frioulan","Friulisch"),
("ful","Fulah","peul","Ful"),
("gaa","Ga","ga","Ga-Sprache"),
("glg","Galician","galicien","Galicisch"),
("gla","Gaelic; Scottish Gaelic","gaélique; gaélique écossais","Gälisch-Schottisch"),
("orm","Oromo","galla","Galla-Sprache"),
("lug","Ganda","ganda","Ganda-Sprache"),
("gay","Gayo","gayo","Gayo-Sprache"),
("gba","Gbaya","gbaya","Gbaya-Sprache"),
("geo","Georgian","géorgien","Georgisch"),
("gem","Germanic languages","germaniques, langues","Germanische Sprachen (Andere)"),
("gil","Gilbertese","kiribati","Gilbertesisch"),
("gon","Gondi","gond","Gondi-Sprache"),
("gor","Gorontalo","gorontalo","Gorontalesisch"),
("got","Gothic","gothique","Gotisch"),
("grb","Grebo","grebo","Grebo-Sprache"),
("grc","Greek, Ancient (to 1453)","grec ancien (jusqu'à 1453)","Griechisch"),
("kal","Kalaallisut; Greenlandic","groenlandais","Grönländisch"),
("grn","Guarani","guarani","Guaraní-Sprache"),
("guj","Gujarati","goudjrati","Gujarati-Sprache"),
("hai","Haida","haida","Haida-Sprache"),
("hat","Haitian; Haitian Creole","haïtien; créole haïtien","Haïtien (Haiti-Kreolisch)"),
("afa","Afro-Asiatic languages","afro-asiatiques, langues","Hamitosemitische Sprachen (Andere)"),
("hau","Hausa","haoussa","Haussa-Sprache"),
("haw","Hawaiian","hawaïen","Hawaiisch"),
("heb","Hebrew","hébreu","Hebräisch"),
("her","Herero","herero","Herero-Sprache"),
("hit","Hittite","hittite","Hethitisch"),
("hil","Hiligaynon","hiligaynon","Hiligaynon-Sprache"),
("him","Himachali languages; Western Pahari languages","langues himachalis; langues paharis occidentales","Himachali"),
("hin","Hindi","hindi","Hindi"),
("hmo","Hiri Motu","hiri motu","Hiri-Motu"),
("hup","Hupa","hupa","Hupa-Sprache"),
("iba","Iban","iban","Iban-Sprache"),
("ibo","Igbo","igbo","Ibo-Sprache"),
("ido","Ido","ido","Ido"),
("ijo","Ijo languages","ijo, langues","Ijo-Sprache"),
("ilo","Iloko","ilocano","Ilokano-Sprache"),
("smn","Inari Sami","sami d'Inari","Inarisaamisch"),
("nai","North American Indian languages","nord-amérindiennes, langues","Indianersprachen, Nordamerika (Andere)"),
("sai","South American Indian languages","sud-amérindiennes, langues","Indianersprachen, Südamerika (Andere)"),
("cai","Central American Indian languages","amérindiennes de l'Amérique centrale, langues","Indianersprachen, Zentralamerika (Andere)"),
("inc","Indic languages","indo-aryennes, langues","Indoarische Sprachen (Andere)"),
("ine","Indo-European languages","indo-européennes, langues","Indogermanische Sprachen (Andere)"),
("inh","Ingush","ingouche","Inguschisch"),
("ina","Interlingua (International Auxiliary Language Association)","interlingua (langue auxiliaire internationale)","Interlingua"),
("ile","Interlingue; Occidental","interlingue","Interlingue"),
("iku","Inuktitut","inuktitut","Inuktitut"),
("ipk","Inupiaq","inupiaq","Inupik"),
("ira","Iranian languages","iraniennes, langues","Iranische Sprachen (Andere)"),
("gle","Irish","irlandais","Irisch"),
("iro","Iroquoian languages","iroquoises, langues","Irokesische Sprachen"),
("ice","Icelandic","islandais","Isländisch"),
("ita","Italian","italien","Italienisch"),
("sah","Yakut","iakoute","Jakutisch"),
("jpn","Japanese","japonais","Japanisch"),
("jav","Javanese","javanais","Javanisch"),
("yid","Yiddish","yiddish","Jiddisch"),
("lad","Ladino","judéo-espagnol","Judenspanisch"),
("jrb","Judeo-Arabic","judéo-arabe","Jüdisch-Arabisch"),
("jpr","Judeo-Persian","judéo-persan","Jüdisch-Persisch"),
("kbd","Kabardian","kabardien","Kabardinisch"),
("kab","Kabyle","kabyle","Kabylisch"),
("kac","Kachin; Jingpho","kachin; jingpho","Kachin-Sprache"),
("xal","Kalmyk; Oirat","kalmouk; oïrat","Kalmückisch"),
("kam","Kamba","kamba","Kamba-Sprache"),
("khm","Central Khmer","khmer central","Kambodschanisch"),
("kan","Kannada","kannada","Kannada"),
("kau","Kanuri","kanouri","Kanuri-Sprache"),
("kaa","Kara-Kalpak","karakalpak","Karakalpakisch"),
("krc","Karachay-Balkar","karatchai balkar","Karatschaiisch-Balkarisch"),
("krl","Karelian","carélien","Karelisch"),
("kar","Karen languages","karen, langues","Karenisch"),
("car","Galibi Carib","karib; galibi; carib","Karibische Sprachen"),
("kaz","Kazakh","kazakh","Kasachisch"),
("kas","Kashmiri","kashmiri","Kaschmiri"),
("csb","Kashubian","kachoube","Kaschubisch"),
("cat","Catalan; Valencian","catalan; valencien","Katalanisch"),
("cau","Caucasian languages","caucasiennes, langues","Kaukasische Sprachen (Andere)"),
("kaw","Kawi","kawi","Kawi"),
("cel","Celtic languages","celtiques, langues; celtes, langues","Keltische Sprachen (Andere)"),
("kha","Khasi","khasi","Khasi-Sprache"),
("khi","Khoisan languages","khoïsan, langues","Khoisan-Sprachen (Andere)"),
("mag","Magahi","magahi","Khotta"),
("kik","Kikuyu; Gikuyu","kikuyu","Kikuyu-Sprache"),
("kmb","Kimbundu","kimbundu","Kimbundu-Sprache"),
("chu","Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic","slavon d'église; vieux slave; slavon liturgique; vieux bulgare","Kirchenslawisch"),
("kir","Kirghiz; Kyrgyz","kirghiz","Kirgisisch"),
("tlh","Klingon; tlhIngan-Hol","klingon","Klingonisch"),
("kom","Komi","kom","Komi-Sprache"),
("kon","Kongo","kongo","Kongo-Sprache"),
("kok","Konkani","konkani","Konkani"),
("cop","Coptic","copte","Koptisch"),
("kor","Korean","coréen","Koreanisch"),
("cor","Cornish","cornique","Kornisch"),
("cos","Corsican","corse","Korsisch"),
("kos","Kosraean","kosrae","Kosraeanisch"),
("kpe","Kpelle","kpellé","Kpelle-Sprache"),
("cpe","Creoles and pidgins, English based","créoles et pidgins basés sur l'anglais","Kreolisch-Englisch (Andere)"),
("cpf","Creoles and pidgins, French-based","créoles et pidgins basés sur le français","Kreolisch-Französisch (Andere)"),
("cpp","Creoles and pidgins, Portuguese-based","créoles et pidgins basés sur le portugais","Kreolisch-Portugiesisch (Andere)"),
("crp","Creoles and pidgins","créoles et pidgins","Kreolische Sprachen; Pidginsprachen (Andere)"),
("crh","Crimean Tatar; Crimean Turkish","tatar de Crimé","Krimtatarisch"),
("hrv","Croatian","croate","Kroatisch "),
("kro","Kru languages","krou, langues","Kru-Sprachen (Andere)"),
("kum","Kumyk","koumyk","Kumükisch"),
("art","Artificial languages","artificielles, langues","Kunstsprachen (Andere)"),
("kur","Kurdish","kurde","Kurdisch"),
("cus","Cushitic languages","couchitiques, langues","Kuschitische Sprachen (Andere)"),
("gwi","Gwich'in","gwich'in","Kutchin-Sprache"),
("kut","Kutenai","kutenai","Kutenai-Sprache"),
("kua","Kuanyama; Kwanyama","kuanyama; kwanyama","Kwanyama-Sprache"),
("wel","Welsh","gallois","Kymrisch"),
("lah","Lahnda","lahnda","Lahnda"),
("iii","Sichuan Yi; Nuosu","yi de Sichuan","Lalo-Sprache"),
("lam","Lamba","lamba","Lamba-Sprache (Bantusprache)"),
("lao","Lao","lao","Laotisch"),
("lat","Latin","latin","Latein"),
("lez","Lezghian","lezghien","Lesgisch"),
("lav","Latvian","letton","Lettisch"),
("lim","Limburgan; Limburger; Limburgish","limbourgeois","Limburgisch"),
("lin","Lingala","lingala","Lingala"),
("lit","Lithuanian","lituanien","Litauisch"),
("jbo","Lojban","lojban","Lojban"),
("lub","Luba-Katanga","luba-katanga","Luba-Katanga-Sprache"),
("lui","Luiseno","luiseno","Luiseño-Sprache"),
("smj","Lule Sami","sami de Lule","Lulesaamisch"),
("lua","Luba-Lulua","luba-lulua","Lulua-Sprache"),
("lun","Lunda","lunda","Lunda-Sprache"),
("luo","Luo (Kenya and Tanzania)","luo (Kenya et Tanzanie)","Luo-Sprache"),
("lus","Lushai","lushai","Lushai-Sprache"),
("ltz","Luxembourgish; Letzeburgesch","luxembourgeois","Luxemburgisch"),
("mad","Madurese","madourais","Maduresisch"),
("mai","Maithili","maithili","Maithili"),
("mak","Makasar","makassar","Makassarisch"),
("mac","Macedonian","macédonien","Makedonisch"),
("mlg","Malagasy","malgache","Malagassi-Sprache"),
("may","Malay","malais","Malaiisch"),
("mal","Malayalam","malayalam","Malayalam"),
("div","Divehi; Dhivehi; Maldivian","maldivien","Maledivisch"),
("man","Mandingo","mandingue","Malinke-Sprache"),
("mlt","Maltese","maltais","Maltesisch"),
("mdr","Mandar","mandar","Mandaresisch"),
("mnc","Manchu","mandchou","Mandschurisch"),
("mno","Manobo languages","manobo, langues","Manobo-Sprachen"),
("glv","Manx","manx; mannois","Manx"),
("mao","Maori","maori","Maori-Sprache"),
("mar","Marathi","marathe","Marathi"),
("mah","Marshallese","marshall","Marschallesisch"),
("mwr","Marwari","marvari","Marwari"),
("mas","Masai","massaï","Massai-Sprache"),
("myn","Mayan languages","maya, langues","Maya-Sprachen"),
("umb","Umbundu","umbundu","Mbundu-Sprache"),
("mul","Multiple languages","multilingue","Mehrere Sprachen"),
("mni","Manipuri","manipuri","Meithei-Sprache"),
("men","Mende","mendé","Mende-Sprache"),
("hmn","Hmong; Mong","hmong","Miao-Sprachen"),
("mic","Mi'kmaq; Micmac","mi'kmaq; micmac","Micmac-Sprache"),
("min","Minangkabau","minangkabau","Minangkabau-Sprache"),
("mwl","Mirandese","mirandais","Mirandesisch"),
("enm","English, Middle (1100-1500)","anglais moyen (1100-1500)","Mittelenglisch"),
("frm","French, Middle (ca.1400-1600)","français moyen (1400-1600)","Mittelfranzösisch"),
("gmh","German, Middle High (ca.1050-1500)","allemand, moyen haut (ca. 1050-1500)","Mittelhochdeutsch"),
("mga","Irish, Middle (900-1200)","irlandais moyen (900-1200)","Mittelirisch"),
("dum","Dutch, Middle (ca.1050-1350)","néerlandais moyen (ca. 1050-1350)","Mittelniederländisch"),
("pal","Pahlavi","pahlavi","Mittelpersisch"),
("moh","Mohawk","mohawk","Mohawk-Sprache"),
("mdf","Moksha","moksa","Mokscha-Sprache"),
("mkh","Mon-Khmer languages","môn-khmer, langues","Mon-Khmer-Sprachen (Andere)"),
("lol","Mongo","mongo","Mongo-Sprache"),
("mon","Mongolian","mongol","Mongolisch"),
("mos","Mossi","moré","Mossi-Sprache"),
("mun","Munda languages","mounda, langues","Mundasprachen (Andere)"),
("mus","Creek","muskogee","Muskogisch"),
("nqo","N'Ko","n'ko","N'Ko"),
("nah","Nahuatl languages","nahuatl, langues","Nahuatl"),
("nau","Nauru","nauruan","Nauruanisch"),
("nav","Navajo; Navaho","navaho","Navajo-Sprache"),
("nde","Ndebele, North; North Ndebele","ndébélé du Nord","Ndebele-Sprache (Simbabwe)"),
("nbl","Ndebele, South; South Ndebele","ndébélé du Sud","Ndebele-Sprache (Transvaal)"),
("ndo","Ndonga","ndonga","Ndonga"),
("nap","Neapolitan","napolitain","Neapel / Mundart"),
("nep","Nepali","népalais","Nepali"),
("gre","Greek, Modern (1453-)","grec moderne (après 1453)","Neugriechisch"),
("tpi","Tok Pisin","tok pisin","Neumelanesisch"),
("syr","Syriac","syriaque","Neuostaramäisch"),
("new","Nepal Bhasa; Newari","nepal bhasa; newari","Newari"),
("nia","Nias","nias","Nias-Sprache"),
("und","Undetermined","indéterminée","Nicht zu entscheiden"),
("nds","Low German; Low Saxon; German, Low; Saxon, Low","bas allemand; bas saxon; allemand, bas; saxon, bas","Niederdeutsch"),
("dut","Dutch; Flemish","néerlandais; flamand","Niederländisch"),
("dsb","Lower Sorbian","bas-sorabe","Niedersorbisch"),
("nic","Niger-Kordofanian languages","nigéro-kordofaniennes, langues","Nigerkordofanische Sprachen (Andere)"),
("ssa","Nilo-Saharan languages","nilo-sahariennes, langues","Nilosaharanische Sprachen (Andere)"),
("niu","Niuean","niué","Niue-Sprache"),
("nyn","Nyankole","nyankolé","Nkole-Sprache"),
("nog","Nogai","nogaï; nogay","Nogaisch"),
("frr","Northern Frisian","frison septentrional","Nordfriesisch"),
("sme","Northern Sami","sami du Nord","Nordsaamisch"),
("nor","Norwegian","norvégien","Norwegisch"),
("nub","Nubian languages","nubiennes, langues","Nubische Sprachen"),
("nym","Nyamwezi","nyamwezi","Nyamwezi-Sprache"),
("nya","Chichewa; Chewa; Nyanja","chichewa; chewa; nyanja","Nyanja-Sprache"),
("nno","Norwegian Nynorsk; Nynorsk, Norwegian","norvégien nynorsk; nynorsk, norvégien","Nynorsk"),
("nyo","Nyoro","nyoro","Nyoro-Sprache"),
("nzi","Nzima","nzema","Nzima-Sprache"),
("hsb","Upper Sorbian","haut-sorabe","Obersorbisch"),
("oji","Ojibwa","ojibwa","Ojibwa-Sprache"),
("oci","Occitan (post 1500)","occitan (après 1500)","Okzitanisch"),
("kru","Kurukh","kurukh","Oraon-Sprache"),
("ori","Oriya","oriya","Oriya-Sprache"),
("osa","Osage","osage","Osage-Sprache"),
("ota","Turkish, Ottoman (1500-1928)","turc ottoman (1500-1928)","Osmanisch"),
("oss","Ossetian; Ossetic","ossète","Ossetisch"),
("rap","Rapanui","rapanui","Osterinsel-Sprache"),
("frs","Eastern Frisian","frison oriental","Ostfriesisch"),
("oto","Otomian languages","otomi, langues","Otomangue-Sprachen"),
("pau","Palauan","palau","Palau-Sprache"),
("pli","Pali","pali","Pali"),
("pam","Pampanga; Kapampangan","pampangan","Pampanggan-Sprache"),
("pan","Panjabi; Punjabi","pendjabi","Pandschabi-Sprache"),
("pag","Pangasinan","pangasinan","Pangasinan-Sprache"),
("fan","Fang","fang","Pangwe-Sprache"),
("pap","Papiamento","papiamento","Papiamento"),
("paa","Papuan languages","papoues, langues","Papuasprachen (Andere)"),
("pus","Pushto; Pashto","pachto","Paschtu"),
("nso","Pedi; Sepedi; Northern Sotho","pedi; sepedi; sotho du Nord","Pedi-Sprache"),
("per","Persian","persan","Persisch"),
("phi","Philippine languages","philippines, langues","Philippinisch-Austronesisch (Andere)"),
("phn","Phoenician","phénicien","Phönikisch"),
("fil","Filipino; Pilipino","filipino; pilipino","Pilipino"),
("pol","Polish","polonais","Polnisch"),
("pon","Pohnpeian","pohnpei","Ponapeanisch"),
("por","Portuguese","portugais","Portugiesisch"),
("pra","Prakrit languages","prâkrit, langues","Prakrit"),
("que","Quechua","quechua","Quechua-Sprache"),
("raj","Rajasthani","rajasthani","Rajasthani"),
("rar","Rarotongan; Cook Islands Maori","rarotonga; maori des îles Cook","Rarotonganisch"),
("roh","Romansh","romanche","Rätoromanisch"),
("rom","Romany","tsigane","Romani (Sprache)"),
("roa","Romance languages","romanes, langues","Romanische Sprachen (Andere)"),
("loz","Lozi","lozi","Rotse-Sprache"),
("rum","Romanian; Moldavian; Moldovan","roumain; moldave","Rumänisch"),
("run","Rundi","rundi","Rundi-Sprache"),
("rus","Russian","russe","Russisch"),
("kin","Kinyarwanda","rwanda","Rwanda-Sprache"),
("smi","Sami languages","sames, langues","Saamisch"),
("kho","Khotanese; Sakan","khotanais; sakan","Sakisch"),
("sal","Salishan languages","salishennes, langues","Salish-Sprache"),
("sam","Samaritan Aramaic","samaritain","Samaritanisch"),
("smo","Samoan","samoan","Samoanisch"),
("sad","Sandawe","sandawe","Sandawe-Sprache"),
("sag","Sango","sango","Sango-Sprache"),
("san","Sanskrit","sanskrit","Sanskrit"),
("sat","Santali","santal","Santali"),
("srd","Sardinian","sarde","Sardisch"),
("sas","Sasak","sasak","Sasak"),
("shn","Shan","chan","Schan-Sprache"),
("sna","Shona","shona","Schona-Sprache"),
("sco","Scots","écossais","Schottisch"),
("swe","Swedish","suédois","Schwedisch"),
("gsw","Swiss German; Alemannic; Alsatian","suisse alémanique; alémanique; alsacien","Schweizerdeutsch"),
("sel","Selkup","selkoupe","Selkupisch"),
("sem","Semitic languages","sémitiques, langues","Semitische Sprachen (Andere)"),
("srp","Serbian","serbe","Serbisch "),
("srr","Serer","sérère","Serer-Sprache"),
("sid","Sidamo","sidamo","Sidamo-Sprache"),
("snd","Sindhi","sindhi","Sindhi-Sprache"),
("sin","Sinhala; Sinhalese","singhalais","Singhalesisch"),
("sit","Sino-Tibetan languages","sino-tibétaines, langues","Sinotibetische Sprachen (Andere)"),
("sio","Siouan languages","sioux, langues","Sioux-Sprachen (Andere)"),
("scn","Sicilian","sicilien","Sizilianisch"),
("sms","Skolt Sami","sami skolt","Skoltsaamisch"),
("den","Slave (Athapascan)","esclave (athapascan)","Slave-Sprache"),
("sla","Slavic languages","slaves, langues","Slawische Sprachen (Andere)"),
("slo","Slovak","slovaque","Slowakisch"),
("slv","Slovenian","slovène","Slowenisch"),
("sog","Sogdian","sogdien","Sogdisch"),
("som","Somali","somali","Somali"),
("son","Songhai languages","songhai, langues","Songhai-Sprache"),
("snk","Soninke","soninké","Soninke-Sprache"),
("wen","Sorbian languages","sorabes, langues","Sorbisch (Andere)"),
("spa","Spanish; Castilian","espagnol; castillan","Spanisch"),
("srn","Sranan Tongo","sranan tongo","Sranantongo"),
("sot","Sotho, Southern","sotho du Sud","Süd-Sotho-Sprache"),
("sma","Southern Sami","sami du Sud","Südsaamisch"),
("suk","Sukuma","sukuma","Sukuma-Sprache"),
("sux","Sumerian","sumérien","Sumerisch"),
("sun","Sundanese","soundanais","Sundanesisch"),
("sus","Susu","soussou","Susu"),
("swa","Swahili","swahili","Swahili"),
("ssw","Swati","swati","Swasi-Sprache"),
("syc","Classical Syriac","syriaque classique","Syrisch"),
("tgk","Tajik","tadjik","Tadschikisch"),
("tgl","Tagalog","tagalog","Tagalog"),
("tah","Tahitian","tahitien","Tahitisch"),
("tmh","Tamashek","tamacheq","Tamašeq"),
("tam","Tamil","tamoul","Tamil"),
("tat","Tatar","tatar","Tatarisch"),
("tel","Telugu","télougou","Telugu-Sprache"),
("tem","Timne","temne","Temne-Sprache"),
("ter","Tereno","tereno","Tereno-Sprache"),
("tet","Tetum","tetum","Tetum-Sprache"),
("tha","Thai","thaï","Thailändisch"),
("tai","Tai languages","tai, langues","Thaisprachen (Andere)"),
("tib","Tibetan","tibétain","Tibetisch"),
("tig","Tigre","tigré","Tigre-Sprache"),
("tir","Tigrinya","tigrigna","Tigrinja-Sprache"),
("tiv","Tiv","tiv","Tiv-Sprache"),
("tli","Tlingit","tlingit","Tlingit-Sprache"),
("tkl","Tokelau","tokelau","Tokelauanisch"),
("tog","Tonga (Nyasa)","tonga (Nyasa)","Tonga (Bantusprache, Sambia)"),
("ton","Tonga (Tonga Islands)","tongan (Îles Tonga)","Tongaisch"),
("chk","Chuukese","chuuk","Trukesisch"),
("chg","Chagatai","djaghataï","Tschagataisch"),
("cze","Czech","tchèque","Tschechisch"),
("chm","Mari","mari","Tscheremissisch"),
("che","Chechen","tchétchène","Tschetschenisch"),
("chv","Chuvash","tchouvache","Tschuwaschisch"),
("tsi","Tsimshian","tsimshian","Tsimshian-Sprache"),
("tso","Tsonga","tsonga","Tsonga-Sprache"),
("tsn","Tswana","tswana","Tswana-Sprache"),
("tum","Tumbuka","tumbuka","Tumbuka-Sprache"),
("tup","Tupi languages","tupi, langues","Tupi-Sprache"),
("tur","Turkish","turc","Türkisch"),
("tuk","Turkmen","turkmène","Turkmenisch"),
("tyv","Tuvinian","touva","Tuwinisch"),
("twi","Twi","twi","Twi-Sprache"),
("udm","Udmurt","oudmourte","Udmurtisch"),
("uga","Ugaritic","ougaritique","Ugaritisch"),
("uig","Uighur; Uyghur","ouïgour","Uigurisch"),
("ukr","Ukrainian","ukrainien","Ukrainisch"),
("hun","Hungarian","hongrois","Ungarisch"),
("urd","Urdu","ourdou","Urdu"),
("uzb","Uzbek","ouszbek","Usbekisch"),
("vai","Vai","vaï","Vai-Sprache"),
("ven","Venda","venda","Venda-Sprache"),
("vie","Vietnamese","vietnamien","Vietnamesisch"),
("vol","Volapük","volapük","Volapük"),
("wak","Wakashan languages","wakashanes, langues","Wakash-Sprachen"),
("wal","Wolaitta; Wolaytta","wolaitta; wolaytta","Walamo-Sprache"),
("wln","Walloon","wallon","Wallonisch"),
("war","Waray","waray","Waray"),
("was","Washo","washo","Washo-Sprache"),
("bel","Belarusian","biélorusse","Weißrussisch"),
("wol","Wolof","wolof","Wolof-Sprache"),
("vot","Votic","vote","Wotisch"),
("xho","Xhosa","xhosa","Xhosa-Sprache"),
("yao","Yao","yao","Yao-Sprache (Bantusprache)"),
("yap","Yapese","yapois","Yapesisch"),
("yor","Yoruba","yoruba","Yoruba-Sprache"),
("ypk","Yupik languages","yupik, langues","Ypik-Sprachen"),
("znd","Zande languages","zandé, langues","Zande-Sprachen"),
("zap","Zapotec","zapotèque","Zapotekisch"),
("zza","Zaza; Dimili; Dimli; Kirdki; Kirmanjki; Zazaki","zaza; dimili; dimli; kirdki; kirmanjki; zazaki","Zazaki"),
("sgn","Sign Languages","langues des signes","Zeichensprachen"),
("zen","Zenaga","zenaga","Zenaga"),
("zha","Zhuang; Chuang","zhuang; chuang","Zhuang"),
("zul","Zulu","zoulou","Zulu-Sprache"),
("zun","Zuni","zuni","Zuñi-Sprache");

