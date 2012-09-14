package up2p.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lights.Field;
import lights.extensions.FastTupleSpace;
import lights.extensions.XMLField;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


import antlr.collections.impl.BitSet;

import stracciatella.Utilities;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.servlet.DownloadService;
import up2p.servlet.DownloadServlet;
import up2p.servlet.HttpParams;
import up2p.tspace.BasicWorker;
import up2p.tspace.CleaningWorker;
import up2p.tspace.MonitorWorker;
import up2p.tspace.TrustWorker;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PFunctionWorker;
import up2p.tspace.UP2PWorker;
import up2p.util.Config;
import up2p.util.FileUtil;
import up2p.util.NetUtil;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.AttachmentReplacer;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.DigestFilter;
import up2p.xml.filter.FileAttachmentFilter;
import up2p.xml.filter.SerializeFilter;
import up2p.xml.filter.ValidationFilter;
import console.QueryLogWorker;

/**
 * Default implementation of a <code>WebAdapter</code> that is used by the
 * servlets and JSPs to access the components of U-P2P.
 * 
 * @author Neal Arthorne
 * @author Alan Davoust 
 * @version 1.0
 */
public class DefaultWebAdapter implements WebAdapter {
    
	//TODO: make this configuration
	private static final boolean VALIDATION_ON = true;
	
	/**
     * Name of the Log4J logger used for the WebAdapter.
     */
    public static final String LOGGER = "up2p.webAdapter";


	/** webadapter hiding the network stuff*/
	private Core2Network toNetwork;
	
	/** webadapter hiding the repository*/
	private Core2Repository toRepository;
	
	private DownloadManager downloadMgr;
	
	/** Logger used by this adapter. */
    public static Logger LOG;

    /**
     * Lists NetworkAdapters that have been used for batch publishing and are
     * yet to be flushed.
     */
    //private Set<NetworkAdapter> batchAdapters;

    /** The configuration used for this adapter. */
    protected Config config;

    /**
     * Configuration file name. Config file is found in same directory as this
     * class.
     */
    public static String WEBADAPTER_CONFIG = "WebAdapter.properties";

    /** Stores the local IP. */
    private String localHost;

    /** Stores the local port used by U-P2P. */
    private int localPort;

    /**
     * To store the responses of the latest search here rather than in the "Session" 
     */
    SearchResponseTree latestSearchResponses;
    
    /** 
     * A list of notifications read from the tuple space which should be displayed 
     * to the user 
     */
    private List<UserNotification> notifications;
    
    
    
    /**
     * Path to the root directory of the up2p client as returned by the Servlet
     * container.
     * 
     * KLUDGE: Made this static so the static methods used throughout the other
     * adapters can access the root path without major modifications, but 
     * logically it really shouldn't be
     */
    private static String rootPath;
    
    /**
     * Identifier that should follow the hostname to access this instance of U-P2P.
     * This should usually be the same as the directory name in which up2p is deployed.
     */
    private String urlPrefix;

    
    /**
     * Manages and provides database access to resources, communities and bridges.
     * 
     * @author Neal Arthorne
     * @version 1.0
     */
    private class ResourceManager {

        

        /** Id of the Bridge Community. */
       // private String bridgeCommunityId;

        /** Id of the Network Adapter community. */
        //private String networkAdapterCommunityId;

        /** Id of the Root Community. */
        private String rootCommunityId;

        /**
         * Creates a Community Manager.
         * 
         * @param databaseAdapter database adapter used by this manager
         * @param webAdapter used to get host and port name of the peer
         */
        public ResourceManager() {
            
        	rootCommunityId = getConfigurationValue("up2p.root.id");
        	//bridgeCommunityId = getConfigurationValue("up2p.BridgeCommunity.id");
        	//networkAdapterCommunityId = getConfigurationValue("up2p.networkAdapter.id");
        }
        
       /* public String getBridgeCommunityId() {
			
			return bridgeCommunityId;
		}* /

		public String getResourceTitle(String resourceId, String communityId) {
			String titleLoc = getTitleLocation(communityId);
		       if (titleLoc == null){
		       	LOG.error("getResourceTitle " + resourceId + " : "+ communityId);
		           return "Error getting title location";
		       }    
		       try {
				return getXPathLocation(resourceId, communityId, titleLoc);
			} catch (CommunityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} */

		private String getTitleLocation(String communityId) {
			String resultStr=null;
			try {
				resultStr = getXPathLocation(communityId, getRootCommunityId(),
				   "/community/titleLocation");
			} catch (CommunityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       // by default we guess the title to be in the first element
       if (resultStr == null)
           resultStr = "//@title";
       return resultStr;
		}

		/*public String getCommunityName(String id) { //todo: maybe remove this method ?
			try {
				return getXPathLocation(id, rootCommunityId, "/community/name");
			} catch (CommunityNotFoundException e) {
				// TODO Auto-generated catch block
				LOG.error("GetCommunityName: community not found" + id);
			} catch (ResourceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}*/

		/*public String getSchemaLocation(String communityId) {
			return getXPathLocation(communityId, getRootCommunityId(),
            "/community/schemaLocation");
		}*/

		public String getRootCommunityId(){
        	return rootCommunityId;
        }
    
    }
    
    /**
     * Xpath cache for DB queries
     */
    private XPathCache xCache;
    
    private ResourceManager resourceManager;
    
    //============================================
    // TUPLE-SPACE architecture 
    
    /**
     * 
     * the tuple space to communicate with the repository and network
     */
    private ITupleSpace tspace;
    /**
     * the tuple space worker for the User-side WA
     */
    private TSWorker tsWorker;

    /**
     * the one who deals with "function" queries
     */
	private UP2PFunctionWorker functionworker;

	/**
	 * worker that removes old tuples from the TS
	 */
	private CleaningWorker cleaningworker;
	private QueryLogWorker Myquerylogger;
	
	/** Worker to calculate trust metrics */
	private TrustWorker trustWorker;
	
	//agent to resolveURIs
	//warning : currently not used
	private BasicWorker resolveURIWorker;
	
	/**
	 * worker that logs activity to a remote monitor via UDP 
	 * To disable, remove lines indicating IP and port of monitor in config file webadapter.properties.
	 */
	private MonitorWorker mw;
    
	/**
     * Caches XPath values from the database.
     * 
     * @author Neal Arthorne
     * @version 1.0
     */
    private class XPathCache {
        /* Three level cache. */
        private Map<String,Map<String,Map<String, String>>> communityCache;
        
  

        public XPathCache() {
            communityCache = new HashMap<String,Map<String,Map<String, String>>>();
        }

        /**
         * Checks the three level cache (community > resource > XPath) and
         * returns a result if found or queries the database if not found.
         * 
         * @param resourceId id of the resource to query
         * @param communityId id of the community to query
         * @param xPath XPath location
         * @return query result or <code>null</code> if not found
         */
        public String getXPathLocation(String resourceId, String communityId,
                String xPath) throws CommunityNotFoundException, ResourceNotFoundException{
            synchronized (communityCache) {
                // get cache for the community
            	Map<String, Map<String,String>> omap = communityCache.get(communityId);
                Map<String, Map<String,String>> resourceCache = null;
                if (omap == null) {
                    // no cache found so create one
                    resourceCache = new HashMap<String, Map<String,String>>();
                    communityCache.put(communityId, resourceCache);
                } else
                    resourceCache =  omap;

                // get cache for the resource
                Map<String,String> pmap = resourceCache.get(resourceId);
                Map<String,String> xPathCache = null;
                if (pmap == null) {
                    // resource not found
                    xPathCache = new HashMap<String,String>();
                    resourceCache.put(resourceId, xPathCache);
                } else
                    xPathCache =  pmap;

                // lookup XPath
                //LOG.debug("before getting xpath");
                Object result = xPathCache.get(xPath);
                //LOG.debug("after getting xpath");
                if (result == null && xPathCache.containsKey(xPath))
                    return null; // null value has been stored
                else if (result == null) {
                    // XPath not found
                	LOG.debug("XPathCache: entry "+resourceId+ " " +communityId + " " + xPath + " not found.");  
                    String xPathResult = tsWorker.lookupXPathLocation(resourceId,
                            communityId, xPath);
                    LOG.debug("XPathCache: got result " + xPathResult + " from TS");
                    xPathCache.put(xPath, xPathResult);
                    return xPathResult;
                } else
                    return (String) result;
            }
        }

        /**
         * Clears the cache contents.
         *  
         */
        public void reset() {
            communityCache.clear();
        }
        
        /**
         *  removes just the relevant entries in the cache rather than everything
         * @param cid
         * @param rid
         */
        public void resetSelectively(String cid, String rid){
        	Map<String, Map<String,String>> cmap = communityCache.get(cid);
        	if (cmap != null) {
        		cmap.remove(rid);
        		LOG.debug("XPathCache: removing cache for c/r:" + cid +" / "+rid);
        	}
        	if (cid == getRootCommunityId()){
        		communityCache.remove(rid);
        		LOG.debug("XPathCache: removing community cache for commnuity:" +rid);
        	}
        }

		
        
    }
    
