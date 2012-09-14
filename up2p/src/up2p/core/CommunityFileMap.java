package up2p.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import up2p.repository.DatabaseAdapter;
import up2p.repository.Repository;
import up2p.search.SearchQuery;
import up2p.xml.TransformerHelper;
import up2p.xml.XUpdateHelper;

/**
 * Maps physical files to their resource IDs in a community. One map of this
 * type is used per community. All mapping information is stored in the local
 * XML database.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class CommunityFileMap {
    /** Name of Logger used by this class. */
    public static final String LOGGER = Repository.LOGGER;

    /**
     * Parses through a file path and replaces any file separator character with
     * their correct system-dependent equivalent.
     * 
     * @param inputPath path to correct
     * @return corrected path
     */
    private static String convertFilePath(String inputPath) {
        char wrongSeparator;
        if (File.separatorChar == '/')
            wrongSeparator = '\\';
        else
            wrongSeparator = '/';

        StringBuffer buf = new StringBuffer(inputPath);
        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == wrongSeparator)
                buf.setCharAt(i, File.separatorChar);
        }
        return buf.toString();
    }

    /** Creates the initial XML stub for use in the database.
     * 
     * @param communityName name of the community
     * @param resourceId resource ID of the community
     * @return XML stub for a community map
     */
    public static Node getXMLStub(String communityName, String resourceId) {
        // create root node
        Document doc = TransformerHelper.newDocument();
        Element root = doc.createElement("community");
        doc.appendChild(root);

        // add community info and stub nodes
        root.setAttribute("name", communityName);
        root.setAttribute("id", resourceId);
        root.setAttribute("resources", "0");

        Element sharedResources = doc.createElement("sharedResources");
        root.appendChild(sharedResources);

        return root;
    }

    /** Name of the community. */
    protected String communityName;

    /** Database adapter for this map. */
    protected DatabaseAdapter dbAdapter;

    /** Collection used to hold community file maps. */
    protected Collection fileMapCollection;

    /** Logger used by this class. */
    protected Logger LOG;

    /** XPath Query Service */
    protected XPathQueryService qService;

    /** Resource ID of the community. */
    protected String resourceId;

    /** XUpdate Service */
    protected XUpdateQueryService xupService;
    
    /**
     * A cached version of the community map that only contains resource Id's and
     * locations. This cache is specifically kept to avoid accessing the database every
     * time a resource mapping is required (very slow)
     * 
     * Map<Resource Id, Location>
     */
    private Map<String, String> locationCache;
    
    /** A flag to indicate that the cache should be updated before a mapping is fetched */
    private volatile boolean dirtyCache;

    /**
     * Constructs a file map for an existing file map stored in the database.
     * 
     * @param resId the id of the community to map
     * @param databaseAdapter adapter for the database in use by this map
     */
    public CommunityFileMap(String resId, DatabaseAdapter databaseAdapter) {
        LOG = Logger.getLogger(LOGGER);
        resourceId = resId;
        dbAdapter = databaseAdapter;
        locationCache = new HashMap<String, String>();
        dirtyCache = true;
        
        // Get the community name
        try {
            fileMapCollection = DatabaseManager.getCollection(dbAdapter
                    .getFileMapUrl(), "admin", null);
           
            Resource res = fileMapCollection.getResource(resourceId);
            if (res == null) {
                LOG.warn("CommunityFileMap File Map for community "
                        + resourceId + " not found, wrong constructor used.");
            } else {
                // get the community name from the database
            	
                Node rootNode = ((XMLResource) res).getContentAsDOM().getFirstChild();
                LOG.info("CommunityFileMap: root node " + rootNode);
                if (rootNode.getLocalName().equals("community")){
                    this.communityName = ((Element) rootNode)
                            .getAttribute("name");
                }
            }
        } catch (XMLDBException e) {
        	LOG.error(
                    "CommunityFileMap Error initializing community file map.",
                    e);
        } catch (Exception e){
        	LOG.info("CommunityFileMap: Node exception is thrown " + e);
        }
        LOG.info("CommunityFileMap: starts initializing DB");	
        initializeDb();
    }

    /**
     * Constructs a file map for the given community.
     * 
     * @param communityName the name of the community to map
     * @param resourceId the id of the community to map
     * @param databaseAdapter adapter for the database in use by this map
     */
    public CommunityFileMap(String communityName, String resourceId,
            DatabaseAdapter databaseAdapter) {
        LOG = Logger.getLogger(LOGGER);
        this.resourceId = resourceId;
        this.communityName = communityName;
        dbAdapter = databaseAdapter;
        locationCache = new HashMap<String, String>();
        dirtyCache = true;

        // create initial structure in db
        initializeDb();
    }

    
    /**
     * Adds an attachment to a resource to the file map.
     * 
     * @param resourceEntryId id of the shared resource
     * @param attachName name of the attachment (unique within resource)
     * @param attachLocation file location of the attachment
     * @throws FileNotFoundException if the attachment file is not found
     * @throws ResourceNotFoundException if the resource is not found
     */
    public synchronized void addAttachment(String resourceEntryId, String attachName,
            File attachLocation) throws FileNotFoundException,
            ResourceNotFoundException {
    	
        // check if the attachment file exists
        if (!attachLocation.exists())
            throw new FileNotFoundException("Attachment file not found: "
                    + attachLocation.getAbsolutePath());
        try {
            // check if the resource exists
            ResourceSet result = qService.queryResource(resourceId,
                    "//community/sharedResources/resource[@id='"
                            + resourceEntryId + "']");
            if (result.getSize() == 0) {
                // resource does not exist
                String err = "CommunityFileMap Error adding attachment to resource "
                        + resourceEntryId
                        + " entry in '"
                        + communityName
                        + "' community file map. Resource entry does not exist.";
                LOG.error(err);
                throw new ResourceNotFoundException(err);
            }

            // check if attachment exists
            result = qService.queryResource(resourceId,
                    "//community/sharedResources/resource[@id='"
                            + resourceEntryId + "']/attachment[@name='"
                            + SearchQuery.normalizeQueryString(attachName)
                            + "']");
                            
            if (result.getSize() > 0) {
                // attachment exists so remove it
                LOG.debug("CommunityFileMap Attachment " + attachName
                        + " already exists. Checking for a duplicate.");
                try {
                    // get the currently mapped attachment
                    File attachFile = getAttachmentMapping(resourceEntryId,
                            attachName);
                    if (!attachFile.getCanonicalFile().equals(
                            attachLocation.getCanonicalFile())) {
                        // duplicate attachment - not unusual
                        LOG.debug("CommunityFileMap Duplicate attachment"
                                + " found. " + "Skipping it.");
                        return;
                    }
                    // not a duplicate
                    LOG.debug("CommunityFileMap Found duplicate attachment"
                            + " name with different files. Replacing "
                            + "previous version.");
                    try {
                        removeAttachment(resourceEntryId, attachName);
                    } catch (AttachmentNotFoundException e) {
                        LOG.error("CommunityFileMap Error removing "
                                + "previous version of duplicate "
                                + "attachment. Attachment not " + "found: '"
                                + attachName + "'", e);
                    } catch (ResourceNotFoundException f) {
                        LOG.error("CommunityFileMap Error removing "
                                + "previous version of duplicate "
                                + "attachment. Resource not found: "
                                + resourceEntryId);
                    }
                } catch (AttachmentNotFoundException e) {
                    // must be an error in mapping
                    LOG.error("CommunityFileMap Attachment name '" + attachName
                            + "' for resource id " + resourceEntryId
                            + " not available in the community map, but"
                            + " found when adding a duplicate attachment.");
                }
            }

            // create an XUpdate blob to insert into the database
            // <attachment name="..." location="..." size="...">
            String xUpdateBlob = XUpdateHelper.appendElement(
                    "//community/sharedResources/resource[@id='"
                            + resourceEntryId + "']", "attachment",
                    XUpdateHelper.createAttribute("name", attachName)
                            + XUpdateHelper.createAttribute("location",
                                   // attachLocation.getCanonicalPath())
                            		attachLocation.getName())
                            + XUpdateHelper.createAttribute("size", String
                                    .valueOf(attachLocation.length())));
            //            LOG.debug("CommunityFileMap With XUpdate expression: "
            //                    + xUpdateBlob);

            // do the update and log errors if update failed
            if (xupService.updateResource(resourceId, xUpdateBlob) != 1)
                LOG.error("CommunityFileMap XUpdate failed on resource "
                        + resourceEntryId + " entry in '" + communityName
                        + "' community file map.");

            // update the resource count for the community
            incrementAttachmentCount(resourceEntryId);
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Error adding attachment to resource "
                    + resourceEntryId + " entry in '" + communityName
                    + "' community file map.", e);
        } catch (IOException e) {
            LOG.error("CommunityFileMap Error handling file: "
                    + attachLocation.getAbsolutePath(), e);
        }
    }

    /**
     * Adds a resource to a community by inserting the resource id and file
     * location into the db.
     * 
     * @param resourceEntryId id of the resource to share
     * @param fileLocation the local file that contains the resource
     * @throws FileNotFoundException if the resource file is not found
     */
    public synchronized void addResource(String resourceEntryId, File fileLocation)
            throws FileNotFoundException {
        dirtyCache = true;
    	
    	if (!fileLocation.exists())
            throw new FileNotFoundException("File not found: "
                    + fileLocation.getAbsolutePath());
        try {
            // check if the resource already exists
            ResourceSet result = qService.queryResource(resourceId,
                    "//community/sharedResources/resource[@id='"
                            + resourceEntryId + "']");
            
            if (result.getSize() > 0) {
                // resource already exists so replace it
                LOG.info("CommunityFileMap Overwriting resource "
                        + resourceEntryId + " entry in '" + communityName
                        + "' community file map.");
                removeResource(resourceEntryId);
            }

            // create an XUpdate blob to insert into the database
            // <resource id="..." location="..." attachCount="..." size="...">
            String elementValue = XUpdateHelper
	            .createAttribute("id", resourceEntryId)
	            + XUpdateHelper.createAttribute("location",
	            		fileLocation.getName())
	            + XUpdateHelper.createAttribute("size",
	                    String.valueOf(fileLocation
	                            .length()))
	            + XUpdateHelper.createAttribute(
	                    "attachCount", "0");
            
            String xUpdateBlob = XUpdateHelper
                    .appendElement("//community/sharedResources", "resource", elementValue);
            
            // LOG.debug("CommunityFileMap Executing XUpdate expression: "
            // 		+ xUpdateBlob);

            // do the update and log errors if update failed
            if (xupService.updateResource(resourceId, xUpdateBlob) != 1)
                LOG.error("CommunityFileMap XUpdate failed on resource "
                        + resourceEntryId + " entry in '" + communityName
                        + "' community file map.");

            // update the resource count for the community
            incrementResourceCount();
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Error adding resource "
                    + resourceEntryId + " entry in'" + communityName
                    + " community.", e);
       /* } catch (IOException e) {
            LOG.error("CommunityFileMap Error handling file: "
                    + fileLocation.getAbsolutePath(), e);*/
        } catch (ResourceNotFoundException e) {
        	LOG.error("CommunityFileMap tried to remove non-existing resource: "+ e);
		}
    }

    /**
     * Lists the attachment names for a given resource.
     * 
     * @param resourceEntryId id of the shared resource
     * @return a list of <code>String</code> attachment names that can be used
     * to retrieve attachments through <code>getAttachmentMapping()</code>
     */
    public synchronized Iterator<String> attachments(String resourceEntryId) {
        String err = "Failed to list attachments for resource "
                + resourceEntryId + " in '" + communityName
                + "' community file map.";

        try {
            // query the database
            ResourceSet result = qService.queryResource(resourceId,
                    "//community/sharedResources/resource[@id='"
                            + resourceEntryId + "']/attachment/@name");

            // create an array to hold names
            SortedSet<String> attachments = new TreeSet<String>();
            ResourceIterator resultIterator = result.getIterator();
            while (resultIterator.hasMoreResources()) {
                XMLResource res = (XMLResource) resultIterator.nextResource();
                attachments.add(res.getContentAsDOM().getFirstChild().getAttributes().getNamedItem("name").getNodeValue());
            }
            return attachments.iterator();
        } catch (XMLDBException e) {
            LOG.error(err, e);
        }
        // error occured so return an empty iterator
        return new TreeSet<String>().iterator();
    }

    /**
     * Clears all entries in the community file map such that no resources are
     * shared in the community or left in the database.
     */
    public synchronized void clearEntries() {
        dirtyCache = true;
    	
    	LOG.debug("CommunityFileMap Clearing all resource entries and creating database.");
       
    	// Remove the database
        remove();
        // Recreate the database
        createDatabase();
    }

    /**
     * Creates the XML in the database for this community file map. Overwrites
     * all existing entries in the database.
     */
    protected synchronized  void createDatabase() {
    	dirtyCache = true;
    	
        try {
            // create file map Collection if necessary
            if (fileMapCollection == null) {
                LOG
                        .debug("CommunityFileMap File map Collection not found in the "
                                + "database, creating a new Collection.");
                // collection does not exist, get root collection and create
                fileMapCollection = dbAdapter
                        .createCollection(DatabaseAdapter.DB_COMMUNITY_FILE_MAP, false);
            }

            // create community file map if necessary
            if (fileMapCollection.getResource(resourceId) == null) {
                // create the XML for this community file map
                XMLResource document = (XMLResource) fileMapCollection
                        .createResource(resourceId, "XMLResource");
                document.setContentAsDOM(getXMLStub(communityName, resourceId));

                // store in the database
                fileMapCollection.storeResource(document);
                LOG.debug("CommunityFileMap Created XML stub for '"
                        + communityName + "' community file map.");
            }
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Error occured in CommunityFileMap.", e);
        }
    }

    /**
     * Decrements the attachment count for a resource.
     * 
     * @param resourceEntryId id of the shared resource
     */
    private synchronized void decrementAttachmentCount(String resourceEntryId) {
    	updateCommunityFileMapCount(
                "//community/sharedResources/resource[@id='" + resourceEntryId
                        + "']/@attachCount", -1);
    }

    /**
     * Decrements the count of shared resources in the community file map.
     */
    private synchronized void decrementResourceCount() {
    	dirtyCache = true;
        updateCommunityFileMapCount("//community/@resources", -1);
    }

    /**
     * Returns the number of attachments associated with a resource.
     * 
     * @param resourceEntryId id of the shared resource
     * @return number of attachments for the given resource
     */
    public synchronized int getAttachmentCount(String resourceEntryId) {
    	int currentValue = 0;
        try {
            // query the resource to get the current attribute value
            ResourceSet result = qService.queryResource(resourceId,
                    "//community/sharedResources/resource[@id='"
                            + resourceEntryId + "']/@attachCount");
            if (result.getSize() == 0) {
                LOG
                        .error("CommunityFileMap Failed to get attachment count in resource "
                                + resourceEntryId
                                + " in '"
                                + communityName
                                + "' community file map.");
                return 0;
            }

            // parse the current value
            XMLResource resultRes = (XMLResource) result.getResource(0);
            Node attr = resultRes.getContentAsDOM().getFirstChild()
            	.getAttributes().getNamedItem("attachCount");
            try {
                currentValue = Integer.parseInt(attr.getNodeValue());
            } catch (NumberFormatException e) {
                LOG.error(e);
            }
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Failed to get resource count in '"
                    + communityName + "' community file map.", e);
        }
        return currentValue;
    }

    
    public synchronized String getAttachmentAsString (String resourceEntryId, String attachName) throws AttachmentNotFoundException {
    	return getAttachmentMapping(resourceEntryId, attachName).getName();
    }
    
    /**
     * Returns the local file for the attachment of a resource.
     * 
     * @param resourceEntryId id of the shared resource
     * @param attachName name of the attachment (unique within the resource)
     * @return file for the attachment
     * @throws AttachmentNotFoundException if the attachment was not found
     */
    public synchronized File getAttachmentMapping(String resourceEntryId, String attachName)
            throws AttachmentNotFoundException {
        String err = "Failed to get attachment mapping '" + attachName
                + "' for resource " + resourceEntryId + " in '" + communityName
                + "' community file map.";

        try {
            // query the database
            String query = "//community/sharedResources/resource[@id='"
                    + resourceEntryId + "']/attachment[@name ='"
                    + SearchQuery.normalizeQueryString(attachName)
                    + "']/@location";
            ResourceSet result = qService.queryResource(resourceId, query);
            if (result.getSize() == 0) {
                LOG
                        .debug("CommunityFileMap Attachment not found in file map. Attach name "
                                + attachName);
                throw new AttachmentNotFoundException(err);
            }

            // parse the current value
            XMLResource resultRes = (XMLResource) result.getResource(0);
            Node attr = resultRes.getContentAsDOM().getFirstChild().getAttributes().getNamedItem("location");
			
            LOG.debug("CommunityFileMap Attachment location "
                    + attr.getNodeValue());
            File realFile = new File(convertFilePath(attr.getNodeValue()));
            
            // TODO: Testing: we remove the absolute file path
            return realFile;
            
            } catch (XMLDBException e) {
				
				LOG.error(e);
			}
            
            return null;
    }
    
    /**
     * Updates the local cache of the community file map, and sets the dirty flag to false.
     * @throws XMLDBException
     */
    public synchronized void updateCache() throws XMLDBException {
    		LOG.debug("CommunityFileMap updating cache for community: " + resourceId);
    		ResourceSet cachedResources = qService.queryResource(resourceId,
        	"//community/sharedResources/resource");
    		locationCache.clear();
    		ResourceIterator resItr = cachedResources.getIterator();
    		while(resItr.hasMoreResources()) {
    			Node xmlRes = ((XMLResource)resItr.nextResource()).getContentAsDOM().getFirstChild();
    			locationCache.put(
    					xmlRes.getAttributes().getNamedItem("id").getNodeValue(),
    					xmlRes.getAttributes().getNamedItem("location").getNodeValue());
    		}
    		dirtyCache = false;
    }

    /**
     * Returns the number of resources shared in the community. Looks up and
     * parses the value directly in the database.
     * 
     * @return the number of shared resources
     */
    public synchronized int getResourceCount() {
        int currentValue = 0;
        try {
            // query the resource to get the current attribute value
            ResourceSet result = qService.queryResource(resourceId,
                    "//community/@resources");
            if (result.getSize() == 0) {
                LOG.error("CommunityFileMap Failed to get resource count in '"
                        + communityName + "' community file map.");
                return 0;
            }

            // parse the current value
            XMLResource resultRes = (XMLResource) result.getResource(0);
            Node attr = resultRes.getContentAsDOM().getFirstChild().getAttributes().getNamedItem("resources");
            try {
                currentValue = Integer.parseInt(attr.getNodeValue());
            } catch (NumberFormatException e) {
                LOG.error(e);
            }
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Failed to get resource count in '"
                    + communityName + "' community file map.", e);
        }
        return currentValue;
    }

    /**
     * Returns the local file for the resource.
     * changed: now returns a relative path
     * 
     * @param resourceEntryId id of the shared resource
     * @return file for the resource
     * @throws ResourceNotFoundException if the resource was not found
     */
    public synchronized File getResourceMapping(String resourceEntryId)
            throws ResourceNotFoundException {
    	LOG.debug("CommunityFileMap: getResourceMapping for Rid:"+resourceEntryId);
        String err = "CommunityFileMap: Failed to get resource mapping for resource "
                + resourceEntryId
                + " in '"
                + communityName
                + "' community file map.";

        try {
        	// Update the local cache if necessary
        	if(dirtyCache) { updateCache(); };
            
        	// Lookup the resource location in the local cache
        	if(locationCache.get(resourceEntryId) == null) {
        		throw new ResourceNotFoundException(err);
        	} else {
        		return new File(convertFilePath(locationCache.get(resourceEntryId)));
        	}

        } catch (XMLDBException e) {
            LOG.error(err, e);
            throw new ResourceNotFoundException(err);
        }
    }

    /**
     * Increments the attachment count for a resource.
     * 
     * @param resourceEntryId id of the shared resource
     */
    private synchronized void incrementAttachmentCount(String resourceEntryId) {
        updateCommunityFileMapCount(
                "//community/sharedResources/resource[@id='" + resourceEntryId
                        + "']/@attachCount", 1);
    }

    /**
     * Increments the count of shared resources in the community file map.
     */
    private synchronized void incrementResourceCount() {
    	dirtyCache = true;
        updateCommunityFileMapCount("//community/@resources", 1);
    }

    /**
     * Initializes the database and services.
     *  
     */
    protected synchronized void initializeDb() {
    	dirtyCache = true;
    	
        try {
            LOG.info("CommunityFileMap Initializing file map for community '"
                    + communityName + "'.");
            fileMapCollection = DatabaseManager.getCollection(dbAdapter
                    .getFileMapUrl(), "admin", "null");
            createDatabase();

            // Initialize the services
            xupService = (XUpdateQueryService) fileMapCollection.getService(
                    "XUpdateQueryService", "1.0");
            qService = (XPathQueryService) fileMapCollection.getService(
                    "XPathQueryService", "1.0");
        } catch (XMLDBException e) {
            LOG
                    .error(
                            "CommunityFileMap Error retrieving community file map database.",
                            e);
        }
    }

    /**
     * Completely removes this file map from the database.
     */
    public synchronized void remove() {
    	dirtyCache = true;
        try {
            Resource res = fileMapCollection.getResource(resourceId);
            if (res != null)
                fileMapCollection.removeResource(res);
            else
                LOG
                        .warn("CommunityFileMap Attempted to remove a file map for community "
                                + resourceId + " that does not exist.");
        } catch (XMLDBException e) {
            LOG.error(
                    "CommunityFileMap Failed to remove file map for community "
                            + resourceId, e);
        }
    }

    /**
     * Removes an attachment from a resource entry in the community file map.
     * 
     * @param resourceEntryId id of the shared resource
     * @param attachName name of the attachment (unique within resource)
     * @throws ResourceNotFoundException if the resource was not found
     * @throws AttachmentNotFoundException if the attachment was not found
     */
    public synchronized void removeAttachment(String resourceEntryId, String attachName)
            throws ResourceNotFoundException, AttachmentNotFoundException {

        // Update the XML in the database using XUpdate
        String selectExpression = "//community/sharedResources/resource[@id='"
                + resourceEntryId + "']/attachment[@name='"
                + SearchQuery.normalizeQueryString(attachName) + "']";
        String xUpdateBlob = XUpdateHelper.remove(selectExpression);
        try {
            // Do the update
            long numUpdates = xupService
                    .updateResource(resourceId, xUpdateBlob);
            // Log errors if update failed
            if (numUpdates < 1) {
                LOG
                        .error("CommunityFileMap XUpdate failed to remove an attachment with select expression: "
                                + selectExpression);
                // Check if resource exists
                ResourceSet result = qService.queryResource(resourceId,
                        "//community/sharedResource/resource[@id='"
                                + resourceEntryId + "']");
                if (result.getSize() == 0)
                    throw new ResourceNotFoundException(
                            "Failed to retrieve unknown resource "
                                    + resourceEntryId
                                    + " to remove attachment '" + attachName
                                    + "'.");
                throw new AttachmentNotFoundException(
                        "Failed to remove unknown attachment '" + attachName
                                + "' from resource " + resourceId + ".");
            }

            // Decrement the resource counter
            decrementAttachmentCount(resourceEntryId);
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Failed to remove attachment '"
                    + attachName + "' in resource " + resourceEntryId
                    + " entry in '" + communityName + "' community file map.");
            LOG
                    .error("CommunityFileMap Select expression: "
                            + selectExpression);
            LOG.error("CommunityFileMap XUpdate blob: " + xUpdateBlob);
            LOG.error("CommunityFileMap Exception:", e);
        }
    }

    /**
     * Removes a resource entry from a file map and all its associated
     * attachments.
     * 
     * @param resourceEntryId id the of the shared resource to remove
     * @throws ResourceNotFoundException if the resource was not found
     */
    public synchronized void removeResource(String resourceEntryId)
            throws ResourceNotFoundException {
    	dirtyCache = true;
        try {
            // Update the XML in the database using XUpdate
            String selectExpression = "//community/sharedResources/resource[@id='"
                    + resourceEntryId + "']";
            String xUpdateBlob = XUpdateHelper.remove(selectExpression);
            // Do the update
            long numUpdates = xupService
                    .updateResource(resourceId, xUpdateBlob);
            // Log errors if update failed
            if (numUpdates < 1) {
                LOG
                        .error("CommunityFileMap Failed to remove a resource with select expression: "
                                + selectExpression);
                throw new ResourceNotFoundException(
                        "Failed to remove resource '" + resourceEntryId
                                + "' in community '" + communityName + "'.");
            }

            // Decrement the resource counter
            decrementResourceCount();
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Failed to remove resource "
                    + resourceEntryId + " entry in '" + communityName
                    + "' community file map.", e);
        }
    }

    /**
     * Lists the resource IDs in this community file map.
     * 
     * @return a list of <code>String</code> resource IDs that can be used to
     * retrieve resource mappings through <code>getResourceMapping()</code>
     */
    public synchronized Iterator<String> resources() {
        String err = "Failed to list resource IDs in '" + communityName
                + "' community file map.";

        try {
            // Update the local cache if necessary
        	if(dirtyCache) { updateCache(); };
        	
        	// Add all the resource Id's in he local cache to the set to return
            SortedSet<String> ids = new TreeSet<String>();
            
            Iterator<String> resIdIter = locationCache.keySet().iterator();
            while(resIdIter.hasNext()) {
            	ids.add(resIdIter.next());
            }

            return ids.iterator();
            
        } catch (XMLDBException e) {
            LOG.error(err, e);
        }
        
        // Error occurred so return an empty iterator
        return new TreeSet<String>().iterator();
    }

    /**
     * Shuts down the file map and releases its associated resources in the
     * database.
     */
    public synchronized void shutdown() {
    	dirtyCache = true;
        try {
            fileMapCollection.close();
        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Failed to shutdown collection for '"
                    + communityName + "' community file map.", e);
        }
    }

    /**
     * Updates the counter found at the given node in the document by the amount
     * given as <code>count</code>. This method first finds the select
     * expression in the community file map XML and parses the current integer
     * value. It then adds the given value to the current value and runs XUpdate
     * to update the XML in the database with the new value. Can be used to
     * update both resource and attachment counts.
     * 
     * @param selectExpression XPath expression to the attribute or element to
     * update
     * @param count the amount to add or subtract from the attribute or element
     */
    private synchronized void updateCommunityFileMapCount(String selectExpression, int count) {
    	dirtyCache = true;
    	
        // Get the current value of the attribute
        // Try to get collection
    	LOG.debug("CommunityFileMap: updateCommunityMapCount, resID="+resourceId+", selectExpr="+selectExpression);
        try {
            // Query the resource to get the current attribute value
            ResourceSet result = qService.queryResource(resourceId,
                    selectExpression);
            LOG.debug("CommunityFileMap: update... step #0... just querying the DB!");
            if (result.getSize() == 0 || result==null) {
                LOG
                        .error("CommunityFileMap Trying to update an expression that is not in the database "
                                + selectExpression);
                return;
            }
            LOG.debug("Result of Query: size="+result.getSize());

            // Parse the current value
            LOG.debug("CommunityFileMap: update... step #1");
            
            XMLResource resultRes = (XMLResource) result.getResource(0);
            LOG.debug("CommunityFileMap: Attribute to read is: " + (selectExpression.substring(selectExpression.lastIndexOf("@") + 1)));
            Node attr = resultRes.getContentAsDOM().getFirstChild().getAttributes()
            	.getNamedItem((selectExpression.substring(selectExpression.lastIndexOf("@") + 1)));
            LOG.debug("CommunityFileMap: update... step #2: res type:"+ resultRes.RESOURCE_TYPE);
            if (resultRes==null){ LOG.debug("resultRes==null!!");}
            if (attr==null){ LOG.debug("attr==null!!");}
            int currentValue = 0;
            try {
                currentValue = Integer.parseInt(attr.getNodeValue());
            } catch (Exception e) { // Considering any exception type here
                LOG.error(e);
                LOG.error("The attr is :"+attr.toString());
            }
            LOG.debug("CommunityFileMap: update... step #3");
            // Update the XML in the database using XUpdate
            String xUpdateBlob = XUpdateHelper.update(selectExpression, String
                    .valueOf(currentValue + count));
            // Do the update
            LOG.debug("CommunityFileMap: update... step #4");
            long numUpdates = xupService
                    .updateResource(resourceId, xUpdateBlob);
            LOG.debug("CommunityFileMap: update... step #5");
            // Log errors if update failed
            if (numUpdates < 1)
                LOG
                        .error("CommunityFileMap XUpdate failed to update a counter with select expression: "
                                + selectExpression);

        } catch (XMLDBException e) {
            LOG.error("CommunityFileMap Error updating a counter in the '"
                    + communityName + "' community file map.", e);
        }
    }
}