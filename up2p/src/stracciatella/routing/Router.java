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
package stracciatella.routing;

//import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
//import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Logger;

import protocol.com.kenmccrary.jtella.util.Log;
import protocol.com.kenmccrary.jtella.util.BoundedQueue;
import stracciatella.Connection;
import stracciatella.ConnectionData;
import stracciatella.ConnectionList;
import stracciatella.GUID;
import stracciatella.HostCache;
import stracciatella.Utilities;
import stracciatella.message.Message;
import stracciatella.message.MessageReceiver;
import stracciatella.message.PongMessage;
import stracciatella.message.PushMessage;
import stracciatella.message.RelayMessage;
import stracciatella.message.SearchMessage;
import stracciatella.message.SearchReplyMessage;

/**
 *  Routes messages read from the network to appropriate
 *  Connections
 *
 */
public class Router extends Thread {
	// TODO flush dead connections from routing tables
	
	/** Name of Logger used by this class. */
    public static final String LOGGER1 = "protocol.jtella";

    /** Logger used by this class. */
    protected static Logger LOG = Logger.getLogger(LOGGER1);
    
    /** Name of Logger used by this class. */
    public static final String LOGGER2 = "protocol.pingpong.jtella";

    /** Logger used by this class. */
    protected static Logger LOG2 = Logger.getLogger(LOGGER2);


	private static int MAX_ROUTER_TABLE = 5000; // capability for routing 5000 messages
	private static byte MAX_HOPS = (byte)7;
	private static byte MAX_TTL = (byte)50;

	protected ConnectionList connectionList;
	private ConnectionData connectionData;
	private HostCache hostCache;
	private RouteTable pingRouteTable;
	protected RouteTable queryRouteTable;
	private RouteTable queryHitRouteTable;
	private RouteTable pushRouteTable;
	private OriginateTable originateTable;
	protected Vector<MessageReceiver> searchReceivers;
	private Vector<MessageReceiver> pushReceivers;
	private Vector<MessageReceiver> relayReceivers;
 	private BoundedQueue messageQueue;
	private boolean shutDownFlag;
	
	private boolean onewayconnections;

	/**
	 *  Collection of active connections to the network
	 *
	 *  @param the list of connections in the system
	 *  @param cache of available hosts in the system
	 */
	public Router(ConnectionList connectionList, ConnectionData connectionData, HostCache hostCache, boolean oneway) {

		super("RouterThread");
		this.connectionList = connectionList;
		this.connectionData = connectionData;
		this.hostCache = hostCache;
		pingRouteTable = new RouteTable(MAX_ROUTER_TABLE);
		queryRouteTable = new RouteTable(MAX_ROUTER_TABLE);
		queryHitRouteTable = new RouteTable(MAX_ROUTER_TABLE);
		pushRouteTable = new RouteTable(MAX_ROUTER_TABLE);
		originateTable = new OriginateTable(); // maps GUIDs back to the application layer component that is waiting for search responses. (Searches issued locally)
		messageQueue = new BoundedQueue(1000);
		searchReceivers = new Vector<MessageReceiver>();
		pushReceivers = new Vector<MessageReceiver>();
		relayReceivers = new Vector<MessageReceiver>();
		onewayconnections=oneway;
	}
	
	public Router(ConnectionList connectionList, ConnectionData connectionData, HostCache hostCache){
		this (connectionList, connectionData, hostCache, false);
	}

	/**
	 *  Stops the operation of the router
	 *
	 */
	public void shutdown() {
		shutDownFlag = true;
		interrupt();
	}

	/**
	 *  Routes a message, used by Connections
	 *
	 *  @return false if routing failed because of overload
	 */
	public boolean route(Message m, Connection connection) {

		if (m.getTTL() < 1) {
			// expired message, no failure signal required
			return true;
		}

		RouteMessage message = new RouteMessage(m, connection);

		boolean result = true;
		synchronized (this) {
			result = messageQueue.enqueue(message);
			LOG2.debug("Router::route:new message enqueued");
			// notify in either case, either a new message on the queue or
			// the queue is full
			notifyAll();
		}

		return result;
	}