    /**
     * The superclass contains the listener mechanism to return answers to asynchronous queries.
     * For synchronous queries there are a few convenience methods.
     * the work() method is in the superclass
     *  
     * @author Alan
     *
     */
    private class TSWorker extends UP2PWorker {

    	
    	public TSWorker(ITupleSpace ts){
    		super(ts);	
    		name= "DEFWA"; // A name for this worker
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.NOTIFY_ERROR, 1));
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.NOTIFY_UI, 1));
    		
    		// Listen for relay messages to initiate file downloads
    		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.RELAY_RECEIVED, 3));
    	}

    	// Implements abstract class
    	public List<ITuple> answerQuery(ITuple template, ITuple t){
			String verb = ((Field) t.get(0)).toString();
			
			if (verb.equals(TupleFactory.NOTIFY_ERROR)) {
				addNotification("Error: " + ((Field) t.get(1)).toString());
			} else if (verb.equals(TupleFactory.NOTIFY_UI)) {
				addNotification(((Field) t.get(1)).toString());
			} else if (verb.equals(TupleFactory.RELAY_RECEIVED)) {
				final String peerId = ((Field) t.get(1)).toString();
				final String relayUrl =((Field) t.get(2)).toString();
				final int relayIdentifier;
				try {
					relayIdentifier = Integer.parseInt(((Field) t.get(3)).toString());
				} catch (NumberFormatException e) {
					LOG.error(name + ": Invalid relay identifier specified in relay message: " + ((Field) t.get(3)).toString());
					return null;
				}
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						// Note: File transfers are handled in a separate thread so that UI notifications can still
						// be displayed during the download process.
						
						// Launch all pending downloads for the specified peer ID
						String pendingTransfer = null;
						while((pendingTransfer = downloadMgr.getFailedTransfer(peerId)) != null) {
							String[] splitTransfer = pendingTransfer.split("/");
							
							// Just use the resource ID as the filename for now
							// TODO: Might want to start new threads to service these
							try {
								LOG.debug(name + ": Launching proxy request for: " + splitTransfer[0] + "/" + splitTransfer[1]);
								File resFile = retrieveFromNetworkThroughProxy(splitTransfer[0], splitTransfer[1], splitTransfer[1] + ".xml", 
										peerId, relayUrl, relayIdentifier);
							} catch (NetworkAdapterException e) {
								LOG.error(name + ": NetworkAdapterException fetching resource through relay.");
								e.printStackTrace();
								continue;
							}
						}
					}
				}).start();
			}
			
			// No result tuples need to be returned
    		return null;
    	}
    	
    	/**
    	 *  To query a xpath lookup with multiple answers
    	 * @param resourceId
    	 * @param communityId
    	 * @param pathLocation
    	 * @return
    	 * @throws CommunityNotFoundException 
    	 * @throws ResourceNotFoundException 
    	 */
    	public List<String> getXPathLocationAsList(String resourceId,
    			String communityId, String pathLocation) throws CommunityNotFoundException, ResourceNotFoundException {
    		//define a tuple for this query.
    		LOG.debug(name + ": About to query tuple space for LookupXPath: arguments "+resourceId+", "+communityId+", "+ pathLocation);

    		// Add a random identifier
    		Random rand = new Random();
    		String qId = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
    		
    		String[] fields = new String[] {resourceId, communityId, pathLocation, qId};

    		ITuple t4query = TupleFactory.createTuple(TupleFactory.LOOKUPXPATH, fields); 

    		ITuple t2ans =TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, fields);
    		t2ans.add(new Field().setType(String.class)); // template is identical + a template field 

    		//query tuple space using synchronous method with multiple answers

    		List<String> toReturn = new ArrayList<String>();
    		try {
    			List<ITuple> answers = synchronousMultiQuery(t4query, t2ans);

    			for (ITuple ans : answers){
    				IField answerField = ans.get(5);
    				String stringanswer;
    				if (answerField instanceof Field){ //should always be the case but let's be defensive
    					stringanswer = ((Field)answerField).getValue().toString();
    					//LOG.debug("Got answer for LookupXPath: "+ answer);
    					toReturn.add(stringanswer);

    					if (stringanswer.equals(TupleFactory.RESOURCE_NOT_FOUND))
    						throw new ResourceNotFoundException();
    					if (stringanswer.equals(TupleFactory.COMMUNITY_NOT_FOUND))
    						throw new CommunityNotFoundException(); 					 

    				}
    			}

    		} catch (TupleSpaceException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		return toReturn;

    	}
    	
    	/**
    	 *  Performs a lookupXPathLocation type query via the tupleSpace.
    	 *  The method is synchronous. (waits for an answer)
    	 *  
    	 * @param resourceId
    	 * @param communityId
    	 * @param xPath
    	 * @throws CommunityNotFoundException if the community is not locally stored
    	 * @throws ResourceNotFoundException if the resource is not locally stored in the community (the community is then found)
    	 * @return
    	 */
    	public String lookupXPathLocation(String resourceId, String communityId, String xPath) throws ResourceNotFoundException, CommunityNotFoundException{
    		//define a tuple for this query.
    		LOG.debug(name + "About to query tuple space for LookupXPath: arguments "+resourceId+", "+communityId+", "+ xPath);
    		
    		// Add a random identifier
    		Random rand = new Random();
    		String qId = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
    		
    		String[] fields = new String[] {resourceId, communityId, xPath, qId};

    		ITuple t4query = TupleFactory.createTuple(TupleFactory.LOOKUPXPATH, fields);

    		ITuple t4ans =TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, fields);
    		t4ans.add(new Field().setType(String.class)); // template for response is identical + a template field
    		

    		String answer = "";

    		IField answerField;
    		try {
    			//answerField = synchronousQuery(t4query, t4ans).get(4); //the answer should be in field number 4 (fifth since it starts at 0)

    			space.out(t4query);
    			answerField = space.rd(t4ans).get(5);
    			
    			if (answerField instanceof Field){ //should always be the case but let's be defensive
    				answer = ((Field)answerField).getValue().toString();
    				LOG.debug(name+ ": Got answer for LookupXPath: "+ answer);
    				
    				if (answer.equals(TupleFactory.RESOURCE_NOT_FOUND)) {
    					throw new ResourceNotFoundException();
    				}
    				if (answer.equals(TupleFactory.COMMUNITY_NOT_FOUND))
    					throw new CommunityNotFoundException(); 					 

    			}
    			//else 
    				//LOG.debug("NO answer for LookupXPath!!!");

    		} catch (TupleSpaceException e) {
    			LOG.error("lookupXpath: TupleSpace Exception!!");
    		}  

    		return answer;
    	}

    	/** Output a message to remove a resource
    	 * 
    	 * @param communityId community where to find resource
    	 * @param resourceId id of the resource
    	 */
    	public void remove(String communityId, String resourceId){
    		String[] fields = new String[] {communityId, resourceId};
    		ITuple t2query = TupleFactory.createTuple(TupleFactory.REMOVE, fields);
    		
    		try {
				space.out(t2query);
			
				//remove all references to this resource from TS:
				ITuple remover = TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCHRESPONSE, communityId);
				remover.add(new Field().setType(String.class));
				remover.add(new Field().setValue(resourceId));
				space.ing(remover);

    		} catch (TupleSpaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	/**
    	 *  Publish an XML document to the repository
    	 *  
    	 * @param documentElement the XML node
    	 * @param resid the resource Id
    	 * @param communityId the comunity where to publish
    	 */
		public void publish(Document xmldocument, String resid,
				String communityId) {
			List<String> flist = new ArrayList<String>();
			flist.add(TupleFactory.PUBLISH);
			flist.add(communityId);
			flist.add(resid);
			ITuple newtuple = TupleFactory.createTuple(flist);
			newtuple.add(new XMLField(xmldocument)); //add the xml doc as an XMLField
			LOG.debug(name + " about to output publish tuple: "+ newtuple.toString());
			try {
				space.out(newtuple);
			} catch (TupleSpaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		/**
		 * map a resource to a file (just using the filename now)
		 * @param communityId
		 * @param resourceId
		 * @param absolutePath filename (warning : no longer absolute path!!)
		 */
		public void addResource(String communityId, String resourceId,
				String absolutePath, Map<String, File> attachments) {
			String[] fields = null;

			// Check if attachments were provided
			if(attachments != null && attachments.size() > 0) {
				
				// If so generate an attachment string in the form "<name1>...<path1>...<name2>...<path2>" ... etc
				String attachString = "";
				Set<String> attachmentLinks = attachments.keySet();
				for(String attachmentLink : attachmentLinks) {
					// "..." is used as a separator since consecutive periods are illegal in valid URLs
					attachString += attachmentLink + "..." + attachments.get(attachmentLink).getAbsolutePath() + "...";
				}
				
				LOG.info(name + ": addResource: " + communityId + "/" + resourceId
		                + " generated attachment string: '"
		                + attachString);
				
				fields = new String[] {communityId, resourceId, absolutePath, attachString};
				
			} else {
				
				LOG.info(name + ": addResource: " + communityId + "/" + resourceId
		                + ": No attachments.'");
				
				fields = new String[] {communityId, resourceId, absolutePath};
			}
			
			ITuple mapFileQuery = TupleFactory.createTuple(TupleFactory.FILEMAP, fields);

			try {
				space.out(mapFileQuery);
			} catch (TupleSpaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public Node getLocalDOM(String communityId, String resourceId) {
			//generate an id
			String qid = System.currentTimeMillis() + ""; 
			ITuple getQuery = TupleFactory.createTuple(new String[] {TupleFactory.GETLOCAL, communityId, resourceId, qid});
			ITuple resultTemplate = TupleFactory.createTuple(new String[] {TupleFactory.GETLOCALRESPONSE, communityId, resourceId});
			resultTemplate.add(TupleFactory.createXMLTemplateField());
			
			ITuple result;
			
			try {
				LOG.debug(name+" about to query TS for GetLocalDOM ");
				//result = synchronousQuery(getQuery, resultTemplate);
				space.out(getQuery);
				
				result = space.rd(resultTemplate);
				
				LOG.debug(name+" got result for GetLocalDOM :"+ result);
				XMLField xfield = (XMLField)result.get(3);// number 3 is the one with the XML
				
				Document dox =	(Document)xfield.getValue();
				int retries = 0;
				while (dox.getDocumentElement() == null && retries<4){ //case where it was not found
					Thread.sleep(500);
					qid = System.currentTimeMillis() + "";
					((Field)getQuery.get(3)).setValue(qid);
					space.out(getQuery);
					
					result = space.rd(resultTemplate);	
					
					retries++;
				}
			
				return dox;
				
			
			} catch (Exception e) {
				notifyErrorToUser(e);
				return null;	
			}
			//expected result: [verb, comId, resId, XMLField with doc]
			
		}
		
		 /**
		 * Construct a DOM tree representing the community contents.
		 * 
		 * @param communityId	The community to construct a DOM for
		 * @param titleXPath	The XPath to the title of the resource for the particular community
		 * @param populate If true, also return the DOM for each resource in the community
		 * @return a DOM containing a root node called "community" with its "id" and "title" as attributes,
		 * and a child for each resource, called "resource", with its "id" and "title" as attributes. If requested each
		 * resource element will have a single child which is the root element of the resource DOM. 
		 */
		public Node getLocalCommunityDOM(String communityId, String titleXPath, boolean populate) {
			//generate an id
			String qid = System.currentTimeMillis() + ""; 
			ITuple getQuery = TupleFactory.createTuple(new String[] {TupleFactory.GETLOCALCOMM, communityId, titleXPath, Boolean.toString(populate), qid});
			ITuple resultTemplate = TupleFactory.createTuple(new String[] {TupleFactory.GETLOCALCOMMRESPONSE, communityId, titleXPath, Boolean.toString(populate), qid});
			resultTemplate.add(TupleFactory.createXMLTemplateField());
			
			ITuple result;
			
			try {
				LOG.debug(name+" about to query TS for GetLocalCommunityDOM ");

				space.out(getQuery);
				result = space.rd(resultTemplate);
				
				LOG.debug(name+" got result for GetLocalCommunityDOM :"+ result);
				XMLField xfield = (XMLField)result.get(5); // number 5 is the one with the XML
				
				Document dox =	(Document)xfield.getValue();
				int retries = 0;
				
				while (dox.getDocumentElement() == null && retries<4){ //case where it was not found
					System.out.println("getLocalCommunityDOM RETRY");
					Thread.sleep(500);
					qid = System.currentTimeMillis() + "";
					((Field)getQuery.get(3)).setValue(qid);
					space.out(getQuery);
					
					result = space.rd(resultTemplate);	
					dox =	(Document)(((XMLField)result.get(5)).getValue());
					
					retries++;
				}
			
				return dox;
				
			
			} catch (Exception e) {
				notifyErrorToUser(e);
				return null;	
			}
		}	
		
		public void issuePushRequest(String peerId) {
			ITuple pushRequest = TupleFactory.createTuple(
					new String[] {TupleFactory.PUSHREQUEST, peerId});
			try {
				space.out(pushRequest);
				LOG.debug(name + ": Requested push message be sent to peer: " + peerId);
			} catch (TupleSpaceException e) {
				LOG.error(name + ": Error outputting push request tuple.");
				e.printStackTrace();
			}
		}
		
		/**
		 * Outputs a "Shutdown" tuple into the tuple space, and terminates
		 * this tuple space worker
		 * @throws TupleSpaceException 
		 */
		public void initiateShutdown() {
			LOG.debug(name + ": Initiating system shutdown.");
			ITuple shutdown = TupleFactory.createTuple(
					new String[] {TupleFactory.SHUTDOWN});
			try {
				space.out(shutdown);
			} catch (TupleSpaceException e) {
				LOG.error(name + ": Error outputting shutdown tuple.");
				e.printStackTrace();
			}
		}
    }
    
    /**
     * Constructs an adapter, and attempts to automatically determine
     * the url prefix from the root path.
     * 
     * @param up2pPath path to the root directory of the client deployment in
     * the Servlet container (e.g. ../webapps/up2p)
     * @param port	The port which should be used to access U-P2P through HTTP
     **/
    public DefaultWebAdapter(String up2pPath, int port) throws IOException {
    	this(up2pPath, rootPath.contains("\\") ? rootPath.substring(rootPath.lastIndexOf("\\") + 1)
    			: (rootPath.contains("/") ? rootPath.substring(rootPath.lastIndexOf("/") + 1) : rootPath), port);
    }
    
    /**
     * Constructs an adapter.
     * 
     * @param up2pPath path to the root directory of the client deployment in
     * the Servlet container (e.g. ../webapps/up2p)
     * @param urlPrefix	The url prefix that should be used to access this instance
     * 					of up2p
     */
    public DefaultWebAdapter(String up2pPath, String urlPrefix, int configPort) throws IOException {
       // batchAdapters = Collections.synchronizedSet(new HashSet());
       // publishedResources = new HashMap();
    	
        // set the directory path for up2p
        rootPath = up2pPath;
        setUrlPrefix(urlPrefix);
                
        // Note: I'm leaving this intact until the WikipediaProxy is updated, but
        // the system property should not be accessed otherwise as it is globally
        // shared by all U-P2P instances running in the same Tomcat instance
        System.setProperty(UP2P_HOME, rootPath);
        System.out.println("U-P2P set root path: \"" + rootPath + "\"");
        System.out.println("U-P2P set URL prefix: \"" + urlPrefix + "\"");

        // create log and data directories
        File dir = new File(getRealFile("data"));
        if (!dir.exists())
            dir.mkdir();
        dir = new File(getRealFile("log"));
        if (!dir.exists())
            dir.mkdir();

        // load the logger
        LOG = Logger.getLogger(WebAdapter.LOGGER);

        // load configuration
        String configFileName = DefaultWebAdapter.class.getPackage().getName()
                .replace('.', File.separatorChar)
                + File.separator + WEBADAPTER_CONFIG;

        try {
            config = new Config(configFileName);
        } catch (IOException e) {
           LOG.fatal("Error reading properties file for " + configFileName
                   + ".", e);
        }
        
       if(config.getProperty("up2p.password.salt") == null) {
        	LOG.info("Got null password salt, generating new salt.");
        	SecureRandom saltGenerator = new SecureRandom();
        	HexBinaryAdapter hexConverter = new HexBinaryAdapter();
        	byte[] salt = new byte[8];
        	
        	saltGenerator.nextBytes(salt);
        	//if (salt==null)
        	/*	System.out.println("salt is \n");
        		for(byte b :salt)
        			System.out.println(b);*/
        	String saltHex = hexConverter.marshal(salt);
        	config.addProperty("up2p.password.salt", saltHex,
				"Hex string of the salt bytes used for user authentication.");
        	LOG.info("Saved salt: " + config.getProperty("up2p.password.salt"));
        }
            
        notifications = new ArrayList<UserNotification>();
       
        
        //Gnutella servent ID generation -------------------------
        if (config.getProperty("up2p.gnutella.serventId")==null){
    		byte[] serventId = Utilities.generateClientIdentifier(System.currentTimeMillis()+getPort());
    		String idstring = "";
    		for (byte k : serventId){
    			idstring = idstring + String.valueOf(k) + ".";
    		}
    		config.addProperty("up2p.gnutella.serventId", idstring,
			"Gnutella Servent Id, stored for persistence over multiple sessions.");
    		LOG.info("Generated new gnutella servent Id:"+config.getProperty("up2p.gnutella.serventId"));
    	} 
            
        //TUPLESPACE!!================
        tspace = new FastTupleSpace();
        tsWorker = new TSWorker(tspace);
        tsWorker.start();
        
        functionworker = new UP2PFunctionWorker(tspace);
        functionworker.start();
        
        cleaningworker = new CleaningWorker(tspace, 6000);
        cleaningworker.start();
        
        latestSearchResponses = new SearchResponseTree();
        latestSearchResponses.connectToTS(tspace);
        
        Myquerylogger = new QueryLogWorker(tspace);
		//Myquerylogger.start();
        
       
		trustWorker = new TrustWorker(tspace);
		trustWorker.start();
        
		/*
        Document agentdef; //Warning : currently this worker is an experiment but not used in standard UP2P functionallity
        //it just shows how additional agents can be created from XML documents
		try {
			agentdef = TransformerHelper.parseXML(new File(getRealFile("test/AgentResolveURI.xml")));
			resolveURIWorker = UP2PAgentFactory.createWorkerFromXML(agentdef, tspace);
			resolveURIWorker.start();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			System.out.println("DEFWA: Error creating Agent ResolveURI: "+ e);
		} catch (IOException e) {
			LOG.error("IOException "+e+ " could not create agent ResolveURI");
		}
		*/
        
        //============================

        //Other parts of the central machinery in UP2P: access to the repository and network 
        toRepository = new Core2Repository(rootPath, urlPrefix, config);
        
        // WARNING: LatestSearchResponses and toRepository must be initialized before this call
        setPort(configPort);
        // Port should be set before initializing the network so that a valid download
        // port can be advertised at the Gnutella level
        toNetwork = new Core2Network(rootPath, this, config);
        
        //TUPLESPACE initialization for other classes
        toNetwork.initializeTS(tspace); //get the ts worker started on the repository side
        toRepository.initializeTS(tspace); //get the ts worker started on the repository side
        
        downloadMgr = new DownloadManager(tspace, this);
        
        //WARNING: the following need to be created AFTER the repository because they need it!
        //-------------------------------------------
        //create XPathCache for queries to the DB
        xCache = new XPathCache();
        
        //creating the (private) resource manager
        resourceManager = new ResourceManager();
        //-------------------------------------------
        
        toRepository.setRootCommunityId(resourceManager.getRootCommunityId());
        
        toRepository.updateSubnets();
        
       // log initial message
        LOG.info(new java.util.Date().toString()
                + " DefaultWebAdapter initialized.");
    }

    
    
    /**
	 * Retrieves a file from the network through a third party proxy peer. Because calls to this method
	 * originate from the tuple space worker and not the upload servlet, this method
	 * also publishes the downloaded file if the retrieve was successful.
	 * 
	 * @param comId	The community ID of the file to retrieve
	 * @param resId	The resource ID of the file to retrieve
	 * @param filename	The filename to save the resource as
	 * @param peerid	The peer ID (hostname:port/urlPrefix) of the peer actually serving the file
	 * @param relayUrl	The url (hostname:port/urlPrefix) of the peer to use as a relay
	 * @param relayIdentifier	The integer identifier used to pair peers through the relay
	 * @return	The downloaded resource file, or null if the download failed.
	 */
	public File retrieveFromNetworkThroughProxy(String comId, String resId, String filename, String peerid, 
			String relayUrl, int relayIdentifier) throws NetworkAdapterException {
		latestSearchResponses.setDownloading(comId, resId);
		File resFile = downloadMgr.retrieveFromNetwork(comId, resId, filename, peerid, true, relayUrl, relayIdentifier);
		if(resFile != null) {
			File attachmentDir = new File(getAttachmentStorageDirectory(comId, resId));
			// Publish the file if it was retrieved successfully
			try {
				publish(comId, resFile, attachmentDir);
				addNotification("Resource \"" + resFile.getName() + "\" was successfully fetched through a relay peer.");
			} catch (Exception e) {
				LOG.error("DefWA: Error publishing file fetched through proxy download.");
				
				// Clean up any fetched file
				if(resFile.exists()) {
					resFile.delete();
				}
				if(attachmentDir.exists()) {
					for(File f : attachmentDir.listFiles()) {
						f.delete();
					}
					attachmentDir.delete();
				}
			}
		}
		return resFile;
	}
    /**
     * Allows XPath queries to the Database through the Cache
     * 
     * @param resourceId resource to be queried
     * @param communityId community to be queried
     * @param pathLocation XPath expression to be applied
     * @return a String containing the answer
     */
    private String getXPathLocation(String resourceId, String communityId,
			String pathLocation) throws CommunityNotFoundException, ResourceNotFoundException {
	
		return xCache.getXPathLocation(resourceId, communityId, pathLocation);
	}
    
    /**
     * Allows XPath queries where several results are returned
     */
    private List<String> getXPAthLocationAsList(String resourceId, String communityId,
			String pathLocation) throws CommunityNotFoundException, ResourceNotFoundException {
    	//go directly to the TS without using XpathCache just because cache doesn't handle multi-valued attributes
    	return tsWorker.getXPathLocationAsList(resourceId, communityId, pathLocation);
    }
    
    
    /*Gets a local file from its community/resource mapping via the fileMapper
     * 
     * @param communityId
     * @param resourceId
     * @return local file
     * /
    public File getLocalFile(String communityId, String resourceId){
    	return toRepository.getLocalFile(communityId, resourceId);
    }*/
    
    /*public File getLocalFileAttachment(String communityId, String resourceId, String attachmentName) throws AttachmentNotFoundException{
    	LOG.debug("DEFWA: Getting a local attachment !");
    	return toRepository.getLocalFileAttachment(communityId, resourceId,attachmentName);
    }*/
    
    /**
     * for testing and viewing by jsp
     * @return
     */
    public ITupleSpace getTS(){
    	return tsWorker.getTS();
    }
    
    /**
     * @see up2p.core.WebAdapter#getUrlPrefix()
     */
    public String getUrlPrefix() {
    	return urlPrefix;
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
     * Sets the urlPrefix that should be used to generate links which refer
     * to this instance of U-P2P
     * @param urlPrefix	The urlPrefix that should be used to generate links which refer
     * 					to this instance of U-P2P
     */
    public void setUrlPrefix(String urlPrefix) {
    	this.urlPrefix = urlPrefix;
    }
    
    /**
     * Prunes any expired notifications from the list, and returns the pruned list.
     * @return	The list of notifications currently pending for the user.
     */
    public List<UserNotification> getNotifications() {
    	// Clear any expired notifications from the list
    	Iterator<UserNotification> notificationIter = notifications.iterator();
    	while(notificationIter.hasNext()) {
    		UserNotification nextNotification = notificationIter.next();
    		if(nextNotification.isExpired()) {
    			notificationIter.remove();
    		}
    	}
    	
    	return notifications;
    }
    
    /**
     * Resets the list of notifications (should be used once the user acknowledges
     * reading the messages).
     */
    public void clearNotifications() {
    	notifications.clear();
    }
    
    /**
     * Adds a new notification to the list of pending notifications.
     * @param notification	The new notification to add.
     */
    public void addNotification(String notification) {
    	notifications.add(new UserNotification(notification));
    }
    
    /////////////////////////////////////////////////////////////////////////////////////search methods ===================================================
    /**
	 * convenience method to directly search locally, generates and returns the queryId
	 * @param communityId
	 * @param query as string, will be converted to default behavior query (substring, case insensitive)
	 * @return
	 */
	public String searchL(String communityId, String stringquery){
		return searchLG(communityId, stringquery, HttpParams.UP2P_SEARCH_LOCAL);	
	}
	/**
	 * convenience method to directly search globally (network + local), generates and returns the queryId
	 * @param communityId
	 * @param query
	 * @return
	 */
	public String searchG(String communityId, String stringquery){
		return searchLG(communityId, stringquery, HttpParams.UP2P_SEARCH_ALL);
	}
	
	// default searching
	private String searchLG(String communityId, String qs, int extent){
		String tohash = communityId+qs;
		String qid = Long.toHexString(new Date().getTime()) + Integer.toHexString(tohash.hashCode());
		search(communityId, new SearchQuery(qs), qid, extent);
		return qid;
	}

    /*
     * @see up2p.core.WebAdapter#browse(String)
     */
    public Set<String> browse(String communityId) {
    	List<String> result;
    	try {
			result = tsWorker.synchronousLocalBrowse(communityId);
			
		} catch (CommunityNotFoundException e) {
			LOG.error("DefWA:"+e);
			result = new ArrayList<String>();
		}
		Set<String> uniqueResults = new TreeSet<String>();
		uniqueResults.addAll(result);
		uniqueResults.remove(""); //this was in the list as an indicator that no answers were found
		return uniqueResults;
    }

    /*
     * @see up2p.core.WebAdapter#browseCommunities()
     */
    public Set<String> browseCommunities() {
        return browse(resourceManager.getRootCommunityId());
    }

    /**
     * Get the actual file from an attachment url
     * The methods checks the given path if it's absolute, otherwise looks in the appropriate directory 
     * (storage directory of the provided resource / community)
     * @param comId communityId 
     * @param rid resourceId
     * @param uri url as found in the resource document
     * @return The resulting attachment file
     * @throws FileNotFoundException 
     */
    public static File getAttachmentFile(String comId, String rid, String uri) throws FileNotFoundException{
    	
    	String originalUri = uri;
        File attachFile = null;
        
        // Strip the 'file:' prefix before trying to find the file
        while(uri.startsWith("file:")){
        	uri = uri.substring(5);
        }
        
        // Decode any URI escape sequences in the filename
        try {
			uri = URIUtil.decode(uri, "UTF-8");
		} catch (URIException e) {
			LOG.error("getAttachmentFile: Failed to decode uri: " + uri);
			e.printStackTrace();
		}
		
		LOG.info("getAttachmentFile: \n\tcommId: " + comId + "\n\tresId: " 
				+ rid + "\n\toriginal uri: " + originalUri + "\n\tprocessed uri: " + uri);
        
        // Create a new file object pointing to the specified url
		attachFile = new File(uri);
        
        // Convert relative path to absolute if necessary
        if (!attachFile.isAbsolute()){
        	LOG.info("getAttachmentFile: Converting filepath relative to absolute");
        	String storagedir = getAttachmentStorageDirectory(comId, rid);
            attachFile = new File(storagedir, attachFile.getName());
        }
        
        LOG.info("getAttachmentFile: Final Path: " + attachFile.getAbsolutePath());
        
        // Ensure file exists, throw an exception if it does not
        if(!attachFile.exists()) {
        	throw new FileNotFoundException("File not found: " +uri +"\n");
        }
        
        return attachFile;	
    }
    
    
    /**
     * Gets a resource file from a community id and file url
     * @param comId communityId 
     * @param url url as found in the resource document
     * @return The resulting resource file
     * @throws FileNotFoundException 
     */
    public static File getResourceFile(String comId, String url) throws FileNotFoundException {
        File resourceFile = null;
        LOG.debug("getResourceFile: ComId: " + comId + " URL: " + url);
        if (url.startsWith("file:")){
        	try {
        		resourceFile = new File((new URL(url)).toURI().getPath());
        		LOG.debug("getResourceFile : URL path :"+ (new URL(url)).toURI().getPath());
        	}catch(NullPointerException ee){ //case of relative path
        		LOG.debug("getResourceFile: relative path");
        		try {
					resourceFile = new File((new URL(url)).getPath());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new FileNotFoundException("file not found:" +url +"\n"+ e);
				}
        	}catch(URISyntaxException e1){
        		e1.printStackTrace();
        		throw new FileNotFoundException("file not found:" +url+"\n"+ e1);
        	} catch (MalformedURLException e2) {
        		e2.printStackTrace();
        		throw new FileNotFoundException("file not found:" +url+"\n"+ e2);
        	}
        }
        else{
        	resourceFile = new File(url); //TODO: which case is this? url doesn't start with "file:"...
        }
        
        // convert relative path to absolute
        if (!resourceFile.isAbsolute()){
        	LOG.info("getResourceFile: Converting filepath relative to absolute");
        	String storagedir = getStorageDirectory(comId);
            resourceFile = new File(storagedir, resourceFile.getName());
        }
        
        LOG.info("getResourceFile: Final Path: " + resourceFile.getAbsolutePath());
        
        // check if XSLT or HTML page exists
        if (!resourceFile.exists()) {throw new FileNotFoundException("file not found:" +url);}
        return resourceFile;	
    }

    /**
     * Change a URL file:thing.ext to the up2p-specific and community-specific full path form.
     * @param communityId community where the attachment is
     * @param resourceId not used
     * @param location original url in the form file:name.ext
     * @return a file: url with an absolute path
     */
    public String getAttachmentFileURL(String communityId, String resourceId,
			String location) {
    	File attFile;
		try {
			// System.out.println("getAttachmentFileURL: " + communityId + " " + resourceId + " " + location);
			attFile = getAttachmentFile(communityId, resourceId, location);
			return attFile.toURI().toURL().toExternalForm();
    	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		}
		return null;
	}

   /* 
   public boolean isResultXSLExists(String communityId){
   		if(getResultsLocation(communityId) != null)
   			return true;
   		return false;
   }*/
   
   /**
    * Returns the location within a resource where the title can be extracted
    * 
    * @param communityId id of the community
    * @return an XPath that points to the title within a resource shared in the
    * given community
    */
   public String getTitleLocation(String communityId) {
       String resultStr=null;
	try {
		resultStr = getXPathLocation(communityId, getRootCommunityId(),
		           "/community/titleLocation");
	} catch (CommunityNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ResourceNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       // by default we guess the title to be in the first element
       if (resultStr == null)
           resultStr = "//@title";
       return resultStr;
   }
   
   /**
    * Returns the title of this resource used in the context of U-P2P.
    * 
    * @param resourceId id of the resource whose title is to be retrieved
    * @param communityId id of the community where the resource is shared
    * @return title of the resource
    */
   public String getResourceTitle(String resourceId, String communityId) {
   	String titleLoc = getTitleLocation(communityId);
       if (titleLoc == null){
       	LOG.error("DefWA::getResourceTitle " + resourceId + " : "+ communityId);
           return "Error getting title location";
       }    
           	  
       String lookup=null;
	try {
		lookup = getXPathLocation(resourceId, communityId, titleLoc);
		
		if (lookup.equals("Error getting title")){
			//retry once
			
			Thread.sleep(500);
			lookup = getXPathLocation(resourceId, communityId, titleLoc);
			
		}
	} catch (CommunityNotFoundException e) {
		LOG.error("getResourceTitle: "+ e);	} 
	catch (ResourceNotFoundException e) {
		LOG.error("getResourceTitle: "+ e);	} 
	catch (InterruptedException e) {
		LOG.error("getResourceTitle: "+ e);	
		}	
	LOG.debug("title location :"+titleLoc+"; get title returning:"+ lookup);
       return lookup;
   }
  
   
   /** get the XPath search terms available for querying a particular community,
    * mapped to their user-friendly representation
    * @param communityId id of the community
    * @return a list of xpaths that point to nodes that can be filtered for
    */
   public Map<String,String> getSearchTerms(String communityId){

	   // get the DOM representing the community
	   Node communityDOM = this.getResourceAsDOM(getRootCommunityId(), communityId);
	   
	   Map<String,String> searchTermsMap = new HashMap<String,String>();
	   /* evaluate Xpath to get the map from:
	   <searchTerm userfriendly="A Line in the Play">/PLAY//LINE</searchTerm>
	   */
	   XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = null;
		try {
			expr = xpath
			.compile("/community/searchTerms/searchTerm"); 

			NodeList terms = (NodeList)expr.evaluate(communityDOM, XPathConstants.NODESET );
			
			for(int i =0;i<terms.getLength();i++){
				Node term = terms.item(i);
				//path search term
				String XP = term.getNodeValue();
				//user-friendly representation, by default the XPath
				String UF =XP;
				
				if(term.getAttributes() != null
						&& term.getAttributes().getNamedItem("UserFriendly") != null) {
					//get the user-friendly representation provided
					UF = term.getAttributes().getNamedItem("UserFriendly").getNodeValue();
				}
				//put in the map of responses
				searchTermsMap.put(XP, UF);
			}



		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		}
	   //TODO: get a specific attribute of the community
	   return searchTermsMap;
   }
   
   /**
    * Determines whether the input resource is locally stored in the referenced community
    * @param comId Community identifier
    * @param rId resource Identifier
    * @param clearCache Determines whether the local cache should be reset before querying
    * @return true is the resource is found locally, false otherwise
    */
   public boolean isResourceLocal(String comId, String rId, boolean clearCache) {
	   if (clearCache) {
		   xCache.resetSelectively(comId, rId);
	   }
 	   
	   try{
		   getXPathLocation(rId, comId, ".");
		   LOG.debug("DEFWA: resource is Local:"+comId+" / "+rId);
		   return true; 
	   }
	   catch (ResourceNotFoundException e) {
		   LOG.debug("DEFWA: resource not found:"+comId+" / "+rId);
		   
	   }
	   catch(CommunityNotFoundException e){
		   LOG.debug("DEFWA: community not found:"+comId+" / "+rId);
		   
	   }
	   return false;
   }
   
   /**
    * Determines whether the input resource is locally stored in the referenced community.
    * The local cache is not reset before querying.
    * @param comId Community identifier
    * @param rId resource Identifier
    * @return true is the resource is found locally, false otherwise
    */
   public boolean isResourceLocal(String comId, String rId) {
	   return isResourceLocal(comId, rId, false);
   }
   

    /** Dumps the database to standard out. * /
    public void dumpDatabase() {
        if (repository instanceof DefaultRepository)
            ((DefaultRepository) repository).dumpDatabase();
    }*/

    /*
     * @see up2p.core.WebAdapter#getConfigProperty(String, String)
     * TODO: move to UserWA
     */
    public String getConfigProperty(String propertyName, String defaultValue) {
        return config.getProperty(propertyName, defaultValue);
    }

    /*
     * @see up2p.core.WebAdapter#getConfigPropertyAsInt(String, int)
     * TODO:move to UserWA
     */
    public int getConfigPropertyAsInt(String propertyName, int defaultValue) {
        return config.getPropertyAsInt(propertyName, defaultValue);
    }

    
    /*
     * @see up2p.core.WebAdapter#getHost()
     */
    public String getHost() {
        return localHost;
    }

    /*
     * @see up2p.core.WebAdapter#getPort()
     */
    public int getPort() {
        return localPort;
    }

    /**
     * Translates from a relative path to a real path using the root directory
     * of the up2p application as a base.
     * 
     * @param filePath path of a file relative to the webserver context
     * @return the full path to the file
     */
    protected String getRealFile(String filePath) {
        return getRootPath() + File.separator + filePath;
    }





    
    /**Resource Manager Methods replacing indirect calls through get RM from servlets, etc.
	 * ============================================================================
	 * 
	 */
    
    /**
     * Gets the location of the creation HTML page for creating objects in the
     * community.
     * 
     * @param communityId id of the community
     * @return an URL location for the create HTML page
     */
    public String getCreateLocation(String communityId) {
        
    	String location= null;
		try {
			location = getXPathLocation(communityId, getRootCommunityId(),
			        "/community/createLocation");
		} catch (CommunityNotFoundException e) {
			LOG.error("getCreateLocation: "+ e);
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.error("getCreateLocation: "+ e);
		}
        if (location != null && location.length()>0)
    		return getAttachmentFileURL(getRootCommunityId(),communityId, location);
    	else 
    		return null;
    }
    
    /**
     * Gets the location of the home HTML page that should be displayed when
     * first viewing a community.
     * 
     * @param communityId	Id of the community
     * @return	URL location for the home HTML page, or null if none was
     * 			defined
     */
    public String getCommunityHomeLocation(String communityId) {
        
    	String location= null;
		try {
			location = getXPathLocation(communityId, getRootCommunityId(),
			        "/community/homeLocation");
		} catch (CommunityNotFoundException e) {
			LOG.error("getHomeLocation (Root community could not be found): " + e);
		} catch (ResourceNotFoundException e) {
			LOG.error("getHomeLocation (Specified comm ID could not be found): " + e);
		}
        if (location != null && location.length()>0) {
    		return getAttachmentFileURL(getRootCommunityId(), communityId, location);
        } else {
    		return null;
    	}
    }
    
    /**
     * Returns the path names for stylesheets to be used when viewing the home page
     * in a community. One or more stylesheets will result in
     * corresponding <code>&lt;link href="..." rel=stylesheet&gt;</code>
     * elements in the <code>&lt;head&gt;</code> section of the HTMl output.
     * Stylesheets are given as a whitespace-separated list in the 'style'
     * attribute in 'homeLocation' in the community definition.
     * 
     * @param communityId id of the community whose stylesheets are to be
     * included.
     * @return a list of <code>String</code> s that are directly inserted into
     * the 'href' attribute of link elements
     */
    public Iterator<String> getCommunityHomeStylesheet(String communityId) {
        return getStylesheets(communityId, "homeLocation");
    }

    /**
     * Gets the location of the display XSLT for displaying objects in the
     * community.
     * 
     * @param communityId id of the community
     * @return an URL location for the display XSLT
     */
    public String getDisplayLocation(String communityId) {
        String location=null;
		try {
			location = getXPathLocation(communityId, getRootCommunityId(),
			        "/community/displayLocation");
		} catch (CommunityNotFoundException e) {
			LOG.error("getDisplayLocation: "+ e);
		} catch (ResourceNotFoundException e) {
			LOG.error("getDisplayLocation: "+ e);		}
		if (location != null && location.length()>0) {
			LOG.debug("getDisplayLocation: Fetching: " + location);
    		return getAttachmentFileURL(getRootCommunityId(),communityId, location);
		} else { 
    		return null;
		}
    }

    /**
     *  get the XSLT stylesheet to display this community nicely
     * @param communityId
     * @return
     */
    public String getCommunityDisplayLocation(String communityId) {
    	String location=null;
    	try {
    		location = getXPathLocation(communityId, getRootCommunityId(),
    				"/community/communityDisplayLocation");
    		LOG.debug("getXPathLocation got: " + location);
    	} catch (CommunityNotFoundException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (ResourceNotFoundException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	if (location != null && location.length()>0)
    		return getAttachmentFileURL(getRootCommunityId(), communityId, location);
    	else 
    		return null;
	}

    /**
     * Gets the location of the result XSLT for displaying search results in the
     * community.
     * 
     * @param communityId id of the community
     * @return an URL location for the result XSLT
     */
	public String getCommunityResultsLocation(String communityId) {
	String location=null;
	try {
		LOG.debug( "DEFWA: getResultsLocation::"+communityId+"/"+getRootCommunityId());
		location = getXPathLocation(communityId, getRootCommunityId(),
				"/community/resultsLocation");
		
	} catch (CommunityNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ResourceNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if (location != null && location.length()>0)
		return getAttachmentFileURL(getRootCommunityId(),communityId, location);
	else 
		return null;
	}
    
	/**
     * Gets the location of the header logo to use for displaying the specified
     * community.
     * 
     * @param communityId	ID of the community
     * @return A URL location for the specified header logo, or null if no custom
     * 		   header logo was specified for the given community.
     */
	public String getCommunityHeaderLogo(String communityId) {
		String header = null;
		if(communityId == null) {
			LOG.debug("DEFWA: getHeaderLogo called with null communityId.");
			return null;
		}
		
		try {
			LOG.debug("DEFWA: getHeaderLogo::"+communityId+"/"+getRootCommunityId());
			header = getXPathLocation(communityId, getRootCommunityId(),
					"/community/headerLogo");
		} catch (CommunityNotFoundException e) {
			LOG.debug("DEFWA: getHeaderLogo called for non-existent community ID: " + communityId);
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			LOG.debug("DEFWA: getHeaderLogo: Community resource could not be found: " + communityId);
			e.printStackTrace();
		}
		
		if (header != null && header.length()>0) {
			File headerFile;
			try {
				headerFile = getAttachmentFile(getRootCommunityId(), communityId, header);
			} catch (FileNotFoundException e) {
				LOG.error("DEFWA: Header file was specified, but could not be found.");
				return null;
			}
            return "/" + getUrlPrefix() + "/community/"
                    + getRootCommunityId() + "/" + communityId + "/"
                    + headerFile.getName();
			
		}
		return null;
	}
	
	/**
     *  Defines the Storage directory to be used for a file in community "communityId"
     * @param communityId
     * @param id
     */ 
    public static String getStorageDirectory(String communityId) {
		
		return getRootPath() + File.separator+"community"+File.separator+communityId;
	}
    
    /**
     *  Defines the Storage directory to be used for the attachments of a resource.
     * @param communityId	The community the resource belongs to
     * @param resourceId	The id of the resource the attachments belong to	
     */ 
    public static String getAttachmentStorageDirectory(String communityId, String resourceId) {
		
		return getRootPath() + File.separator + "community" 
			+ File.separator + communityId + File.separator + resourceId;
	}
    
    /**
     * Returns the path names for stylesheets to be used when creating a
     * resource in a community. One or more stylesheets will result in
     * corresponding <code>&lt;link href="..." rel=stylesheet&gt;</code>
     * elements in the <code>&lt;head&gt;</code> section of the HTMl output.
     * Stylesheets are given as a whitespace-separated list in the 'style'
     * attribute in 'searchLocation' in the community definition.
     * 
     * @param communityId id of the community whose stylesheets are to be
     * included.
     * @return a list of <code>String</code> s that are directly inserted into
     * the 'href' attribute of link elements
     */
    public Iterator<String> getCommunityCreateStylesheet(String communityId) {
        return getStylesheets(communityId, "createLocation");
    }

    /**
     * Returns the path names for stylesheets to be used when displaying a
     * resource in a community. One or more stylesheets will result in
     * corresponding <code>&lt;link href="..." rel=stylesheet&gt;</code>
     * elements in the <code>&lt;head&gt;</code> section of the HTMl output.
     * Stylesheets are given as a whitespace-separated list in the 'style'
     * attribute in 'displayLocation' in the community definition.
     * 
     * @param communityId id of the community whose stylesheets are to be
     * included.
     * @return a list of <code>String</code> s that are directly inserted into
     * the 'href' attribute of link elements
     */
    public Iterator<String> getCommunityDisplayStylesheet(String communityId) {
        return getStylesheets(communityId, "displayLocation");
    }

    /**
     * Gets the title for a community.
     * 
     * @param communityId community id
     * @return community title
     */
    public String getCommunityTitle(String communityId) {
        return getResourceTitle(communityId, getRootCommunityId());
    }
    
    /**
     * Gets file links from a whitespace-separated list of files (using the
     * "file:<filename>" format) in the provided element and attribute
     * in the community definition.
     * 
     * @param communityId	ID of the community to get file links for
     * @param elementName 	Name of the element to retrieve from e.g.
     * @param attributeName	Name of the attribute to retrieve from e.g.
     * @return list of <code>String</code> file locations (as relative links)
     * 
     */
    private Iterator<String> getFileLinks(String communityId, String elementName,
    		String attributeName) {
    	ArrayList<String> fileList = new ArrayList<String>();
        if (communityId == null) {
            return fileList.iterator();
        }

        String attrValue= null;
		try {
			attrValue = getXPathLocation(communityId, getRootCommunityId(),
			        "//community/" + elementName + "/@" + attributeName);
		} catch (CommunityNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
		}
		
        LOG.debug("Get " + elementName + "/@" + attributeName 
        		+ ". File link returned :" + attrValue);
        
        if (attrValue == null || attrValue.equals("")) {
            LOG.debug("No files found");
            return fileList.iterator();
        }
        	
        // Check if it's a list of files or just one
        if (attrValue.indexOf(" ") > -1) {
            StringTokenizer tokens = new StringTokenizer(attrValue);
            int count = 0;
            while (tokens.hasMoreTokens()) {
                fileList.add(tokens.nextToken());
                LOG.debug("File: "+fileList.get(count));
                count++;
            }
        } else {
            // just one value so return it
            fileList.add(attrValue);
        }

        // Go through file links and convert to URLs
        for (int i = 0; i < fileList.size(); i++) {
            String filename = (String) fileList.get(i);
            File attachFile;
			try {
				attachFile = getAttachmentFile(getRootCommunityId(), communityId, filename);
            fileList.set(i, "/" + getUrlPrefix() + "/community/"
                    + getRootCommunityId() + "/" + communityId + "/"
                    + attachFile.getName());
            } catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        }
        
        return fileList.iterator();
    }

    /**
     * Gets the stylesheet names from a whitespace-separated list in the 'style'
     * attribute of the given element name in the community definition.
     * 
     * @param communityId id of the community to get stylesheets for
     * @param elementName name of the element to retrieve from e.g.
     * displayLocation retrieves from 'community/displayLocation'
     * @return list of <code>String</code> stylesheet locations
     * 
     */
    private Iterator<String> getStylesheets(String communityId, String elementName) {
        return getFileLinks(communityId, elementName, "style");
    }
    
	/**
     * Retrieves the id of the Root Community.
     * 
     * @return id of the Root Community
     */
    public String getRootCommunityId() {
        // get the config value
    	return resourceManager.getRootCommunityId();/*
        String rootCommunityId = getConfigurationValue("up2p.root.id");
            //LOG.debug("DefWA: root community set to:"  + rootCommunityId);
        
        return rootCommunityId;*/
    }

    public String getConfigurationValue(String pname) {
		
		return toRepository.getConfigurationValue(pname);
	}


	/**
     * Gets the location where the schema of the community can be found.
     * 
     * @param communityId id of the community
     * @return an URL location for the XML Schema document
     */
    public String getSchemaLocation(String communityId) {
    	String location=null;
		try {
			location = getXPathLocation(communityId, getRootCommunityId(),
			"/community/schemaLocation");
		} catch (CommunityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (location != null && location.length()>0)
    		return getAttachmentFileURL(getRootCommunityId(),communityId, location);
    	else 
    		return null;
    	
    	/*
    	File schemafile;
		try {
			schemafile = getAttachmentFile(getRootCommunityId(),communityId, location);
		
        
    	return schemafile.toURI().toURL().toExternalForm();
    	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		}
		return null;*/
    }
    
    
    /*
     * Gets the short name of the community. The short name contains no spaces
     * or special characters.
     * 
     * @param communityId id of the community
     * @return the short name of the community or <code>null</code> if not
     * found
     * /
    public String getCommunityName(String communityId) {
        try {
			return getXPathLocation(communityId, getRootCommunityId(), "/community/name");
		} catch (CommunityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }*/
    
    /**
     * Returns the path names for stylesheets to be used when searching a
     * resource in a community. One or more stylesheets will result in
     * corresponding <code>&lt;link href="..." rel=stylesheet&gt;</code>
     * elements in the <code>&lt;head&gt;</code> section of the HTMl output.
     * Stylesheets are given as a whitespace-separated list in the 'style'
     * attribute in 'searchLocation' in the community definition.
     * 
     * @param communityId id of the community whose stylesheets are to be
     * included.
     * @return a list of <code>String</code> s that are directly inserted into
     * the 'href' attribute of link elements
     */
    public Iterator<String> getCommunitySearchStylesheet(String communityId) {
        return getStylesheets(communityId, "searchLocation");
    }

    /**
     * Returns the path names for stylesheets to be used when displaying search
     * results in a community. One or more stylesheets will result in
     * corresponding <code>&lt;link href="..." rel=stylesheet&gt;</code>
     * elements in the <code>&lt;head&gt;</code> section of the HTMl output.
     * Stylesheets are given as a whitespace-separated list in the 'style'
     * attribute in 'searchLocation' in the community definition.
     * 
     * @param communityId id of the community whose stylesheets are to be
     * included.
     * @return a list of <code>String</code> s that are directly inserted into
     * the 'href' attribute of link elements
     */
    public Iterator<String> getCommunitySearchResultsStylesheet(String communityId) {
        return getStylesheets(communityId, "resultsLocation");
    }
    
    /**
     * Returns the path names for javascript files to be included in
     * displayResults.jsp when displaying community search results.
     * Stylesheets are given as a whitespace-separated list in the 'style'
     * attribute in 'searchLocation' in the community definition.
     * 
     * @param communityId id of the community whose javascript are to be
     * included.
     * @return a list of <code>String</code>s that are directly inserted into
     * 		the "src" attribute of script elements
     */
    public Iterator<String> getCommunitySearchResultsJavascript(String communityId) {
        return getFileLinks(communityId, "resultsLocation", "jscript");
    }
   
    
    /**
     * Gets the location of the search HTML page for searching for objects in
     * the community.
     * 
     * @param communityId id of the community
     * @return an URL location for the search HTML page
     */
    public String getSearchLocation(String communityId) {
        //get relative url stored in the DB
    	String location= null;
		try {
			location = getXPathLocation(communityId, getRootCommunityId(),
			        "/community/searchLocation");
		} catch (CommunityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//get full URL in file:/C:/full/path/file.txt format
    	if (location != null && location.length()>0)
    		return getAttachmentFileURL(getRootCommunityId(),communityId, location);
    	else 
    		return null;
    }
    
    
    /**
	 * Stores the last query run by the user
	 * @param lastQuery	A map of values keyed by XPath representing the
	 * 					last query
	 */
	public void setLastQuery(Map<String, String> lastQuery) {
		latestSearchResponses.setLastQueryMap(lastQuery);
	}
	
	/**
	 * @return	A map of values keyed by XPaths representing the last query
	 * 			initiated by the user.
	 */
	public Map<String, String> getLastQuery() {
		return latestSearchResponses.getLastQueryMap();
	}

  
	/**
     * Clears the cache of XPath values used by the resource manager. The cache
     * should be cleared when the database had been modified by removing items.
     *  
     */
    private void clearXPathCache() {
        xCache.reset();
    }

    
	/**
	 * Processes a resource file, and replaces all "file:<filename>" attachment
	 * references with a URI compatible version by replacing illegal URI
	 * characters with underscores.
	 * 
	 * @param resourceFile	The XML resource file that should be processed
	 * @param attachDirectory	The directory in which attachment files can be found
	 * @return	A map of the attachment URIs keyed by their new file names (i.e. Map<Filename, EscapedLinkString>)
	 */
    public Map<String, String> renameAttachmentsToURI(File resourceFile, File attachmentDir) 
    		throws IOException, SAXException 
    {	
    	// Use a filter chain to find all attachment names
    	XMLReader reader = TransformerHelper.getXMLReader();
		DefaultResourceFilterChain chain = new DefaultResourceFilterChain();
		FileAttachmentFilter attachListFilter = new FileAttachmentFilter("file:");
		chain.addFilter(attachListFilter);
		chain.doFilter(reader, new InputSource(new FileInputStream(resourceFile)));
		Map<String, String> attachments = attachListFilter.getNameToLinkMap();
		
		// Determine which attachment names need to have special characters replaced with
		// underscores, and rename the attachments files accordingly
		Map<String, String> newFileReferences = new HashMap<String, String>();
		Map<String, String> replacementPairs = new HashMap<String, String>();
		boolean fileRewriteRequired = false;
		
		for(String attachmentName : attachments.keySet()) {
			File oldAttachment = new File(attachmentDir, attachmentName);
			if(!oldAttachment.exists()) {
				throw new IOException("Attachment file: " + attachmentName + " could not be found.\n"
						+ "This typically occurs when special characters in the file name were not "
						+ "encoded properly during file upload, or when a required attachment was "
						+ "accidentally left out of the upload. Try removing any special characters from "
						+ "the attachment filename, and ensure the file is included in the upload form.");
			}
			
			String newAttachmentName = FileUtil.normalizeFileName(attachmentName);
			if(!attachmentName.equals(newAttachmentName)) {
				// Attachment name must be changed, rename the file and notify the user
				addNotification("File: \"" + attachmentName + "\" was renamed to \"" + newAttachmentName + "\"");
				File renameTarget = new File(attachmentDir, newAttachmentName);
				while(renameTarget.exists()) {
					// Just append underscores in the case of duplicate attachment names for now
					// (should be very rare that attachments differ only by illegal characters anyway)
					newAttachmentName = newAttachmentName + "_";
					renameTarget = new File(attachmentDir, newAttachmentName);
				}
				
				if(oldAttachment.renameTo(renameTarget)) {
					newFileReferences.put(newAttachmentName, URIUtil.encodeQuery("file:" + newAttachmentName));
					replacementPairs.put(attachmentName, newAttachmentName);
					fileRewriteRequired = true;
				} else {
					LOG.error("Attachment rename failed!");
					throw new IOException("Attachment file: " + attachmentName + " could not be renamed.");
				}
				
				oldAttachment.delete();
			} else {
				newFileReferences.put(attachmentName, URIUtil.encodeQuery("file:" + attachmentName));
			}
		}
		
		if(fileRewriteRequired) {
			// Generate a new resource file with the updated attachment references
			File newResourceFile = new File(resourceFile.getAbsolutePath() + ".temp");
			PrintWriter resourceOutput = new PrintWriter(new OutputStreamWriter(new FileOutputStream(newResourceFile), "UTF-8"));
			BufferedReader resourceInput = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFile), "UTF-8"));
			String xmlLine;
			
			while((xmlLine = resourceInput.readLine()) != null) {
				for(String attachmentName : replacementPairs.keySet()) {
					xmlLine = xmlLine.replace("file:" + attachmentName, "file:" + replacementPairs.get(attachmentName));
				}
				// System.out.println(xmlLine); // DEBUG
				resourceOutput.println(xmlLine);
			}
			resourceInput.close();
			resourceOutput.close();
			
			// Replace the original resource file with the new file, and return the map
			// of new attachment filenames / URIs
			resourceFile.delete();
			newResourceFile.renameTo(resourceFile);
		}
		return newFileReferences;
    }
	
	
    public DefaultResourceFilterChain generateHashFilterChain(File resourceFile) throws IOException, SAXException {
    	XMLReader reader =TransformerHelper.getXMLReader();
		
		// Create a filter chain for capturing attachment links
		DefaultResourceFilterChain chain = new DefaultResourceFilterChain();
		// Add filter to catch attachment links
		FileAttachmentFilter attachListFilter = new FileAttachmentFilter("file:");
		chain.addFilter(attachListFilter);
		// Process chain to get attachment links
		chain.doFilter(reader, new InputSource(
				new FileInputStream(resourceFile)));

		/*
		 * We now have a list of attachments so we can remove them before
		 * hashing.
		 */

		// create new filter chain
		chain = new DefaultResourceFilterChain();
		chain.setProperty(FileAttachmentFilter.ATTACH_LIST_PROPERTY,
				attachListFilter.getNameToLinkMap());
		
		// add a filter to remove attachment links
		chain.addFilter(AttachmentReplacer
				.createRemovalFilter(attachListFilter));
		// add a digest filter to generate the resource ID
		chain.addFilter(new DigestFilter());
		// add serialize filter for debugging
		StringWriter strW = null;
		if (LOG.isDebugEnabled()) {
			strW = new StringWriter();
			SerializeFilter serialFilter = new SerializeFilter(strW, true,
					DownloadServlet.ENCODING);
			chain.addFilter(serialFilter);
		}

		// execute the filter chain
		chain.doFilter(reader, new InputSource(
				new FileInputStream(resourceFile)));
		
		if (LOG.isDebugEnabled()){
			LOG.debug("result:"+strW.toString());
		}
		
		return chain;
    }
    

	/**
	 * Publishes a file by adding it to the community database repository, and
	 * maps any attachments.
	 * 
	 * @param communityId	The id of the community the file belongs to
	 * @param resourceFile		The file to publish
	 * @param attachmentDir	The temporary directory to look for attachments to the resource
	 * @return	The resource id generated from the file
	 * 
	 * @throws SAXParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws DuplicateResourceException
	 * @throws NetworkAdapterException
	 * @throws ResourceNotFoundException
	 */
	public String publish(String communityId, File resourceFile, File attachmentDir) throws  SAXException,
	IOException, DuplicateResourceException, NetworkAdapterException ,SAXParseException, ResourceNotFoundException 
	{
		LOG.debug("*** Entering publish with params: cid="+communityId+"resfile="+resourceFile.getAbsolutePath()+"attach directory="+attachmentDir+" ****");
		
		// From the provided file name, get the file itself, stored in the proper directory 
		// (handled by servlets), and URI encode any attachment links before attempting validation
		resourceFile = new File(getStorageDirectory(communityId), resourceFile.getName());
		Map<String,String> attachments = null;
		if (attachmentDir==null) //we know already there won't be any attachments
			attachments=new HashMap<String,String>();
		else
			attachments = renameAttachmentsToURI(resourceFile, attachmentDir);
		if(!resourceFile.exists()) {
			throw new IOException("Specified resouce file: " + resourceFile.getAbsolutePath() + " could not be found for publishing.");
		}

		// Perform validation (if enabled)
		LOG.debug("Validation is on: "+VALIDATION_ON);
		if (VALIDATION_ON) {
			// Try to get the location of the schema file for the community
			String schemaLocation = getSchemaLocation(communityId);
			File schemaFile= null;
			schemaFile = getAttachmentFile(getRootCommunityId(), communityId, schemaLocation);
		
			// Once we've found the exact file, go back to a url in order to set the validation
			schemaLocation = schemaFile.toURI().toURL().toExternalForm().replace("file:", "file://");
			XMLReader validationReader = TransformerHelper.getXMLReader(schemaLocation);
			
			// Perform the validation
			DefaultResourceFilterChain validationChain = new DefaultResourceFilterChain();
			validationChain.addFilter(new ValidationFilter());
			validationChain.doFilter(validationReader, new InputSource(
					new InputStreamReader(new FileInputStream(resourceFile), "UTF-8")));
		}
		
		// Generate the hash filter chain, and determine the resource's id
		DefaultResourceFilterChain chain = generateHashFilterChain(resourceFile);
		String resourceId = (String) chain.getProperty(DigestFilter.HASH_PROPERTY); 
		
		LOG.debug("Publish: Resource id: " + resourceId);
		if (isResourceLocal(communityId, resourceId)) {
			LOG.debug("Publish: Resource with id " + resourceId + " already exists locally!");
			throw new DuplicateResourceException(resourceId, communityId);
		}
		
		// Get the attachment storage directory so that we can search for attachments.
		// TODO: Use a temp directory for each upload
		String resourceDirectory = resourceFile.getParent();
		LOG.debug("Publish: Directory of uploaded file: "+ resourceDirectory);
		
		String debugstring ="Publish: attachments: ";
		for(String k : attachments.keySet()){
			debugstring= debugstring + "\n"+ k + " mapped to: " + attachments.get(k);
		}
		LOG.debug(debugstring);
		

		/*
		 * We'll struggle to find each attachment, 
		 * copy it to the home directory, 
		 * then keep a map of names / files
		 */
		Map<String,File> AttachFiles = new HashMap<String,File>(); 
		Iterator<String> attachmentKeys = attachments.keySet().iterator();
		String fileLink = "";
		String fileName = "";
		
		/*
		 * Now the attachment links returned by this iterator are the full URL of each attachment
		 * in the form file:[path][filename] 
		 */
		while (attachmentKeys.hasNext()) {
			fileName = attachmentKeys.next();
			fileLink = attachments.get(fileName);

			File attach=null;
			try {
				LOG.debug("Publish: Attachment link: " + fileLink + " Filename:" + fileName);
				try{
					attach = new File(attachmentDir, fileName);
					if (!attach.canRead()) {
						// Attachment was not in the supplied directory, check if it's already been moved
						// to the proper directory ("RetrieveServlet behaves this way")
						attach = new File(getAttachmentStorageDirectory(communityId, resourceId), fileName);
					}
				}
				catch (NullPointerException npe1) {

					// this exception is thrown if the URL is of the form file:foo/bar/thing.txt
					LOG.debug("relative path!");
					try{
						attach = new File((new URL(fileLink)).getPath());
					}
					catch (MalformedURLException e){//still can't get it right!!
						LOG.error("MapResource: Malformed URL: "+fileLink);
						LOG.error(e);}
				}


				// Once the attachment is found, copy it to the specific resource's folder
				if(attach.canRead()){  
					LOG.debug("Publish: Attachment found at location: "+ attach.getAbsolutePath());
					if(!attach.getParent().equals(getAttachmentStorageDirectory(communityId, resourceId))){
						LOG.debug("Publish: Copying attachment to correct storage directory:"+getAttachmentStorageDirectory(communityId, resourceId));
						
						// Ensure the attachment directory for the community exists
						File attachDirectory = new File(getAttachmentStorageDirectory(communityId, resourceId));
						attachDirectory.mkdir();
						
						File AttachmentCopy = new File(getAttachmentStorageDirectory(communityId, resourceId), attach.getName());
						//copy to the new file using static utility function from up2p.util.FileUtil
						FileOutputStream AttachOutStream = new FileOutputStream(AttachmentCopy);
						FileUtil.writeFileToStream(AttachOutStream, attach, true);

						// TODO: Disabled for testing, make sure to uncomment
						attach = AttachmentCopy;
					}
				} else {
					LOG.error("Publish: Attachment "+ fileName + "not found. Tentative location was: "+ attach.getAbsolutePath());
				}
				
				LOG.debug("changing attachment link in map to:" + attach.toURI().toURL().toString());
				AttachFiles.put(fileLink, attach); //we now consider the copy

			} catch (Exception e1) {
				// if we can't copy the file then too bad, if there's a problem with the URL too bad as well
				LOG.error(e1);
			}
		}//end while loop of attachments

		//now we've got a map with all the correct files.

		// map local file to the resource id
		mapResource(communityId, resourceId, resourceFile, AttachFiles);

		LOG.info("Publish: Successfully parsed resource id " + resourceId
				+ " in community id " + communityId + ".");

		// store in XML repository
		// code got from store(file,...) from Def Repository
		// check if contents are readable
		if (!resourceFile.canRead()) {
			LOG.info("Attempt to store a file failed. File "
					+ resourceFile.getAbsolutePath() + " not found.");
			throw new ResourceNotFoundException("Unable to read resource from "
					+ "file " + resourceFile.getAbsolutePath() + ".");
		}

		try {
			// parse in the document as XML
			Document resourceXML = TransformerHelper
			.parseXML(new FileInputStream(resourceFile));
			LOG.info("Def WA > publishing:  " + resourceFile.getAbsolutePath());
			if (resourceXML==null){
				LOG.error("ERROR: resourceXML is null.");
			}
			LOG.info("Core2Repository> publish: about to store: " + resourceFile.getAbsolutePath());
			//===============================================================
			//method to set apart from the rest
			tsWorker.publish(resourceXML, resourceId, communityId);
			//=================================================================
			xCache.resetSelectively(communityId, resourceId);
		} catch (IOException e) {
			LOG.error("Error occured in store(File).", e);
			throw new ResourceNotFoundException(
			"Store operation failed because the resource file could not be read.");
		} catch (SAXException e) {
			LOG.error("Error occured in store(File).", e);
			throw new ResourceNotFoundException(
			"Store operation failed because there was an error parsing the XML resource file.");
		}


		// Success! Remove any pending push requests for the resource and 
		// return the resource ID.
		//clearFailedTransfer(communityId, resourceId);
		return resourceId;
	}

    
	
    


	/**
     * Maps a resource and its attachments to their corresponding files on the
     * local file system.
     * 
     * @param communityId id of the community under which the resource will be
     * mapped
     * @param resourceId id of the resource to be mapped
     * @param resourceFile the file containing the resource
     * @param chain the filter chain that handled the parsing and processing of
     * the XML file
     */
    private void mapResource(String communityId, String resourceId,
            File resourceFile, Map<String,File> attachments) {
        LOG.info("mapResource: Mapping resource ID " + resourceId
                + " in community '"
                + communityId + "' to "
                + resourceFile.getName());
        
        
        tsWorker.addResource(communityId, resourceId, resourceFile.getName(), attachments);
    }
    
   

    /*
     * @see up2p.core.WebAdapter#remove(String, String)
     * note: moved to Network and repository
     */
    public void remove(String communityId, String resourceId)
             {
        LOG.debug("Removing resource " + resourceId + " from community id "
                + communityId);

        //toNetwork.remove(communityId, resourceId); no more removing from network
        tsWorker.remove(communityId, resourceId);
        
     // clear the cache
        xCache.resetSelectively(communityId, resourceId);
        //clearXPathCache();
        
        //remove from search results [isLocal now must be false]
        latestSearchResponses.removeLocal(communityId,resourceId);
        
    }
    
    /*
     * @see up2p.core.WebAdapter#setHost(String)
     * only called by AccessFilter 
     */
    public void setHost(String host) {
    	toRepository.setHost(host);
    	
        try {
            InetAddress newHost = InetAddress.getByName(host);
            if (newHost.isLoopbackAddress()) {
                // change loopback to real IP
                // check if preferred interface is set
                String preferredName = config.getProperty(
                        CONFIG_PREFERRED_NETIFACE_NAME, null);
                if (preferredName != null) {
                    newHost = NetUtil.getFirstInetAddress(preferredName);
                } else {
                    newHost = NetUtil.getFirstNonLoopbackAddress();
                }
            }
            if (newHost != null) {
                localHost = newHost.getHostAddress();
                LOG.info("Host address set to '" + localHost + "'.");
            } else {
                // enumeration failed or something went wrong
                localHost = InetAddress.getLocalHost().getCanonicalHostName();
                LOG.error("An error occured in getting the real IP for the"
                        + " loopback interface. Setting address to host name '"
                        + localHost + "'.");
            }
        } catch (UnknownHostException e) {
            LOG.error("An error occurs in getting the address of host '" + host
                    + "'.", e);
        }
        
        String monitorIP = config.getProperty("up2p.monitoring.ip") ;
        String monitorport = config.getProperty("up2p.monitoring.port") ;
       
        if(monitorIP != null) {
        	
        	String networkId =getHost()+":"+config.getProperty("up2p.gnutella.incoming", "6346")+"["+urlPrefix+"]";
        	String gserventId =	config.getProperty("up2p.gnutella.serventId"); //
        	try {
				mw = new MonitorWorker(tspace, networkId, gserventId);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //the gnutella incoming port uniquely identifies a node on a given host. 
        	mw.addUDPListener(monitorIP, Integer.parseInt(monitorport));
        	mw.start();
        }
    }

    /*
     * @see up2p.core.WebAdapter#setPort(int)
     */
    public void setPort(int port) {
        localPort = port;
        toRepository.setPort(port);
        latestSearchResponses.setLocalLocationString("localhost:"
        		+ port + "/" + getUrlPrefix());
    }

    /*
     * @see up2p.core.WebAdapter#shutdown()
     */
    public void shutdown() {
        LOG.info(new java.util.Date().toString()
                + " Shutting down the WebAdapter of U-P2P.");
        tsWorker.initiateShutdown();
    }

	/*
	 * get the File containing a resource
	 * @return a file: 
	 * deprecated: use getLocalFile
	 * /
	 File FMgetResourceMapping(String communityId, String resourceId) throws ResourceNotFoundException{
		
		return toRepository.getLocalFile(communityId, resourceId);
	}*/

	public DownloadService getDownloadService() {
		// TODO Auto-generated method stub
		return toRepository.getDownloadService();
	}

	/** Replaces previous search methods
	 * 
	 * @param communityId
	 * @param query
	 * @param listener a listener for a callback when we have an answer
	 * @param qid some identifier provided by the client (will be provided in the callback)
	 * @param extent 	Determines what the scope of the search should be.
	 * 					This value should be one of HttpParams.UP2P_SEARCH_ALL,
	 * 					UP2P_SEARCH_NETWORK, or UP2P_SEARCH_LOCAL
	 */
	public void search(String communityId, SearchQuery query,
			SearchResponseListener listener, String qid, int extent) {
		// update 9 apr 2009: now accepting DOM metadata
		tsWorker.searchWithDOM(communityId, query.getQuery(), listener, qid, extent);
		//toNetwork.searchNetwork(communityId, query, listener);//TODO: go through tuplespace for this one too
		
	}


	public Node getResourceAsDOM(String communityId, String resourceId) {
		return tsWorker.getLocalDOM(communityId, resourceId);
	}
	
	 /**
	 * Construct a DOM tree representing the community contents.
	 * 
	 * @param communityId	The community to construct a DOM for
	 * @param populate If true, also return the DOM for each resource in the community
	 * @return a DOM containing a root node called "community" with its "id" and "title" as attributes,
	 * and a child for each resource, called "resource", with its "id" and "title" as attributes. If requested each
	 * resource element will have a single child which is the root element of the resource DOM. 
	 */
	public Node getCommunityAsDOM(String communityId, boolean populate) {
		return tsWorker.getLocalCommunityDOM(communityId, getTitleLocation(communityId), populate);
	}


	public void search(String communityId, SearchQuery query, String qid, int extent) {
		//latestSearchResponses.clear(); //clear previous responses
		search(communityId, query, latestSearchResponses, qid, extent); //send search
	}


	public SearchResponse[] getSearchResults(String queryid) {
		SearchResponse[] resp = latestSearchResponses.getResponsesAsArray(queryid);
		LOG.debug("DefWA: getSearchResults for qid="+queryid+" size: "+resp.length);
		/*String debugstring = ""; 
		for (SearchResponse r: resp)
			debugstring+=  r + "\n";
		LOG.debug("GetSearchResults returning:" +debugstring);*/
		return resp; 
	}
	
	/**
	 * Attempts to retrieve a search response for a specific community and
	 * resource ID.
	 * @param communityId	The community ID of the specified resource
	 * @param resourceId	The resource ID of the specified resource
	 * @return	A SearchResponse corresponding to the requested resource, or
	 * 			null if no valid response could be found.
	 */
	public SearchResponse getSearchResponse(String communityId, String resourceId) {
		return latestSearchResponses.getSearchResponse(communityId, resourceId);
	}
	
	

	/**
	 * Retrieves a file from the network.
	 * @param comId	The community ID of the file to retrieve
	 * @param resId	The resource ID of the file to retrieve
	 * @param filename	The filename to save the resource as
	 * @param peerid	The peer ID (hostname:port/urlPrefix) to use as a download source
	 * @return	The downloaded resource file, or null if the download failed.
	 */
	public File retrieveFromNetwork(String comId, String resId, String filename, String peerid) throws NetworkAdapterException {
		latestSearchResponses.setDownloading(comId, resId);
		return downloadMgr.retrieveFromNetwork(comId, resId, filename, peerid); //TODO: now in downloadmanager
	}

	/**
	 * clears the search results from the input query id from the local storage
	 * @param currentSearchId
	 */
	public void clearSearchResults(String qid) {
		
		//latestSearchResponses.clear(qid);
		//for now we'll clear all because otherwise we have some issues with searchresponses being recorded for network searches
		latestSearchResponses.clearAll();
		
	}

	/**
	 * Adds a dummy search response to the list of search responses. This should be called when
	 * a request for a single resource fails and a PUSH message is sent. In this case, the user
	 * is redirected to the search page so they can see if the PUSH message arrives.
	 * @param communityId	The community ID of the resource
	 * @param resourceId	The resource ID of the resource
	 * @param peerId		The peerId of the resource
	 * @param title		The title of the resource
	 * @param filename	The filename of the resource
	 * @param queryId	The query Id to use for the generated SearchResponse
	 */
	public void addDummySearchResponse(String communityId, String resourceId, 
			String peerId, String title, String filename, String queryId) {
		latestSearchResponses.receiveSearchResponse( new SearchResponse[] {
				new SearchResponse(resourceId, title, communityId, filename, 
						null, true, queryId)} );
	}
	
	/**
	 * Issues a push request for a network resource. This should be used when a direct
	 * file transfer has failed, and an attempt to initiate a connection from the network
	 * node should be attempted.
	 * @param communityId	The community ID of the resource to be pushed
	 * @param resourceId	The resource Id of the resource to be pushed
	 * @param peerId	The peer ID of the serving node (IP:Port/urlPrefix)
	 */
	public void issuePushRequest(String communityId, String resourceId, String peerId) {
		
		downloadMgr.addFailedTransfer(peerId, communityId, resourceId); //in case it comes back with a relay request
		latestSearchResponses.setDownloading(communityId, resourceId);
		tsWorker.issuePushRequest(peerId);
	}
	
	
	
	
	
	/**
	 * @return The hex string of salt bytes that should be used for user authentication.
	 */
	public String getSaltHex() {
		return config.getProperty("up2p.password.salt");
	}
	
	/**
	 * @return	The hex string of the hash used to validate the user's password. Returns null
	 * 			if no user has been set.
	 */
	public String getPasswordHashHex() {
		return config.getProperty("up2p.password.hash");
	}
	
	/**
	 * @return	The username that should be used to log in to U-P2P. Returns null
	 * 			if no user has been set.
	 */
	public String getUsername() {
		return config.getProperty("up2p.username");
	}
	
	/**
	 * Sets the username and password hash for the single user of the
	 * U-P2P node
	 * @param username	The username for the account
	 * @param passwordHashHex	A hex string of the user's password's SHA-1 x1000 hash
	 */
	public void setUser(String username, String passwordHashHex) {
		config.addProperty("up2p.username", username,
				"Username required to login to this U-P2P instance.");
		config.addProperty("up2p.password.hash", passwordHashHex,
				"Hex string of the user's password (digested 1000 times with SHA-1)");
	}

	/**
	 * asynchronous download: searches if needed, then downloads (methods starts a new thread, that does all this then dies)
	 * @param comid
	 * @param resid
	 * @return
	 */
	public String asyncDownload(String comid, String resid) {
	return downloadMgr.asyncDownload(comid, resid);	
	}      
	
	public List<String> listDownloads() {
	return downloadMgr.listDownloads();
	}
	
	public String getDownloadStatus(String downid){
		return downloadMgr.getStatus(downid);
	}

	//method moved to download manager
	public String getFailedTransfer(String peerId) {
		
		return downloadMgr.getFailedTransfer(peerId);
	}

	public void allowPush(String resRequest, String peerId) {
		downloadMgr.allowPush(resRequest, peerId);
	}

	/** check if this pair IP, communtiyId match an expected PUSH*/
	public boolean checkForPush(String sender, String communityId) {
		return downloadMgr.checkForPush(sender, communityId);
	}
	        
			
	} ////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
