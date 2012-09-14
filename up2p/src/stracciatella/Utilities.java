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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;


/**
 * EDITED BY: Daniel Meyers, 2003<br>
 * General purpose utilities
 *
 */
public class Utilities {
	private static byte[] clientID = null;
	private static Random rand = new Random(System.currentTimeMillis());

	/**
	 * EDITED BY: Daniel Meyers, 2003
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Generate something remotely resembling a windows guid
	 * The short this returns is cast to a byte when it is actually used
	 *
	 */
	public static byte[] generateGUID() {
		byte[] data = new byte[16];
		
		rand.nextBytes(data);
		return data;
	}

	/**
	 * Generate something resembling a guid for this host
	 * @param seed seed for randomization  
	 */
	public static byte[] generateClientIdentifier(long seed){
		
		rand.setSeed(seed); //seed with time + config port
		
		return generateGUID();
	}
	
	/**
	 * get the GUID for this host, generate it if it's not available
	 */
	public static byte[] getClientIdentifier() {
		return StracciatellaConnection.getServentIdentifier().getData();
	}

	/**
	 * Returns the client guid in the form of the wrapper GUID
	 *
	 */
	public static GUID getClientGUID() {
		return StracciatellaConnection.getServentIdentifier();
	}

	/**
	 * Gets the host address, works around byte[] getAddress()
	 * looking negative
	 *
	 * @return address
	 */
	static short[] getHostAddress() {
		short[] address = new short[4];
		try {
			InetAddress netAddress = InetAddress.getLocalHost();
			String ipAddress = netAddress.getHostAddress();

			int beginIndex = 0;
			int endIndex = ipAddress.indexOf('.');

			address[0] =
				(short)Integer.parseInt(
					ipAddress.substring(beginIndex, endIndex));

			beginIndex = endIndex + 1;
			endIndex = ipAddress.indexOf('.', beginIndex);

			address[1] =
				(short)Integer.parseInt(
					ipAddress.substring(beginIndex, endIndex));

			beginIndex = endIndex + 1;
			endIndex = ipAddress.indexOf('.', beginIndex);

			address[2] =
				(short)Integer.parseInt(
					ipAddress.substring(beginIndex, endIndex));

			beginIndex = endIndex + 1;

			address[3] =
				(short)Integer.parseInt(
					ipAddress.substring(beginIndex, ipAddress.length()));
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return address;
	}

	// test
	public static void main(String[] args) {
		byte[] guid = Utilities.generateGUID();

		System.out.println("GUID: ");
		for (int i = 0; i < guid.length; i++) {

			System.out.println(Integer.toHexString(guid[i]));
		}
	}
	
	/**
	 * Attempts to determine if the two hostname:port strings reference the
	 * same address by performing a DNS lookup. This falls back on a simple
	 * string comparison if the DNS lookup fails.
	 * 
	 * @param ipPort1	The first hostname:port string to be compared
	 * @param ipPort2	The second hostname:port string to be compared
	 * @return	True if the two addresses represent the same raw IP address
	 */
	public static boolean isSameAddress(String ipPort1, String ipPort2) {
		try {
			InetAddress address1 = InetAddress.getByName(ipPort1);
			InetAddress address2 = InetAddress.getByName(ipPort2);
			return address1.equals(address2);
		} catch (UnknownHostException e) {
			return ipPort1.equalsIgnoreCase(ipPort2);
		}
	}
}
