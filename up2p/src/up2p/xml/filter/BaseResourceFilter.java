package up2p.xml.filter;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implements a base resource filter that ignores all incoming events and passes
 * them on to the next filter in the chain. Subclasses should extend this filter
 * and override the methods for desired events.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class BaseResourceFilter implements ResourceFilter {
    protected ResourceFilterChain chain;

    protected Map<String,Object> properties;

    protected Locator docLocator;

    /**
     * Creates an empty filter.
     */
    public BaseResourceFilter() {
        properties = new HashMap<String,Object>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see up2p.xml.ResourceFilter#setFilterChain(ResourceFilterChain)
     */
    public void setFilterChain(ResourceFilterChain filterChain) {
        chain = filterChain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        docLocator = locator;
        chain.setDocumentLocator(locator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        chain.startDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        chain.endDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        chain.startPrefixMapping(prefix, uri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        chain.endPrefixMapping(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(String, String, String,
     * Attributes)
     */
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        chain.startElement(namespaceURI, localName, qName, atts);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        chain.endElement(namespaceURI, localName, qName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        chain.characters(ch, start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        chain.ignorableWhitespace(ch, start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        chain.processingInstruction(target, data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
        chain.skippedEntity(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException {
        chain.warning(exception);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#error(SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        chain.error(exception);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        chain.fatalError(exception);
    }

}