package com.a3s.tools.acpgen;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ACP File Generator
 *
 * @author martin.bergljung@opsera.com
 */
public class ACPGenerator {
    public static final String IMPORT_USER = "admin";
    public static final String IMPORT_LOCALE = "en_GB_";
    public static final String COMPANY_HOME_PATH = "/Company Home";

    private static MimeTypeHelper m_mimeTypeHelper = new MimeTypeHelper();
    private static BufferedWriter m_metadataFileWriter;
    private static Map<String, String> m_uuidMapping = new HashMap<String, String>();
    private static File m_importDir;
    private static int m_fileId;
    private static File m_tempAcpDir;
    private static File m_tempAcpContentDir;

    /**
     * Main function for the generator
     *
     * @param args args[0] the path to the directory from where to start import
     *             args[1] name of the ACP file, will be created in current directory
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String acpDirPath = null;
        String acpFileName = null;

        if (args.length > 1) {
            // Set source directory where we should start the folder and file import to ACP
            m_importDir = new File(args[0]);
            if (!m_importDir.isDirectory()) {
                System.out.println("Import directory is not a directory: " + m_importDir);
                usage();
            }
            System.out.println("ACP File will be created from content in: " + m_importDir);

            // Set the ACP File name
            acpFileName = args[1];

            // Check if we got a UUID Mapping file to use 
            if (args[2] != null) {
                String uuidMappingFileName = args[2];
                FileInputStream fs = new FileInputStream(uuidMappingFileName);
                DataInputStream in = new DataInputStream(fs);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String strLine = null;
                while ((strLine = br.readLine()) != null) {
                    System.out.println(strLine);
                    m_uuidMapping.put(strLine.substring(0, strLine.indexOf("=")).trim(),
                            strLine.substring(strLine.indexOf("=") + 1).trim());
                }

                System.out.println("Loaded " + m_uuidMapping.size() +
                        " Folder Path -> UUID mapping properties from " + uuidMappingFileName);
                fs.close();
            }

            // Create a temporary output directory for ACP file content
            File currentDir = new File(".");
            acpDirPath = currentDir.getCanonicalPath();
            String tempAcpDirPath = acpDirPath + "/ACPtmp";
            m_tempAcpDir = new File(tempAcpDirPath);
            if (!m_tempAcpDir.exists()) {
                m_tempAcpDir.mkdir();
            }
            m_tempAcpContentDir = new File(m_tempAcpDir + "/import");
            if (!m_tempAcpContentDir.exists()) {
                m_tempAcpContentDir.mkdir();
            }

            System.out.println("Temp directory for ACP: " + m_tempAcpDir);
            System.out.println("Temp directory for ACP content: " + m_tempAcpContentDir);
        } else {
            usage();
        }

        // Create the metadata XML file contained in the ACP
        createMetadataFile();

        // Generate ACP file
        createACPFile(acpDirPath + "/" + acpFileName, m_tempAcpDir.getPath());

        // Clean up temp files
        cleanTempACPDir(m_tempAcpDir.getPath());
    }

    /**
     * Prints how to use this tool
     */
    private static void usage() {
        System.out.println("Usage: java -jar acp_gen.jar importDirPath acpFileName uuidMapping.properties");
        System.out.println("uuidMapping.properties is optional");
        System.out.println("Example: java -jar acp_gen.jar c:\\docs mydocs.acp");
        System.exit(0);
    }

    /**
     * Creates the metadata file with folder definitions and content metadata
     *
     * @throws IOException
     */
    private static void createMetadataFile() throws IOException {
        String metadataFilePath = m_tempAcpDir.getPath() + "/import.xml";
        OutputStream fos = new FileOutputStream(metadataFilePath);
        Writer osw = new OutputStreamWriter(fos, "UTF-8");
        m_metadataFileWriter = new BufferedWriter(osw);
        System.out.println("ACP Metadata file: " + metadataFilePath);

        // Add the header for the metadata file
        m_metadataFileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        m_metadataFileWriter.write("<view:view xmlns:view=\"http://www.alfresco.org/view/repository/1.0\">\r\n");

        // Generate all XML for all files and directories to be imported
        generateMetadataForDir(m_importDir, COMPANY_HOME_PATH + "/" + m_importDir.getName());

        // End the XML file
        m_metadataFileWriter.write("</view:view>");
        m_metadataFileWriter.flush();
        m_metadataFileWriter.close();
    }

