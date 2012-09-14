package up2p.rdf;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.openrdf.model.URI;

/**
 * UP2P URIs : local implementation of the openrdf.model URI interface.
 * 
 * The UP2P URIs are of the form :
 * up2p:communityid/resourceid
 * OR
 * up2p:communityid/?/xpath
 * 
 * 
 * @author alan
 *
 */
public class Up2pURI extends Up2pRDFResource implements URI {

	
	private static final int QMARK = 38;
	private static final int COM1 = 5; //index1 for communityid
	private static final int COM2 = 37; // index2 for communityid
	

	public Up2pURI(String urii){
		super(urii);
	}
	
	/**
	 * Check if this URI is that of an xpath-based predicate (up2p:communityid/?/xpath)
	 * TODO: use a regular-expression to identify this
	 * @return true if the URI is of the form "up2p:communityid/?/xpath" 
	 */
	public boolean isXpathURI(){
		Pattern xpathuri = Pattern.compile("up2p:[a-f0-9]+/\\?/.*"); //TODO: figure out how to say : 32 times a pattern.
		Matcher mmm = xpathuri.matcher(stringValue);
		return (mmm.matches() && stringValue.substring(QMARK, QMARK +1).equals("?"));
/*		if (stringValue.substring(QMARK, QMARK +1).equals("?")){ //TODO: do it with a reg expr.
			return true;
		}
		return false;*/
	}
	
	/**
	 * Check if this URI identifies a UP2P document (if it's well formed... not if the document actually exists.)
	 * @return
	 */
	public boolean isDocumentURI(){
		Pattern docuri = Pattern.compile("up2p:[a-f0-9]+/[a-f0-9]+"); //todo: figure out how to say : 32 times a pattern.
		Matcher mmm = docuri.matcher(stringValue);
		return (mmm.matches() && stringValue.substring(QMARK-1, QMARK).equals("/"));	}
	
	/**
	 * get the community Identifier from the URI up2p:communityId/[whatever]
	 * @return communityId
	 */
	public String getCommunity() {
		return stringValue.substring(COM1,COM2); //TODO: check these values and do it with a reg exp, rather.
	}
	
	/**
	 *  get the resource id from the URI up2p:communityId/resourceId
	 * @return resourceId
	 */
	public String getResourceId(){
		if (isDocumentURI()){
			return stringValue.substring(QMARK);
		} else {
			return ""; //TODO : make this an exception
		}
	}
	
	/**
	 *  get the xpath part of an xpath-type URI up2p:communityId/?/xpath
	 * @return
	 */
	public String getXpath() {
		if (isXpathURI()){
			
		return stringValue.substring(QMARK+1);
		} else {
			return ""; //TODO: make an exception
		}
	}
	/**
	 * returns the full URI (up2p:blah/blih or up2p:blah/?/bluh) 
	 */
	public String getLocalName() {
		
		return stringValue;
	}

	/**
	 * returns "up2p" 
	 */
	public String getNamespace() {
		
		return "up2p";
	}

}
