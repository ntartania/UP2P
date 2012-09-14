package up2p.peer.jtella;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

import protocol.com.dan.jtella.ConnectedHostsListener;
import protocol.com.dan.jtella.HostsChangedEvent;
import stracciatella.StracciatellaConnection;
import stracciatella.subnet.SubnetStracciatellaConnection;
import stracciatella.Connection;
import stracciatella.ConnectionData;
import stracciatella.GUID;
import stracciatella.Host;
import stracciatella.HostCache;
import stracciatella.SearchSession;
import stracciatella.message.MessageReceiver;
import stracciatella.message.PushMessage;
import stracciatella.message.RelayMessage;
import stracciatella.message.SearchMessage;
import stracciatella.message.SearchReplyMessage;
import stracciatella.subnet.SubNetRouter;
import up2p.core.BasePeerNetworkAdapter;
import up2p.core.Core2Network;
import up2p.core.LocationEntry;
import up2p.core.NetworkAdapterException;
import up2p.core.ResourceNotFoundException;
import up2p.core.WebAdapter;
import up2p.repository.ResourceEntry;
import up2p.repository.ResourceList;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.servlet.HttpParams;
import up2p.util.Config;
import up2p.xml.TransformerHelper;

/**
 * Implements a Network Adapter using a Gnutella peer-to-peer protocol.
 * 
 * @author Michael Yartsev (anijap@gmail.com)
 * @author alan
 * @author Alexander Craig
 * @version 1.2
 */

public class JTellaAdapter extends BasePeerNetworkAdapter implements MessageReceiver, SearchResponseListener, ConnectedHostsListener {
    /** Location of local file containing cache of Gnutella hosts. */
    //public static final String HOST_CACHE_FILE = "up2p.peer.jtella.hostCache";
	public static final String HOST_CACHE_FILE = "data" + File.separator + "HostCache.xml";
	
	/** The number of milliseconds that search sessions should remain active for */
	public static final int SEARCH_TIMEOUT_MILLIS = 60000;
	
	/**the number of wanted connections: when the "autoconnect"is disable, this is just a limit*/
	public static int OUTGOING_CONNECTIONS_WANTED = 100;
	public static int INCOMING_CONNECTIONS_WANTED = 20;
	//note: these can be adjusted by the config servlet (and potentially others)

    /** Local port where Gnutella listens for incoming connections. */
    public static final String DEFAULT_LOCAL_PORT = "6346";

    /** Name of Logger used by this adapter. */
    public static final String LOGGER = "up2p.peer.jtella";

    /** Logger used by this adapter. */
    private static Logger LOG = Logger.getLogger(LOGGER);

    /** Location of property file for Log4J configuration for this adapter. */
   // public static final String LOG_PROPERTY_FILE = "up2p.peer.jtella.log4j.properties";
    
    private String serventId; // Gnutella serventId for this peer
    
    /**
     * GNUTellaConnection object (of the JTella library) that implements the Gnutella protocol
     */
    private static StracciatellaConnection c;
    
    /**
     * When a search is received with a duplicate query ID but new search criteria, a new
     * query ID is generated locally to ensure old results are not used. This map stores
     * the mapping of randomly generated query ID's to the original query ID that prompted
     * the search. Entries should be removed once a response has been sent.
     * 
     * Map<randomlyGeneratedQueryID, responseQueryID)
     */
    private Map<String, String> duplicateQueryIds;
    
    /**
     * May used to cache resource lists to reduce overall database
     * access.
     * 
     * Map<community, resourceList>
     */
    private  Map<String, List<String>> hostedResListCache;
    
    /** 
     * A singleton instance of HostCacheParser used to manage the static
     * host cache. This should always be accessed through the singleton
     * accessor "getHostCacheParser()"
     */
    private HostCacheParser hostCacheParser;
    
    /**
     * A map of query ID's of received search messages keyed by community ID.
     * When a search is received, the query ID should be added to this map until
     * a hosted resource list is sent through the connection, at which point the
     * mapping should be removed.
     */
    private Map<String, List<String>> requestedResourceLists;
    
    /** 
     * Stores the messages from the network indexed by their query Id.
     * This way, once the answer comes from the system, we can find the original query message.
     * Note: Multiple requests can be associated with the same query id. In this case, all messages
     * must have the same originating connection or they will be discarded. Only the most recently
     * received request is stored in the messageTable.
     **/
    private Map<String, SearchMessage> messageTable;
    
    /**
     * A map of peer ID's (IP address : port / urlPrefix) and client identifiers
     * (Gnutella GUIDs). This is updated every time a search reply message is received,
     * and the mapping is used to generate PUSH messages when a direct file
     * transfers fails.
     * 
     * Map < peer ID (IP:Port/urlPrefix) , Client Identifier (GUID) >
     */
    private Map<String, GUID> peerIdToClientGuid;
    
    /**
     * A list of all currently active search session threads (primarily used for
     * orderly termination)
     */
    private List<Thread> openSearchSessions;
    
    /**
     * The url prefix that should be included with outgoing SearchResponseMessages
     * and PushMessages.
     */
    private String urlPrefix;
    
    /**
     * The URL that should be advertised as a peer relay if a connection can not
     * be established for a PUSH file transfer.
     */
    private String relayPeerUrl;
    
