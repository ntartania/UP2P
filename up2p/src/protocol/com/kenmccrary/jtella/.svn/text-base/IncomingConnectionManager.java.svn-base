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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import java.io.IOException;
import java.util.Vector;

import protocol.com.dan.jtella.ConnectedHostsListener;

/**
 * Manager for incomming connections
 *
 *
 */
class IncomingConnectionManager extends ConnectionManager {
	private static final int SOCKET_RETRY_COUNT = 5;
	private ServerSocket serverSocket;
	private Vector<ConnectedHostsListener> listeners;

	/**
	 *  Constructs the incoming connection manager
	 *
	 */
	IncomingConnectionManager(
		Router router,
		ConnectionList connectionList,
		ConnectionData connectionData,
		HostCache hostCache)
		throws IOException {
		super(router, connectionList, connectionData, "IncomingConnectionManager");
		this.hostCache = hostCache;
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
	 *  Main processing loop
	 *
	 */
	public void run() {
		while (!isShutdown()) {
			// clean dead connections
			int numLive = connectionList.cleanDeadConnections(NodeConnection.CONNECTION_INCOMING);
			
			

			// check for need to reduce connection count
			if (numLive > connectionData.getIncommingConnectionCount()) {
				
				connectionList.reduceActiveIncomingConnections(
					connectionData.getIncommingConnectionCount());
			}

			try {
				//wait for new connections
				//LOG.debug("Incoming connection manager: waiting for new connections.");
				Socket socket = serverSocket.accept();
				
				LOG.debug("Incoming connection manager: New incoming connection requested!");
				LOG.debug("IncomingConnectionmanager:: "+numLive + " live incoming  connections, "+connectionData.getIncommingConnectionCount()+" wanted.");
				// TODO: Might need to keep references to the generated
				// threads in this section so they can be interrupted
				// for clean shutdown. However, these threads should be short
				// lived enough that the waiting time would be negligible
				// anyway.
				
				if (!isShutdown()) {
					//if () {
						final NodeConnection connection =
							new NodeConnection(
								router,
								hostCache,
								socket,
								connectionData,
								connectionList,
								listeners);

						// accept the new connection
						if(numLive < connectionData.getIncommingConnectionCount()){
						new Thread(new Runnable() {
							@Override
							public void run() {
								LOG.debug("Incoming connection manager: accepting new incoming connection.");
								connectionList.addConnection(connection);
								connection.startIncomingConnection(false);
							}
						}).start();
						} else // start it but reject it !
							new Thread(new Runnable() {
								@Override
								public void run() {
									LOG.debug("Incoming connection manager: accepting new incoming connection.");
									
									connection.startIncomingConnection(true);
								}
							}).start();
					
					
				}
			}
			catch (IOException io) {
				//socket timeout, will happen every 5 seconds.
				
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
