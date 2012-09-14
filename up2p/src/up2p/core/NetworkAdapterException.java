package up2p.core;

/**
 * An exception that is thrown when a Nework Adapter encounters difficulty in
 * connecting to its network.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class NetworkAdapterException extends Exception {

    /**
     * Creates an exception with the given message as the reason for throwing
     * the exception.
     * 
     * @param message reason for the exception or other message
     */
    public NetworkAdapterException(String message) {
        super(message);
    }
}