    /**
     * Creates a JTella Adapter
     * @param incomingPort	The port to listen for Gnutella connections on.
     * 						Uses the default value if null is passed.
     * @param relayUrl	The relay URL (hostname:port/urlPrefix) that should be advertised as
     * 					a peer relay if a PUSH connection fails
     * @param peerDiscovery	Determines whether automatic peer discovery will be enabled
     * @param urlPrefix		The URL prefix for this U-P2P instance
     * @param adapter		The adapter to use to communicate with the tuple space
     */
    public JTellaAdapter(Config config, String urlPrefix, Core2Network adapter){ 
    		/*
    		String incomingPort, boolean peerDiscovery,
    			String relayUrl, String urlPrefix, Core2Network adapter) {
    	*/
    	
		this.urlPrefix = urlPrefix;
    	this.relayPeerUrl = config.getProperty("networkAdapter.relayPeer", "");
    	this.serventId = config.getProperty("up2p.gnutella.serventId");
    	System.out.println("JTELLA:: gnutella servent Id:"+serventId);
    	
    	////////////////////////////////////////////////////set Gnutella Servent ID from config
		byte[] serventIdasArray = new byte[16];
		int i = 0;
		for(String k : serventId.split("\\.")){
			serventIdasArray[i]=Byte.parseByte(k);
			i++;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////
		StracciatellaConnection.setServentIdentifier(serventIdasArray);
    	setWebAdapter(adapter);
    	initialize(config.getProperty("up2p.gnutella.incoming", "6346"),
    			Boolean.parseBoolean(config.getProperty("up2p.gnutella.peerdiscovery", "false")));
    	initializeHostCache();
    	messageTable = new HashMap<String,SearchMessage>();
    	peerIdToClientGuid = new HashMap<String,GUID>();
    	openSearchSessions = new ArrayList<Thread>();
    	requestedResourceLists = new HashMap<String, List<String>>();
    	hostedResListCache = new HashMap<String, List<String>>();
    	duplicateQueryIds = new HashMap<String, String>();
    	LOG.info("JTella adapter initialized.");
    }
    
    /*
     * @see up2p.core.NetworkAdapter#setProperty(java.lang.String,
     * java.lang.String)
     */
    public void setProperty(String propertyName, String propertyValue) {
        LOG.info("setProperty method was called in JTellaAdapter.");
        
    }
    
    /**
     * Initializes the adapter
     * @param incomingPort	The port to listen for Gnutella connections on.
     * 						Uses the default value if null is passed.
     * @param peerDiscovery	Determines whether automatic peer discovery will be enabled
     */
    private void initialize(String incomingPort, boolean peerDiscovery) {
		try {
			ConnectionData connData = new ConnectionData();
			connData.setIncommingConnectionCount(INCOMING_CONNECTIONS_WANTED);
			connData.setOutgoingConnectionCount(OUTGOING_CONNECTIONS_WANTED);
			connData.setUltrapeer(true);
			connData.setIncomingPort(Integer.valueOf(DEFAULT_LOCAL_PORT).intValue());
			connData.setAgentHeader("up2p");
			connData.setUrlPrefix(getUrlPrefix());
			connData.setFileTransferPort(adapter.getPort());
			connData.setPeerLookupEnabled(peerDiscovery);
			
			// Note: The incoming port assignment here explicitly replaces the default value
			// ("6346") used in ConnectionData, and changing the constant in ConnectionData
			// will have no effect on U-P2P. To change the incoming port, change the value of
			// "up2p.gnutella.incoming" in webAdapter.properties (located at
			// "up2p\WEB-INF\classes\\up2p\core")
			if(incomingPort == null) {
				incomingPort = DEFAULT_LOCAL_PORT;
			}
			try {
				connData.setIncomingPort(Integer.parseInt(incomingPort));
			} catch (NumberFormatException e) {
				LOG.error("Invalid Gnutella port specified (\"" + incomingPort + "\"), using default.");
				incomingPort = DEFAULT_LOCAL_PORT;
				connData.setIncomingPort(Integer.parseInt(incomingPort));
			}
			LOG.info("Listening for incoming connections on port: " + incomingPort);
			LOG.info("Peer discovery enabled: " + peerDiscovery);
			System.out.println("Incoming Gnutella port: " + incomingPort);
			
			c = new StracciatellaConnection(connData);
			c.getSearchMonitorSession(this);
			c.createFileServerSession(this);
			c.addListener(this);
			
						
			LOG.info("JTellaAdapter:: init: about to start the GnutellaConnection" );
			c.start();
			LOG.info("JTellaAdapter:: init: GnutellaConnection started" );
			LOG.info("JTellaAdapter::  Servent identifier GUID: " + StracciatellaConnection.getServentIdentifier());
		} 
		catch(NumberFormatException e) {
			LOG.debug("NumberFormatException while initializing JTella adapter: " + e.getMessage());	
		}
    	catch (UnknownHostException e) {
			LOG.error("UnknownHostException while initializing JTellaAdapter: " + e.getMessage());
		} 
		catch (IOException e) {
			LOG.error("IOException while initializing JTellaAdapter: " + e.getMessage());
		}
    }
    
    /**
     * Initializes the Host Cache
     */
    private void initializeHostCache() {
    	LOG.info("== Initializing Host Cache ==");
    	
    	hostCacheParser = new HostCacheParser(Core2Network.getRootPath()
    			+ File.separator + HOST_CACHE_FILE);
    	//includes initialization of HC from parsing file
    	
    	LOG.info("== Finished initializing Host Cache ==");
    }
    
    
    
	public boolean isAsynchronous() {
		return true;
	}

    /**
     * Performs a search on the GNutella network and returns the results
     * TODO: deprecated: I just leave it to avoid having to clean up properly; tbd: remove and clean up
     */
	public SearchResponse[] searchNetwork(String communityId, SearchQuery query, long maxTimeout) 
		throws NetworkAdapterException {
        return null;
	}

	/**
     * Performs a search on the GNutella network and returns the results
     */
	public void searchNetwork(String communityId, String query, String queryId) {
		LOG.info("User requesting to search the network. Query:: " + query);
		
		// Just to make sure that we're not outputting a new search that's from the network 
		if (messageTable.containsKey(queryId)){
			LOG.error("JTELLA Adapter error: outputting a search that was already from the network! [caught]");
			return;
		}
		
		// Form the search request using provided query id
        SearchRequestMessage request = new SearchRequestMessage(queryId, communityId, query);
        
        // -Do the search-
		StringWriter sw = new StringWriter();
		Node requestXML = request.serialize(); //Serializing the node
        try {
			TransformerHelper.encodedTransform((Element) requestXML, "UTF-8", sw, true); //Applying the transformation
		} 
        catch (IOException e) {
			System.out.println("Failed sending up2p request.");
			e.printStackTrace();
		}
        
        final String searchCriteria = sw.toString().trim();
        final String subNetId = communityId;
        
        // Create a new thread to generate and terminate the search session after a specified timeout
        Thread searchSession = new Thread(new Runnable() {
			@Override
			public void run() {
				SearchSession search = null;
		        if(c instanceof SubnetStracciatellaConnection) {
		        	search = ((SubnetStracciatellaConnection)c).createSubNetSearchSession(searchCriteria, 0, subNetId, 10, 0, JTellaAdapter.this);
		        } else {
		        	search = c.createSearchSession(searchCriteria, 0, 10, 0, JTellaAdapter.this);
		        }
		        LOG.debug("Search initiated: Hash - " + search.hashCode());
				
				try {
					Thread.sleep(SEARCH_TIMEOUT_MILLIS);
				} catch (InterruptedException e) {
					LOG.error("Search session timeout thread interrupted, search session closing prematurely.");
				}
				LOG.debug("Search closed: Hash - " + search.hashCode());
				search.close();
				
				synchronized(openSearchSessions) {
		        	openSearchSessions.remove(this);
		        }
			}
        });
        
        synchronized(openSearchSessions) {
        	openSearchSessions.add(searchSession);
        }
        searchSession.start();
        
		LOG.info("JTellaAdapter:exiting search method");        
	}

	public void shutdown() {
		//TODO - is there anything else to the shutdown sequence?
		LOG.info("Stopping Gnutella connection");
		//save hostcache;
		hostCacheParser.saveHostCache(); //save hostcache to file
		
		synchronized(openSearchSessions) {
			for(Thread session : openSearchSessions) {
				session.interrupt();
			}
        }
		c.stop();
	}

	/**
	 * Handles incoming search requests from the network.
	 * 
	 * New: Multiple requests with the same query id are now serviced if the query
	 * is deemed valid (see below), and all tuples are collected as part of a single result set.
	 * 
	 * A never before seen query id is automatically considered to be valid. If the query.
	 * id has already been serviced than the new request must have the same originating connection, and
	 * must have different search criteria than the last request serviced.
	 */
	public void receiveSearch(SearchMessage message) {
		LOG.info("Received a search message");
		
		// Parse u-p2p message
		String xmlMessage = message.getSearchCriteria().trim();
		
		try {
			// Parse the XML into a SearchRequestMessage
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = db.parse(new ByteArrayInputStream(xmlMessage.getBytes("UTF-8")));
			SearchRequestMessage searchMessage = SearchRequestMessage.parse(document.getDocumentElement());
			
			// Check to ensure the request is valid
			// TODO: The criteria should probably be checked against all messages with the same query id instead
			//             of just the most recent	
			boolean validQuery = false;
			String queryId = searchMessage.getId();
			
			if (!messageTable.containsKey(searchMessage.getId())) {
				
				// The message has a new query id, automatically valid
				LOG.info("New unique query id: "+ searchMessage.getId()+" received, processing request." );
				messageTable.put(searchMessage.getId(), message);
				validQuery = true;
				
			} else {
				
				// Query id has been seen before, need to further check validity
				SearchMessage lastMessage = messageTable.get(searchMessage.getId());
				
				if(lastMessage.getOriginatingConnection() == message.getOriginatingConnection() &&
						!lastMessage.getSearchCriteria().trim().equals(xmlMessage)) {
					
					// Message has the same originating connection and new criteria, handle it
					LOG.info("New search criteria for query id: "+ searchMessage.getId()+" received, processing request." );
					messageTable.remove(searchMessage.getId());
					messageTable.put(searchMessage.getId(), message);
					
					// Generate a new random query ID to use for the local search (prevents old
					// results from being returned)
					queryId = Long.toString((Integer.parseInt(searchMessage.getId()) % 10000) + (new Date()).getTime());
					synchronized(duplicateQueryIds) {
						duplicateQueryIds.put(queryId, searchMessage.getId());
					}
					validQuery = true;
					
				} else {
		
					// Message criteria is identical to the previous request, ignore it
					LOG.info("Query id: "+ searchMessage.getId()+" received duplicate search criteria, ignoring request." );
					
				}
			}
			
			if (validQuery) {
				// Send query to Core2Network adapter
				LOG.info("Launching search with criteria:\n\t" + xmlMessage);
				adapter.searchLocal(searchMessage.getCommunityId(), searchMessage.getQuery(), queryId);
			}else {
				// Otherwise just ignore the query, we've already had it (the exact same query id and search criteria)
				LOG.info("Ignoring search message with id: "+ searchMessage.getId());
			}
		}
		catch (Exception e)
		{}
			
			
	}

	/**
	 *  implementation of ReceiveSearchReply in the JTella interface: receives responses from the network [QUERYHIT messages].
	 *  The message is parsed and the contents sent to the tuplespace
	 *  @param searchReplyMessage: the JTella message received from the network
	 */
	public void receiveSearchReply(SearchReplyMessage searchReplyMessage) {
		LOG.info("Received a search reply message from " + searchReplyMessage.getOriginatingConnection().getConnectedServentIdAsString());
		
		//Parse u-p2p message
		String xmlMessage = searchReplyMessage.getXmlBlock();
		if(xmlMessage == null) {
			// If the XML block was empty, the sender is probably 
			// a node using the old QUERYHIT format
			xmlMessage = searchReplyMessage.getFileRecord(0).getName().trim();
		}
		
		LOG.info("xmlMessage of the search reply: " + xmlMessage);
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = db.parse(new ByteArrayInputStream(xmlMessage.getBytes("UTF-8")));
			SearchResponseMessage responseMessage = SearchResponseMessage.parse(document.getDocumentElement());
			
			// Determine the peerIdentifier of the remote peer by using the JTella level IP and port
			String peerIdentifier = searchReplyMessage.getIPAddress() + ":" + searchReplyMessage.getPort() + "/"
				+ responseMessage.getUrlPrefix();
			
			// Add the mapping of peer IP/Port to client identifier, and remove any
			// previous mapping
			if(peerIdToClientGuid.containsKey(peerIdentifier)) {
				peerIdToClientGuid.remove(peerIdentifier);
			}
			peerIdToClientGuid.put(peerIdentifier, searchReplyMessage.getClientIdentifier());
			LOG.debug("JTellaAdapter: Mapped \"" + peerIdentifier  + "\" to GUID: " 
					+ searchReplyMessage.getClientIdentifier().toString());
			
			if(responseMessage.getHostedResIdList() != null) {
				fireNetworkResourceList(peerIdentifier, responseMessage.getCommunityId(), responseMessage.getHostedResIdList());
				LOG.debug("Fired network resource list.");
			}
			
			if (responseMessage.getResultSetSize() > 0) {
				SearchResponse[] resultSet = responseMessage.getResponses();
				for(SearchResponse response : resultSet) {
					// Rebuild a location entry for each response using the
					// IP / Port determined at the JTella level
					LocationEntry[] locations = new LocationEntry[1];
					locations[0] = new LocationEntry(searchReplyMessage.getIPAddress(),
							searchReplyMessage.getPort(), responseMessage.getUrlPrefix());
					response.setLocations(locations);
				}
				
				fireTrustMetric(peerIdentifier, responseMessage.getCommunityId(), "Network Distance", 
						Integer.toString(searchReplyMessage.getHops() + 1));
				
				// Fire any other included trust metrics
				List<String> metricNames = responseMessage.getMetricNames();
				if(metricNames != null) {
					for(String metricName : metricNames) {
						fireTrustMetric(peerIdentifier, responseMessage.getCommunityId(), metricName, 
								responseMessage.getTrustMetric(metricName));
					}
				}
				
				fireSearchResponse(resultSet);
				LOG.debug("Fired search responses.");
			}
			
		} 
		catch (ParserConfigurationException e) {
			LOG.error("ParserConfigurationException in JTellaSearchMonitor: " + e.getMessage());
		}
		catch (SAXException e) {
			LOG.error("SAXException in JTellaSearchMonitor: " + e.getMessage());
		} 
		catch (IOException e) {
			LOG.error("IOException in JTellaSearchMonitor: " + e.getMessage());
		} 
		catch (MalformedPeerMessageException e) {
			LOG.error("MalformedPeerMessageException in JTella Adapter: " + e.getMessage());
			LOG.error(e.getStackTrace().toString());
		}
	}

