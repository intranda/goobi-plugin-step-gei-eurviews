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
import de.intranda.goobi.model.resource.Image;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class ResourceBibliographicManager {
    private static final Logger logger = Logger.getLogger(ResourceBibliographicManager.class);

    private static final String TABLE_NAME_RESOURCE = "resource";
    private static final String COLUMN_NAME_RESOURCE_RESOURCEID = "resourceID";
    private static final String COLUMN_NAME_RESOURCE_PROCESSID = "prozesseID";
    private static final String COLUMN_NAME_RESOURCE_DOCUMENT_TYPE = "documentType";
    private static final String COLUMN_NAME_RESOURCE_MAIN_TITLE = "maintitle";
    private static final String COLUMN_NAME_RESOURCE_SUB_TITLE = "subtitle";
    private static final String COLUMN_NAME_RESOURCE_AUTHOR_FIRSTNAME = "authorFirstname";
    private static final String COLUMN_NAME_RESOURCE_AUTHOR_LASTNAME = "authorLastname";
    private static final String COLUMN_NAME_RESOURCE_LANGUAGE = "language";
    private static final String COLUMN_NAME_RESOURCE_PUBLISHER = "publisher";
    private static final String COLUMN_NAME_RESOURCE_PLACE_OF_PUBLICATION = "placeOfPublication";
    private static final String COLUMN_NAME_RESOURCE_PUBLICATION_YEAR = "publicationYear";
    private static final String COLUMN_NAME_RESOURCE_NUMBER_OF_PAGES = "numberOfPages";
    private static final String COLUMN_NAME_RESOURCE_SHELFMARK = "shelfmark";
    private static final String COLUMN_NAME_RESOURCE_COPYRIGHT = "copyright";

    private static final String TABLE_NAME_IMAGE = "image";
    private static final String COLUMN_NAME_IMAGE_IMAGEID = "imageID";
    private static final String COLUMN_NAME_IMAGE_PROCESSID = "prozesseID";
    private static final String COLUMN_NAME_IMAGE_FILENAME = "fileName";
    private static final String COLUMN_NAME_IMAGE_SEQUENCE = "sequence";
    private static final String COLUMN_NAME_IMAGE_STRUCTTYPE = "structType";
    private static final String COLUMN_NAME_IMAGE_DISPLAYIMAGE = "displayImage";
    private static final String COLUMN_NAME_IMAGE_LICENCE = "licence";
    private static final String COLUMN_NAME_IMAGE_REPRESNTATIVE = "representative";
    
    public static void saveBibliographicData(BibliographicData data) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (data.getResourceID() == null) {
                sql.append("INSERT INTO ");
                sql.append(TABLE_NAME_RESOURCE);
                sql.append("(");
                sql.append(COLUMN_NAME_RESOURCE_PROCESSID);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_DOCUMENT_TYPE);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_MAIN_TITLE);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_SUB_TITLE);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_AUTHOR_FIRSTNAME);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_AUTHOR_LASTNAME);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_LANGUAGE);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_PUBLISHER);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_PLACE_OF_PUBLICATION);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_PUBLICATION_YEAR);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_NUMBER_OF_PAGES);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_SHELFMARK);
                sql.append(", ");
                sql.append(COLUMN_NAME_RESOURCE_COPYRIGHT);
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
                sql.append(TABLE_NAME_RESOURCE);
                sql.append(" SET ");
                sql.append(COLUMN_NAME_RESOURCE_PROCESSID);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_DOCUMENT_TYPE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_MAIN_TITLE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_SUB_TITLE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_AUTHOR_FIRSTNAME);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_AUTHOR_LASTNAME);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_LANGUAGE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_PUBLISHER);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_PLACE_OF_PUBLICATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_PUBLICATION_YEAR);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_NUMBER_OF_PAGES);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_SHELFMARK);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_RESOURCE_COPYRIGHT);
                sql.append(" = ? WHERE ");
                sql.append(COLUMN_NAME_RESOURCE_RESOURCEID);
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
        sql.append(TABLE_NAME_RESOURCE);
        sql.append(" WHERE ");
        sql.append(COLUMN_NAME_RESOURCE_PROCESSID);
        sql.append(" = " + processId);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            BibliographicData ret =
                    new QueryRunner().query(connection, sql.toString(), ResourceBibliographicManager.resultSetToBibliographicDataHandler);
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
                    sql.append(TABLE_NAME_IMAGE);
                    sql.append(" (");
                    sql.append(COLUMN_NAME_IMAGE_PROCESSID);
                    sql.append(", ");
                    sql.append(COLUMN_NAME_IMAGE_FILENAME);
                    sql.append(", ");
                    sql.append(COLUMN_NAME_IMAGE_SEQUENCE);
                    sql.append(", ");
                    sql.append(COLUMN_NAME_IMAGE_STRUCTTYPE);
                    sql.append(", ");
                    sql.append(COLUMN_NAME_IMAGE_DISPLAYIMAGE);
                    sql.append(", ");
                    sql.append(COLUMN_NAME_IMAGE_LICENCE);
                    sql.append(", ");
                    sql.append(COLUMN_NAME_IMAGE_REPRESNTATIVE);
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
                    sql.append(TABLE_NAME_IMAGE);
                    sql.append(" SET ");
                    sql.append(COLUMN_NAME_IMAGE_PROCESSID);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_NAME_IMAGE_FILENAME);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_NAME_IMAGE_SEQUENCE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_NAME_IMAGE_STRUCTTYPE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_NAME_IMAGE_DISPLAYIMAGE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_NAME_IMAGE_LICENCE);
                    sql.append(" = ?, ");
                    sql.append(COLUMN_NAME_IMAGE_REPRESNTATIVE);
                    sql.append(" = ? WHERE ");
                    sql.append(COLUMN_NAME_IMAGE_IMAGEID);
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
        sql.append(TABLE_NAME_IMAGE);
        sql.append(" WHERE ");
        sql.append(COLUMN_NAME_IMAGE_PROCESSID);
        sql.append(" = " + processId);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }

            List<Image> ret = new QueryRunner().query(connection, sql.toString(), ResourceBibliographicManager.resultSetToImageListHandler);
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
                    Integer resourceId = rs.getInt(COLUMN_NAME_RESOURCE_RESOURCEID);
                    if (rs.wasNull()) {
                        resourceId = null;
                    }
                    Integer processId = rs.getInt(COLUMN_NAME_RESOURCE_PROCESSID);
                    if (rs.wasNull()) {
                        processId = null;
                    }
                    BibliographicData data = new BibliographicData(processId);
                    data.setResourceID(rs.getInt(resourceId));
                    data.setAuthorFirstname(rs.getString(COLUMN_NAME_RESOURCE_AUTHOR_FIRSTNAME));
                    data.setAuthorLastname(rs.getString(COLUMN_NAME_RESOURCE_AUTHOR_LASTNAME));
                    data.setCopyright(rs.getString(COLUMN_NAME_RESOURCE_COPYRIGHT));
                    data.setDocumentType(rs.getString(COLUMN_NAME_RESOURCE_DOCUMENT_TYPE));
                    data.setLanguage(rs.getString(COLUMN_NAME_RESOURCE_LANGUAGE));
                    data.setMaintitle(rs.getString(COLUMN_NAME_RESOURCE_MAIN_TITLE));
                    data.setNumberOfPages(rs.getString(COLUMN_NAME_RESOURCE_NUMBER_OF_PAGES));
                    data.setPlaceOfPublication(rs.getString(COLUMN_NAME_RESOURCE_PLACE_OF_PUBLICATION));
                    data.setPublicationYear(rs.getString(COLUMN_NAME_RESOURCE_PUBLICATION_YEAR));
                    data.setPublisher(rs.getString(COLUMN_NAME_RESOURCE_PUBLISHER));
                    data.setShelfmark(rs.getString(COLUMN_NAME_RESOURCE_SHELFMARK));
                    data.setSubtitle(rs.getString(COLUMN_NAME_RESOURCE_SUB_TITLE));
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
                    Image image = new Image(rs.getInt(COLUMN_NAME_IMAGE_PROCESSID));
                    image.setImageId(rs.getInt(COLUMN_NAME_IMAGE_IMAGEID));
                    image.setFileName(rs.getString(COLUMN_NAME_IMAGE_FILENAME));
                    image.setOrder(rs.getInt(COLUMN_NAME_IMAGE_SEQUENCE));
                    ;
                    image.setStructType(rs.getString(COLUMN_NAME_IMAGE_STRUCTTYPE));
                    image.setDisplayImage(rs.getBoolean(COLUMN_NAME_IMAGE_DISPLAYIMAGE));
                    image.setLicence(rs.getString(COLUMN_NAME_IMAGE_LICENCE));
                    image.setRepresentative(rs.getBoolean(COLUMN_NAME_IMAGE_REPRESNTATIVE));
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
}
