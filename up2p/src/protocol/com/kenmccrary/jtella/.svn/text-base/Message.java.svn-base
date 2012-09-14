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

import org.apache.log4j.Logger;

//import protocol.com.kenmccrary.jtella.util.Log;

/**
 *  Abstract base class for GNUTella messages
 *
 */
public abstract class Message {
	
	/** Name of Logger used by this class. */
    public static final String LOGGER = "protocol.pingpong.jtella";

    /** Logger used by this class. */
    protected static Logger LOG = Logger.getLogger(LOGGER);
    
	public final static int SIZE = 23;

	public final static short PING = (short)0x00;
	public final static short PONG = (short)0x01;
	public final static short PUSH = (short)0x40;
	public final static short RELAY = (short)0x41;
	public final static short QUERY = (short)0x80;
	public final static short QUERYREPLY = (short)0x81;

	final static int SIZE_PING_PAYLOAD = 0;
	final static int SIZE_PONG_PAYLOAD = 14;
	final static int SIZE_RELAY_PAYLOAD = 4000;
	final static int SIZE_PUSH_PAYLOAD = 4000;	// Modified to account for an arbitrarily sized GGEP extension block
	final static int SIZE_QUERY_PAYLOAD = 4000; //Modified by Alan 2009.01.22 : the normal Gnutella query length (max 256 bytes) is too short for U-P2P.
												//We now make it 4kb.
	final static int SIZE_QUERYREPLY_PAYLOAD = 65536;

	protected Connection originatingConnection;
	protected GUID guid;
	protected short type;
	protected byte ttl;
	protected byte hops;
	protected short[] payload;
	protected int payloadSize;
	//protected short[] clientID;
	
	/**
	 *  Constructs a new message
	 *
	 *
	 *  @param type function type
	 */
	Message(int type) {
		this(new GUID(), type);
	}

	/**
	 *  Construct a message with a specific guid
	 *
	 *  @param guid
	 *  @type message type
	 */
	Message(GUID guid, int type) {
		this.guid = guid;
		this.type = (short)type;
		ttl = 7;
		hops = 0;
	}

	/**
	 *  Query the type of message
	 *
	 *  @return type
	 */
	public int getType() {
		return type;
	}

	/**
	 *  Constructs a message from data read from network
	 *
	 *  @param rawMessage bytes
	 */
	protected Message(short[] rawMessage, Connection originatingConnection) {
		this.originatingConnection = originatingConnection;
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < rawMessage.length; i++) {
			buffer.append("[" + Integer.toHexString(rawMessage[i]) + "]");

		}

		LOG.debug("Message constructor: Raw Message Bytes: " + buffer.toString());

		// Copy the GUID
		short[] guidData = new short[16];
		System.arraycopy(rawMessage, 0, guidData, 0, guidData.length);
		guid = new GUID(guidData);

		// Copy the function identifier
		type = rawMessage[16];

		// Copy the TTL 
		ttl = (byte)rawMessage[17];

		// Copy the hop count
		hops = (byte)rawMessage[18];

		// Copy the payload size (little endian)
		int byte1 = rawMessage[19];
		int byte2 = rawMessage[20];
		int byte3 = rawMessage[21];
		int byte4 = rawMessage[22];

