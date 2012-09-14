package up2p.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;



/*import com.sun.org.apache.xml.internal.serialize.LineSeparator;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.Method;*/

import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.XSLTFilter;

/**
 * Helper methods for JSPs and XML tools.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class TransformerHelper {
    /** Encoding used for plain output methods. */
    public static final String PLAIN = "UTF-8";
    
    
    public static final String PROPERTY_JAXP_SCHEMA_SOURCE =  "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String PROPERTY_JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * Transforms the input to the output using the given encoding scheme.
     * 
     * @param xmlDOM the XML to output
     * @param encoding the encoding scheme to use on the output
     * @param out the result that will receive the output
     * @param normalizeLR <code>true</code> if the carriage returns and line
     * feeds are to be normalized to one output method (UNIX platform)
     * @throws IOException if an error occurs transforming the XML
     */
    public static void encodedTransform(Document xmlDOM, String encoding,
            OutputStream out, boolean normalizeLR) throws IOException {
        encodedTransform(xmlDOM.getDocumentElement(), encoding, out,
                normalizeLR);
    }

    /**
     * Transforms the input to the output using the given encoding scheme.
     * 
     * @param xmlNode the XML to output
     * @param encoding the encoding scheme to use on the output
     * @param out the result that will receive the output
     * @param normalizeLR <code>true</code> if the carriage returns and line
     * feeds are to be normalized to one output method (UNIX platform)
     * @throws IOException if an error occurs transforming the XML
     */
    public static void encodedTransform(Element xmlNode, String encoding,
            OutputStream out, boolean normalizeLR) throws IOException {
        
        TransformerFactory transformerFactory = TransformerFactory
        .newInstance();
        try {
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.transform(new DOMSource(xmlNode), new StreamResult(out));
        } catch (Exception e) {
	    	throw new IOException(e.getMessage());
	    }

    }

    /**
     * Transforms the input to the output using the given encoding scheme.
     * 
     * @param xmlNode the XML to output
     * @param encoding the encoding scheme to use on the output
     * @param out the output writer to write to
     * @param normalizeLR <code>true</code> if the carriage returns and line
     * feeds are to be normalized to one output method (UNIX platform)
     * @throws IOException if an error occurs transforming the XML
     */
    public static void encodedTransform(Node xmlNode, String encoding,
            Writer out, boolean normalizeLR) throws IOException {
    	
    	TransformerFactory transformerFactory = TransformerFactory
        .newInstance();
        try {
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.transform(new DOMSource(xmlNode), new StreamResult(out));
        } catch (Exception e) {
	    	throw new IOException(e.getMessage());
	    }
    }

    /**
     * Returns an <code>OutputFormat</code> that has the given parameters.
     * 
     * @param encoding the encoding used in the output
     * @param indent <code>true</code> for pretty printing
     * @param omitDeclaration <code>true</code> to omit the XML declaration
     * @param normalizeLR <code>true</code> if the carriage returns and line
     * feeds are to be normalized to one output method (UNIX platform)
     * @return a format for the given parameters
     */
    public static OutputFormat getOutputFormat(String encoding, boolean indent,
            boolean omitDeclaration, boolean normalizeLR) {
        OutputFormat format = new OutputFormat(Method.XML, encoding, indent);
        if (normalizeLR)
            format.setLineSeparator(LineSeparator.Unix);
        format.setLineWidth(0);
        format.setOmitXMLDeclaration(omitDeclaration);
        format.setPreserveSpace(true);
        return format;
    }

    /**
     * Returns a SAX XML parser that does not validate.
     * 
     * @return a SAX XML reader
     */
    public static XMLReader getXMLReader() {
        return getXMLReader(null);
    } 
    
    /**
     * Returns a SAX XML parser that uses the given schema location.
     * 
     * @param schemaLocation the location of the community schema
     * @return a SAX XML reader that uses the given schema
     */
    public static XMLReader getXMLReader(String schemaLocation) {
        // create a SAX parser
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        
        if (schemaLocation != null)
            spf.setValidating(true);
        else
            spf.setValidating(false);
        
        XMLReader reader = null;
        try {
            SAXParser parser = spf.newSAXParser();
            reader = parser.getXMLReader();
            if (schemaLocation != null) {
            	parser.setProperty(PROPERTY_JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            	parser.setProperty(PROPERTY_JAXP_SCHEMA_SOURCE, schemaLocation);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     * Returns an empty DOM.
     * 
     * @return an empty Document
     */
    public static Document newDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
        }
        return db.newDocument();
    }

    /**
     * Parses an XML document from a file and returns the XML. The XML is parsed
     * using a non-validating, namespace-aware parser.
     * 
     * @param inputFile the file to parse
     * @return the parsed XML document
     * @throws SAXException when there is an error in parsing the XML
     * @throws IOException when there is an error reading from the stream
     */
    public static Document parseXML(File inputFile) throws SAXException,
            IOException {
        return parseXML(new FileInputStream(inputFile));
    }
    
    /**
     * Parses an XML document from an input stream and returns the XML. The XML
     * is parsed using a non-validating, namespace-aware parser.
     * 
     * @param in the stream to read the XML from
     * @return the parsed XML document
     * @throws SAXException when there is an error in parsing the XML
     * @throws IOException when there is an error reading from the stream
     */
    public static Document parseXML(InputStream in) throws SAXException,
            IOException {
    try {	
    	  
	      
    	
    	// setup parser
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        
            db = dbf.newDocumentBuilder();
        
            DOMImplementation impl = db.getDOMImplementation();
            
  	    
            // parse the document
            return db.parse(in);

        } catch (ParserConfigurationException e) {
        	System.out.println(">>>>>>>>>>>>>>>>>>>>>ParserConfigurationException>>>>>>>>>");
        }


        return null;
    }

    /**
     *  modifies the attachments of a document using the input prefix (old prefix is "file:")
     * @param indoc
     * @param urlprefix
     * @return
     */
    public static Document attachmentReplace(Document indoc, String urlprefix)  {
    	// Create the NodeIterator
        DocumentTraversal traversable = (DocumentTraversal) indoc;
        NodeIterator iterator = traversable.createNodeIterator(
         indoc, NodeFilter.SHOW_TEXT, null, true);

        // Iterate over the text
        Node node;
        String content;
        String filename;
        while ((node = iterator.nextNode()) != null) {
        	// Replace attachments beginning with file:
        	content = node.getNodeValue();
        	if (content != null //shouldn't happen...
        			&& content.startsWith("file:")) {//attachment
        		if (content.lastIndexOf("/")==-1) {//no slashes in the url, it's file:fname.ext
        			filename= content.substring(5);
        		} else // url of the form file:[stuff]/fname.ext
        			filename= content.substring(content.lastIndexOf("/")+1);
        		node.setNodeValue(urlprefix+filename);
        	}
        }
        
        // Use a second iterator to iterate over all element nodes,
        // and process their attributes
        iterator = traversable.createNodeIterator(
                indoc, NodeFilter.SHOW_ELEMENT, null, true);
        while ((node = iterator.nextNode()) != null) {
        	NamedNodeMap attributes = node.getAttributes();
        	if(attributes != null) {
            	for(int i = 0; i < attributes.getLength(); i++) {
            		Node attr = attributes.item(i);
            		content = attr.getNodeValue();
                	if (content != null //shouldn't happen...
                			&& content.startsWith("file:")) {//attachment
                		if (content.lastIndexOf("/")==-1) {//no slashes in the url, it's file:fname.ext
                			filename= content.substring(5);
                		} else // url of the form file:[stuff]/fname.ext
                			filename= content.substring(content.lastIndexOf("/")+1);
                		attr.setNodeValue(urlprefix+filename);
                	}
            	}
        	}
        }
        
        return indoc; 
    }
    
    
    /**
     * Parses an XML document from a string and returns the XML. The XML is
     * parsed using a non-validating, namespace-aware parser.
     * 
     * @param xmlString the string containing XML content
     * @return the parsed XML document
     * @throws SAXException when there is an error in parsing the XML
     * @throws IOException when there is an error reading from the stream
     */
    public static Document parseXML(String xmlString) throws SAXException,
            IOException {
        // setup parser
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
        }

        // parse the document
        return db.parse(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Transforms the input to the output using a plain serializer from the JAXP
     * Transformer interface and UTF-8 encoding.
     * 
     * @param xmlNode the XML to output
     * @param out the writer to write the output to
     */
    public static void plainTransform(Node xmlNode, Writer out) {
        try {
            encodedTransform(xmlNode, PLAIN, out, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method for JSPs to transform XML into HTML.
     * 
     * @param xmlInput the XML to be transformed
     * @param xsltInput the XSLT stylesheet
     * @param output the writer to direct the output to
     * @throws FileNotFoundException if the file is not found
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static void transform(File xmlInput, File xsltInput, Writer output)
            throws FileNotFoundException, SAXException, IOException,
            TransformerException {
        transform(xmlInput, xsltInput, output, null);
    }

    /**
     * Helper method for JSPs to transform XML into HTML.
     * 
     * @param xmlInput the XML to be transformed
     * @param xsltInput the XSLT stylesheet
     * @param output the writer to direct the output to
     * @param paramTable a table of name <code>String</code> value pairs that
     * should be passed as parameters to the XSLT transformer
     * @throws FileNotFoundException if the source or transform file are not
     * found
     * @throws SAXException if an error occurs during the transform
     * @throws IOException if an error occurs reading or writing to a stream
     * @throws TransformerException if an error occurs getting the XSLT filter
     */
    public static void transform(File xmlInput, File xsltInput, Writer output,
            Map<String, String> paramTable) throws FileNotFoundException, SAXException,
            IOException, TransformerException {
        transform(new InputSource(new FileInputStream(xmlInput)), xsltInput,
                output, paramTable);
    }

    /**
     * Helper method for JSPs to transform XML into HTML.
     * 
     * @param xmlInput the XML to be transformed
     * @param xsltInput the XSLT stylesheet
     * @param output the writer to direct the output to
     * @param paramTable a table of name <code>String</code> value pairs that
     * should be passed as parameters to the XSLT transformer
     * @throws FileNotFoundException if the source or transform file are not
     * found
     * @throws SAXException if an error occurs during the transform
     * @throws IOException if an error occurs reading or writing to a stream
     * @throws TransformerException if an error occurs getting the XSLT filter
     */
    public static void transform(InputSource xmlInput, File xsltInput,
            Writer output, Map<String, String> paramTable) throws FileNotFoundException,
            SAXException, IOException, TransformerException {
        if (xsltInput == null || !xsltInput.exists()) {
            throw new FileNotFoundException("Error transforming XSLT file "
                    + xsltInput);
        } else if (xmlInput == null) {
            throw new FileNotFoundException(
                    "Error getting XML source for transformation. Source file: "
                            + xmlInput);
        }

        // create a chain of filters
        DefaultResourceFilterChain chain = new DefaultResourceFilterChain();

        // create an XSLT filter
        XSLTFilter filter1 = new XSLTFilter(xsltInput, output, paramTable);
        chain.addFilter(filter1);

        // create a reader and parse the XML
        XMLReader reader = getXMLReader();
        chain.doFilter(reader, xmlInput);
    }

    /**
     * Helper method for JSPs to transform XML into HTML.
     * 
     * @param resourceURL URL to the XML source
     * @param xsltFile XSLT file
     * @param out output for the transform
     * @throws FileNotFoundException if the source or transform file are not
     * found
     * @throws SAXException if an error occurs during the transform
     * @throws IOException if an error occurs reading or writing to a stream
     * @throws TransformerException if an error occurs getting the XSLT filter
     */
    public static void transform(String resourceURL, File xsltFile, Writer out)
            throws FileNotFoundException, SAXException, IOException,
            TransformerException {
        TransformerHelper.transform(new InputSource(resourceURL), xsltFile,
                out, null);
    }

    /**
     * Helper method for JSPs to transform XML into HTML.
     * 
     * @param resourceURL URL to the XML source
     * @param xsltFile XSLT file
     * @param out output for the transform
     * @param params table of parameters to pass to the stylesheet
     * @throws FileNotFoundException if the source or transform file are not
     * found
     * @throws SAXException if an error occurs during the transform
     * @throws IOException if an error occurs reading or writing to a stream
     * @throws TransformerException if an error occurs getting the XSLT filter
     */
    public static void transform(String resourceURL, File xsltFile, Writer out,
            Map<String, String> params) throws FileNotFoundException, SAXException,
            IOException, TransformerException {
        TransformerHelper.transform(new InputSource(resourceURL), xsltFile,
                out, params);
    }
    
    /**
     *  Transform a DOM Document into a DOM Node (in fact should also be a document -  i just can't guarantee that) using a stylesheet file
     * @param sourceDoc the input Document
     * @param stylesheetFile the file containing the xsl templates
     * @return the result Node 
     * 
     */
    public static Node transform(Node sourceDoc, File stylesheetFile, Map<String,String> params) throws DOMException{
	    // Set up the XSLT stylesheet for use with Xalan-J 2
	    TransformerFactory transformerFactory =
        	(SAXTransformerFactory) new TransformerFactoryImpl();
	    Templates stylesheet;
		try {
			stylesheet = transformerFactory.newTemplates(
			        new StreamSource(stylesheetFile));
	    Transformer processor = stylesheet.newTransformer();
	    
	    Source xmlSource = new DOMSource(sourceDoc);
	    
	    DOMResult xmlResult = new DOMResult();
	    
	    for(String key: params.keySet()){
	    	processor.setParameter(key, params.get(key));
	    }
	    processor.transform(xmlSource, xmlResult);
	    
	    Node resultDoc = xmlResult.getNode();
	    
	    return resultDoc;
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}