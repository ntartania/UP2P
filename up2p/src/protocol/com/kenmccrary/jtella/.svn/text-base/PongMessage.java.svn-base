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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

//import com.kenmccrary.jtella.util.Log;

/**
 *  A Pong message is sent in reply to a Ping and provides host information
 *
 */
public class PongMessage extends Message {
	private short port;
	private String ipAddress;
	private int fileCount;
	private int fileSize;

	/**
	 *  Construct a PONG message 
	 *
	 *  @param port listening port
	 *  @param ipAddress ipAdress for servant
	 *  @param fileCount shared file count
	 *  @param fileSize shared file size in KB
	 */
	PongMessage(short port, String ipAddress, int fileCount, int fileSize) {
		super(Message.PONG);
		init(port, ipAddress, fileCount, fileSize);
	}

	/**
	 *  Construct a PONG message 
	 *
	 *  @param GUID specific guid, generally from PING
	 *  @param port listening port
	 *  @param ipAddress ipAdress for servant
	 *  @param fileCount shared file count
	 *  @param fileSize shared file size in KB
	 */
	PongMessage(
		GUID guid,
		short port,
		String ipAddress,
		int fileCount,
		int fileSize) {
		super(guid, Message.PONG);
		init(port, ipAddress, fileCount, fileSize);
	}

	/**
	 *  Construct a PongMessage from data read from network
	 *
	 *  @param rawMessage binary data from a connection
	 *  @param originatingConnection Connection creating this message
	 */
	PongMessage(short[] rawMessage, Connection originatingConnection) {
		super(rawMessage, originatingConnection);
	}

	/**
	 *  Construct a Pong message using a Host from the cache
	 *
	 *  @param host
	 */
	PongMessage(Host host) {
		this(
			(short)host.getPort(),
			host.getIPAddress(),
			host.getSharedFileCount(),
			host.getSharedFileSize());
	}

	/**
	 *  Common init
	 *
	 *  @param GUID specific guid, generally from PING
	 *  @param port listening port
	 *  @param ipAddress ipAdress for servant
	 *  @param fileCount shared file count
	 *  @param fileSize shared file size in KB
	 *
	 */
	private void init(
		short port,
		String ipAddress,
		int fileCount,
		int fileSize) {
		this.port = port;
		try {
			this.ipAddress = InetAddress.getByName(ipAddress).getHostAddress();
		} catch (UnknownHostException e) {
			this.ipAddress = ipAddress;
		}
		this.fileCount = fileCount;
		this.fileSize = fileSize;

		buildPayload();
	}

	/*
	 *  Payload 0-1 Port (little endian)
	 *          2-5 IP address (little endian)
	 *          6-9 shared file count
	 *          10-13 total shared file size in kb
	 */

	/**
	 *  Query the IP address for this pong message
	 *  result is an IP address in the form of
	 *  "206.26.48.100".
	 *
	 *  @return IP address
	 */
	String getIPAddress() {
		StringBuffer ipBuffer = new StringBuffer();

		ipBuffer
			.append(Integer.toString(payload[2]))
			.append(".")
			.append(Integer.toString(payload[3]))
			.append(".")
			.append(Integer.toString(payload[4]))
			.append(".")
			.append(Integer.toString(payload[5]));

		return ipBuffer.toString();
	}

	/**
	 *  Query the port for this pong message
	 *
	 *  @return port
	 */
	int getPort() {
		int port = 0;
		int byte1 = 0;
		int byte2 = 0;
		byte1 |= payload[0];
		byte2 |= payload[1];
		port |= byte1;
		port |= (byte2 << 8);
		return port;
	}

	/**
	 *  Get the number of shared files
	 *
	 *  @return shared files
	 */
	int getSharedFileCount() {
		int byte1 = payload[6];
		int byte2 = payload[7];
		int byte3 = payload[8];
		int byte4 = payload[9];

		return byte1 | (byte2 << 8 | byte3 << 16 | byte4 << 24);

	}

	/**
	 *  Get the size of shared files
	 *
	 *  @return size
	 */
	int getSharedFileSize() {
		int byte1 = payload[10];
		int byte2 = payload[11];
		int byte3 = payload[12];
		int byte4 = payload[13];

		return byte1 | (byte2 << 8 | byte3 << 16 | byte4 << 24);
	}

	/**
	 *   Builds the PONG message payload
	 *
	 */
	void buildPayload() {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);

			// write the port, little endian
			int byte1 = 0x00FF & port;
			int byte2 = (0xFF00 & port) >> 8;
			dataStream.write(byte1);
			dataStream.write(byte2);

			// write the ip address

			// IP Address, little endian
			int beginIndex = 0;
			int endIndex = ipAddress.indexOf('.');

			int ip1 =
				Integer.parseInt(ipAddress.substring(beginIndex, endIndex));

			beginIndex = endIndex + 1;
			endIndex = ipAddress.indexOf('.', beginIndex);

			int ip2 =
				Integer.parseInt(ipAddress.substring(beginIndex, endIndex));

			beginIndex = endIndex + 1;
			endIndex = ipAddress.indexOf('.', beginIndex);

			int ip3 =
				Integer.parseInt(ipAddress.substring(beginIndex, endIndex));

			beginIndex = endIndex + 1;

			int ip4 =
				Integer.parseInt(
					ipAddress.substring(beginIndex, ipAddress.length()));

			dataStream.write(ip1);
			dataStream.write(ip2);
			dataStream.write(ip3);
			dataStream.write(ip4);

			// write the number of files
			int fileCountByte1 = 0x000000FF & fileCount;
			int fileCountByte2 = (0x0000FF00 & fileCount) >> 8;
			int fileCountByte3 = (0x00FF0000 & fileCount) >> 16;
			int fileCountByte4 = (0xFF000000 & fileCount) >> 24;
			dataStream.writeByte(fileCountByte1);
			dataStream.writeByte(fileCountByte2);
			dataStream.writeByte(fileCountByte3);
			dataStream.writeByte(fileCountByte4);

			// write the file size
			int fileSizeByte1 = 0x000000FF & fileSize;
			int fileSizeByte2 = (0x0000FF00 & fileSize) >> 8;
			int fileSizeByte3 = (0x00FF0000 & fileSize) >> 16;
			int fileSizeByte4 = (0xFF000000 & fileSize) >> 24;
			dataStream.writeByte(fileSizeByte1);
			dataStream.writeByte(fileSizeByte2);
			dataStream.writeByte(fileSizeByte3);
			dataStream.writeByte(fileSizeByte4);

			addPayload(byteStream.toByteArray());
			dataStream.close();

		}
		catch (IOException io) {}
	}

}
