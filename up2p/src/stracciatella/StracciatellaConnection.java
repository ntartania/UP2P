package stracciatella;


import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
//import java.util.LinkedList;

import org.apache.log4j.Logger;

import protocol.com.kenmccrary.jtella.util.Log;
//import com.kenmccrary.jtella.util.LoggingThreadGroup;

import protocol.com.dan.jtella.GetHostsFromCache;
import protocol.com.dan.jtella.ConnectedHostsListener;
import stracciatella.message.Message;
import stracciatella.message.MessageReceiver;
import stracciatella.routing.Router;
import up2p.rest.UP2PConnection;

/**
 * EDITED BY: Daniel Meyers, 2003<br>
 * The GNUTellaConnection represents a connection to the GNUTella 
 * <b>network</b>. The connection consists of one or more socket 
 * connections to servant nodes on the network.<p>
 *
 */
public class StracciatellaConnection {

	// Name of logger used
	public static final String LOGGER = "protocol.com.dan.jtella";
	// Instance of logger
	public static Logger LOG = Logger.getLogger(LOGGER);
	
	private static GUID servantId; // static because unique for this peer anyway.. and provides easier access from anywhere
	
	//private boolean shutdownFlag;
	private static ConnectionData connectionData;
	private HostCache hostCache;
	private ConnectionList connectionList;
	private Router router;
	private IncomingConnectionManager incomingConnectionManager;
	private OutgoingConnectionManager outgoingConnectionManager;
	private GetHostsFromCache getHostsFromCache;
	//private SearchMonitorSession searchMonitorSession;
	private KeepAliveThread keepAliveThread;
	

	/**
	 * Constructs an empty connection, the application must add a host cache or
	 * servant to generate activity
	 */
	public StracciatellaConnection() throws IOException {
		this(null);
	}

	/**
	 * EDITED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Construct the connection specifying connection data. The connection will
	 * not have access to a host cache unless specified later.
	 *
	 * @param connData connection data
	 **/
	public StracciatellaConnection(ConnectionData connData) throws IOException {
		LOG.info("Network connection initializing");
		LOG.info(System.getProperties().toString());

		if (null != connData) {
			connectionData = connData;
		}
		else {
			connectionData = new ConnectionData();
		}

		// the cache of known gnutella hosts
		hostCache = HostCache.getHostCache();
		connectionList = new ConnectionList(connectionData);
		hostCache.setConnectionList(connectionList);
	
		router = initializeRouter();
		connectionList.setRouter(router);
		

		// the router routes messages received on the connections


		// This replaces hostfeed as a means of getting hosts for bootstrapping
		getHostsFromCache = new GetHostsFromCache(hostCache, connectionList, connData);

		// Maintains appropriate incoming connections
		incomingConnectionManager =
			new IncomingConnectionManager(
				connectionList,
				connectionData
				);

		outgoingConnectionManager =
			new OutgoingConnectionManager(connectionList, hostCache);
		hostCache.setOutgoingConnectionManager(outgoingConnectionManager);
		connectionList.setOutgoingConnectionManager(outgoingConnectionManager);

		// TODO: Configurable "lookup" pings can probably be removed once
		// active / known host separation is complete. For now, just set the
		// lookup ping to true at all times.
		keepAliveThread = new KeepAliveThread(connectionList, 
				true);
	}
	
	/**
	 * Generates and returns the router thread for the Gnutella connection.
	 * @return The router thread for the Gnutella connection.
	 */
	protected Router initializeRouter() {
		return new Router(connectionList, connectionData, hostCache);
	}
	
	/**
	 * Fetch the router used by this Stracciatella connection.
	 * @return the router used by this Stracciatella connection
	 */
	public Router getRouter() {
		return router;
	}

