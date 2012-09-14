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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * EDITED BY: Daniel Meyers, 2003<br>
 * Contains the location of a host on the network
 *
 */
public class Host {
	/** 
	 * Possible values for the hostState attribute
	 * ACTIVE: Outgoing connection attempts should be made to this host if
	 *         no existing connection exists.
	 * KNOWN: The host is known to exist on the network, but no outgoing
	 *        connections will be attempted.
	 */
	public enum HostState { ACTIVE, KNOWN };
	
	private boolean manualAdd; // this indicates whether this host was manually added by the user, or dynamically from PONGs, etc.
	private String creationIpAddress;
	private String ipAddress;
	private String canonicalHostname;
	
	private int port;
	private HostState hostState;
	
	/** The number of failed connection attempts to this host */
	private int failedAttempts;

	/**
	 * This remains false as default, but can be set if required
	 */
	private boolean isUltrapeer;
	private int sharedFileCount;
	private int sharedFileSize;
	
	/**
	 * Constructs a Host which defaults to the ACTIVE state
	 *
	 * @param ipAddress IP address
	 * @param port port
	 * @param sharedFileCount count of shared files
	 * @param sharedFileSize total shared file size in KB
	 */
	public Host(String ipAddress, int port, int sharedFileCount, int sharedFileSize) {
		this(ipAddress, port, sharedFileCount, sharedFileSize, HostState.ACTIVE);
	}

	/**
	 * Constructs a Host
	 *
	 * @param ipAddress IP address
	 * @param port port
	 * @param sharedFileCount count of shared files
	 * @param sharedFileSize total shared file size in KB
	 * @param state	The initial state of the host
	 */
	public Host(String ipAddress, int port, int sharedFileCount, int sharedFileSize,
			HostState state) {
		this.creationIpAddress = ipAddress;
		this.port = port;
		this.isUltrapeer = false;
		this.sharedFileCount = sharedFileCount;
		this.sharedFileSize = sharedFileSize;
		this.hostState = state;
		this.failedAttempts = 0;
		this.manualAdd = false; // by default assume added dynamically
		
		if(ipAddress.equalsIgnoreCase("localhost") || ipAddress.equalsIgnoreCase("localhost.localdomain")) {
			// Special case for the loopback interface
			this.ipAddress = "127.0.0.1";
		} else {
			// Try to determine the raw IP and hostname for the host
			try {
				this.ipAddress = InetAddress.getByName(ipAddress).getHostAddress();
				this.canonicalHostname = InetAddress.getByName(ipAddress).getCanonicalHostName();
			} catch (UnknownHostException e) {
				// Host was unreachable
				this.ipAddress = this.creationIpAddress;
				this.canonicalHostname = this.creationIpAddress;
			}
		}
	}

	/**
	 * Constructs a Host using a Pong
	 * @param pongMessage	The pongMessage to extract host information from
	 * @param state	The initial state of the host
	 */
	public Host(PongMessage pongMessage, HostState state) {
		this(
			pongMessage.getIPAddress(),
			pongMessage.getPort(),
			pongMessage.getSharedFileCount(),
			pongMessage.getSharedFileSize(),
			state);
	}
	
	/**
	 * indicates that this host has been added manually: it should not be dropped 
	 * @param m
	 */
	public void setManual(boolean m){
		manualAdd = m;
	}
	
	/**
	 * gets whether this host was added manually or dynamically
	 * @return
	 */
	public boolean getManual(){
		return manualAdd;
	}
	
	/**
	 * @return	The current state of the cached host.
	 */
	public HostState getHostState() {
		return hostState;
	}
	
	/**
	 * Sets the state of the cached host.
	 */
	public void setHostState(HostState state) {
		this.hostState = state;
	}

	/**
	 * Returns the address
	 */
	public String getIPAddress() {
		return ipAddress;
	}
	
	/**
	 * @return	The IP address that was used to construct this host
	 * 			(Primarily used to check for duplicates in the static host
	 * 			 cache)
	 */
	public String getCreationIPAddress() {
		return this.creationIpAddress;
	}
	
	/**
	 * @return	The canonical hostname for the host as determined by a DNS
	 * 			lookup at construction time.
	 */
	public String getCanonicalHostname() {
		return this.canonicalHostname;
	}

	/**
	 * Returns the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * ADDED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Sets whether this host is an Ultrapeer
	 *
	 */
	public void setUltrapeer(boolean ultrapeer) {
		this.isUltrapeer = ultrapeer;
	}

	/**
	 * ADDED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Return true if this host is an Ultrapeer, false otherwise
	 *
	 */
	public boolean getUltrapeer() {
		return isUltrapeer;
	}

	/**
	 * Return shared file count
	 *
	 * @return file count
	 */
	public int getSharedFileCount() {
		return sharedFileCount;
	}

	/**
	 * Reurn the shared file size
	 *
	 * @return size in KB
	 */
	public int getSharedFileSize() {
		return sharedFileSize;
	}
	
	/**
	 * @return	The number of failed connection attempts to this host.
	 */
	public int getFailedAttempts() {
		return failedAttempts;
	}
	
	/** Increments the number of failed connection attempts by 1 */
	public void incrementFailedAttempts() {
		failedAttempts = failedAttempts + 1;
	}
	
	/**
	 * Sets the number of failed connection attempts to this host
	 * @param failedAttempts	The new number of failed connection attempts
	 */
	public void setFailedAttempts(int failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	/**
	 * Equals comparison
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Host)) {
			return false;
		}

		Host rhs = (Host)obj;

		return getIPAddress().equals(rhs.getIPAddress())
			&& getPort() == rhs.getPort();
	}

	/**
	 * Use IP+port for the hashcode
	 */
	public int hashCode() {
		return (getIPAddress()+String.valueOf(getPort())).hashCode();
	}

	/**
	 * Get text based host information
	 */
	public String toString() {
		return new String(ipAddress + ":" + port);
	}
}
