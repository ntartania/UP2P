package up2p.xml.filter;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Catches the errors in an XML stream and throws them if necessary.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class ValidationFilter extends BaseResourceFilter {
    private boolean hasError;

    private int lineNumber;

    private int columnNumber;

    private String publicId;

    private String systemId;

    /**
     * Constructs a validation filter.
     */
    public ValidationFilter() {
        hasError = false;
    }

    /**
     * Catches validation errors and throws them after logging the relevant
     * information.
     * 
     * @param e the parse exception
     * @throws SAXException throws the given parse exception
     */
    public void error(SAXParseException e) throws SAXException {
        recordException(e);
        throw e;
    }

    /**
     * Catches fatal errors and throws them after logging the relevant
     * information.
     * 
     * @param e the parse exception
     * @throws SAXException throws the given parse exception
     */
    public void fatalError(SAXParseException e) throws SAXException {
        recordException(e);
        throw e;
    }

    /**
     * Catches warnings and throws them after logging the relevant
     * information.
     * 
     * @param e the parse exception
     * @throws SAXException throws the given parse exception
     */
    public void warning(SAXParseException e) throws SAXException {
        recordException(e);
        throw e;
    }

    /**
     * Records the exception information.
     * 
     * @param e exception to record
     */
    private void recordException(SAXParseException e) {
        lineNumber = e.getLineNumber();
        columnNumber = e.getColumnNumber();
        publicId = e.getPublicId();
        systemId = e.getSystemId();
        hasError = true;
    }

    /**
     * Returns the columnNumber.
     * 
     * @return int
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Returns the hasError.
     * 
     * @return boolean
     */
    public boolean isHasError() {
        return hasError;
    }

    /**
     * Returns the lineNumber.
     * 
     * @return int
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the publicId.
     * 
     * @return String
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * Returns the systemId.
     * 
     * @return String
     */
    public String getSystemId() {
        return systemId;
    }
}