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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import protocol.com.dan.jtella.ConnectedHostsListener;

/**
 * Manager for incomming connections
 *
 *
 */
class IncomingConnectionManager extends Thread {
	private static final int SOCKET_RETRY_COUNT = 5;
	private ServerSocket serverSocket;
	private Vector<ConnectedHostsListener> listeners;
	private ConnectionList connectionList;
	private ConnectionData connectionData;
	private boolean shutdown;
	
	// Name of logger used
	public static final String LOGGER = "protocol.jtella";
	// Instance of logger
	public static Logger LOG = Logger.getLogger(LOGGER);


	/**
	 *  Constructs the incoming connection manager
	 *
	 */
	IncomingConnectionManager(
		ConnectionList connectionList,
		ConnectionData connectionData)
		throws IOException {
		super("IncomingConnectionManager");
		this.connectionList=connectionList;
		this.connectionData=connectionData;
		serverSocket = createServerSocket();
		serverSocket.setSoTimeout(5000);
		listeners = new Vector<ConnectedHostsListener>(1, 1);
	}

	/**
	 *  Creates a <code>ServerSocket</code> using an alternate port if needed
	 *
	 *  @throws IOException if socket creation fails
	 *  @return socket 
	 */
	ServerSocket createServerSocket() throws IOException {
		ServerSocket socket = null;
		int retryCount = SOCKET_RETRY_COUNT;

		int port = connectionData.getIncomingPort();
		/*while*/
		if (null == socket) {
			try {
				socket = new ServerSocket(port);
			}
			catch (BindException be) {
				port++;
				retryCount--;

				if (0 == retryCount) {
					throw be;
				}
				// TODO mechanism to explicitly communicate the error to apply

			}
		}

		connectionData.setIncomingPort(port);
		return socket;
	}

	/**
	 *  Adds a listener to this connection manager
	 *
	 */
	public void addListener(ConnectedHostsListener listener) {
		listeners.add(listener);
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
	 *  Main processing loop
	 *
	 */
	public void run() {
		while (!isShutdown()) {
			// clean dead connections
			//int numLive = connectionList.cleanDeadConnections(Connection.CONNECTION_INCOMING);
			int numLive = connectionList.getActiveIncomingConnectionCount();
			/*// check for need to reduce connection count
			if (numLive > connectionData.getIncommingConnectionCount()) {
				
				connectionList.reduceActiveIncomingConnections(
					connectionData.getIncommingConnectionCount());
			}*/

			try {
				//wait for new connections
				//LOG.debug("Incoming connection manager: waiting for new connections.");
				Socket socket = serverSocket.accept();

				LOG.debug("Incoming connection manager: New incoming connection requested!");
				LOG.debug("IncomingConnectionmanager:: "+numLive + " live incoming  connections, "+connectionData.getIncommingConnectionCount()+" wanted.");

				if (!isShutdown()) 
					connectionList.startIncomingConnection(socket);
			}
			catch (SocketTimeoutException ste){
				//socket timeout, will happen every 5 seconds.
			}
			catch (IOException io) {
				LOG.error("Incoming Connection Manager = socket IO exception.");

			}
			
			

		}

		if (null != serverSocket) {
			try {
				serverSocket.close();
			}
			catch (Exception ignore) {}
		}
	}
}
