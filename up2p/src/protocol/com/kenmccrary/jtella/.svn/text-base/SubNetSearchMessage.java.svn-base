package protocol.com.kenmccrary.jtella;


/**
 * The SubNetSearchMessage class represents a SUBNETQUERY message.
 * SUBNETQUERY messages are a U-P2P specific extension to the Gnutella
 * protocol which allows a QUERY to be directed only to certain segments
 * of the network (servents that serve the specified sub-network
 * identifier)
 * 
 * @author Alexander Craig
 * @version July 2010
 */
public class SubNetSearchMessage extends Message {
	/** The header descriptor ID for a SUBNETQUERY message */
	public final static short SUBNETQUERY = (short)0x82;
	
	/** Get file by name */
	public final static int GET_BY_NAME = 0;
	/** Get file by sha1 hash */
	public final static int GET_BY_HASH = 1;
	
	/** Sub-network identifiers are 18 bytes */
	public static final int SUB_NET_ID_LENGTH = 18;
	
	/** The search criteria for the query */
	private String criteria;
	
	/** The type of the search (look up by name or hash, using integer constants) */
	private int searchType;
	
	/** 
	 * The sub-network identifier of the query. Should be an 18 byte string (to correspond
	 *  to U-P2P community ID's). If an identifier of less than 18 bytes is provided it will be
	 *  padded with zeros, and a longer identifier will be truncated.
	 */
	private String subNetIdentifier;

	/**
	 * Construct a U-P2P / GNUTella SUBNETSEARCH query. Note that the minimum
	 * download speed is not considered and is included in the payload as 0 (as U-P2P
	 * does not make use of the value).
	 * 
	 * @param criteria	The search criteria of the message
	 * @param searchType	The search type (look up by hash or name)
	 * @param subNetId An 18 byte sub-network identifier string
	 */
	public SubNetSearchMessage(String criteria, int searchType, String subNetId) {
		super(SubNetSearchMessage.SUBNETQUERY);
		this.searchType = searchType;
		this.criteria = criteria;
		
		// Ensure the sub-network identifier is an 18 byte string, and zero pad
		// if a shorter identifier is supplied
		if(subNetId.length() > SUB_NET_ID_LENGTH) {
			subNetIdentifier = subNetId.substring(0, SUB_NET_ID_LENGTH);
		} else if (subNetId.length() < SUB_NET_ID_LENGTH) {
			StringBuffer paddingBuffer = new StringBuffer(subNetId);
			for(int i = subNetId.length(); i < SUB_NET_ID_LENGTH; i++) {
				paddingBuffer.append("0");
			}
			subNetIdentifier = paddingBuffer.toString();
		} else {
			subNetIdentifier = subNetId;
		}

		buildPayload();
	}

	/**
	 * Construct a SubNetSearchMessage from data read from network. Note that
	 * this method only constructs the header of the message, and the input stream
	 * is left at the beginning of the message payload upon return.
	 *
	 * @param rawMessage binary data from a connection
	 * @param originatingConnection Connection creating this message
	 *
	 */
	public SubNetSearchMessage(short[] rawMessage, Connection originatingConnection) {
		super(rawMessage, originatingConnection);
	}

