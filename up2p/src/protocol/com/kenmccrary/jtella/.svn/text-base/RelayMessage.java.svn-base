package protocol.com.kenmccrary.jtella;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * RELAY messages are sent in response to PUSH messages that could
 * not be serviced (i.e. connection could not be made to the origin
 * of the PUSH), and are used to coordinate a file transfer through
 * a third party relay peer.
 * 
 * @author Alexander Craig
 */
public class RelayMessage extends Message {
	/** The GUID of the servent this message should be routed to */
	private GUID targetClientIdentifier;
	
	/** A unique identifier used to coordinate pairs of peers through a relay */
	private int relayId;
	
	/** The URL that the receiving peer should use to access the relay */
	private String relayUrl;
	
	/** The HTTP download port of the peer serving files (must be identical to original QUERY HIT) */
	private int port;
	
	/** The IP address of the peer serving files (must be identical to original QUERY HIT) */
	private String ipAddress;
	
	/** The URL prefix of the peer serving files (must be identical to original QUERY HIT) */
	private String urlPrefix;
	
	/**
	 * Construct a RELAY message, indicating that the remote node should
	 * direct a download request through the specified relay.
	 */
	RelayMessage() {
		super(Message.RELAY);
	}
	
	/**
	 *  Construct a RelayMessage from network data
	 *
	 *  @param rawMessage binary data from a connection
	 *  @param originatingConnection Connection creating this message
	 */
	RelayMessage(short[] rawMessage, Connection originatingConnection) {
		super(rawMessage, originatingConnection);
	}
	
	/**
	 *  Construct a RelayMessage using a previously received PushMessage.
	 *
	 *  @param pushMessage PUSH message containing target servent ID
	 *  @param relayId	The relay identifier used to pair servents through the relay
	 *  @param relayUrl	The URL of the peer to use as a relay (hostname:port/urlPrefix)
	 *  @param ipAddress IP address of the peer serving files (must be identical to original QUERY HIT)
	 *  @param port	The HTTP download port of the serving peer (must be identical to original QUERY HIT)
	 *  @param urlPrefix The URL prefix of the peer serving files (must be identical to original QUERY HIT)
	 */
	public RelayMessage(PushMessage pushMessage, int relayId, String relayUrl,
			String ipAddress, int port, String urlPrefix) {
		this(pushMessage.getSourceIdentifier(), relayId, relayUrl, ipAddress, port, urlPrefix);
	}
	
	/**
	 *  Construct a RelayMessage by specifiying a target GUID
	 *
	 *  @param clientIdentifier The client identifier of the servent that this
	 *  						RELAY message should be routed to
	 *  @param relayId	The relay identifier used to pair servents through the relay
	 *  @param relayUrl	The URL of the peer to use as a relay (hostname:port/urlPrefix)
	 *  @param ipAddress IP address of the peer serving files (must be identical to original QUERY HIT)
	 *  @param port	The HTTP download port of the serving peer (must be identical to original QUERY HIT)
	 *  @param urlPrefix The URL prefix of the peer serving files (must be identical to original QUERY HIT)
	 */
	public RelayMessage(GUID clientIdentifier, int relayId, String relayUrl,
			String ipAddress, int port, String urlPrefix) {
		super(Message.RELAY);
		this.targetClientIdentifier = clientIdentifier;
		this.relayId = relayId;
		this.relayUrl = relayUrl;
		this.port = port;
		try {
			this.ipAddress = InetAddress.getByName(ipAddress).getHostAddress();
		} catch (UnknownHostException e) {
			this.ipAddress = ipAddress;
		}
		this.urlPrefix = urlPrefix;

		buildPayload();
	}
	
	/**
	 * Builds the RELAY message payload
	 */
	void buildPayload() {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);

			// Target Servant Identifier
			short[] targetGuidData =
				targetClientIdentifier.getData();
			for (int i = 0; i < targetGuidData.length; i++) {
				dataStream.writeByte((byte)targetGuidData[i]);
			}

			// Relay ID
			dataStream.writeInt(relayId);
			
			// Source IP Address
			int beginIndex = 0;
			int endIndex = ipAddress.indexOf('.');
			int ip1 = Integer.parseInt(ipAddress.substring(beginIndex, endIndex));
			beginIndex = endIndex + 1;
			endIndex = ipAddress.indexOf('.', beginIndex);
			int ip2 = Integer.parseInt(ipAddress.substring(beginIndex, endIndex));
			beginIndex = endIndex + 1;
			endIndex = ipAddress.indexOf('.', beginIndex);
			int ip3 = Integer.parseInt(ipAddress.substring(beginIndex, endIndex));
			beginIndex = endIndex + 1;
			int ip4 = Integer.parseInt(ipAddress.substring(beginIndex, ipAddress.length()));
			dataStream.writeByte(ip1);
			dataStream.writeByte(ip2);
			dataStream.writeByte(ip3);
			dataStream.writeByte(ip4);
			
