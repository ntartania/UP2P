package up2p.repository;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Holds a list of resources with their ID, their community, their local file
 * and their title. No duplicate resource IDs can be in the list and each
 * resource has exactly one of each attribute.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class ResourceList {
    /** Internal list of ResourceEntry objects keyed by resourceId. */
    private Map<String,ResourceEntry> resList;

    /**
     * Constructs an empty list.
     *  
     */
    public ResourceList() {
        resList = new HashMap<String,ResourceEntry>();
    }

    /**
     * Puts a resource in the list.
     * 
     * @param entry information on the resource
     */
    public void addResource(ResourceEntry entry) {
        resList.put(entry.resourceId, entry);
    }

    /**
     * Adds a resource to the list.
     * 
     * @param resourceId id of the resource
     * @param communityId id of the community where the resource is shared
     * @param resourceFile local file that holds the resource
     * @param resourceTitle title of the resource
     * @param resourceURL URL for downloading the resource
     */
    public void addResource(String resourceId, String communityId,
            File resourceFile, String resourceTitle, String resourceURL) {
        resList.put(resourceId, new ResourceEntry(resourceId, communityId,
                resourceFile, resourceTitle, resourceURL));
    }

    /**
     * Removes a resource from the list.
     * 
     * @param resourceId id of the resource
     */
    public void removeResource(String resourceId) {
        resList.remove(resourceId);
    }

    /**
     * Gets the community ID of a resource in this list.
     * 
     * @param resourceId ID of the resource
     * @return community ID of the resource or <code>null</code> if not found
     */
    public String getCommunityId(String resourceId) {
        Object o = resList.get(resourceId);
        if (o != null)
            return ((ResourceEntry) o).communityId;
        return null;
    }

    /**
     * Gets the resource file of a resource in this list.
     * 
     * @param resourceId ID of the resource
     * @return resource file of a resource or <code>null</code> if not found
     */
    public File getResourceFile(String resourceId) {
        Object o = resList.get(resourceId);
        if (o != null)
            return ((ResourceEntry) o).resourceFile;
        return null;
    }

    /**
     * Gets the file name of a resource in this list.
     * 
     * @param resourceId ID of the resource
     * @return file name of a resource or <code>null</code> if not found
     */
    public String getResourceFileName(String resourceId) {
        File f = getResourceFile(resourceId);
        if (f != null)
            return f.getName();
        return null;
    }

    /**
     * Gets the title of a resource in this list.
     * 
     * @param resourceId ID of the resource
     * @return title of the resource or <code>null</code> if not found
     */
    public String getResourceTitle(String resourceId) {
        Object o = resList.get(resourceId);
        if (o != null)
            return ((ResourceEntry) o).title;
        return null;
    }

    /**
     * Gets the URL of a resource in this list.
     * 
     * @param resourceId ID of the resource
     * @return title of the resource or <code>null</code> if not found
     */
    public String getResourceURL(String resourceId) {
        Object o = resList.get(resourceId);
        if (o != null)
            return ((ResourceEntry) o).URL;
        return null;
    }

    /**
     * Iterates over all the resource Ids in this list.
     * 
     * @return list of <code>String</code> resource ids
     */
    public Iterator<String> iterator() {
        return resList.keySet().iterator();
    }

    /**
     * Gets the number of resources in this list.
     * 
     * @return number of resources
     */
    public int getSize() {
        return resList.size();
    }

    /**
     * Clears all resources from the list.
     *  
     */
    public void clear() {
        resList.clear();
    }
}