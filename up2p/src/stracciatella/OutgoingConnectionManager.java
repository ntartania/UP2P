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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;
//import java.util.Collections;

import protocol.com.dan.jtella.ConnectedHostsListener;


import protocol.com.kenmccrary.jtella.util.Log;

/**
 * EDITED BY: Daniel Meyers, 2003<br/>
 * Edited by Alan Davoust, 2009<br/>.
 * Manages the outgoing connections, attempts to connect
 * to the network agressively. Initiates more than required
 * connections in an attempt to quickly achieve connections
 *  
 */
class OutgoingConnectionManager {
	public static final long RETRY_TIME = 60000L;
	private Timer timer;
	private StarterPool starterPool;
	private ConnectionList connectionList;
	private HostCache hc;
	
	// Name of logger used
	public static final String LOGGER = "protocol.jtella";
	// Instance of logger
	public static Logger LOG = Logger.getLogger(LOGGER);
	
	private Map<Host, TimerTask> scheduledConnections;
	
	public OutgoingConnectionManager(ConnectionList cl, HostCache hc){
		connectionList = cl;
		starterPool = new StarterPool(null);
		scheduledConnections = new HashMap<Host, TimerTask>();
		//listeners = new Vector<ConnectedHostsListener>(1,1); //TODO: how is this used?
	}
	
	//private Vector<ConnectedHostsListener> listeners;

	/*
	 * Constructs the outgoing connection manager
	 *
	 * /
	OutgoingConnectionManager(
		Router router,
		HostCache hostCache,
		ConnectionList connectionList,
		ConnectionData connectionData)
		throws IOException {
		super(
			router,
			connectionList,
			connectionData,
			"OutgoingConnectionManager");
		this.hostCache = hostCache;
		starterPool = new StarterPool(connectionData);
		listeners = new Vector<ConnectedHostsListener>(1, 1);
	}*/
	
	
	
	public void shutdown() {
		timer.cancel();
		
	}

	/**
	 * schedule a connection to a host
	 * @param h
	 * @param waitingtime delay until we try connecting. 0 for immediate attempts.
	 */
	public void scheduleConnection(Host h, long waitingtime){
		TimerTask task = new HostConnectionTask(h);
		timer.schedule(task , waitingtime);
		
	}
	
	
	
	/**
	 * notify that a specific host is known to be online at a specific IP/port location 
	 * @param h host
	 * @param ipp optional: the IP/port location where the host was found (may be null)
	 */
	public void notifyHostOnline(Host h, IPPort ipp){
		TimerTask task = new HostConnectionTask(h, ipp);
		timer.schedule(task , 0);
		
	}

	/**
	 * Asynchronously attempts to start a connection
	 *
	 */
	class ConnectionStarter extends Thread {
		private Host host;
		private IPPort firstLocation;
		private ConnectionData connectionData;
		private StarterPool starterPool;
		private boolean shutdown;

		ConnectionStarter(
			StarterPool starterPool,
			ConnectionData connectionData) {
			super("ConnectionStarter");
			this.connectionData = connectionData;
			this.starterPool = starterPool;
			firstLocation = null;
			host = null;
		}

		void shutdown() {
			shutdown = true;
			interrupt();
		}

		/**
		 * Set the host for this connection starter to work on
		 *
		 * @param host host to work on
		 */
		public void setHost(Host host) {
			this.host = host;
			synchronized (this) {
				notify();
			}
		}
		
		/**
		 * set the first IP/Port location to look. Used when a host is actually found online, 
		 * to connect where it is found
		 * @param ipp
		 */
		public void setFirstLocation(IPPort ipp) {
			firstLocation = ipp;
		}

		/**
		 * Run the starter
		 */
		public void run() {
			while (!shutdown) {
				while (null == host) {
					synchronized (this) {
						try {
							wait();
						}
						catch (InterruptedException e) {
							break;
						}
					}
				}

				if (null != host) {
					try {
						boolean success = false;
						List<IPPort> trylocations = host.getKnownLocations();
						Iterator<IPPort> iter = trylocations.iterator();
						//try this ip-port location in priority
						if (firstLocation !=null)
							success=connectionList.startOutgoingConnection(iter.next());
						
						while (iter.hasNext() && !shutdown && !success){
							success=connectionList.startOutgoingConnection(iter.next());
						}
						if (!success)
							scheduleConnection(host,RETRY_TIME);
						
						host = null;
						firstLocation =null;
						starterPool.putStarter(this);
					}
					catch (Exception e) {
						LOG.error(e);
					}
				}
			}
		}

		
	}
	
	private class HostConnectionTask extends TimerTask{

		private Host h;
		private IPPort ipp;
		
		public HostConnectionTask(Host h){
			this.h=h;
		}
		
		public HostConnectionTask(Host h, IPPort ipp){
			this.h =h;
			this.ipp=ipp;
		}
		
		@Override
		public void run() {
			
			//first check that we're not already connected to this peer 
			//(in case we had a notification and opened an immediate connection)
			if (connectionList.hasOutgoingConnectionTo(h.getGUID()))
				return;
			
			ConnectionStarter starter = starterPool.getStarter();
			if(ipp !=null)
				starter.setFirstLocation(ipp);
			starter.setHost(h);
					
		}
	
	}

	/**
	 * Pool of connection starters
	 *
	 */
	private class StarterPool {
		private List<ConnectionStarter> starterList;
		private ConnectionData connectionData;
		private boolean shutdown = false;

		/**
		 * Construct the starter pool and populate it
		 *
		 */
		public StarterPool(ConnectionData connectionData) {
			starterList = new LinkedList<ConnectionStarter>();
			this.connectionData = connectionData;

			// Why were 4 times the number of requested outgoing connections worth of
			// threads being created here? Surely the system will not regularly be
			// trying to intitiate connections to 4 times the maximum number of peers at once.
			for (int i = 0;
				i < (connectionData.getOutgoingConnectionCount());
				i++) {
				ConnectionStarter starter =
					new ConnectionStarter(this, connectionData);
				starter.start();
				starterList.add(starter);
			}
		}

		/**
		 * Shutdown the StarterPool
		 */
		synchronized void shutdown() {
			shutdown = true;
			while (!starterList.isEmpty()) {
				ConnectionStarter temp =
					starterList.remove(0);
				temp.shutdown();
			}
		}

		/**
		 * Get a starter thread
		 * 
		 * @return starter thread
		 */
		synchronized ConnectionStarter getStarter() {
			if (starterList.size() == 0) {
				ConnectionStarter starter =
					new ConnectionStarter(this, connectionData);
				starter.start();
				return starter;
			}

			return starterList.remove(0);
		}

		/**
		 * Put a starter back into the pool
		 *
		 * @param connectionStarter connection starter
		 */
		synchronized void putStarter(ConnectionStarter connectionStarter) {
			if (starterList.size()
				> (connectionData.getOutgoingConnectionCount())
				|| (shutdown)) {
				// not needed
				connectionStarter.shutdown();
				return;
			}

			starterList.add(connectionStarter);
		}
	}

	/** notification from hostcache that we added this host as a friend.
	 * */
	
	public void notifyFriend(Host host) {
		scheduleConnection(host,0);
		
	}


	
}
