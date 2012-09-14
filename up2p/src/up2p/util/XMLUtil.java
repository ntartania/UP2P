package up2p.util;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {

	/**
	 * convenience method to generate an XPathExpression object from a string representing an Xpath
	 * @param pathstring the string
	 * @return an Xpath object, or null if there was an exception
	 */
	public static XPathExpression makeXPathFromString(String pathstring){
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = null;
		try {
			expr = xpath.compile(pathstring); 
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		}
		return  expr;
	}
	
	/**
	 *  convenience method to evaluate an xpath expression against a DOM document
	 * @param DOM the document
	 * @param xpath the xpath
	 * @return NodeList of retrieved nodes
	 * @throws XPathExpressionException
	 */
	public static NodeList evaluateXPath(Document DOM, XPathExpression xpath) throws XPathExpressionException{
		
		NodeList result = (NodeList) xpath.evaluate(DOM, XPathConstants.NODESET);
		
		return result;
	}
	
	/**
	 * convenience method to directly get the text content of an element from an XPAth.
	 * Better to use when you know that evaluating the xpath only returns one item.
	 * @param DOM
	 * @param xpath
	 * @return the string value of the XML Node obtained by evaluating the XPath against the Document.
	 */
	public static String getElementContentFromXPath(Document DOM, XPathExpression xpath) {
		
		try{
		Node thenode = evaluateXPath(DOM, xpath).item(0);
		if (thenode != null)
			return thenode.getNodeValue();
		else return "";
		}
		catch(XPathExpressionException e){
			return "";
		}
		
	}
}
