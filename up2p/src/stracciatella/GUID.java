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

import java.util.Arrays;
import java.util.Set;
import java.util.zip.Adler32;

/**
 * Represents a unique ID, encapsulates an array of bytes
 *
 */
public class GUID {
	private byte[] data;
	
//	private static Set<GUID> allGUIDs; 	[bad idea.]

	private GUID(byte[] data){ 
		this.data = data;
	}
	/**
	 * Construct a new GUID
	 *
	 */
	public static GUID newGUID() {
		byte[] data = Utilities.generateGUID();
		return new GUID(data);
	}

	/**
	 * Create a guid from network data
	 *
	 */
	public static GUID getGUID(byte[] data) {
		return new GUID(data); // TODO= improve the management of GUIDs
	}
	
	/** 
	 * creates a GUID object from a String (primarily to be used with strings read from a web connection)
	 * @param guidAsString
	 * @return
	 */
	public static GUID getGUID(String guidAsString){
		//TODO: check length
		return getGUID(guidAsString.getBytes());
	}

	/**
	 * Query the bytes in this GUID
	 *
	 * @return bytes
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Compare guids
	 *
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof GUID)) {
			return false;
		}

		// Check if the data is the same
		GUID rhs = (GUID)obj;
		byte[] lhsData = getData();
		byte[] rhsData = rhs.getData();

		return Arrays.equals(lhsData, rhsData);
	}

	/**
	 * Produce a hashcode for this GUID
	 *
	 */
	public int hashCode() {
		Adler32 adler32 = new Adler32();

		// todo fix this stuff
		byte[] tempData = new byte[data.length];

		for (int i = 0; i < data.length; i++) {
			tempData[i] = (byte)data[i];
		}

		adler32.update(tempData);

		return (int)adler32.getValue();
	}

	/**
	 * Returns a GUID as a raw String
	 *
	 * @return unformatted text form of guid
	 */
	public String toRawString() {
		StringBuffer message = new StringBuffer();

		for (int i = 0; i < data.length; i++) {
			StringBuffer messageSection = new StringBuffer();
			messageSection.append(Integer.toHexString(data[i]));
			
			// Ensure every value is 2 chars long (i.e. 0F instead of F)
			if (messageSection.length() < 2) {
				message.append('0');
			}
			message.append(messageSection);
		}

		return message.toString();
	}
	
	/**
	 * Returns a GUID as a String
	 *
	 * @return text form of guid
	 */
	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append("GUID: ");

		for (int i = 0; i < data.length; i++) {
			message.append("[" + Integer.toHexString(data[i]) + "]");
		}

		return message.toString();
	}

	
}
