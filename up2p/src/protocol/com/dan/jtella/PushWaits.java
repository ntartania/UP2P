/**
 * @author Daniel Meyers
 * Created On:    Jan 29, 2004
 * Last Modified: Jan 29, 2004
 * 
 */
package protocol.com.dan.jtella;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.util.Hashtable;
import java.net.Socket;
import java.io.IOException;

/**
 * Keeps a record of what push requests have been made to which remote hosts, so that when
 * a response to a push request is detected it can be discarded or sent back to the
 * PushResponseInterface that sent the push request.
 */
public class PushWaits {

	// Hashtable of PushResponseInterface objects linked to push requests
	private static Hashtable<String,PushResponseInterface> waiting = new Hashtable<String,PushResponseInterface>();

	/** 
	 * Synchronised method to notify PushResponseInterface and remove it from Hashtable
	 * if a PushResponseInterface is waiting on this push response
	 * 
	 * @param serventGUID The GUID of the servent a push request has been received from
	 * @param sock The socket connected to the servent in question
	 * @param inputStream A DataInputStream wrapped around the Sockets input stream
	 * @param outputStream A DataOutputStream wrapped around the Sockets output stream
	 * @param bufferedReader A BufferedReader wrapped around the Sockets input stream
	 * @param bufferedWriter A BufferedWriter wrapped around the Sockets output stream
	 */
	public static synchronized boolean pushReceived(
		String serventGUID,
		Socket sock,
		DataInputStream inputStream,
		DataOutputStream outputStream,
		BufferedReader bufferedReader,
		BufferedWriter bufferedWriter) {

		String upperGUID = serventGUID.toUpperCase();

		if (waiting.containsKey(upperGUID)) {
			((PushResponseInterface)waiting.get(upperGUID)).pushReceived(
				sock,
				inputStream,
				outputStream,
				bufferedReader,
				bufferedWriter);
			waiting.remove(upperGUID);
			return true;
		}
		else {
			try {
				bufferedReader.close();
			}
			catch (IOException ioe) {}
			try {
				bufferedWriter.close();
			}
			catch (IOException ioe) {}
			try {
				inputStream.close();
			}
			catch (IOException ioe) {}
			try {
				outputStream.close();
			}
			catch (IOException ioe) {}
			try {
				sock.close();
			}
			catch (IOException ioe) {}
			return false;
		}
	}

	/**
	 * Synchronised method to add a downloader and a servent ID that it is waiting on
	 * 
	 * @param downloader A PushResponseInterface that is waiting for a response to a push request
	 * @param serventGUID The GUID of the servent that the downloader is waiting on
	 * 
	 */
	public static synchronized void addWait(PushResponseInterface downloader, String serventGUID) {
		String upperGUID = serventGUID.toUpperCase();

		waiting.put(upperGUID, downloader);
	}
}
