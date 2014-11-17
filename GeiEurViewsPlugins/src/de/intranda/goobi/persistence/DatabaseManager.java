package de.intranda.goobi.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.model.resource.Category;
import de.intranda.goobi.model.resource.Description;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class);

    private static final String TABLE_RESOURCE = "resource";
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

    private static final String TABLE_IMAGE = "image";
    private static final String COLUMN_IMAGE_IMAGEID = "imageID";
    private static final String COLUMN_IMAGE_PROCESSID = "prozesseID";
    private static final String COLUMN_IMAGE_FILENAME = "fileName";
    private static final String COLUMN_IMAGE_SEQUENCE = "sequence";
    private static final String COLUMN_IMAGE_STRUCTTYPE = "structType";
    private static final String COLUMN_IMAGE_DISPLAYIMAGE = "displayImage";
    private static final String COLUMN_IMAGE_LICENCE = "licence";
    private static final String COLUMN_IMAGE_REPRESNTATIVE = "representative";

    private static final String TABLE_DESCRIPTION = "description";
    private static final String COLUMN_DESCRIPTION_DESCRIPTIONID = "descriptionID";
    private static final String COLUMN_DESCRIPTION_PROCESSID = "prozesseID";
    private static final String COLUMN_DESCRIPTION_LANGUAGE = "language";
    private static final String COLUMN_DESCRIPTION_TITLE = "title";
    private static final String COLUMN_DESCRIPTION_SHORTDESCRIPTION = "shortDescription";
    private static final String COLUMN_DESCRIPTION_LONGDESCRIPTION = "longDescription";

    private static final String TABLE_KEYWORD = "keyword";
    private static final String COLUMN_KEYWORD_KEYWORDID = "keywordID";
    private static final String COLUMN_KEYWORD_PROCESSID = "prozesseID";
    private static final String COLUMN_KEYWORD_EXTERNALID = "externalID";
    private static final String COLUMN_KEYWORD_LABEL = "label";

    private static final String TABLE_CATEGORY = "category";
    private static final String COLUMN_CATEGORY_CATEGORYID = "categoryId";
    private static final String COLUMN_CATEGORY_PROCESSID = "prozesseID";
    private static final String COLUMN_CATEGORY_EXTERNALID = "externalID";
    private static final String COLUMN_CATEGORY_LABEL = "label";

    public static void saveBibliographicData(BibliographicData data) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (data.getResourceID() == null) {
                sql.append("INSERT INTO ");
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
                sql.append("UPDATE ");
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

    public static BibliographicData getBibliographicData(int processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(TABLE_RESOURCE);
        sql.append(" WHERE ");
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

    public static void saveImages(List<Image> currentImages) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Image curr : currentImages) {
                StringBuilder sql = new StringBuilder();
                if (curr.getImageId() == null) {
                    sql.append("INSERT INTO ");
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
                    sql.append("UPDATE ");
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
        sql.append("SELECT * FROM ");
        sql.append(TABLE_IMAGE);
        sql.append(" WHERE ");
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
                    sql.append("INSERT INTO ");
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
                    sql.append(") VALUES (?, ?, ?, ?, ?)");

                    Object[] parameter =
                            { current.getProcessID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTitle()) ? null : current.getTitle(),
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
                    sql.append("UPDATE ");
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
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_DESCRIPTION_DESCRIPTIONID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProcessID(), StringUtils.isEmpty(current.getLanguage()) ? null : current.getLanguage(),
                                    StringUtils.isEmpty(current.getTitle()) ? null : current.getTitle(),
                                    StringUtils.isEmpty(current.getShortDescription()) ? null : current.getShortDescription(),
                                    StringUtils.isEmpty(current.getLongDescription()) ? null : current.getLongDescription(),
                                    current.getDescriptionID() };
                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    run.update(connection, sql.toString(), parameter);
                }

                saveKeywordList(current.getKeywordList());
                saveCategoryList(current.getCategoryList());
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
        sql.append("SELECT * FROM ");
        sql.append(TABLE_DESCRIPTION);
        sql.append(" WHERE ");
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

    private static void saveKeywordList(List<Keyword> keywordList) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Keyword current : keywordList) {
                StringBuilder sql = new StringBuilder();
                if (current.getKeywordId() == null) {
                    sql.append("INSERT INTO ");
                    sql.append(TABLE_KEYWORD);
                    sql.append(" (");
                    sql.append(COLUMN_KEYWORD_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_KEYWORD_EXTERNALID);
                    sql.append(", ");
                    sql.append(COLUMN_KEYWORD_LABEL);

                    sql.append(") VALUES (?, ?, ?)");

                    Object[] parameter =
                            { current.getProcessId(), StringUtils.isEmpty(current.getExternalId()) ? null : current.getExternalId(),
                                    StringUtils.isEmpty(current.getLabel()) ? null : current.getLabel() };

                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                    if (id != null) {
                        current.setKeywordId(id);
                    }

                } else {
                    sql.append("UPDATE ");
                    sql.append(TABLE_KEYWORD);
                    sql.append(" SET ");
                    sql.append(COLUMN_KEYWORD_PROCESSID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_KEYWORD_EXTERNALID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_KEYWORD_LABEL);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_KEYWORD_KEYWORDID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProcessId(), StringUtils.isEmpty(current.getExternalId()) ? null : current.getExternalId(),
                                    StringUtils.isEmpty(current.getLabel()) ? null : current.getLabel(), current.getKeywordId() };
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

    private static List<Keyword> getKeywordList(int processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(TABLE_KEYWORD);
        sql.append(" WHERE ");
        sql.append(COLUMN_KEYWORD_PROCESSID);
        sql.append(" = " + processId + " ORDER BY " + COLUMN_KEYWORD_KEYWORDID);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<Keyword> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToKeywordListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static void saveCategoryList(List<Category> list) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (Category current : list) {
                StringBuilder sql = new StringBuilder();
                if (current.getCategoryId() == null) {
                    sql.append("INSERT INTO ");
                    sql.append(TABLE_CATEGORY);
                    sql.append(" (");
                    sql.append(COLUMN_CATEGORY_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_CATEGORY_EXTERNALID);
                    sql.append(", ");
                    sql.append(COLUMN_CATEGORY_LABEL);

                    sql.append(") VALUES (?, ?, ?)");

                    Object[] parameter =
                            { current.getProcessId(), StringUtils.isEmpty(current.getExternalId()) ? null : current.getExternalId(),
                                    StringUtils.isEmpty(current.getLabel()) ? null : current.getLabel() };

                    if (logger.isDebugEnabled()) {
                        logger.debug(sql.toString() + ", " + Arrays.toString(parameter));
                    }
                    Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, parameter);
                    if (id != null) {
                        current.setCategoryId(id);
                    }

                } else {
                    sql.append("UPDATE ");
                    sql.append(TABLE_CATEGORY);
                    sql.append(" SET ");
                    sql.append(COLUMN_CATEGORY_PROCESSID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_CATEGORY_EXTERNALID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_CATEGORY_LABEL);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_CATEGORY_CATEGORYID);
                    sql.append(" = ? ;");

                    Object[] parameter =
                            { current.getProcessId(), StringUtils.isEmpty(current.getExternalId()) ? null : current.getExternalId(),
                                    StringUtils.isEmpty(current.getLabel()) ? null : current.getLabel(), current.getCategoryId() };
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

    private static List<Category> getCategoryList(int processId) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(TABLE_CATEGORY);
        sql.append(" WHERE ");
        sql.append(COLUMN_CATEGORY_PROCESSID);
        sql.append(" = " + processId + " ORDER BY " + COLUMN_CATEGORY_CATEGORYID);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<Category> ret = new QueryRunner().query(connection, sql.toString(), DatabaseManager.resultSetToCategoryListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static ResultSetHandler<BibliographicData> resultSetToBibliographicDataHandler = new ResultSetHandler<BibliographicData>() {
        @Override
        public BibliographicData handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    Integer resourceId = rs.getInt(COLUMN_RESOURCE_RESOURCEID);
                    if (rs.wasNull()) {
                        resourceId = null;
                    }
                    Integer processId = rs.getInt(COLUMN_RESOURCE_PROCESSID);
                    if (rs.wasNull()) {
                        processId = null;
                    }
                    BibliographicData data = new BibliographicData(processId);
                    data.setResourceID(rs.getInt(resourceId));
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
                    List<Keyword> keys = getKeywordList(desc.getProcessID());
                    desc.setKeywordList(keys);
                    List<Category> cat = getCategoryList(desc.getProcessID());
                    desc.setCategoryList(cat);
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

    private static ResultSetHandler<List<Keyword>> resultSetToKeywordListHandler = new ResultSetHandler<List<Keyword>>() {
        public List<Keyword> handle(ResultSet rs) throws SQLException {
            List<Keyword> answer = new ArrayList<Keyword>();
            try {
                while (rs.next()) {
                    Keyword keyword = new Keyword(rs.getInt(COLUMN_KEYWORD_PROCESSID));
                    keyword.setExternalId(rs.getString(COLUMN_KEYWORD_EXTERNALID));
                    Integer keywordId = rs.getInt(COLUMN_KEYWORD_KEYWORDID);
                    if (rs.wasNull()) {
                        keywordId = null;
                    }
                    keyword.setKeywordId(keywordId);
                    keyword.setLabel(rs.getString(COLUMN_KEYWORD_LABEL));
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

    private static ResultSetHandler<List<Category>> resultSetToCategoryListHandler = new ResultSetHandler<List<Category>>() {
        public List<Category> handle(ResultSet rs) throws SQLException {
            List<Category> answer = new ArrayList<Category>();
            try {
                while (rs.next()) {
                    Category category = new Category(rs.getInt(COLUMN_CATEGORY_PROCESSID));
                    category.setExternalId(rs.getString(COLUMN_CATEGORY_EXTERNALID));
                    Integer keywordId = rs.getInt(COLUMN_CATEGORY_CATEGORYID);
                    if (rs.wasNull()) {
                        keywordId = null;
                    }
                    category.setCategoryId(keywordId);
                    category.setLabel(rs.getString(COLUMN_CATEGORY_LABEL));
                    answer.add(category);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;

        };
    };
}
