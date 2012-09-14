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
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
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
	
	// Name of logger used
	public static final String LOGGER = "protocol.jtella";
	// Instance of logger
	public static Logger LOG = Logger.getLogger(LOGGER);
	
	
	OutgoingConnectionManager(ConnectionList cl){
		connectionList = cl;
	}
	
	private Vector<ConnectedHostsListener> listeners;

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
	
	/**
	 * Adds a listener to this connection manager
	 *
	 */
	public void addListener(ConnectedHostsListener listener) {
		listeners.add(listener);
	}
	
	

	/**
	 * schedule a connection to a host
	 * @param h
	 * @param waitingtime delay until we try connecting. 0 for immediate attempts.
	 */
	public void scheduleConnection(Host h, long waitingtime){
		timer.schedule(new HostConnectionTask(h), waitingtime);
	}
	
	/**
	 * Attempts to add an immediate connection, opening a slot if needed
	 *
	 * @param ipAddress host IP address
	 * @param port port number
	 */
	private void attemptConnection(Host host) {
		
		ConnectionStarter starter = starterPool.getStarter();
		starter.setHost(host);
	}

	/**
	 * Asynchronously attempts to start a connection
	 *
	 */
	class ConnectionStarter extends Thread {
		private Host host;
		private ConnectionData connectionData;
		private StarterPool starterPool;
		private boolean shutdown;

		ConnectionStarter(
			StarterPool starterPool,
			ConnectionData connectionData) {
			super("ConnectionStarter");
			this.connectionData = connectionData;
			this.starterPool = starterPool;
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
		void setHost(Host host) {
			this.host = host;
			synchronized (this) {
				notify();
			}
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
						List<IPPort> trylocations = host.getKnownLocations();
						Iterator<IPPort> iter = trylocations.iterator();
						boolean success = false;
						while (iter.hasNext() && !shutdown && !success){
							success=connectionList.startOutgoingConnection(iter.next());
						}
						if (!success)
							scheduleConnection(host,RETRY_TIME);
						
						host = null;
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
		
		public HostConnectionTask(Host h){
			this.h=h;
		}
		
		@Override
		public void run() {
			attemptConnection(h);
			
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

	public void shutdown() {
		timer.cancel();
		
	}
}
