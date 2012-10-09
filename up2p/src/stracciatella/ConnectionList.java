/*
 * Copyright (C) 2000-2001  Ken McCrary
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Email: jkmccrary@yahoo.com
 */
package stracciatella;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import protocol.com.dan.jtella.ConnectedHostsListener;
import protocol.com.dan.jtella.DroppedConnectionEvent;
import protocol.com.dan.jtella.HostsChangedEvent;
import protocol.com.dan.jtella.NewConnectionEvent;
import protocol.com.dan.jtella.PushWaits;
import protocol.com.kenmccrary.jtella.util.Log;
import protocol.com.kenmccrary.jtella.util.SocketFactory;
import stracciatella.IPPort;
import stracciatella.routing.Router;

/**
 *  Contains the set of current connections.
 *  only contains active connections.
 *  Those that are initializing are not added until their status is checked
 *
 */
public class ConnectionList {
	/** Name of Logger used by this adapter. */
    public static final String LOGGER = "protocol.pingpong.jtella";

	private static final int EVENT_NEWCONNECTION = 0;
	private static final int EVENT_DROPPEDCONNECTION = 1;
	private static final int EVENT_CHANGEDCONNECTION = 2;

    /** Logger used by this adapter. */
    private static Logger LOG = Logger.getLogger(LOGGER);
	private Map<GUID, Connection> guid2ConnectionMap; //keyed by GUID
	private Map<IPPort, Connection> ipport2ConnectionMap;
	
	private HostCache hostCache;
	private Router router;
	private boolean shutdownflag;
	private ConnectionData connectionData;
	private OutgoingConnectionManager outgoingConnectionMgr;

	private List<ConnectedHostsListener> listeners;

	//private List<Connection> connectionsStarting; //stores connections not yet established (no gnutellaId)

	public ConnectionList(ConnectionData connectionData) {
		guid2ConnectionMap = Collections.synchronizedMap(new HashMap<GUID, Connection>());
		ipport2ConnectionMap = Collections.synchronizedMap(new HashMap<IPPort, Connection>());
		hostCache = HostCache.getHostCache();
		shutdownflag= false;
		this.connectionData = connectionData;
		
	}

	
	/** set the router (necessary to initialize connections*/
	public void setRouter(Router r){
		router =r;
	}
	
	public Router getRouter(){
		return router;
	}
	
	/** set the outgoingConnectionManager*/
	public void setOutgoingConnectionManager(OutgoingConnectionManager mgr){
		outgoingConnectionMgr = mgr;
	}
	
	public ConnectionData getConnectionData(){
		return connectionData;
	}
	/** shutdown all connections*/
	public void shutdown(){
		shutdownflag= true;
		for (Connection c: guid2ConnectionMap.values()){
			c.shutdown();
		}
		
	}
	


	/**
	 *  Removes a connection
	 *
	 *  @param GUID of the connection to remove
	 */
	public void dropConnection(GUID g) {
		Connection c = 	guid2ConnectionMap.get(g);
		dropConnection(c);
	}
	
	/**
	 *  Removes a connection
	 *
	 *  @param GUID of the connection to remove
	 */
	public void dropConnection(Connection c) {
			guid2ConnectionMap.remove(c.getRemoteHost().getGUID());
			guid2ConnectionMap.remove(c.getRemoteIPPort());
			if(c.getStatus() != Connection.STATUS_STOPPED)
				c.shutdown();
			notifyListeners(c, EVENT_DROPPEDCONNECTION);
			
			if (hostCache.isFriend(c.getRemoteHost())) //we only notify if it was a friend.
				outgoingConnectionMgr.notifyFriend(c.getRemoteHost());
	}
	
	

	/**
	 *  Check if a connection exists to a host
	 *
	 *  @param GUID Gnutella servent id
	 *  
	 *  @return true if a connection exists to a per with this ID
	 */
	public boolean contains(GUID guid) {
		//boolean result = false;
		
			if(guid2ConnectionMap.containsKey(guid))
				return true;
			return false;
			
	}
	
	
	/**
	 * check if we have an incoming connection from a particular peer
	 * @param guid the peer GUID
	 * @return true if an incoming connection from this peer exists.
	 */
	public boolean hasIncomingConnectionFrom(GUID guid){
		Connection c= guid2ConnectionMap.get(guid);
		if (c!=null)
			if (c.isIncoming())
				return true;
		return false;		
	}
	