	/** 
	 * EDITED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Starts the connection
	 */
	public void start() {
		try {
			// run the components
			router.start();
			getHostsFromCache.start();
			incomingConnectionManager.start();
			keepAliveThread.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * EDITED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Stop the connection, after execution the <code>GNUTellaConnection</code>
	 * is unusable. A new connection must be created if needed. If a 
	 * temporary disconnect from NodeConnections is desired, the connection count
	 * requests can be set to 0
	 *
	 */
	public void stop() {
		keepAliveThread.shutdown();
		getHostsFromCache.shutdown();
		incomingConnectionManager.shutdown();
		outgoingConnectionManager.shutdown();
		connectionList.shutdown();
		router.shutdown();
	}

	/**
	 * Get the current <code>HostCache</code>. Using the <code>HostCache</code>
	 * an application can query the known hosts, and add and remove hosts
	 *
	 * @return host cache
	 */
	public HostCache getHostCache() {
		return hostCache;
	}

	/**
	 * Query if we are online with the network, with at least one active
	 * node connection
	 *
	 * @return true if online, false otherwise
	 */
	public boolean isOnline() {
		if (null == connectionList) {
			return false;
		}

		return !connectionList.getActiveIncomingConnections().isEmpty()
			|| !connectionList.getActiveOutgoingConnections().isEmpty();
	}

	/**
	 * Get the <code>ConnectionData</code> settings
	 *
	 * @return connection data
	 */
	public ConnectionData getConnectionData() {
		return connectionData;
	}

	/** 
	 * Creates a session to conduct network searches
	 *
	 * @param query search query
	 * @param maxResults maximum result set size
	 * @param minSpeed minimum speed for responding servants
	 * @param receiver receiver for search responses
	 * @return session
	 */
	public SearchSession createSearchSession(
		String query,
		int queryType,
		int maxResults,
		int minSpeed,
		MessageReceiver receiver) {
			return new SearchSession(
				query,
				queryType,
				maxResults,
				minSpeed,
				this,
				router,
				receiver);
	}

	/**
	 * Get a search monitor session to monitor query requests
	 * flowing through this network connection. 
	 * 
	 * @param searchReceiver message receiver
	 */
	public SearchMonitorSession getSearchMonitorSession(MessageReceiver searchReceiver) {
		return new SearchMonitorSession(router, searchReceiver);
	}

	/**
	 * Creates a file serving session. <code>FileServerSession</code> can respond
	 * with a query hit
	 *
	 * @param receiver message receiver
	 */
	public FileServerSession createFileServerSession(MessageReceiver receiver) {
		return new FileServerSession(router, receiver);
	}

	

	/**
	 * Adds a listener to the connectionList
	 *
	 */
	public void addListener(ConnectedHostsListener chl) {
		connectionList.addListener(chl);
		
	}
	
	
	// TODO the two methods below should possibly be merged
	// consider if the ConnectionList should be publicly available
	/**
	 * Gets the current list of connections to GNUTella
	 *
	 * @return list of connections
	 * /
	public List<NodeConnection> getConnectionList() {
		return connectionList.getList();
	}*/

	/**
	 * Get the connection list
	 */
	public ConnectionList getConnections() {
		return connectionList;
	}

	/*
	 * Cleans dead connections from the connection list
	 * /
	public void cleanDeadConnections() {
		connectionList.cleanDeadConnections(Connection.CONNECTION_OUTGOING);
	}*/

	
	
	/**add an entry point to the network
	 * any host found at this IP-port will be added to the list of "known hosts" but no "friend" connection will be established
	 */
	public void addHostLocation (String IP, int port) {
		//TODO: manage this with hostcache
		
	}
	
	/** add an IP-port to contact a friend.
	 * once the GUID at this location is resolved, the connected host will be added to the friends list.
	 * @param IP
	 * @param port
	 */
	public void addFriend(String IP, int port){
	HostCache.getHostCache().addFriendLocation(IP, port);
	}
	
	/** add an IP-port to contact a friend.
	 * once the GUID at this location is resolved, the connected host will be added to the friends list.
	 * @param IP
	 * @param port
	 */
	public void addFriend(String guid){
	HostCache.getHostCache().friend(HostCache.getHost(GUID.getGUID(guid)));
	}
	
	/**
	 * remove the target peer from friend list.
	 * only applies if there exists a connection to this IP/port
	 * @param IP
	 * @param port
	 * @return
	 */
	public boolean unFriend(String IP, int port) {
		return connectionList.unFriend(IP, port);
	}

	/**
	 * Get the servant identifier the <code>GnutellaConnection</code> 
	 * is using. The servant identifier is used in connection with Push
	 * message processing
	 *
	 * @return servant identifier
	 */
	public static GUID getServentIdentifier() {
		return servantId;//Utilities.getClientIdentifier();
	}
	
	public static void setServentIdentifier( byte[] id){
		servantId = GUID.getGUID(id);
	}

	/**
	 * Sends a message to all connections
	 *
	 * @param m message to broadcast
	 * @param receiver message receiver
	 */
	void broadcast(Message m, MessageReceiver receiver) {
		List<Connection> connections = connectionList.getActiveConnections();

		Log.getLog().logDebug(
			"Broadcasting message, type: "
				+ m.getType()
				+ ", to "
				+ connections.size()
				+ " connections");

		ListIterator<Connection> i = connections.listIterator();

		while (i.hasNext()) {
			Connection connection = (Connection)i.next();

			try {
				connection.sendAndReceive(m, receiver);
			}
			catch (IOException io) {
				Log.getLog().log(io);
			}
		}
	}

	/**
	 * convenience method to get the Gnutella Servent Id as a string.
	 * @return
	 */
	public static String getServentIdentifierAsString() {
		
		return getServentIdentifier().toRawString();
	}
	
	/** blacklist a peer based on its gnutella ID*/
	public void blacklistPeer(String guid){	
		hostCache.blacklistPeer(GUID.getGUID(guid));	
	}
	
	/** blacklist a peer based on the IP/port it's currently connected with
	 * drops the connection to that peer, if it existed
	 * @return the GUID of the blacklisted peer, or null if none was found at that ipport
	 * */
	public String blacklistPeerFromIPPort(String IP, int port){
		Connection c = connectionList.getConnectionByIPPort(new IPPort(IP, port));
	
		if (c==null)
			return null;
		connectionList.dropConnection(c);
		hostCache.blacklistPeer(c.getConnectedServentGUID());
		
		return c.getConnectedServentIdAsString();
	}


	public void unBlacklistPeer(String guid){
		hostCache.unBlacklist(GUID.getGUID(guid));
	}
	
	public boolean isBlacklisted(String remoteServentId) {		
		return hostCache.isBlacklisted(GUID.getGUID(remoteServentId));
	}
	
	public List<String> getBlackList(){
		return hostCache.getBlackList();
	}

	public List<Connection> getActiveConnections() {
		// TODO Auto-generated method stub
		return connectionList.getActiveConnections();
	}

	/*public void addHost(Host host) {
		hostCache.addHost(host);
		
	}*/

}