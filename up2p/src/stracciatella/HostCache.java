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

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import org.apache.log4j.Logger;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


import protocol.com.kenmccrary.jtella.util.Log;
import stracciatella.message.PongMessage;

/**
 * EDITED BY: Daniel Meyers, 2003<br>
 * A cache of the known hosts on the network
 * 
 *How this works:
 * - There is a "soft" limit size (30 hosts): up to this size, hosts found in PONG messages will be added to the hostcache.
 * - Any nodes manually added by the user are in the hostcache
 * - The user can manually add an unlimited number of hosts
 * - If the hostcache gets bigger than the  "soft limit", and there are some "dynamically added" hosts in the hostcache, then the dynamically added ones are removed to make space for manually added ones.
 *
 */
public class HostCache {
	private static int MAX_CACHED_HOSTS = 30; // 30 hosts to match number of available connections
	
	private static HostCache singletonHC;

	
	private int nextHostIndex;
	private Set<Host> friends; 
	//private Set<Host> friendOf; thought it may be useful to know who's connected to me...
	private Set<Host> blacklist; //list of gnutella-id blacklisted hosts
	private static Map<GUID,Host> knownHosts; //a list of known network locations to find peers
	private Set<IPPort> knownLocations; //known locations (IPPort) of friends, bootstrap before we have the GUIDs. 
	private Set<Host> knownRelays; //a list of possible relay peers
	private boolean removingAllHosts = false;
	private ConnectionList activeConnections;
	/** Name of Logger used by this class. */
    public static final String LOGGER = "protocol.com.kenmccrary.jtella";

    /** Logger used by this class. */
    private static Logger LOG = Logger.getLogger(LOGGER);

	/**
	 * Constructs an empty HostCache
	 *
	 */
	public static HostCache getHostCache() {
		if (singletonHC ==null){
			singletonHC = new HostCache(); // TODO = get from file ? 
			knownHosts = Collections.synchronizedMap(new HashMap<GUID,Host>()); //ok checked: GUID implements hashCode/equals
		}
		return singletonHC;
	}
	
	private HostCache(){
		//all different, possibly overlapping subsets of hosts
		friends = Collections.synchronizedSet(new HashSet<Host>());
		blacklist=Collections.synchronizedSet(new HashSet<Host>());
		knownRelays = Collections.synchronizedSet(new HashSet<Host>());
		nextHostIndex = 0;
	}
	
	/** Gets a host by its GUID.
	 * This should be the primary way of creating Host objects, to ensure that only one Host object is created for each GUID.
	 * @param guid the GUID
	 * @return the new or existing Host
	 */
	public static Host getHost(GUID guid) {
		if (knownHosts.containsKey(guid)){
			return knownHosts.get(guid);
		} else {
			Host host = new Host(guid);
			knownHosts.put(guid, host);
			return host;	
		}
		
	}
	
	
	// Add to beginning, remove from beginning (get newest all the time)

	/*
	 * EDITED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Adds a host to the cache, or sets the host's state to active
	 * if it already exists in the cache (does not change the state of the
	 * passed parameter)
	 *
	 * @param host Host object representing the host to add
	 * @return true if the host was added, false if it was already present
	 * /
	public void addknownHost(Host host) {
		
		synchronized(knownHosts){		
		knownHosts.put(host.getGUID(), host);
		}
		
	}*/
	
	/**
	 * add this host to our friendlist
	 * and to the "known" list as well
	 * @param host
	 */
	public void friend(Host host){
		synchronized(friends){		
			friends.add(host);
			}
			
		// check: should always be in known hosts.
		if(!knownHosts.containsKey(host.getGUID())){
			LOG.warn("warning: HostCache.addfriend() was called on a host not in the known hosts");
			knownHosts.put(host.getGUID(), host);
		}
	}
	
	

	/**
	 * ADDED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Adds a host to the Hostcache by IP address and port
	 * 
	 * @param ipAddress A string representation of the IP address of the host to add
	 * @param port The port that the host accepts incoming connections on
	 * /
	public synchronized void addHost(String ipAddress, int port) {
		Host host = new Host(ipAddress, port, 0, 0);
		addHost(host);
	}*/

	/**
	 *  Removes a host from the cache
	 *
	 *  @param ipAddress address of host to remove
	 *  @param port port of host to remove
	 * /
	public synchronized void removeHost(String ipAddress, int port) {
		remove(new Host(ipAddress, port, 0, 0));
	}*/

	/**
	 *  Removes a host from the cache
	 *
	 *  @param host host to remove
	 */
	public synchronized void remove(Host host) {
		friends.remove(host);
	}

	/**
	 *  Removes all host from the cache, if all hosts are replying as busy
	 *  after time limit X it might be an idea to clear the cache and 
	 *  repopulate from GWebCaches instead of X-Try-Ultrapeers
	 */
	public synchronized void removeAllHosts() {
		removingAllHosts = true;
		while (!friends.isEmpty()) {
			friends.remove(0);
		}
		removingAllHosts = false;
	}

