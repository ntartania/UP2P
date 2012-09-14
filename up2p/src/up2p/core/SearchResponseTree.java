package up2p.core;

import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import lights.Field;
//import lights.TupleSpace;
import lights.extensions.XMLField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.search.TrustMetric;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PWorker;
import up2p.xml.TransformerHelper;

public class SearchResponseTree implements SearchResponseListener {

	/** Logger used by this thing. */
    public static Logger LOG = Logger.getLogger(WebAdapter.LOGGER);
    
    /** a DB to store the results: communityId - resourceId - SearchResponse
     * question: how about accessing the location ?
     * perhaps I can store the filename / locations / etc in attributes ?
     * [probably not necessary] 
     * */
    private Map<String,Map<String,SearchResponse>> responseDB;
    
    private Map<String, List<SearchResponse>> indexByQueryId;
    
    /**
     * A List of the trust metrics that have been processed for the current search. This list
     * is used to update SearchResponses whenever SearchResponses are fetched for use outside
     * this class.
     */
    private List<TrustMetric> trustMetrics;
    
    private SearchResponseManager respManagerWorker; 
    
    /** Flag to indicate if a new response has arrived since the last fetch of responses */
    boolean newResponse;
    
    /** Flag to indicate if a new trust metric has arrived since the last fetch of responses */
    boolean newMetric;
    
    /** The last search query initiated by the user, stored as a map of values keyed by XPaths */
    private Map<String, String> lastQueryMap;
    
    // this guy will have its own worker to read and store search results as they come into the TS
    private class SearchResponseManager extends UP2PWorker {

    	public SearchResponseManager(ITupleSpace ts){
    		super(ts);
    		name = "RESPMANAGERWorker";
    		addQueryTemplate(TupleFactory.createSearchReplyTemplateWithDOM());
    		//new lookup verb specific to search responses [mostly for dynamically created agents]
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.LOOKUPSEARCHRESP, 4));
    		
