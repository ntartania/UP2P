package up2p.tspace;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import lights.Field;
import lights.Tuple;
import lights.extensions.XMLField;
import lights.interfaces.ITuple;

public class TupleFactory {

	//the different possible verbs
	public static final String LOOKUPXPATH = "LookupXpath";
	public static final String LOOKUPXPATHANSWER = "LookupXpathAnswer";
	public static final String SEARCHXPATH = "SearchXpath";
	public static final String SEARCHXPATHANSWER = "SearchXpathAnswer";
	public static final String SEARCHXPATH_NORESULT = "SearchXPathNoResult";
	public static final String REMOVE = "Remove";
	public static final String RESID_FROM_URI = "ResIDFromURI"; //function
	public static final String COMMUNITY_FROM_URI = "CommunityFromURI"; //function
	public static final String GET_RESID_FROM_URI = "GetResIdFromURI";
	public static final String GET_COMMFROMURI = "GetCommunityFromURI";
	public static final String PUBLISH = "PublishXML";
	public static final String NOTIFY_ERROR = "NotifyError";
	public static final String NOTIFY_UI = "NotifyUI";
	public static final String FILEMAP = "MapFile";
	public static final String GETLOCAL = "GetLocal";
	public static final String GETLOCALRESPONSE = "GetLocalR";
	public static final String GETLOCALCOMM = "GetLocalCommunity";
	public static final String GETLOCALCOMMRESPONSE = "GetLocalCommunityR";
	public static final String RESOLVEURI = "ResolveURI";
	public static final String CONCAT = "Concatenate";
	public static final String CONCATANSWER = "ConcatenateAnswer";
	public static final String COMMUNITY_NOT_FOUND = "CommunityNotFound";
	public static final String RESOURCE_NOT_FOUND = "ResourceNotFound";
	public static final String LOOKUPSEARCHRESP = "LookupSearchResponse"; //to lookup metadata of searchresponses
	public static final String LOOKUPSEARCHRESP_ANS = "LookupSearchResponseAnswer";
	public static final String LOCALSYNCSEARCH = "SynchroLocalSearch";
	public static final String LOCALSYNCSEARCHRESPONSE = "SynchroLocalSearchResp";
	public static final String CR_TO_URI = "CRtoURI"; //function to compose a URi from Community / Resource
	public static final String CR_TO_URI_RESP = "CRtoURIResp"; //response to the above
	public static final String URI_TO_CR = "URItoCR"; //function to decompose a URI into Community / Resource
	public static final String URI_TO_CR_RESP = "URItoCRResp"; //resp[onse to the above
	public static final String COMPLEXQUERY = "ComplexQuery"; // trigger of a complex query. Parameters are in the subqueries, and trigger ID
	public static final String NETRESOURCELIST = "NetworkResourceList";
	public static final String TRUSTMETRIC = "TrustMetric";
	public static final String BROWSELOCALCOMMUNITY = "BrowseLocalCommunity";
	public static final String BROWSELOCALCOMMUNITYANSWER = "BrowseLocalCommunityAnswer";
	public static final String UPDATESUBNETS = "UpdateSubnets";
	
	/**
	 * Generated when a relay message is received by the JTellaAdapter, and specifies
	 * that a file download should be initiated by the web adapter through a relay
	 * peer.
	 * 2 args:
	 * 1 - Peer ID - The peer identifier (hostname:port/urlPrefix) of the peer actually serving files
	 * 				 (used to determine which resources to request)
	 * 2 - Relay URL - The URL that should be used to connect to the relay peer (provided in the
	 * 				   received relay message)
	 * 3 - Relay peer identifier - Integer provided in the received relay message
	 */
	public static final String RELAY_RECEIVED = "RelayReceived";
	
	public static final String NOTIFYCONNECTION = "NotifyConnectionChange"; 
	// a change in a network connection, opening or closing, comes with 4 fields:
	// IP, port, connectionType[OUTGOING / INCOMING], opening [true / false]
	
	/**
	 * No arguments, triggers shutdown of all application threads
	 * (Special case handled by UP2PWorker)
	 */
	public static final String SHUTDOWN = "Shutdown";
	
	/**
	 * A request for a PUSH message to be issued.
	 * 1 Arg:
     * 1 - Peer Id of the remote node to send a PUSH to (IP:port)
     */
	public static final String PUSHREQUEST = "PushRequest";
	
