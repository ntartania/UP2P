package up2p.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import up2p.repository.DatabaseAdapter;

/**
 * Maps local resource files to their IDs and attachments to local files by
 * resource ID and attachment name.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class FileMapper implements Serializable {
    
	/** random serial version number*/
	private static final long serialVersionUID = 80429L;
	
    /** Community file maps index by resource ID. */
    protected Map<String,CommunityFileMap> communityMaps;

    /** Database adapter for this map. */
    protected DatabaseAdapter dbAdapter;

    /** Collection used to hold community file maps. */
    protected Collection fileMapCollection;

    /** Logger used by this class. */
    protected Logger LOG;

    /** XPath Query Service */
    protected XPathQueryService qService;

    /** XUpdate Service */
    protected XUpdateQueryService xupService;

    /**
     * Constructs a file mapper.
     * 
     * @param databaseAdapter adapter for the database used by this file map
     */
    public FileMapper(DatabaseAdapter databaseAdapter) {
        dbAdapter = databaseAdapter;
        LOG = Logger.getLogger(WebAdapter.LOGGER);
        communityMaps = new HashMap<String,CommunityFileMap>();
        loadCommunityMaps();
    }

    /**
     * Creates a mapping for an attachment to a resource. The name of the
     * attachment must be unique within the context of the resource or it will
     * overwrite any previous mapping. If the resource is not found an exception
     * will be thrown.
     * 
     * @param communityId the id of the community where the resource is shared
     * @param resourceId the id of the resource with which the attachment is
     * associated
     * @param attachName the name of the attached file
     * @param attachFile the attached file
     * @throws FileNotFoundException if the attachment file is not found
     * @throws ResourceNotFoundException if the resource is not found
     * @throws CommunityNotFoundException 
     */
    public void addAttachment(String communityId, String resourceId,
            String attachName, File attachFile) throws FileNotFoundException,
            ResourceNotFoundException, CommunityNotFoundException {
        Object o = communityMaps.get(communityId);
        if (o != null) {
            ((CommunityFileMap) o).addAttachment(resourceId, attachName,
                    attachFile);
        } else
            throw new CommunityNotFoundException("Community id '" + communityId
                    + "' not found in the database.");
    }

    /**
     * Adds a community to the community list.
     * 
     * @param communityName the name of the community to map
     * @param communityId the id of the community
     */
    public void addCommunity(String communityName, String communityId) {
        communityMaps.put(communityId, new CommunityFileMap(communityName,
                communityId, dbAdapter));
    }

    /**
     * Create a mapping for a resource.
     * 
     * @param communityId the id of the community where the resource is shared
     * @param resourceId the id of the resource to share
     * @param fileLocation the local file that contains the resource
     * @throws FileNotFoundException if the resource file is not found
     * @throws CommunityNotFoundException 
     */
    public void addResource(String communityId, String resourceId,
            File fileLocation) throws FileNotFoundException, CommunityNotFoundException {
        Object o = communityMaps.get(communityId);
        if (o != null) {
            ((CommunityFileMap) o).addResource(resourceId, fileLocation);
        } else
            throw new CommunityNotFoundException("Community id '" + communityId
                    + "' not found in the database.");
    }

    /**
     * Returns the local file for the attachment of a resource.
     * 
     * @param communityId id of the community where the resource is shared
     * @param resourceId id of the shared resource
     * @param attachName name of the attachment (unique within the resource)
     * @return file for the attachment
     * @throws AttachmentNotFoundException if the attachment was not found
     * @throws CommunityNotFoundException 
     */
    public File getAttachmentMapping(String communityId, String resourceId,
            String attachName) throws AttachmentNotFoundException, CommunityNotFoundException {
    	LOG.debug("FileMapper: Getting file for attachment:"+attachName+ "|"+communityId+"|"+resourceId);
        Object o = communityMaps.get(communityId);
        if (o != null) {
        	File res =((CommunityFileMap) o).getAttachmentMapping(resourceId,
                    attachName); 
        	LOG.debug("FileMapper: file: "+res.getAbsolutePath());
            return res;
        }
        throw new CommunityNotFoundException("Community id '" + communityId
                + "' not found in the database.");
    }

    /**
     * Returns a list of unique attachment names associated with this resource.
     * 
     * @param communityId id of the community where the resource is shared
     * @param resourceId id of the shared resource
     * @return a list of <code>String</code> attachment names
     * @throws CommunityNotFoundException 
     */
    public Iterator<String> getAttachmentNames(String communityId, String resourceId) throws CommunityNotFoundException {
        Object o = communityMaps.get(communityId);
        if (o != null) {
            return ((CommunityFileMap) o).attachments(resourceId);
        }
        throw new CommunityNotFoundException("Community id '" + communityId
                + "' not found in the database.");
    }

    /**
     * Returns the number of communities held in the FileMapper.
     * 
     * @return the number of communities
     */
    public int getCommunityCount() {
        try {
            if (fileMapCollection != null)
                return fileMapCollection.getResourceCount();
        } catch (XMLDBException e) {
        }
        return 0;
    }

    /**
     * Returns a list of community IDs.
     * 
     * @return list of <code>String</code> community IDs sorted
     * lexicographically
     */
    public Iterator<String> getCommunityIds() {
        TreeSet<String> ids = new TreeSet<String>();
        try {
            String[] resourceIds = fileMapCollection.listResources();
            for (int i = 0; i < resourceIds.length; i++) {
                ids.add(resourceIds[i]);
            }
        } catch (XMLDBException e) {
            LOG.error("FileMapper Error listing community IDs.", e);
        }
        return ids.iterator();
    }

    /**
     * Returns a list of community names.
     * 
     * @return a list of <code>String</code> names for communities sorted
     * lexicographically
     */
    public Iterator<String> getCommunityNames() {
        TreeSet<String> names = new TreeSet<String>();
        try {
            // run XPath query to get names
            ResourceSet result = qService.query("community/@name");
            if (result.getSize() > 0) {
                for (int i = 0; i < result.getSize(); i++) {
                    names.add((String) result.getResource(i).getContent()); // I think it must be strings we're talking about
                }
            }
        } catch (XMLDBException e) {
            LOG.error("FileMapper Error getting community names.", e);
        }
        return names.iterator();
    }

    /**
     * Lists the resources ids in a community.
     * 
     * @param communityId id of the community where the resources are shared
     * @return a list of <code>String</code> resource ids
     * @throws CommunityNotFoundException 
     */
    public Iterator<String> getResourceIds(String communityId) throws CommunityNotFoundException {
        Object o = communityMaps.get(communityId);
        if (o != null) {
            return ((CommunityFileMap) o).resources();
        }
        throw new CommunityNotFoundException("Community id '" + communityId
                + "' not found in the database.");
    }

    /**
     * Returns the local file for the resource.
     * 
     * @param communityId the id of the community where the resource is shared
     * @param resourceId the id of the resource to get
     * @return the file on the local file system
     * @throws ResourceNotFoundException if the resource or file was not found
     * @throws CommunityNotFoundException 
     */
    public File getResourceMapping(String communityId, String resourceId)
            throws ResourceNotFoundException, CommunityNotFoundException {
    	LOG.debug("FileMapper: Getting file for resource:"+communityId +"|"+ resourceId);
        CommunityFileMap o = communityMaps.get(communityId);
        if (o != null) {
        	File res =o.getResourceMapping(resourceId); 
        	
        	LOG.debug("FileMapper: file: "+res.getPath());
            return res;
        }
        LOG.error("FileMapper::getResourceMapping:Error: community file map not found!");
        throw new CommunityNotFoundException("Community id '" + communityId
                + "' not found in the database.");
    }

    /**
     * Checks if the resource is found in the given community and if the local
     * file exists.
     * 
     * @param communityId id of the community where the resource is shared
     * @param resourceId id of the resource to check
     * @return <code>true</code> if the resource has a mapping and exists,
     * <code>false</code> otherwise
     */
    public boolean isMapped(String communityId, String resourceId) {
        try {
            File f = getResourceMapping(communityId, resourceId);
            return f.exists();
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (CommunityNotFoundException e) {
			// TODO Auto-generated catch block
        	return false;
		}
    }

    /**
     * Loads community map objects for each community in the database.
     *  
     */
    private void loadCommunityMaps() {
        LOG.debug("Initializing file maps.");
        try {
            try {
                fileMapCollection = DatabaseManager.getCollection(dbAdapter
                        .getFileMapUrl());
                if (fileMapCollection == null) {
                    // collection does not exist in the database
                    LOG.debug("FileMap collection not found in the "
                            + "database, creating a new Collection.");
                    // collection does not exist, get root collection and create
                    fileMapCollection = dbAdapter
                            .createCollection(DatabaseAdapter.DB_COMMUNITY_FILE_MAP, false);
                }
            } catch (XMLDBException e) {
                if (e.errorCode == ErrorCodes.NO_SUCH_DATABASE) {
                    // collection does not exist in the database
                    LOG.debug("FileMap collection not found in the "
                            + "database, creating a new Collection.");
                    // collection does not exist, get root collection and create
                    fileMapCollection = dbAdapter
                            .createCollection(DatabaseAdapter.DB_COMMUNITY_FILE_MAP, false);
                } else
                    LOG.error("Error initializing the file map.", e);
            }
            // initialize the services
            xupService = (XUpdateQueryService) fileMapCollection.getService(
                    "XUpdateQueryService", "1.0");
            qService = (XPathQueryService) fileMapCollection.getService(
                    "XPathQueryService", "1.0");

            // load each community file map
            String[] resourceList = fileMapCollection.listResources();
            for (int i = 0; i < resourceList.length; i++) {
                // create the community map and add it to the local hash table
                communityMaps.put(resourceList[i], new CommunityFileMap(
                        resourceList[i], dbAdapter));
            }
        } catch (XMLDBException e) {
            LOG.error("Error initializing the file map.", e);
        }
    }

    /**
     * Removes a mapping for an attachment to a resource.
     * 
     * @param communityId the id of the community where the resource is shared
     * @param resourceId the id of the resource with which the attachment is
     * associated
     * @param attachName the name of the attached file
     * @throws ResourceNotFoundException if the resource was not found
     * @throws AttachmentNotFoundException if the attachment was not found
     * @throws CommunityNotFoundException 
     * @throws ResourceNotFoundException 
     */
    public void removeAttachment(String communityId, String resourceId,
            String attachName) throws AttachmentNotFoundException, CommunityNotFoundException, ResourceNotFoundException {
        Object o = communityMaps.get(communityId);
        if (o != null) {
            ((CommunityFileMap) o).removeAttachment(resourceId, attachName);
        } else
            throw new CommunityNotFoundException("Community id '" + communityId
                    + "' not found in the database.");
    }

    /**
     * Removes a community from the community list.
     * 
     * @param resourceId the id of the community
     */
    public void removeCommunity(String resourceId) {
        Object o = communityMaps.get(resourceId);
        
        if (o == null) {
            LOG.warn("FileMapper Tried to remove a community that did not exist. Id "
                            + resourceId);
            return;
        }
        CommunityFileMap comMap = ((CommunityFileMap) o);
        comMap.remove();
        communityMaps.remove(resourceId);
    }

    /**
     * Removes a resource entry from a file map and all its associated
     * attachments.
     * 
     * @param communityId the id of the community where the resource is shared
     * @param resourceId the id of the resource to remove
     * @throws ResourceNotFoundException if the resource was not found
     * @throws CommunityNotFoundException 
     */
    public void removeResource(String communityId, String resourceId)
            throws ResourceNotFoundException, CommunityNotFoundException {
        Object o = communityMaps.get(communityId);
        if (o != null) {
            ((CommunityFileMap) o).removeResource(resourceId);
        } else
            throw new CommunityNotFoundException("Community id '" + communityId
                    + "' not found in the database.");
    }
}