	/**
	 *  Record a message we originate, so we can route it back
	 *
	 */
	public void routeBack(Message m, MessageReceiver receiver) {
		originateTable.put(m.getGUID(), receiver);
	}

	/**
	 *  Removes a message sender's origination data
	 *
	 *  @param messasgeGUIDs the originated message guids
	 */
	public void removeMessageSender(List<GUID> messageGUIDs) {
		Iterator<GUID> iterator = messageGUIDs.iterator();

		while (iterator.hasNext()) {
			GUID guid = (GUID)iterator.next();

			originateTable.remove(guid);
		}
	}

	/**
	 *  Adds a search listener
	 *
	 *  @param receiver search receiver
	 */
	public void addSearchMessageReceiver(MessageReceiver receiver) {
		if(!searchReceivers.contains(receiver)) {
			searchReceivers.addElement(receiver);
		}
	}

	/**
	 *  Removes a search receiver
	 *
	 *  @param receiver message receiver
	 */
	public void removeSearchMessageReceiver(MessageReceiver receiver) {
		searchReceivers.removeElement(receiver);
	}
	
	/**
	 *  Adds a relay listener
	 *
	 *  @param receiver relay receiver
	 */
	public void addRelayMessageReceiver(MessageReceiver receiver) {
		if(!relayReceivers.contains(receiver)) {
			relayReceivers.addElement(receiver);
		}
	}

	/**
	 *  Removes a relay receiver
	 *
	 *  @param receiver message receiver
	 */
	void removeRelayMessageReceiver(MessageReceiver receiver) {
		relayReceivers.removeElement(receiver);
	}

	/**
	 *  Adds a push listener
	 *
	 *  @param receiver push message receiver
	 */
	public void addPushMessageReceiver(MessageReceiver receiver) {
		if(!pushReceivers.contains(receiver)) {
			pushReceivers.addElement(receiver);
		}
	}

	/**
	 *  Removes a push receiver
	 *
	 *  @param receiver message receiver
	 */
	public void removePushMessageReceiver(MessageReceiver receiver) {
		pushReceivers.removeElement(receiver);
	}

	/**
	 *  Query the next message to route, blocks if no message are available
	 *
	 *  @return message to route
	 */
	RouteMessage getNextMessage() throws InterruptedException {
		synchronized (this) {
			while (messageQueue.empty()) {
				try {
					//LOG.debug("Router::getNextMsg: queue empty: waiting");
					wait();
				}
				catch (InterruptedException ie) {
					//ie.printStackTrace();
					if (shutDownFlag) {
						throw new InterruptedException();
					}
				}
			}
			LOG2.debug("Router::getNextMsg: I'm awake! getting next message...");
			return (RouteMessage)messageQueue.dequeue();
		}
	}

	/**
	 *  Runs along routing messages
	 *
	 */
	public void run() {

		while (!shutDownFlag) {
			try {
				RouteMessage routeMessage = getNextMessage();

				if (null == routeMessage) {
					LOG.error("Router::Null message in router");
					continue;
				}
				
				//-----------------------------------------------------------
				// Check if this is a response to a message we generated
				//-----------------------------------------------------------
				if (originateTable.containsGUID(routeMessage.getMessage().getGUID())) {
					LOG.info(
						"Routing response to originated message\r\n"
							+ routeMessage.getMessage().getGUID());

					// Retrieve the message receiver
					Message m = routeMessage.getMessage();

					MessageReceiver receiver = originateTable.get(m.getGUID());

					if (m instanceof SearchReplyMessage) {
						queryHitRouteTable.put(
								((SearchReplyMessage)m).getClientIdentifier(),
								routeMessage.getConnection());
						receiver.receiveSearchReply((SearchReplyMessage)m);
					}
					else {
						PongMessage pong =
							new PongMessage(
								m.getGUID(),
								(short)connectionData.getIncomingPort(),
								m.getOriginatingConnection().getPublicIP(),
								connectionData.getMyServentIdAsGUID());
						pong.setTTL((byte)m.getTTL());
						m.getOriginatingConnection().send(pong);
						LOG.error("Router::Routeback unknown message");
					}

					continue;
				}

				//-----------------------------------------------------------
				// Don't forward invalid messages
				//-----------------------------------------------------------
				if (!validateMessage(routeMessage.getMessage())) {
					continue;
				}

				//-----------------------------------------------------------
				// Route the network traffic to our connections
				//-----------------------------------------------------------
				dispatchNewMessage(routeMessage);
				
			}
			catch (Exception e) {
				// keep running
				LOG.error(e);
			}
		}
	}
	