	/** 
	 * A request for the absolute file paths of a resource and all required attachments. This is
	 * primarily used to locate files for PUSH transfers.
	 * 3 Args:
	 * 1 - Community ID
	 * 2 - Resource ID
	 * 3 - Query Identifier
	 */
	public static final String GETRESOURCEFILEPATHS = "GetResourceFilePaths";
	
	/**
	 * A response to a resource file path lookup.
	 * 3 Args:
	 * 1 - Absolute file path of resource file
	 * 2 - Absolute file paths of attachment files (separated with ":::", as ":" is an illegal file path char in all major OS's)
	 * 3 - Query Identifier
	 * Note: Arguments 1 and 2 will be TupleFactory.RESOURCE_NOT_FOUND if the resource could not be found.
	 */
	public static final String GETRESOURCEFILEPATHSRES = "GetResourceFilePathsResponse";

	/** create a tuple template with a verb and n string fields
	 * @param verb the verb to place in the first field of the tuple
	 * @return howManyFields how many fields of type String to make as template 
	 */
	public static ITuple createQueryTupleTemplate(String verb, int howManyFields) {
		ITuple t = new Tuple();

		t.add(new Field().setValue(verb)); // the verb given as input 

		for (int i=0;i<howManyFields;i++){ //the n formal fields of the tuple, expecting the String arguments
			t.add(new Field().setType(String.class)); //new field of type "String"	
		}

		return t;
	}
	/** create a tuple with a verb and an array of objects
	 */
	public static ITuple createTuple(String verb, Object[] values) {
		ITuple t = new Tuple();

		t.add(new Field().setValue(verb)); // the verb given as input 

		for (Object o: values){ //TODO: check that o is not null and figure out what to do
			t.add(new Field().setValue(o)); //this sets also the class of the object
		}

		return t;
	}
	
	/** create a tuple template with a verb and an array of objects
	 */
	public static ITuple createTuple(List<String> fieldvalues) {
		
		ITuple t = new Tuple();

		for (String s: fieldvalues){
			t.add(new Field().setValue(s)); 
		}

		return t;


	}
	
	/** create a tuple template with a verb and an array of objects
	 */
	public static ITuple createTuple(String[] fieldvalues) {
		ITuple t = new Tuple();

		for (String s: fieldvalues){
			t.add(new Field().setValue(s)); 
		}

		return t;


	}
	
	/**
	 * Create a tuple of Type `SEARCH` from the provided fields
	 * @param communityId id of the community to search
	 * @param xPath XPATH expression to use as search criterion
	 * @param listener object to 
	 * @param qid
	 * @param extent	Determines what the scope of the search should be.
	 * 					This value should be one of HttpParams.UP2P_SEARCH_ALL,
	 * 					UP2P_SEARCH_NETWORK, or UP2P_SEARCH_LOCAL
	 * @return
	 */
	public static ITuple createSearchTuple(String communityId, String xPath, 
			String qid, int extent) {
		//define a tuple for this query.
			
		String extentString = Integer.toString(extent);
		String[] fields = new String[] {communityId, xPath, qid, extentString};
	
		 return createTuple(TupleFactory.SEARCHXPATH, fields);
	}
	
	/**template for xpath search tuple, for a specific extent. The Network worker uses this one.*/
	public static ITuple createSearchTemplate(int extent){
			
		 ITuple tt = createQueryTupleTemplate(TupleFactory.SEARCHXPATH, 3);////3+1 fields for the search: comId, xpath, qid, extent
		 tt.add(new Field().setValue(Integer.toString(extent)));
		 return tt;
	}
	
	/**template for xpath search tuple, for any search of any extent*/
	public static ITuple createSearchTemplate(){
			
		 ITuple tt = createQueryTupleTemplate(TupleFactory.SEARCHXPATH, 4);////3+1 fields for the search: comId, xpath, qid, extent
		 
		 return tt;
	}

	
	/** create a tuple notifying of a search response
	 * 
	 * @param comId communityId 
	 * @param id resource Id 
	 * @param title title of resource
	 * @param filename filename for resource
	 * @param location download link for resource
	 * @param qid id of corresponding search query
	 * @return a tuple representing this search response
	 */
	public static ITuple createSearchReply(String comId, String id , String title, String filename , String location, String qid){
		return createTuple(TupleFactory.SEARCHXPATHANSWER, new String [] {comId, id ,title , filename, location, qid});
	}
	
	/**
	 * create a tuple directly from a SearchResponse object
	 * @param response the object representing the searchresponse
	 * @param the first query id in the list of query Ids (once the response is stored other ids may be added) 
	 * @return a tuple with the search responses, no DOM field
	 */
	public static ITuple createSearchReply(SearchResponse response){
		 return createTuple(TupleFactory.SEARCHXPATHANSWER, new String [] {response.getCommunityId(), response.getId() , response.getTitle(), response.getFileName(), response.getLocations().get(0).getLocationString(), response.getQueryId()});
	}
	