	/**
	 * Handles an incoming PUSH message. Initiates a connection to the remote nodes
	 * PushServlet, and receives transfer requests. Continually requests and pushes
	 * file transfers until an "OK" handshake is received.
	 */
	public void receivePush(PushMessage pushMsg) {
		// TODO: Make a new thread to handle the transfers? Test if doing this
		// in the same thread affects usual message routing.
		
		LOG.debug("JTellaAdapter: Recieved PUSH message from: " 
				+ pushMsg.getIPAddress() + ":" + pushMsg.getPort() + " Url Prefix: " + pushMsg.getUrlPrefix());
		
		String localIp;
		try {
			localIp = InetAddress.getByName(pushMsg.getOriginatingConnection().getPublicIP()).getHostAddress();
		} catch (UnknownHostException e) {
			localIp = pushMsg.getOriginatingConnection().getPublicIP();
		}
		
		String urlString = "http://" + pushMsg.getIPAddress() + ":" + pushMsg.getPort() + "/" 
			+ pushMsg.getUrlPrefix() + "/push?" + HttpParams.UP2P_PEERID + "=" 
			+ localIp + ":" + adapter.getPort()
			+ "/" + getUrlPrefix();
		
		
		LOG.debug("JTellaAdapter: Initiating HTTP connection to: " + urlString);
		
		HttpURLConnection pushConn = null;
		
		try {
			// open a URL connection to the Servlet
			URL url = new URL(urlString);
			
			boolean pushComplete = false;
			
			while(!pushComplete) {
				// Open a HTTP connection to the URL
				pushConn = (HttpURLConnection) url.openConnection();
				pushConn.setDoInput(true);
				pushConn.setDoOutput(true);
				pushConn.setUseCaches(false);
				pushConn.setRequestMethod("GET");
				
				// Set required request headers
				pushConn.setRequestProperty("Connection", "Keep-Alive");
				pushConn.setRequestProperty("User-Agent","UP2P");
				pushConn.setRequestProperty("Accept","[star]/[star]");
				
				// Read the response from the server
				// This should be either "OK" to signal all transfers are complete, or
				// "GIV comId/resId" to specify a resource to transfer.
	
				BufferedReader inStream = new BufferedReader(new InputStreamReader(pushConn.getInputStream()));
				String serverResponse = inStream.readLine();
				inStream.close();
				LOG.debug("JTellaAdapter: Received from PUSH servlet: " + serverResponse);
				
				if (serverResponse.startsWith("OK")) {
					// If the OK handshake is received, terminate the connection
					LOG.debug("JTellaAdapter: PUSH transfers complete.");
					pushComplete = true;
					
				} else if(serverResponse.startsWith("GIV")) {
					// A transfer request was received, get the community and resource ID
					serverResponse = serverResponse.substring(serverResponse.indexOf(" ") + 1);
					String[] splitResponse = serverResponse.split("/");
					LOG.debug("JTellaAdapter: Got PUSH request for ComId: " + splitResponse[0] + "   ResId: " + splitResponse[1]);
					
					try {
						// Fetch the file paths for the requested resource, and initiate a file transfer
						List<String> filePathList = adapter.lookupFilepaths(splitResponse[0], splitResponse[1]);
						String resourceFilePath = filePathList.remove(0);
						/*pushResource(pushMsg.getIPAddress() + ":" + pushMsg.getPort() + "/" + pushMsg.getUrlPrefix(), 
								splitResponse[0],
								resourceFilePath, filePathList);*/
						//TODO: now using the REST service rather than the upload servlet, which may be blocked for security reasons and handles multipart uploads better
						pushResource2REST(pushMsg.getIPAddress() + ":" + pushMsg.getPort() + "/" + pushMsg.getUrlPrefix(), 
								splitResponse[0],
								resourceFilePath, filePathList);
								
						
					} catch (ResourceNotFoundException e) {
						LOG.error("JTellaAdapter: Could not find resource specified by PUSH message: " +
								splitResponse[0] + "/" + splitResponse[1]);
					}
				}
				
				// Close the connection once the "OK" handshake is received
				pushConn.disconnect();
			}
			
		} catch (IOException e) {
			LOG.error("JTellaAdapter: PUSH file transfer failed: " + e.getMessage());
			
			
			if(!this.relayPeerUrl.equals("")) {
				issueRelayMessage(pushMsg);
			}
		}
	}
	
