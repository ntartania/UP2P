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

import java.net.Socket;
import java.io.IOException;
import java.util.Vector;

/**
 *  A <code>InformationConnection</code> is formed in order to provide a
 *  servant on the network with Host information. The connection will return
 *  the IP address of 10 hosts on the network if possible.
 *
 */
class InformationConnection extends Connection {
	//private HostCache hostCache;

	/**
	 *  Construct the connection with an existing socket
	 *
	 *  @param router message router
	 *  @param socket socket connection to another servant
	 */
	InformationConnection(
		Router router,
		Socket socket,
		ConnectionData connectionData,
		ConnectionList connectionList,
		Vector listeners,
		HostCache hostCache)
		throws IOException {
			
		super(router, hostCache, socket, connectionData, connectionList, listeners);
		//this.hostCache = hostCache;
	}

	@Override
	public void run() {
		//do nothing
	}
}