	//Template for search responses
	public static ITuple createSearchReplyTemplate(){
		return createQueryTupleTemplate(TupleFactory.SEARCHXPATHANSWER, 6);////6 fields for the answer template: comId, rid, title, location, qid)
	}
	
	//Template for search responses
	public static ITuple createSearchReplyTemplateWithDOM(){
		ITuple tpl= createQueryTupleTemplate(TupleFactory.SEARCHXPATHANSWER, 6);////7 fields for the answer template: comId, rid, title, location, qid, DOM)
		tpl.add(createXMLTemplateField());
		return tpl;
	}

	//Template for search responses of a specific queryId
	public static ITuple createSearchReplyTemplate(String qid){
		ITuple tuple = createQueryTupleTemplate(TupleFactory.SEARCHXPATHANSWER, 5);////5 fields for the answer template: comId, rid, title, location)
		tuple.add(new Field().setValue(qid));// add the qid as actual field at the end
		return tuple;
	}
	
	//Template for search responses of a specific queryId
	public static ITuple createSearchReplyTemplateWithDOM(String qid){
		ITuple tuple = createQueryTupleTemplate(TupleFactory.SEARCHXPATHANSWER, 5);////5 fields for the answer template: comId, rid, title, location)
		tuple.add(new Field().setValue(qid));// add the qid as actual field at the end
		tuple.add(createXMLTemplateField());//new XMLField with wildcard
		return tuple;
	}
	/////////////////////////////////////////
	
	/**
	 *  create a tuple from a verb and a single value 
	 *  @return a Tuple with two fields, one containing the verb (a String) and the other the provided value
	 *  @param verb the verb to be placed as first field
	 *  @param singleValue the object to be placed in the second field of the tuple
	 */
	public static ITuple createTuple(String verb, Object singleValue) {
		ITuple t = new Tuple();

		t.add(new Field().setValue(verb)); // the verb given as input 

		
		t.add(new Field().setValue(singleValue)); //this sets also the class of the object
		

		return t;


	}


	/**
	 * Creates a template tuple with a (literal) verb and a number of formal fields of given classes.
	 * @param verb the verb for the request, i.e. a String that will be in the first field of the Tuple.
	 * @param classes the array of classes that the formal fields should match.
	 * @return the tuple
	 */
	public static ITuple createTemplate(String verb, Class<?>[] classes) {
			ITuple t = new Tuple();

			t.add(new Field().setValue(verb)); // the verb given as input 

			for (int i=0;i<classes.length;i++){ //the n formal fields of the tuple, expecting the String arguments
				t.add(new Field().setType(classes[i])); //new field of type [whatever class is classes[i]]	
			}

			return t;
		
	}

	/**
	 * Creates a template Tuple that matches a ``publish" request  
	 * @return the Tuple 
	 */
	public static ITuple createPublishTemplate() {
		//create a template for "Publish"+ two formal "String" fields  
		ITuple newtuple = TupleFactory.createTemplate(TupleFactory.PUBLISH, new Class[] {String.class, String.class});
		newtuple.add(createXMLTemplateField()); //add a formal field that matches any XML doc
		return newtuple;
	}

		
	/**
	 * creates a formal XMLField that matches any literal XMLField (any XMLField with a DOM value).
	 * @return the XMLField
	 */
	public static XMLField createXMLTemplateField (){
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = null;
		try {
			expr = xpath
					.compile("."); //the wildcard!! this formal field will match any field containing an XMLDocument
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		}
		return  new XMLField(expr); //if there's an error it will create a field with a null Xpath but it shouldn't happen
	}
	
	/**
	 *  create an enhanced search response, with the additional field containing the DOM.
	 * @param comId
	 * @param id
	 * @param title
	 * @param filename
	 * @param location
	 * @param qid
	 * @param resourceDOM
	 * @return
	 */
	public static ITuple createSearchReplyWithDOM(String comId, String id,
			String title, String filename, String location, String qid,
			Document resourceDOM) {
		ITuple tup = createSearchReply(comId, id, title, filename, location, qid);
		tup.add(new XMLField(resourceDOM));
		return tup;
	}
	
	/* maybe not...
	 * Generate a unique (enough) identifier for a query tuple
	 * 
	 * @return
	 * /
	public static String generateID(){
		return "00";
	}*/
	
	
}
