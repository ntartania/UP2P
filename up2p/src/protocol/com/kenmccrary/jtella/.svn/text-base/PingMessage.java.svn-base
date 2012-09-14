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
 *  GNUTella Ping message
 *
 *
 */
public class PingMessage extends Message {
	/**
	 *  Ping message queries for hosts
	 *  No payload on this message
	 *
	 */
	public PingMessage() {
		super(Message.PING);
	}

	/**
	 *  Construct a PingMessage from network data
	 *
	 *  @param rawMessage binary data from a connection
	 *  @param originatingConnection Connection creating this message
	 */
	PingMessage(short[] rawMessage, Connection originatingConnection) {
		super(rawMessage, originatingConnection);
	}

}
