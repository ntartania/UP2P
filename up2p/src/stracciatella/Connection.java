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

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import protocol.com.kenmccrary.jtella.util.Log;
import protocol.com.kenmccrary.jtella.util.SocketFactory;

import protocol.com.dan.jtella.ConnectedHostsListener;
import protocol.com.dan.jtella.HostsChangedEvent;
import protocol.com.dan.jtella.PushWaits;
import stracciatella.HostCache;
import stracciatella.message.Message;
import stracciatella.message.MessageFactory;
import stracciatella.message.MessageReceiver;
import stracciatella.message.PingMessage;
import stracciatella.message.PongMessage;
import stracciatella.routing.Router;


/**
 * EDITED BY: Daniel Meyers, 2003<br>
 * Represents a connection to an application communicating with the STRACCIATELLA protocol
 *
 */
public class Connection implements Runnable {
    /** Name of Logger used by this adapter. */
    public static final String LOGGER = "protocol.jtella";
    public static final String LOGGER2 = "protocol.pingpong.jtella";

    /** Loggers used by this class:
     * LOGnormal for stuff that doesn't happens too often, LOGall for the background noise (pings, pongs, etc.) */
    protected static Logger LOGnormal = Logger.getLogger(LOGGER);
    
    protected static Logger LOGall = Logger.getLogger(LOGGER2);
    
	/**
	 * Connection is attempting to connected to STRACCIATELLA
	 */
	public final static int STATUS_CONNECTING = 0;

	/**
	 * Connection is operating normally
	 */
	public final static int STATUS_OK = 1;

	/**
	 * Connection is not operating normally
	 */
	public final static int STATUS_FAILED = 2;

	/**
	 * Connection has been stopped
	 */
	public final static int STATUS_STOPPED = 3;

	/**
	 * Connection created by another servant
	 */
	public final static int CONNECTION_INCOMING = 0;

	/**
	 * Connection created by JTella servant
	 */
	public final static int CONNECTION_OUTGOING = 1;
	
	/**
	 * Two-way friendship.
	 */
	public final static int CONNECTION_TWOWAY = 2;
	
	public final static int CONNECTION_ANYTYPE =9;

	
	private List<MessageData> messageBacklog;
	private Thread connectionThread;
	//protected HostCache hostCache;
	protected boolean shutdownFlag = false;
	protected Socket socket;
	private Host connectedHost;
	private IPPort myIpport;
	protected DataInputStream inputStream;
	protected DataOutputStream outputStream;
	protected BufferedReader bufferedReader;
	protected BufferedWriter bufferedWriter;
	protected boolean pushRequest = false;
	protected AsyncSender asyncSender;
	protected Router router;
	protected ConnectionData connectionData;
	protected ConnectionList connectionList;
	private IPPort remoteIpport; //remote host
	//protected int port; //remote port
	protected int status;
	protected int type;
	// True if the *remote* servent is an ultrapeer
	protected boolean ultrapeer = false;
	protected int inputCount = 0;
	protected int outputCount = 0;
	protected int droppedCount = 0;
	protected long createTime;
	protected long sendTime;
	
	// Listeners to notify whenever the ConnectionList changes
	private List<ConnectedHostsListener> listeners;
	
	///////////////////////
	protected Map<String,String> connectionProperties; //this stores all the info about the connection as read from the STRACCIATELLA connect headers

	


public Connection(Socket socket, ConnectionList clist)
		throws IOException {
		createTime = System.currentTimeMillis();

		this.connectionList = clist;
		this.router = clist.getRouter();
		
		this.socket = socket;
		this.connectionData = clist.getConnectionData();
		this.connectionList = clist;
		remoteIpport = new IPPort(socket.getInetAddress().getHostAddress(), socket.getPort());
		 
		//type = CONNECTION_INCOMING;
		messageBacklog = Collections.synchronizedList(new LinkedList<MessageData>());
		listeners = Collections.synchronizedList(new ArrayList<ConnectedHostsListener>());
		initSocket();
}

