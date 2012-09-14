package up2p.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters all file names ending in XML.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class XmlFilenameFilter implements FilenameFilter {

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilenameFilter#accept(File, String)
     */
    public boolean accept(File dir, String name) {
        // filter all files ending in xml
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex > -1) {
            String ending = name.substring(lastIndex + 1);
            return ending.equalsIgnoreCase("XML");
        }
        return false;
    }
}