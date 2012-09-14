package up2p.core;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import up2p.repository.ResourceEntry;
import up2p.repository.ResourceList;
import up2p.search.SearchMetricListener;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;

/**
 * Adapts the underlying peer-to-peer network to U-P2P by providing search,
 * download and publish capabilities. Implementations must handle the methods
 * defined in this interface in a way that is appropriate for their type of
 * network. For example, networks with centralized servers will have to upload
 * resources when the <code>publish()</code> method is invoked.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface NetworkAdapter {
    /**
     * The separator used when identifying adapters by their provider class and
     * version. Class and version are combined using the sperator to create a
     * unique string that identifies the adapter provider.
     */
    public static final String SEPARATOR = "-^^-";

    /**
     * Adds a listener to receive search reponses.
     * 
     * @param listener search response listener
     */
    public void addSearchResponseListener(SearchResponseListener listener);
    
    /**
     * Adds a listener to receive search metric data.
     * 
     * @param listener search metric listener
     */
    public void addSearchMetricListener(SearchMetricListener listener);
    
    /**
     * Removes a listener for search metric data.
     * 
     * @param listener listener to remove
     * @return <code>true</code> if the listener was removed,
     * <code>false</code> otherwise
     */
    public boolean removeSearchMetricListener(SearchMetricListener listener);

    /**
     * Returns the info for this adapter.
     * 
     * @return the network adapter info for this adapter
     */
    public NetworkAdapterInfo getNetworkAdapterInfo();

    /**
     * Returns a property value.
     * 
     * @param propertyName the name of the property to retrieve
     * @return the property value or <code>null</code> if not found
     */
    public String getProperty(String propertyName);

    /**
     * Returns a list of available property names.
     * 
     * @return a list of <code>String</code> property names
     */
    public Enumeration<String> getPropertyNames();

    /**
     * Returns whether messages are sent asychronously by this adapter and
     * therefore, listeners should be registered.
     * 
     * @return <code>true</code> if this adapter searches and retrieves files
     * asynchronously, <code>false</code> otherwise
     */
    public boolean isAsynchronous();

    /**
     * Publishes (or uploads/shares) a single resource to the network and
     * supports buffering of the resource so it can be published as a batch.
     * This method does not store the resource in the local repository.
     * 
     * @param resourceEntry information on the resource to publish
     * @param buffer if <code>true</code> then the resources will be held
     * until <code>publishFlush()</code> is invoked, if <code>false</code>
     * then the resource will be published immediately
     * @throws NetworkAdapterException if an error occurs in publishing the
     * resource
     */
    public void publish(ResourceEntry resourceEntry, boolean buffer)
            throws NetworkAdapterException;

    /**
     * Publishes (or uploads/shares) a set of resources on the network and
     * performs any implementation-specific tasks needed for this operation.
     * This method does not store the resource in the local repository.
     * 
     * @param resourceList list of resources to publish
     * @throws NetworkAdapterException if an error occurs in publishing the
     * resources
     */
    public void publishAll(ResourceList resourceList)
            throws NetworkAdapterException;

    /**
     * Flushes the publishing buffer and forces all buffered resources to be
     * published to the network. This allows batch publishing of resources for
     * when a user logs on to a network (instead of many individual
     * transactions).
     * 
     * @throws NetworkAdapterException if an error occurs in publishing the
     * resources
     */
    public void publishFlush() throws NetworkAdapterException;

    /**
     * Notifies the network that a resource is no longer available (unpublish or
     * unshare).
     * 
     * @param resourceEntry information on the resource to remove
     * @throws NetworkAdapterException if an error occurs in removing the
     * resource
     */
    public void remove(ResourceEntry resourceEntry)
            throws NetworkAdapterException;

    /**
     * Notifies the network that all of the given resources are no longer
     * available (unpublish/unshare).
     * 
     * @param resourceList list of resources to make unavailable, local file
     * location entries can be empty
     * @throws NetworkAdapterException if an error occurs in removing the
     * resources
     */
    public void removeAll(ResourceList resourceList)
            throws NetworkAdapterException;

    /**
     * Removes a listener for search responses.
     * 
     * @param listener listener to remove
     * @return <code>true</code> if the listener was removed,
     * <code>false</code> otherwise
     */
    public boolean removeSearchResponseListener(SearchResponseListener listener);

    /*
     * Downloads a given resource from the peer network and saves it to a local
     * file. Attachments are not processed or handled by this method.
     * 
     * @param response the search result containing possible download locations
     * for the resource to be downloaded
     * @param downloadDirectory directory on the local disk where the downloaded
     * file should be saved
     * @return downloaded resource file
     * @throws NetworkAdapterException if an error occurs in retrieving the file
     * from the network
     * /
    public File retrieve(SearchResponse response, File downloadDirectory)
            throws NetworkAdapterException;
*/
    /* *
     * Downloads a single attachment from the peer network and saves it to a
     * local file.
     * 
     * @param attachmentURL link to the attachment to download given in a format
     * specific to the P2P network protocol (e.g. HTTP)
     * @param downloadFile local file where the attachment is to be downloaded
     * @throws NetworkAdapterException if an error occurs in retrieving the file
     * from the network
     * /
    public void retrieveAttachment(String attachmentURL, File downloadFile)
            throws NetworkAdapterException;
*/
    /**
     * Performs a search using a generic query format. Zero responses will be
     * returned if the adapter uses asynchronous messages and returns results
     * through a listener.
     * 
     * @param communityId id of the community within which the search will be
     * executed
     * @param query the search to perform
     * @param maxTimeout the maximum number of seconds to wait for results,
     * after which the method should return
     * @return a list of zero or more responses
     * @throws NetworkAdapterException if an error occurs in searching the
     * network
     */
    public SearchResponse[] searchNetwork(String communityId,
            SearchQuery query, long maxTimeout) throws NetworkAdapterException;

    /**
     * Associates this adapter with a given community. Network Adapter
     * implementations may ignore this association if they serve more than one
     * community.
     * 
     * @param communityId id of the community associated with this adapter
     */
    public void setCommunity(String communityId);

    /**
     * Sets the info for this adapter.
     * 
     * @param netAdapterInfo info for this adapter
     */
    public void setNetworkAdapterInfo(NetworkAdapterInfo netAdapterInfo);

    /**
     * Sets all the properties of the adapter.
     * 
     * @param props a table of properties where the keys and values are both
     * <code>String</code> s
     */
    public void setProperties(Hashtable<String,String> props);

    /**
     * Sets a property value.
     * 
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     */
    public void setProperty(String propertyName, String propertyValue);

    /**
     * Sets the WebAdapter used by this adapter.
     * 
     * @param adapter U-P2P client web adapter
     */
    public void setWebAdapter(Core2Network adapter);

    /**
     * Shuts down the peer-to-peer client.
     */
    public void shutdown();
}