package up2p.peer.jtella;

/**
 * Interface for all peer messages that are serializable.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface SerializableMessage {

    /**
     * Serializes a peer message to XML.
     * 
     * @return the serialized message
     */
    public org.w3c.dom.Node serialize();
}