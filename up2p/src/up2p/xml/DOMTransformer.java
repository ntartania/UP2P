package up2p.xml;

	import java.io.File;
	import java.io.FileInputStream;
	import java.io.IOException;
	import java.io.OutputStream;

	import javax.xml.parsers.DocumentBuilder;
	import javax.xml.parsers.DocumentBuilderFactory;
	import javax.xml.parsers.FactoryConfigurationError;
	import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
	import javax.xml.transform.Source;
	import javax.xml.transform.Templates;
	import javax.xml.transform.Transformer;
	import javax.xml.transform.TransformerConfigurationException;
	import javax.xml.transform.TransformerException;
	import javax.xml.transform.TransformerFactory;
	import javax.xml.transform.dom.DOMResult;
	import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
	import javax.xml.transform.stream.StreamSource;

	import org.apache.xml.serialize.OutputFormat;
	import org.apache.xml.serialize.XMLSerializer;
	import org.w3c.dom.DOMException;
	import org.w3c.dom.DOMImplementation;
	import org.w3c.dom.Document;
	import org.w3c.dom.Element;
	import org.w3c.dom.NamedNodeMap;
	import org.w3c.dom.Node;
import org.xml.sax.SAXException;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

	public class DOMTransformer {

		
		public static Node transform(Node sourceDoc, File stylesheetFile) {
		    // Set up the XSLT stylesheet for use with Xalan-J 2
		    TransformerFactory transformerFactory =
		    TransformerFactory.newInstance();
		    Templates stylesheet;
			try {
				stylesheet = transformerFactory.newTemplates(
				        new StreamSource(stylesheetFile));
		    Transformer processor = stylesheet.newTransformer();
		    
		    Source xmlSource = new DOMSource(sourceDoc);
		    DOMResult xmlResult = new DOMResult();
		    
		    processor.transform(xmlSource, xmlResult);
		    
		    Node resultDoc = xmlResult.getNode();
		    //System.out.println(xmlResult.);
		    
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
		
		public static Document parseFileToDOM(String url){
			Document xml= null;
			
			try {
			      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			      factory.setNamespaceAware(true);
			      DocumentBuilder parser = factory.newDocumentBuilder();
			      
			      // Check for the traversal module
			      DOMImplementation impl = parser.getDOMImplementation();
			      if (!impl.hasFeature("traversal", "2.0")) {
			        System.out.println(
			         "A DOM implementation that supports traversal is required."
			        );  
			        return null;
			      }
			      
			      File inputfile = new File(url);
			      FileInputStream stream = new FileInputStream(inputfile);

			      /*if (inputfile.canRead()){
			    	  System.out.println("I can read the file");
			      }*/
			      
			      // Read the document
			      xml = parser.parse(stream); 
			      
			      System.out.println("node name: "+xml.getNodeName() + " getvalue: "+ xml.getNodeValue() + "class" + xml.getClass());
			      
			      	       
			    }
			    catch (SAXException e) {
			      System.out.println(e);
			      System.out.println(url + " is not well-formed.");
			    }
			    catch (IOException e) { 
			      System.out.println(
			       "Due to an IOException, the parser could not check " + url
			      ); 
			    }
			    catch (FactoryConfigurationError e) { 
			      System.out.println("Could not locate a factory class"); 
			    }
			    catch (ParserConfigurationException e) { 
			      System.out.println("Could not locate a JAXP parser"); 
			    }
			    
			    return xml;
		}
		
		
		
		 
		
		/*public static String getStringRep(Node node){
		switch(node.getNodeType()){
		case Node.ATTRIBUTE_NODE:
			return getStringRepAttribute(node);
		case Node.ELEMENT_NODE:
			return getStringRepElement((Element)node);
		default:
			return getStringRepDocument((Document)node);
		}
		
		public static String getStringRepElement(Element node){
			String toreturn ="<"+node.getNodeName();
			if (node.hasAttributes()){
				NamedNodeMap attlist = node.getAttributes();
				for (int i = 0; i<attlist.getLength();i++){
					toreturn = toreturn + getStringRep(attlist.item(i));	
				}
				toreturn = toreturn + ">"+ node.getNodeValue()+ "</"+ node.getNodeName() + ">" ;
			}
			return toreturn;
		}
		
		public static String getStringRepAttribute(Node attrib){
			return " "+ attrib.getNodeName() +"=" + "\""+attrib.getNodeValue() +"\"";
		}
		*/
		
		public static String basicStringRep(Node node){
			String toreturn = node.getNodeName() +"="; 
			try{
				toreturn += node.getNodeValue();
			}
			catch(DOMException e){
				toreturn += "[no value]";
			}
			return toreturn;
		}
		
		public static void prettyPrintNode(Node doc, OutputStream out) {
	        
	        TransformerFactory tfactory = TransformerFactory.newInstance();
	        Transformer serializer;
	        try {
	            serializer = tfactory.newTransformer();
	            //Setup indenting to "pretty print"
	            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
	            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	            
	            serializer.transform(new DOMSource(doc), new StreamResult(out));
	        } catch (TransformerException e) {
	            // this is fatal, just dump the stack and throw a runtime exception
	            e.printStackTrace();
	            
	            
	        }
	    }
		
		public static void prettyPrint (Document arg, OutputStream out){
			// Serialize the document
		      OutputFormat format = new OutputFormat(arg);
		      format.setLineWidth(40);
		      format.setIndenting(true);
		      format.setIndent(2);
		      XMLSerializer serializer = new XMLSerializer(out, format);
		      try {
				serializer.serialize(arg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public static void prettyPrint (Document arg){
		prettyPrint(arg, System.out);
		}
	}


