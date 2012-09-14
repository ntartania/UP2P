package up2p.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lights.Field;
import lights.extensions.XMLField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import polyester.Worker;

import up2p.peer.jtella.JTellaAdapter;
import up2p.search.SearchMetricListener;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.servlet.DownloadServlet;
import up2p.servlet.HttpParams;
import up2p.tspace.TSScanner;
import up2p.tspace.TupleFactory;
import up2p.tspace.TuplePair;
import up2p.tspace.UP2PWorker;
import up2p.tspace.WorkerInbox;
import up2p.util.Config;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.AttachmentListFilter;
import up2p.xml.filter.AttachmentReplacer;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.DigestFilter;
import up2p.xml.filter.FileAttachmentFilter;
import up2p.xml.filter.SerializeFilter;

/**
 * Implementation of <code>WebAdapter</code> which masks the network functionalities for UP2P.
 * Allows the local client to search and download a resource from the network,
 * and interfaces with the downloadservice servlet to provide other UP2P clients with access to the local repository,
 * for search and download. 
 * 
 *  Adapted/refactored from DefaultWebAdapter.
 * 
 * @author Alan Davoust
 * @version 1.0
 */
public class Core2Network implements WebAdapter {
    /** Logger used by this adapter. */
    public static Logger LOG;

   
    

    /** The configuration used for this adapter. */
    protected Config config;


    
    /**
     * The manager of <code>NetworkAdapter</code>s.
     */
    protected JTellaAdapter networkAdapter;


    
    /** 
     * access to the rest of up2p
     */
    private WebAdapter CoreAdapter;
    
    /**
     * Tuple-space worker
     */
    private NetworkWorker tsWorker;
    
  /**the other worker that deals with search responses.*/
    private SearchResponseCollector searchResponseWorker; 

    /**
     * Path to the root directory of the up2p client as returned by the Servlet
     * container.
     */
    private static String rootPath;
    
    /**A special tuple space worker that handles only searchResponses
     * 
     * Used for the purpose of collecting search results by "batches" rather than one at a time, the consequence is that the 
     * @author alan
     *
     */
    private class SearchResponseCollector extends  UP2PWorker {

    	//List<String> activequeryIds; // must contain all Ids of active queries from the network 

    	public SearchResponseCollector(ITupleSpace space) {

    		super(space);

    		this.addQueryTemplate(TupleFactory.createSearchReplyTemplateWithDOM());

    		//activequeryIds = new LinkedList<String>();
    	}


    	@Override
    	public void answerQueries() {

    		List<TuplePair> queries = workerInbox.getAll(); 
    		// this is a synchronized method that will wait while the inbox is empty then, when notified of any new tuples, collect everything.
    		// We will rely on a collection of tuples being dumped all at the same time in the TS rather than one at a time. 

    		List<SearchResponse> allresponses = new ArrayList<SearchResponse>();
    		for (TuplePair tp : queries){

    			ITuple query = tp.query; // tp is a pair (template, query), here we know all templates are the same because this worker only handles one.
    			//each tuple : result of search
    			String comId = ((Field) query.get(1)).toString();
    			String resId = ((Field) query.get(2)).toString();
    			String title = ((Field) query.get(3)).toString();
    			String fname = ((Field) query.get(4)).toString();
    			String location = ((Field) query.get(5)).toString();
    			String qid = ((Field) query.get(6)).toString();

    			LOG.debug("Core2Network::SearchResponseCollector:  SEARCHXPATHANSWER\tqid: " + qid + "\tcomid: " + comId
    					+"\tresid: " + resId + "\tfname: " + fname);

    			/*if(!activequeryIds.contains(qid)) //this means the original search didn't come from the network
    				continue;*/

    			// indicate the location for the file (URL)
    			LocationEntry[] locations = new LocationEntry[1];
    			locations[0] = new LocationEntry(location);

    			SearchResponse newresponse = new SearchResponse(resId , title, comId, fname, locations, false, qid);
    			if (query.length()>7){// the searchresponse has metadata !
    				LOG.debug(name+": search response has metadata");
    				Object metaobj = ((XMLField) query.get(7)).getValue();

    				// Add the metadata to the search response
    				Document metadata = (Document) metaobj;
    				newresponse.addResourceDOM(metadata);
    			}

    			allresponses.add(newresponse);
    		}
    		// Send collection of Search Responses to JTella Adapter. If not originally from the network, will be discarded then... but we've already done a first check using the list activeQueryIds.
    		networkAdapter.receiveSearchResponse(allresponses.toArray(new SearchResponse[0]));

    	}


