package up2p.peer.jtella;

/**
 * Thrown when an XML message being parsed is badly formed.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class MalformedPeerMessageException extends Exception {

    public MalformedPeerMessageException(String msg) {
        super(msg);
    }
}