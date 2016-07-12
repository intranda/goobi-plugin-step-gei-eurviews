package de.intranda.goobi.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.StringPair;

import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.annotation.Contribution;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.plugins.ResourceAnnotationPlugin;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class);

    private static final String QUERY_DELETE_FROM = "DELETE FROM ";
    private static final String QUERY_SELECT_FROM = "SELECT * FROM ";
    private static final String QUERY_INSERT_INTO = "INSERT INTO ";
    private static final String QUERY_WHERE = " WHERE ";
    private static final String QUERY_UPDATE = "UPDATE ";

    private static final String TABLE_RESOURCE = "plugin_gei_eurviews_resource";
    private static final String COLUMN_RESOURCE_RESOURCEID = "resourceID";
    private static final String COLUMN_RESOURCE_PROCESSID = "prozesseID";
    private static final String COLUMN_RESOURCE_DOCUMENT_TYPE = "documentType";
    private static final String COLUMN_RESOURCE_MAINTITLE_ORIGINAL = "maintitleOriginal";
    private static final String COLUMN_RESOURCE_SUBTITLE_ORIGINAL = "subtitleOriginal";
    private static final String COLUMN_RESOURCE_PUBLICATIONYEAR = "publicationYear";
    private static final String COLUMN_RESOURCE_NUMBEROFPAGES = "numberOfPages";
    private static final String COLUMN_RESOURCE_SHELFMARK = "shelfmark";
    private static final String COLUMN_RESOURCE_MAINTITLE_GERMAN = "maintitleGerman";
    private static final String COLUMN_RESOURCE_MAINTITLE_ENGLISH = "maintitleEnglish";
    private static final String COLUMN_RESOURCE_PLACEOFPUBLICATION = "placeOfPublication";
    private static final String COLUMN_RESOURCE_VOLUMETITLE_ORIGINAL = "volumeTitleOriginal";
    private static final String COLUMN_RESOURCE_VOLUMETITLE_GERMAN = "volumeTitleGerman";
    private static final String COLUMN_RESOURCE_VOLUMETITLE_ENGLISH = "volumeTitleEnglish";
    private static final String COLUMN_RESOURCE_VOLUME_NUMBER = "volumeNumber";
    private static final String COLUMN_RESOURCE_SCHOOL_SUBJECT = "schoolSubject";
    private static final String COLUMN_RESOURCE_EDUCATION_LEVEL = "educationLevel";
    private static final String COLUMN_RESOURCE_EDITION = "edition";
    private static final String COLUMN_RESOURCE_ISBN = "isbn";
    private static final String COLUMN_RESOURCE_PHYSICALLOCATION = "physicalLocation";
    private static final String COLUMN_RESOURCE_RESOURCETYPE = "resourceType";
    private static final String COLUMN_RESOURCE_RESOURCETITLE_ORIGINAL = "resourceTitleOriginal";
    private static final String COLUMN_RESOURCE_RESOURCETITLE_GERMAN = "resourceTitleGerman";
    private static final String COLUMN_RESOURCE_RESOURCETITLE_ENGLISH = "resourceTitleEnglish";
    private static final String COLUMN_RESOURCE_STARTPAGE = "startPage";
    private static final String COLUMN_RESOURCE_ENDPAGE = "endPage";
    private static final String COLUMN_RESOURCE_SUPPLIER = "supplier";

    private static final String TABLE_IMAGE = "plugin_gei_eurviews_image";
    private static final String COLUMN_IMAGE_IMAGEID = "imageID";
    private static final String COLUMN_IMAGE_PROCESSID = "prozesseID";
    private static final String COLUMN_IMAGE_FILENAME = "fileName";
    private static final String COLUMN_IMAGE_SEQUENCE = "sequence";
    private static final String COLUMN_IMAGE_STRUCTTYPE = "structType";
    private static final String COLUMN_IMAGE_DISPLAYIMAGE = "displayImage";
    private static final String COLUMN_IMAGE_LICENCE = "licence";
    private static final String COLUMN_IMAGE_REPRESNTATIVE = "representative";
    private static final String COLUMN_IMAGE_COPYRIGHT = "copyright";
    private static final String COLUMN_IMAGE_RESOLUTION = "resolution";
    private static final String COLUMN_IMAGE_PLACEHOLDER = "placeholder";

    private static final String TABLE_DESCRIPTION = "plugin_gei_eurviews_context";
    private static final String COLUMN_DESCRIPTION_DESCRIPTIONID = "descriptionID";
    private static final String COLUMN_DESCRIPTION_PROCESSID = "prozesseID";
    private static final String COLUMN_DESCRIPTION_LANGUAGE = "language";
    private static final String COLUMN_DESCRIPTION_SHORTDESCRIPTION = "shortDescription";
    private static final String COLUMN_DESCRIPTION_LONGDESCRIPTION = "longDescription";
    private static final String COLUMN_DESCRIPTION_BOOKINFORMATION = "bookInformation";

    private static final String TABLE_TRANSCRIPTION = "plugin_gei_eurviews_transcription";
    private static final String COLUMN_TRANSCRIPTION_TRANSCRIPTIONID = "transcriptionID";
    private static final String COLUMN_TRANSCRIPTION_PROCESSID = "prozesseID";
    private static final String COLUMN_TRANSCRIPTION_LANGUAGE = "language";
    private static final String COLUMN_TRANSCRIPTION_TRANSCRIPTION = "transcription";
    private static final String COLUMN_TRANSCRIPTION_TRANSLATOR = "author";
    private static final String COLUMN_TRANSCRIPTION_PUBLISHER = "publisher";
    private static final String COLUMN_TRANSCRIPTION_PROJECT = "project";
    private static final String COLUMN_TRANSCRIPTION_APPROVAL = "approval";
    private static final String COLUMN_TRANSCRIPTION_AVAILABILITY = "availability";
    private static final String COLUMN_TRANSCRIPTION_LICENCE = "licence";

    private static final String TABLE_CONTRIBUTIONDESCRIPTION = "plugin_gei_eurviews_contributiondescription";
    private static final String COLUMN_CONTRIBUTIONDESCRIPTION_ID = "id";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_PROCESSID = "prozesseID";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_CONTRIBTUTIONTYPE = "contributionType";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_EDITION = "edition";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_PUBLISHER = "publisher";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_PROJECT = "project";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_AVAILABILITY = "availability";
    private static final String COLUMN_CONTRIBUTIONESCRIPTION_LICENCE = "licence";

    private static final String TABLE_CONTRIBUTION = "plugin_gei_eurviews_contribution";
    private static final String COLUMN_CONTRIBUTION_ID = "id";
    private static final String COLUMN_CONTRIBUTION_PROCESSID = "processId";
    private static final String COLUMN_CONTRIBUTION_TITLE_ORIGINAL = "titleOriginal";
    private static final String COLUMN_CONTRIBUTION_TITLE_TRANSLATION = "titleTranslation";
    private static final String COLUMN_CONTRIBUTION_LANGUAGE_ORIGINAL = "languageOriginal";
    private static final String COLUMN_CONTRIBUTION_LANGUAGE_TRANSLATION = "languageTranslation";
    private static final String COLUMN_CONTRIBUTION_ABSTRACT_ORIGINAL = "abstractOriginal";
    private static final String COLUMN_CONTRIBUTION_ABSTRACT_TRANSLATION = "abstractTranslation";
    private static final String COLUMN_CONTRIBUTION_CONTENT_ORIGINAL = "contentOriginal";
    private static final String COLUMN_CONTRIBUTION_CONTENT_TRANSLATION = "contentTranslation";
    private static final String COLUMN_CONTRIBUTION_NOTE_ORIGINAL = "noteOriginal";
    private static final String COLUMN_CONTRIBUTION_NOTE_TRANSLATION = "noteTranslation";
    private static final String COLUMN_CONTRIBUTION_REFERENCE_ORIGINAL = "referenceOriginal";
    private static final String COLUMN_CONTRIBUTION_REFERENCE_TRANSLATION = "referenceTranslation";

    private static final String TABLE_SOURCE = "plugin_gei_eurviews_source";
    private static final String COLUMN_SOURCE_ID = "resourceId";
    private static final String COLUMN_PROCESSID = "prozesseID";
    private static final String COLUMN_SOURCE_DATA = "data";
    private static final String COLUMN_SOURCE_MAINSOURCE = "mainsource";

    private static final String COLUMN_TOPIC = "topic";

    private static final String COLUMN_KEYWORD = "keyword";

    private static final String TABLE_STRINGS = "plugin_gei_eurviews_resource_stringlist";

    private static final String TABLE_METADATA = "plugin_gei_eurviews_resource_metadatalist";

    private static final String TABLE_KEYWORD = "plugin_gei_eurviews_keyword";

    public static void saveBibliographicData(BibliographicData data) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (data.getResourceID() == null) {
                sql.append(QUERY_INSERT_INTO);
                sql.append(TABLE_RESOURCE);

                sql.append("(");
                sql.append(COLUMN_RESOURCE_PROCESSID);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_DOCUMENT_TYPE);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_MAINTITLE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_SUBTITLE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_PUBLICATIONYEAR);
                sql.append(", ");

                sql.append(COLUMN_RESOURCE_NUMBEROFPAGES);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_SHELFMARK);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_MAINTITLE_GERMAN);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_MAINTITLE_ENGLISH);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_PLACEOFPUBLICATION);
                sql.append(", ");

                sql.append(COLUMN_RESOURCE_VOLUMETITLE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_VOLUMETITLE_GERMAN);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_VOLUMETITLE_ENGLISH);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_VOLUME_NUMBER);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_SCHOOL_SUBJECT);
                sql.append(", ");

                sql.append(COLUMN_RESOURCE_EDUCATION_LEVEL);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_EDITION);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_ISBN);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_PHYSICALLOCATION);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_RESOURCETYPE);
                sql.append(", ");

                sql.append(COLUMN_RESOURCE_RESOURCETITLE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_RESOURCETITLE_GERMAN);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_RESOURCETITLE_ENGLISH);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_STARTPAGE);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_ENDPAGE);
                sql.append(", ");

                sql.append(COLUMN_RESOURCE_SUPPLIER);

                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                Object[] parameter =
                        { data.getProzesseID(), data.getDocumentType(), data.getMaintitleOriginal(), data.getSubtitleOriginal(),
                                data.getPublicationYear(),

                                data.getNumberOfPages(), data.getShelfmark(), data.getMaintitleGerman(), data.getMaintitleEnglish(),
                                data.getPlaceOfPublication(),

                                data.getVolumeTitleOriginal(), data.getVolumeTitleGerman(), data.getVolumeTitleEnglish(), data.getVolumeNumber(),
                                data.getSchoolSubject(),

                                data.getEducationLevel(), data.getEdition(), data.getIsbn(), data.getPhysicalLocation(), data.getResourceType(),

                                data.getResourceTitleOriginal(), data.getResourceTitleGerman(), data.getResourceTitleEnglish(), data.getStartPage(),
                                data.getEndPage(),

                                data.getSupplier()

                        };
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                if (id != null) {
                    data.setResourceID(id);
                }

            } else {
                sql.append(QUERY_UPDATE);
                sql.append(TABLE_RESOURCE);
                sql.append(" SET ");
                sql.append(COLUMN_RESOURCE_PROCESSID);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_DOCUMENT_TYPE);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_MAINTITLE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_SUBTITLE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_PUBLICATIONYEAR);
                sql.append(" = ?, ");

                sql.append(COLUMN_RESOURCE_NUMBEROFPAGES);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_SHELFMARK);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_MAINTITLE_GERMAN);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_MAINTITLE_ENGLISH);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_PLACEOFPUBLICATION);
                sql.append(" = ?, ");

                sql.append(COLUMN_RESOURCE_VOLUMETITLE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_VOLUMETITLE_GERMAN);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_VOLUMETITLE_ENGLISH);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_VOLUME_NUMBER);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_SCHOOL_SUBJECT);
                sql.append(" = ?, ");

                sql.append(COLUMN_RESOURCE_EDUCATION_LEVEL);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_EDITION);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_ISBN);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_PHYSICALLOCATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_RESOURCETYPE);
                sql.append(" = ?, ");

                sql.append(COLUMN_RESOURCE_RESOURCETITLE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_RESOURCETITLE_GERMAN);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_RESOURCETITLE_ENGLISH);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_STARTPAGE);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_ENDPAGE);
                sql.append(" = ?, ");

                sql.append(COLUMN_RESOURCE_SUPPLIER);
                sql.append(" = ? WHERE ");
                sql.append(COLUMN_RESOURCE_RESOURCEID);
                sql.append(" = ? ;");

                Object[] parameter =
                        { data.getProzesseID(), data.getDocumentType(), data.getMaintitleOriginal(), data.getSubtitleOriginal(),
                                data.getPublicationYear(),

                                data.getNumberOfPages(), data.getShelfmark(), data.getMaintitleGerman(), data.getMaintitleEnglish(),
                                data.getPlaceOfPublication(),

                                data.getVolumeTitleOriginal(), data.getVolumeTitleGerman(), data.getVolumeTitleEnglish(), data.getVolumeNumber(),
                                data.getSchoolSubject(),

                                data.getEducationLevel(), data.getEdition(), data.getIsbn(), data.getPhysicalLocation(), data.getResourceType(),

                                data.getResourceTitleOriginal(), data.getResourceTitleGerman(), data.getResourceTitleEnglish(), data.getStartPage(),
                                data.getEndPage(),

                                data.getSupplier(), data.getResourceID() };

                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                run.update(connection, sql.toString(), parameter);
            }

            String delete = "DELETE FROM " + TABLE_STRINGS + " WHERE resourceID = ? AND prozesseID = ?";
            Object[] param = { data.getResourceID(), data.getProzesseID() };
            run.update(connection, delete, param);

            List<SimpleMetadataObject> languageList = data.getLanguageList();
            for (SimpleMetadataObject lang : languageList) {
                insertListItem(run, connection, data.getResourceID(), data.getProzesseID(), "language", lang.getValue());
            }

            List<SimpleMetadataObject> countryList = data.getCountryList();

            for (SimpleMetadataObject country : countryList) {
                insertListItem(run, connection, data.getResourceID(), data.getProzesseID(), "country", country.getValue());
            }

            List<SimpleMetadataObject> stateList = data.getStateList();
            for (SimpleMetadataObject state : stateList) {
                insertListItem(run, connection, data.getResourceID(), data.getProzesseID(), "state", state.getValue());
            }

            delete = "DELETE FROM " + TABLE_METADATA + " WHERE resourceID = ? AND prozesseID = ?";
            run.update(connection, delete, param);
            List<Person> authorList = data.getPersonList();
            for (Person author : authorList) {
                insertMetadata(run, connection, data.getResourceID(), data.getProzesseID(), "book", author);
            }
            authorList = data.getVolumePersonList();
            for (Person author : authorList) {
                insertMetadata(run, connection, data.getResourceID(), data.getProzesseID(), "volume", author);
            }
            authorList = data.getResourceAuthorList();
            for (Person author : authorList) {
                insertMetadata(run, connection, data.getResourceID(), data.getProzesseID(), "resource", author);
            }

            List<Publisher> publisherList = data.getPublisherList();
            for (Publisher publisher : publisherList) {
                insertMetadata(run, connection, data.getResourceID(), data.getProzesseID(), "publisher", publisher);
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static void insertMetadata(QueryRunner run, Connection connection, Integer resourceID, Integer prozesseID, String type,
            ComplexMetadataObject obj) {
        StringBuilder sql = new StringBuilder();

        if (type.equals("publisher")) {
            Publisher pub = (Publisher) obj;
            sql.append(QUERY_INSERT_INTO);
            sql.append(TABLE_METADATA);
            sql.append("(");
            sql.append(COLUMN_RESOURCE_RESOURCEID);
            sql.append(", ");
            sql.append(COLUMN_RESOURCE_PROCESSID);
            sql.append(", type, role, normdataAuthority, normdataValue , firstValue) VALUES (?, ?, ?, ?, ?, ?, ?);");
            Object[] parameter = { resourceID, prozesseID, type, pub.getRole(), pub.getNormdataAuthority(), pub.getNormdataValue(), pub.getName() };
            try {
                run.insert(connection, sql.toString(), dummyHandler, parameter);
            } catch (SQLException e) {
                logger.error(e);
            }

        } else {
            Person aut = (Person) obj;
            sql.append(QUERY_INSERT_INTO);
            sql.append(TABLE_METADATA);
            sql.append(" (");
            sql.append(COLUMN_RESOURCE_RESOURCEID);
            sql.append(", ");
            sql.append(COLUMN_RESOURCE_PROCESSID);
            sql.append(", type, role, normdataAuthority, normdataValue , firstValue, secondValue) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
            Object[] parameter =
                    { resourceID, prozesseID, type, aut.getRole(), aut.getNormdataAuthority(), aut.getNormdataValue(), aut.getFirstName(),
                            aut.getLastName() };
            try {
                run.insert(connection, sql.toString(), dummyHandler, parameter);
            } catch (SQLException e) {
                logger.error(e);
            }
        }

    }

    private static void insertListItem(QueryRunner run, Connection connection, int resourceId, int processId, String type, String value) {
        StringBuilder sql = new StringBuilder();

        sql.append(QUERY_INSERT_INTO);
        sql.append(TABLE_STRINGS);
        sql.append(" (");
        sql.append(COLUMN_RESOURCE_RESOURCEID);
        sql.append(", ");
        sql.append(COLUMN_RESOURCE_PROCESSID);
        sql.append(", type, data ) VALUES (?, ?, ?, ?);");
        Object[] parameter = { resourceId, processId, type, value };
        try {
            run.insert(connection, sql.toString(), dummyHandler, parameter);
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    public static BibliographicData getBibliographicData(Integer processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_RESOURCE);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_RESOURCE_PROCESSID);
        sql.append(" = " + processId);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            BibliographicData ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToBibliographicDataHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static BibliographicData getBibliographicDataByResouceID(String resouceId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_RESOURCE);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_RESOURCE_RESOURCEID);
        sql.append(" = " + resouceId);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            BibliographicData ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToBibliographicDataHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveImages(List<Image> currentImages) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Image curr : currentImages) {
                StringBuilder sql = new StringBuilder();
                if (curr.getImageId() == null) {
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_IMAGE);
                    sql.append(" (");
                    sql.append(COLUMN_IMAGE_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_FILENAME);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_SEQUENCE);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_STRUCTTYPE);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_DISPLAYIMAGE);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_LICENCE);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_REPRESNTATIVE);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_COPYRIGHT);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_RESOLUTION);
                    sql.append(", ");
                    sql.append(COLUMN_IMAGE_PLACEHOLDER);

                    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { curr.getProcessId(), curr.getFileName(), curr.getOrder(),
                                    StringUtils.isEmpty(curr.getStructType()) ? null : curr.getStructType(), curr.isDisplayImage(),
                                    StringUtils.isEmpty(curr.getLicence()) ? null : curr.getLicence(), curr.isRepresentative(), curr.getCopyright(),
                                    curr.getResolution(), curr.getPlaceholder() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                    if (id != null) {
                        curr.setImageId(id);
                    }
                } else {
                    sql.append(QUERY_UPDATE);
                    sql.append(TABLE_IMAGE);
                    sql.append(" SET ");
                    sql.append(COLUMN_IMAGE_PROCESSID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_FILENAME);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_SEQUENCE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_STRUCTTYPE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_DISPLAYIMAGE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_LICENCE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_REPRESNTATIVE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_COPYRIGHT);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_RESOLUTION);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_IMAGE_PLACEHOLDER);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_IMAGE_IMAGEID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { curr.getProcessId(), curr.getFileName(), curr.getOrder(),
                                    StringUtils.isEmpty(curr.getStructType()) ? null : curr.getStructType(), curr.isDisplayImage(),
                                    StringUtils.isEmpty(curr.getLicence()) ? null : curr.getLicence(), curr.isRepresentative(), curr.getCopyright(),
                                    curr.getResolution(), curr.getPlaceholder(), curr.getImageId() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    run.update(connection, sql.toString(), parameter);
                }

            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static List<Image> getImages(int processId) throws SQLException {

        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_IMAGE);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_IMAGE_PROCESSID);
        sql.append(" = " + processId + " ORDER BY " + COLUMN_IMAGE_SEQUENCE);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<Image> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToImageListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveDesciptionList(List<Context> descriptionList) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Context current : descriptionList) {

                StringBuilder sql = new StringBuilder();
                if (current.getDescriptionID() == null) {
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_DESCRIPTION);
                    sql.append(" (");
                    sql.append(COLUMN_DESCRIPTION_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_LANGUAGE);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_BOOKINFORMATION);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_SHORTDESCRIPTION);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_LONGDESCRIPTION);

                    sql.append(") VALUES (?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { current.getProcessID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getBookInformation()) ? null : current.getBookInformation(),
                                    StringUtils.isEmpty(current.getShortDescription()) ? null : current.getShortDescription(),
                                    StringUtils.isEmpty(current.getLongDescription()) ? null : current.getLongDescription() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                    if (id != null) {
                        current.setDescriptionID(id);
                    }
                } else {
                    sql.append(QUERY_UPDATE);
                    sql.append(TABLE_DESCRIPTION);
                    sql.append(" SET ");
                    sql.append(COLUMN_DESCRIPTION_PROCESSID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_LANGUAGE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_BOOKINFORMATION);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_SHORTDESCRIPTION);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_LONGDESCRIPTION);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_DESCRIPTION_DESCRIPTIONID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProcessID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getBookInformation()) ? null : current.getBookInformation(),
                                    StringUtils.isEmpty(current.getShortDescription()) ? null : current.getShortDescription(),
                                    StringUtils.isEmpty(current.getLongDescription()) ? null : current.getLongDescription(),
                                    current.getDescriptionID() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    run.update(connection, sql.toString(), parameter);
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Context> getDescriptionList(Integer processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_DESCRIPTION);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_DESCRIPTION_PROCESSID);
        sql.append(" = " + processId + " ORDER BY " + COLUMN_DESCRIPTION_DESCRIPTIONID);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<Context> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToDescriptionListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static BibliographicData convertBibliographicData(ResultSet rs) throws SQLException {
        Integer resourceId = rs.getInt(COLUMN_RESOURCE_RESOURCEID);
        if (rs.wasNull()) {
            resourceId = null;
        }
        Integer processId = rs.getInt(COLUMN_RESOURCE_PROCESSID);
        if (rs.wasNull()) {
            processId = null;
        }
        BibliographicData data = new BibliographicData(processId);

        data.setResourceID(resourceId);
        data.setDocumentType(rs.getString("documentType"));
        data.setMaintitleOriginal(rs.getString("maintitleOriginal"));
        data.setSubtitleOriginal(rs.getString("subtitleOriginal"));
        data.setPublicationYear(rs.getString("publicationYear"));

        data.setNumberOfPages(rs.getString("numberOfPages"));
        data.setShelfmark(rs.getString("shelfmark"));
        data.setMaintitleGerman(rs.getString("maintitleGerman"));
        data.setMaintitleEnglish(rs.getString("maintitleEnglish"));
        data.setPlaceOfPublication(rs.getString("placeOfPublication"));

        data.setVolumeTitleOriginal(rs.getString("volumeTitleOriginal"));
        data.setVolumeTitleGerman(rs.getString("volumeTitleGerman"));
        data.setVolumeTitleEnglish(rs.getString("volumeTitleEnglish"));
        data.setVolumeNumber(rs.getString("volumeNumber"));
        data.setSchoolSubject(rs.getString("schoolSubject"));

        data.setEducationLevel(rs.getString("educationLevel"));
        data.setEdition(rs.getString("edition"));
        data.setIsbn(rs.getString("isbn"));
        data.setPhysicalLocation(rs.getString("physicalLocation"));
        data.setResourceType(rs.getString("resourceType"));

        data.setResourceTitleOriginal(rs.getString("resourceTitleOriginal"));
        data.setResourceTitleGerman(rs.getString("resourceTitleGerman"));
        data.setResourceTitleEnglish(rs.getString("resourceTitleEnglish"));
        data.setStartPage(rs.getString("startPage"));
        data.setEndPage(rs.getString("endPage"));

        data.setSupplier(rs.getString("supplier"));

        getLists(data);

        return data;
    }

    private static void getLists(BibliographicData data) throws SQLException {
        String sql = "SELECT data FROM " + TABLE_STRINGS + " WHERE resourceID = ? AND prozesseID = ? AND type = ?";
        Connection connection = null;

        String metadata = "SELECT * FROM " + TABLE_METADATA + " WHERE resourceID = ? AND prozesseID = ? AND type = ?";

        try {
            Object[] lparameter = { data.getResourceID(), data.getProzesseID(), "language" };
            Object[] cparameter = { data.getResourceID(), data.getProzesseID(), "country" };
            Object[] sparameter = { data.getResourceID(), data.getProzesseID(), "state" };
            connection = MySQLHelper.getInstance().getConnection();

            List<String> languages = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler, lparameter);
            for (String s : languages) {
                data.addLanguage(new SimpleMetadataObject(s));
            }

            List<String> countries = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler, cparameter);
            for (String s : countries) {
                data.addCountry(new SimpleMetadataObject(s));
            }

            List<String> states = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler, sparameter);
            for (String s : states) {
                data.addState(new SimpleMetadataObject(s));
            }

            Object[] bookAuthor = { data.getResourceID(), data.getProzesseID(), "book" };
            Object[] volumeAuthor = { data.getResourceID(), data.getProzesseID(), "volume" };
            Object[] resourceAuthor = { data.getResourceID(), data.getProzesseID(), "resource" };
            Object[] publisher = { data.getResourceID(), data.getProzesseID(), "publisher" };

            List<Person> book = new QueryRunner().query(connection, metadata, DatabaseManager.resultSetToPersonListHandler, bookAuthor);
            data.setPersonList(book);

            List<Person> vol = new QueryRunner().query(connection, metadata, DatabaseManager.resultSetToPersonListHandler, volumeAuthor);
            data.setVolumePersonList(vol);

            List<Person> res = new QueryRunner().query(connection, metadata, DatabaseManager.resultSetToPersonListHandler, resourceAuthor);
            data.setResourceAuthorList(res);

            List<Publisher> pub = new QueryRunner().query(connection, metadata, DatabaseManager.resultSetToPublisherListHandler, publisher);
            data.setPublisherList(pub);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static ResultSetHandler<List<Person>> resultSetToPersonListHandler = new ResultSetHandler<List<Person>>() {
        @Override
        public List<Person> handle(ResultSet rs) throws SQLException {
            try {
                List<Person> answer = new ArrayList<>();
                while (rs.next()) {
                    Person aut = new Person();

                    aut.setRole(rs.getString("role"));
                    aut.setNormdataAuthority(rs.getString("normdataAuthority"));
                    aut.setNormdataValue(rs.getString("normdataValue"));
                    aut.setFirstName(rs.getString("firstValue"));
                    aut.setLastName(rs.getString("secondValue"));
                    answer.add(aut);
                }
                return answer;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    private static ResultSetHandler<List<Publisher>> resultSetToPublisherListHandler = new ResultSetHandler<List<Publisher>>() {
        @Override
        public List<Publisher> handle(ResultSet rs) throws SQLException {
            try {
                List<Publisher> answer = new ArrayList<>();
                while (rs.next()) {
                    Publisher pub = new Publisher();

                    pub.setRole(rs.getString("role"));
                    pub.setNormdataAuthority(rs.getString("normdataAuthority"));
                    pub.setNormdataValue(rs.getString("normdataValue"));
                    pub.setName(rs.getString("firstValue"));

                    answer.add(pub);
                }
                return answer;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    private static ResultSetHandler<List<BibliographicData>> resultSetToBibliographicDataListHandler =
            new ResultSetHandler<List<BibliographicData>>() {
                @Override
                public List<BibliographicData> handle(ResultSet rs) throws SQLException {
                    try {
                        List<BibliographicData> answer = new ArrayList<BibliographicData>();

                        while (rs.next()) {
                            answer.add(convertBibliographicData(rs));
                        }

                        return answer;
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }
                    }
                }
            };

    private static ResultSetHandler<List<String>> resultSetToStringListHandler = new ResultSetHandler<List<String>>() {
        @Override
        public List<String> handle(ResultSet rs) throws SQLException {
            try {
                List<String> answer = new ArrayList<String>();

                while (rs.next()) {
                    answer.add(rs.getString("data"));
                }

                return answer;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    private static ResultSetHandler<BibliographicData> resultSetToBibliographicDataHandler = new ResultSetHandler<BibliographicData>() {
        @Override
        public BibliographicData handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return convertBibliographicData(rs);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    private static ResultSetHandler<List<Image>> resultSetToImageListHandler = new ResultSetHandler<List<Image>>() {

        public List<Image> handle(ResultSet rs) throws SQLException {

            List<Image> answer = new ArrayList<Image>();

            try {
                while (rs.next()) {
                    Image image = new Image(rs.getInt(COLUMN_IMAGE_PROCESSID));
                    image.setImageId(rs.getInt(COLUMN_IMAGE_IMAGEID));
                    image.setFileName(rs.getString(COLUMN_IMAGE_FILENAME));
                    image.setOrder(rs.getInt(COLUMN_IMAGE_SEQUENCE));
                    image.setStructType(rs.getString(COLUMN_IMAGE_STRUCTTYPE));
                    image.setDisplayImage(rs.getBoolean(COLUMN_IMAGE_DISPLAYIMAGE));
                    image.setLicence(rs.getString(COLUMN_IMAGE_LICENCE));
                    image.setRepresentative(rs.getBoolean(COLUMN_IMAGE_REPRESNTATIVE));
                    image.setCopyright(rs.getString(COLUMN_IMAGE_COPYRIGHT));
                    image.setResolution(rs.getString(COLUMN_IMAGE_RESOLUTION));
                    image.setPlaceholder(rs.getString(COLUMN_IMAGE_PLACEHOLDER));
                    answer.add(image);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        };
    };

    private static ResultSetHandler<List<Context>> resultSetToDescriptionListHandler = new ResultSetHandler<List<Context>>() {

        public List<Context> handle(ResultSet rs) throws SQLException {
            List<Context> answer = new ArrayList<Context>();
            try {
                while (rs.next()) {
                    Context desc = new Context(rs.getInt(COLUMN_DESCRIPTION_PROCESSID));
                    desc.setDescriptionID(rs.getInt(COLUMN_DESCRIPTION_DESCRIPTIONID));
                    desc.setLanguage(rs.getString(COLUMN_DESCRIPTION_LANGUAGE));
                    desc.setShortDescription(rs.getString(COLUMN_DESCRIPTION_SHORTDESCRIPTION));
                    desc.setLongDescription(rs.getString(COLUMN_DESCRIPTION_LONGDESCRIPTION));
                    desc.setBookInformation(rs.getString(COLUMN_DESCRIPTION_BOOKINFORMATION));
                    answer.add(desc);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        };
    };

    private static ResultSetHandler<List<Transcription>> resultSetToTranscriptionListHandler = new ResultSetHandler<List<Transcription>>() {

        public List<Transcription> handle(ResultSet rs) throws SQLException {
            List<Transcription> answer = new ArrayList<Transcription>();
            try {
                while (rs.next()) {
                    Transcription trans = new Transcription(rs.getInt(COLUMN_TRANSCRIPTION_PROCESSID));
                    trans.setTranscriptionID(rs.getInt(COLUMN_TRANSCRIPTION_TRANSCRIPTIONID));
                    trans.setLanguage(rs.getString(COLUMN_TRANSCRIPTION_LANGUAGE));
                    trans.setTranscription(rs.getString(COLUMN_TRANSCRIPTION_TRANSCRIPTION));
                    String translator = rs.getString(COLUMN_TRANSCRIPTION_TRANSLATOR);
                    if (StringUtils.isNotBlank(translator)) {
                        String[] translators = translator.split(";");
                        for (String s : translators) {
                            trans.addNewTranslator(new SimpleMetadataObject(s));
                        }
                    }
                    trans.setPublisher(rs.getString(COLUMN_TRANSCRIPTION_PUBLISHER));
                    trans.setProject(rs.getString(COLUMN_TRANSCRIPTION_PROJECT));
                    trans.setApproval(rs.getString(COLUMN_TRANSCRIPTION_APPROVAL));
                    trans.setAvailability(rs.getString(COLUMN_TRANSCRIPTION_AVAILABILITY));
                    trans.setLicence(rs.getString(COLUMN_TRANSCRIPTION_LICENCE));

                    answer.add(trans);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        };
    };

    public static List<Transcription> getTransciptionList(Integer processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_TRANSCRIPTION);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_TRANSCRIPTION_PROCESSID);
        sql.append(" = " + processId + " ORDER BY " + COLUMN_TRANSCRIPTION_TRANSCRIPTIONID);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<Transcription> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToTranscriptionListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void saveTranscriptionList(List<Transcription> transcriptionList) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Transcription current : transcriptionList) {
                StringBuilder sql = new StringBuilder();
                String trans = "";
                if (!current.getTranslatorList().isEmpty()) {
                    for (SimpleMetadataObject translator : current.getTranslatorList()) {
                        if (StringUtils.isNotBlank(trans)) {
                            trans += ";";
                        }
                        trans += translator.getValue();
                    }
                }
                if (current.getTranscriptionID() == null) {
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_TRANSCRIPTION);
                    sql.append(" (");
                    sql.append(COLUMN_TRANSCRIPTION_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_LANGUAGE);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_TRANSCRIPTION);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_TRANSLATOR);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_PUBLISHER);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_PROJECT);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_APPROVAL);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_AVAILABILITY);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_LICENCE);
                    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { current.getProzesseID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTranscription()) ? null : current.getTranscription(), trans,
                                    StringUtils.isEmpty(current.getPublisher()) ? null : current.getPublisher(),
                                    StringUtils.isEmpty(current.getProject()) ? null : current.getProject(),
                                    StringUtils.isEmpty(current.getApproval()) ? null : current.getApproval(),
                                    StringUtils.isEmpty(current.getAvailability()) ? null : current.getAvailability(),
                                    StringUtils.isEmpty(current.getLicence()) ? null : current.getLicence() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                    if (id != null) {
                        current.setTranscriptionID(id);
                    }
                } else {
                    sql.append(QUERY_UPDATE);
                    sql.append(TABLE_TRANSCRIPTION);
                    sql.append(" SET ");
                    sql.append(COLUMN_TRANSCRIPTION_PROCESSID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_TRANSCRIPTION_LANGUAGE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_TRANSCRIPTION_TRANSCRIPTION);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_TRANSCRIPTION_TRANSLATOR);
                    sql.append(" =?, ");
                    sql.append(COLUMN_TRANSCRIPTION_PUBLISHER);
                    sql.append(" =?, ");
                    sql.append(COLUMN_TRANSCRIPTION_PROJECT);
                    sql.append(" =?, ");
                    sql.append(COLUMN_TRANSCRIPTION_APPROVAL);
                    sql.append(" =?, ");
                    sql.append(COLUMN_TRANSCRIPTION_AVAILABILITY);
                    sql.append(" =?, ");
                    sql.append(COLUMN_TRANSCRIPTION_LICENCE);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_TRANSCRIPTION_TRANSCRIPTIONID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProzesseID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTranscription()) ? null : current.getTranscription(), trans,
                                    StringUtils.isEmpty(current.getPublisher()) ? null : current.getPublisher(),
                                    StringUtils.isEmpty(current.getProject()) ? null : current.getProject(),
                                    StringUtils.isEmpty(current.getApproval()) ? null : current.getApproval(),
                                    StringUtils.isEmpty(current.getAvailability()) ? null : current.getAvailability(),
                                    StringUtils.isEmpty(current.getLicence()) ? null : current.getLicence(), current.getTranscriptionID() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    run.update(connection, sql.toString(), parameter);
                }

            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteTranscription(Transcription currentTranscription) throws SQLException {
        if (currentTranscription.getTranscriptionID() != null) {
            String sql =
                    QUERY_DELETE_FROM + TABLE_TRANSCRIPTION + QUERY_WHERE + COLUMN_TRANSCRIPTION_TRANSCRIPTIONID + " = "
                            + currentTranscription.getTranscriptionID();
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql.toString());

            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteDescription(Context currentDescription) throws SQLException {
        if (currentDescription.getDescriptionID() != null) {
            String sql =
                    QUERY_DELETE_FROM + TABLE_DESCRIPTION + QUERY_WHERE + COLUMN_DESCRIPTION_DESCRIPTIONID + " = "
                            + currentDescription.getDescriptionID();
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql.toString());

            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }

        }

    }

    public static List<BibliographicData> getBibliographicData(String query) throws SQLException {
        String sql = QUERY_SELECT_FROM + TABLE_RESOURCE;
        if (!StringUtils.isEmpty(query)) {
            sql +=
                    QUERY_WHERE + COLUMN_RESOURCE_MAINTITLE_ORIGINAL + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%'" + " OR "
                            + COLUMN_RESOURCE_MAINTITLE_ENGLISH + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%'" + " OR "
                            + COLUMN_RESOURCE_MAINTITLE_ORIGINAL + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%'" + " OR "
                            + COLUMN_RESOURCE_RESOURCEID + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%';";
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            List<BibliographicData> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToBibliographicDataListHandler);

            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveContribution(Contribution contribution, int processId) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (contribution.getContributionId() == null) {
                sql.append(QUERY_INSERT_INTO);
                sql.append(TABLE_CONTRIBUTION);
                sql.append("(");
                sql.append(COLUMN_CONTRIBUTION_PROCESSID);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_TITLE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_TITLE_TRANSLATION);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_LANGUAGE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_LANGUAGE_TRANSLATION);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_ABSTRACT_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_ABSTRACT_TRANSLATION);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_CONTENT_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_CONTENT_TRANSLATION);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_NOTE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_NOTE_TRANSLATION);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_REFERENCE_ORIGINAL);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTION_REFERENCE_TRANSLATION);
                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                Object[] parameter =
                        { contribution.getProcessId(), StringUtils.isEmpty(contribution.getTitleOriginal()) ? null : contribution.getTitleOriginal(),
                                StringUtils.isEmpty(contribution.getTitleTranslation()) ? null : contribution.getTitleTranslation(),

                                StringUtils.isEmpty(contribution.getLanguageOriginal()) ? null : contribution.getLanguageOriginal(),
                                StringUtils.isEmpty(contribution.getLanguageTranslation()) ? null : contribution.getLanguageTranslation(),
                                StringUtils.isEmpty(contribution.getAbstractOriginal()) ? null : contribution.getAbstractOriginal(),
                                StringUtils.isEmpty(contribution.getAbstractTranslation()) ? null : contribution.getAbstractTranslation(),
                                StringUtils.isEmpty(contribution.getContentOriginal()) ? null : contribution.getContentOriginal(),
                                StringUtils.isEmpty(contribution.getContentTranslation()) ? null : contribution.getContentTranslation(),
                                StringUtils.isEmpty(contribution.getNoteOriginal()) ? null : contribution.getNoteOriginal(),
                                StringUtils.isEmpty(contribution.getNoteTranslation()) ? null : contribution.getNoteTranslation(),
                                StringUtils.isEmpty(contribution.getReferenceOriginal()) ? null : contribution.getReferenceOriginal(),
                                StringUtils.isEmpty(contribution.getReferenceTranslation()) ? null : contribution.getReferenceTranslation() };
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                if (id != null) {
                    contribution.setContributionId(id);
                }

            } else {
                sql.append(QUERY_UPDATE);
                sql.append(TABLE_CONTRIBUTION);
                sql.append(" SET ");
                sql.append(COLUMN_CONTRIBUTION_PROCESSID);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_TITLE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_TITLE_TRANSLATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_LANGUAGE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_LANGUAGE_TRANSLATION);
                sql.append(" = ?, ");

                sql.append(COLUMN_CONTRIBUTION_ABSTRACT_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_ABSTRACT_TRANSLATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_CONTENT_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_CONTENT_TRANSLATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_NOTE_ORIGINAL);
                sql.append(" = ?, ");

                sql.append(COLUMN_CONTRIBUTION_NOTE_TRANSLATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_REFERENCE_ORIGINAL);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTION_REFERENCE_TRANSLATION);
                sql.append(" = ? WHERE ");
                sql.append(COLUMN_CONTRIBUTION_ID);
                sql.append(" = ? ;");

                Object[] parameter =
                        { contribution.getProcessId(), StringUtils.isEmpty(contribution.getTitleOriginal()) ? null : contribution.getTitleOriginal(),
                                StringUtils.isEmpty(contribution.getTitleTranslation()) ? null : contribution.getTitleTranslation(),
                                StringUtils.isEmpty(contribution.getLanguageOriginal()) ? null : contribution.getLanguageOriginal(),
                                StringUtils.isEmpty(contribution.getLanguageTranslation()) ? null : contribution.getLanguageTranslation(),
                                StringUtils.isEmpty(contribution.getAbstractOriginal()) ? null : contribution.getAbstractOriginal(),
                                StringUtils.isEmpty(contribution.getAbstractTranslation()) ? null : contribution.getAbstractTranslation(),
                                StringUtils.isEmpty(contribution.getContentOriginal()) ? null : contribution.getContentOriginal(),
                                StringUtils.isEmpty(contribution.getContentTranslation()) ? null : contribution.getContentTranslation(),
                                StringUtils.isEmpty(contribution.getNoteOriginal()) ? null : contribution.getNoteOriginal(),
                                StringUtils.isEmpty(contribution.getNoteTranslation()) ? null : contribution.getNoteTranslation(),
                                StringUtils.isEmpty(contribution.getReferenceOriginal()) ? null : contribution.getReferenceOriginal(),
                                StringUtils.isEmpty(contribution.getReferenceTranslation()) ? null : contribution.getReferenceTranslation(),
                                contribution.getContributionId() };

                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                run.update(connection, sql.toString(), parameter);
            }

            String delete = "DELETE FROM " + TABLE_STRINGS + " WHERE resourceID = ? AND prozesseID = ?";
            Object[] param = { contribution.getContributionId(), contribution.getProcessId() };
            run.update(connection, delete, param);

            List<SimpleMetadataObject> translatorListOriginal = contribution.getTranslatorListOriginal();
            for (SimpleMetadataObject lang : translatorListOriginal) {
                insertListItem(run, connection, contribution.getContributionId(), contribution.getProcessId(), "translatorOriginal", lang.getValue());
            }

            List<SimpleMetadataObject> translatorListTranslation = contribution.getTranslatorListTranslation();

            for (SimpleMetadataObject country : translatorListTranslation) {
                insertListItem(run, connection, contribution.getContributionId(), contribution.getProcessId(), "translatorTranslation", country
                        .getValue());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Contribution getContribution(int processId) throws SQLException {

        String sql = QUERY_SELECT_FROM + TABLE_CONTRIBUTION + QUERY_WHERE + COLUMN_CONTRIBUTION_PROCESSID + " = " + processId;
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }
            Contribution contribution = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToContributionHandler);
            if (contribution != null) {
                sql = "SELECT data FROM " + TABLE_STRINGS + " WHERE resourceID = ? AND prozesseID = ? AND type = ?";

                Object[] lparameter = { contribution.getContributionId(), contribution.getProcessId(), "translatorOriginal" };
                Object[] cparameter = { contribution.getContributionId(), contribution.getProcessId(), "translatorTranslation" };

                List<String> languages = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler, lparameter);
                for (String s : languages) {
                    contribution.addTranslatorOriginal(new SimpleMetadataObject(s));
                }

                List<String> countries = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler, cparameter);
                for (String s : countries) {
                    contribution.addTranslatorTranslation(new SimpleMetadataObject(s));
                }
            }
            return contribution;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static ResultSetHandler<Contribution> resultSetToContributionHandler = new ResultSetHandler<Contribution>() {
        @Override
        public Contribution handle(ResultSet rs) throws SQLException {
            try {

                if (rs.next()) {
                    Contribution contribution = new Contribution(rs.getInt(COLUMN_CONTRIBUTION_PROCESSID));
                    contribution.setContributionId(rs.getInt(COLUMN_CONTRIBUTION_ID));

                    contribution.setTitleOriginal(rs.getString(COLUMN_CONTRIBUTION_TITLE_ORIGINAL));
                    contribution.setTitleTranslation(rs.getString(COLUMN_CONTRIBUTION_TITLE_TRANSLATION));
                    contribution.setLanguageOriginal(rs.getString(COLUMN_CONTRIBUTION_LANGUAGE_ORIGINAL));
                    contribution.setLanguageTranslation(rs.getString(COLUMN_CONTRIBUTION_LANGUAGE_TRANSLATION));
                    contribution.setAbstractOriginal(rs.getString(COLUMN_CONTRIBUTION_ABSTRACT_ORIGINAL));
                    contribution.setAbstractTranslation(rs.getString(COLUMN_CONTRIBUTION_ABSTRACT_TRANSLATION));
                    contribution.setContentOriginal(rs.getString(COLUMN_CONTRIBUTION_CONTENT_ORIGINAL));
                    contribution.setContentTranslation(rs.getString(COLUMN_CONTRIBUTION_CONTENT_TRANSLATION));
                    contribution.setNoteOriginal(rs.getString(COLUMN_CONTRIBUTION_NOTE_ORIGINAL));
                    contribution.setNoteTranslation(rs.getString(COLUMN_CONTRIBUTION_NOTE_TRANSLATION));
                    contribution.setReferenceOriginal(rs.getString(COLUMN_CONTRIBUTION_REFERENCE_ORIGINAL));
                    contribution.setReferenceTranslation(rs.getString(COLUMN_CONTRIBUTION_REFERENCE_TRANSLATION));

                    return contribution;
                }

                return null;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    public static void saveSourceList(List<Source> list, int processId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            String delete = QUERY_DELETE_FROM + TABLE_SOURCE + QUERY_WHERE + COLUMN_PROCESSID + " = " + processId;
            // first delete old categories

            run.update(connection, delete);
            if (list != null) {
                for (Source current : list) {
                    StringBuilder sql = new StringBuilder();
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_SOURCE);
                    sql.append(" (");
                    sql.append(COLUMN_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_SOURCE_DATA);
                    sql.append(", ");
                    sql.append(COLUMN_SOURCE_MAINSOURCE);
                    sql.append(") VALUES (?, ?, ?)");

                    Object[] parameter = { processId, current.getData() == null ? null : current.getData().getProzesseID(), current.isMainSource() };

                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static List<Source> getSourceList(int processId) throws SQLException {
        String sql = QUERY_SELECT_FROM + TABLE_SOURCE + QUERY_WHERE + COLUMN_PROCESSID + " = " + processId;

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }

            List<Source> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToSourceListHandler);

            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static ResultSetHandler<List<Source>> resultSetToSourceListHandler = new ResultSetHandler<List<Source>>() {
        @Override
        public List<Source> handle(ResultSet rs) throws SQLException {
            try {
                List<Source> answer = new ArrayList<Source>();

                while (rs.next()) {
                    Source source = new Source(rs.getInt(COLUMN_PROCESSID));
                    source.setResourceId(rs.getInt(COLUMN_SOURCE_ID));
                    Integer dataId = rs.getInt(COLUMN_SOURCE_DATA);
                    if (rs.wasNull()) {
                        dataId = null;
                    } else {
                        source.setData(getBibliographicData(dataId));
                    }
                    source.setMainSource(rs.getBoolean(COLUMN_SOURCE_MAINSOURCE));
                    answer.add(source);
                }

                return answer;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    private static ResultSetHandler<Object> dummyHandler = new ResultSetHandler<Object>() {
        @Override
        public Object handle(ResultSet rs) throws SQLException {
            return null;
        }
    };

    private static ResultSetHandler<List<StringPair>> resultSetToTopicListHandler = new ResultSetHandler<List<StringPair>>() {

        @Override
        public List<StringPair> handle(ResultSet rs) throws SQLException {
            List<StringPair> answer = new ArrayList<>();
            while (rs.next()) {

                String topicName = rs.getString(COLUMN_TOPIC);
                String keywordName = rs.getString(COLUMN_KEYWORD);
                StringPair sp = new StringPair(topicName, keywordName);

                answer.add(sp);
            }
            return answer;
        }
    };

    public static List<StringPair> getKeywordList(Integer processId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = QUERY_SELECT_FROM + TABLE_KEYWORD + QUERY_WHERE + COLUMN_PROCESSID + " = " + processId;
            List<StringPair> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToTopicListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveKeywordList(List<Topic> topicList, Integer processId) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            String delete = QUERY_DELETE_FROM + TABLE_KEYWORD + QUERY_WHERE + COLUMN_PROCESSID + " = " + processId;
            run.update(connection, delete);
            for (Topic topic : topicList) {
                for (Keyword keyword : topic.getKeywordList()) {
                    if (keyword.isSelected()) {
                        // save
                        StringBuilder sql = new StringBuilder();
                        sql.append(QUERY_INSERT_INTO);
                        sql.append(TABLE_KEYWORD);
                        sql.append(" (");
                        sql.append(COLUMN_PROCESSID);
                        sql.append(", ");
                        sql.append(COLUMN_TOPIC);
                        sql.append(", ");
                        sql.append(COLUMN_KEYWORD);
                        sql.append(") VALUES (?, ?, ?)");
                        Object[] parameter = { processId, topic.getNameDE(), keyword.getKeywordNameDE() };
                        if (logger.isDebugEnabled()) {
                            logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                        }
                        run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                    }
                }
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void saveContribtutionDescription(ResourceAnnotationPlugin plugin) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            if (plugin.getId() == null) {
                // insert
                sql.append(QUERY_INSERT_INTO);
                sql.append(TABLE_CONTRIBUTIONDESCRIPTION);
                sql.append(" (");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PROCESSID);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_CONTRIBTUTIONTYPE);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_EDITION);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PUBLISHER);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PROJECT);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_AVAILABILITY);
                sql.append(", ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_LICENCE);
                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?)");

                Object[] parameter =
                        { plugin.getProcessId(), StringUtils.isEmpty(plugin.getContributionType()) ? null : plugin.getContributionType(),
                                StringUtils.isEmpty(plugin.getEdition()) ? null : plugin.getEdition(),
                                StringUtils.isEmpty(plugin.getPublisher()) ? null : plugin.getPublisher(),
                                StringUtils.isEmpty(plugin.getProject()) ? null : plugin.getProject(),
                                StringUtils.isEmpty(plugin.getAvailability()) ? null : plugin.getAvailability(),
                                StringUtils.isEmpty(plugin.getLicence()) ? null : plugin.getLicence() };
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                if (id != null) {
                    plugin.setId(id);
                }
            } else {
                // update
                sql.append(QUERY_UPDATE);
                sql.append(TABLE_CONTRIBUTIONDESCRIPTION);
                sql.append(" SET ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PROCESSID);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_CONTRIBTUTIONTYPE);
                sql.append(" = ?, ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_EDITION);
                sql.append(" =?, ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PUBLISHER);
                sql.append(" =?, ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PROJECT);
                sql.append(" =?, ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_AVAILABILITY);
                sql.append(" =?, ");
                sql.append(COLUMN_CONTRIBUTIONESCRIPTION_LICENCE);
                sql.append(" = ? WHERE ");
                sql.append(COLUMN_CONTRIBUTIONDESCRIPTION_ID);
                sql.append(" = ? ;");

                Object[] parameter =
                        { plugin.getProcessId(), StringUtils.isEmpty(plugin.getContributionType()) ? null : plugin.getContributionType(),
                                StringUtils.isEmpty(plugin.getEdition()) ? null : plugin.getEdition(),
                                StringUtils.isEmpty(plugin.getPublisher()) ? null : plugin.getPublisher(),
                                StringUtils.isEmpty(plugin.getProject()) ? null : plugin.getProject(),
                                StringUtils.isEmpty(plugin.getAvailability()) ? null : plugin.getAvailability(),
                                StringUtils.isEmpty(plugin.getLicence()) ? null : plugin.getLicence(), plugin.getId() };
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                run.update(connection, sql.toString(), parameter);
            }
            String delete = "DELETE FROM " + TABLE_METADATA + " WHERE resourceID = ? AND prozesseID = ?";
            Object[] param = { plugin.getId(), plugin.getProcessId() };
            run.update(connection, delete, param);
            List<Person> authorList = plugin.getAuthorList();
            for (Person author : authorList) {
                insertMetadata(run, connection, plugin.getId(), plugin.getProcessId(), "contribution", author);
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void getContributionDescription(ResourceAnnotationPlugin plugin) throws SQLException {

        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_CONTRIBUTIONDESCRIPTION);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_CONTRIBUTIONESCRIPTION_PROCESSID);
        sql.append(" = " + plugin.getProcessId());
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            Map<String, String> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToMapHandler);
            if (!ret.isEmpty()) {
                plugin.setId(Integer.parseInt(ret.get(COLUMN_CONTRIBUTIONDESCRIPTION_ID)));
                plugin.setContributionType(ret.get(COLUMN_CONTRIBUTIONESCRIPTION_CONTRIBTUTIONTYPE));
                plugin.setEdition(ret.get(COLUMN_CONTRIBUTIONESCRIPTION_EDITION));
                plugin.setPublisher(ret.get(COLUMN_CONTRIBUTIONESCRIPTION_PUBLISHER));
                plugin.setProject(ret.get(COLUMN_CONTRIBUTIONESCRIPTION_PROJECT));
                plugin.setAvailability(ret.get(COLUMN_CONTRIBUTIONESCRIPTION_AVAILABILITY));
                plugin.setLicence(ret.get(COLUMN_CONTRIBUTIONESCRIPTION_LICENCE));
            }
            String metadata = "SELECT * FROM " + TABLE_METADATA + " WHERE resourceID = ? AND prozesseID = ? AND type = ?";

            Object[] parameter = { plugin.getId(), plugin.getProcessId(), "contribution" };
            List<Person> per = new QueryRunner().query(connection, metadata, DatabaseManager.resultSetToPersonListHandler, parameter);
            plugin.setAuthorList(per);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static ResultSetHandler<Map<String, String>> resultSetToMapHandler = new ResultSetHandler<Map<String, String>>() {
        @Override
        public Map<String, String> handle(ResultSet rs) throws SQLException {
            try {
                Map<String, String> answer = new HashMap<String, String>();
                if (rs.next()) {
                    answer.put(COLUMN_CONTRIBUTIONDESCRIPTION_ID, "" + rs.getInt(COLUMN_CONTRIBUTIONDESCRIPTION_ID));
                    answer.put(COLUMN_CONTRIBUTIONESCRIPTION_CONTRIBTUTIONTYPE, rs.getString(COLUMN_CONTRIBUTIONESCRIPTION_CONTRIBTUTIONTYPE));
                    answer.put(COLUMN_CONTRIBUTIONESCRIPTION_EDITION, rs.getString(COLUMN_CONTRIBUTIONESCRIPTION_EDITION));
                    answer.put(COLUMN_CONTRIBUTIONESCRIPTION_PUBLISHER, rs.getString(COLUMN_CONTRIBUTIONESCRIPTION_PUBLISHER));
                    answer.put(COLUMN_CONTRIBUTIONESCRIPTION_PROJECT, rs.getString(COLUMN_CONTRIBUTIONESCRIPTION_PROJECT));
                    answer.put(COLUMN_CONTRIBUTIONESCRIPTION_AVAILABILITY, rs.getString(COLUMN_CONTRIBUTIONESCRIPTION_AVAILABILITY));
                    answer.put(COLUMN_CONTRIBUTIONESCRIPTION_LICENCE, rs.getString(COLUMN_CONTRIBUTIONESCRIPTION_LICENCE));
                }
                return answer;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    /* 
    CREATE TABLE `goobi`.`plugin_gei_eurviews_source` (
    `resourceId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `data` varchar(255) DEFAULT NULL,
    `mainsource` bit(1) DEFAULT false,
    PRIMARY KEY (`resourceId`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */

    /* 
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
     */

    /* 
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
     */

    /* 
    CREATE TABLE `goobi`.`plugin_gei_eurviews_categories` (
    `catId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `german` varchar(255) DEFAULT NULL,
    `english` varchar(255) DEFAULT NULL,
    `french` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`catId`)
     ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */

    /* 
    CREATE TABLE `goobi`.`plugin_gei_eurviews_keywords` (
    `keyId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `german` varchar(255) DEFAULT NULL,
    `english` varchar(255) DEFAULT NULL,
    `french` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`keyId`)
     ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */

    /* 
    CREATE TABLE `goobi`.`plugin_gei_eurviews_category` (
    `categoryId` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`categoryId`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */

    /* 
    CREATE TABLE `goobi`.`plugin_gei_eurviews_keyword` (
    `keywordID` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `prozesseID` int(10) unsigned NOT NULL DEFAULT '0',
    `value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`keywordID`),
    KEY `prozesseID` (`prozesseID`)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
     */

    /* 
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

     */

    /* 
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
     */

    /* 
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
     */

    /* 
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
     */

    // UPDATE 5.6.2015
    /*
    alter table `goobi`.`plugin_gei_eurviews_author` add column mail2 varchar(255) default null;
    alter table `goobi`.`plugin_gei_eurviews_author` add column mail3 varchar(255) default null;
    
    alter table `goobi`.`plugin_gei_eurviews_source` MODIFY data INTEGER;
    
    alter table `goobi`.`plugin_gei_eurviews_annotation` add column classification varchar(255) default null;
    alter table `goobi`.`plugin_gei_eurviews_annotation` add column footnote text default null;
    
    */

    // Update Januar 2016

    /*
     
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
     */

    /*

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

     */

    /* 
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
     */

    /* 
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
     */

    /*
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



         */

}