	/**
	 *  Get a list of the Hosts cached
	 *
	 *  @return host list
	 */
	public List<Host> getKnownHosts() {
		synchronized(friends){
		return new LinkedList<Host>(friends);
		}
	}

	/**
	 *  Remove a host from the cache, probably because its not responding
	 *
	 */
	public void removeHost(Host host) {
		synchronized(friends){
		friends.remove(host);
		}
	}

	/**
	 * Return the maximum number of hosts to have cached
	 *
	 */
	public int getMaxHosts() {
		return MAX_CACHED_HOSTS;
	}

	/**
	 *  Query how many hosts are cached
	 *  
	 *  @return number of hosts
	 */
	public int friendListSize() {
		synchronized(friends){
		return friends.size();
		}
	}
	
	/**
	 * @return	True if the host cache contains one or more hosts with an
	 * 			inactive state (i.e anything other than HostState.ACTIVE)
	 * /
	public boolean hasInactiveHost() {
		synchronized(friends){
		Iterator<Host> iter = friends.iterator();
		while (iter.hasNext()){
			Host host = iter.next();
			if (host.getHostState() != HostState.ACTIVE) {
				return true;
			}
		}
		}
		return false;
	}*/

	/*
	 *  Get the next host available
	 *
	 *  @return host or null if none available
	 * /
	Host nextHost() {
		if (size() == 0) {
			return null;
		}

		if(nextHostIndex > size() - 1) {
			nextHostIndex = 0;
		}
		synchronized(friends){
		Host returnHost = friends.get(nextHostIndex);
		
		nextHostIndex++;
		return returnHost;
		}
	}*/

	/**
	 *  Get an iterator of the hosts cached
	 *
	 */
	Iterator<Host> getHosts() {
		return friends.iterator();// .elements();
	}
	
	/**
	 * Checks if the specified host is present in the friendlist
	 * 
	 * @param host the host to check for
	 * 
	 */
	public boolean isFriend(Host host) {
		return friends.contains(host);
	}

	/**
	 *  Retrieve a random sample of known hosts. The returned sample may be equal
	 *  to or smaller than the requested size
	 *
	 *  @param sampleSize desired sample size
	 *  @return sample
	 */
	public Host[] getRandomSample(int sampleSize) {
		
		List<Host> toreturn = new ArrayList<Host>();

		toreturn.addAll(friends);

		if(friends.size()>sampleSize){ //awkwardly copy over the list to an array
			Collections.shuffle(toreturn); //shuffle so that the first "samplesize" are random
			toreturn = toreturn.subList(0, sampleSize-1);

		}
		return toreturn.toArray(new Host[toreturn.size()]);		
	}

	/**
	 * add a peer to the blacklist, based on its GUID.
	 * Also removes it from the friend list if it was present there
	 * @param guid the peer's GUID
	 */
	public void blacklistPeer(GUID guid){	
		blacklistHost(getHost(guid));	
	}
	
	/**
	 * add a host to the blacklist
	 * Also removes it from the friend list if it was present there.
	 * @param h the host.
	 */
	public void blacklistHost(Host h){
		blacklist.add(h);
		friends.remove(h);
	}
	
	/**
	 * remove a host from the blacklist, based on its GUID
	 * @param guid
	 */
	public void unBlacklist(GUID guid){
		blacklist.remove(getHost(guid));
	}
	
	/**
	 * returns whether a peer is on the blacklist, identified by its GUID
	 * @param remoteGUID
	 * @return true if it's in the blacklist, false otherwise.
	 */
	public boolean isBlacklisted(GUID remoteGUID) {		
		return isBlacklisted((getHost(remoteGUID)));
	}
	
	/**
	 * returns whether a host is on the blacklist
	 * @param host the host
	 * @return true if it's in the blacklist, false otherwise.
	 */

	public boolean isBlacklisted(Host h) {

		return blacklist.contains(h);
	}

	/**
	 * 
	 * @return a *copy* of the black list
	 */
	public List<String> getBlackList(){
		List<String> toreturn = new LinkedList<String>();
		for(Host h: blacklist){
			toreturn.add(h.getGUIDAsString());
		}
		
		return toreturn;
	}

	public List<Host> getFriendList() {
		
		return null;
	}

	/**
	 * remove Host from friendlist
	 * @param guid the GUID to identify the host
	 */
	public void unFriend(GUID guid) {
 
		friends.remove(HostCache.getHost(guid));
		
	}

	/** Creates an instance of a Host from a Pong message
	 * @return a host object with the GUID given in the Pong message, with a known location (IP+port) as given in the Pong message. 
	 * */
	public static Host getHost(PongMessage pongMessage) {
		Host h = getHost(pongMessage.getRemoteHostGUID());
		h.addKnownLocation(pongMessage.getIPAddress(), pongMessage.getPort());
		return h;
	}

	/**
	 * adds a location (IP+port) where a "friend" peer is expected to be found.
	 * This method is necessary to bootstrap the system and join the network without knowing the GUID of other hosts.
	 * @param ipAddress
	 * @param port
	 */
	public void addFriendLocation(String ipAddress, int port) {
		knownLocations.add(new IPPort(ipAddress, port));
		
	}

	
	


}
