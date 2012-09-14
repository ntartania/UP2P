package up2p.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xindice.client.xmldb.services.DatabaseInstanceManager;
import org.apache.xml.serialize.DOMSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import up2p.core.CommunityNotFoundException;
import up2p.core.Core2Repository;
import up2p.core.DuplicateResourceException;
import up2p.core.ResourceNotFoundException;
import up2p.xml.TransformerHelper;
import up2p.xml.Util;

/**
 * Default implementation of a local XML repository for shared resources. The
 * dbXML API is used to access a native XML database whose URL location is
 * provided by the <code>DatabaseAdapter</code>.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DefaultRepository implements Repository {
    /** Serializer used for dumping database. */
    private static DOMSerializer serializer = Util.getDOMSerializer(System.out);

    /** The adapter that handles the database. */
    protected DatabaseAdapter dbAdapter;

    /** Holds references to query services for collections in the database. */
    private Map<String,XPathQueryService> queryServices;
    
    /** Logger used by the repository. */
    private Logger LOG;

    /** Id of the Network Adapter Community. *
    private String networkAdapterCommunityId;*/

    
    /**
     * Empty constructor for instantiating the repository without configuration.
     */
    public DefaultRepository() {
        // configured through configureRepository
    }

    /*
     * @see up2p.repository.Repository#configureRepository(up2p.core.WebAdapter)
     */
    public void configureRepository(Core2Repository webAdapter, DatabaseAdapter dadapter) {
        if (LOG == null)
            LOG = Logger.getLogger(Repository.LOGGER);

        // log initial message
        LOG.info(new java.util.Date().toString()
                + " DefaultRepository configured.");

        // can be null if dbAdapter is configured using setDbAdapter()
        if (webAdapter != null)
            dbAdapter = dadapter;
        
        queryServices = new HashMap<String,XPathQueryService>();
    }
    
    
    /**
     * Retrieves a configuration value stored in the database (such as the root
     * community id). Limited configuration parameters are stored in the
     * database and retrieved for use when U-P2P is running. Other config
     * parameters are stored in a text file in
     * <code>/up2p/WEB-INF/classes</code> called
     * <code>WebAdapter.properties</code>.
     * 
     * <p>
     * Supported parameter names: up2p.root.id, up2p.networkAdapter.id
     * 
     * @param paramName name of the parameter to retrieve (e.g. up2p.root.id)
     * @return the parameter value or <code>null</code> if not found or an
     * error occurs
     */
    public String getConfigurationValue(String paramName) {
    	
        try {
            
            ResourceSet result = queryConfigCollection("up2pConfig",
                    "/up2p/configuration/setting[@name='" + paramName
                            + "']/@value");
            if (result.getSize() == 0) {
                LOG
                        .error("ResourceManager Error getting configuration parameter "
                                + paramName
                                + " from the database. No query results.");
                return null;
            }
            // result is an attribute
            XMLResource firstResult = (XMLResource) result.getResource(0);
            Node resultDom = firstResult.getContentAsDOM().getFirstChild();
            
            // return the value
            //LOG.debug("ResourceManager got value:"+resultAttribute.getNodeValue());
            return resultDom.getAttributes().getNamedItem("value").getNodeValue();
        } catch (XMLDBException e) {
            LOG.error("ResourceManager Error getting configuration parameter "
                    + paramName + " from the database.", e);
        }
        catch(Exception e){
        	LOG.error("ResourceManager Error getting configuration parameter "
                    + paramName + " from the database.", e);
        	
        }
        return null;
    }
    /**
     * Returns the value at the given XPath if found in the given resource.
     * 
     * @param resourceId id of the resource to query
     * @param communityId id of the community where the resource is stored
     * @param xPathLocation XPath location within the community
     * @return the value at the location or <code>null</code> if not found
     */
    public List<String> lookupXPathLocation(String resourceId, String communityId,
            String xPathLocation) throws CommunityNotFoundException, ResourceNotFoundException {
    	
    	//LOG.debug("Entering lookupXPath------------");
    	
            Collection comcol;
			try {
				comcol = getCommunityCollection(communityId);
			} catch (XMLDBException e1) { //some exception
				throw new CommunityNotFoundException(e1.getMessage());
			}
			
			if (comcol==null) //community not found
				throw new CommunityNotFoundException("Community "+ communityId+ "not found locally.");

			try {
				Resource res = comcol.getResource(resourceId);
				//LOG.debug("DefRepository::lookupXpath: resource:"+res.getContent());
				if (res==null) {
					throw new ResourceNotFoundException();
				}
			} catch (XMLDBException e) { //resource not found : no, this doesn't cause an exception
				throw new ResourceNotFoundException(e.getMessage());
			}
			
			 //if we get here, community and resource both exist
        	XPathQueryService qService = getQueryService(comcol);
        	List<String> toreturn = new ArrayList<String>();
        	if (xPathLocation.equals(".")) { //checking to see if the resource exists at all   	
        		toreturn.add(resourceId);	
        		return toreturn;
        	}
        	
        	// Store the attribute string if an attribute was requested
        	String attrName = null;
        	
        	String [] attr = xPathLocation.split("@");
        	if (attr.length > 1) {
        		attrName = attr[attr.length - 1];
        	}
       
        	try { 
            ResourceSet result = qService.queryResource(resourceId,
                    xPathLocation);
            
            // check if there are any results
            if (result.getSize() > 0) {
        		//list all the results
            	for (int i= 0; i<result.getSize();i++)	{
        			toreturn.add(getXMLResourceValue((XMLResource) result.getResource(i), attrName));
            	}
            }
         
            return toreturn;
            
        } catch (XMLDBException e) { 
            LOG.error("DefRepository getXPathLocation Error getting XPath "
                    + xPathLocation + " in resource id " + resourceId
                    + " for community id " + communityId, e);
            return toreturn;
        }
        
    }
    
    /**
     * Returns a list of all the local resource id's within a given community.
     * 
     * @param comId	The community to browse.
     * @return	A list of resource id's in the given community
     */
    public List<String> browseCommunity(String communityId) 
    		throws CommunityNotFoundException {
    	List<String> communityIds = null;
    	
    	Collection comcol;
		try {
			comcol = getCommunityCollection(communityId);
		} catch (XMLDBException e) { // Some exception
			throw new CommunityNotFoundException(e.getMessage());
		}
			
		if (comcol==null) // Community not found
			throw new CommunityNotFoundException("Community "+ communityId+ "not found locally.");
    	
		try {
			communityIds = Arrays.asList(comcol.listResources());
		} catch (XMLDBException e) { // Some exception
			throw new CommunityNotFoundException(e.getMessage());
		}
		
		return communityIds;
    }
    
    /**
     * Gets a collection from the database for a given community.
     * 
     * @param communityId id of the community
     * @return collection containing all resources shared in the community
     * @throws XMLDBException if the collection cannot be found
     */
    private Collection getCommunityCollection(String communityId)
            throws XMLDBException {
        return DatabaseManager.getCollection(dbAdapter.getCommunityUrl() + "/"
                + communityId, "admin", null);
    }
    
    /**Query the DB
     * 
     * @param id : the id of the document to query (should be "up2pConfig" here)
     * @param query : the query to be sent
     * 
     * returns: a resultSet , the result of the query 
     */
     public ResourceSet queryConfigCollection(String id, String query) throws XMLDBException {
    	 
    	 //LOG.debug("Core2Repository::Config Query: "+query+" Community: config");
    	 ResourceSet results = null;
    	Collection configCollection = DatabaseManager.getCollection(
               dbAdapter.getConfigUrl(), "admin", null);
       XPathQueryService qService = getQueryService(configCollection);
    	  // query all resources in the Community collection
    	  results = qService.queryResource(id,query);
    	 /*if (results==null) LOG.debug("results=null");
    	 else LOG.debug("results not null");*/
    	 return results;
     }

    /**
     * Sets the Logger name to use for all log statements. If this is not
     * set the default Repository.LOGGER will be setup when configure()
     * is called.
     * 
     * @param loggerName name of the logger to use
     */
    public void setLogger(String loggerName) {
        LOG = Logger.getLogger(loggerName);
    }

    /**
     * Retrieves the XPathQueryService for a collection.
     * 
     * @param col collection on which the service acts
     * @return query service or <code>null</code> if there is an error getting
     * the service
     */
    private XPathQueryService getQueryService(Collection col) {
        String colName = null;
        try {
            colName = col.getName();
        } catch (XMLDBException e) {
            LOG.error(
                    "ResourceManager getQueryService Error getting the name of a"
                            + " collection.", e);
            return null;
        }

        // check the cache of service instances
        if (queryServices.containsKey(colName)) {
            return (XPathQueryService) queryServices.get(colName);
        }
        // not stored so initialize the service
        XPathQueryService qService = null;
        try {
            qService = (XPathQueryService) col.getService("XPathQueryService",
                    "1.0");
        } catch (XMLDBException e) {
            LOG.error("ResourceManager getQueryService Error getting the XPath"
                    + " Query Service for collection " + colName, e);
        }
        return qService;
    }
    
    /**
     * Returns the value of the first node in the XML resource.
     * 
     * @param res XML resources to process
     * @return attribute or text node's value or first text node child of an
     * element if available
     * @throws XMLDBException if an error occurs when retrieving the value
     */
    private String getXMLResourceValue(XMLResource res, String attrName) throws XMLDBException {
        Node domNode = res.getContentAsDOM().getFirstChild();

        if (domNode.getFirstChild() != null && domNode.getFirstChild().getNodeType() == Node.TEXT_NODE) {
        	// Node has a text first child
            return domNode.getFirstChild().getNodeValue();
        }
        
    	if(attrName != null) {
    		// Node is an attribute result
    		return domNode.getAttributes().getNamedItem(attrName).getNodeValue();
    	} else {
    		// Node is a text node
    		return domNode.getNodeValue();
    	}

    }
    /*
     * @see up2p.repository.Repository#createCommunity(String)
     */
    public void createCommunity(String communityId) {
        // create the collection Url
        String collectionUrl = "/" + dbAdapter.getCommunityCollection() + "/"
                + communityId;
        try {
        	LOG.info("Attempting creation of community collection: " + communityId);
            // create the collection
            if (dbAdapter.isCollection(collectionUrl)) {
                LOG.info("Community collection " + collectionUrl
                        + " already exists in the database.");
            } else {
                dbAdapter.createCollection(communityId, true);
                LOG.info("Created community collection: " + collectionUrl);
            }
        } catch (XMLDBException e) {
            LOG.error("Error occured in createCommunity.\n\tCommunityUrl: " +collectionUrl, e);
        }
    }

    /**
     * Displays the database contents. TODO Is this used?
     */
    public void dumpDatabase() {
        try {
            System.out.println("Dumping database:");
            Collection root = DatabaseManager.getCollection(dbAdapter
                    .getDatabaseRootUrl(), "admin", null);
            Util.displayCollection(root, System.out, 0, serializer);
        } catch (XMLDBException e) {
            e.printStackTrace();
        }
    }

    /*
     * @see up2p.repository.Repository#getDbAdapter()
     */
    public DatabaseAdapter getDbAdapter() {
        return dbAdapter;
    }

    /*
     * @see up2p.repository.Repository#isValid(String,String)
     */
    public boolean isValid(String communityId, String resourceId) {
        // try to get collection
        Collection col = null;
        try {
            col = DatabaseManager.getCollection(dbAdapter.getCommunityUrl()
                    + "/" + communityId, "admin", null);
            if (col == null)
                return false;
        } catch (XMLDBException e) {
            return false;
        }

        try {
            org.xmldb.api.base.Resource r = col.getResource(resourceId);
            if (r == null)
                return false;
            return true;
        } catch (XMLDBException e) {
            return false;
        }
    }

    /*
     * @see up2p.repository.Repository#remove(String, String)
     */
    public void remove(String resourceId, String communityId) throws CommunityNotFoundException, ResourceNotFoundException {
        LOG.debug("Received remove request for resource id " + resourceId
                + " in community id " + communityId + ".");

        

        /*/ prevent removal of network adapter community
        if (resourceId.equals(networkAdapterCommunityId))
            throw new CommunityNotFoundException(
                    "The Network Adapter Community cannot be removed.");
		*/
        // try to get collection
        String collectionUrl = dbAdapter.getCommunityUrl() + "/" + communityId;
        Collection col = null;
        try {
            col = DatabaseManager.getCollection(collectionUrl, "admin", null);
            if (col == null)
                throw new CommunityNotFoundException("Community id "
                        + communityId + " does not exist.");
        } catch (XMLDBException e) {
            LOG.error("Error occured in remove.", e);
            throw new CommunityNotFoundException("Error removing resource id "
                    + resourceId + "  from community id " + communityId + ".");
        }

        // try to get the resource
        try {
            org.xmldb.api.base.Resource r = col.getResource(resourceId);
            if (r == null)
                throw new ResourceNotFoundException("Resource id " + resourceId
                        + " not found in community " + communityId + ".");
            LOG.info("Removing resource " + resourceId + " from community id "
                    + communityId + ".");
            col.removeResource(r);
        } catch (XMLDBException e) {
            LOG.error("Error occured in remove.", e);
            throw new ResourceNotFoundException("Error removing resource id "
                    + resourceId + " not found in community id " + communityId
                    + ".");
        }
    }

    /*
     * @see up2p.repository.Repository#removeCommunity(String)
     */
    public void removeCommunity(String communityId) throws CommunityNotFoundException {
        // try to get collection
        String collectionUrl = dbAdapter.getCommunityUrl();

        try {
            Collection communityRoot = DatabaseManager.getCollection(
                    collectionUrl, "admin", null);
            if (communityRoot == null) {
                LOG
                        .error("Error occured in removeCommunity. The community root collection is not accessible.");
                throw new CommunityNotFoundException(
                        "Failed to remove community id " + communityId + ".");
            }
            CollectionManagementService mgtService = (CollectionManagementService) communityRoot
                    .getService("CollectionManagementService", "1.0");
            // remove the collection
            mgtService.removeCollection(communityId);
            LOG.info("Removed community id " + communityId + ".");
        } catch (XMLDBException e) {
            LOG.info("Failed to remove community id " + communityId + ".");
        }
    }

    /*
     * @see up2p.repository.Repository#search(String, SearchQuery)
     * 
     */
    public List<XMLResource> search(String communityId, String queryString, int maxResults) throws CommunityNotFoundException {
        LOG.debug("Received search request for community id " + communityId
                + ". Query: " + queryString);

        List<XMLResource> searchResults = new ArrayList<XMLResource>();

        // Try to get the collection
        Collection col = null;
        try {
            col = DatabaseManager.getCollection(dbAdapter.getCommunityUrl()
                    + "/" + communityId, "admin", null);
            if (col == null) {
                LOG.info("Search request failed. Community id " + communityId
                        + " not found.");
                throw new CommunityNotFoundException("Community id "
                        + communityId + " not found.");
            }
        } catch (XMLDBException e) {
            throw new CommunityNotFoundException("Community id " + communityId
                    + " not found.");
        }

        try {
            XPathQueryService service = (XPathQueryService) col.getService(
                    "XPathQueryService", "1.0");

            // Build the XPath query expression and execute the search
            if(queryString.equals(".")){
            	LOG.info("Identified 'browse' search: ");
            	queryString = "/*";
            }
            	
        	LOG.info("Executing search query: " + queryString);
        	ResourceSet result = service.query(queryString);


        	// log empty search result
        	if (result.getSize() == 0)
        		LOG.info("Search query returned no results.");
        	else 
        		LOG.info("Search query returned "+result.getSize()+ " results.");

        	// Go through results and add them to the returned list
        	int resultCounter = 0;
        	
        	ResourceIterator i = result.getIterator();
        	while (i.hasMoreResources()
        			&& resultCounter < maxResults) {
        		org.xmldb.api.base.Resource r = i.nextResource();
        		if (r.getResourceType().equals("XMLResource")) {
        			searchResults.add((XMLResource)r);
        			resultCounter++;
        		}
        	}
        	
        	// Keep a note in the log if the full set of resources was trimmed
        	if (i.hasMoreResources())
        		LOG
        		.debug("Returning limited number of results. Real result count: "
        				+ result.getSize()
        				+ " Max result limit: "
        				+ maxResults);
        } catch (XMLDBException e) {
        	LOG.error("Error occured during search.", e);
        }
        
        return searchResults;
    }

    /**
     * Set the DatabaseAdapter used by this Repository. This method is
     * used the Generic Central Server because it doesn't start a WebAdapter
     * but starts an XML database and sets the dbAdapter directly.
     * 
     * @param dbAdapter XML database adapter
     */
    public void setDbAdapter(DatabaseAdapter dbAdapter) {
        this.dbAdapter = dbAdapter;
    }

    /*
     * @see up2p.repository.Repository#shutdown()
     */
    public void shutdown() {
    	// Database shutdown should be handled gracefully by Xindice
    	// without further intervention
    }

    /*
     * @see up2p.repository.Repository#store(Node, String, String)
     */
    public void store(Node xml, String resourceId, String communityId) throws DuplicateResourceException, CommunityNotFoundException {
    	LOG.info("Repository:  store(): XML " + xml );
    	
        try {
            // try to get collection
            String collectionUrl = dbAdapter.getCommunityUrl() + "/"
                    + communityId;
            Collection col = null;
            try {
                col = DatabaseManager.getCollection(collectionUrl, "admin",
                        null);
                if (col == null) {
                    LOG.debug("Collection for community id " + communityId
                            + " not found, creating in community collection.");
                    // collection does not exist, get root collection and create
                    col = dbAdapter.createCollection(communityId, true);
                }
            } catch (XMLDBException e) {
                LOG.error("Error occured in method store(Node,String,String).",
                        e);
                throw new CommunityNotFoundException("Store operation failed. "
                        + e.getMessage() + " error code=" + e.errorCode
                        + " vendor code=" + e.vendorErrorCode);
            }

            // check if the id is in use
            if (col.getResource(resourceId) != null) {
                LOG.info("Store ignored because document " + resourceId
                        + " already exists in community id " + communityId
                        + ".");
                throw new DuplicateResourceException(resourceId, communityId);
            } else {
                // not in use so create and store document
                XMLResource document = (XMLResource) col.createResource(
                        resourceId, "XMLResource");
                LOG.info(">>>>>>**********XML content: " + xml);
                document.setContentAsDOM(xml);
                col.storeResource(document);
                LOG.info("Stored document " + document.getId()
                        + " in community id " + communityId + ".");
            }
        } catch (XMLDBException e) {
            LOG.error("Error occured in store using a DOM.", e);
        }
    }
    
    /*
     * @see up2p.repository.Repository#getResource(java.lang.String, java.lang.String)
     */
    public XMLResource getResource(String communityId, String resourceId) 
    	throws CommunityNotFoundException, ResourceNotFoundException {
    	
    	XMLResource resource = null;
    	
    	// Get the resource from the database
		try {
			Collection col =
				DatabaseManager.getCollection(dbAdapter.getCommunityUrl() + "/" + communityId);
			resource = ((XMLResource) col.getResource(resourceId));
		} catch (XMLDBException e) {
			if(e.vendorErrorCode == ErrorCodes.NO_SUCH_DATABASE) {
				throw new CommunityNotFoundException("Community " + communityId + " could not be found.");
			} else {
				throw new ResourceNotFoundException("Resource " + resourceId + "(Community: " + communityId + ") could not be found.");
			}
		}
		
		return resource;
    }

    /*
     * @see up2p.repository.Repository#store(java.lang.String, java.lang.String,
     * java.io.File)
     */
    public void store(String resourceId, String communityId, File resourceFile) throws DuplicateResourceException, CommunityNotFoundException, ResourceNotFoundException {

    	// check if contents are readable
    	if (!resourceFile.canRead()) {
            LOG.info("Attempt to store a file failed. File "
                    + resourceFile.getAbsolutePath() + " not found.");
            throw new ResourceNotFoundException("Unable to read resource from "
                    + "file " + resourceFile.getAbsolutePath() + ".");
        }

        try {
            // parse in the document as XML
            Document resourceXML = TransformerHelper
            		.parseXML(new FileInputStream(resourceFile));
            LOG.info("Def repository > storing:  " + resourceFile.getAbsolutePath());
            if (resourceXML==null){
            	LOG.error("ERROR: resourceXML is null.");
            }
            store(resourceXML.getDocumentElement(), resourceId, communityId);
        } catch (IOException e) {
            LOG.error("Error occured in store(File).", e);
            throw new ResourceNotFoundException(
                    "Store operation failed because the resource file could not be read.");
        } catch (SAXException e) {
            LOG.error("Error occured in store(File).", e);
            throw new ResourceNotFoundException(
                    "Store operation failed because there was an error parsing the XML resource file.");
        }
    }

	//@Override
	public boolean communityHasResource(String comId, String rid) {
		String collectionUrl = dbAdapter.getCommunityUrl() + "/" + comId;
		Collection col = null;
		try {
			col = DatabaseManager.getCollection(collectionUrl, "admin",null);
		if (col.getResource(rid)!=null)
			return true;
		else
			return false;
		}
		catch(Exception e){
		LOG.error("DefRepository: communityHasResource: error "+ e);
		return false;
		}
					
	}

}