package up2p.core;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import up2p.search.SearchMetricListener;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.servlet.HttpParams;
import up2p.util.FileUtil;

/**
 * Skeleton class that should be extended to implement a
 * <code>NetworkAdapter</code>. Provides basic support for setting/getting
 * properties, firing search response events and retrieving files and
 * attachments.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class BasePeerNetworkAdapter implements NetworkAdapter {
    /** Protocol that can be handled by this adapter. */
    public static final String HTTP_PROTOCOL = "http";

	private static final String LOGGER = "up2p.peer.jtella";

    /** Logger used by this class. */
    private static Logger LOG = Logger.getLogger(LOGGER);

    /** The WebAdapter used by this peer client. */
    protected Core2Network adapter;

    /** Community ID associated with this adapter. */
    protected String associatedCommunityId;

    /** Network Adapter info for this adapter. */
    protected NetworkAdapterInfo networkAdapterInfo;

    /** Properties of the adapter. */
    protected Hashtable<String,String> properties;

    /** Sets of search response listeners. */
    protected Set<SearchResponseListener> searchResponseListeners;
    
    /** Sets of search response listeners. */
    protected Set<SearchMetricListener> searchMetricListeners;

    /**
     * Construct an empty adapter.
     */
    public BasePeerNetworkAdapter() {
        properties = new Hashtable<String,String>();
        searchResponseListeners = new HashSet<SearchResponseListener>();
        searchMetricListeners = new HashSet<SearchMetricListener>();
    }

    /*
     * @see up2p.core.NetworkAdapter#addSearchResponseListener(up2p.search.SearchResponseListener)
     */
    public void addSearchResponseListener(SearchResponseListener listener) {
        searchResponseListeners.add(listener);
    }
    
    /*
     * @see up2p.core.NetworkAdapter#addSearchMetricListener(up2p.search.SearchMetricListener)
     */
    public void addSearchMetricListener(SearchMetricListener listener) {
        searchMetricListeners.add(listener);
    }

    /**
     * Fires the given search responses to all registered search listeners.
     * 
     * @param responses response(s) to send to the listeners
     */
    protected void fireSearchResponse(SearchResponse[] responses) {
        Iterator<SearchResponseListener> i = searchResponseListeners.iterator();
        int cnt =0;
        while (i.hasNext()) {
            SearchResponseListener listener = i.next();
            listener.receiveSearchResponse(responses);
            cnt++;
        }
        LOG.debug("BasePeerNetwork Adapter: Sent search " + responses.length + 
        		" responses to "+ String.valueOf(cnt)+" listeners");
    }
    
    /**
     * Fires the given list of network resources to all registered search metric listeners.
     * @param peerIdentifier	The IP/Port of the network peer
     * @param communityId	The community id of the resource list
     * @param netResIds	The list of resource id's in the specified community that the network
     * 									peer is hosting.
     */
    protected void fireNetworkResourceList(String peerIdentifier, String communityId, List<String> netResIds) {
        Iterator<SearchMetricListener> i = searchMetricListeners.iterator();
        int cnt =0;
        while (i.hasNext()) {
            SearchMetricListener listener = i.next();
            listener.receiveNetworkResourceList(peerIdentifier, communityId, netResIds);
            cnt++;
        }
        LOG.debug("BasePeerNetwork Adapter: Sent network resource list to "+ String.valueOf(cnt)+"listeners");
    }
    
    protected void fireTrustMetric(String peerIdentifier, String communityId, String metricName, String metricValue) {
        Iterator<SearchMetricListener> i = searchMetricListeners.iterator();
        int cnt =0;
        while (i.hasNext()) {
            SearchMetricListener listener = i.next();
            listener.receiveMetricValue(peerIdentifier, communityId, metricName, metricValue);
            cnt++;
        }
        LOG.debug("BasePeerNetwork Adapter: Sent trust metric (" + metricName + ") to "+ String.valueOf(cnt)+" listeners");
    }

    /*
     * @see up2p.core.NetworkAdapter#getNetworkAdapterInfo()
     */
    public NetworkAdapterInfo getNetworkAdapterInfo() {
        return networkAdapterInfo;
    }

    /*
     * @see up2p.core.ResourceProperties#getProperty(String)
     */
    public String getProperty(String propertyName) {
        String o = properties.get(propertyName);
        return o;
        /*if (o != null)
            return (String) o;
        return null;*/
    }

    /*
     * @see up2p.core.ResourceProperties#getPropertyNames()
     */
    public Enumeration<String> getPropertyNames() {
        return properties.keys();
    }

    /*
     * @see up2p.core.NetworkAdapter#removeSearchResponseListener(up2p.search.SearchResponseListener)
     */
    public boolean removeSearchResponseListener(SearchResponseListener listener) {
        return searchResponseListeners.remove(listener);
    }
    
    /*
     * @see up2p.core.NetworkAdapter#removeSearchMetricListener(up2p.search.SearchMetricListener)
     */
    public boolean removeSearchMetricListener(SearchMetricListener listener) {
        return searchMetricListeners.remove(listener);
    }

    
   
    /*
     * @see up2p.core.NetworkAdapter#setCommunity(java.lang.String)
     */
    public void setCommunity(String communityId) {
        associatedCommunityId = communityId;
    }

    /*
     * @see up2p.core.NetworkAdapter#setNetworkAdapterInfo(up2p.core.NetworkAdapterInfo)
     */
    public void setNetworkAdapterInfo(NetworkAdapterInfo netAdapterInfo) {
        networkAdapterInfo = netAdapterInfo;
    }

    /*
     * @see up2p.core.NetworkAdapter#setProperties(Hashtable)
     */
    public void setProperties(Hashtable<String,String> props) {
        properties.putAll(props);
    }

    /*
     * @see up2p.core.NetworkAdapter#setProperty(String, String)
     */
    public void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    /*
     * @see up2p.core.NetworkAdapter#setWebAdapter(up2p.core.WebAdapter)
     */
    public void setWebAdapter(Core2Network adapter) {
        this.adapter = adapter;
    }
    /*
     * just so we can see stuff from the jsp
     */
    public Core2Network getWebAdapter() {
        return adapter;
    }

}