package up2p.repository;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

//import up2p.core.WebAdapter;
import up2p.core.CommunityNotFoundException;
import up2p.core.Core2Repository;
import up2p.core.DuplicateResourceException;
import up2p.core.ResourceNotFoundException;
//import up2p.search.SearchQuery;
import up2p.search.SearchResponse;

/**
 * Repository for storing all shared XML resources, for handling search requests
 * from the peer network and from the local user interface.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface Repository {

    /** Default implementing class for the repository. */
    public static String DEFAULT_REPOSITORY_PROVIDER = "up2p.repository.DefaultRepository";

	public static String RESOLVE_URI = "ResourceId=";

    /** Name of the logger used for logging. */
    public static final String LOGGER = "up2p.repository";

    /**
     * Starts and configures the repository implementation.
     * 
     * @param webAdapter adapter that uses the repository
     * @param dadapter the DataBaseAdapter to use 
     */
    public void configureRepository(Core2Repository webAdapter, DatabaseAdapter dadapter);

    /**
     * Creates a collection in the database for a given community.
     * 
     * @param communityId id of the community collection to create
     */
    public void createCommunity(String communityId);

    /**
     * Checks if a resource is valid and is stored in the repository.
     * 
     * @param communityId id of the community where the resource is stored
     * @param resourceId id of the stored resource
     * @return <code>true</code> if the resource is stored, <code>false</code>
     * otherwise
     */
    public boolean isValid(String communityId, String resourceId);

    /**
     * Removes a resource from the repository.
     * 
     * @param resourceId the id of the resource to remove
     * @param communityId the id of the community where the resource is stored
     * @throws CommunityNotFoundException 
     * @throws up2p.core.CommunityNotFoundException when the community is not found
     * @throws ResourceNotFoundException 
     * @throws up2p.core.ResourceNotFoundException when the resource is not found
     */
    public void remove(String resourceId, String communityId) throws CommunityNotFoundException, ResourceNotFoundException;

    /**
     * Removes a community collection from the repository.
     * 
     * @param communityId community to remove
     * @throws CommunityNotFoundException 
     * @throws up2p.core.CommunityNotFoundException if the community does not exist
     */
    public void removeCommunity(String communityId) throws CommunityNotFoundException;

    /**
     * Executes the given search query within a community in the repository and
     * returns a set of matching XMLResources within the given community.
     * 
     * @param communityId the id of the community to search
     * @param query the search query to execute
     * @return A list of XMLResources that match the query
     */
    public List<XMLResource> search(String communityId, String queryString, int maxResults) throws CommunityNotFoundException;

    /**
     * Shuts down the local instance of the repository.
     */
    public void shutdown();

    /**
     * Stores a file in the repository using a pre-parsed XML DOM as the content
     * and creating a collection for the community if necessary.
     * 
     * @param xml the XML DOM to store in the repository
     * @param resourceId the id of the resoruce to store
     * @param communityId the id of the community under which to store the
     * resource
     * @throws up2p.core.DuplicateResourceException when the resource is already found in the database.
     * @throws up2p.core.CommunityNotFoundException when the community is not
     * found
     */
    public void store(Node xml, String resourceId, String communityId) throws DuplicateResourceException, CommunityNotFoundException;

    /**
     * Stores a file in the repository. This method only affects the local
     * repository and does not interact with the peer network.
     * 
     * @param resourceId id of the resource to store in the repository
     * @param communityId id of the community where the resource will be stored
     * @param resourceFile the file that contains the resource
     * @throws DuplicateResourceException 
     * @throws up2p.core.ResourceNotFoundException when an error occurs in
     * reading a file
     * @throws up2p.core.CommunityNotFoundException when the community is not
     * found
     * @throws ResourceNotFoundException 
     */
    public void store(String resourceId, String communityId, File resourceFile) throws DuplicateResourceException, CommunityNotFoundException, ResourceNotFoundException;

	/**
	 * Method used to determine whether a particular resource exists in a particular community (locally stored)
	 * @param comId community Id to check for
	 * @param rid resource Id to check for
	 * @return true if the resource exists in the local repository, false otherwise
	 */
    public boolean communityHasResource(String comId, String rid);
	
    /**
     * Used to retrieve the value of a configuration parameter
     * @param paramName name of the parameter
     * @return the value of the parameter
     */
	public String getConfigurationValue(String paramName);

	/**
	 *  Method used to lookup the value(s) of an attribute in a particular resource. The attribute may be multi-valued.
	 * @param cid community Id
	 * @param rid resource Id 
	 * @param xpath path to the attribute to be looked up. It is assumed that this path matches one or several full branches of the XML tree so as to obtain a non empty list of responses. 
	 * @return a list containing zero to many values in String format. The method should return all the matching values.
	 * @throws CommunityNotFoundException
	 * @throws ResourceNotFoundException
	 */
	public List<String> lookupXPathLocation(String cid, String rid, String xpath) throws CommunityNotFoundException, ResourceNotFoundException;
	
	/**
     * Returns a list of all the local resource id's within a given community.
     * 
     * @param comId	The community to browse.
     * @return	A list of resource id's in the given community
     */
    public List<String> browseCommunity(String communityId) throws CommunityNotFoundException;
    
    /**
     * Retrieves a specific resource (XMLResource) object from the database.
     * @param communityId	The communityId of the resource to fetch
     * @param resourceId	The resourceId of the resource to fetch
     * @return	The XMLResource object for the corresponding resource
     * @throws CommunityNotFoundException	If the specified community does not exist
     * @throws ResourceNotFoundException	If the specified resource does not exist
     */
    public XMLResource getResource(String communityId, String resourceId) 
    	throws CommunityNotFoundException, ResourceNotFoundException;
}