			// Source Port
			dataStream.writeInt(this.port);
			
			// Source URL Prefix
			dataStream.writeInt(urlPrefix.length());	// Length field
			for(char c : urlPrefix.toCharArray()) {
				dataStream.write(c);
			}
			
			// Relay URL
			dataStream.writeInt(relayUrl.length());	// Length field
			for(char c : relayUrl.toCharArray()) {
				dataStream.write(c);
			}
			
			addPayload(byteStream.toByteArray());
			dataStream.close();
		} catch (IOException io) {
			// Writing to a byte array should never throw an IOException
		}
	}
	
	/**
	 * @return The client GUID targeted by this RELAY message
	 */
	public GUID getTargetIdentifier() {
		short[] guidData = new short[16];
		// Target servent ID is bytes 0-15
		System.arraycopy(payload, 0, guidData, 0, 16);
		return new GUID(guidData);
	}
	
	/**
	 * @return The relay identifier specified by this RELAY message
	 */
	public int getRelayIdentifier() {
		// Relay identifier is bytes 16-19
		// Big endian format
		int relayId = payload[19] & 0xff;
		relayId |= (payload[18] & 0xff) << 8;
		relayId |= (payload[17] & 0xff) << 16;
		relayId |= (payload[16] & 0xff) << 24;
		return relayId;
	}
	
	/**
	 * @return	The URL which is specified as a peer relay by this RELAY message
	 */
	public String getRelayUrl() {
		// Must read the length of the source URL prefix first so those bytes
		// can be skipped
		int urlPrefixLength = (payload[31] & 0xff);
		urlPrefixLength |= (payload[30] & 0xff) << 8;
		urlPrefixLength |= (payload[29] & 0xff) << 16;
		urlPrefixLength |= (payload[28] & 0xff) << 24;
		int payloadIndex = 32 + urlPrefixLength;
		
		// Length of the URL is 4 bytes (big endian)
		int urlLength = (payload[payloadIndex + 3] & 0xff);
		urlLength |= (payload[payloadIndex + 2] & 0xff) << 8;
		urlLength |= (payload[payloadIndex + 1] & 0xff) << 16;
		urlLength |= (payload[payloadIndex] & 0xff) << 24;
		
		// Got the length, now read off the URL
		payloadIndex = payloadIndex + 4;
		StringBuffer relayUrl = new StringBuffer();
		for(int i = 0; i < urlLength; i++) {	
			relayUrl.append((char)payload[payloadIndex + i]);
		}
		return relayUrl.toString();
	}
	
	/**
	 * @return	IP address of the peer serving files (must be identical to original QUERY HIT)
	 */
	public String getSourceIP() {
		// Source IP is bytes 20-23
		StringBuffer ipBuffer = new StringBuffer();

		ipBuffer
			.append(Integer.toString(payload[20]))
			.append(".")
			.append(Integer.toString(payload[21]))
			.append(".")
			.append(Integer.toString(payload[22]))
			.append(".")
			.append(Integer.toString(payload[23]));

		return ipBuffer.toString();
	}
	
	/**
	 * @return	The HTTP download port of the serving peer (must be identical to original QUERY HIT)
	 */
	public int getSourcePort() {
		// Source port is bytes 24-27
		int sourcePort = (payload[27] & 0xff);
		sourcePort |= (payload[26] & 0xff) << 8;
		sourcePort |= (payload[25] & 0xff) << 16;
		sourcePort |= (payload[24] & 0xff) << 24;
		return sourcePort;
	
	}
	
	/**
	 * @return The URL prefix of the peer serving files (must be identical to original QUERY HIT)
	 */
	public String getSourceUrlPrefix() {
		// Read the length of the url prefix first
		int urlPrefixLength = (payload[31] & 0xff);
		urlPrefixLength |= (payload[30] & 0xff) << 8;
		urlPrefixLength |= (payload[29] & 0xff) << 16;
		urlPrefixLength |= (payload[28] & 0xff) << 24;
		
		// Got the length, now read off the URL (bytes 32-)
		StringBuffer sourceUrlPrefix = new StringBuffer();
		for(int i = 0; i < urlPrefixLength; i++) {	
			sourceUrlPrefix.append((char)payload[32 + i]);
		}
		return sourceUrlPrefix.toString();
	}
	
	/**
	 * @return	The full peer identifier (hostname:port/urlPrefix) of the peer serving files
	 */
	public String getSourcePeerId() {
		return getSourceIP() + ":" + getSourcePort() + "/" + getSourceUrlPrefix();
	}
}