    /**
     * Clean up temp directory with ACP content, need to recursively do
     * this as you cannot delete a directory that is not empty.
     *
     * @param path ACP temp directory path
     * @throws IOException
     */
    private static void cleanTempACPDir(String path) throws IOException {
        File directory = new File(path);
        if (directory.isDirectory()) {
            String[] Filelist = directory.list();
            for (int i = 0; i < Filelist.length; i++) {
                cleanTempACPDir(new File(path + "/" + Filelist[i]).getPath());
            }
        }
        directory.delete();
    }

    /**
     * Generate all metadata XML for all directories and files contained in passed in directory
     *
     * @param dir
     * @throws IOException
     */
    private static void generateMetadataForDir(File dir, String currentAlfrescoFolderPath) throws IOException {
        String dirName = dir.getName();
        System.out.println("---- Generating Metadata XML for directory: " + dir.getPath());

        // Setup UUID if folder exist in Repository
        String nodeUUID = "";
        if (m_uuidMapping != null) {
            nodeUUID = m_uuidMapping.get(currentAlfrescoFolderPath);
            System.out.println("---- UUID for " + currentAlfrescoFolderPath + " is " + nodeUUID);
            if (nodeUUID != null && nodeUUID.length() > 0) {
                nodeUUID = "  <sys:node-uuid>" + nodeUUID + "</sys:node-uuid>\r\n";
            } else {
                nodeUUID = "";
            }
        }

        m_metadataFileWriter.write("<cm:folder " +
                "xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" " +
                "xmlns:alf=\"http://www.alfresco.org\" " +
                "xmlns:d=\"http://www.alfresco.org/model/dictionary/1.0\" " +
                "xmlns:view=\"http://www.alfresco.org/view/repository/1.0\" " +
                "xmlns:act=\"http://www.alfresco.org/model/action/1.0\" " +
                "xmlns:wf=\"http://www.alfresco.org/model/workflow/1.0\" " +
                "xmlns:app=\"http://www.alfresco.org/model/application/1.0\" " +
                "xmlns:ver=\"http://www.alfresco.org/model/versionstore/1.0\" " +
                "xmlns:usr=\"http://www.alfresco.org/model/user/1.0\" " +
                "xmlns:cm=\"http://www.alfresco.org/model/content/1.0\" " +
                "xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" " +
                "xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" " +
                "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" " +
                "xmlns:wcm=\"http://www.alfresco.org/model/wcmmodel/1.0\" " +
                "xmlns:wca=\"http://www.alfresco.org/model/wcmappmodel/1.0\" " +
                "xmlns:sys=\"http://www.alfresco.org/model/system/1.0\" " +
                "xmlns:wcmwf=\"http://www.alfresco.org/model/wcmworkflow/1.0\" " +
                "xmlns:rule=\"http://www.alfresco.org/model/rule/1.0\" " +
                "xmlns:fm=\"http://www.alfresco.org/model/forum/1.0\" " +
                "xmlns:bpm=\"http://www.alfresco.org/model/bpm/1.0\" " +
                "xmlns:custom=\"custom.model\" " +
                "xmlns=\"\" " +
                "view:childName=\"cm:" + dirName + "\">\r\n" +
                "<view:properties>\r\n" +
                // Skip icon, title, description so they are not updated, if they are set then they override existing
                //  "<app:icon>space-icon-default</app:icon>\r\n" +
                "  <cm:name>" + dirName + "</cm:name>\r\n" +
                nodeUUID +
                "  <sys:store-protocol>workspace</sys:store-protocol>\r\n" +
                "  <sys:store-identifier>SpacesStore</sys:store-identifier>\r\n" +
                "</view:properties>\r\n" +
                "<view:associations>\r\n" +
                "  <cm:contains>\r\n");

        // Navigate into this directory and get all contained directories and files
        getTree(dir, currentAlfrescoFolderPath);
    }

