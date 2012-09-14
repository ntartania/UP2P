package up2p.peer.jtella;

import java.util.Scanner;

/**
 * Base class for all messages exchanged between the client and server for the
 * generic peer-to-peer implementation.
 *
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class GenericPeerMessage implements SerializableMessage {
    /** A message sent by a client to perform a search on the server. */
    public final static int SEARCH_REQUEST = 0;

    /** A message sent by the server in response to a search request. */
    public final static int SEARCH_RESPONSE = 1;

    /** A message sent by a client to register shared files with the server. */
    public final static int REGISTER_REQUEST = 2;

    /** A message sent by a client to register shared files with the server. */
    public final static int REGISTER_RESPONSE = 3;

    /** The opening and closing tag for all U-P2P XML messages */
    public final static String X_UP2P_MESSAGE = "up2pMessage";

    /** The message type. */
    protected int type;

    /** The id of the message, used to match requests to responses */
    protected String id;

    /**
     * Constructs the given type of message.
     *
     * @param messageType the type of the message as defined in
     * <code>GenericPeerMessage</code>
     */
    public GenericPeerMessage(int messageType) {
        type = messageType;
    }

    /**
     * Returns the type of the message as defined in
     * <code>GenericPeerMessage</code>.
     *
     * @return the message type
     */
    public int getType() {
        return type;
    }
    
   
    /**
     * Returns a unique id used for a request-response message sequences.
     *
     * @return a unique id
     */
    public static String getUniqueId() {
        return String.valueOf(new java.util.Date().getTime());
    }

    /**
     * Returns the id.
     *
     * @return String
     */
    public String getId() {
        return id;
    }

}