	/**
	 * check if we have an outgoing connection to a particular peer
	 * @param guid
	 * @return
	 */
	public boolean hasOutgoingConnectionTo(GUID guid){
		Connection c= guid2ConnectionMap.get(guid);
		if (c!=null)
			if (c.isOutgoing())
				return true;
		return false;		
	}
	
	/**
	 * checks if we have an active connection to the specified ip-port
	 * note: dynamic ports and ip addresses make this method not terribly useful.
	 * @param ipp
	 * @return true if a connection exists to this ip/port
	 */
	public boolean contains (IPPort ipp){
		if(ipport2ConnectionMap.containsKey(ipp))
			return true;
		return false;
	}
	
	/////////////////////////////////////////////////////// factory!
	/**
	 * EDITED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Starts an incoming connection to a node on the network,
	 * does initial handshake
	 * 
	 * addition by alan, aug 2012: GUID blacklist implemented: connection from blacklisted peers refused.
	 * @param reject: to reject new connections (set to true when we already have enough connections)
	 * 
	 * @return true for a good start, false otherwise
	 */
	public boolean startIncomingConnection(Socket socket) {
		boolean result = false;
		
		Connection connection;
		//InputStream inputStream;
		BufferedReader bufferedReader;
		//OutputStream outputStream;
		BufferedWriter bufferedWriter;
		try {
			connection = new Connection(socket, this);
		
		//this is the code in connection.initsocket()
		//inputStream  = new DataInputStream(socket.getInputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//outputStream = new DataOutputStream(socket.getOutputStream());
		bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		} catch (IOException e1) {
			LOG.error("startIncomingConnection: failed while trying to initialize the socket");
			return false;
		}

		Map<String,String> connectionProperties = new TreeMap<String, String>();

		try {
			// Read the first line of the request
			String request = "";
			request = bufferedReader.readLine();
			if (request == null) {
				//shutdown(); //connection.shutdown()
				return false;
			}
			
			
			// Gnutella 0.6 connection
			 if (request.startsWith(ConnectionData.CONNECT_STRING_COMPARE)) {

				/*// Check if we should accept the connection 
				 * --update 2011-08-02: no this has been checked before invoking the method
				if (connectionList.getActiveIncomingConnectionCount()
					< connectionData.getIncommingConnectionCount()) {*/ 

					//in this loop the lines of the request are read one by one
					while ((request != null) && (!request.equals(""))) { 
						/*Parse what "connection property" is announced by the connecting host
						 * such as :
						 * X-Accept-encoding:...
						 * X-Ultrapeer-Query-Routing: 0.1..
						* X-Query-Routing: 0.1..
						* Listen-IP: 202.143.244.190:6346..
						* X-Ext-Probes: 0.1..
						* Remote-IP: 134.117.60.83..
						* GGEP: 0.5..
						* X-Dynamic-Querying: 0.1
						 */
						int split = request.indexOf(":"); //find : separating property name and value
						if (split>0){
							String prop = request.substring(0, split);
							String value = request.substring(split+1);
							connectionProperties.put(prop, value.trim()); //store in connectionsproperties list
						}
						
						

						try {
							request = bufferedReader.readLine(); //next line from the incoming request
						}
						catch (IOException ioe) {
							LOG.error("#1: " + ioe);
							break;
						}
					}////end while (reading lines from the incoming request)
					
					
					////////////////// Criterion for accepting the connection: (based on unique ID) 
					//  1 - make sure it's not blacklisted
					// 2 - check if it's a duplicate (in which case we make the other connection 2-way instead)
					
					String remoteServentId = connectionProperties.get(ConnectionData.SERVENTID_HEADER);
					GUID remoteGUID= GUID.getGUID(remoteServentId);
					//	Host remoteHost = HostCache.getHost(remoteGUID);
					
					//TODO in future: authenticate, using Station to Station protocol based on public keys.
					
					// Step 2 : respond ----------------------------------------------------------------------------------
					try {
						//TODO: use serventid to check for duplicates
						LOG.info("Checking for incoming duplicate: remote-id ="+ remoteServentId);
						
						//
						
						if (hostCache.isBlacklisted(remoteGUID)){ //--is it blacklisted?
							LOG.info("Incoming connection detected as blacklisted peer."
									+ "\nConnection rejected.");
							String response = ConnectionData.SERVER_REJECT + ConnectionData.CRLF;
							bufferedWriter.write(response);
							bufferedWriter.flush();
							
							return false;
						}
						else	if(this.contains(remoteGUID)) { //--is it a duplicate connection ?

								Connection other = getConnectionByID(remoteGUID);
								// modify type of other connection, now two way
								if (other.isOutgoing()){
									other.setType(Connection.CONNECTION_TWOWAY);
									notifyListeners(other, EVENT_CHANGEDCONNECTION); //connection modified
								}
								LOG.info("Incoming connection detected as duplicate of outgoing connection."
										+ "\nConnection made 2-way.");
								String response = ConnectionData.SERVER_ACCEPT_DUPLICATE + ConnectionData.CRLF;
								
								bufferedWriter.write(response);
								bufferedWriter.flush();
								
								
								return false;
							} else  {
								LOG.info("new incoming connection from \""
									+ socket.getInetAddress().getCanonicalHostName() + ":"
									+ connectionProperties.get("Listen-Port")
									+ "\" is unique, proceeding.");
								//TODO: possibly we shouldn't systematically accept all incoming connections.
							}
						
					} catch (Exception e) {
						LOG.debug("IP Address or port of incoming connection could not be determined."
								+ "\nConnection rejected.");
						//e.printStackTrace();
						String response = ConnectionData.SERVER_REJECT+ ConnectionData.CRLF;
						bufferedWriter.write(response);
						bufferedWriter.flush();
						
						return false;
					}
					
					//ok, accept 
					//TODO: connection data could directly provide the entire accept message
					String myIP= connectionProperties.get(ConnectionData.REMOTE_IP);
					String theirIP = socket.getInetAddress().toString();

					String response = connectionData.getConnectionAcceptGreeting(myIP, theirIP); 
					

						bufferedWriter.write(response);
						bufferedWriter.flush();
						LOG.debug("Incoming connection: Wrote response.");
						
						//step 3: set the connection properties for the headers
						connection.setProperties(connectionProperties);
						
						// Read the client's response and any headers ---------------------------------------------------------------------
						// Only if the client accepts our headers will the connect succeed
						String line = "";
						try {
							while((line = bufferedReader.readLine()) != null && (!line.equals(""))) {
								if (line.startsWith(ConnectionData.SERVER_READY_COMPARE)) {
									// Success
									result = true;
								} else {
									// Read additional headers
									int split = line.indexOf(":"); //find : separating property name and value
									if (split>0){
										String prop = line.substring(0, split);
										String value = line.substring(split+1);
										connectionProperties.put(prop, value.trim()); //store in connectionsproperties list
									}
								}
							}
						}
						catch (IOException ioe) {
							LOG.error("#2: " + ioe);
						}
						
					
					}
					else {
						String response = ConnectionData.SERVER_REJECT + ConnectionData.CRLF;
						bufferedWriter.write(response);
						bufferedWriter.flush();
					}
				
	/*		}
			 UPdate: Pushes not managed here any more.
			// Incoming connection in response to a push message
			else if (request.startsWith("GIV")) {
				pushRequest = true;

				// Get servent ID as a string
				String guidString = (request.split(".*:", 2))[1].split("/.*", 2)[0];

				// Notify waiting PushResponseInterface
				PushWaits.pushReceived(
					guidString,
					socket,
					inputStream,
					outputStream,
					bufferedReader,
					bufferedWriter);
			}*/
		}
		catch (IOException ioe) {}

		if ((result) && (!shutdownflag)) {

			
			// Now start monitoring network messages			
			Thread connectionThread = new Thread(connection, "ConnectionThread");
			connectionThread.start();
			
			this.addConnection(connection);
			notifyListeners(connection, EVENT_NEWCONNECTION);// new connection
			//TODO = parse connectionProperties and set all info for connection

 
			/*Log.getLog().logDebug*/
			LOG.debug("Incoming connection established: ");
		}
		

		return result;
	}
	