    	@Override
    	protected List<ITuple> answerQuery(ITuple template, ITuple query)  {
    		throw new UnsupportedOperationException("This method is not implemented."); 
    	}


    	/*
    	/** notify of a query from the network * /
		public void addQid(String qid) {
			activequeryIds.add(qid);
			
		} */

    }


    private class NetworkWorker extends UP2PWorker implements SearchResponseListener, SearchMetricListener {

    	
    	public NetworkWorker(ITupleSpace ts){
    		super(ts);	
    		name= "NETWK"; //a name for this worker
    		
			//add a tuple = template for SearchXPath queries (3 arguments + extent): only interested in searches that go for the network + local.
    		//This worker issues searches with extent = UP2P_SEARCH_LOCAL
    		addQueryTemplate(TupleFactory.createSearchTemplate(HttpParams.UP2P_SEARCH_ALL));
    		
    		//template for search responses. We will ultimately only process those that are from the network.
    		//addQueryTemplate(TupleFactory.createSearchReplyTemplateWithDOM());
    		//update 2011-07-15: search results will be managed by a separate worker in order to have search results grouped.
    		
    		// Template for subnetwork updates (1 argument: Root Community Id)
    		// Does not need query ID as no reply is expected
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.UPDATESUBNETS, 1));
    		
