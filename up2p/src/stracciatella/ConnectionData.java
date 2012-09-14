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

import java.util.Vector;

import protocol.com.dan.jtella.ConnectedHostsListener;


/**
 * EDITED BY: Daniel Meyers, 2004<br>
 * Provides a linkage between the Servant and JTella. ConnectionData is used
 * by <code>GNUTellaConnection</code> and others in determining current parameters settable
 * by the Servant application.
 */
public class ConnectionData {
	
	/**
	 * Backlog level which will drop ping messages
	 */
	public  final static int BACKLOG_PING_LEVEL = 5;

	/**
	 * Backlog level which will drop pong messages
	 */
	public final static int BACKLOG_PONG_LEVEL = 10;

	/**
	 * Backlog level which will drop query messages
	 */
	public final static int BACKLOG_QUERY_LEVEL = 15;

	/**
	 * Backlog level which will drop query reply messages
	 */
	public final static int BACKLOG_QUERYREPLY_LEVEL = 20;

	/**
	 * Backlog level which will drop push messages
	 */
	public final static int BACKLOG_PUSH_LEVEL = 25;
	
	/**
	 * Backlog level which will drop relay messages
	 */
	public final static int BACKLOG_RELAY_LEVEL = 25;
	
	public static final int SEQUENTIAL_READ_ERROR_LIMIT = 5;



	public static String SERVER_READY = "STRACCIATELLA OK\n\n";
	public static String SERVER_READY_COMPARE = "STRACCIATELLA OK";
	public static String SERVER_REJECT = "STRACCIATELLA Reject\n\n";
	public static String SERVER_ACCEPT_DUPLICATE = "STRACCIATELLA DUPLICATE\n\n";
	public static String CONNECT_STRING = "STRACCIATELLA CONNECT/0.1\n\n";
	public static String CONNECT_STRING_COMPARE = "STRACCIATELLA CONNECT/0.1";
	//private static String V06_CONNECT_STRING = "STRACCIATELLA CONNECT/0.6\r\n";
	//private static String V06_CONNECT_STRING_COMPARE = "STRACCIATELLA CONNECT/0.6";
	//private static String V06_SERVER_READY = "STRACCIATELLA/0.6 200 OK\r\n";
	//private static String V06_SERVER_READY_COMPARE = "STRACCIATELLA/0.6 200";
	//public static String V06_SERVER_REJECT = "STRACCIATELLA/0.1 503 Service Unavailable\r\n";
	// These 2 are set by the ConnectionData instance on instantiation of
	// this class 
	public static String AGENT_HEADER ="User-Agent:";
	public static String X_ULTRAPEER = "X-Ultrapeer: false\r\n"; //TODO: set to true if relevant
	
	/**
	 * This custom (non-standard Gnutella) header field is used to exchange
	 * URL prefixes for download locations.
	 */
	public static String UP2P_URL_PREFIX = "Up2p-Url-Prefix: ";
	
	/**
	 * This custom (non-standard Gnutella) header field is used to exchange
	 * download ports for download locations.
	 */
	public static String UP2P_DOWNLOAD_PORT = "Up2p-Download-Port: ";
	
	
	
	/*
	 *connection property headers: 
	 */
	public static String LISTEN_IP = "Listen-IP: ";
	public static String LISTEN_PORT = "Listen-Port: ";
	public static String REMOTE_IP = "Remote-IP: ";
	//private static String V06_X_ULTRAPEER_NEEDED = "X-Ultrapeer-Needed: true\r\n";
	protected static String V06_X_TRY_ULTRAPEERS_COMPARE = "X-Try-Ultrapeers:";
	protected static String CRLF = "\r\n";
	public static int MAX_HEADER_SIZE = 4096;
	
	
	
	private int outgoingConnectionCount = 30; // should be set to user's preferred value 
	private int incommingConnectionCount = 30;
	private String gatewayIP = null;
	private int incommingConnectionPort = 6346;
	
	/** The port that should be advertised for file transfer requests */
	private int downloadPort = 8080;
	
	private int sharedFileCount = 0;
	private int sharedFileSize = 0;
	//private String connectionGreeting = "GNUTELLA CONNECT/0.6\r\n\r\n";
	private String agentHeader = "User-Agent: UP2P 4.0\r\n";
	private boolean ultrapeer = false;
	private String vendorCode;
	
