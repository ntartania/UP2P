package up2p.xml.filter;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;

/**
 * A filter used to process a resource. It receives a stream of SAX parsing
 * events and collects information and/or modifies the event stream before
 * sending it to the next filter in the chain.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface ResourceFilter extends ContentHandler, ErrorHandler {
    /**
     * Sets the chain whose event handler methods will be called by this filter
     * after receiving an event.
     * 
     * @param filterChain the chain to execute
     */
    public void setFilterChain(ResourceFilterChain filterChain);
}