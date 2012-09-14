package up2p.xml.filter;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import up2p.core.ResourceProperties;

/**
 * Executes a series of registered filters on a stream of SAX events generated
 * from the given XML parser. A filter can propagate a SAX event by invoking the
 * next link in the filter chain or surpress the event by calling
 * <code>resetIterator</code> on the chain.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface ResourceFilterChain extends ContentHandler, ErrorHandler,
        ResourceProperties {

    /**
     * Adds a filter to the end of the chain.
     * 
     * @param filter the filter to add
     */
    public void addFilter(ResourceFilter filter);

    /**
     * Removes the given filter if it exists or ignores the request if the
     * filter is not found.
     * 
     * @param filter the filter to remove
     * @return <code>true</code> if the filter was removed, <code>false</code>
     * otherwise
     */
    public boolean removeFilter(ResourceFilter filter);

    /**
     * Filters the stream of events generated from the given SAX parser.
     * 
     * @param reader the XML SAX parser to filter
     * @param source the XML to parse and filter
     * @throws SAXException if a SAX error occurs in parsing or filtering
     * @throws IOException if an error with I/O occurs in parsing or filtering
     */
    public void doFilter(XMLReader reader, InputSource source)
            throws SAXException, IOException;

    /**
     * Resets the iterator used in the filter chain.
     */
    public void resetIterator();
}