	/** The URL prefix that should be advertised as a download source for this peer */
	private String urlPrefix = "";
	
	
	
	public final static String SERVENTID_HEADER= "Straciatella-Servent-id";
	/** 
	 * The maximum number of failed connections attempts that will be made before a host is flagged
	 * as unavailable.
	 */
	private int maxFailedConnections = 3;
	
	/** Determines whether the KeepAliveThread will generate TTL=7 pings periodically */
	private boolean peerLookupEnabled = false;

	public String getMyServentIdAsString(){
		return StracciatellaConnection.getServentIdentifier().toRawString();
	}

	public GUID getMyServentIdAsGUID(){
		return StracciatellaConnection.getServentIdentifier();
	}
	
	/**
	 * Returns the requested number of outgoing connections
	 *
	 * @return requested outgoing connection count
	 */
	public int getOutgoingConnectionCount() {
		return outgoingConnectionCount;
	}

	/**
	 * Set the requested number of outgoing connections.
	 * Defaults to three connections
	 *
	 * @param count count of desired output connections
	 */
	public void setOutgoingConnectionCount(int count) {
		outgoingConnectionCount = count;
	}

	/**
	 * Get the max number of incoming connections
	 *
	 * @return requested incoming connection count
	 */
	public int getIncommingConnectionCount() {
		return incommingConnectionCount;
	}

	/**
	 * Set the requesting number of incomming connections.
	 * Defaults to three connections
	 *
	 * @param count count of requested incomming connections
	 */
	public void setIncommingConnectionCount(int count) {
		incommingConnectionCount = count;
	}
	
	/**
	 * Set whether the keep alive thread should generate TTL = 7
	 * PING messages for the purpose of automatic peer discovery.
	 * 
	 * @param enabled	True if automatic peer discovery should be enabled
	 */
	public void setPeerLookupEnabled(boolean enabled) {
		peerLookupEnabled = enabled;
	}
	
	/**
	 * @return	True if automatic peer discovery is enabled, false if not
	 */
	public boolean getPeerLookupEnabled() {
		return peerLookupEnabled;
	}

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Get the IP of this machine's gateway to the internet
	 *
	 * return IP of this machine's gateway to the internet,
	 * or null if unknown
	 */
	public String getGatewayIP() {
		return gatewayIP;
	}

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Set the value of the IP of this machine's gateway to the internet
	 *
	 * @param gatewayIP The IP of this machine's gateway to the internet
	 */
	public void setGatewayIP(String gatewayIP) {
		this.gatewayIP = gatewayIP;
	}

	/**
	 * Get the port to used for incoming connections
	 * Defaults to 6346.
	 * This property must be set appropriately before constructing
	 * <code>NetworkConnection</code>
	 *
	 * @return port number
	 */
	public int getIncomingPort() {
		return incommingConnectionPort;
	}

	/**
	* Set the port to used for incomming connections.
	* The port number defaults to 6346.
	* This property must be set appropriately before constructions
	* <code>NetworkConnection</code>
	*
	* 
	*/
	public void setIncomingPort(int port) {
		incommingConnectionPort = port;
	}

	/**
	 * Get the value for shared file count
	 *
	 * @return shared file count
	 */
	public int getSharedFileCount() {
		return sharedFileCount;
	}

	/**
	 * Set the value for current number of shared files
	 * Defaults to zero
	 *
	 * @param count number of shared files
	 */
	public void setSharedFileCount(int count) {
		sharedFileCount = count;
	}

	/**
	 * Get the value for shared file size, this is the total size of shared files
	 *
	 * @return shared file size in KB
	 */
	public int getSharedFileSize() {
		return sharedFileSize;
	}

	/**
	 * Set the value for shared file size
	 * Defaults to zero
	 *
	 * @param size size of shared files in KB
	 */
	public void setSharedFileSize(int size) {
		sharedFileSize = size;
	}

	/**
	 * Get the connection handshake greeting
	 * for an new outgoing connection
	 * @param remoteHost the remote host that we are trying to connect to 
	 * @return connection greeting
	 */
	public String getOutgoingConnectionGreeting(String remoteHost) {
		return CONNECT_STRING
		+ AGENT_HEADER 
		+ X_ULTRAPEER
		+ REMOTE_IP
		+ remoteHost + CRLF
		+ LISTEN_PORT + getIncomingPort() 
		+ CRLF
		+ ConnectionData.SERVENTID_HEADER+": "+getMyServentIdAsString()
		+ CRLF + CRLF;
	}

	
	