	/**
	 * Constructs the payload for the sub-network search message. The payload format
	 * for sub-network queries is as follows:
	 * 
	 * || 0 - Minimum Speed - 1 | 2 - Sub-Network ID - 19 | 20 - Search Criteria - END_OF_PAYLOAD || 
	 *
	 * Bytes 0-1: Minimum download speed
	 * Bytes 2-19: Sub-Network Identifier
	 * Bytes 20 - payloadLength: Search criteria (null terminated)
	 */
	void buildPayload() {
		byte[] criteriaChars = criteria.getBytes();
		byte[] subNetChars = subNetIdentifier.getBytes();
		short[] payload = null;
		
		if (searchType == GET_BY_NAME) {
			// +3 Bytes for minimum speed, and null following criteria
			// +5 Bytes for URN request and following null
			payload = new short[criteriaChars.length + SUB_NET_ID_LENGTH + 8];
			
		} else if (searchType == GET_BY_HASH) {
			// +3 Bytes for minimum speed, and null following criteria
			payload = new short[criteriaChars.length + SUB_NET_ID_LENGTH + 3];
			
		} else {
			// Add a null payload and return if an invalid searchType is specified
			addPayload(payload);
			return;
		}
		
		// Speed always 0 for now (unused)
		payload[0] = 0;
		payload[1] = 0;

		// Add the sub-network index
		int payloadIndex = 2;
		for(int i = 0; i < SUB_NET_ID_LENGTH; i++) {
			payload[payloadIndex] = subNetChars[i];
			payloadIndex++;
		}
	
		// Add the criteria along with null terminator
		for (int i = 0; i < criteriaChars.length; i++) {
			payload[payloadIndex] = criteriaChars[i];
			payloadIndex += 1;
		}
		payload[payloadIndex++] = 0;

		if (searchType == GET_BY_NAME) {
			// Now add request for sha1 string and null terminator
			payload[payloadIndex++] = 'u';
			payload[payloadIndex++] = 'r';
			payload[payloadIndex++] = 'n';
			payload[payloadIndex++] = ':';
			payload[payloadIndex++] = 0;
		}
			
		LOG.debug("SearchMessage:buildPayload(): Created payload of length"+ payloadIndex);
		addPayload(payload);
	}

	/**
	 * Get the minimum download speed for responses.
	 * @return Minimum download speed for responses
	 */
	public int getMinimumDownloadSpeed() {
		int byte1 = payload[0];
		int byte2 = payload[1];

		return byte1 | (byte2 << 8);
	}

	/**
	 * Query the search criteria for this message.
	 * @return The query search criteria
	 */
	public String getSearchCriteria() {
		if (null == payload || payload.length < (3 + SUB_NET_ID_LENGTH)) {
			// No search criteria
			return new String("");
		}

		byte[] text = null;
		
		if (searchType == GET_BY_NAME) {
			// 2 speed, 2 nulls, 4 "urn:", SUB_NET_ID_LENGTH for sub-network ID
			text = new byte[payload.length - 2 - 2 - 4 - SUB_NET_ID_LENGTH];
		} else {
			// 2 speed, 1 null, SUB_NET_ID_LENGTH for sub-network ID
			text = new byte[payload.length - 2 - 1 - SUB_NET_ID_LENGTH];
		}
		
		// Skip 2 speed bytes and sub-network identifier
		int payloadIndex = 2 + SUB_NET_ID_LENGTH;
		for (int i = 0; i < text.length; i++) {
			text[i] = (byte)payload[payloadIndex++];
		}

		this.criteria = new String(text);
		return this.criteria;
	}
	
	/**
	 * Query the sub-network identifier for this message.
	 * @return The query sub-network identifier
	 */
	public String getSubNetIdentifier() {
		if (null == payload || payload.length < (3 + SUB_NET_ID_LENGTH)) {
			// Sub-Network ID is invalid or missing (not 18 bytes)
			return new String("");
		}
		
		byte[] subNetId = new byte[SUB_NET_ID_LENGTH];
		int payloadIndex = 2;
		for(int i = 0; i < subNetId.length; i++) {
			subNetId[i] = (byte)payload[payloadIndex++];
		}
		
		subNetIdentifier = new String(subNetId);
		return subNetIdentifier;
	}
	
	/**
	 * Converts the message into a standard SearchMessage (discards the sub-network
	 * identifier). This is useful for passing the message to U-P2P once it has been
	 * determined that the message should be serviced.
	 * 
	 * @return A standard search message with the same criteria and message type
	 */
	public SearchMessage toSearchMessage() {
		return new SearchMessage(criteria, searchType, 0);
	}
}
