package up2p.xml.filter;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Transforms a stream of XML SAX events according to the XSLT rules in a given
 * template.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class XSLTFilter extends BaseResourceFilter implements ErrorListener {

    private TransformerHandler handler;

    private Result out;

    private Map<String, String> parameters;
    private Source xsltSource;

    /**
     * Construct an XSLT filter with the given XSLT source and result.
     * 
     * @param xsltFile the XSLT file
     * @param output the stream to which the transformed XML will be written
     * @throws TransformerException if an error occurs with the XSLT source
     */
    public XSLTFilter(File xsltFile, OutputStream output)
            throws TransformerException {
        this(new StreamSource(xsltFile), new StreamResult(output), null);
    }

    /**
     * Construct an XSLT filter with the given XSLT source and result.
     * 
     * @param xsltFile the XSLT file
     * @param output the stream to which the transformed XML will be written
     * @param params the parameters to pass to the transformer
     * @throws TransformerException if an error occurs with the XSLT source
     */
    public XSLTFilter(File xsltFile, OutputStream output, Map<String, String> params)
            throws TransformerException {
        this(new StreamSource(xsltFile), new StreamResult(output), params);
    }

    /**
     * Construct an XSLT filter with the given XSLT source and result.
     * 
     * @param xsltFile the XSLT file
     * @param writer the writer to which the transformed XML will be written
     * @throws TransformerException if an error occurs with the XSLT source
     */
    public XSLTFilter(File xsltFile, Writer writer) throws TransformerException {
        this(new StreamSource(xsltFile), new StreamResult(writer), null);
    }

    /**
     * Construct an XSLT filter with the given XSLT source and result.
     * 
     * @param xsltFile the XSLT file
     * @param writer the writer to which the transformed XML will be written
     * @param params the parameters to pass to the transformer
     * @throws TransformerException if an error occurs with the XSLT source
     */
    public XSLTFilter(File xsltFile, Writer writer, Map<String, String> params)
            throws TransformerException {
        this(new StreamSource(xsltFile), new StreamResult(writer), params);
    }

    /**
     * Construct an XSLT filter with the given template source and result.
     * 
     * @param xslt the XSLT template rules
     * @param output the result to which the transformed XML will be written
     * @param params the parameters to pass to the transformer
     * @throws TransformerException if an error occurs with the source
     */
    public XSLTFilter(Source xslt, Result output, Map<String, String> params)
            throws TransformerException {
        xsltSource = xslt;
        out = output;
        parameters = params;
        initialize();
    }

    /*
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        handler.characters(arg0, arg1, arg2);
        super.characters(arg0, arg1, arg2);
    }

    /*
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        handler.endDocument();
        super.endDocument();
    }

    /*
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        handler.endElement(arg0, arg1, arg2);
        super.endElement(arg0, arg1, arg2);
    }

    /*
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String arg0) throws SAXException {
        handler.endPrefixMapping(arg0);
        super.endPrefixMapping(arg0);
    }

    /*
     * @see javax.xml.transform.ErrorListener#error(TransformerException)
     */
    public void error(TransformerException e) throws TransformerException {
        throw e;
    }

    /*
     * @see javax.xml.transform.ErrorListener#fatalError(TransformerException)
     */
    public void fatalError(TransformerException e) throws TransformerException {
        throw e;
    }

    /**
     * Returns the parameters passed to the XSLT engine as a map.
     * 
     * @return parameters passed to the XSLT
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /*
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        handler.ignorableWhitespace(arg0, arg1, arg2);
        super.ignorableWhitespace(arg0, arg1, arg2);
    }

    /**
     * Initializes the XSLT transformer.
     * 
     * @throws TransformerException if an error occurs with the XSLT source
     */
    protected void initialize() throws TransformerException {
        // create a factory
        SAXTransformerFactory stf = 
        	(SAXTransformerFactory) new net.sf.saxon.TransformerFactoryImpl();
        stf.setErrorListener(this);

        // create the ContentHandler for use in this filter
        handler = stf.newTransformerHandler(xsltSource);
        handler.setResult(out);
        // get the transformer to set properties and parameters
        Transformer t = handler.getTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        // set parameters if they are present
        if (parameters != null && parameters.size() > 0) {
            Iterator<String> i = parameters.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                t.setParameter(key, parameters.get(key));
            }
        }
    }

    /*
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        handler.processingInstruction(arg0, arg1);
        super.processingInstruction(arg0, arg1);
    }

    /*
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator arg0) {
        handler.setDocumentLocator(arg0);
        super.setDocumentLocator(arg0);
    }

    /**
     * Sets the parameters passed to the XSLT engine.
     * 
     * @param parameters a map containing key/value pairs of
     * <code>String<code>s that are passed to the XSLT template
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /*
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String arg0) throws SAXException {
        handler.skippedEntity(arg0);
        super.skippedEntity(arg0);
    }

    /*
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        handler.startDocument();
        super.startDocument();
    }

    /*
     * @see org.xml.sax.ContentHandler#startElement(String, String, String,
     * Attributes)
     */
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        handler.startElement(arg0, arg1, arg2, arg3);
        super.startElement(arg0, arg1, arg2, arg3);
    }

    /*
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        handler.startPrefixMapping(arg0, arg1);
        super.startPrefixMapping(arg0, arg1);
    }

    /*
     * @see javax.xml.transform.ErrorListener#warning(TransformerException)
     */
    public void warning(TransformerException e) throws TransformerException {
        throw e;
    }

}