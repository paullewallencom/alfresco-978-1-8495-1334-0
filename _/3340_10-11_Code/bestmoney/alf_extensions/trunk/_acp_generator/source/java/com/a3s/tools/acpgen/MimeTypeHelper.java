package com.a3s.tools.acpgen;

import java.io.InputStream;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.util.List;
import java.util.Iterator;

/**
 * Helper class to get the MIME Type for a filename extension.
 * The <code>mimetype-map.xml</code> has been copied from Alfresco source code.
 *
 * @author martin.bergljung@opsera.com
 */
public class MimeTypeHelper {
    public static final String MIMETYPE_MAP_FILE = "java/com/a3s/tools/acpgen/mimetype-map.xml";
    static Document m_document;
    static Element m_root;

    public MimeTypeHelper() {
        SAXBuilder sxb = new SAXBuilder();

        try {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(MIMETYPE_MAP_FILE);
            m_document = sxb.build(stream);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        m_root = m_document.getRootElement();
    }

    /**
     * Gets the Mimetype for passed in file extension
     *
     * @param ext
     * @return
     */
    public String GetMimetype(String ext) {
        String mimetype = "content/unknown";
        List listTypeMime = m_root.getChildren("mimetype");

        Iterator i = listTypeMime.iterator();
        while (i.hasNext()) {
            Element currentElement = (Element) i.next();
            List listExt = currentElement.getChildren("extension");
            Iterator idext = listExt.iterator();
            while (idext.hasNext()) {
                Element curext = (Element) idext.next();
                if (curext.getText().equals(ext)) {
                    mimetype = currentElement.getAttributeValue("mimetype");
                }
            }
        }

        return mimetype;
    }
}