    /**
     * Generate the Metadata XML for a file
     *
     * @param file
     * @param dirname
     * @throws IOException
     */
    private static void generateMetadataForFile(File file, String dirname) throws IOException {
        String fileName = file.getName();
        String fileExtension = getExt(file.getName());
        String contentFileNumber = "content" + m_fileId + "." + fileExtension;

        m_metadataFileWriter.write(
                "<cm:content view:childName=\"cm:" + fileName + "\">\r\n" +
                        "  <view:aspects>\r\n" +
                        "    <cm:auditable></cm:auditable>\r\n" +
                        "    <sys:referenceable></sys:referenceable>\r\n" +
                        "    <cm:titled></cm:titled>\r\n" +
                        "  </view:aspects>\r\n" +
                        "  <view:acl></view:acl>\r\n" +
                        "  <view:properties>\r\n" +
                        "    <sys:store-protocol>workspace</sys:store-protocol>\r\n" +
                        "    <sys:store-identifier>SpacesStore</sys:store-identifier>\r\n" +
                        "    <cm:name>" + fileName + "</cm:name>\r\n" +
                        "    <cm:content>contentUrl=import\\" + contentFileNumber + "|mimetype=" + m_mimeTypeHelper.GetMimetype(fileExtension) + "|size=" + file.length() + "|encoding=UTF-8|locale=" + IMPORT_LOCALE + "</cm:content>\r\n" +
                        "    <cm:title>\r\n" +
                        "      <view:mlvalue view:locale=\"" + IMPORT_LOCALE + "\">" + fileName + "</view:mlvalue>\r\n" +
                        "    </cm:title>\r\n" +
                        "    <cm:description>\r\n" +
                        "      <view:mlvalue view:locale=\"" + IMPORT_LOCALE + "\"></view:mlvalue>\r\n" +
                        "    </cm:description>\r\n" +
                        "    <cm:creator>" + IMPORT_USER + "</cm:creator>\r\n" +
                        "    <cm:created>" + formatDate(new Date(file.lastModified())) + "</cm:created>\r\n" +
                        "    <cm:modifier>" + IMPORT_USER + "</cm:modifier>\r\n" +
                        "    <cm:modified>" + formatDate(new Date(file.lastModified())) + "</cm:modified>\r\n" +
                        "  </view:properties>\r\n" +
                        "</cm:content>\r\n");

        // Copy the file to the temporary import directory and
        // rename it as content0, content1, ...,contentn
        copyFile(file, new File(m_tempAcpContentDir.getPath() + "/" + contentFileNumber));
        System.out.println("Added file (" + file.getPath() + ") as /import/" + contentFileNumber);
        m_fileId++;
    }