	/**
	 * Calls one of the message handler functions depending on the type
	 * of message received. This should only be called for messages once it
	 * has already been verified that the message GUID has not been
	 * seen by this servent previously.
	 * 
	 * @param msg	The message to dispatch.
	 */
	protected void dispatchNewMessage(RouteMessage msg) {
		switch (msg.getMessage().getType()) {
		
		case Message.PING: {
				LOG2.info("Router::Routing ping message");
				routePingMessage(msg);
				return;
			}

		case Message.PONG: {
				LOG2.info("Router::Routing pong message");
				routePongMessage(msg);
				return;
			}

		case Message.PUSH: {
				LOG.info("Router::Routing push message");
				routePushMessage(msg);
				return;
			}
		
		case Message.RELAY: {
			LOG.info("Router::Routing relay message");
			routeRelayMessage(msg);
			return;
		}

		case Message.QUERY: {
				LOG.info("Router::Routing query message");
				routeQueryMessage(msg);
				return;
			}

		case Message.QUERYREPLY: {
				LOG.info("Router::Routing query reply message");
				routeQueryReplyMessage(msg);
				return;
			}
			
		default:
			// Do nothing if the message type is not recognized (subclasses may
			// call this method and add additional handling cases after the method
			// returns)
			return;
		}
	}

	/**
	 *  Get the source of a previously received message query message
	 *
	 *  @param message a search message
	 */
	public Connection getQuerySource(SearchMessage message) {
		return queryRouteTable.get(message.getGUID());
	}
	
	/**
	 * Get the source of a previously received query hit message based on the
	 * received client identifier.
	 * @param clientIdentifier	The client identifier of the responding query hit
	 * @return	The Connection the query hit was received on
	 */
	public Connection getQueryHitSource(GUID clientIdentifier) {
		return queryHitRouteTable.get(clientIdentifier);
	}
	
	/**
	 * Get the source of a previously received push message based on the
	 * received client identifier.
	 * @param clientIdentifier	The client identifier of the push message
	 * @return	The Connection the query hit was received on
	 */
	public Connection getPushSource(GUID clientIdentifier) {
		return pushRouteTable.get(clientIdentifier);
	}
	
	/**
	 * get whether the connections are one-way or 2-way
	 */
	public boolean isOneWay(){
		return onewayconnections;
	}

	// TODO history for routing

	/**
	 *  Routes PING messages -
	 *  RULES: Route PING messages to all connections
	 *  except originator
	 *
	 */
	void routePingMessage(RouteMessage m) {
		if (m.getConnection().getStatus() != Connection.STATUS_OK) {
			// Originating connection does not exist, so drop the message
			// If we received Pong responses, there would be no connection to
			// route them to
			LOG.debug("Connection NOK, dropping ping message");
			return;
		}
		
		if(pingRouteTable.containsGUID(m.getMessage().getGUID())) {
			LOG2.info("Ping has been previously observed (TTL = "
					+ m.getMessage().getTTL() + "), dropping the message.");
			return;
		}

		// Make a local connection list to avoid concurrency issues
		List<Connection> listcopy = connectionList.getActiveConnections();
		ListIterator<Connection> iterator = listcopy.listIterator(0);

		prepareMessage(m.getMessage());

		while (iterator.hasNext()) {
			Connection connection = (Connection)iterator.next();

			if (!connection.equals(m.getConnection())
				&& connection.getStatus() == Connection.STATUS_OK) {
				routerSend(connection, m.getMessage());
			}
		}

		// History of Pings
		pingRouteTable.put(m.getMessage().getGUID(), m.getConnection());
	}

