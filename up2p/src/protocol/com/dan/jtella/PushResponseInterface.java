/**
 * @author Daniel Meyers
 * Created On:    Jan 29, 2004
 * Last Modified: Jan 29, 2004
 * 
 */
package protocol.com.dan.jtella;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Provides an interface for classes that wish to be notified when an incoming push response
 * relates to them
 */
public interface PushResponseInterface {

	/**
	 * Called when a push message relating an instance of the class
	 * implementing this interface is received.
	 *
	 * @param s The Socket connected to the remote host that sent the push response
	 * @param inputStream A DataInputStream connected to the Socket input stream
	 * @param outputStream A DataOutputStream connected to the Socket output stream
	 * @param bufferedReader A BufferedReader connected to the Socket input stream
	 * @param bufferedWriter A BufferedWriter connected to the Socket output stream
	 */
	public void pushReceived(
		Socket s,
		DataInputStream inputStream,
		DataOutputStream outputStream,
		BufferedReader bufferedReader,
		BufferedWriter bufferedWriter);
}
