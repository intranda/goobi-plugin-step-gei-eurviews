package de.intranda.goobi.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.intranda.goobi.model.annotation.Annotation;
import de.intranda.goobi.model.annotation.Author;
import de.intranda.goobi.model.annotation.Source;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Description;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Transcription;
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
    private static final String COLUMN_RESOURCE_MAIN_TITLE = "maintitle";
    private static final String COLUMN_RESOURCE_SUB_TITLE = "subtitle";
    private static final String COLUMN_RESOURCE_AUTHOR_FIRSTNAME = "authorFirstname";
    private static final String COLUMN_RESOURCE_AUTHOR_LASTNAME = "authorLastname";
    private static final String COLUMN_RESOURCE_LANGUAGE = "language";
    private static final String COLUMN_RESOURCE_PUBLISHER = "publisher";
    private static final String COLUMN_RESOURCE_PLACE_OF_PUBLICATION = "placeOfPublication";
    private static final String COLUMN_RESOURCE_PUBLICATION_YEAR = "publicationYear";
    private static final String COLUMN_RESOURCE_NUMBER_OF_PAGES = "numberOfPages";
    private static final String COLUMN_RESOURCE_SHELFMARK = "shelfmark";
    private static final String COLUMN_RESOURCE_COPYRIGHT = "copyright";

    private static final String TABLE_IMAGE = "plugin_gei_eurviews_image";
    private static final String COLUMN_IMAGE_IMAGEID = "imageID";
    private static final String COLUMN_IMAGE_PROCESSID = "prozesseID";
    private static final String COLUMN_IMAGE_FILENAME = "fileName";
    private static final String COLUMN_IMAGE_SEQUENCE = "sequence";
    private static final String COLUMN_IMAGE_STRUCTTYPE = "structType";
    private static final String COLUMN_IMAGE_DISPLAYIMAGE = "displayImage";
    private static final String COLUMN_IMAGE_LICENCE = "licence";
    private static final String COLUMN_IMAGE_REPRESNTATIVE = "representative";

    private static final String TABLE_DESCRIPTION = "plugin_gei_eurviews_description";
    private static final String COLUMN_DESCRIPTION_DESCRIPTIONID = "descriptionID";
    private static final String COLUMN_DESCRIPTION_PROCESSID = "prozesseID";
    private static final String COLUMN_DESCRIPTION_LANGUAGE = "language";
    private static final String COLUMN_DESCRIPTION_TITLE = "title";
    private static final String COLUMN_DESCRIPTION_SHORTDESCRIPTION = "shortDescription";
    private static final String COLUMN_DESCRIPTION_LONGDESCRIPTION = "longDescription";
    private static final String COLUMN_DESCRIPTION_ORIGINALLANGUAGE = "originalLanguage";

    private static final String TABLE_KEYWORD = "plugin_gei_eurviews_keyword";
    private static final String COLUMN_KEYWORD_PROCESSID = "prozesseID";
    private static final String COLUMN_KEYWORD_VALUE = "value";

    private static final String TABLE_CATEGORY = "plugin_gei_eurviews_category";
    private static final String COLUMN_CATEGORY_PROCESSID = "prozesseID";
    private static final String COLUMN_CATEGORY_VALUE = "value";

    private static final String TABLE_TRANSCRIPTION = "plugin_gei_eurviews_transcription";
    private static final String COLUMN_TRANSCRIPTION_TRANSCRIPTIONID = "transcriptionID";
    private static final String COLUMN_TRANSCRIPTION_PROCESSID = "prozesseID";
    private static final String COLUMN_TRANSCRIPTION_LANGUAGE = "language";
    private static final String COLUMN_TRANSCRIPTION_TRANSCRIPTION = "transcription";
    private static final String COLUMN_TRANSCRIPTION_AUTHOR = "author";
    private static final String COLUMN_TRANSCRIPTION_FILENAME = "fileName";

    private static final String TABLE_CATEGORIES = "plugin_gei_eurviews_categories";
    private static final String TABLE_KEYWORDS = "plugin_gei_eurviews_keywords";

    private static final String COLUMN_LANGUAGE_GERMAN = "german";
    private static final String COLUMN_LANGUAGE_ENGLISH = "english";
    private static final String COLUMN_LANGUAGE_FRENCH = "french";

    private static final String TABLE_ANNOTATION = "plugin_gei_eurviews_annotation";
    private static final String COLUMN_ANNOTATION_ID = "annotationID";
    private static final String COLUMN_ANNOTATION_PROCESSID = "prozesseID";
    private static final String COLUMN_ANNOTATION_TITLE = "title";
    private static final String COLUMN_ANNOTATION_LANGUAGE = "language";
    private static final String COLUMN_ANNOTATION_CONTENT = "content";
    private static final String COLUMN_ANNOTATION_TRANSLATOR = "translator";
    private static final String COLUMN_ANNOTATION_REFERENCE = "reference";
    private static final String COLUMN_ANNOTATION_CLASSIFICATION = "classification";
    private static final String COLUMN_ANNOTATION_FOOTNOTE = "footnote";

    private static final String TABLE_AUTHOR = "plugin_gei_eurviews_author";
    private static final String COLUMN_AUTHOR_ID = "authorID";
    private static final String COLUMN_AUTHOR_PROCESSID = "prozesseID";
    private static final String COLUMN_AUTHOR_NAME = "name";
    private static final String COLUMN_AUTHOR_ORGANIZATION = "organization";
    private static final String COLUMN_AUTHOR_MAIL = "mail";
    private static final String COLUMN_AUTHOR_MAIL2 = "mail2";
    private static final String COLUMN_AUTHOR_MAIL3 = "mail3";
    private static final String COLUMN_AUTHOR_URL = "url";

    private static final String TABLE_SOURCE = "plugin_gei_eurviews_source";
    private static final String COLUMN_SOURCE_ID = "resourceId";
    private static final String COLUMN_SOURCE_PROCESSID = "prozesseID";
    private static final String COLUMN_SOURCE_DATA = "data";
    private static final String COLUMN_SOURCE_MAINSOURCE = "mainsource";

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
                sql.append(COLUMN_RESOURCE_MAIN_TITLE);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_SUB_TITLE);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_AUTHOR_FIRSTNAME);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_AUTHOR_LASTNAME);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_LANGUAGE);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_PUBLISHER);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_PLACE_OF_PUBLICATION);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_PUBLICATION_YEAR);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_NUMBER_OF_PAGES);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_SHELFMARK);
                sql.append(", ");
                sql.append(COLUMN_RESOURCE_COPYRIGHT);
                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                Object[] parameter =
                        { data.getProzesseID(), StringUtils.isEmpty(data.getDocumentType()) ? null : data.getDocumentType(),
                                StringUtils.isEmpty(data.getMaintitle()) ? null : data.getMaintitle(),
                                StringUtils.isEmpty(data.getSubtitle()) ? null : data.getSubtitle(),
                                StringUtils.isEmpty(data.getAuthorFirstname()) ? null : data.getAuthorFirstname(),
                                StringUtils.isEmpty(data.getAuthorLastname()) ? null : data.getAuthorLastname(),
                                StringUtils.isEmpty(data.getLanguage()) ? null : data.getLanguage(),
                                data.getPublisher() == null ? null : data.getPublisher(),
                                StringUtils.isEmpty(data.getPlaceOfPublication()) ? null : data.getPlaceOfPublication(),
                                StringUtils.isEmpty(data.getPublicationYear()) ? null : data.getPublicationYear(),
                                StringUtils.isEmpty(data.getNumberOfPages()) ? null : data.getNumberOfPages(),
                                StringUtils.isEmpty(data.getShelfmark()) ? null : data.getShelfmark(),
                                data.getCopyright() == null ? null : data.getCopyright() };
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
                sql.append(COLUMN_RESOURCE_MAIN_TITLE);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_SUB_TITLE);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_AUTHOR_FIRSTNAME);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_AUTHOR_LASTNAME);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_LANGUAGE);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_PUBLISHER);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_PLACE_OF_PUBLICATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_PUBLICATION_YEAR);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_NUMBER_OF_PAGES);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_SHELFMARK);
                sql.append(" = ?, ");
                sql.append(COLUMN_RESOURCE_COPYRIGHT);
                sql.append(" = ? WHERE ");
                sql.append(COLUMN_RESOURCE_RESOURCEID);
                sql.append(" = ? ;");

                Object[] parameter =
                        { data.getProzesseID(), StringUtils.isEmpty(data.getDocumentType()) ? null : data.getDocumentType(),
                                StringUtils.isEmpty(data.getMaintitle()) ? null : data.getMaintitle(),
                                StringUtils.isEmpty(data.getSubtitle()) ? null : data.getSubtitle(),
                                StringUtils.isEmpty(data.getAuthorFirstname()) ? null : data.getAuthorFirstname(),
                                StringUtils.isEmpty(data.getAuthorLastname()) ? null : data.getAuthorLastname(),
                                StringUtils.isEmpty(data.getLanguage()) ? null : data.getLanguage(),
                                data.getPublisher() == null ? null : data.getPublisher(),
                                StringUtils.isEmpty(data.getPlaceOfPublication()) ? null : data.getPlaceOfPublication(),
                                StringUtils.isEmpty(data.getPublicationYear()) ? null : data.getPublicationYear(),
                                StringUtils.isEmpty(data.getNumberOfPages()) ? null : data.getNumberOfPages(),
                                StringUtils.isEmpty(data.getShelfmark()) ? null : data.getShelfmark(),
                                data.getCopyright() == null ? null : data.getCopyright(), data.getResourceID() };
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                }
                run.update(connection, sql.toString(), parameter);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
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
                    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { curr.getProcessId(), curr.getFileName(), curr.getOrder(),
                                    StringUtils.isEmpty(curr.getStructType()) ? null : curr.getStructType(), curr.isDisplayImage(),
                                    StringUtils.isEmpty(curr.getLicence()) ? null : curr.getLicence(), curr.isRepresentative() };
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
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_IMAGE_IMAGEID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { curr.getProcessId(), curr.getFileName(), curr.getOrder(),
                                    StringUtils.isEmpty(curr.getStructType()) ? null : curr.getStructType(), curr.isDisplayImage(),
                                    StringUtils.isEmpty(curr.getLicence()) ? null : curr.getLicence(), curr.isRepresentative(), curr.getImageId() };
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

    public static void saveDesciptionList(List<Description> descriptionList) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Description current : descriptionList) {
                StringBuilder sql = new StringBuilder();
                if (current.getDescriptionID() == null) {
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_DESCRIPTION);
                    sql.append(" (");
                    sql.append(COLUMN_DESCRIPTION_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_LANGUAGE);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_TITLE);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_SHORTDESCRIPTION);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_LONGDESCRIPTION);
                    sql.append(", ");
                    sql.append(COLUMN_DESCRIPTION_ORIGINALLANGUAGE);
                    sql.append(") VALUES (?, ?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { current.getProcessID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTitle()) ? null : current.getTitle(),
                                    StringUtils.isEmpty(current.getShortDescription()) ? null : current.getShortDescription(),
                                    StringUtils.isEmpty(current.getLongDescription()) ? null : current.getLongDescription(),
                                    current.isOriginalLanguage() };
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
                    sql.append(COLUMN_DESCRIPTION_TITLE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_SHORTDESCRIPTION);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_LONGDESCRIPTION);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_DESCRIPTION_ORIGINALLANGUAGE);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_DESCRIPTION_DESCRIPTIONID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProcessID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTitle()) ? null : current.getTitle(),
                                    StringUtils.isEmpty(current.getShortDescription()) ? null : current.getShortDescription(),
                                    StringUtils.isEmpty(current.getLongDescription()) ? null : current.getLongDescription(),
                                    current.isOriginalLanguage(), current.getDescriptionID() };
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

    public static List<Description> getDescriptionList(Integer processId) throws SQLException {
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

            List<Description> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToDescriptionListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveKeywordList(List<String> list, int processId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            // first delete old categories
            String delete = QUERY_DELETE_FROM + TABLE_KEYWORD + QUERY_WHERE + COLUMN_KEYWORD_PROCESSID + " = " + processId;
            run.update(connection, delete);
            if (list != null) {
                for (String current : list) {
                    StringBuilder sql = new StringBuilder();
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_KEYWORD);
                    sql.append(" (");
                    sql.append(COLUMN_KEYWORD_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_KEYWORD_VALUE);
                    sql.append(") VALUES (?, ?)");

                    Object[] parameter = { processId, current };

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

    public static List<String> getKeywordList(int processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_KEYWORD);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_KEYWORD_PROCESSID);
        sql.append(" = " + processId);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<String> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToKeywordListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveCategoryList(List<String> list, int processId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            // first delete old categories
            String delete = QUERY_DELETE_FROM + TABLE_CATEGORY + QUERY_WHERE + COLUMN_CATEGORY_PROCESSID + " = " + processId;
            run.update(connection, delete);
            if (list != null) {
                for (String current : list) {
                    StringBuilder sql = new StringBuilder();

                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_CATEGORY);
                    sql.append(" (");
                    sql.append(COLUMN_CATEGORY_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_CATEGORY_VALUE);
                    sql.append(") VALUES (?, ?)");

                    Object[] parameter = { processId, current };

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

    public static List<String> getCategoryList(int processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append(QUERY_SELECT_FROM);
        sql.append(TABLE_CATEGORY);
        sql.append(QUERY_WHERE);
        sql.append(COLUMN_CATEGORY_PROCESSID);
        sql.append(" = " + processId);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<String> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToCategoryListHandler);
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
        data.setAuthorFirstname(rs.getString(COLUMN_RESOURCE_AUTHOR_FIRSTNAME));
        data.setAuthorLastname(rs.getString(COLUMN_RESOURCE_AUTHOR_LASTNAME));
        data.setCopyright(rs.getString(COLUMN_RESOURCE_COPYRIGHT));
        data.setDocumentType(rs.getString(COLUMN_RESOURCE_DOCUMENT_TYPE));
        data.setLanguage(rs.getString(COLUMN_RESOURCE_LANGUAGE));
        data.setMaintitle(rs.getString(COLUMN_RESOURCE_MAIN_TITLE));
        data.setNumberOfPages(rs.getString(COLUMN_RESOURCE_NUMBER_OF_PAGES));
        data.setPlaceOfPublication(rs.getString(COLUMN_RESOURCE_PLACE_OF_PUBLICATION));
        data.setPublicationYear(rs.getString(COLUMN_RESOURCE_PUBLICATION_YEAR));
        data.setPublisher(rs.getString(COLUMN_RESOURCE_PUBLISHER));
        data.setShelfmark(rs.getString(COLUMN_RESOURCE_SHELFMARK));
        data.setSubtitle(rs.getString(COLUMN_RESOURCE_SUB_TITLE));
        return data;
    }

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

    private static ResultSetHandler<List<Description>> resultSetToDescriptionListHandler = new ResultSetHandler<List<Description>>() {

        public List<Description> handle(ResultSet rs) throws SQLException {
            List<Description> answer = new ArrayList<Description>();
            try {
                while (rs.next()) {
                    Description desc = new Description(rs.getInt(COLUMN_DESCRIPTION_PROCESSID));
                    desc.setDescriptionID(rs.getInt(COLUMN_DESCRIPTION_DESCRIPTIONID));
                    desc.setLanguage(rs.getString(COLUMN_DESCRIPTION_LANGUAGE));
                    desc.setTitle(rs.getString(COLUMN_DESCRIPTION_TITLE));
                    desc.setShortDescription(rs.getString(COLUMN_DESCRIPTION_SHORTDESCRIPTION));
                    desc.setLongDescription(rs.getString(COLUMN_DESCRIPTION_LONGDESCRIPTION));
                    desc.setOriginalLanguage(rs.getBoolean(COLUMN_DESCRIPTION_ORIGINALLANGUAGE));
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

    private static ResultSetHandler<List<String>> resultSetToKeywordListHandler = new ResultSetHandler<List<String>>() {
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<String>();
            try {
                while (rs.next()) {
                    String keyword = rs.getString(COLUMN_KEYWORD_VALUE);
                    answer.add(keyword);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;

        };
    };

    private static ResultSetHandler<List<String>> resultSetToCategoryListHandler = new ResultSetHandler<List<String>>() {
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<String>();
            try {
                while (rs.next()) {
                    String value = rs.getString(COLUMN_CATEGORY_VALUE);
                    answer.add(value);
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
                    trans.setAuthor(rs.getString(COLUMN_TRANSCRIPTION_AUTHOR));
                    trans.setImageName(rs.getString(COLUMN_TRANSCRIPTION_FILENAME));
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

    private static ResultSetHandler<List<String>> resultSetToStringListHandler = new ResultSetHandler<List<String>>() {
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<String>();
            try {
                while (rs.next()) {
                    String value = rs.getString(COLUMN_LANGUAGE_GERMAN);
                    String id = rs.getString(1);
                    answer.add(value + " (" + id + ")");
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
                    sql.append(COLUMN_TRANSCRIPTION_FILENAME);
                    sql.append(", ");
                    sql.append(COLUMN_TRANSCRIPTION_AUTHOR);
                    sql.append(") VALUES (?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { current.getProzesseID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTranscription()) ? null : current.getTranscription(),
                                    StringUtils.isEmpty(current.getImageName()) ? null : current.getImageName(),
                                    StringUtils.isEmpty(current.getAuthor()) ? null : current.getAuthor() };
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
                    sql.append(COLUMN_TRANSCRIPTION_FILENAME);
                    sql.append(" =?, ");
                    sql.append(COLUMN_TRANSCRIPTION_AUTHOR);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_TRANSCRIPTION_TRANSCRIPTIONID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProzesseID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTranscription()) ? null : current.getTranscription(),
                                    StringUtils.isEmpty(current.getImageName()) ? null : current.getImageName(),
                                    StringUtils.isEmpty(current.getAuthor()) ? null : current.getAuthor(), current.getTranscriptionID() };
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

    public static void deleteDescription(Description currentDescription) throws SQLException {
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

    public static List<String> getCategories(String query) throws SQLException {
        String sql = QUERY_SELECT_FROM + TABLE_CATEGORIES;
        if (!StringUtils.isEmpty(query)) {
            sql += QUERY_WHERE + COLUMN_LANGUAGE_GERMAN + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%';";
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<String> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void addCategory(String german, String english, String french) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            StringBuilder sql = new StringBuilder();

            sql.append(QUERY_INSERT_INTO);
            sql.append(TABLE_CATEGORIES);
            sql.append(" (");
            sql.append(COLUMN_LANGUAGE_GERMAN);
            sql.append(", ");
            sql.append(COLUMN_LANGUAGE_ENGLISH);
            sql.append(", ");
            sql.append(COLUMN_LANGUAGE_FRENCH);

            sql.append(") VALUES (?, ?, ?)");

            Object[] parameter = { german, english, french };
            run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void addKeyword(String german, String english, String french) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            StringBuilder sql = new StringBuilder();

            sql.append(QUERY_INSERT_INTO);
            sql.append(TABLE_KEYWORDS);
            sql.append(" (");
            sql.append(COLUMN_LANGUAGE_GERMAN);
            sql.append(", ");
            sql.append(COLUMN_LANGUAGE_ENGLISH);
            sql.append(", ");
            sql.append(COLUMN_LANGUAGE_FRENCH);

            sql.append(") VALUES (?, ?, ?)");

            Object[] parameter = { german, english, french };
            run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static List<String> getKeywords(String query) throws SQLException {
        String sql = QUERY_SELECT_FROM + TABLE_KEYWORDS;
        if (!StringUtils.isEmpty(query)) {
            sql += QUERY_WHERE + COLUMN_LANGUAGE_GERMAN + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%';";
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<String> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToStringListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static List<BibliographicData> getBibliographicData(String query) throws SQLException {
        String sql = QUERY_SELECT_FROM + TABLE_RESOURCE;
        if (!StringUtils.isEmpty(query)) {
            sql +=
                    QUERY_WHERE + COLUMN_RESOURCE_MAIN_TITLE + " LIKE '%" + StringEscapeUtils.escapeSql(query) + "%'" + " OR "
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

    public static void saveAnnotationList(List<Annotation> list, int processId) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            String delete = QUERY_DELETE_FROM + TABLE_ANNOTATION + QUERY_WHERE + COLUMN_ANNOTATION_PROCESSID + " = " + processId;
            // first delete old categories

            run.update(connection, delete);
            if (list != null) {
                for (Annotation current : list) {
                    StringBuilder sql = new StringBuilder();
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_ANNOTATION);
                    sql.append(" (");
                    sql.append(COLUMN_ANNOTATION_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_TITLE);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_LANGUAGE);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_CONTENT);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_TRANSLATOR);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_REFERENCE);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_CLASSIFICATION);
                    sql.append(", ");
                    sql.append(COLUMN_ANNOTATION_FOOTNOTE);
                    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { processId, current.getTitle(), current.getLanguage(), current.getContent(), current.getTranslator(),
                                    current.getReference(), current.getClassification(), current.getFootnote() };

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

    public static List<Annotation> getAnnotationList(int processId) throws SQLException {

        String sql = QUERY_SELECT_FROM + TABLE_ANNOTATION + QUERY_WHERE + COLUMN_ANNOTATION_PROCESSID + " = " + processId;

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }

            List<Annotation> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToAnnotationListHandler);

            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static ResultSetHandler<List<Annotation>> resultSetToAnnotationListHandler = new ResultSetHandler<List<Annotation>>() {
        @Override
        public List<Annotation> handle(ResultSet rs) throws SQLException {
            try {
                List<Annotation> answer = new ArrayList<Annotation>();

                while (rs.next()) {
                    Annotation annotation = new Annotation(rs.getInt(COLUMN_ANNOTATION_PROCESSID));
                    annotation.setAnnotationId(rs.getInt(COLUMN_ANNOTATION_ID));
                    annotation.setTitle(rs.getString(COLUMN_ANNOTATION_TITLE));
                    annotation.setLanguage(rs.getString(COLUMN_ANNOTATION_LANGUAGE));
                    annotation.setContent(rs.getString(COLUMN_ANNOTATION_CONTENT));
                    annotation.setTranslator(rs.getString(COLUMN_ANNOTATION_TRANSLATOR));
                    annotation.setReference(rs.getString(COLUMN_ANNOTATION_REFERENCE));
                    annotation.setClassification(rs.getString(COLUMN_ANNOTATION_CLASSIFICATION));
                    annotation.setFootnote(rs.getString(COLUMN_ANNOTATION_FOOTNOTE));
                    answer.add(annotation);
                }

                return answer;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    public static void saveAuthorList(List<Author> list, int processId) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            String delete = QUERY_DELETE_FROM + TABLE_AUTHOR + QUERY_WHERE + COLUMN_AUTHOR_PROCESSID + " = " + processId;
            // first delete old categories

            run.update(connection, delete);
            if (list != null) {
                for (Author current : list) {
                    StringBuilder sql = new StringBuilder();
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_AUTHOR);
                    sql.append(" (");
                    sql.append(COLUMN_AUTHOR_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_AUTHOR_NAME);
                    sql.append(", ");
                    sql.append(COLUMN_AUTHOR_ORGANIZATION);
                    sql.append(", ");
                    sql.append(COLUMN_AUTHOR_MAIL);
                    sql.append(", ");
                    sql.append(COLUMN_AUTHOR_MAIL2);
                    sql.append(", ");
                    sql.append(COLUMN_AUTHOR_MAIL3);
                    sql.append(", ");

                    sql.append(COLUMN_AUTHOR_URL);

                    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { processId, current.getName(), current.getOrganization(), current.getMail(), current.getMail2(), current.getMail3(),
                                    current.getUrl() };

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

    public static List<Author> getAuthorList(int processId) throws SQLException {

        String sql = QUERY_SELECT_FROM + TABLE_AUTHOR + QUERY_WHERE + COLUMN_AUTHOR_PROCESSID + " = " + processId;

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }

            List<Author> ret = new QueryRunner().query(connection, sql, DatabaseManager.resultSetToAuthorListHandler);

            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static ResultSetHandler<List<Author>> resultSetToAuthorListHandler = new ResultSetHandler<List<Author>>() {
        @Override
        public List<Author> handle(ResultSet rs) throws SQLException {
            try {
                List<Author> answer = new ArrayList<Author>();

                while (rs.next()) {
                    Author author = new Author(rs.getInt(COLUMN_AUTHOR_PROCESSID));
                    author.setAuthorId(rs.getInt(COLUMN_AUTHOR_ID));
                    author.setName(rs.getString(COLUMN_AUTHOR_NAME));
                    author.setOrganization(rs.getString(COLUMN_AUTHOR_ORGANIZATION));
                    author.setMail(rs.getString(COLUMN_AUTHOR_MAIL));
                    author.setMail2(rs.getString(COLUMN_AUTHOR_MAIL2));
                    author.setMail3(rs.getString(COLUMN_AUTHOR_MAIL3));
                    author.setUrl(rs.getString(COLUMN_AUTHOR_URL));
                    answer.add(author);
                }

                return answer;
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

            String delete = QUERY_DELETE_FROM + TABLE_SOURCE + QUERY_WHERE + COLUMN_SOURCE_PROCESSID + " = " + processId;
            // first delete old categories

            run.update(connection, delete);
            if (list != null) {
                for (Source current : list) {
                    StringBuilder sql = new StringBuilder();
                    sql.append(QUERY_INSERT_INTO);
                    sql.append(TABLE_SOURCE);
                    sql.append(" (");
                    sql.append(COLUMN_SOURCE_PROCESSID);
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

        String sql = QUERY_SELECT_FROM + TABLE_SOURCE + QUERY_WHERE + COLUMN_SOURCE_PROCESSID + " = " + processId;

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
                    Source source = new Source(rs.getInt(COLUMN_SOURCE_PROCESSID));
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

    // TODO UPDATE 5.6.2015
    /*
    alter table `goobi`.`plugin_gei_eurviews_author` add column mail2 varchar(255) default null;
    alter table `goobi`.`plugin_gei_eurviews_author` add column mail3 varchar(255) default null;
    
    alter table `goobi`.`plugin_gei_eurviews_source` MODIFY data INTEGER;
    
    alter table `goobi`.`plugin_gei_eurviews_annotation` add column classification varchar(255) default null;
    alter table `goobi`.`plugin_gei_eurviews_annotation` add column footnote text default null;
    
    */
}