	/*
	 * Set the connection handshake greeting. Using alternate greetings can
	 * create private greetings. The default value is 
	 * GNUTELLA CONNECT/0.4\n\n
	 *
	 * @param greeting
	 * /
	public void setConnectionGreeting(String greeting) {
		connectionGreeting = greeting;
	}*/

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Get Agent Header
	 *
	 * @return agent header
	 */
	public String getAgentHeader() {
		return agentHeader;
	}

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Set Agent Header for use in connection attempts
	 *
	 * @param agentHeader String
	 */
	public void setAgentHeader(String agentHeader) {
		this.agentHeader = "User-Agent: " + agentHeader + "\r\n";
	}

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Get Ultrapeer value for this servent
	 *
	 * @return ultrapeer
	 */
	public boolean getUltrapeer() {
		return ultrapeer;
	}

	/**
	 * ADDED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Set Ultrapeer value for this servent
	 * Update: let's not manually configure the expected number of connections.
	 * @param ultrapeer boolean
	 */
	public void setUltrapeer(boolean ultrapeer) {
		this.ultrapeer = ultrapeer;
		
		/*if (ultrapeer) {
			incommingConnectionCount = 30;
		}
		else {
			incommingConnectionCount = 3;
		}*/
	}

	/**
	 * Get Vendor code for use in QueryReply messages. Vendor code is a
	 * 4 character code that must be registered with the GDF
	 *
	 * @return vendor code
	 */
	public String getVendorCode() {
		return vendorCode;
	}

	/**
	 * Set Vendor code for use in QueryReply messages. Vendor code is a
	 * 4 character code that must be registered with the GDF
	 *
	 * @param vendorCode 4 character code
	 */
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}
	
	/** 
	 * Sets the maximum number of failed connections attempts that will be made 
	 * before a host is flagged as unavailable.
	 */
	public void setMaxFailedConnectionAttempts(int maxAttempts) {
		maxFailedConnections = maxAttempts;
	}
	
	/**
	 * @return The maximum number of failed connections attempts that will be made 
	 * before a host is flagged as unavailable.
	 */
	public int getMaxFailedConnectionAttempts() {
		return maxFailedConnections;
	}
	
	/**
	 * Sets the URL prefix that should be advertised as a download source for this peer
	 * @param urlPrefix	the URL prefix that should be advertised as a download source for this peer
	 */
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	
	/**
	 * @return	the URL prefix that should be advertised as a download source for this peer
	 */
	public String getUrlPrefix() {
		return urlPrefix;
	}
	
	/**
	 * Sets the port that should be advertised for file transfer requests
	 * @param port	The port that should be advertised for file transfer requests
	 */
	public void setFileTransferPort(int port) {
		this.downloadPort = port;
	}
	
	/**
	 * @return	The port that should be advertised for file transfer requests
	 */
	public int getFileTransferPort() {
		return downloadPort;
	}
	
	/**
	 * the string to return to an incoming connection that we accept.
	 * @param myIP
	 * @param theirIP
	 * @return
	 */
	public String getConnectionAcceptGreeting(String myIP, String theirIP) {
		
		return SERVER_READY
		//this is an incoming connection so I can advertize the same IP they used to connect before, there shouldn't be any firewall issues
		+ LISTEN_IP + myIP + ":" + Integer.toString(getIncomingPort())
		+ CRLF
		+ REMOTE_IP + theirIP
		+ CRLF
		+ UP2P_URL_PREFIX + getUrlPrefix()
		+ CRLF
		+ UP2P_DOWNLOAD_PORT + getFileTransferPort()
		+ CRLF
		+ X_ULTRAPEER
		+ AGENT_HEADER
		+ CRLF
		+ SERVENTID_HEADER + ": "+ getMyServentIdAsString()
		+ CRLF + CRLF;

	}
	public String getConnectionGreetingStep3(String myIP) {
		
		return SERVER_READY +		LISTEN_IP + myIP + ":" + getIncomingPort() 
		+ CRLF
		+ UP2P_URL_PREFIX + getUrlPrefix()
		+ CRLF
		+ UP2P_DOWNLOAD_PORT + getFileTransferPort()
		+ CRLF + CRLF;
	}
}
