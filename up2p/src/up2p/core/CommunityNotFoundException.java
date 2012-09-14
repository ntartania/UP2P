package up2p.core;

/**
 * Thrown when a user tries to perform an action on a community that does not
 * exist in the local repository.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class CommunityNotFoundException extends Exception {

    /**
     * Constructs an exception.
     */
    public CommunityNotFoundException() {
        super();
    }

    /**
     * Constructs an exception with a message string.
     * 
     * @param message a message to throw
     */
    public CommunityNotFoundException(String message) {
        super(message);
    }

}