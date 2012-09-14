package up2p.core;

/**
 * A general U-P2P exception that can be used to provide feedback messages to
 * the end-user.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class UP2PException extends Exception {

    /**
     * Constructs an exception with a message.
     * 
     * @param msg the message to send
     */
    public UP2PException(String msg) {
        super(msg);
    }

    /**
     * Constructs an exception.
     *  
     */
    public UP2PException() {
    }
}