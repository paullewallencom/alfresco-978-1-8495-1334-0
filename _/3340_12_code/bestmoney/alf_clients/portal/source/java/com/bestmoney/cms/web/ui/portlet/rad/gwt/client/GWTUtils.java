package com.bestmoney.cms.web.ui.portlet.rad.gwt.client;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import java.util.HashMap;
import java.util.Map;

/**
 * GWT/GXT Utilities
 *
 * @author martin.bergljung@opsera.com
 */
public class GWTUtils {
    public static final Map<String, AbstractImagePrototype> FILE_EXTENSION_2_IMAGE_MAP;

    static {
        FILE_EXTENSION_2_IMAGE_MAP = new HashMap<String, AbstractImagePrototype>();
        FILE_EXTENSION_2_IMAGE_MAP.put(".acp", RecentlyAddedDocumentsApp.ICONS.fileTypeAcp());
        FILE_EXTENSION_2_IMAGE_MAP.put(".asf", RecentlyAddedDocumentsApp.ICONS.fileTypeAsf());
        FILE_EXTENSION_2_IMAGE_MAP.put(".avi", RecentlyAddedDocumentsApp.ICONS.fileTypeAvi());
        FILE_EXTENSION_2_IMAGE_MAP.put(".bmp", RecentlyAddedDocumentsApp.ICONS.fileTypeBmp());
        FILE_EXTENSION_2_IMAGE_MAP.put(".csv", RecentlyAddedDocumentsApp.ICONS.fileTypeCsv());
        FILE_EXTENSION_2_IMAGE_MAP.put(".doc", RecentlyAddedDocumentsApp.ICONS.fileTypeDoc());
        FILE_EXTENSION_2_IMAGE_MAP.put(".docx", RecentlyAddedDocumentsApp.ICONS.fileTypeDocx());
        FILE_EXTENSION_2_IMAGE_MAP.put(".eml", RecentlyAddedDocumentsApp.ICONS.fileTypeEml());
        FILE_EXTENSION_2_IMAGE_MAP.put(".exe", RecentlyAddedDocumentsApp.ICONS.fileTypeExe());
        FILE_EXTENSION_2_IMAGE_MAP.put(".ftl", RecentlyAddedDocumentsApp.ICONS.fileTypeFtl());
        FILE_EXTENSION_2_IMAGE_MAP.put(".gif", RecentlyAddedDocumentsApp.ICONS.fileTypeGif());
        FILE_EXTENSION_2_IMAGE_MAP.put(".htm", RecentlyAddedDocumentsApp.ICONS.fileTypeHtm());
        FILE_EXTENSION_2_IMAGE_MAP.put(".html", RecentlyAddedDocumentsApp.ICONS.fileTypeHtml());
        FILE_EXTENSION_2_IMAGE_MAP.put(".jpg", RecentlyAddedDocumentsApp.ICONS.fileTypeJpg());
        FILE_EXTENSION_2_IMAGE_MAP.put(".js", RecentlyAddedDocumentsApp.ICONS.fileTypeJs());
        FILE_EXTENSION_2_IMAGE_MAP.put(".lnk", RecentlyAddedDocumentsApp.ICONS.fileTypeLnk());
        FILE_EXTENSION_2_IMAGE_MAP.put(".mp2", RecentlyAddedDocumentsApp.ICONS.fileTypeMp2());
        FILE_EXTENSION_2_IMAGE_MAP.put(".mp3", RecentlyAddedDocumentsApp.ICONS.fileTypeMp3());
        FILE_EXTENSION_2_IMAGE_MAP.put(".mp4", RecentlyAddedDocumentsApp.ICONS.fileTypeMp4());
        FILE_EXTENSION_2_IMAGE_MAP.put(".mpeg", RecentlyAddedDocumentsApp.ICONS.fileTypeMpeg());
        FILE_EXTENSION_2_IMAGE_MAP.put(".mpg", RecentlyAddedDocumentsApp.ICONS.fileTypeMpg());
        FILE_EXTENSION_2_IMAGE_MAP.put(".msg", RecentlyAddedDocumentsApp.ICONS.fileTypeMsg());
        FILE_EXTENSION_2_IMAGE_MAP.put(".odf", RecentlyAddedDocumentsApp.ICONS.fileTypeOdf());
        FILE_EXTENSION_2_IMAGE_MAP.put(".odg", RecentlyAddedDocumentsApp.ICONS.fileTypeOdg());
        FILE_EXTENSION_2_IMAGE_MAP.put(".odp", RecentlyAddedDocumentsApp.ICONS.fileTypeOdp());
        FILE_EXTENSION_2_IMAGE_MAP.put(".ods", RecentlyAddedDocumentsApp.ICONS.fileTypeOds());
        FILE_EXTENSION_2_IMAGE_MAP.put(".odt", RecentlyAddedDocumentsApp.ICONS.fileTypeOdt());
        FILE_EXTENSION_2_IMAGE_MAP.put(".pdf", RecentlyAddedDocumentsApp.ICONS.fileTypePdf());
        FILE_EXTENSION_2_IMAGE_MAP.put(".png", RecentlyAddedDocumentsApp.ICONS.fileTypePng());
        FILE_EXTENSION_2_IMAGE_MAP.put(".ppt", RecentlyAddedDocumentsApp.ICONS.fileTypePpt());
        FILE_EXTENSION_2_IMAGE_MAP.put(".pptx", RecentlyAddedDocumentsApp.ICONS.fileTypePptx());
        FILE_EXTENSION_2_IMAGE_MAP.put(".psd", RecentlyAddedDocumentsApp.ICONS.fileTypePsd());
        FILE_EXTENSION_2_IMAGE_MAP.put(".rtf", RecentlyAddedDocumentsApp.ICONS.fileTypeRtf());
        FILE_EXTENSION_2_IMAGE_MAP.put(".shtml", RecentlyAddedDocumentsApp.ICONS.fileTypeShtml());
        FILE_EXTENSION_2_IMAGE_MAP.put(".swf", RecentlyAddedDocumentsApp.ICONS.fileTypeSwf());
        FILE_EXTENSION_2_IMAGE_MAP.put(".tif", RecentlyAddedDocumentsApp.ICONS.fileTypeTif());
        FILE_EXTENSION_2_IMAGE_MAP.put(".tiff", RecentlyAddedDocumentsApp.ICONS.fileTypeTiff());
        FILE_EXTENSION_2_IMAGE_MAP.put(".txt", RecentlyAddedDocumentsApp.ICONS.fileTypeTxt());
        FILE_EXTENSION_2_IMAGE_MAP.put(".url", RecentlyAddedDocumentsApp.ICONS.fileTypeUrl());
        FILE_EXTENSION_2_IMAGE_MAP.put(".wmv", RecentlyAddedDocumentsApp.ICONS.fileTypeWmv());
        FILE_EXTENSION_2_IMAGE_MAP.put(".wpd", RecentlyAddedDocumentsApp.ICONS.fileTypeWpd());
        FILE_EXTENSION_2_IMAGE_MAP.put(".xls", RecentlyAddedDocumentsApp.ICONS.fileTypeXls());
        FILE_EXTENSION_2_IMAGE_MAP.put(".xlsx", RecentlyAddedDocumentsApp.ICONS.fileTypeXlsx());
        FILE_EXTENSION_2_IMAGE_MAP.put(".xml", RecentlyAddedDocumentsApp.ICONS.fileTypeXml());
        FILE_EXTENSION_2_IMAGE_MAP.put(".xsd", RecentlyAddedDocumentsApp.ICONS.fileTypeXsd());
        FILE_EXTENSION_2_IMAGE_MAP.put(".xsl", RecentlyAddedDocumentsApp.ICONS.fileTypeXsl());
        FILE_EXTENSION_2_IMAGE_MAP.put(".xslx", RecentlyAddedDocumentsApp.ICONS.fileTypeXslx());
        FILE_EXTENSION_2_IMAGE_MAP.put(".zip", RecentlyAddedDocumentsApp.ICONS.fileTypeZip());
    }

    public static AbstractImagePrototype getImageForFileExtension(String fileName) {
        String extension = null;

        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            extension = fileName.substring(lastIndexOfDot, fileName.length());
        } else {
            // Probably a folder
            return RecentlyAddedDocumentsApp.ICONS.folder();
        }

        if (extension == null) {
            return RecentlyAddedDocumentsApp.ICONS.fileTypeDefault();
        } else {
            return FILE_EXTENSION_2_IMAGE_MAP.get(extension);
        }
    }

    public static String getHostPageBaseURL() {
        if (GWT.isScript()) {
            return GWT.getHostPageBaseURL();
        } else {
            // Running in Hosted Mode
            return "http://localhost:9080/";
        }
    }

    public static void showInfoMsg(String msg) {
        MessageBox box = new MessageBox();
        box.setButtons(MessageBox.OK);
        box.setIcon(MessageBox.INFO);
        box.setTitle("Information");
        box.setMessage(msg);
        box.show();
    }
}
