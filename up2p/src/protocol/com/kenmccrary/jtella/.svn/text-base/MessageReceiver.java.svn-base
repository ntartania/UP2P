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

/**
 *  A class implementing <code>MessageReceiver</code> is capable of receiving
 *  GNUTella network messages
 *
 */
public interface MessageReceiver {
	/**
	 *  Implement to receive a message reply
	 *
	 *  @param searchReplyMessage search reply
	 */
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage);

	/**
	 *  Implement to receive a query message from the network,
	 *  this will be called for every query and subsequently should
	 *  be implement efficiently
	 *
	 *  @param searchMessage search message
	 */
	public void receiveSearch(SearchMessage searchMessage);

	/**
	 *  Implement to receive a push request
	 *
	 *  @param pushMessage request to push a file
	 */
	public void receivePush(PushMessage pushMessage);
	
	/**
	 *  Implement to receive a relay request
	 *
	 *  @param relayMessage signal to connect to a peer relay
	 *  					using the specified URL and relay identifier
	 */
	public void receiveRelay(RelayMessage relayMessage);
}