    		// Template for push message requests (1 argument: Peer Id)
    		// Does not need query ID as no reply is expected
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.PUSHREQUEST, 1));
    		
    		// Template for local browse responses (used to send out hosted resource lists
    		// for trust metric purposes)
    		// 3 arguments:
    		// 1 - Community ID
    		// 2 - Query ID
    		// 3 - List of resource ID's ("/" separated)
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.BROWSELOCALCOMMUNITYANSWER, 3));
    		
    		// Template for when a local search returns no results
    		// 2 arguments:
    		// 1 - Community ID
    		// 2 - Query ID
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.SEARCHXPATH_NORESULT, 1));
    		
    		// Two fields for REMOVE: <comId, resId>
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.REMOVE, 2));

    		// Three fields for PUBLISH: <comId, resId, xmlNode>
    		addQueryTemplate(TupleFactory.createPublishTemplate());
    	}

    	/**
    	 *  Sends out a local search to the tuple space
    	 *  The method is asynchronous. (doesn't wait for an answer)
    	 *  update 2011-07-12: no longer uses QueryListeners
    	 * @param resourceId
    	 * @param communityId
    	 * @param updateResList True if a tuple to update the hosted resource list for the community
    	 * 						should also be generated.
    	 * @param xPath
    	 * @return
    	 */
    	public void searchWithDOM(String communityId, String xPath, //SearchResponseListener listener,
    			String qid) {
    		
    		//define a tuple for this query.
    		LOG.debug(name + " About to put search tuple: arguments "+communityId+", "+xPath+", "
    				+ qid + ", " + HttpParams.UP2P_SEARCH_LOCAL);
    		try {
    			ITuple t3query = TupleFactory.createSearchTuple(communityId, xPath, qid, 
    					HttpParams.UP2P_SEARCH_LOCAL);
    			
    			// Add the query to the list to be ignored so that this worker doesn't 
    			// try to process it
        		//ignoreSearchQuery(t3query);
        		//ITuple t7ans = TupleFactory.createSearchReplyTemplateWithDOM(qid);
        		
    			// Output the desired query
    			space.out(t3query); //asynchronousQuery(t3query, t7ans, listener); //modified: doesn't add the listener
    			//addListener(listener,t7ans);
    		} catch (TupleSpaceException e) {
    			LOG.error(name+" :searchWithDOM: "+ e.toString());
    		}
    		LOG.debug(name+" sent.");

    	}
    	
    	//implements abstract class
    	public List<ITuple> answerQuery(ITuple template_not_used, ITuple query){
    		String verb = ((Field) query.get(0)).toString();
    		
    		if (verb.equals(TupleFactory.SEARCHXPATH)){
    			// Only handle queries whose extent includes the network
    			try {
    				int extent = Integer.parseInt(((Field) query.get(4)).toString());
    				if(extent == HttpParams.UP2P_SEARCH_ALL || extent == HttpParams.UP2P_SEARCH_NETWORK) {
		    			String comId = ((Field) query.get(1)).toString();
		    			String xpath = ((Field) query.get(2)).toString();
		    			String qid = ((Field) query.get(3)).toString();
		
		    			LOG.info("Outputting a network search. Query: "+ xpath);
		    			
		    			//this search is asynchronous (void method)
		    			networkAdapter.searchNetwork(comId, xpath, qid);
    				}
    			} catch (NumberFormatException e) {
    				LOG.error(name + ": Search extent could not be determined, discarding.");
    			}
				
    		} else if (verb.equals(TupleFactory.UPDATESUBNETS)){

    			String rootCommId = ((Field) query.get(1)).toString();
    			try {
    				List<String> subnetList = synchronousLocalBrowse(rootCommId);
        			
        			Set<String> subnetTree = new TreeSet<String>();
        			subnetTree.addAll(subnetList);
        			
        			networkAdapter.updateSubnets(subnetTree);
    			} catch (CommunityNotFoundException e) {
    				LOG.error("Invalid root community ID specified in UpdateSubnets tuple.");
    			}			
    		} else if (verb.equals(TupleFactory.PUSHREQUEST)){
    			
    			String peerId = ((Field) query.get(1)).toString();
    			LOG.debug(name + ": Issuing push request to peer: " + peerId);
        		networkAdapter.issuePushMessage(peerId);
        		
    		} else if (verb.equals(TupleFactory.BROWSELOCALCOMMUNITYANSWER)){
    			
    			String communityId = ((Field) query.get(1)).toString();
    			String queryId = ((Field) query.get(2)).toString();
    			String resourceList = ((Field) query.get(3)).toString();
    			
    			if(resourceList.equals(TupleFactory.COMMUNITY_NOT_FOUND)) {
    				LOG.debug(name + ": Got empty response to local community browse.");
    			} else {
    				networkAdapter.updateResourceList(queryId, communityId, Arrays.asList(resourceList.split("/")));
    			}
    		} else if (verb.equals(TupleFactory.SEARCHXPATH_NORESULT)) {
    			String qid = ((Field) query.get(1)).toString();
    			// networkAdapter.ignoreResourceList(qid);
    			
    		} else if (verb.equals(TupleFactory.REMOVE)
    				|| verb.equals(TupleFactory.PUBLISH))
    		{
    			String comId = ((Field) query.get(1)).toString();
    			networkAdapter.invalidateCachedResourceList(comId);
    		}

    		
    		return new ArrayList<ITuple>();
    	}

		//@Override
		//This is to receive responses from the network. 
		public void receiveSearchResponse(SearchResponse[] responses) {
			
			List<ITuple> answers = new ArrayList<ITuple>();
			
			LOG.debug(name+"got "+ responses.length+" search responses from the network");
			for(SearchResponse resp : responses){
				LOG.debug("A response:"+resp.toString());
				String rid = resp.getId();
				String comId= resp.getCommunityId();
				List<LocationEntry> locs = resp.getLocations();
				if (locs.size()>1)
					LOG.warn("ReceiveSearchResponse: Warning: there's more than one location returned in this searchresponse from the network");
				String location = locs.get(0).getLocationString(); 
				
				String title = resp.getTitle();
				String qid = resp.getQueryId();
				String fname = resp.getFileName();
				
				ITuple atuple = TupleFactory.createSearchReply(comId, rid,  title,fname, location, qid);
				
				// if we have metadata, then we output a tuple response with the DOM (avoid the one without, see SearchReponseTree)
				if(resp.getResourceDOM() !=null){ //metadata attached
					ITuple btuple = TupleFactory.createSearchReplyWithDOM(comId, rid, title, fname, location, qid, resp.getResourceDOM());
					answers.add(btuple);
				} else
				{
				answers.add(atuple); // otherwise use the one without metadata 
				}
				
			}
			LOG.debug(name+" outputting "+ answers.size()+" tuples search responses");
			try {
				for (ITuple t: answers){
					space.out(t);	
				}
				
			} catch (Exception e) {
				LOG.error("Yikes!! an error! "+ name+e);
			}
			LOG.debug(name+" done.");
		}

		/*
	     * @see up2p.search.SearchMetricListener#receiveMetricValue
	     */
		@Override
		public void receiveMetricValue(String peerIdentifier,
				String communityId, String metricName, String metricValue) {
			
			ITuple metricTuple = TupleFactory.createTuple(TupleFactory.TRUSTMETRIC,
					new String[] {peerIdentifier, communityId, metricName, metricValue});
			
			try {
				space.out(metricTuple);
			} catch (TupleSpaceException e) {
				LOG.error(name + ": Could not add trust metric value to tuple space.");
				e.printStackTrace();
			}
			
		}

		/*
	     * @see up2p.search.SearchMetricListener#receiveNetworkResourceList
	     */
		@Override
		public void receiveNetworkResourceList(String peerIdentifier,
				String communityId, List<String> netResIds) {
			
			if(netResIds == null || communityId == null) {
				return;
			}
			
			String netIdsString = "";
    		for(String rId : netResIds) {
    			netIdsString += "/" + rId;
    		}
    		netIdsString =  netIdsString.substring(1);
    		
    		LOG.debug(name + ": Got Resource List:\t" + peerIdentifier + "\t" + communityId + "\t" + netIdsString);
    		
    		LOG.debug(name + ": Adding resource list for: " + peerIdentifier + " - " + communityId);
    		ITuple resListTuple = TupleFactory.createTuple(TupleFactory.NETRESOURCELIST,
    				new String[] {peerIdentifier, communityId, netIdsString});
    		
    		try {
    			space.out(resListTuple);
    		} catch (TupleSpaceException e) {
    			LOG.error(name + ": Could not add network resource list to tuple space.");
    		}
		}

		/**
		 * Retrieves a list of absolute file paths for the given resource. The first path.
		 * of the list is the resource file, and any further paths are attachments.
		 * @param communityId	The community Id of the resource to be fetched
		 * @param resourceId	The resource Id of the resource to be fetched
		 * @return	A list of file paths for the given resource, where the first is the
		 * 			resource file and all following paths are attachments
		 */
		public List<String> lookupFilepaths(String communityId, String resourceId) {
			String qid = System.currentTimeMillis() + "lfp"; 
			
			ITuple getQuery = TupleFactory.createTuple(new String[] {TupleFactory.GETRESOURCEFILEPATHS, communityId, resourceId, qid});
			
			ITuple resultTemplate = TupleFactory.createTuple(new String[] {TupleFactory.GETRESOURCEFILEPATHSRES});
			resultTemplate.add(new Field().setType(String.class)); // Resource file path field
			resultTemplate.add(new Field().setType(String.class)); // Attachments file path field
			resultTemplate.add(new Field().setValue(qid)); // Query ID field
			
			ITuple result;
			
			try {
				LOG.debug(name + ": Performing file path lookup for: " + communityId + "/" + resourceId);

				space.out(getQuery);
				result = space.rd(resultTemplate);
				
				String resFilePath = ((Field) result.get(1)).toString();
				String attachmentPaths = ((Field) result.get(2)).toString();
				
				if (resFilePath.equals(TupleFactory.RESOURCE_NOT_FOUND)) {
					return null;
				}
				
				List<String> returnList = new ArrayList<String>();
				returnList.add(resFilePath);
				
				LOG.debug("Got attachment string: " + attachmentPaths);
				for(String attachPath : attachmentPaths.split("\\.\\.\\.")) {
					if(!attachPath.equals("")) {
						returnList.add(attachPath);
					}
				}
				
				return returnList;
				
			
			} catch (Exception e) {
				notifyErrorToUser(e);
				return null;	
			}
		}
		
		/**
		 * @see UP2PWorker.shutdownCleanup()
		 */
		public void shutdownCleanup() {
			Core2Network.this.shutdown();
		}
		
		/**
		 * Output a local browse for the purpose of updating the hosted resource list
		 * passed with searchResults.
		 * @param comId	The community to browse
		 * @param queryId	The queryID to use for the browse request
		 */
		public void fetchHostedResourceList(String comId, String queryId) {
			// Output a local browse on the queried community to determine the
			// hosted resource list for trust metrics
			String[] fields = new String[] {comId, queryId};
			ITuple localBrowse = TupleFactory.createTuple(TupleFactory.BROWSELOCALCOMMUNITY, fields);
			
			try {
				space.out(localBrowse);
			} catch (TupleSpaceException e) {
				LOG.error(name + ": Could not fetch hosted resource list.");
				e.printStackTrace();
			}
		}

		/** output a tuple notifying of the opening or closing of a connection */
		public void notifyConnection(String remoteservent, int port,
				String connectionType, boolean opening) {
			
			String[] fields = new String[] {remoteservent, String.valueOf(port), connectionType, String.valueOf(opening)};
			ITuple notif = TupleFactory.createTuple(TupleFactory.NOTIFYCONNECTION, fields);
			
			try {
				space.out(notif);
			} catch (TupleSpaceException e) {
				LOG.error(name + "error:"+e);
				e.printStackTrace();
			}
			
		}
		
		/**
		 * Notifies the tuple space that a relay message has been received.
		 * @param servingPeerId	The peer ID (hostname:port/urlPrefix) of the peer serving files
		 * @param relayUrl	The URL of the relay to use for downloads
		 * @param relayIdentifier	The peer identifier of the relay pair
		 */
		public void notifyRelayReceived(String servingPeerId, String relayUrl, int relayIdentifier) {
			String[] fields = new String[] {servingPeerId, relayUrl, String.valueOf(relayIdentifier)};
			ITuple relayNotif = TupleFactory.createTuple(TupleFactory.RELAY_RECEIVED, fields);
			try {
				space.out(relayNotif);
			} catch (TupleSpaceException e) {
				LOG.error(name + " Error outputing tuple: " + e);
				e.printStackTrace();
			}
		}
    }

    
    /**
     * Constructs an adapter. UPDATED jan 03, 2008 : removed non-network stuff
     * 
     * @param up2pPath path to the root directory of the client deployment in
     * the Servlet container (e.g. ../webapps/up2p)
     * @param adapter the core WebAdapter that handles the interface towards the repository
     * @param conf the Config object containing some configuration properties for the whole system.
     */
    public Core2Network(String up2pPath, WebAdapter adapter, Config conf) {
    	this.config = conf;
        //batchAdapters = Collections.synchronizedSet(new HashSet<NetworkAdapter>());
        //publishedResources = new HashMap<String,ResourceList>();
        CoreAdapter = adapter; //added because the network needs to access the rest of up2p
        
        // set the directory path for up2p (from DefWA)
        rootPath = up2pPath;
        
        // load the logger
        LOG = Logger.getLogger(WebAdapter.LOGGER);
        
        // create the JTella Network Adapter
        try{
        	networkAdapter =  new JTellaAdapter(config, getUrlPrefix(), this);
        		/*new JTellaAdapter(config.getProperty("up2p.gnutella.incoming", "6346"), 
        			Boolean.parseBoolean(config.getProperty("up2p.gnutella.peerdiscovery", "false")),
        			config.getProperty("networkAdapter.relayPeer", ""), getUrlPrefix(), this);*/
        }
        catch(ExceptionInInitializerError err){
        	err.getCause().printStackTrace();
        }
        networkAdapter.setWebAdapter(this);
        /*networkAdapterManager.setNetworkAdapterDirectory(new File(
                getRealFile(config.getProperty("community.root", "community")
                        + File.separator
                        + config.getProperty("networkAdapter.home",
                                "NetworkAdapter"))));
         */

        // log initial message
        LOG.info(new java.util.Date().toString()
                + "Core2Network Adapter initialized.");
        
        
    }

    /**
     * initialize the local tsworker
     */
    public void initializeTS(ITupleSpace ts) {
    	
    	//tspace = ts; I don't actually need to keep a ref to the tuplespace... just to the worker.
    	tsWorker = new NetworkWorker(ts);
    	tsWorker.start();
    	LOG.debug("Core2Network : tuple space worker started.");
    	
    	// Register the TSWorker to receive network search responses and trust metric data
    	networkAdapter.addSearchResponseListener(tsWorker);
    	networkAdapter.addSearchMetricListener(tsWorker);
    	
       searchResponseWorker = new SearchResponseCollector(ts);
       searchResponseWorker.start();
    }
    
    /**
     * for testing and viewing by jsp
     * @return
     */
    public ITupleSpace getTS(){
    	return tsWorker.getTS();
    }

   /** Perform local search on repository via core webadapter.
     * 
     * @param communityId
     * @param query
     * @return search results as returned by the core webadapter.
     */
    public void searchLocal(String communityId, SearchQuery query, String qid) {
    	//use the one below to get responses with a DOM field

    	tsWorker.searchWithDOM(communityId, query.getQuery(), qid);
    	//searchResponseWorker.addQid(qid); for now not dealing with qids at this level.. otherwise I won't know when to purge these    	
    	
    }
    
    /**
	 * Initiates a asynchronous browse of the specified community.
	 * @param comId	The community to browse
	 * @param queryId	The queryID to use for the browse request
	 */
	public void fetchHostedResourceList(String comId, String queryId) {
		tsWorker.fetchHostedResourceList(comId, queryId);
	}
    
    /**
     * Trims the result set to the maximum desired results, as stored in the
     * search query itself.
     * 
     * @param results original result set
     * @param query search query
     * @return cropped result set
     */
    private SearchResponse[] cropResults(SearchResponse[] results,
            SearchQuery query) {
        // cut down to max results
        if (results != null && results.length > query.getMaxResults()) {
            SearchResponse[] shortenedResults = new SearchResponse[query
                    .getMaxResults()];
            System.arraycopy(results, 0, shortenedResults, 0,
                    shortenedResults.length);
            return shortenedResults;
        }
        return results;
    }


   

   
    /*
     * @see up2p.core.WebAdapter#getHost()
     */
    public String getHost() { //host is set for the "core" network adapter and the repository
        return CoreAdapter.getHost();
    }

    /*
     * @see up2p.core.WebAdapter#getPort()
     */
    public int getPort() {
        return CoreAdapter.getPort();
    }
    
    /**
     * @see up2p.core.WebAdapter#getUrlPrefix()
     */
    public String getUrlPrefix() {
    	return CoreAdapter.getUrlPrefix();
    }
    
    /**
     * KLUDGE: Made this static to avoid major modifications throughout
     * the WebAdapters, but logically it really shouldn't be
     * 
     * @return	The absolute path for the root directory 
     * 			for this instance of U-P2P.
     */
    public static String getRootPath() {
    	return rootPath;
    }

    /**
     * Translates from a relative path to a real path using the root directory
     * of the up2p application as a base.
     * 
     * @param filePath path of a file relative to the webserver context
     * @return the full path to the file
     */
    private String getRealFile(String filePath) {
        return rootPath + File.separator + filePath;
    }

    /**
     * 
     * Retrieves a file from the network.
     * @param response A searchResponse object referencing the resource to be downloaded
     * @return the URL of the resource to be redirected to the upload servlet. ("/upload? "... "/view.jsp..." )
     * 
     */
    
    /*
     * @see up2p.core.WebAdapter#shutdown()
     */
    public void shutdown() {
        LOG.info(new java.util.Date().toString()
                + " Shutting down the Network adapter  of U-P2P.");
        networkAdapter.shutdown();
        
        //TODO : manage host cache and shutdown network listener??
        
    }

    
	
	/**
	 * Retrieves a list of absolute file paths for the given resource. The first path.
	 * of the list is the resource file, and any further paths are attachments.
	 * @param communityId	The community Id of the resource to be fetched
	 * @param resourceId	The resource Id of the resource to be fetched
	 * @return	A list of file paths for the given resource, where the first is the
	 * 			resource file and all following paths are attachments
	 * @throws ResourceNotFoundException 
	 */
	public List<String> lookupFilepaths(String communityId, String resourceId) 
		throws ResourceNotFoundException {
		List<String> returnList = tsWorker.lookupFilepaths(communityId, resourceId);
		if(returnList == null) {
			throw new ResourceNotFoundException(communityId + "/" + resourceId);
		}
		return returnList;
	}

	/**
	 * Notifies (via tuple space) of changes in the connections (connections opening or closing)
	 * @param iP remote host
	 * @param port remote port
	 * @param connectionType INCOMING or OUTGOING
	 * @param opening true if the connection is opening, false if it's closing.
	 */
	public void notifyConnection(String remoteservent, int port, String connectionType,
			boolean opening) {
		while (tsWorker == null){ // this should handle the delay between initialization to finish and the notifications getting in from the network.
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				return;
				//e.printStackTrace();
			} // wait until the TS gets initialized.
		}
		tsWorker.notifyConnection(remoteservent, port, connectionType, opening);
	}
	
	/**
	 * Notifies the tuple space that a relay message has been received.
	 * @param servingPeerId	The peer ID (hostname:port/urlPrefix) of the peer serving files
	 * @param relayUrl	The URL of the relay to use for downloads
	 * @param relayIdentifier	The peer identifier of the relay pair
	 */
	public void notifyRelayReceived(String servingPeerId, String relayUrl, int relayIdentifier) {
		tsWorker.notifyRelayReceived(servingPeerId, relayUrl, relayIdentifier);
	}
}