	//convbenience method to add a connection (avoids missing one of the maps)
	private void addConnection(Connection connection) {
		guid2ConnectionMap.put(connection.getRemoteHost().getGUID(), connection);
		ipport2ConnectionMap.put(connection.getRemoteIPPort(), connection);
		
	}


	/**
	 * EDITED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Starts an outgoing connection to a node on the network,
	 * does initial handshaking
	 *
	 * @return true for a good start, false for failure
	 */
	public boolean startOutgoingConnection(IPPort ipport) {
		boolean success = true;

		try {
			Socket socket = SocketFactory.getSocket(ipport.IP, ipport.port, 10000);
			Connection connection = new Connection(socket, this); 
			connection.status = Connection.STATUS_CONNECTING;


			//this is the code in connection.initsocket()
			InputStream inputStream = new DataInputStream(socket.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			String remoteHost = socket.getInetAddress().getHostAddress();

			String connectionGreeting = connectionData.getOutgoingConnectionGreeting(remoteHost);

			LOG.debug("Sending greeting to : " + ipport.IP + ":" + ipport.port
					+ "\nGreeting:\n" + connectionGreeting);

			byte[] greetingData = connectionGreeting.getBytes();
			outputStream.write(greetingData, 0, greetingData.length);
			outputStream.flush();

			// Is this blocking the entire system for too long?
			String response = bufferedReader.readLine();

			LOG.info("Received greeting response: " + response + " from host: " + ipport);

			//option 1 = ok, new connection accepted
			if (response.startsWith(ConnectionData.SERVER_READY_COMPARE)){
				//ok, accepted
				success=true;
				//option 2 = this is a duplicate, but other connection is made 2-way
			}else if (response.startsWith(ConnectionData.SERVER_ACCEPT_DUPLICATE)) {
				//accepted as duplicate: the connection is now two way
				//Expected: next sent = a line indicating the remote hosts's GUID.
				//need to parse GUID
				if (!bufferedReader.ready())
					throw new Exception("Remote host "+ipport+" claimed connection was duplicate but didn't identify itself.");
				//read another line
				String otherGUID = bufferedReader.readLine();
				if (otherGUID.startsWith(ConnectionData.SERVENTID_HEADER)){
					otherGUID = otherGUID.substring(ConnectionData.SERVENTID_HEADER.length()).trim();
				} else {
					throw new Exception("Remote host "+ipport+" claimed connection was duplicate but didn't identify itself.");
				}
				Connection other = getConnectionByID(GUID.getGUID(otherGUID));
				if (other==null)
					throw new Exception("Remote host "+ipport+" claimed connection was duplicate but other connection not found [GUID="+otherGUID+"]");
				if(other.isIncoming() && !other.isOutgoing()){ //other connection only incoming
					other.setType(Connection.CONNECTION_TWOWAY);
				}
			} else { 
				LOG.info("Connection rejection: " + ipport);
				connection.status = Connection.STATUS_FAILED;
				success = false;
			}


			Map<String,String> connectionProperties = new TreeMap<String, String>();
			//parse response lines for connection properties

			while (response != null) { ///////////////read each line of the response (headers: Listen-IP, Remote-IP, X-Try-Ultrapeers, etc.)

				int split = response.indexOf(":"); //find : separating property name and value
				if (split>0){
					String prop = response.substring(0, split);
					String value = response.substring(split+1);

					if (prop.startsWith(ConnectionData.V06_X_TRY_ULTRAPEERS_COMPARE)) {

						StringTokenizer ultrapeers = new StringTokenizer(value, ":, ");
						//-------------------------------------------------------------------						
						//expected format of *value*:
						// guid:IP:port
						//-------------------------------------------------------------------
						/* Set delims to ", " to separate out the ultrapeers, and 
						// remove one on each iteration of the loop
						// Tokenize the host IP and port, and create a new 
						// Ultrapeer type host*/
						String guid =ultrapeers.nextToken();
						Host host =HostCache.getHost(GUID.getGUID(guid));
						//parse list of ip ports given for this ip/port
						while (ultrapeers.hasMoreTokens()) {
							String ip = ultrapeers.nextToken();
							int port = Integer.parseInt(ultrapeers.nextToken());
							host.addKnownLocation(ip, port);
							//host.setUltrapeer(true);
							//hostCache.addknownHost(host); update: done automatically by using HostCache.getHost()
						}


						connectionProperties.put(prop, value.trim()); //store in connectionsproperties list
					}

					// Look for X-Try headers and cache them. Format= 
					// X-Try-Ultrapeer: guid1: 1.2.3.4:1234, 2:5.6.7.8:5678\r\n
					// X-Try-Ultrapeer: guid2: 3.2.3.1:1284, 2:5.6.7.5:5678\r\n
					// X-Try-Ultrapeer: guid1: 1.2.3.5:1294, 2:5.6.7.0:5679\r\n



					/*if (foundXTryUltrapeers && foundXUltrapeer) {
					break;
				}*/

					if (bufferedReader.ready())
						response = bufferedReader.readLine();
					else break;
				}
			}

			String remoteServentId = connectionProperties.get(ConnectionData.SERVENTID_HEADER);
			GUID remoteGUID= GUID.getGUID(remoteServentId);


			// Check to ensure the the node isn't connecting to itself
			if (success) {
				if(	remoteGUID.equals(connectionData.getMyServentIdAsGUID())){ //TODO: make sure this test succeeds for identical GUIDs

					LOG.info("Abandoning connection to: " + ipport + " (appears to be myself!)");
					success = false;

				}
				if(contains(remoteGUID)){ //duplicate connection
					Connection other = getConnectionByID(remoteGUID);
					if(other.isIncoming() && !other.isOutgoing()){
						other.setType(Connection.CONNECTION_TWOWAY);
						notifyListeners(other, EVENT_CHANGEDCONNECTION); //this connection has changed
					}
						success=true; //still abandon this connection, we already have one.

					
				}
			}

			if(success && !shutdownflag) {		
				try {
					String finalok = connectionData.getConnectionGreetingStep3(connectionProperties.get(ConnectionData.REMOTE_IP));
					bufferedWriter.write(finalok);

					bufferedWriter.flush();

					LOG.info("Connection started on: " + ipport +" to host guid="+ remoteServentId);

					// Now start monitoring network messages
					Thread connectionThread = new Thread(connection, "ConnectionThread");
					// Starts NodeConnection's run method
					connectionThread.start();
					
					// add connection to list.
					this.addConnection(connection);

					notifyListeners(connection, EVENT_NEWCONNECTION);
					
				}
				catch (IOException ioe) {
					LOG.error("I/O Exception transmitting ack");
					connection.status = Connection.STATUS_FAILED;
					success = false;
				}
			}
		}//end initial try block
		catch (Exception e) {
			LOG.error(e);
			success = false;
		}


		return success;
	}



	//notify all listeners that a particular connection has changed (been created, dropped, or modified)
	private void notifyListeners(Connection connection, int evtype) {
		HostsChangedEvent evt;
		switch(evtype){
		case(EVENT_NEWCONNECTION):
			evt = new NewConnectionEvent(connection);
		break;
		case(EVENT_DROPPEDCONNECTION):
			evt= new DroppedConnectionEvent(connection);
		break;
		default:
			evt = new HostsChangedEvent(connection);
		}
		
		for (ConnectedHostsListener chl:listeners)
			chl.hostsChanged(evt);
		
	}


	/**
	 *  Get active outgoing and incoming connections
	 *  (A copy of the List)
	 *
	 *  @return active connections
	 */
	public List<Connection> getActiveConnections() {
		List<Connection> all = getActiveOutgoingConnections();

		all.addAll(getActiveIncomingConnections());

		return all;
	}

	/**
	 *  Gets the active outgoing connections
	 *
	 *  @return list of connections
	 */
	public List<Connection> getActiveOutgoingConnections() {
		return getActiveConnections(Connection.CONNECTION_OUTGOING);
	}

	/**
	 *  Gets the active incoming connections
	 *
	 *  @return active list
	 */
	public List<Connection> getActiveIncomingConnections() {
		return getActiveConnections(Connection.CONNECTION_INCOMING);
	}
	
	/**
	 * Checks if any incoming connections have yet to provide a Listen-IP.
	 * This is used to ensure that no outgoing connections are created while
	 * an unidentified incoming connection exists, since attempting to open both
	 * an outgoing and incoming connection to the same host:port will cause
	 * disconnects.
	 * 
	 * @return	True if any connection has failed to provide a valid Listen-IP.
	 * /
	public boolean hasUnidentifiedConnection() {
		synchronized (currentConnectionList) {
			for(Connection connection : currentConnectionList) {
				if (connection.getListenString() == null) {
					LOG.debug("Unidentified found!");
					return true;
				}
			}
		}
		return false;
	}*/

	/**
	 *  Gets the active connections of a particular type
	 * Connection.CONNECTION_ANYTYPE is the wildcard for all types, incoming, outgoing, two-way 
	 *  @return list of connections
	 */
	private List<Connection> getActiveConnections(int type) {
		LinkedList<Connection> activeList = new LinkedList<Connection>();

		
			Iterator<Connection> i = guid2ConnectionMap.values().iterator();

			while (i.hasNext()) {
				Connection connection = i.next();

				if (type == Connection.CONNECTION_ANYTYPE || //match on connection type
						connection.getType() == Connection.CONNECTION_TWOWAY || 
						connection.getType() == type)
					if( connection.getStatus() == Connection.STATUS_OK) //connection is active
						activeList.add(connection);

			}
		

		return activeList;
	}

	/**
	 *  Gets a count of the active (running outgoing connections)
	 *
	 *  @return count
	 */
	int getActiveOutgoingConnectionCount() {
		return getActiveConnectionCount(Connection.CONNECTION_OUTGOING);
	}

	/**
	 *  Get the count of active incoming connections
	 *
	 *  @return count
	 */
	int getActiveIncomingConnectionCount() {
		return getActiveConnectionCount(Connection.CONNECTION_INCOMING);
	}

	/**
	 *  Gets a count of the active connections of the given type
	 *
	 *  @return count
	 */
	public int getActiveConnectionCount(int type) {
		
//TODO: yeah, not optimized for performance since we make the list instead of just counting.
		return getActiveConnections(type).size();
	}

	/**
	 *  Reduce the number of outgoing connections
	 *
	 * /
	void reduceActiveOutgoingConnections(int newCount) {
		int activeCount = getActiveOutgoingConnectionCount();

		if (activeCount <= newCount) {
			// nothing to do
			return;
		}

		int killCount = activeCount - newCount;
		int killed = 0;
		synchronized (currentConnectionList) {
			Iterator<Connection> i = currentConnectionList.iterator();

			while (i.hasNext() && killed != killCount) {
				Connection connection = (Connection)i.next();

				if (connection.getType() == Connection.CONNECTION_OUTGOING
					&& connection.getStatus() == Connection.STATUS_OK) {
					connection.shutdown();
					killed++;
				}
			}
		}
	}*/

	/**
	 *  Reduce the number of incoming connections
	 *
	 * /
	void reduceActiveIncomingConnections(int newCount) {
		int activeCount = getActiveIncomingConnectionCount();

		if (activeCount <= newCount) {
			// nothing to do
			return;
		}

		int killCount = activeCount - newCount;
		int killed = 0;
		synchronized (currentConnectionList) {
			Iterator<Connection> i = currentConnectionList.iterator();

			while (i.hasNext() && killed != killCount) {
				Connection connection = (Connection)i.next();

				if (connection.getType() == Connection.CONNECTION_INCOMING
					&& connection.getStatus() == Connection.STATUS_OK) {
					connection.shutdown();
					killed++;
				}
			}
		}
	}*/

	/*
	 *  Remove any dead connections from the list
	 *
	 *  @param type of collection to clean
	 *  @return number of live connections remaining
	 * /
	public int cleanDeadConnections(int type) {
		int liveCount = 0;
		Log.getLog().logInformation("Live connection list start: ");

		List<GUID> toremove = new ArrayList<GUID>();
		
			Iterator<GUID> i = guid2ConnectionMap.keySet().iterator();

			while (i.hasNext()) {
				GUID current= i.next();
				Connection connection = guid2ConnectionMap.get(current);

				if (type == Connection.CONNECTION_ANYTYPE || //match on connection type
						connection.getType() == Connection.CONNECTION_TWOWAY || 
						connection.getType() == type)
					if( connection.getStatus() == Connection.STATUS_FAILED || connection.getStatus() == Connection.STATUS_STOPPED ) //connection is dead
					{
						toremove.add(current);
					} else {
						liveCount++;
					}

			}
			for (GUID g: toremove){
				dropConnection(g);
			}
		

		return liveCount;

	}*/

	

	/**
	 *  Closes all connections and removes them from the collection
	 *TODO: beware of deadlock situations
	 */
	public void empty() {
			for(GUID k: guid2ConnectionMap.keySet())
				guid2ConnectionMap.remove(k).shutdown();
			
			ipport2ConnectionMap.clear();
	}


	/**
	 * initiate an outgoing connection
	 * @param connection
	 * /
	public void addStartingConnection(Connection connection) {
		connectionsStarting.add(connection);
		
	}*/


	/**
	 * drop attempted outgoing connection
	 * @param connection
	 * /
	public void abortConnection(Connection connection) {
		connectionsStarting.remove(connection);
		
	}*/

	/** get a connection by the GUID of a particular host
	 * @param remoteGUID the GUID of the remote peer
	 * @return the connection to this peer, if it exists, otherwise null
	 *  */
	public Connection getConnectionByID(GUID remoteGUID) {
		return guid2ConnectionMap.get(remoteGUID);
		
	}

	/** get the Connection instance connected to the given IP/port
	 * 
	 * @param ipPort the target IP+port
	 * @return the Connection object, or null if no connection exists to that IP/port
	 *TODO: check new connections (status =STARTING or whatever)
	 */
	public Connection getConnectionByIPPort(IPPort ipPort) {
		return ipport2ConnectionMap.get(ipPort);
	}


	/** this method is just used to get the connection from the IP/port, and will go back to 
	 * the hostcache to unfriend the peer based on the peer s GUID*/
	public boolean unFriend(String iP, int port) {
		IPPort ipp = new IPPort(iP,port);
		Connection c= getConnectionByIPPort(ipp);
		if (c==null)
			return false;
		hostCache.unFriend(c.getConnectedServentGUID());
		
		return true;
		
	}


	/** add a listener. Listeners are notified when a new connection is started or if a connection is dropped*/
	public void addListener(ConnectedHostsListener chl) {
		listeners.add(chl);
		
	}


	/**
	 * notification that the specified host is now blacklisted. Drop any incoming connection from it.
	 * Future reconnections will be refused anyway.
	 * @param h
	 */
	public void notifyBlacklist(Host h) {
		GUID g = h.getGUID();
		if (hasIncomingConnectionFrom(g))
			dropConnection(g);
		
	}

	/** receive a notification that the identified peer is no longer a friend. Will drop the outgoing 
	 * connection to the peer, but keep the incoming one.
	 * TODO: requires a new network message to notify the other peer
	 * for now, just drop the connection, but accept future incoming connections.
	 * @param guid
	 */
	public void notifyUnfriend(GUID guid) {
		//for now just do the same as for a blacklisted peer: drop the connection
		if (hasIncomingConnectionFrom(guid))
			dropConnection(guid);
		
	}




}
