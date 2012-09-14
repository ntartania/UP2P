package up2p.repository;

import java.io.File;

/**
 * Entry in a resource list containing information on one resouce.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class ResourceEntry {
    /** ID of the resource. */
    public String resourceId;

    /** ID of the community where the resource is published. */
    public String communityId;

    /** Local file where the resource is stored. */
    public File resourceFile;

    /** Title of the resource. */
    public String title;

    /** Location URL for retrieving the resource from the local host. */
    public String URL;

    /**
     * Construct an entry with all information.
     * 
     * @param resId ID of the resource
     * @param commId ID of the community where the resource is published
     * @param resFile Local file where the resource is stored
     * @param resTitle Title of the resource
     * @param resURL Location URL for retrieving the resource from the local
     * host
     */
    public ResourceEntry(String resId, String commId, File resFile,
            String resTitle, String resURL) {
        resourceId = resId;
        communityId = commId;
        resourceFile = resFile;
        title = resTitle;
        URL = resURL;
    }

    /**
     * Construct an entry with everything but a local file.
     * 
     * @param resId ID of the resource
     * @param commId ID of the community where the resource is published
     * @param resTitle Title of the resource
     * @param resURL Location URL for retrieving the resource from the local
     * host
     */
    public ResourceEntry(String resId, String commId, String resTitle,
            String resURL) {
        resourceId = resId;
        communityId = commId;
        title = resTitle;
        URL = resURL;
    }
}