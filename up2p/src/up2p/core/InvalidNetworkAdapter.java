package up2p.core;

/**
 * An exception that is thrown when an attempt is made to create an instance of
 * a <code>NetworkAdapter</code> that cannot be created because the provider
 * class could not be found or an error occured in loading the provider.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class InvalidNetworkAdapter extends Exception {
    /** The class name of the failed adapter. */
    protected String className;

    /** The version of the failed adapter. */
    protected String version;

    /** The exception thrown that may have caused a failure. */
    protected Exception failureException;

    /**
     * Creates an exception with a message.
     * 
     * @param msg the message to send
     */
    public InvalidNetworkAdapter(String msg) {
        super(msg);
    }

    /**
     * Creates an exception with the provider class name and version.
     * 
     * @param msg a message explaining the exception
     * @param className the fully-qualified class name of the provider class
     * @param version the version of the network adapter that failed to load
     * @param failureException the exception thrown that may have caused the
     * failure
     */
    public InvalidNetworkAdapter(String msg, String className, String version,
            Exception failureException) {
        super(msg);
        this.className = className;
        this.version = version;
        this.failureException = failureException;
    }

    /**
     * Creates an exception with the provider class name and version.
     * 
     * @param msg a message explaining the exception
     * @param className the fully-qualified class name of the provider class
     * @param version the version of the network adapter that failed to load
     */
    public InvalidNetworkAdapter(String msg, String className, String version) {
        super(msg);
        this.className = className;
        this.version = version;
    }

    /**
     * Returns the failure exception.
     * 
     * @return the failure that caused the exception
     */
    public Exception getFailureException() {
        return failureException;
    }
}