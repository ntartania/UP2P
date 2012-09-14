package up2p.core;

/**
 * Thrown when an attachment is not found during an operation on the repository.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class AttachmentNotFoundException extends Exception {
    /**
     * Constructs an empty exception.
     */
    public AttachmentNotFoundException() {
        super();
    }

    /**
     * Constructs an exception with a string message.
     * 
     * @param message
     */
    public AttachmentNotFoundException(String message) {
        super(message);
    }
}