	/**
	 * Constructor helper
	 *
	 */
	private void initSocket() throws IOException {
		socket.setSoTimeout(7000);
		socket.setTcpNoDelay(true);
		inputStream = new DataInputStream(socket.getInputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		outputStream = new DataOutputStream(socket.getOutputStream());
		bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public Host getRemoteHost(){
		return connectedHost;
	}
	
	
	/**
	 * Get the public IP address for this connection, as seen from the remote host
	 * @return The IP, or null if we don't have it
	 */
	public String getPublicIP(){
		return myIpport.IP;//connectionProperties.get("Remote-IP"); // this is the format of the property returned by the remote host.
	}
	
	/**
	 * @return	The URL Prefix advertised by the connected servent during the connection
	 * 			handshake, or null if the handshake has not been fully performed.
	 */
	public String getUrlPrefix() {
		return connectionProperties.get("Up2p-Url-Prefix");
	}
	
	/**
	 * @return	The port that the remote node has advertised for file transfer requests
	 */
	public String getDownloadPort() {
		return connectionProperties.get("Up2p-Download-Port");
	}
	
	/**
	 * 
	 * @return the STRACCIATELLA GUID of the remote servent
	 */
	public String getRemoteServentId(){
		return connectionProperties.get("Straciatella-Servent-id");
	}
	
	/**
	 * @return	The IP/port string that should be used to initiate connections to
	 * 			the connected remote peer.
	 */
	public String getListenString() {
		return connectionProperties.get("Listen-IP");
	}
	
	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Notifies all listeners of some change to the connection
	 *
	 */
	protected void notifyListeners() { //TODO: check that we don't have a deadlock with shutdown() method in connectionList

		for (ConnectedHostsListener temp :listeners)
			temp.hostsChanged(new HostsChangedEvent(this));
	}



	/*
	 * Retrieve the host this connection links to
	 * NOTE: duplicate method
	 * /
	public String getHost() {
		return host;
	}*/

	/**
	 * EDITED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Stops the connection and cleans up
	 *
	 */
	public void shutdown() {
		shutdownFlag = true;

		if (null != connectionThread && Thread.currentThread() != connectionThread){ //if the thread interrupts itself then it can't finish executing this method.
			connectionThread.interrupt();
		}
		

		if (null != asyncSender) {
			asyncSender.shutdown();
		}

		if ((status == STATUS_OK) || (status == STATUS_FAILED)) {
			status = STATUS_STOPPED;
			notifyListeners();
		}
		else {
			status = STATUS_STOPPED;
		}

		// Only close the socket if it is not going to be handed on as
		// part of the response to a push request
		if (!pushRequest) {
			try {
				if (null != bufferedWriter) {
					bufferedWriter.close();
				}
			}
			catch (IOException ioe) {}
			try {
				if (null != bufferedReader) {
					bufferedReader.close();
				}
			}
			catch (IOException ioe) {}
			try {
				if (null != outputStream) {
					outputStream.close();
				}
			}
			catch (IOException ioe) {}
			try {
				if (null != inputStream) {
					inputStream.close();
				}
			}
			catch (IOException ioe) {}
			try {
				if (null != socket) {
					socket.close();
				}
			}
			catch (IOException ioe) {}
		}
	}

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * The Thread.wait() method cannot be called from an unsyncronised method, so a synchronised
	 * one was created for the purpose of allowing the Connection to wait()
	 * 
	 * @param millis The number of milliseconds to wait for. 0 is equal to no limit
	 */
	protected synchronized void waitMethod(int millis) {
		try {
			if (millis == 0) {
				wait();
			}
			else {
				wait(millis);
			}
		}
		catch (InterruptedException ie) {}
	}

//	/**
//	 * EDITED BY: Daniel Meyers, 2004
//	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
//	 * Starts an incoming connection to a node on the network,
//	 * does initial handshake
//	 * 
//	 * addition by alan, aug 2012: GUID blacklist implemented: connection from blacklisted peers refused.
//	 * @param reject: to reject new connections (set to true when we already have enough connections)
//	 * 
//	 * @return true for a good start, false otherwise
//	 */
//	public boolean startIncomingConnection() {
//		boolean result = false;
//		pushRequest = false;
//
//		try {
//			// Read the first line of the request
//			String request = "";
//			request = bufferedReader.readLine();
//			if (request == null) {
//				shutdown();
//				return result;
//			}
//			
//			/* / Gnutella 0.4 connection
//			else if (request.startsWith(CONNECT_STRING_COMPARE)) {
//				String response = SERVER_REJECT + CRLF;
//
//				// GNUTella 0.4 handshake
//				if (connectionData.getUltrapeer()) {
//					// the 0.4 handshake is good
//					result = true;
//					response = SERVER_READY;
//				}
//
//				// write the response
//				bufferedWriter.write(response);
//				bufferedWriter.flush();
//
//			}*/
//			
//			// Gnutella 0.6 connection
//			 if (request.startsWith(V06_CONNECT_STRING_COMPARE)) {
//
//				/*// Check if we should accept the connection 
//				 * --update 2011-08-02: no this has been checked before invoking the method
//				if (connectionList.getActiveIncomingConnectionCount()
//					< connectionData.getIncommingConnectionCount()) {*/ 
//
//					//in this loop the lines of the request are read one by one
//					while ((request != null) && (!request.equals(""))) { 
//						/*Parse what "connection property" is announced by the connecting host
//						 * such as :
//						 * X-Accept-encoding:...
//						 * X-Ultrapeer-Query-Routing: 0.1..
//						* X-Query-Routing: 0.1..
//						* Listen-IP: 202.143.244.190:6346..
//						* X-Ext-Probes: 0.1..
//						* Remote-IP: 134.117.60.83..
//						* GGEP: 0.5..
//						* X-Dynamic-Querying: 0.1
//						 */
//						int split = request.indexOf(":"); //find : separating property name and value
//						if (split>0){
//							String prop = request.substring(0, split);
//							String value = request.substring(split+1);
//							connectionProperties.put(prop, value.trim()); //store in connectionsproperties list
//						}
//						
//						
//						// Look for X-Ultrapeer header (only used if we are not an ultrapeer)
//						if ((request.equalsIgnoreCase("X-Ultrapeer: true"))
//							|| (request.equalsIgnoreCase("X-Ultrapeer: yes"))) {
//
//							ultrapeer = true;
//						}
//
//						try {
//							request = bufferedReader.readLine(); //next line from the incoming request
//						}
//						catch (IOException ioe) {
//							LOGnormal.error("#1: " + ioe);
//							break;
//						}
//					}////end while (reading lines from the incoming request)
//					
//					// Step 2 : respond ----------------------------------------------------------------------------------
//					try {
//						//TODO: use serventid to check for duplicates
//						LOGnormal.info("Checking for incoming duplicate: " + socket.getInetAddress().getCanonicalHostName() + ":"
//								+ connectionProperties.get("Listen-Port"));
//						
//							if(connectionList.contains(getRemoteServentId())) {
//								
//
//
//								Connection other = connectionList.getConnectionByID(getRemoteServentId());
//								// modify type of other connection, now two way
//								if (other.getType()== Connection.CONNECTION_OUTGOING)
//									other.setType(Connection.CONNECTION_TWOWAY);
//								LOGnormal.info("Incoming connection detected as duplicate of outgoing connection."
//										+ "\nConnection rejected.");
//								String response = V06_SERVER_REJECT + CRLF;
//								//TODO: change reject message
//								bufferedWriter.write(response);
//								bufferedWriter.flush();
//								shutdown();
//								
//								return result;
//							} else if (hostCache.isBlacklisted(getRemoteServentId())){
//								LOGnormal.info("Incoming connection detected as blacklisted peer."
//										+ "\nConnection rejected.");
//								String response = V06_SERVER_REJECT + CRLF;
//								bufferedWriter.write(response);
//								bufferedWriter.flush();
//								shutdown();
//								return result;
//							}
//							else {
//								LOGnormal.info("Incoming connection \""
//									+ socket.getInetAddress().getCanonicalHostName() + ":"
//									+ connectionProperties.get("Listen-Port")
//									+ "\" is unique, proceeding.");
//							}
//						
//					} catch (Exception e) {
//						LOGnormal.debug("IP Address or port of incoming connection could not be determined."
//								+ "\nConnection rejected.");
//						//e.printStackTrace();
//						String response = V06_SERVER_REJECT+ CRLF;
//						bufferedWriter.write(response);
//						bufferedWriter.flush();
//						shutdown();
//						return result;
//					}
//					
//					if (connectionData.getUltrapeer() || ultrapeer) {
//						// A connect string was recognized, send the response indicating
//						// the version number and user agent
//						// TODO Send X-Try-Ultrapeers 
//						String response = "";
//
//						if (connectionData.getUltrapeer()) {
//							response =
//								V06_SERVER_READY
//									+ V06_LISTEN_IP + getPublicIP() + ":" + Integer.toString(connectionData.getIncomingPort())
//									+ CRLF
//									+ V06_REMOTE_IP + host
//									+ CRLF
//									+ UP2P_URL_PREFIX + connectionData.getUrlPrefix()
//									+ CRLF
//									+ UP2P_DOWNLOAD_PORT + connectionData.getFileTransferPort()
//									+ CRLF
//									+ V06_X_ULTRAPEER
//									+ V06_AGENT_HEADER
//									+ CRLF
//									+ connectionData.getServentIdForHandshake()
//									+ CRLF + CRLF;
//						} else {
//							response =
//								V06_SERVER_READY
//									+ V06_LISTEN_IP + getPublicIP() + ":" + Integer.toString(connectionData.getIncomingPort())
//									+ CRLF
//									+ V06_REMOTE_IP + host
//									+ CRLF
//									+ UP2P_URL_PREFIX + connectionData.getUrlPrefix()
//									+ CRLF
//									+ UP2P_DOWNLOAD_PORT + connectionData.getFileTransferPort()
//									+ CRLF
//									+ V06_AGENT_HEADER
//									+ CRLF
//									+ connectionData.getServentIdForHandshake()
//									+ CRLF + CRLF;
//						}
//
//						bufferedWriter.write(response);
//						bufferedWriter.flush();
//						LOGnormal.debug("Incoming connection: Wrote response.");
//						
//						// Read the client's response and any headers ---------------------------------------------------------------------
//						// Only if the client accepts our headers will the connect succeed
//						String line = "";
//						try {
//							while((line = bufferedReader.readLine()) != null && (!line.equals(""))) {
//								if (line.startsWith(V06_SERVER_READY_COMPARE)) {
//									// Success
//									result = true;
//								} else {
//									// Read additional headers
//									int split = line.indexOf(":"); //find : separating property name and value
//									if (split>0){
//										String prop = line.substring(0, split);
//										String value = line.substring(split+1);
//										connectionProperties.put(prop, value.trim()); //store in connectionsproperties list
//									}
//								}
//							}
//						}
//						catch (IOException ioe) {
//							LOGnormal.error("#2: " + ioe);
//						}
//						
//						if(getListenString() != null) {
//							LOGnormal.debug("Incoming connection reported listen-IP: " + getListenString());
//						}
//					}
//					else {
//						String response = V06_SERVER_REJECT + CRLF;
//						bufferedWriter.write(response);
//						bufferedWriter.flush();
//					}
//				/*}   part of removed check at the top !
//				else {
//					String response = V06_SERVER_REJECT + CRLF;
//					bufferedWriter.write(response);
//					bufferedWriter.flush();
//				}*/
//			}
//			
//			// Incoming connection in response to a push message
//			else if (request.startsWith("GIV")) {
//				pushRequest = true;
//
//				// Get servent ID as a string
//				String guidString = (request.split(".*:", 2))[1].split("/.*", 2)[0];
//
//				// Notify waiting PushResponseInterface
//				PushWaits.pushReceived(
//					guidString,
//					socket,
//					inputStream,
//					outputStream,
//					bufferedReader,
//					bufferedWriter);
//			}
//		}
//		catch (IOException ioe) {}
//
//		if ((result) && (!shutdownFlag)) {
//
//			status = STATUS_OK;
//
//			// start the asynchronous sender
//			asyncSender = new AsyncSender();
//			asyncSender.start();
//
//			// Now start monitoring network messages
//			//connectionThread = new Thread(this, "ConnectionThread");
//			//connectionThread.start();
//			//------------------------------------------
//			//Update: instead of "this" thread simply shutting down and starting a new one 
//			//to run the connection, the current thread becomes the connection thread.
//			this.run(); 
//			/*Log.getLog().logDebug*/
//			LOGnormal.debug("Incoming connection established: " + getListenString());
//			notifyListeners();
//		}
//		else {
//			
//			connectionList.removeConnection(this);
//			
//			shutdown();
//		}
//
//		return result;
//	}

	/** modify the connection type*/
	protected void setType(int connectiontype) {
		this.type = connectiontype;
		
	}

//	/**
//	 * EDITED BY: Daniel Meyers, 2004
//	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
//	 * Starts an outgoing connection to a node on the network,
//	 * does initial handshaking
//	 *
//	 * @return true for a good start, false for failure
//	 */
//	protected boolean startOutgoingConnection() {
//		boolean success = true;
//
//		status = STATUS_CONNECTING;
//
//		try {
//			if (null == socket) {
//				socket = SocketFactory.getSocket(host, port, 10000);
//				initSocket();
//			}
//
//			String remoteHost = socket.getInetAddress().getHostAddress();
//
//			connectionData.setConnectionGreeting(
//				CONNECT_STRING
//					+ V06_AGENT_HEADER
//					+ V06_X_ULTRAPEER
//					+ V06_X_ULTRAPEER_NEEDED
//					+ V06_REMOTE_IP
//					+ remoteHost + CRLF
//					+ V06_LISTEN_PORT + connectionData.getIncomingPort() 
//					+ CRLF
//					+ connectionData.getServentIdForHandshake()
//					+ CRLF + CRLF);
//			
//			LOGnormal.debug("Sending greeting to : " + host + ":" + port
//					+ "\nGreeting:\n" + connectionData.getConnectionGreeting());
//			
//			byte[] greetingData = connectionData.getConnectionGreeting().getBytes();
//			outputStream.write(greetingData, 0, greetingData.length);
//			outputStream.flush();
//
//			// Is this blocking the entire system for too long?
//			String response = bufferedReader.readLine();
//			/*Log.getLog().logDebug*/
//			LOGnormal.info("Received greeting response: " + response + " from host: " + host);
//
//			if (!response.startsWith(SERVER_READY_COMPARE)) {
//				LOGnormal.info("Connection rejection: " + host);
//				status = STATUS_FAILED;
//				success = false;
//			}
//
//			boolean foundXTryUltrapeers = false;
//			boolean foundXUltrapeer = false;
//
//			while (response != null) { ///////////////read each line of the response (headers: Listen-IP, Remote-IP, X-Try-Ultrapeers, etc.)
//
//				int split = response.indexOf(":"); //find : separating property name and value
//				if (split>0){
//					String prop = response.substring(0, split);
//					String value = response.substring(split+1);
//					
//					if (prop.startsWith(V06_X_TRY_ULTRAPEERS_COMPARE)) {
//
//						StringTokenizer ultrapeers = new StringTokenizer(value, ":, ");
//						//-------------------------------------------------------------------						
//						//expected format of *value*:
//						// guid:IP:port
//						//-------------------------------------------------------------------
//						/* Set delims to ", " to separate out the ultrapeers, and 
//						// remove one on each iteration of the loop
//						// Tokenize the host IP and port, and create a new 
//						// Ultrapeer type host*/
//						while (ultrapeers.hasMoreTokens()) {
//							String guid =ultrapeers.nextToken();
//							String ip = ultrapeers.nextToken();
//							int port = Integer.parseInt(ultrapeers.nextToken());
//							Host host =new Host(guid);
//							host.addKnownLocation(ip, port);
//							//host.setUltrapeer(true);
//							hostCache.addknownHost(host);
//						}
//
//						foundXTryUltrapeers = true;
//						//TODO: use "ultrapeers"  as relay peers.
//					}else if (prop.equalsIgnoreCase("X-Ultrapeer") && value.equalsIgnoreCase("true")) {			// Look for ultrapeer status of node
//							 
//						ultrapeer = true;
//
//						foundXUltrapeer = true;
//					}
//					connectionProperties.put(prop, value.trim()); //store in connectionsproperties list
//				}
//				
//				// Look for X-Try headers and cache them. Format= 
//				// X-Try-Ultrapeer: guid1: 1.2.3.4:1234, 2:5.6.7.8:5678\r\n
//				// X-Try-Ultrapeer: guid2: 3.2.3.1:1284, 2:5.6.7.5:5678\r\n
//				// X-Try-Ultrapeer: guid1: 1.2.3.5:1294, 2:5.6.7.0:5679\r\n
//				
//				 
//
//				/*if (foundXTryUltrapeers && foundXUltrapeer) {
//					break;
//				}*/
//
//				if (bufferedReader.ready())
//					response = bufferedReader.readLine();
//				else break;
//			}
//
//			// Check to ensure the the node isn't connecting to itself
//			if (success) {
//				if(getPublicIP() != null && getListenString() != null) {
//					String localListenString = getPublicIP() + ":" + Integer.toString(connectionData.getIncomingPort());
//					if(getListenString().equalsIgnoreCase(localListenString)
//							|| getPublicIP().equalsIgnoreCase("127.0.0.1")
//							|| getPublicIP().equalsIgnoreCase("localhost")
//							|| getPublicIP().equalsIgnoreCase("localhost.localdomain")) {
//						LOGnormal.info("Abandoning connection to: " + remoteHost + " (reported loopback listen address \""
//								+ getListenString() + "\")");
//						success = false;
//					}
//				}
//				if(getRemoteServentId()!=null){
//					String remote = getRemoteServentId();
//					if (connectionList.contains(remote)){ //duplicate connection
//						Connection other = connectionList.getConnectionByID(remote);
//						if(other.getType()==Connection.CONNECTION_INCOMING)
//							other.setType(CONNECTION_TWOWAY);
//					}
//					success=false; //abandon this connection, we already have one.
//				}
//			}
//					
//			if(success) {		
//				try {
//					bufferedWriter.write(SERVER_READY 
//							+ V06_LISTEN_IP + getPublicIP() + ":" + connectionData.getIncomingPort() 
//							+ CRLF
//							+ UP2P_URL_PREFIX + connectionData.getUrlPrefix()
//							+ CRLF
//							+ UP2P_DOWNLOAD_PORT + connectionData.getFileTransferPort()
//							+ CRLF + CRLF);
//					bufferedWriter.flush();
//					
//					LOGnormal.info("Connection started on: " + host);
//					status = STATUS_OK;
//
//					// start the async sender
//					asyncSender = new AsyncSender();
//					asyncSender.start();
//
//					// Now start monitoring network messages
//					connectionThread = new Thread(this, "ConnectionThread");
//					// Starts NodeConnection's run method
//					connectionThread.start();
//
//					notifyListeners();
//				}
//				catch (IOException ioe) {
//					LOGnormal.error("I/O Exception transmitting ack to server");
//					status = STATUS_FAILED;
//					success = false;
//				}
//			}
//		}
//		catch (Exception e) {
//			LOGnormal.error(e);
//			success = false;
//		}
//		
//		if (!success) {
//			shutdown();
//		}
//
//		return success;
//	}

	// TODO return a boolean indicating if the message was enqueued

	/**
	 * Send a priority message. Priority message are not subject to flow control
	 * dropping
	 *
	 * @param m message
	 */
	public void prioritySend(Message m) throws IOException {
		if (shutdownFlag) {
			return;
		}

		enqueueMessage(m, true);
	}

	/**
	 * Sends a Message through this connection
	 *
	 * @param m message
	 */
	public void send(Message m) throws IOException {
		if (shutdownFlag) {
			return;
		}
		LOGall.debug("About to enqueue message "+ m.toString());

		enqueueMessage(m, false);

	}

	/**
	 * Sends a message down the connection and sends any response
	 * to <code>MessageReceiver</code>
	 *
	 */
	public void sendAndReceive(Message m, MessageReceiver receiver) throws IOException {
		if (shutdownFlag) {
			return;
		}

		LOGall.debug(
			"Connection sendAndReceive: "
				+ m.getType()
				+ " to "
				+ socket.getInetAddress().getHostAddress());

		router.routeBack(m, receiver);

		prioritySend(m);
	}

	/**
	 * Adds a message to the send queue, subject to dropping due to a message
	 * backlog
	 *
	 */
	public void enqueueMessage(Message message, boolean priority) {

		if (droppedCount > (.5 * outputCount)) {
			// drop this connection, it is hung or performing at less than half our
			// send rate
			shutdown();
			return;
		}

		int type = message.getType();
		int backlogSize = messageBacklog.size();

		if (!priority) {
			switch (type) {
				case Message.PING :
					{
						if (backlogSize > ConnectionData.BACKLOG_PING_LEVEL) {
							LOGall.debug("dropping PING message");
							droppedCount++;
							return;
						}

						break;
					}

				case Message.PONG :
					{
						if (backlogSize > ConnectionData.BACKLOG_PONG_LEVEL) {
							LOGall.debug("dropping PONG message");
							droppedCount++;
							return;
						}

						break;
					}

				case Message.QUERY :
					{
						if (backlogSize > ConnectionData.BACKLOG_QUERY_LEVEL) {
							droppedCount++;
							return;
						}

						break;
					}

				case Message.QUERYREPLY :
					{
						if (backlogSize > ConnectionData.BACKLOG_QUERYREPLY_LEVEL) {
							droppedCount++;
							return;
						}

						break;
					}

				case Message.PUSH :
					{
						if (backlogSize > ConnectionData.BACKLOG_PUSH_LEVEL) {
							droppedCount++;

							// give up on this connection
							shutdown();
							return;
						}

						break;
					}
					
				case Message.RELAY :
					{
						if (backlogSize > ConnectionData.BACKLOG_RELAY_LEVEL) {
							droppedCount++;
	
							// give up on this connection
							shutdown();
							return;
						}
	
						break;
					}
			}
		}

		MessageData messageData = new MessageData(message, priority);

		if (priority) {
			synchronized (messageBacklog) { //lock object backlog while we're adding messages
			messageBacklog.add(0, messageData);
			LOGall.debug("Connection:: enqueued priority message of type + "+ message.getType());
			}
		}
		else {
			// add non-priority messages at the end of the list
			synchronized (messageBacklog) {
				// prevent race between size/add
				int currentSize = messageBacklog.size();
				messageBacklog.add(0 == currentSize ? 0 : currentSize - 1, messageData);
				LOGall.debug("Connection:: enqueued non-priority message of type + "+ message.getType());
			}
		}

		synchronized (asyncSender) {
			asyncSender.notify();
		}

	}

	/**
	 * Handles a serious error on the connection
	 *
	 */
	void handleConnectionError(Exception e) {
		LOGnormal.debug("Shutting down connection: " + remoteIpport);
		status = STATUS_FAILED;
		if (null != e) {
			LOGnormal.debug("Exception message: " + e.getMessage());
			Log.getLog().log(e);
		} else {
			LOGnormal.debug("Exception was null.");
		}
		shutdown();
	}

	/**
	 * Get the connected host's Stracciatella ID
	 *
	 * @return the GUID of the remote host, as a string.
	 */
	public String getConnectedServentIdAsString() {
		return connectedHost.getGUIDAsString();
	}
	
	/**
	 * Get the connected host's Stracciatella ID
	 *
	 * @return the GUID of the remote host, as a string.
	 */
	public GUID getConnectedServentGUID() {
		return connectedHost.getGUID();
	}
	
	/**
	 * get the IP address of the remote host
	 * @return the IP address this connection is established to
	 */
	public String getConnectedServentIP() {
		return this.remoteIpport.IP;
	}
	/**
	 * Get port for the connected host
	 *
	 * @return host port
	 */
	public int getConnectedServentPort() {
		return this.remoteIpport.port;
	}
	/**
	 * ADDED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Get the ultrapeer status of this servent
	 * 
	 * @return a boolean representing the ultrapeer status of this servent
	 */
	public boolean getUltrapeer() {
		return ultrapeer;
	}

	/**
	 * Get the current status of the connection
	 *
	 * @return status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Get the type of connection, incoming or outgoing or two-way
	 *
	 * @return connection type
	 */
	public int getType() {
		return type;
	}
	
	public boolean isIncoming(){
		if (type == CONNECTION_INCOMING||type == CONNECTION_TWOWAY){
			return true;
			
		}
		return false;
	}
	
	public boolean isOutgoing(){
		if (type == CONNECTION_OUTGOING||type == CONNECTION_TWOWAY){
			return true;
			
		}
		return false;
	}

	/**
	 * Get the message output count
	 *
	 * @return output
	 */
	public int getMessageOutput() {
		return outputCount;
	}

	/**
	 * Get the message input count
	 *
	 * @return input
	 */
	public int getMessageInput() {
		return inputCount;
	}

	/**
	 * Get the number of messages dropped on this connection
	 *
	 * @return dropcount
	 */
	public int getMessageDropCount() {
		return droppedCount;
	}

	/**
	 * Get the lenght of time the connection has lived
	 *
	 * @return time in seconds
	 */
	public int getUpTime() {
		long msLife = System.currentTimeMillis() - createTime;

		return (int) (msLife / 1000);
	}

	/**
	 * Returns the timestamp of the last send
	 *
	 * @return timestamp
	 */
	public long getSendTime() {
		return sendTime;
	}
	

	/**
	 * EDITED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Connection operation
	 */
	public void run() {
		status = STATUS_OK;
		int sequentialReadError = 0;
		// start the asynchronous sender
		asyncSender = new AsyncSender();
		asyncSender.start();


		try {
			PingMessage temp = new PingMessage();
			temp.setTTL((byte)1);
			send(temp);
			
			while (!shutdownFlag)
	START : {

				if (sequentialReadError >= ConnectionData.SEQUENTIAL_READ_ERROR_LIMIT) {
					shutdown();
					continue;
				}

				// Read a message
				//byte[] data = new byte[Message.SIZE];
				byte[] message = new byte[Message.SIZE]; //create empty array the length of the expected header

				//int i = 0;
				//while (i < data.length) {
				for (int i = 0; i < message.length; i++) {//read the message header into the array
					try {
						
						message[i] = (byte)inputStream.readByte();//  .readUnsignedByte();
						
					}
					catch (IOException io) {
						LOGnormal.debug("Read timeout, sending ping to " + this.getConnectedServentIdAsString());

						// try to recover from read timeout with a ping
						PingMessage keepAlivePing = new PingMessage();
						keepAlivePing.setTTL((byte)1);
						prioritySend(keepAlivePing);
						sequentialReadError++;
						// Wait briefly to allow data to filter through over the internet
						//waitMethod(100);
						break START;
					}
					/*catch (Exception e) {
						System.err.println("NodeConnection\r\n" + e);
						sequentialReadError = SEQUENTIAL_READ_ERROR_LIMIT + 1;
						break START;
					}*/
				}
				
				/*for (int j=0; j < data.length; j++) {
					// & 0xFF to ensure it shows up as an unsigned byte converted to the short
					message[j] = (short)(data[j] & 0xFF);
				}*/

				sequentialReadError = 0;
				
				/*We have now read the header and we send it to the Message Factory
				 */
				Message readMessage = MessageFactory.createMessage(message, this);
				//the parsed result is now stored in the Message object
				
				if (null == readMessage) {
					LOGnormal.error("MessageFactory.createMessage() returned null");
					continue;
				}

				int payloadSize = readMessage.getPayloadLength();

				//this filters out messages that do not conform to the format specified in the Gnutella protocol
				if (!readMessage.validatePayloadSize()) {  
					handleConnectionError(null);
					LOGnormal.info(
						"Received invalid message from: "
							+ getConnectedServentIdAsString()
							+ ", message type: "
							+ readMessage.getType());
					continue;
				}
				
				/*
				* We now read the rest of the message (so far we only have the header) depending on the 
				* payload length declared in the header
				*/
				if (payloadSize > 0) {
					byte[] payload = new byte[payloadSize];
					// Read the payload
					for (int p = 0; p < payloadSize; p++) {
						payload[p] = (byte)inputStream.readByte();
					}

					readMessage.addPayload(payload);
				}

				LOGall.debug("Read message from " + connectedHost.getGUIDAsString() + " : " + readMessage.toString());

				// count the i/o
				inputCount++;

				// Message is read, route it
				boolean routeOK = router.route(readMessage, this);

				if (!routeOK) {
					// indicates an overrun router, too many connections
					LOGnormal.debug("Connection shut " + "down, overrun router");
					shutdown();
					continue;
				}

				// always give an ack pong to avoid disconnection
				if (readMessage instanceof PingMessage) {
					LOGall.info("Responding to ping");
					PongMessage pong =
						new PongMessage(
							readMessage.getGUID(),
							(short)connectionData.getIncomingPort(),
							readMessage.getOriginatingConnection().getPublicIP(),
							connectionData.getMyServentIdAsGUID());
					pong.setTTL((byte)readMessage.getTTL());
					LOGall.debug("Generated pong with IP/port: " + readMessage.getOriginatingConnection().getPublicIP() 
							+ ":" + String.valueOf(connectionData.getIncomingPort()));
					send(pong);
				}
			}
		} catch (Exception e) {
			handleConnectionError(e);
		}
	}
	
	/**
	 * Message data stored in the message backlog
	 *
	 */
	private class MessageData {
		private Message message;
		private boolean priority;

		/**
		 * Constructs message data
		 *
		 * @param message STRACCIATELLA message
		 * @param priority priority messages are not subject to dropping
		 */
		MessageData(Message message, boolean priority) {
			this.message = message;
			this.priority = priority;
		}

		/**
		 * Get the message
		 *
		 * @return message
		 */
		Message getMessage() {
			return message;
		}

		/**
		 * Check if this is a priority message. Generally, messages originated by
		 * the JTella servant are considered priority messages
		 *
		 * @return priority flag
		 */
		boolean isPriority() {
			return priority;
		}
	}

	/**
	 * Provides a mechanism to send a message and handle the problem of
	 * blocking on write, due to an unresponsive servant on the connection
	 *
	 */
	private class AsyncSender extends Thread {
		private boolean shutdown = false;

		AsyncSender() {
			super("AsyncSender");
		}

		/**
		 * Get the message
		 *
		 */
		public Message getMessage() {
			int size = messageBacklog.size();

			while (0 == size && !shutdown) {
				try {
					LOGall.debug("AsyncSender waits for a new message!");
					synchronized (this) {
						wait();
					}
				}
				catch (InterruptedException ie) {}
				LOGall.debug("AsyncSender awakens!");
				size = messageBacklog.size();
			}

			if (shutdown) {
				return null;
			}
			Message m=null;
			synchronized (messageBacklog){ //lock the msg queue while we retrieve a message
				m = (messageBacklog.remove(0)).getMessage();
			}
			return m;
		}

		public void shutdown() {
			shutdown = true;
			interrupt();
		}

		public void run() {
			while (!shutdown) {
				Message message = getMessage();

				if (message != null) {
					try {
						Connection.this.sendTime = System.currentTimeMillis();
						byte[] messageBytes = message.getByteArray();
						outputCount++;

						LOGall.info("Sending message of type "+message.getType()+ " to host " + getConnectedServentIdAsString());
						LOGall.debug(message.toString());
						Connection.this.outputStream.write(messageBytes, 0, messageBytes.length);
						Connection.this.outputStream.flush();
					}
					catch (Exception e) {
						shutdown();
						LOGall.error("AsyncSender error!");
						LOGall.error("Exception: " + e);
						LOGall.error("Exception Message: " + e.getMessage());
						
					}
				}

			}
		}
	}

	/** parse the map of properties and set all the info for the connection
	 * TODO: finish this
	 * */
	public void setProperties(Map<String, String> connectionProperties) {
		for (String k: connectionProperties.keySet()){
			if (k.equals(ConnectionData.REMOTE_IP)){
				//my public IP
				
			} else if (k.equals(ConnectionData.LISTEN_IP)){
				// the IP of the connected host
				
			} else if (k.equals(ConnectionData.UP2P_DOWNLOAD_PORT)){
				// the download port for remote connection
				
			} else if (k.equals(ConnectionData.UP2P_URL_PREFIX)){
				// the http url prefix to get the right UP2P, the right application
				
			} 
		}
		
	}

	/** get the ip/port that this connection points to*/
	public IPPort getRemoteIPPort() {
		
		return this.remoteIpport;
	}
}
