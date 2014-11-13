package de.intranda.goobi.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.intranda.goobi.model.resource.BibliographicData;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class ResourceBibliographicManager {
    private static final Logger logger = Logger.getLogger(ResourceBibliographicManager.class);

    private static final String TABLE_NAME_RESOURCE = "resource";
    private static final String COLUMN_NAME_RESOURCEID = "resourceID";
    private static final String COLUMN_NAME_PROCESSID = "prozesseID";
    private static final String COLUMN_NAME_DOCUMENT_TYPE = "documentType";
    private static final String COLUMN_NAME_MAIN_TITLE = "maintitle";
    private static final String COLUMN_NAME_SUB_TITLE = "subtitle";
    private static final String COLUMN_NAME_AUTHOR_FIRSTNAME = "authorFirstname";
    private static final String COLUMN_NAME_AUTHOR_LASTNAME = "authorLastname";
    private static final String COLUMN_NAME_LANGUAGE = "language";
    private static final String COLUMN_NAME_PUBLISHER = "publisher";
    private static final String COLUMN_NAME_PLACE_OF_PUBLICATION = "placeOfPublication";
    private static final String COLUMN_NAME_PUBLICATION_YEAR = "publicationYear";
    private static final String COLUMN_NAME_NUMBER_OF_PAGES = "numberOfPages";
    private static final String COLUMN_NAME_SHELFMARK = "shelfmark";
    private static final String COLUMN_NAME_COPYRIGHT = "copyright";

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
                sql.append(COLUMN_NAME_PROCESSID);
                sql.append(", ");
                sql.append(COLUMN_NAME_DOCUMENT_TYPE);
                sql.append(", ");
                sql.append(COLUMN_NAME_MAIN_TITLE);
                sql.append(", ");
                sql.append(COLUMN_NAME_SUB_TITLE);
                sql.append(", ");
                sql.append(COLUMN_NAME_AUTHOR_FIRSTNAME);
                sql.append(", ");
                sql.append(COLUMN_NAME_AUTHOR_LASTNAME);
                sql.append(", ");
                sql.append(COLUMN_NAME_LANGUAGE);
                sql.append(", ");
                sql.append(COLUMN_NAME_PUBLISHER);
                sql.append(", ");
                sql.append(COLUMN_NAME_PLACE_OF_PUBLICATION);
                sql.append(", ");
                sql.append(COLUMN_NAME_PUBLICATION_YEAR);
                sql.append(", ");
                sql.append(COLUMN_NAME_NUMBER_OF_PAGES);
                sql.append(", ");
                sql.append(COLUMN_NAME_SHELFMARK);
                sql.append(", ");
                sql.append(COLUMN_NAME_COPYRIGHT);
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
                sql.append(COLUMN_NAME_PROCESSID);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_DOCUMENT_TYPE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_MAIN_TITLE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_SUB_TITLE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_AUTHOR_FIRSTNAME);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_AUTHOR_LASTNAME);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_LANGUAGE);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_PUBLISHER);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_PLACE_OF_PUBLICATION);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_PUBLICATION_YEAR);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_NUMBER_OF_PAGES);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_SHELFMARK);
                sql.append(" = ?, ");
                sql.append(COLUMN_NAME_COPYRIGHT);
                sql.append(" = ? WHERE ");
                sql.append(COLUMN_NAME_RESOURCEID);
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
        sql.append(COLUMN_NAME_PROCESSID);
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

    private static ResultSetHandler<BibliographicData> resultSetToBibliographicDataHandler = new ResultSetHandler<BibliographicData>() {
        @Override
        public BibliographicData handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    Integer resourceId = rs.getInt(COLUMN_NAME_RESOURCEID);
                    if (rs.wasNull()) {
                        resourceId = null;
                    }
                    Integer processId = rs.getInt(COLUMN_NAME_PROCESSID);
                    if (rs.wasNull()) {
                        processId = null;
                    }
                    BibliographicData data = new BibliographicData(processId);
                    data.setResourceID(rs.getInt(resourceId));
                    data.setAuthorFirstname(rs.getString(COLUMN_NAME_AUTHOR_FIRSTNAME));
                    data.setAuthorLastname(rs.getString(COLUMN_NAME_AUTHOR_LASTNAME));
                    data.setCopyright(rs.getString(COLUMN_NAME_COPYRIGHT));
                    data.setDocumentType(rs.getString(COLUMN_NAME_DOCUMENT_TYPE));
                    data.setLanguage(rs.getString(COLUMN_NAME_LANGUAGE));
                    data.setMaintitle(rs.getString(COLUMN_NAME_MAIN_TITLE));
                    data.setNumberOfPages(rs.getString(COLUMN_NAME_NUMBER_OF_PAGES));
                    data.setPlaceOfPublication(rs.getString(COLUMN_NAME_PLACE_OF_PUBLICATION));
                    data.setPublicationYear(rs.getString(COLUMN_NAME_PUBLICATION_YEAR));
                    data.setPublisher(rs.getString(COLUMN_NAME_PUBLISHER));
                    data.setShelfmark(rs.getString(COLUMN_NAME_SHELFMARK));
                    data.setSubtitle(rs.getString(COLUMN_NAME_SUB_TITLE));
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

}