    /**
     * Navigate all contained directories and files in passed in directory recursively
     *
     * @param directory
     * @throws IOException
     */
    private static void getTree(File directory, String currentAlfrescoFolderPath) throws IOException {
        System.out.println("About to navigate directory tree: " + directory.getPath());

        if (directory.isDirectory()) {
            File[] list = directory.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        generateMetadataForDir(list[i], currentAlfrescoFolderPath + "/" + list[i].getName());
                    } else {
                        generateMetadataForFile(list[i], directory.getName());
                    }
                }
            }

            m_metadataFileWriter.write("  </cm:contains>\r\n" +
                    "</view:associations>\r\n" +
                    "</cm:folder>\r\n");
        }
    }

    /**
     * Get the extension part of a file name
     *
     * @param filename
     * @return ext
     */
    private static String getExt(String filename) {
        // Getting the extension of a filename, (plain or including dirname)
        // This code is much faster than any regex technique.

        // Extension without the dot
        String ext = null;

        //	Where the last dot is. There may be more than one.
        int dotPlace = filename.lastIndexOf('.');
        if (dotPlace >= 0) {
            // Possibly empty
            ext = filename.substring(dotPlace + 1);
        } else {
            // Was no extension
            ext = "";
        }

        return ext;
    }

    /**
     * Method for returning the last modified file time and date in the alfresco
     * acp xml format (e.g. 2005-09-29T14:06:00.000+01:00)
     *
     * @param date
     * @return The input date and time in alfresco style in a string
     */
    private static String formatDate(Date date) {
        String output;
        Calendar lastModifiedDate = Calendar.getInstance();
        lastModifiedDate.setTime(date);
        output = lastModifiedDate.get(Calendar.YEAR) + "-" +
                getTwoDigitDate(lastModifiedDate.get(Calendar.MONTH) + 1) + "-" +
                getTwoDigitDate(lastModifiedDate.get(Calendar.DAY_OF_MONTH));
        output = output + "T" +
                getTwoDigitDate(lastModifiedDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                getTwoDigitDate(lastModifiedDate.get(Calendar.MINUTE)) + ":" +
                getTwoDigitDate(lastModifiedDate.get(Calendar.SECOND));
        output = output + ".000+01:00";
        return output;
    }

    /**
     * Method converting a single or double digit date element
     * (day of the month or month) to two digit format
     * (e.g. 1 becomes 01 and 11 remains 11)
     *
     * @param date A single or double digit int
     * @return The date element in two digit format
     */
    private static String getTwoDigitDate(int date) {
        String theDate = "" + date;
        if (theDate.length() == 1) return "0" + date;
        else return theDate;
    }

    /**
     * Copy a file from its original place to the temporary ACP directory
     *
     * @param sourceFile document file to copy into ACP
     * @param destFile   the file's location and name in the ACP
     */
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Creates the ACP file, which is basically a ZIP file
     *
     * @param acpFile    the location of the ACP file
     * @param acpTempDir the location of the ACP Temp dir
     * @throws IOException
     */
    private static void createACPFile(String acpFile, String acpTempDir) throws IOException {
        System.out.println("---- Creating ACP File: " + acpFile);

        File zip = new File(acpFile);
        FileOutputStream fos = new FileOutputStream(zip);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);

        zipDir(acpTempDir, zos, "");

        zos.close();
    }

    /**
     * Zips content in passed in source directory recursively.
     *
     * @param dir2zip     the temp directory with all the ACP content
     * @param zos
     * @param currentPath
     */
    private static void zipDir(String dir2zip, ZipOutputStream zos, String currentPath) {
        System.out.println("Populating ACP File from:  " + dir2zip);

        try {
            // Create a new File object based on the directory we have to zip
            File zipDir = new File(dir2zip);

            // Get a listing of the directory content
            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;

            // Loop through directory content and zip any files
            for (int i = 0; i < dirList.length; i++) {
                File f = new File(zipDir, dirList[i]);
                if (f.isDirectory()) {
                    // This is a directory so zip contained files recursively
                    String filePath = f.getPath();
                    currentPath += f.getName() + "\\";
                    zipDir(filePath, zos, currentPath);
                    continue;
                }

                // We got a file so add it to the ZIP
                BufferedInputStream fis = new BufferedInputStream(new FileInputStream(f));

                // Create a new zip entry
                if (!f.getPath().contains(currentPath)) {
                    currentPath = "";
                }
                ZipEntry anEntry = new ZipEntry(currentPath + f.getName());
                System.out.println("Adding file to ACP: " + f.getPath() + " (ACP path " + currentPath + ")");

                // Place the zip entry in the ZipOutputStream object
                zos.putNextEntry(anEntry);

                // Now write the content of the file to the ZipOutputStream
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }

                // Close ZIP Entry
                zos.closeEntry();

                // Close file put into Zip
                fis.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