    		//this is an extra template for lookup retries!
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.LOOKUPSEARCHRESP, 5));
    		
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.TRUSTMETRIC, 4));
    		
    		addQueryTemplate(TupleFactory.createPublishTemplate());
    		
    		newResponse = false;
    		newMetric = false;
    		
    	}
		@Override
		protected List<ITuple> answerQuery(ITuple template_not_used, ITuple tu) {
			
			// TODO Auto-generated method stub
			List<ITuple> ansTuple= new ArrayList<ITuple>(); //will be created by the factory according to the query
			String verb = ((Field) tu.get(0)).toString(); //what did we just read?
			
			
			if (verb.equals(TupleFactory.SEARCHXPATHANSWER)){ //a searchResponse!
				
				//each tuple : result of search
				String comId = ((Field) tu.get(1)).toString();
				String resId = ((Field) tu.get(2)).toString();
				String title = ((Field) tu.get(3)).toString();
				String fname = ((Field) tu.get(4)).toString();
				String location = ((Field) tu.get(5)).toString();
				String qid = ((Field) tu.get(6)).toString();
				
				LOG.debug(name + ": answerQuery - SEARCHXPATHANSWER\tqid: " + qid + "\tcomid: " + comId
						+"\tresid: " + resId + "\tfname: " + fname);

				// indicate the location for the file (URL)
				LocationEntry[] locations = new LocationEntry[1];
				locations[0] = new LocationEntry(location);
				
				SearchResponse newresponse = new SearchResponse(resId , title, comId, fname, locations, false, qid);
				if (tu.length()>7){// the searchresponse has metadata !
					LOG.debug(name+": search response has metadata");
					Object metaobj = ((XMLField) tu.get(7)).getValue();
					
					// Add the metadata to the search response
					Document metadata = (Document) metaobj;
					newresponse.addResourceDOM(metadata);
					
					// Output a new tuple with the metadata DOM removed. This allows 
					// agents who expect a 6 field response tuple (no metadata) to process 
					// the tuple (primarily the complex graph query agents)
					ITuple noDomTuple = TupleFactory.createTuple(TupleFactory.SEARCHXPATHANSWER, 
							new String[]{comId, resId, title, fname, location, qid});
					ansTuple.add(noDomTuple);
					
					// Prefix the attachments with "attach://" then add to search response object
					
					// TODO: Running the attachment replace transform was causing a bug where the RESPMANAGER
					//       worker would process the same SEARCHXPATHANSWER tuple over and over again.
					//       Possibly caused by modifying the document metadata of the tuple?
					
					// newresponse.addResourceDOM(TransformerHelper.attachmentReplace(metadata, "attach://"));

					//we only add one response at a time since we're only treating one tuple at a time!
					receiveSearchResponse(new SearchResponse [] {newresponse});
				} else  {
					LOG.debug(name+" got response with no metadata, won't do anything with it");
				}
			
			} else if (verb.equals(TupleFactory.LOOKUPSEARCHRESP)){
				//extract the three arguments of the query
				String resId = ((Field) tu.get(1)).toString();
				String comId = ((Field) tu.get(2)).toString();
				String xpath = ((Field) tu.get(3)).toString();
				String qid = ((Field) tu.get(4)).toString();

				//call the regular method

				List<String> ans = lookupXPathLocation(comId,resId,xpath);

				if (ans.isEmpty()){ //no answer: most likely the searchReponse hasn't arrived. We still want to return something so that the synchronous lookup doesn't get stuck
					ansTuple.add(TupleFactory.createTuple(TupleFactory.LOOKUPSEARCHRESP_ANS, ""));
					if (tu.length()==5) {//if this is NOT a retry we retry the query, just so that it can come back a second time.
						//This is a hack because sometimes the LookupSearchReponse arrive before the corresponding SearchResponse, which is slower to process.
						ITuple retryquery = (ITuple) tu.clone(); 
						ansTuple.add(retryquery.add(new Field().setValue("retry")));
					}
				} else
					//create a tuple with the answer(s)
					for (String s:ans) { 
						ansTuple.add(TupleFactory.createTuple(TupleFactory.LOOKUPSEARCHRESP_ANS, new String[]{resId, comId, xpath, s, qid}));
						LOG.debug(name + " has answer:"+s);
					}
				
			} else if (verb.equals(TupleFactory.TRUSTMETRIC)) {
				
				// Extract the four arguments of the trust metric
				String peerId = ((Field) tu.get(1)).toString();
				String communityId = ((Field) tu.get(2)).toString();
				String metricName = ((Field) tu.get(3)).toString();
				String metricValue = ((Field) tu.get(4)).toString();
				
				// Generate the trust metric object
				newMetric = true;
				TrustMetric metric = new TrustMetric(peerId, communityId,
						metricName, metricValue);
				
				// Discard any previous instances of this same metric
				Iterator<TrustMetric> metricIter = trustMetrics.iterator();
				while(metricIter.hasNext()) {
					TrustMetric other = metricIter.next();
					if(metric.isSameMetricType(other)) {
						metricIter.remove();
					}
				}
				
				// Add the new trust metric to the list of stored metrics
				trustMetrics.add(new TrustMetric(peerId, communityId,
						metricName, metricValue));
						
			} else if (verb.equals(TupleFactory.PUBLISH)){
				// A resource was successfully published, set the resource as local in any
				// stored search responses
				String comId = ((Field) tu.get(1)).toString();
				String resId = ((Field) tu.get(2)).toString();
				setLocal(comId, resId);
			}
			
			return ansTuple; //in fact we don't need to return anything.
		}
    	
    }
	
	private String localhost;
	//private int localport;
	
	//private String localPrefix = "not set";

	/**
	 * clear response list
	 * @param qid 
	 */
	public synchronized void clear(String qid){
		List<String> queryIdList;
		if (!indexByQueryId.containsKey(qid)) //if the previous query had no responses
			return;
			
		for (SearchResponse r: indexByQueryId.get(qid)){
			//loop through the SearchReponses pointed by the index
			queryIdList=r.getAllQueryIds();
			queryIdList.remove(qid); //this SearchResponse is no longer needed as a response for qid
			if (queryIdList.isEmpty()){
				//remove this from the map where it's stored:
				//the map is responseDB->communityId, 
				//and the response is stored with the key resourceId within this map
				Map<String,SearchResponse> map = responseDB.get(r.getCommunityId());
				if (map!= null) //defensive
					map.remove(r.getId()); //remove the SearchResponse from the map where it's stored
				
			}
			
		}
		indexByQueryId.remove(qid); //remove the list of searchResponses from the index as well.
		//responseList = new HashMap<String, SearchResponse>();
	}
	
	 
	public void setLocalLocationString(String host){
		localhost = host;
	}
	
	public SearchResponseTree(){
		// load the WebAdapter logger
        LOG = Logger.getLogger(WebAdapter.LOGGER);
        localhost= "not set";
        // localport= 0;
        // initialize the maps
		responseDB = new TreeMap<String,Map<String,SearchResponse>>();
		indexByQueryId = new TreeMap<String, List<SearchResponse>>();
		trustMetrics = new ArrayList<TrustMetric>();
		lastQueryMap = new HashMap<String, String>();
	}
	
	public synchronized void clearAll(){
		// Explicitly set to null as a hint for the garbage collector
		responseDB = null;
		indexByQueryId = null;
		trustMetrics = null;
		
		// Generate new collections
		responseDB = new TreeMap<String,Map<String,SearchResponse>>();
		indexByQueryId = new TreeMap<String, List<SearchResponse>>();
		trustMetrics = new ArrayList<TrustMetric>();
	}
	
	/**
	 * Stores the last query run by the user
	 * @param lastQuery	A map of values keyed by XPath representing the
	 * 					last query
	 */
	public void setLastQueryMap(Map<String, String> lastQuery) {
		lastQueryMap = lastQuery;
	}
	
	/**
	 * @return	A map of values keyed by XPaths representing the last query
	 * 			initiated by the user.
	 */
	public Map<String, String> getLastQueryMap() {
		return lastQueryMap;
	}
	
	/**
	 *  create the tuplespace worker that reads search responses and stores them in here
	 *  
	 * @param ts the tuple space to read responses from
	 */
	public void connectToTS(ITupleSpace ts){
		respManagerWorker = new SearchResponseManager(ts);
		respManagerWorker.start();
	}
	/**
	 *  slightly tormented way to find out if a response is local or not
	 *  TODO: will have to change this if I start using peerid [IP:port] instead of full http locations
	 */
	public String getLocalLocationString(){
		if (localhost.equals("not set")){
			LOG.warn("SearchResponseTree:local host not set");
		}
		return localhost;
	}
	
	public synchronized List<String> lookupXPathLocation(String comId, String resId, String xpath){
		List<String> toReturn = new ArrayList<String>();
		////// get the DOM in the map of search responses, create the XPath and evaluate the xpath against the dom.
		try {
			//get searchresponse metadata
			SearchResponse resp = responseDB.get(comId).get(resId);
			Document doc = resp.getResourceDOM();

			XPathFactory factory = XPathFactory.newInstance();

			XPath xpaththing = factory.newXPath();
			XPathExpression expr = xpaththing
			.compile(xpath); //input xpath expression in string format

			//do the lookup
			Object res = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) res;
			//for each returned node, get the string value of the node
			for (int i = 0; i < nodes.getLength(); i++) {
				Node curnode = nodes.item(i);
				String val = curnode.getTextContent();//.getNodeValue();
				if (val!= null)
					toReturn.add(val);
			}
		} catch (Exception e){
			LOG.debug("Exception on lookup:"+e);
		}
		return toReturn;
	}
	
	/**
	 * Updates search responses corresponding to a specific query identifier
	 * with any newly arrived trust metric information. The update is only 
	 * processed if a new trust metric or a new search response has arrived 
	 * since the last trust metric update.
	 * 
	 * @param queryId	Specifies which search responses should be updated
	 * @return The complete list of updated search responses (corresponding to
	 * 				  the passed query identifier)
	 */
	public synchronized List<SearchResponse> updateSearchResponses(String queryId) {
		List<SearchResponse> responseList = indexByQueryId.get(queryId);
		
		if (responseList!= null) {
			// Update the trust metric information iff a new response on new metric
			// has been received
			if(newResponse || newMetric) {
				for(SearchResponse response : responseList) {
					// Skip local resources (saves time and metric is unnecessary)
					if(!response.foundLocally()) {
						for(TrustMetric metric : trustMetrics) {
							metric.setTrustResponseMetrics(response);
						}
					}
				}
				
				newResponse = false;
				newMetric = false;
			}
		}
		
		return responseList;
	}
	
	/**
	 * Get the responses of the search identified by the queryId
	 * @param qid query identifier
	 * @return an array of SearchResponse objects returned within this qid
	 */
	public synchronized SearchResponse[] getResponsesAsArray(String qid){
		SearchResponse [] toReturn;
		
		List<SearchResponse> responses = updateSearchResponses(qid);
		
		if (responses != null) {
			SearchResponse [] returnArray = responses.toArray(new SearchResponse[responses.size()]);
			return returnArray;
			// return sortbyLocation(toReturn);
		} else { 
			return new SearchResponse[0];
		}
	}

	/*
     * @see up2p.search.SearchResponseListener#receiveSearchResponse(
     * up2p.search.SearchResponse[])
     * 
     */
	public synchronized void receiveSearchResponse(SearchResponse[] responses) {
		
		LOG.info("SearchResponseTree received " + responses.length
				+ " asynchronous responses.");

		// check for lack of results
		if (responses.length == 0)
			return;
		
		// with the two-level map [DB] storage ================

		//get the list of responses for this queryId
		String qid = responses[0].getQueryId(); //we're sure that the list isn't empty yet

		//hack to deal with dereferencing queries : we want them grouped under a single id:
		if (qid.indexOf("DEREF")!=-1) {
			qid = qid.substring(0, qid.indexOf("DEREF")+5); //to get the id before + "DEREF"
		}
		
		List<SearchResponse> thisindex;
		if (indexByQueryId.containsKey(qid))
			thisindex= indexByQueryId.get(qid);
		else {
			thisindex = new ArrayList<SearchResponse>();
			indexByQueryId.put(qid, thisindex);
		}
		////////////

		for (SearchResponse resp : responses) {
			
			String loc = null;
			if(resp.getLocations() != null) {
				loc = resp.getLocations().get(0).getLocationString();
			}

			//check for community
			if (responseDB.containsKey(resp.getCommunityId())) {

				Map<String, SearchResponse> comMap = responseDB.get(resp.getCommunityId());

				//check resource level
				if (comMap.containsKey(resp.getId())){
					// remove the old response from the map
					SearchResponse existingResponse = comMap.get(resp.getId());
					//update the resultLocation in place
					existingResponse.addLocations(resp.getLocations());
					//update the queryId list of the result [now this result is an answer to two or more queries]
					existingResponse.addQueryId(qid);
					
					if (loc != null && loc.startsWith(getLocalLocationString())) {
						existingResponse.setLocal(); //the added location is the local DB
					}
					if(!thisindex.contains(existingResponse)){//case where this response was returned also by a different query
						thisindex.add(existingResponse);
					}

				}else {
					//place in index by qid
					thisindex.add(resp);
					//place the new resource in the map
					comMap.put(resp.getId(), resp);
					if (loc != null && loc.startsWith(getLocalLocationString()))
						resp.setLocal();

				}

			} else { //community doesn't exist in the map
				Map<String, SearchResponse> comMap = new TreeMap<String,SearchResponse>();
				comMap.put(resp.getId(), resp);
				//place in index by qid
				thisindex.add(resp);
				if (loc != null && loc.startsWith(getLocalLocationString()))
					resp.setLocal(); //the added location is the local DB
				responseDB.put(resp.getCommunityId(), comMap); //place the new map in the DB
			}
		}
		
		newResponse = true;
		LOG.debug(" "+thisindex.size()+ " combined results");
	}

	/**
	 * Unset the "isLocal" property of a resource within the search results.
	 * Used when a resource is deleted from the local repository: if it was removed then future search results should no longer consider it local.
	 * @param communityId
	 * @param resourceId
	 */
	public synchronized void removeLocal(String communityId, String resourceId) {
		if (responseDB.containsKey(communityId)){
			Map<String, SearchResponse> cmap = responseDB.get(communityId);
			if (cmap.containsKey(resourceId)){
				cmap.get(resourceId).clearLocal();
			}
		}
		
	}

	/**
	 * Flags a response as existing in the local file system.
	 * @param communityId	The community Id of the downloaded resource
	 * @param resourceId	The resource Id of the downloaded resource
	 */
	public synchronized void setLocal(String communityId, String resourceId) {
		SearchResponse response = getSearchResponse(communityId, resourceId);
		if(response != null) {
			response.setLocal(true);
		}
	}
	
	/**
	 * Flags a response as having a download initiated.
	 * @param communityId	The community Id of the resource
	 * @param resourceId	The resource Id of the resource
	 */
	public synchronized void setDownloading(String communityId, String resourceId) {
		SearchResponse response = getSearchResponse(communityId, resourceId);
		if(response != null) {
			response.setDownloading(true);
		}
	}
	
	/**
	 * Attempts to retrieve a search response for a specific community and
	 * resource ID.
	 * @param communityId	The community ID of the specified resource
	 * @param resourceId	The resource ID of the specified resource
	 * @return	A SearchResponse corresponding to the requested resource, or
	 * 			null if no valid response could be found.
	 */
	public synchronized SearchResponse getSearchResponse(String communityId, String resourceId) {
		Map<String, SearchResponse> communityMap = responseDB.get(communityId);
		if(communityMap != null) {
			return communityMap.get(resourceId);
		}
		return null;
	}
}
