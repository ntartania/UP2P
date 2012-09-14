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
package protocol.com.kenmccrary.jtella;

/**
 * EDITED BY: Daniel Meyers, 2004<br>
 * Provides a linkage between the Servant and JTella. ConnectionInformation is used
 * by <code>GNUTellaConnection</code> and others in determining current parameters settable
 * by the Servant application.
 */
public class ConnectionData {
	private int outgoingConnectionCount = 3; // should be set to user's preferred value 
	private int incommingConnectionCount = 3;
	private String gatewayIP = null;
	private int incommingConnectionPort = 6346;
	
	/** The port that should be advertised for file transfer requests */
	private int downloadPort = 8080;
	
	private int sharedFileCount = 0;
	private int sharedFileSize = 0;
	private String connectionGreeting = "GNUTELLA CONNECT/0.6\r\n\r\n";
	private String agentHeader = "User-Agent: JTella 0.8\r\n";
	private boolean ultrapeer = false;
	private String vendorCode;
	
	/** The URL prefix that should be advertised as a download source for this peer */
	private String urlPrefix = "";
	
	private String GServentId = "Gnutella-Servent-id: "+GNUTellaConnection.getServantIdentifierAsString();
	
	/** 
	 * The maximum number of failed connections attempts that will be made before a host is flagged
	 * as unavailable.
	 */
	private int maxFailedConnections = 3;
	
	/** Determines whether the KeepAliveThread will generate TTL=7 pings periodically */
	private boolean peerLookupEnabled = false;

	public String getServentIdForHandshake(){
		return GServentId;
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
	 * Get the requested number of incomming connection
	 *
	 * return requested incomming connection count
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
	 *
	 * @return connection greeting
	 */
	public String getConnectionGreeting() {
		return connectionGreeting;
	}

	/**
	 * Set the connection handshake greeting. Using alternate greetings can
	 * create private greetings. The default value is 
	 * GNUTELLA CONNECT/0.4\n\n
	 *
	 * @param greeting
	 */
	public void setConnectionGreeting(String greeting) {
		connectionGreeting = greeting;
	}

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
}