	/**
	 *  Routes PONG messages -
	 *  RULES: Route PONG messages only to the connection
	 *  the PING arrived on
	 *
	 */
	void routePongMessage(RouteMessage m) {
		// Harvest this servant from the pong message
		PongMessage pongMessage = (PongMessage)m.getMessage();
		
		
		//if(connectionData.getPeerLookupEnabled()) {
		HostCache.getHostCache().getHostFromPONG(pongMessage); //just makes this host "known"
				

		Connection originator = pingRouteTable.get(m.getMessage().getGUID());
		if (null != originator && originator.getStatus() == Connection.STATUS_OK) {
			prepareMessage(m.getMessage());

			routerSend(originator, m.getMessage());
		}
		else {
			LOG.info("No connection for routing pong");
		}
	}

	/**
	 *  Routes QUERY messages -
	 *  RULES: Route QUERY messages to all connections
	 *  except the originator
	 *
	 */
	void routeQueryMessage(RouteMessage m) {

		if (m.getConnection().getStatus() != Connection.STATUS_OK || queryRouteTable.containsGUID(m.getMessage().getGUID())) {
			// Originating connection does not exist, so drop the message
			// If we received a response to the query, there would be no
			// connection to route it to
			
			// OR ELSE: the message has already been seen. don't forward or modify the routing table!!
			return;
		}

		// Make a local connection list to avoid concurrency L
		List<Connection> listcopy = connectionList.getActiveConnections();
		ListIterator<Connection> iterator = listcopy.listIterator(0);

		prepareMessage(m.getMessage());

		while (iterator.hasNext()) {
			Connection connection = (Connection)iterator.next();

			if (!connection.equals(m.getConnection())
				&& connection.getStatus() == Connection.STATUS_OK) {
				
				//alan 2012-08-29: make connections one-way:
				if (!(onewayconnections && connection.getType()== Connection.CONNECTION_INCOMING) )
					routerSend(connection, m.getMessage());

			}
		}

		// History of Queries
		queryRouteTable.put(m.getMessage().getGUID(), m.getConnection());

		// inform any search listener
		if (0 != searchReceivers.size()) {
			fireSearchMessage((SearchMessage)m.getMessage());
		} else {
			LOG.debug("no search receivers to fire the search message!");
		}
	}

	/**
	 *  Routes QUERY REPLY messages -
	 *  Rules: Route Query Replys to the connection
	 *  which had the Query
	 *
	 */
	void routeQueryReplyMessage(RouteMessage m) {
		Connection originator = queryRouteTable.get(m.getMessage().getGUID());

		if (null != originator && originator.getStatus() == Connection.STATUS_OK) {
			prepareMessage(m.getMessage());

			routerSend(originator, m.getMessage());

			// Record query hit route, for push forwarding
			// Push messages are routed by Servant ID
			queryHitRouteTable.put(
				((SearchReplyMessage)m.getMessage()).getClientIdentifier(),
				m.getConnection());
		}
		else {
			LOG.info("No connection for routing query reply");
		}
	}

	/**
	 *  Routes PUSH messages -
	 *  Rules: Route PUSH on the connection that had the QUERYHIT
	 *
	 *
	 */
	void routePushMessage(RouteMessage m) {

		PushMessage pushMessage = (PushMessage)m.getMessage();
		
		// Keep track of the origin so RELAY messages can be routed back
		// WARNING: This needs to happen before the call to firePushMessage() to ensure
		// that relay messages are handled correctly.
		pushRouteTable.put(pushMessage.getSourceIdentifier(), m.getConnection());
		
		if (0 != pushReceivers.size()
			&& Utilities.getClientGUID().equals(pushMessage.getTargetIdentifier())) {
			LOG.debug("Got PUSH message intended for local peer.");
			// this is a push request for the JTella servant
			firePushMessage(pushMessage);
			return;
		}
		
		Connection originator = queryHitRouteTable.get(pushMessage.getTargetIdentifier());

		if (null != originator) {
			prepareMessage(pushMessage);
			routerSend(originator, pushMessage);
		}
		else {
			LOG.info("No connection for routing push");
		}
	}
	
