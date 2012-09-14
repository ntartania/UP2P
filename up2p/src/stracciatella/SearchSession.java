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

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;


import protocol.com.kenmccrary.jtella.util.Log;
import stracciatella.Connection;
import stracciatella.message.Message;
import stracciatella.message.MessageReceiver;
import stracciatella.message.PushMessage;
import stracciatella.message.SearchMessage;
import stracciatella.message.SearchReplyMessage;
import stracciatella.routing.Router;

/**
 *  A session for initiating searches on the network
 *
 */
public class SearchSession {
	private MessageReceiver receiver;
	private StracciatellaConnection connection;
	private Router router;
	private SendThread sendThread;
	private String query;
	private int queryType;
	//private int maxResults; //not used at this point
	private int minSpeed;
	private List<GUID> searchGUIDList;

	public SearchSession(
		String query,
		int queryType,
		int maxResults,
		int minSpeed,
		StracciatellaConnection connection,
		Router router,
		MessageReceiver receiver) {
			this.connection = connection;
			this.receiver = receiver;
			this.router = router;
			this.query = query;
			this.queryType = queryType;
			//this.maxResults = maxResults;
			this.minSpeed = minSpeed;
			searchGUIDList = Collections.synchronizedList(new LinkedList<GUID>());
			sendThread = new SendThread();
			sendThread.start();
	}
	
	/**
	 * Fetches the query for this search session.
	 * @return	The query for this search session
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * Fetches the query type for this search session.
	 * @return	The query type for this search session (see constants in SearchMessage)
	 */
	public int getQueryType() {
		return queryType;
	}
	
	/**
	 * Fetches the minimum accepted response speed for this search session.
	 * @return	The minimum accepted response speed for this search session
	 */
	public int getMinSpeed() {
		return minSpeed;
	}
	
	/**
	 * Generates the Gnutella message that should be sent to the network for this query.
	 * @return The Gnutella message that should be sent to the network for this query
	 */
	protected Message generateNetworkMessage() {
		return new SearchMessage(getQuery(), getQueryType(), getMinSpeed());
	}

	/**
	 *   Request a replying servant push a file
	 *
	 *   @param searchReplyMessage search reply containing file to push
	 *   @param pushMessage push message
	 *   @return true if the message could be sent
	 */
	public static boolean sendPushMessage(
		SearchReplyMessage searchReplyMessage,
		PushMessage pushMessage) {
		// the push message will be sent on the connection the searchReply
		// arrived on if it is available
		Log.getLog().logDebug("In sendPushmessage");
		Connection connection = searchReplyMessage.getOriginatingConnection();

		/*Log.getLog().logInformation*/
		System.out.println("qr connection status: " + connection.getStatus());
		if (connection.getStatus() == Connection.STATUS_OK) {
			try {

				Log.getLog().logDebug("Sending push");
				connection.prioritySend(pushMessage);
				return true;
			}
			catch (IOException io) {}
		}

		return false;
	}

	/**
	 *  Close the session, ignore future query hits
	 *
	 */
	public void close() {
		// When the session closes, clean out the origination information
		// stored for this session
		router.removeMessageSender(searchGUIDList);
		sendThread.shutdown();
	}

	/**
	 *  Continuously monitors for newly formed active connections to send
	 *  to
	 *
	 */
	class SendThread extends Thread {
		private LinkedList<Connection> sentConnections;
		private boolean shutdown;

		SendThread() {
			super("SearchSession$SendThread");
			sentConnections = new LinkedList<Connection>();
			shutdown = false;
		}

		/**
		 *  Cease operation
		 */
		void shutdown() {
			shutdown = true;
			interrupt();
		}

		/**
		 *  Working loop
		 *
		 */
		public void run() {
			while (!shutdown) {
				try {
					// Get all the active connections
					List<Connection> activeList =
						connection.getActiveConnections();

					// Check if any new connections exists
					activeList.removeAll(sentConnections);

					for (int i = 0; i < activeList.size(); i++) {
						Connection nodeConnection =
							(Connection)activeList.get(i);
						//if we are doing "one-way routing" (social network-like) we only forward the message to our outgoing / two-way connections 
						if (!(router.isOneWay() && nodeConnection.getType() == Connection.CONNECTION_INCOMING)){
							Message searchMessage = generateNetworkMessage();
							searchGUIDList.add(searchMessage.getGUID());
							nodeConnection.sendAndReceive(searchMessage, receiver);
						}
						sentConnections.add(nodeConnection);
					}

					sleep(5000);
				}
				catch (Exception e) {
					// keep running
				}
			}
		}
	}
}
