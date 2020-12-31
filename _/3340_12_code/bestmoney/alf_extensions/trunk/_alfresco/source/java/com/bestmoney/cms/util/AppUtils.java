package com.bestmoney.cms.util;

import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.component.UIActionLink;

import javax.faces.event.ActionEvent;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Useful utility methods
 *
 * @author martin.bergljung@opsera.com
 */
public class AppUtils {
    /**
     * Execute a LUCENE search
     *
     * @param searchService the Search Service to use
     * @param storeRef      the store to search in
     * @param query         the LUCENE query to use
     * @return a list of node references matching search query
     */
    public static List<NodeRef> search(SearchService searchService, StoreRef storeRef, StringBuilder query) {
        ResultSet results = null;
        List<NodeRef> matchingNodes = null;

        try {
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query.toString());
        } finally {
            if (results != null) {
                matchingNodes = results.getNodeRefs();

                // Underlying search engine, such as Lucene, might have IO resources open so close them
                results.close();
            } else {
                matchingNodes = new ArrayList<NodeRef>();
            }
        }

        return matchingNodes;
    }

    /**
     * Returns a LUCENE query string that matches passed in property.
     * Will also escape query to handle special characters (+ - ! ( ) { } [ ] ^ " ~ * ? : \)
     * in property name and value.
     *
     * @param property      the QName for the property (e.g. ImapEmailModel.PROP_MESSAGE_ID)
     * @param propertyValue the value for the property
     *                      (e.g. <f470f68e0804281247x2fd2b526mec0d56b58a16938d@mail.gmail.com>)
     * @return a query like
     *         '+@{http://www.opsera.com/opsera/model/0.1}messageid:"<f470f2fd2b526mec0d56b58a16938d@mail.gmail.com>"'
     */
    public static String getPropertyQuery(QName property, String propertyValue) {
        StringBuilder query = new StringBuilder(100);
        query.append("+");
        query.append("@");
        query.append(LuceneQueryParser.escape(property.toString()));
        query.append(":\"");
        query.append(propertyValue);
        // Cannot escape value as then a value like this-is-a-string would be escaped this\-is\-a\-string and not found query.append(LuceneQueryParser.escape(propertyValue));
        query.append("\"");

        return query.toString();
    }

    /**
     * Convert Java SQL Date to Java Util Date
     *
     * @param date a Java SQL date
     * @return a Java Util date
     */
    public static Date getJavaUtilDate(java.sql.Date date) {
        java.util.Date javaUtilDate = null;
        if (date != null) {
            javaUtilDate = new Date(date.getTime());
        }

        return javaUtilDate;
    }

    /**
     * Convert Java SQL Timestamp to Java Util Date
     *
     * @param timestamp the Java SQL Timestamp
     * @return a Java Util date
     */
    public static Date getJavaUtilDate(Timestamp timestamp) {
        java.util.Date javaUtilDate = null;
        if (timestamp != null) {
            javaUtilDate = new Date(timestamp.getTime());
        }

        return javaUtilDate;
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (Exception e) {
                System.out.println("Error closing database connection: "+ e.getMessage());
            }
        }
    }

    public static void close(java.sql.ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                rs = null;
            } catch (Exception e) {
                System.out.println("Error closing result set: "+ e.getMessage());
            }
        }
    }

    public static void close(Statement s) {
        if (s != null) {
            try {
                s.close();
                s = null;
            } catch (Exception e) {
                System.out.println("Error closing statement: "+ e.getMessage());
            }
        }
    }

    public static String getParamValue(ActionEvent event, String paramName) {
        UIActionLink link = (UIActionLink) event.getComponent();
        Map<String, String> params = link.getParameterMap();
        return params.get(paramName);
    }

}
