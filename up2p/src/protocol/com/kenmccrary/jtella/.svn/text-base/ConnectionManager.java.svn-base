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

import org.apache.log4j.Logger;

/**
 *  Base class for connection managers
 * 
 *  
 */
abstract class ConnectionManager extends Thread {
	private int desiredConnectionCount;
	protected ConnectionList connectionList;
	private boolean shutdown;
	protected ConnectionData connectionData;
	protected Router router;
	protected HostCache hostCache;
	
	// Name of logger used
	public static final String LOGGER = "protocol.jtella";
	// Instance of logger
	public static Logger LOG = Logger.getLogger(LOGGER);

	/**
	 *  Connection management constructor
	 *
	 *  @param router message router
	 *  @param connectionList list of active connections
	 *  @param connectionData connection data for the system
	 * 
	 */
	ConnectionManager(
		Router router,
		ConnectionList connectionList,
		ConnectionData connectionData,
		String type) {
			super(type);
			shutdown = false;
			this.connectionList = connectionList;
			this.connectionData = connectionData;
			this.router = router;
	}

	/**
	 *  Set the desired connection count
	 *
	 *  @param count desired connection count
	 */
	void setDesiredConnectionCount(int count) {
		desiredConnectionCount = count;
	}

	/**
	 *  Get the desired connection count
	 *
	 *  @return desired connection count
	 */
	int getDesiredConnectionCount() {
		return desiredConnectionCount;
	}

	/**
	 *  Closes all connections and stops processing
	 *
	 */
	void shutdown() {
		shutdown = true;
		interrupt();
	}

	/**
	 *  Check if the manager is shut down
	 *
	 *  @return true if shut down, false otherwise
	 */
	boolean isShutdown() {
		return shutdown;
	}

	/**
	 *  Gets the list of connections
	 *
	 *  @return connection list
	 */
	ConnectionList getConnectionList() {
		return connectionList;
	}

}