		payloadSize += byte1;
		payloadSize += (byte2 << 8);
		payloadSize += (byte3 << 16);
		payloadSize += (byte4 << 24);
		LOG.debug("Message: payload size:"+ payloadSize);

	}

	/**
	 *  Query the GUID 
	 *
	 *  @return 16 byte array
	 */

	public GUID getGUID() {
		return guid;
	}

	/**
	 *  Apply a guid to the message
	 *
	 *  @param guid new guid
	 */
	void setGUID(GUID guid) {
		this.guid = guid;
	}

	/**
	 *  Get the Time to live for the message
	 *
	 */
	public int getTTL() {
		return ttl;
	}

	/**
	 *  Set the ttl value for the message
	 *
	 */
	void setTTL(byte ttl) {
		this.ttl = ttl;
	}

	/**
	 *  Get the hop count for this message
	 *
	 */
	 public int getHops() {
		return hops;
	}

	/**
	 *  Set the hop count for this message, seven is the recommended maximum
	 *
	 */
	void setHops(byte hops) {
		this.hops = hops;
	}

	/**
	 *  Add a payload to the message
	 *
	 *  @param payload payload for the message
	 */
	void addPayload(short[] payload) {
		this.payload = payload;
		payloadSize = payload.length;
	}

	/**
	 *  Add a payload to the message
	 *
	 *  @param payload payload for the message
	 */
	void addPayload(byte[] payload) {
		short[] shortPayload = new short[payload.length];

		for (int i = 0; i < payload.length; i++) {
			shortPayload[i] = payload[i];
		}

		addPayload(shortPayload);
	}

	/**
	 *  Query the payload size for this message
	 *
	 */
	int getPayloadLength() {
		LOG.debug("Message::getPayloadlength(): about to return:"+ payloadSize);
		return payloadSize;
	}

	/**
	 *  Retrieve the message payload
	 *
	 *  @return payload
	 */
	short[] getPayload() {
		return payload;
	}

	/**
	 *  Produces a byte[] suitable for 
	 *  GNUTELLA network
	 *
	 */
	byte[] getByteArray() {
		// TODO handle exception avoid null pointer
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		try {

			//byteStream = ;
			DataOutputStream payloadStream = new DataOutputStream(byteStream);

			// Write the guid
			short[] guidData = guid.getData();
			for (int i = 0; i < guidData.length; i++) {
				payloadStream.writeByte((byte)guidData[i]);
			}

			// Write the function (payload descriptor)
			payloadStream.writeByte(type);

			// Write the time to live
			payloadStream.writeByte(ttl);

			// Write the hop count
			payloadStream.writeByte(hops); // Why was this 0?

			if (this.getType() == PING) {
				payload = null;
			}

			// Write the Payload size in little-endian
			int payloadSize1 = 0x000000FF & payloadSize;
			int payloadSize2 = (0x0000FF00 & payloadSize) >> 8;
			int payloadSize3 = (0x00FF0000 & payloadSize) >> 16;
			int payloadSize4 = (0xFF000000 & payloadSize) >> 24;

			payloadStream.writeByte(payloadSize1);
			payloadStream.writeByte(payloadSize2);
			payloadStream.writeByte(payloadSize3);
			payloadStream.writeByte(payloadSize4);

			// Write the payload
			if (null != payload) {
				// Message may not have a payload (ex. Ping)
				for (int i = 0; i < payload.length; i++) {
					byte payloadByte = (byte)payload[i];
					payloadStream.writeByte(payloadByte);
				}
			}

			// all done
			payloadStream.close();

		}
		catch (IOException io) {
			LOG.error(io);
		}

		return byteStream.toByteArray();
	}

	/**
	 *  Checks validity of a payloads size
	 *
	 *  @param message to test
	 *  @return true if valid, false otherwise
	 */
	boolean validatePayloadSize() {
		boolean result = false;

		switch (type) {
			case PING :
				{
					if (SIZE_PING_PAYLOAD == payloadSize) {
						result = true;
					}
					break;
				}

			case PONG :
				{
					if (SIZE_PONG_PAYLOAD == payloadSize) {
						result = true;
					}
					break;
				}

			case PUSH :
				{
					if (payloadSize < SIZE_PUSH_PAYLOAD) {
						result = true;
					}
					break;
				}
				
			case RELAY :
			{
				if (payloadSize < SIZE_RELAY_PAYLOAD) {
					result = true;
				}
				break;
			}

			case QUERY :
				{
					if (payloadSize < SIZE_QUERY_PAYLOAD) {
						result = true;
					}
					break;
				}

			case QUERYREPLY :
				{
					if (payloadSize < SIZE_QUERYREPLY_PAYLOAD) {
						result = true;
					}
					break;
				}
				
			case SubNetSearchMessage.SUBNETQUERY:
				{
					if(payloadSize < SIZE_QUERY_PAYLOAD) {
						result = true;
					}
					break;
				}
		}

		return result;
	}

	/**
	 *  String representation of the message
	 *
	 */
	public String toString() {
		StringBuffer message = new StringBuffer();

		message.append("GUID: ");

		short[] guidData = getGUID().getData();
		for (int i = 0; i < guidData.length; i++) {
			message.append("[" + Integer.toHexString(guidData[i]) + "]");
		}

		message.append("\n");

		switch (type) {
			case PING :
				{
					message.append("PING message\n");
					break;
				}

			case PONG :
				{
					message.append("PONG message\n");
					break;
				}

			case QUERY :
				{
					message.append("QUERY message\n");
					break;
				}

			case QUERYREPLY :
				{
					message.append("QUERY REPLY message\n");
					break;
				}

			case PUSH :
				{
					message.append("PUSH message\n");
					break;
				}
				
			case RELAY :
			{
				message.append("RELAY message\n");
				break;
			}
				
			case SubNetSearchMessage.SUBNETQUERY:
				{
					message.append("SUBNETQUERY message\n");
					break;
				}

			default :
				{
					message.append("Unknown message");
					break;
				}
		}

		message.append("TTL: " + getTTL() + "\n");
		message.append("Hops: " + getHops() + "\n");
		
		message.append("Payload length: " + payloadSize + "\n");
		if (null != payload) {
			message.append("Payload (raw): \n");
			for (int i = 0; i < payload.length; i++) {
				message.append("[" + Integer.toHexString(payload[i]) + "]");
			}
			message.append("\n");
			
			if(type == QUERY || type == SubNetSearchMessage.SUBNETQUERY) {
				message.append("Payload (string): \n");
				StringBuffer msgBuffer = new StringBuffer();
				for (int i = 0; i < payload.length; i++) {
					msgBuffer.append(Character.toString((char)payload[i]));
				}
				message.append(msgBuffer.toString());
				message.append("\n");
			}
		}
		else {
			message.append("No payload");
		}

		return message.toString();
	}

	/**
	 *  Returns a String containing the flattened message
	 *
	 *  @return message string
	 */
	public String toRawString() {
		StringBuffer buffer = new StringBuffer();
		byte[] rawMessage = getByteArray();

		for (int i = 0; i < rawMessage.length; i++) {
			buffer.append("[" + Integer.toHexString(rawMessage[i]) + "]");

		}

		return buffer.toString();
	}

	/**
	 *  Get the connection that was the source for this message
	 *
	 * 
	 *  @return originating connection or null if this <code>Message</code> was
	 *  not read from the network
	 */
	public Connection getOriginatingConnection() {
		return originatingConnection;
	}
	
}
