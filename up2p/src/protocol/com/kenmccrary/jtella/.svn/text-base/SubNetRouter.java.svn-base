package protocol.com.kenmccrary.jtella;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * SubNetRouter is an extension of the standard JTella Router to support
 * custom U-P2P SUBNETQUERY messages.
 * 
 * @author Alexander Craig
 * @version July 2010
 */
public class SubNetRouter extends Router {
	
	/** A list of all sub-network IDs this router is currently serving */
	Set<String> servedSubNetIds;
	
	/**
	 *  Generates a new SubNetRouter
	 *
	 *  @param connectionList The list of connections in the system
	 *  @param connectionData Local connection information used primarily for sending 
	 *  									   information in Pong messages.
	 *  @param hostCache Cache of available hosts in the system
	 */
	SubNetRouter(ConnectionList connectionList, ConnectionData connectionData, HostCache hostCache) {
		super(connectionList, connectionData, hostCache);
		servedSubNetIds = new TreeSet<String>();
	}
	
	/**
	 * Calls one of the message handler functions depending on the type
	 * of message received. This function attempts to handle any SUBNETQUERY
	 * messages, then hands off the dispatch to the standard Router code.
	 * This should only be called for messages once it has already been verified 
	 * that the message GUID has not been seen by this servent previously. 
	 * 
	 * @param msg	The message to dispatch.
	 */
	protected void dispatchNewMessage(RouteMessage msg) {
		if (msg.getMessage().getType() == SubNetSearchMessage.SUBNETQUERY) {
			if(subNetIsServed(((SubNetSearchMessage)msg.getMessage()).getSubNetIdentifier())) {
				routeSubNetQueryMessage(msg);
			}
		} else {
			super.dispatchNewMessage(msg);
		}
	}
	
	/**
	 *  Routes SUBNETQUERY messages -
	 *  RULES: Route SUBNETQUERY messages to all connections  except the originator
	 *  if the specified sub-network is served by this node.
	 */
	protected void routeSubNetQueryMessage(RouteMessage m) {

		if (m.getConnection().getStatus() != NodeConnection.STATUS_OK || queryRouteTable.containsGUID(m.getMessage().getGUID())) {
			// Originating connection does not exist, or has already been seen
			// Drop the message and do nothing
			return;
		}

		if(subNetIsServed(((SubNetSearchMessage)m.getMessage()).getSubNetIdentifier())) {
			// Make a local connection list to avoid concurrency L
			LinkedList<NodeConnection> listcopy = connectionList.getList();
			ListIterator<NodeConnection> iterator = listcopy.listIterator(0);
	
			prepareMessage(m.getMessage());
	
			while (iterator.hasNext()) {
				NodeConnection connection = (NodeConnection)iterator.next();
				if (!connection.equals(m.getConnection())
					&& connection.getStatus() == NodeConnection.STATUS_OK) {
					routerSend(connection, m.getMessage());
				}
			}
	
			// History of Queries
			queryRouteTable.put(m.getMessage().getGUID(), m.getConnection());
	
			// inform any search listener
			if (0 != searchReceivers.size()) {
				fireSearchMessage((SearchMessage)m.getMessage());
			} else {
				LOG.debug("no search receivers to fire the search message!");
			}
		} else {
			LOG.debug("Router dropped message for community (unserved sub-network): " 
					+ ((SubNetSearchMessage)m.getMessage()).getSubNetIdentifier()); 
		}
	}
	
	/**
	 * Checks if a specified sub-network identifier is served by this node
	 * @param subNetId	The sub-network identifier to check against
	 * @return	True if the sub-network is served, false if not
	 */
	protected boolean subNetIsServed(String subNetId) {
		return servedSubNetIds.contains(subNetId);
	}
	
	/**
	 * Adds a sub-network ID to the set of served IDs for this router.
	 * @param subNetId	The sub-network ID to serve
	 */
	public void addServedSubNet(String subNetId) {
		servedSubNetIds.add(subNetId);
	}
	
	/**
	 * Removes a sub-network ID from the set of served IDs for this router.
	 * @param subNetId	The sub-network ID to stop serving
	 * @return true if the specified sub-network was being served, false if not
	 */
	public boolean removeServedSubNet(String subNetId) {
		return servedSubNetIds.remove(subNetId);
	}
	
	/**
	 * Replaces the list of served sub-networks with the specified set.
	 * @param subNetIds	The new set of served sub-networks.
	 */
	public void setServedSubNets(Set<String> subNetIds) {
		LOG.debug("Updated sub-network list.");
		servedSubNetIds = subNetIds;
	}
}
