package up2p.core;

/**
 * Thrown when a resource is not found during an operation on the repository.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class ResourceNotFoundException extends Exception {

    /**
     * Constructs an empty exception.
     */
    public ResourceNotFoundException() {
        super();
    }

    /**
     * Constructs an exception with a string message.
     * 
     * @param message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

}