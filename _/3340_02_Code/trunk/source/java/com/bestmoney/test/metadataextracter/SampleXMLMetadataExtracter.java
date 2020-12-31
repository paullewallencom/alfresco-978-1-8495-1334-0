package com.bestmoney.test.metadataextracter;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class SampleXMLMetadataExtracter extends AbstractMappingMetadataExtracter {
    private static final String KEY_CREATION_DATE = "date";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "desc";

    public static String[] SUPPORTED_MIMETYPES = new String[]{MimetypeMap.MIMETYPE_XML};

    public SampleXMLMetadataExtracter() {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    @Override
    public Map<String, Serializable> extractRaw(
            ContentReader reader) throws Throwable {
        Map<String, Serializable> rawProperties = newRawMap();

        InputStream is = null;
        try {
            is = reader.getContentInputStream();

            // Use some magical tool to parse and extract
            // properties from XML file
            // For now, just hard-code the extracted values
            Calendar cal = Calendar.getInstance();
            cal.set(2000, 1, 1, 12, 0);
            Date dateValue = cal.getTime();
            String titleValue = "This is an extracted title";
            String descValue = "This is an extracted description";

            logger.info("Extracted date, title, and desc");

            boolean wasAdded = putRawValue(KEY_CREATION_DATE, dateValue, rawProperties);
            if (!wasAdded) {
                logger.error("Could not add date to raw properties");
            }
            wasAdded = putRawValue(KEY_TITLE, titleValue, rawProperties);
            if (!wasAdded) {
                logger.error("Could not add title to raw properties");
            }
            wasAdded = putRawValue(KEY_DESCRIPTION, descValue, rawProperties);
            if (!wasAdded) {
                logger.error("Could not add desc to raw properties");
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }

        return rawProperties;
    }
}