	/**
	 *  Routes RELAY messages -
	 *  Rules: Route RELAY on the connection that had the PUSH
	 */
	void routeRelayMessage(RouteMessage m) {
		RelayMessage relayMessage = (RelayMessage)m.getMessage();
		if (0 != relayReceivers.size()
			&& Utilities.getClientGUID().equals(relayMessage.getTargetIdentifier())) {
			LOG.debug("Got RELAY message intended for local peer.");
			// this is a push request for the JTella servant
			fireRelayMessage(relayMessage);
			return;
		}
		
		Connection originator = pushRouteTable.get(relayMessage.getTargetIdentifier());

		if (null != originator) {
			prepareMessage(relayMessage);
			routerSend(originator, relayMessage);
		}
		else {
			LOG.info("No connection for routing relay");
		}
	}

	/**
	 *  Updates a message for sending
	 *
	 *  @param message message to update
	 */
	protected void prepareMessage(Message message) {
		message.setTTL((byte) (message.getTTL() - 1));
		message.setHops((byte) (message.getHops() + 1));
	}

	/**
	 *  Utility method for common send
	 *
	 *  @param connection connection
	 *  @param message message
	 */
	protected boolean routerSend(Connection connection, Message message) {
		try {
			connection.send(message);
		}
		catch (IOException io) {
			Log.getLog().log(io);
		}

		return true; // TODO fix
	}

	/**
	 *  Performs some validation against network trafic
	 *
	 *  @return true if the message is acceptable, false otherwise
	 */
	boolean validateMessage(Message m) {

		//---------------------------------------------------------------
		//  The idea is to limit trafic, making sure hops doesn't exceed
		//  a reasonable amount - 7
		//---------------------------------------------------------------
		if (m.getHops() > MAX_HOPS) {
			LOG.info("Router dropped message exceeding max hops");
			return false;
		}

		if (m.getTTL() > MAX_TTL) {
			LOG.info("Router dropped message exceeding max ttl");
			return false;
		}

		if (m.getTTL() > MAX_HOPS && m.getTTL() < MAX_TTL) {
			LOG.info("Router adjusted message ttl to 7");
			m.setTTL(MAX_HOPS);
		}

		if ((m.getTTL() + m.getHops()) > MAX_HOPS) {
			LOG.info("Router adjusted message ttl to 7");
			m.setTTL((byte) (MAX_HOPS - m.getHops()));
		}

		return true;
	}

	/**
	 *  Sends a search message to listeners
	 *
	 *  @param searchMessage search message to send
	 */
	protected void fireSearchMessage(SearchMessage searchMessage) {
		Vector<MessageReceiver> v = (Vector<MessageReceiver>)searchReceivers.clone();
		Enumeration<MessageReceiver> e = v.elements();

		while (e.hasMoreElements()) {
			MessageReceiver receiver = (MessageReceiver)e.nextElement();
			receiver.receiveSearch((SearchMessage)searchMessage);
		}

	}

	/**
	 *  Sends a push message to listener(s)
	 *
	 *  @param pushMessage push message to send
	 */
	void firePushMessage(PushMessage pushMessage) {
		Vector<MessageReceiver> v = (Vector<MessageReceiver>)pushReceivers.clone();
		Enumeration<MessageReceiver> e = v.elements();

		while (e.hasMoreElements()) {
			MessageReceiver receiver = (MessageReceiver)e.nextElement();
			receiver.receivePush((PushMessage)pushMessage);
		}

	}
	
	/**
	 *  Sends a relay message to listener(s)
	 *
	 *  @param relayMessage relay message to send
	 */
	void fireRelayMessage(RelayMessage relayMessage) {
		Vector<MessageReceiver> v = (Vector<MessageReceiver>)relayReceivers.clone();
		Enumeration<MessageReceiver> e = v.elements();

		while (e.hasMoreElements()) {
			MessageReceiver receiver = (MessageReceiver)e.nextElement();
			receiver.receiveRelay((RelayMessage)relayMessage);
		}
	}

	/**
	 *  Records a message to route
	 *
	 */
	protected class RouteMessage {
		Message m;
		Connection connection;

		public RouteMessage(Message m, Connection connection) {
			this.m = m;
			this.connection = connection;
		}

		/**
		 *
		 *
		 */
		public Message getMessage() {
			return m;
		}

		/**
		 *
		 *
		 */
		public Connection getConnection() {
			return connection;
		}
	}
}
