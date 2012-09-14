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
 * EDITED BY: Daniel Meyers, 2004<br>
 * SearchMessage, the message for queries
 *
 *
 */
public class SearchMessage extends Message {
	
	// Get file by name
	public final static int GET_BY_NAME = 0;
	// Get file by sha1 hash
	public final static int GET_BY_HASH = 1;
	
	private String criteria;
	private int searchType;

	/**
	 * Construct a GNUTella search query
	 *
	 */
	// TODO something with the speed
	public SearchMessage(String criteria, int searchType, int minumumSpeed) {
		super(Message.QUERY);
		this.searchType = searchType;
		this.criteria = criteria;

		buildPayload();
	}

	/**
	 * Construct a SearchMessage from data read from network
	 *
	 * @param rawMessage binary data from a connection
	 * @param originatingConnection Connection creating this message
	 *
	 */
	SearchMessage(short[] rawMessage, Connection originatingConnection) {
		super(rawMessage, originatingConnection);
	}

	/**
	 * EDITED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Contructs the payload for the search message
	 *
	 * Bytes 0-1 download speed
	 * followed by search query (null terminated)
	 */
	void buildPayload() {
		byte[] chars = criteria.getBytes();
		short[] payload = null;
		
		if (searchType == GET_BY_NAME) {
			// +5 for URN request and null, +3 for speed & null 
			payload = new short[(chars.length + 5) + 3];
			payload[0] = 0; // TODO speed
			payload[1] = 0;

			int payloadIndex = 2;
		
			for (int i = 0; i < chars.length; i++) {
				payload[payloadIndex] = chars[i];
				payloadIndex += 1;
			}

			payload[payloadIndex++] = 0;

			// Now add request for sha1 string
			payload[payloadIndex++] = 'u';
			payload[payloadIndex++] = 'r';
			payload[payloadIndex++] = 'n';
			payload[payloadIndex++] = ':';
			payload[payloadIndex++] = 0;
			LOG.debug("SearchMessage:buildPayload(): Created payload of length"+ payloadIndex);
		}
		else if (searchType == GET_BY_HASH) {
			// +3 for speed & null, +1 for extra null
			payload = new short[(chars.length + 3) + 1];
			payload[0] = 0; // TODO speed
			payload[1] = 0;

			int payloadIndex = 2;
			
			payload[payloadIndex++] = 0;
			
			for (int i = 0; i < chars.length; i++) {
				payload[payloadIndex] = chars[i];
				payloadIndex += 1;
			}
			
			payload[payloadIndex++] = 0;
			LOG.debug("SearchMessage:buildPayload(): Created payload of length"+ payloadIndex);
		}
		
		addPayload(payload);
	}

	/**
	 * Get the minimum download speed for responses
	 *
	 * @return download speed
	 */
	public int getMinimumDownloadSpeed() {
		int byte1 = payload[0];
		int byte2 = payload[1];

		return byte1 | (byte2 << 8);
	}

	/**
	 * EDITED BY: Daniel Meyers, 2004
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * Query the search criteria for this message
	 *
	 * @return search criteria
	 */
	public String getSearchCriteria() {
		if (null == payload || payload.length < 3) {
			// no data
			return new String("");
		}

		byte[] text = null;
		
		if (searchType == GET_BY_NAME) {
			// 2 speed, 2 nulls, 4 'urn:'
			text = new byte[payload.length - 2 - 2 - 4];
			// skip 2 speed bytes
			int payloadIndex = 2;
			for (int i = 0; i < text.length; i++) {
				text[i] = (byte)payload[payloadIndex++];
			}
		}

		return new String(text);
	}
}
