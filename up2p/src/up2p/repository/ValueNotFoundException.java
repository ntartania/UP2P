package up2p.repository;

import org.xmldb.api.base.XMLDBException;

/**
 * Exception thrown when executing XPath queries that return no result or
 * produce an error.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class ValueNotFoundException extends Exception {
    private XMLDBException dbe;

    /**
     * Create an exception with a message
     * 
     * @param msg the message
     */
    public ValueNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Create an exception with a message and a database exception.
     * 
     * @param msg the message
     * @param dbException the database exception
     */
    public ValueNotFoundException(String msg, XMLDBException dbException) {
        super(msg);
        dbe = dbException;
    }

    /**
     * Gets the XMLDBException.
     * 
     * @return the exception or <code>null</code> if it is not found
     */
    public XMLDBException getDBException() {
        return dbe;
    }
}