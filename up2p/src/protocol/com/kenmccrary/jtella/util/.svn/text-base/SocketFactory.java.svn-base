package protocol.com.kenmccrary.jtella.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.io.InterruptedIOException;

public class SocketFactory {
	/**
	 *  Creates a socket with a maxiumum wait value on the initial connection
	 *
	 *  @param host remote host
	 *  @param port remote port
	 *  @param maxWait maximum wait time for connection in milliseconds
	 *  @return socket
	 */
	public static Socket getSocket(String host, int port, int maxWait)
		throws IOException {
		Socket socket = new Socket();
		
		// Exception will be thrown on connect() if the connection fails
		// for any reason
		socket.connect(new InetSocketAddress(host, port), maxWait);
		
		return socket;
	}
}