	/**
	 * Handles an incoming RELAY message. Notifies the tuple space of the relay identifier
	 * and the relay URL so that download requests can be initiated.
	 */
	public void receiveRelay(RelayMessage relayMsg) {
		LOG.info("Got RelayMessage:\nTarget Identifier: " + relayMsg.getTargetIdentifier()
				+ "\nRelay Identifier: " + relayMsg.getRelayIdentifier() + "\nPeer URL: " + relayMsg.getRelayUrl()
				+ "\nSource URL: " + relayMsg.getSourcePeerId());
		
		// Generate a tuple to notify the tuple space of the received message
		adapter.notifyRelayReceived(relayMsg.getSourcePeerId(), relayMsg.getRelayUrl(), relayMsg.getRelayIdentifier());
	}
	
	/**
	 * Send a PUSH message to the specified remote node.
	 * @param peerId	The peer ID of the remote node (IP:port/urlPrefix)
	 */
	public void issuePushMessage(String peerId) {
		LOG.debug("JTellaAdapter: PUSH requested to remote node: " + peerId);
		// Get the GUID of the remote node (cached when the query hit was received),
		// and generate a PUSH message
		if(peerIdToClientGuid.get(peerId) == null) {
			LOG.error("JTellaAdapter: PUSH message requested for unknown peer ID: " 
					+ peerId);
			return;
		}
		
		GUID clientIdentifier = peerIdToClientGuid.get(peerId);
		LOG.debug("JTellaAdapter: Issuing PUSH with advertised address: "
				+ c.getRouter().getQueryHitSource(clientIdentifier).getPublicIP() + ":"
				+ adapter.getPort() + "/" + getUrlPrefix());
		PushMessage pushRequest = new PushMessage(clientIdentifier, 0,
				c.getRouter().getQueryHitSource(clientIdentifier).getPublicIP(), 
				(short)adapter.getPort(), getUrlPrefix());
		
		try {
			// Get the originating connection of the query hit, and send the PUSH message
			Connection clientConnection = c.getRouter().getQueryHitSource(clientIdentifier);
			clientConnection.send(pushRequest);
			LOG.debug("JTellaAdapter: PUSH message sent to client: " + peerId 
					+ " (" + clientIdentifier.toString() + ")");
			LOG.debug("JTellaAdapter: PUSH sent on connection: " + clientConnection.getListenString());
		} catch (IOException e) {
			LOG.error("JTellaAdapter: Error sending PUSH message.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Issues a relay message in response to the specified push message (this should
	 * only be called when a push file transfer fails)
	 * @param pushMsg	The push message which initiated the failed transfer.
	 */
	private void issueRelayMessage(PushMessage pushMsg) {
		Random rand = new Random();
		int relayId = rand.nextInt(Integer.MAX_VALUE - 1) + 1; // Generate only positive values
		LOG.info("JTellaAdapter: Sending a RELAY message (Relay ID: " + relayId + ").");
		
		// Register the generated relay ID with the specified relay peer
		String sourceIp = pushMsg.getOriginatingConnection().getPublicIP();
		try {
			sourceIp = InetAddress.getByName(sourceIp).getHostAddress();
		} catch (UnknownHostException e2) {
			// Just use the hostname if the IP can't be determined
		}
		String sourceUrl = sourceIp + ":" + adapter.getPort() + "/" + adapter.getUrlPrefix();
		String relayRegistration;
		try {
			relayRegistration = "http://" + this.relayPeerUrl + "/relay?up2p:registerrelay="
				+ URIUtil.encodeQuery(sourceUrl, "UTF-8") + "&up2p:relayidentifier=" + relayId;
		} catch (URIException e1) {
			// Will only ever be thrown if UTF-8 is unavailable... highly unlikely to say the least
			relayRegistration = "http://" + this.relayPeerUrl + "/relay?up2p:registerrelay="
			+ sourceUrl + "&up2p:relayidentifier=" + relayId;
		}
		LOG.info("JTellaAdapter: Registering relay pair with URL: " + relayRegistration);
		
		boolean relayRegSuccessful = false;

		
		HttpURLConnection relayRegConn = null;
		try {
			URL relayRegUrl = new URL(relayRegistration);
			relayRegConn = (HttpURLConnection) relayRegUrl.openConnection();
			relayRegConn.setRequestMethod("GET");
			relayRegConn.setRequestProperty("User-Agent","UP2P");
			relayRegConn.setRequestProperty("Accept","[star]/[star]");
			relayRegConn.connect();
			if(relayRegConn.getResponseCode() == 200) {
				LOG.info("JTellaAdapter: Relay peer registration was successful.");
				relayRegSuccessful = true;
			}
			relayRegConn.disconnect();
		} catch (IOException e2) {
			LOG.error("JTellaAdapter: Relay peer registration failed, abandoning relay attempt.");
		}
		
		if(relayRegSuccessful) {
			// Generate the RelayMessage
			RelayMessage relayMessage = new RelayMessage(pushMsg, relayId,
					this.relayPeerUrl, 
					pushMsg.getOriginatingConnection().getPublicIP(), adapter.getPort(), adapter.getUrlPrefix());
			
			try {
				// Get the originating connection of the query hit, and send the PUSH message
				Connection clientConnection = c.getRouter().getPushSource(pushMsg.getSourceIdentifier());
				LOG.debug("JTellaAdapter: Got client connection for RELAY: " + clientConnection.getListenString());
				clientConnection.send(relayMessage);
				LOG.debug("JTellaAdapter: RELAY message sent to client: " + relayMessage.getTargetIdentifier());
			} catch (IOException ex) {
				LOG.error("JTellaAdapter: Error sending RELAY message.");
				ex.printStackTrace();
			}
		}
	}
	
    //used by network-status.jsp
    public static StracciatellaConnection getConnection() {
    	return c;
    }
    
    /**
     * @return	The url prefix that should be attached to outgoing SearchResponse
     * 			and Push messages.
     */
    public String getUrlPrefix() {
    	return urlPrefix;
    }

    /*
     * @see up2p.core.NetworkAdapter#publish(up2p.repository.ResourceEntry,
     * boolean)
     */
    public void publish(ResourceEntry resourceEntry, boolean buffer) throws NetworkAdapterException {
        // published files are not advertised on the network
    }

    /*
     * @see up2p.core.NetworkAdapter#publishAll(up2p.repository.ResourceList)
     */
    public void publishAll(ResourceList resourceList) throws NetworkAdapterException {
        // published files are not advertised on the network
    }

    /*
     * @see up2p.core.NetworkAdapter#publishFlush()
     */
    public void publishFlush() throws NetworkAdapterException {
        // published files are not advertised on the network
    }

    /*
     * @see up2p.core.NetworkAdapter#remove(up2p.repository.ResourceEntry)
     */
    public void remove(ResourceEntry resourceEntry) throws NetworkAdapterException {
        // published files are not advertised on the network
    }

    /*
     * @see up2p.core.NetworkAdapter#removeAll(up2p.repository.ResourceList)
     */
    public void removeAll(ResourceList resourceList) throws NetworkAdapterException {
        // published files are not advertised on the network
    }

    /**
     * build a list of searchresponse messages (in case we need to break up a large list)
     * from a set of search results
     * @param results
     * @return
     */
    private SearchResponseMessage buildResponseMessages(SearchResponse [] results){
    	//get some important identifiers from the response (they're common to every response in array)
		String communityId= results[0].getCommunityId();
		String qid = results[0].getQueryId();
		
		//TODO handle case where the are several communities in the response
		//TODO: include some XML metadata in the message, and produce several messages limited to 65kb each (!).
		
		// Note: Assumes the IP/Port of the first location for the first result is the local IP/Port (should always
		// be true as this function is only used to generate response messages from local repository
		// results)
		SearchResponseMessage responseMessage = new SearchResponseMessage(qid, communityId, getUrlPrefix());

		// add results to the response
		int resultCount = 0;
		if (results.length > 0) {
			for (SearchResponse r: results) {
				responseMessage.addResult(r);
				resultCount++;
			}
		}

		LOG.info(" -Search returning " + resultCount
				+ " search results for query in community " + communityId);
		return responseMessage;
    }
    
    /**
     * Receives local search responses from the repository, and outputs
     * search response messages to the network.
     */
	public void receiveSearchResponse(SearchResponse[] results) {
		
		LOG.debug(" -Local repository returned " + results.length + " results.");
	
		StringWriter sw2 = new StringWriter();
		
		// Form a search response
		SearchResponseMessage theresponse = buildResponseMessages(results);
		
		// Check to see if the query ID needs to be replaced before sending the results
		synchronized(duplicateQueryIds) {
			if(duplicateQueryIds.get(theresponse.getId()) != null) {
				theresponse.setId(duplicateQueryIds.remove(theresponse.getId()));
			}
		}
		
		// Check to see if a resource list for the specified community has already
		// been returned and cached. If not, record the query ID so the hosted 
		// resource list can be routed back once it has been asynchronously fetched
		synchronized(hostedResListCache) {
			if(hostedResListCache.get(theresponse.getCommunityId()) != null) {
				theresponse.setHostedResIdList(hostedResListCache.remove(theresponse.getCommunityId()));
				LOG.debug("JTellaAdapter: Found cached resource list for community: " + theresponse.getCommunityId());
			} else {
				synchronized(requestedResourceLists) {
					if(requestedResourceLists.get(theresponse.getCommunityId()) == null) {
						ArrayList<String> idList = new ArrayList<String>();
						idList.add(theresponse.getId());
						requestedResourceLists.put(theresponse.getCommunityId(), idList);
					} else {
						requestedResourceLists.get(theresponse.getCommunityId()).add(theresponse.getId());
					}
				}
				LOG.debug("JTellaAdapter: Added mapping: " + theresponse.getCommunityId() +
						" -> " + theresponse.getId() + " to requested resource lists.");
				adapter.fetchHostedResourceList(theresponse.getCommunityId(), theresponse.getId());
			}
		}
		
		
		// Set the number of network neighbours
		int neighbours = 0;
		for(Connection connection : c.getActiveConnections()) {
			if (connection.isIncoming() ){
				neighbours++;
			}
		}
		theresponse.addTrustMetric("Network Neighbours", Integer.toString(neighbours));
		
		// Note: Splitting the message will return the original message wrapped in
		// an array if it is already small enough.
		List<SearchResponseMessage> messagesplit = theresponse.split();
		
		// Restart the procedure with each smaller message
		for (SearchResponseMessage responseMessage : messagesplit){
			
			responseMessage.addTrustMetric("Network Neighbours", Integer.toString(neighbours));
			StringWriter sw = new StringWriter();
			Node requestXML = responseMessage.serialize();

			try {
				TransformerHelper.encodedTransform((Element) requestXML, "UTF-8", sw, true); 
			} catch (IOException e) {
				LOG.error("JTella Adapter:"+e);
				e.printStackTrace();
			}
			
			//get the queryId 
			String qid = responseMessage.getId();
			//output the message
			sendSearchReplyMessage(sw.toString(), qid);
		}
	}

	private void sendSearchReplyMessage(String xmlmsg, String qid){
		LOG.info(" -Sending the search response");
		LOG.debug("content="+xmlmsg);
		
		//get the message of the original query
		LOG.debug("getting original msg with id:"+qid);
		SearchMessage message = messageTable.get(qid) ;

		//TODO: remove messages from the table, periodically.
		//idea: I could make a queue and drop the oldest ones when I reach a size limit
		SearchReplyMessage replyMessage;
		//sometimes the local IP address hasn't been set.

		if (message != null
				&& message.getOriginatingConnection().getPublicIP() != null)
		{
			replyMessage= new SearchReplyMessage(message, (short)adapter.getPort(), 
					message.getOriginatingConnection().getPublicIP(), 0);
			replyMessage.addCompressedXmlBlock(xmlmsg);

			try {
				// output the response on the Gnutella connection where the search came from
				message.getOriginatingConnection().send(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOG.debug("Using public IP address: "
					+ message.getOriginatingConnection().getPublicIP());
			LOG.info("JTellaAdapter: Finished sending search response");
		} else if (message==null) {
			LOG.error("Error: didn't find original message to reply to...");
			LOG.debug("The remembered messages are: ");
			Iterator<String> iter = messageTable.keySet().iterator();
			while(iter.hasNext()){
				LOG.debug("Query identifier: "+ iter.next());	
			}
		} else {
			// In the case we don't know the local IP address, we cannot provide a URL to the resources.
			LOG.error("Error: unknown local IP address. Ignoring query response.");
		}
	}
	
	/**
	 * Updates the set of sub-network ID's that this node should
	 * serve (if sub-network support is enabled)
	 * @param subNetIds	A set of U-P2P community IDs (also used as sub-network IDs)
	 */
	public void updateSubnets(Set<String> subNetIds) {
		if(c instanceof SubnetStracciatellaConnection) {
			((SubNetRouter)((SubnetStracciatellaConnection)c).getRouter()).setServedSubNets(subNetIds);
		}
	}
	
	/**
	 * pushes a resource to the UP2P REST service
	 * @throws IOException 
	 */
	private void pushResource2REST(String peerId, String communityId, String resourceFilePath, 
	List<String> attachmentFilePaths) throws IOException{
		Client wrclient = Client.create(); 
		//URL for UP2P RESTful service
		String urlString = "http://" + peerId + "/rest/up2p/";
		//url for the particular community
		WebResource service = wrclient.resource(urlString).path("community").path(communityId);
		//"publish" action is POST
	    //String xmldoc = "<?xml version=\"1.0\"?><up2p><doc>file:heulaooo.pdf</doc><doc>file:maya.jpg</doc></up2p>";
		ClientResponse response = null;
	    if (attachmentFilePaths.size()>0){

	    	// Construct a MultiPart with the different elements
	    MultiPart multiPart = new MultiPart().
	    		//first the XML part
	    bodyPart(new BodyPart(new FileInputStream(new File(resourceFilePath)), MediaType.TEXT_XML_TYPE)); //TODO: testing: can we get the file and send as text/xml?
	    //add all attachments
	    for (String attach: attachmentFilePaths)
	    	    multiPart = multiPart.bodyPart(new BodyPart(new FileInputStream(new File(attach)), MediaType.APPLICATION_OCTET_STREAM_TYPE));
	    	    
	    	// POST the request
	    	 response = service.type("multipart/mixed").post(ClientResponse.class, multiPart);
	    } else { //no multipart needed, just send resource as text/xml
	    	response = service.entity( new FileInputStream(new File(resourceFilePath)), MediaType.TEXT_XML_TYPE).post(ClientResponse.class);
	    }
	    if(!response.getClientResponseStatus().equals(ClientResponse.Status.CREATED)){
	    	//error 
	    	LOG.info("PUSHresource2REST = failed");
	    	//return relay request: taken care of by caller method on IO exception
	    	throw new IOException("push failed");
	    }
	  }
	
	
	
	/**
	 * Pushes a resource to a remote peer by connection to upload servlet and emulating
	 * a web browser upload request.
	 * @param peerId	The IP:port/urlPrefix of the remote peer to upload to
	 * @param communityId	The community Id of the resource being uploaded
	 * @param resourceFilePath	The absolute file path to the resource file
	 * @param attachmentFilePaths	A list of absolute file paths to any required resource files
	 */
	private void pushResource(String peerId, String communityId, String resourceFilePath, 
			List<String> attachmentFilePaths) throws IOException {
		String urlString = "http://" + peerId + "/upload";
		HttpURLConnection uploadConnection = null;
		DataOutputStream connOutput = null;
		FileInputStream fileInput = null;

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		
		// Arbitrary boundary, might want to make sure it doesn't
		// appear in file contents
		String boundary = "232404jkg4220957934FW";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		try {
			
			// First, ensure all files exist and cancel if they do not
			File resourceFile = new File(resourceFilePath);
			if(!resourceFile.exists()) {
				LOG.error("JTellaAdapter: Resource file could not be found for push: "
						+ resourceFilePath);
				return;
			}
			List<File> attachments = new ArrayList<File>();
			for(String attachmentPath : attachmentFilePaths) {
				File attachFile = new File(attachmentPath);
				if(!attachFile.exists()) {
					LOG.error("JTellaAdapter: Attachment file could not be found for push: "
							+ attachmentPath);
					return;
				}
				attachments.add(attachFile);
			}
			
			LOG.debug("JTellaAdapter: Initiating push to: " + urlString);
			
			// open a URL connection to the Servlet
			URL url = new URL(urlString);

			// Open a HTTP connection to the URL
			uploadConnection = (HttpURLConnection) url.openConnection();
			uploadConnection.setDoInput(true);
			uploadConnection.setDoOutput(true);
			uploadConnection.setUseCaches(false);

			// Set the content type to multipart form data and the method to
			// POST to emulate a browser upload request
			uploadConnection.setRequestMethod("POST");
			uploadConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			
			// Set required request headers
			uploadConnection.setRequestProperty("Connection", "Keep-Alive");
			uploadConnection.setRequestProperty("User-Agent","UP2P");
			uploadConnection.setRequestProperty("Accept","[star]/[star]");

			// Open the file output stream and write the community ID and PUSH message parameters
			connOutput = new DataOutputStream(uploadConnection.getOutputStream());
			connOutput.writeBytes(twoHyphens + boundary + lineEnd);
			connOutput.writeBytes("Content-Disposition: form-data; name=\"up2p:community\"" 
					+ lineEnd + lineEnd);
			connOutput.writeBytes(communityId + lineEnd);
			connOutput.writeBytes(twoHyphens + boundary + lineEnd);
			connOutput.writeBytes("Content-Disposition: form-data; name=\"up2p:pushupload\"" 
					+ lineEnd + lineEnd + "true" + lineEnd);
			connOutput.writeBytes(twoHyphens + boundary + lineEnd);
			
			boolean fileWriteComplete = false;
			boolean resourceFileWritten = false;
			File nextFile = null;

			while(!fileWriteComplete) {
				// Determine the next file to be written
				if(!resourceFileWritten) {
					nextFile = resourceFile;
				} else {
					nextFile = attachments.remove(0);
				}
				
				LOG.debug("JTellaAdapter: PUSHing file: " + nextFile.getAbsolutePath());
				
				// Write the file metadata
				connOutput.writeBytes("Content-Disposition: form-data; name=\"up2p:filename\";"
						+ " filename=\"" + nextFile.getName() + "\"" + lineEnd);
				connOutput.writeBytes(lineEnd);
				
				// Open an input stream to the file and allocate a buffer to hold
				// the file contents
				fileInput = new FileInputStream(nextFile);
				bytesAvailable = fileInput.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
	
				// Read the file and write it to the Http connection
				bytesRead = fileInput.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					connOutput.write(buffer, 0, bufferSize);
					bytesAvailable = fileInput.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInput.read(buffer, 0, bufferSize);
				}
	
				// Send the multipart boundary to indicate the end of the resource file
				connOutput.writeBytes(lineEnd);
				if(attachments.isEmpty()) {
					connOutput.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				} else {
					connOutput.writeBytes(twoHyphens + boundary + lineEnd);
				}
				
				// Set the resource file written flag to true, and finish writing
				// files if the attachment list is empty
				resourceFileWritten = true;
				if(attachments.isEmpty()) { fileWriteComplete = true; }
			}
			
			// Read the server response to ensure the transfer was received
			BufferedReader inStream = new BufferedReader(new InputStreamReader(uploadConnection.getInputStream()));
			while(inStream.readLine() != null);
			inStream.close();		
			
			LOG.debug("JTellaAdapter: Push upload was succesful.");
	
		} catch (MalformedURLException ex) {
			LOG.error("JTellaAdapter: pushResource Malformed URL: " + ex);
			throw new IOException("pushResource failed for URL: " + urlString);
		} catch (IOException ioe) {
			LOG.error("JTellaAdapter: pushResource IOException: " + ioe);
			throw new IOException("pushResource failed for URL: " + urlString);
		} finally {
			// Close streams
			try {
				if(fileInput != null) { fileInput.close(); }
				if(connOutput != null) { connOutput.flush(); }
				if(connOutput != null) { connOutput.close(); }
				if(uploadConnection != null) { uploadConnection.disconnect(); }
			} catch (IOException e) {
				LOG.error("JTellaAdapter: pushResource failed to close connection streams.");
			}
		}
	}
	
	/**
	 * Distributes a list of hosted resources for the specified community to
	 * in response to any search reply messages that have yet to be serviced.
	 * @param communityId	Community ID of all listed resources
	 * @param resourceIds	A list of all resource IDs served in the specified community
	 */
	public void updateResourceList(String queryId, String communityId, List<String> resourceIds) {
		LOG.debug("JTellaAdapter: Got resource list for community: " + communityId);
		
		synchronized(hostedResListCache) {
			hostedResListCache.put(communityId, resourceIds);
		}
		
		List<String> unservicedQueries = null;
		synchronized(requestedResourceLists) {
			if(requestedResourceLists.get(communityId) == null) {
				LOG.debug("JTellaAdapter: No outstanding requests for this resource list.");
				return;
			} else {
				unservicedQueries = requestedResourceLists.remove(communityId);
			}
		}
		
		LOG.debug("JTellaAdapter: Sending resource list in response to " + unservicedQueries.size()
				+ " unserviced search reply messages.");
		for(String oldQueryId : unservicedQueries) {			
			// Build a SearchResponseMessage to carry the hosted resource list
			SearchResponseMessage responseMessage = new SearchResponseMessage(oldQueryId,
					communityId, getUrlPrefix());
			responseMessage.setHostedResIdList(resourceIds);
			
			// Serialize the XML of the response
			StringWriter sw = new StringWriter();
			Node requestXML = responseMessage.serialize();
			try {
				TransformerHelper.encodedTransform((Element) requestXML, "UTF-8", sw, true); 
			} catch (IOException e) {
				LOG.error("JTella Adapter:"+e);
				e.printStackTrace();
			}

			// Send the response
			sendSearchReplyMessage(sw.toString(), oldQueryId);
		}
	}
	
	/**
	 * Removes the list of cached resources for a particular community.
	 * @param communityId	The community to clear cached resources for.
	 */
	public void invalidateCachedResourceList(String communityId) {
		LOG.debug("JTellaAdapter: Invalidating cached resource list for community: " 
				+ communityId);
		synchronized(hostedResListCache) {
			hostedResListCache.remove(communityId);
		}
	}

	@Override
	/**
	 * This is a notification of changes in the Gnutella Connections list: when new connections are opened or closed.
	 */
	public void hostsChanged(HostsChangedEvent he) {
		if(he.getSource() instanceof Connection){//just being defensive
					Connection c = (Connection) he.getSource();

					String connectionType;
					if (c.getType()== Connection.CONNECTION_OUTGOING)
						connectionType = "OUTGOING"; // Connection.CONNECTION_OUTGOING); //to know if the connection is incoming or outgoing
					else
						connectionType = "INCOMING";

					
					String remoteservent = c.getRemoteServentId();
					if (remoteservent==null){ //version compatibility pb! // shouldn't happen any longer
						remoteservent = "[UnknownServentId]"+c.getConnectedServentIP(); // use remote IP address...
					}
					
					int port = c.getConnectedServentPort();
					boolean opening = (c.getStatus() == Connection.STATUS_OK); // indicates if the connection is stopping or starting
					adapter.notifyConnection(remoteservent, port, connectionType, opening);	
		}
	} 
		
	
}