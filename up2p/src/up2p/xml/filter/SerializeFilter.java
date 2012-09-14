package up2p.xml.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Serializes a stream of XML events to a given output stream.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class SerializeFilter extends BaseResourceFilter {
    /** The serializer used to serialize the XML stream. */
    protected XMLSerializer serializer;

    /** The serializer as a content handler. */
    protected ContentHandler handler;

    /** The default encoding is ISO-8859-1. */
    public static final String ENCODING = "ISO-8859-1";

    /** By default spaces are preserved when serializing. */
    public static final boolean PRESERVE_SPACE = true;

    /** By default the XML declaration is omitted. */
    public static final boolean OMIT_XML_DECLARATION = true;

    /** By default the XML comments are omitted. */
    public static final boolean OMIT_COMMENTS = true;

    /** By default indenting is disabled. */
    public static final boolean INDENTING = false;

    /**
     * Creates a serializer with no options initialized.
     */
    public SerializeFilter() {
    }

    /**
     * Creates a serialize filter with default options for encoding and no
     * normalization.
     * 
     * @param out the output to which the XML is serialized
     */
    public SerializeFilter(OutputStream out) {
        try {
            initialize(out, false, ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a serialize filter with default options for encoding and no
     * normalization.
     * 
     * @param writer writer to which the XML is serialized
     */
    public SerializeFilter(Writer writer) {
        try {
            initialize(writer, false, ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a serialize filter with the given output format.
     * 
     * @param out the ouput to which the XML will be serialized
     * @param format the format for the serializer
     */
    public SerializeFilter(OutputStream out, OutputFormat format) {
        try {
            initialize(out, format);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a serializer with the given options.
     * 
     * @param out the output stream that will receive the serialized XML
     * @param normalize if <code>true</code> the serializer outputs a
     * canonical form of XML that uses Unix platform line feeds
     * @param encoding the encoding for the output
     */
    public SerializeFilter(OutputStream out, boolean normalize, String encoding) {
        try {
            initialize(out, normalize, encoding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a serializer with the given options.
     * 
     * @param out the output writer that will receive the serialized XML
     * @param normalize if <code>true</code> the serializer outputs a
     * canonical form of XML that uses Unix platform line feeds
     * @param encoding the encoding for the output
     */
    public SerializeFilter(Writer out, boolean normalize, String encoding) {
        try {
            initialize(out, normalize, encoding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the serializer.
     * 
     * @param out the output stream that will receive the serialized XML
     * @param normalize if <code>true</code> the serializer outputs a
     * canonical form of XML that uses Unix platform line feeds
     * @param encoding the encoding for the output
     * @throws IOException if there is an error creating the content handler
     */
    protected void initialize(OutputStream out, boolean normalize,
            String encoding) throws IOException {
        initialize(out, getFormat(normalize, encoding));
    }

    /**
     * Initializes the serializer.
     * 
     * @param writer the output writer that will receive the serialized XML
     * @param normalize if <code>true</code> the serializer outputs a
     * canonical form of XML that uses Unix platform line feeds
     * @param encoding the encoding for the output
     * @throws IOException if there is an error creating the content handler
     */
    protected void initialize(Writer writer, boolean normalize, String encoding)
            throws IOException {
        initialize(writer, getFormat(normalize, encoding));
    }

    /**
     * Initializes the serializer.
     * 
     * @param out the output stream to which the XML will be written
     * @param format the format for the serializer
     * @throws IOException if there is an error creating the content handler
     */
    protected void initialize(OutputStream out, OutputFormat format)
            throws IOException {
        // create the serializer
        serializer = new XMLSerializer(out, format);
        handler = serializer.asContentHandler();
    }

    /**
     * Initializes the serializer.
     * 
     * @param writer the writer to which the XML will be written
     * @param format the format for the serializer
     * @throws IOException if there is an error creating the content handler
     */
    protected void initialize(Writer writer, OutputFormat format)
            throws IOException {
        // create the serializer
        serializer = new XMLSerializer(writer, format);
        handler = serializer.asContentHandler();
    }

    /**
     * Gets an output format using the given parameters.
     * 
     * @param normalize true for normalization
     * @param encoding the encoding for the output
     * @return a format for the serializer
     */
    private OutputFormat getFormat(boolean normalize, String encoding) {
        // create the format
        OutputFormat format = new OutputFormat(Method.XML, encoding, false);
        if (normalize)
            format.setLineSeparator(LineSeparator.Unix);
        format.setLineWidth(0);
        format.setIndenting(INDENTING);
        format.setOmitXMLDeclaration(OMIT_XML_DECLARATION);
        format.setPreserveSpace(PRESERVE_SPACE);
        format.setOmitComments(OMIT_COMMENTS);
        return format;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        handler.characters(arg0, arg1, arg2);
        super.characters(arg0, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        handler.endDocument();
        super.endDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        handler.endElement(arg0, arg1, arg2);
        super.endElement(arg0, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String arg0) throws SAXException {
        handler.endPrefixMapping(arg0);
        super.endPrefixMapping(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        handler.ignorableWhitespace(arg0, arg1, arg2);
        super.ignorableWhitespace(arg0, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        handler.processingInstruction(arg0, arg1);
        super.processingInstruction(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator arg0) {
        handler.setDocumentLocator(arg0);
        super.setDocumentLocator(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String arg0) throws SAXException {
        handler.skippedEntity(arg0);
        super.skippedEntity(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        handler.startDocument();
        super.startDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(String, String, String,
     * Attributes)
     */
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        handler.startElement(arg0, arg1, arg2, arg3);
        super.startElement(arg0, arg1, arg2, arg3);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        handler.startPrefixMapping(arg0, arg1);
        super.startPrefixMapping(arg0, arg1);
    }

}