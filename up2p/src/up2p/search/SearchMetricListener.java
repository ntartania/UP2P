package up2p.search;

import java.util.List;

/**
 * Classes that implement the SearchMetricListener interface
 * are responsible for receiving search metric information from the
 * JTellaAdapter level and propagating this information in to the tuple
 * space to be processed by other agents.
 * 
 * @author Alexander Craig
 * @version June 10th, 2010
 */
public interface SearchMetricListener {

	/**
	 * Receives a list of resource Id's that a network peer is hosting
	 * in a given community. The list of resource id's should be processed
	 * and added to the tuple space so that other agents can perform
	 * trust metric calculations.
	 * 
	 * @param peerIdentifier	The IP/port of the peer hosting this set of resources
	 * @param communityId	The community id of the received resource list
	 * @param localResIds	The list of resources the peer is hosting in the given community
	 */
	public void receiveNetworkResourceList(String peerIdentifier, 
			String communityId, List<String> netResIds);
	
	/**
	 * Receives a raw metric value. This metric value should be propagated directly
	 * to the tuple space (used for metrics which do not require any further processing on
	 * the receiving node's side such as network distance or network connectivity of the 
	 * network peer)
	 * 
	 * @param peerIdentifier	The IP/port of the peer
	 * @param communityId	The community id this metric is valid for
	 * @param metricName	The name of the metric
	 * @param metricValue		The value of the metric
	 */
	public void receiveMetricValue(String peerIdentifier, String communityId,
			String metricName, String metricValue);
}
