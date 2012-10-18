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
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import protocol.com.dan.jtella.ConnectedHostsListener;
import protocol.com.dan.jtella.HostsChangedEvent;

/**
 * EDITED BY: Daniel Meyers, 2003<br>
 * Contains the location of a host on the network
 *
 */
public class Host {
	
	
	private GUID guid; /// this is the primary identifier of a host.
	private LinkedList<IPPort> knownLocations; //IP/port combinations where we've seen this host in the past. Ideally there's just one, but not true if IP is dynamic
	//first in list is most recent known location
	//private Connection activeConnection; //null or the connection that this host is accessed through
	
	private static HostCache hostCache;
	private Set<AccessPath> foafInfo; //how do we know this host?
		
	/** The number of failed connection attempts to this host */
	private int failedAttempts;

	//visible within package, to encourage host management through Hostcache.getHost(guid)
	Host(GUID guid){
		this.guid = guid;
		knownLocations = new LinkedList<IPPort>();
		foafInfo = new HashSet<AccessPath>();
	}
	
	
	public static Host newHost(String guid){
		return  new Host(GUID.getGUID(guid.getBytes())); //the bytes of the guid are the host.
	}
	
	
	
	/** returns the GUID of this host in raw string format (hex string)*/
	public String getGUIDAsString(){
		return guid.toRawString();
	}
	

	/**
	 * add a known (i.e. possible) location for this host
	 * @param ipp the IP/port
	*/
	public void addKnownLocation(IPPort ipp) {
		if(knownLocations.contains(ipp))
			knownLocations.remove(ipp);
		// adds IP-port to known locations or moves it to the head of the list
		knownLocations.addFirst(ipp);
		
	}
	/**
	 * add a known (i.e. possible) location for this host
	 * @param IP the IP
	 * @param port the listening port
	 */
	public void addKnownLocation(String IP, int port){
		IPPort ipp = new IPPort(IP, port);
		addKnownLocation(ipp);
	}
	
	public void addAccessPath(Host friend, int distance){
		foafInfo.add(new AccessPath(friend, distance));
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
	
	/*public void setConnected(Connection c){
		this.activeConnection = c;
	}*/
	
	/** gets the latest known IP address of this host*/
	public String getLatestKnownIP(){
		if (knownLocations.isEmpty())
			return null;
		else
			return knownLocations.getFirst().IP;
	}
	
	/** get the listening port last known to be used by this host*/
	public int getLatestKnownPort() {

		return knownLocations.getFirst().port;
	}


	/**
	 * Equals comparison: beware! only compares guid strings.
	 */
	public boolean equals(Object obj) {
		
		if (obj==null)
			return false;
		
		if (!(obj instanceof Host)) {
			return false;
		}

		Host rhs = (Host)obj;

		return guid.equals(rhs.guid);
	}

	/**
	 * Use IP+port for the hashcode
	 */
	public int hashCode() {
		return new String("HOST"+guid).hashCode();
	}

	/**
	 * Get text based host information
	 */
	public String toString() {
		return new String("Host: "+guid);
	}
	
	
	
	/** a class to describe how one is acquainted (FOAF-like) to another host*/
	class AccessPath {
		public Host friend;
		public int distance;
		
		public AccessPath(Host h, int i){
			friend =h;
			distance =i;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + distance;
			result = prime * result
					+ ((friend == null) ? 0 : friend.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AccessPath other = (AccessPath) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (distance != other.distance)
				return false;
			if (friend == null) {
				if (other.friend != null)
					return false;
			} else if (!friend.equals(other.friend))
				return false;
			return true;
		}
		private Host getOuterType() {
			return Host.this;
		}
		
		
	}


/** gets the GUID of this host*/
	public GUID getGUID() {

		return guid;
	}

/**
 * get a copy of the list of known locations
 * @return a copy of the list
 */
public List<IPPort> getKnownLocations() {
	List<IPPort> toreturn = new LinkedList<IPPort>();
	Collections.copy(toreturn, knownLocations);
	return toreturn;
}




/*
	@Override
	public void hostsChanged(HostsChangedEvent he) {
		//this means a connection with this host was either created, changed, or dropped 
		Connection c = (Connection)(he.getSource());
		if (c.getStatus()==Connection.STATUS_OK && c.getRemoteServentId().equals(guid.toRawString())){
			this.activeConnection = c;
		} else
			this.activeConnection = null;
		
	}
*/
	
}
