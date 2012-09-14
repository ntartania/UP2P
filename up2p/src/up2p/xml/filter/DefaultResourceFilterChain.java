package up2p.xml.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Implements a simple filter chain for processing SAX events. For each SAX
 * event received, the chain iterates over a list of filters and invokes the SAX
 * method on each filter. If the filter does not invoke the next link in the
 * chain, the iterator will be reset when a new SAX event occurs.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DefaultResourceFilterChain implements ResourceFilterChain {
    /** The list of filters in the chain. */
    private List<ResourceFilter> filters;

    /** An iterator used to step through the chain. */
    private Iterator<ResourceFilter> filterIterator;

    /** Map for storing properties in the chain. */
    protected Map<String,Object> properties;

    /** The locator for the current SAX event. */
    private Locator location;

    /**
     * Constructs an empty chain.
     */
    public DefaultResourceFilterChain() {
        filters = new ArrayList<ResourceFilter>();
        properties = new HashMap<String,Object>();
    }

    /*
     * @see up2p.xml.filter.ResourceFilterChain#addFilter(ResourceFilter)
     */
    public void addFilter(ResourceFilter filter) {
        filters.add(filter);
        filter.setFilterChain(this);
    }

    /*
     * @see up2p.xml.filter.ResourceFilterChain#removeFilter(ResourceFilter)
     */
    public boolean removeFilter(ResourceFilter filter) {
        return filters.remove(filter);
    }

    /**
     * Iterates over all the added filters for each SAX event received.
     * 
     * @see up2p.xml.filter.ResourceFilterChain#doFilter(XMLReader, InputSource)
     */
    public void doFilter(XMLReader reader, InputSource source)
            throws SAXException, IOException {
        filterIterator = filters.iterator();
        // parse if at least one filter exists
        if (filters.size() > 0) {
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            reader.parse(source);
        }
    }

    /*
     * @see up2p.xml.filter.ResourceProperties#getProperty(String)
     */
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /*
     * @see up2p.xml.filter.ResourceProperties#setProperty(String, Object)
     */
    public void setProperty(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    /*
     * @see up2p.xml.filter.ResourceProperties#getPropertyNames()
     */
    public Iterator<String> getPropertyNames() {
        return properties.keySet().iterator();
    }

    /*
     * @see up2p.xml.filter.ResourceFilterChain#resetIterator()
     */
    public void resetIterator() {
        filterIterator = filters.iterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        // store location information for current event
        location = locator;

        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next())
                    .setDocumentLocator(location);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).startDocument();
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).endDocument();
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).startPrefixMapping(prefix,
                    uri);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).endPrefixMapping(prefix);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#startElement(String, String, String,
     * Attributes)
     */
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).startElement(namespaceURI,
                    localName, qName, atts);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).endElement(namespaceURI,
                    localName, qName);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).characters(ch, start,
                    length);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).ignorableWhitespace(ch,
                    start, length);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).processingInstruction(
                    target, data);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).skippedEntity(name);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).warning(exception);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ErrorHandler#error(SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).error(exception);
        else
            resetIterator();
    }

    /*
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        if (filterIterator.hasNext())
            ((ResourceFilter) filterIterator.next()).fatalError(exception);
        else
            resetIterator();
    }
}