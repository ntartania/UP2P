package up2p.peer.jtella;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import up2p.core.LocationEntry;
import up2p.search.SearchResponse;
import up2p.xml.TransformerHelper;

/**
 * Message sent from server to client in response to a search request.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class SearchResponseMessage extends GenericPeerMessage {
	/** 
	 * Determines the maximum number of results that will be placed in each 
	 * SearchResponseMessage when split() is called and the message's size could not
	 * be determined..
	 */
	public static int DEFAULT_MESSAGE_SPLIT_COUNT = 5;
	
	/**
	 * The maximum size of the generated message (64kb)
	 */
	public static int MAX_MESSAGE_SIZE = 64000;
	
	/** 
	 * The number of bytes which should be left unused for payload
	 * (assumed to be header content)
	 */
	public static int HEADER_PADDING_BYTES = 2000;
	
    /** Holds a list of SearchResponse objects */
    private ArrayList<SearchResponse> results;

    private String communityId;
    
    /** 
     * A list of hosted resource id's to be included as meta-data in the search response
     * (Optional, data is simply not included if the list is null).
     */
    private List<String> hostedResIdList;
    
    /**
     * A map of trust metrics to include in the search response. Each key should be
     * a metric name and the corresponding value should be the metric value.
     * 
     * Ex.
     * trustMetrics.put("Network Neighbours", "0")
     */
    private Map<String, String> trustMetrics;
    
    /** 
     * The IP/Port of the peer serving these search results.
     * Note: The IP/Port is not serialized into the message XML,
     * as it can be read at the Gnutella level. 
     **/
    private String ipPort;

    /** The url prefix of the peer serving these results */
    private String urlPrefix;
    
	/** Error message returned when a parsing error occurs */
    public static final String ERROR_MSG = "Error parsing a Search Request";

    /** XML tags for response messages */
    public static final String X_COMMUNITY = "communityId";

    public static final String X_SEARCH_RESPONSE = "searchResponse";

    public static final String X_SEARCH_RESULT = "searchResult";

    public static final String X_SEARCH_ID = "id";

    public static final String X_RESULT_SIZE = "resultSetSize";

    public static final String X_TITLE = "title";

    public static final String X_RESOURCE_ID = "id";
    
    public static final String X_HOSTED_RES_ID_LIST = "LocalResList";
    
    public static final String X_TRUST_METRIC = "trustMetric";

    public static final String X_FILENAME = "filename";
    
    public static final String X_ATTR_METRIC_NAME = "name";
    
    public static final String X_ATTR_METRIC_VALUE = "value";
    
    public static final String X_URL_PREFIX = "urlPrefix";

    /**
     * Constructs an empty response message.
     * 
     * @param id The id of the search response
     * @param communityId The communityId of the response message
     * @param urlPrefix	The urlPrefix of the peer serving these results.
     */
    public SearchResponseMessage(String id, String communityId, String urlPrefix) {
        super(SEARCH_RESPONSE);
        results = new ArrayList<SearchResponse>();
        hostedResIdList = null;
        trustMetrics = new TreeMap<String, String>();
        this.urlPrefix = urlPrefix;
        this.id = id;
        this.communityId = communityId;
    }
    
    /**
     * Sets the list of local resource id's that should be added to the search response
     * XML to provide metric data to the querying node.
     * 
     * @param localResIds	A list of resource id's that this node is hosting in the queried
     * 									community. This will be passed to the querying node for
     * 									use in trust metric calculations.
     */
    public void setHostedResIdList(List<String> localResIds) {
    	hostedResIdList = localResIds;
    }
    
    /**
     * Fetches the list of trust metric resource id's included in this message as a
     * "/" separated string.
     * 
     * @return	A "/" separated string of the list of trust metric id's included in this message,
     * 					or null if the list is null or empty.
     */
    public String getHostedResIdListString() {
    	if(hostedResIdList == null || hostedResIdList.isEmpty()) {
    		return null;
    	} else {
    		String metricString = "";
    		for(String rId : hostedResIdList) {
    			metricString += "/" + rId;
    		}
    		return metricString.substring(1);
    	}
    }
    
    
    /**
     * Fetches the list of resource identifiers included in this search response as trust
     * metric data.
     * @return	The list of resource identifiers included in this search response as trust
     * metric data, or null if the list is empty or has not been specified.
     */
    public List<String> getHostedResIdList() {
    	if(hostedResIdList == null || hostedResIdList.isEmpty()) {
    		return null;
    	}
    	return hostedResIdList;
    }
    
    /** @return The IP address / port of the server hosting these search results. */
    public String getIpPort() {
		return ipPort;
	}

    /** 
     * Sets the IP address / port and the url prefix of the server 
     * hosting these search results. 
     */
	public void setHost(String ipPort) {
		this.ipPort = ipPort;
	}
	
	/** @return The url prefix of the server hosting these search results */
	public String getUrlPrefix() {
		return urlPrefix;
	}
	
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

    /**
     * Adds a search result to this search response.
     * 
     * @param communityId id of the community where the resource is shared
     * @param resourceId id of the resource to add to the result
     * @param resourceTitle title of the resource
     * @param fileName name of the file where the resource should be saved
     * @param locations location entries for the given resource
     */
    public void addResult(String communityId, String resourceId,
            String resourceTitle, String fileName, LocationEntry[] locations) {
        results.add(new SearchResponse(resourceId, resourceTitle, communityId,
                fileName, locations, false));
    }
    
    //the simplest: if we already have a response
    public void addResult(SearchResponse r){
    	results.add(r);
    }
    
    /**
     * Adds a new trust metric to the list of metrics for this response message.
     * If an existing metric exists under the same name it is removed.
     * @param name	The name of the new trust metric to add
     * @param value	The value of the trust metric
     */
    public void addTrustMetric(String name, String value) {
    	if(trustMetrics.containsKey(name)) {
    		trustMetrics.remove(name);
    	}
    	trustMetrics.put(name, value);
    }
    
    /**
     * Fetches a trust metric value by metric name.
     * @param name	The name of the trust metric to fetch
     * @return	The associated value of the trust metric, or null if no entry was found.
     */
    public String getTrustMetric(String name) {
    	return trustMetrics.get(name);
    }

    /**
     * Adds a search result to this search response.
     * 
     * @param communityId id of the community where the resource is shared
     * @param resourceId id of the resource to add to the result
     * @param resourceTitle title of the resource
     * @param fileName name of the file where the resource should be saved
     * @param locations location entries for the given resource
     */
    public void addResult(String communityId, String resourceId,
            String resourceTitle, String fileName, LocationEntry[] locations, String queryId) {
        results.add(new SearchResponse(resourceId, resourceTitle, communityId,
                fileName, locations, false, queryId));
    }
    
    /**
     * Adds a search result to this search response. with a Document contianing the metadata of the result resource
     * 
     * @param communityId id of the community where the resource is shared
     * @param resourceId id of the resource to add to the result
     * @param resourceTitle title of the resource
     * @param fileName name of the file where the resource should be saved
     * @param locations location entries for the given resource
     */
    public void addResult(String communityId, String resourceId,
            String resourceTitle, String fileName, LocationEntry[] locations, String queryId, Document resourceDOM) {
    	SearchResponse resp = new SearchResponse(resourceId, resourceTitle, communityId,
                fileName, locations, false, queryId);
    	resp.addResourceDOM(resourceDOM);
        results.add(resp);
    }
    
    
    /**
     * Returns a list of <code>SearchResponse</code> objects that are
     * contained in this Search Response Message.
     * 
     * @return a list of responses
     */
    public SearchResponse[] getResponses() {
        return (SearchResponse[]) results.toArray(new SearchResponse[results
                .size()]);
    }

    /**
     * Parses a search response from the given XML fragment.
     * 
     * @param xmlNode the XML containing the search response
     * @return the search response
     * @throws MalformedPeerMessageException on a badly formed or invalid
     * message
     */
    public static SearchResponseMessage parse(Node xmlNode)
            throws MalformedPeerMessageException {

        // check first node name
        try {
            if (xmlNode.getNodeName().equals(GenericPeerMessage.X_UP2P_MESSAGE)) {
                Node currentNode = xmlNode.getFirstChild();
                while (currentNode != null
                        && currentNode.getNodeType() != Node.ELEMENT_NODE)
                    currentNode = currentNode.getNextSibling();
                Element current = (Element) currentNode;

                String communityId = current.getAttribute(X_COMMUNITY);
                String searchId = current.getAttribute(X_SEARCH_ID);
                String urlPrefix = current.getAttribute(X_URL_PREFIX);
                
                SearchResponseMessage response = new SearchResponseMessage(searchId, communityId, urlPrefix);

                // add result for each searchResult child, and process the metric list
                // if it exists
                LocationEntry[] entries = null;
                NodeList resultList = current.getChildNodes();
                for (int i = 0; i < resultList.getLength(); i++) {

                    // check node type to avoid mixed content
                    if (resultList.item(i).getNodeType() == Node.ELEMENT_NODE &&
                    		resultList.item(i).getNodeName().equals(X_SEARCH_RESULT)) {
                    	
                    	// A Search Result element has been found
                    	String resourceTitle = null;
                        String resourceId = null;
                        String fileName = null;
                    	
                        Element result = (Element) resultList.item(i);
                        resourceTitle = result.getAttribute(X_TITLE);
                        resourceId = result.getAttribute(X_RESOURCE_ID);
                        fileName = result.getAttribute(X_FILENAME);

                        // Construct the SearchResponse with what we have so far (missing DOM, locations are not serialized)
                        SearchResponse newresponse = new SearchResponse(resourceId, resourceTitle, communityId, 
                                fileName,null, false, searchId);
                       
                        if (result.hasChildNodes()){ //in this case, yes! we have a winner!
                        	//some gymnastics to create the exact same class of Document, in order to be able to adopt nodes...
                        	//DOMImplementation impl = locationNode.getOwnerDocument().getImplementation();
                        	Document metadata = TransformerHelper.newDocument();//impl.createDocument(null, "qname",locationNode.getOwnerDocument().getDoctype() );
                       
                        	/*
                        	System.out.println("adopter class:"+ metadata.getClass());
                        	System.out.println("adoptee class:" + locationNode.getOwnerDocument().getClass());
                        	*/
                        	
                        	// Import the metadata into the new document context
                        	NodeList copyNodes = result.getChildNodes();
        	                for(int k = 0; k < copyNodes.getLength(); k++) {
        		                Node importedNode = metadata.importNode(copyNodes.item(k), true);
        		                metadata.appendChild(importedNode);
        	                }
                        	
                        	//add the DOM to the searchResponse under construction
                        	newresponse.addResourceDOM(metadata);
                        }

                        response.addResult(newresponse);
                    } else if (resultList.item(i).getNodeType() == Node.ELEMENT_NODE &&
                    		resultList.item(i).getNodeName().equals(X_HOSTED_RES_ID_LIST)) {
                    	
                    	// A hosted resource id list element has been found
                    	
                    	// The node is a metric list, build the metric list for this search response
                    	List<String> respMetrics = new ArrayList<String>();
                    	NodeList metricIds = resultList.item(i).getChildNodes();
                        for (int j = 0; j < metricIds.getLength(); j++) {
                        	respMetrics.add(metricIds.item(j).getTextContent());
                        }
                        response.setHostedResIdList(respMetrics);
                    } else if (resultList.item(i).getNodeType() == Node.ELEMENT_NODE &&
                    		resultList.item(i).getNodeName().equals(X_TRUST_METRIC)) {
                    	
                    	// A trust metric element has been found
                    	String metricName = ((Element)resultList.item(i)).getAttribute(X_ATTR_METRIC_NAME);
                    	String metricValue = ((Element)resultList.item(i)).getAttribute(X_ATTR_METRIC_VALUE);
                    	response.addTrustMetric(metricName, metricValue);
                    }
                    
                }
                return response;
            }
            
            throw new MalformedPeerMessageException(ERROR_MSG
                    + " Root node name: " + xmlNode.getNodeName());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new MalformedPeerMessageException(ERROR_MSG);
        }
    }
    
    /**
     * Returns a list of all trust metric names contained in the search response.
     * @return	A list of all trust metric names contained in the search response, or
     * 					null if the list would be empty
     */
    public List<String> getMetricNames() {
    	if(trustMetrics.isEmpty()) {
    		return null;
    	}
    	
    	List<String> returnList = new ArrayList<String>();
    	Iterator<String> metricKeys = trustMetrics.keySet().iterator();
    	while(metricKeys.hasNext()) {
    		returnList.add(metricKeys.next());
    	}
    	return returnList;
    	
    }

    /*
     * @see up2p.peer.generic.message.SerializableMessage#serialize()
     */
    public Node serialize() {
        // create the XML DOM
        Document document = TransformerHelper.newDocument();

        // create root node
        Element root = document
                .createElement(GenericPeerMessage.X_UP2P_MESSAGE);
        document.appendChild(root);

        Element current = document.createElement(X_SEARCH_RESPONSE);
        current.setAttribute(X_URL_PREFIX, urlPrefix);
        current.setAttribute(X_SEARCH_ID, id);
        current.setAttribute(X_RESULT_SIZE, String.valueOf(getResultSetSize()));
        current.setAttribute(X_COMMUNITY, communityId);
        root.appendChild(current);
        
        // Add the metric list to the root node (if applicable)
        if (hostedResIdList != null && !hostedResIdList.isEmpty()) {
        	// Generate the metricList node
        	Element metricNode = document.createElement(X_HOSTED_RES_ID_LIST);
        	// Add all the resource Ids
        	for(String rId : hostedResIdList) {
        		Element rIdNode = document.createElement(X_RESOURCE_ID);
        		rIdNode.setTextContent(rId);
        		metricNode.appendChild(rIdNode);
        	}
        	// Append the metricList to the search result node
        	current.appendChild(metricNode);
        }
        
        // Add any raw trust metrics to the generated XML (if the trust metric map has elements)
        if(!trustMetrics.isEmpty()) {
        	Iterator<String> metricKeys = trustMetrics.keySet().iterator();
        	while(metricKeys.hasNext()) {
        		Element rawMetricNode = document.createElement(X_TRUST_METRIC);
        		String metricName = metricKeys.next();
        		rawMetricNode.setAttribute(X_ATTR_METRIC_NAME, metricName);
        		rawMetricNode.setAttribute(X_ATTR_METRIC_VALUE, trustMetrics.get(metricName));
        		current.appendChild(rawMetricNode);
        	}
        }

        // create searchResults nodes for each search result
        SearchResponse[] responses = getResponses();
        for (int i = 0; i < responses.length; i++) {
            SearchResponse response = responses[i];
            current.appendChild(document.importNode(serializeSearchResponse(response), true));
        }
        return document.getDocumentElement();
    }
    
    /**
     * @return The number of bytes the serialized message will require to store 
     * in UTF-8 encoding.
     */
    public int getSerializedBytes() throws IOException {
    	StringWriter serializeBuffer = new StringWriter();
    	TransformerHelper.encodedTransform((Element)this.serialize(), "UTF-8", serializeBuffer, true);
		
		return serializeBuffer.toString().length() * 2; // 2 bytes per character in UTF-8
    }
    
    /**
     * Generates an XML Element representing a single SearchResponse
     * in the format expected by a SearchResponseMessage.
     * @param response	The SearchResponse to generate XML for
     * @return	The generated XML element
     */
    public static Element serializeSearchResponse(SearchResponse response) {
    	Document document = TransformerHelper.newDocument();
    	
    	Element searchResult = document.createElement(X_SEARCH_RESULT);
        searchResult.setAttribute(X_TITLE, response.getTitle());
        searchResult.setAttribute(X_RESOURCE_ID, response.getId());
        searchResult.setAttribute(X_FILENAME, response.getFileName());

        // Add article DOM as a child of the searchResult
        try{
            NodeList copyNodes = response.getResourceDOM().getChildNodes();
            for(int k = 0; k < copyNodes.getLength(); k++) {
                Node importedNode = document.importNode(copyNodes.item(k), true);
                searchResult.appendChild(importedNode);
            }
            
        } catch(DOMException e){
        	e.printStackTrace(); // just output the trace and continue	
        }
        
        return searchResult;
    }
    
    /**
     * Determines the number of bytes that will be used to store the serialized
     * representation of a single SearchResponse (assuming UTF-8 encoding)
     * @param response	The SearchResponse to serialize and evaluate
     * @return	The number of bytes used to store the serialized response
     */
    public static int getSerializedResponseBytes(SearchResponse response) throws IOException {
    	StringWriter serializeBuffer = new StringWriter();
		Node serializeNode = serializeSearchResponse(response);
		TransformerHelper.encodedTransform((Element) serializeNode, "UTF-8", serializeBuffer, true);
		
		return serializeBuffer.toString().length() * 2; // 2 bytes per character in UTF-8
    }

    /**
     * Returns the result set size.
     * 
     * @return the size of the result set
     */
    public int getResultSetSize() {
        return results.size();
    }

    /**
     * Sets the resource id.
     * 
     * @param messageId resource id to set
     */
    public void setId(String messageId) {
        id = messageId;
    }
    
    /**
     * Sets the resource id.
     * 
     * @param messageId resource id to set
     */
    public String getId() {
        return id;
    }
    
    
    /**
     * Returns the community id.
     * 
     * @return community id
     */
    public String getCommunityId() {
        return communityId;
    }

    /**
     * Sets the community id.
     * 
     * @param communityId id to set
     */
    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }
    
    /**
     * If the message produces a Gnutella message of over 65kb, it needs to be reduced in size by
     * splitting the message into a number of smaller messages. Whenever a message is split
     * any resource ID list the message may have contained will be split into an entirely separate
     * message. The number of results to store in each message is dynamically generated based
     * on the size of the original serialized message.

     * @return An array of new, smaller messages (or an array containing the original message
     * if it was already small enough)
     */
	public List<SearchResponseMessage> split() {
		ArrayList<SearchResponseMessage> returnList = new ArrayList<SearchResponseMessage>();
		
		int messageSize;
		try {
			messageSize = getSerializedBytes();
		} catch (IOException e) {
			// Just print the stack trace and return the original message if the size
			// cannot be determined (not sure how this would happen)
			e.printStackTrace();
			returnList.add(this);
			return returnList;
		}
		
		if(messageSize < MAX_MESSAGE_SIZE) {
			// Message is already small enough, just wrap it in an array
			returnList.add(this);
			return returnList;
		} else {
			// The message exceeds the maximum message size and must be split
			SearchResponseMessage splitMessage = new SearchResponseMessage(getId(), getCommunityId(),
					getUrlPrefix());
			returnList.add(splitMessage);
			
			// If a hosted resource list or metrics exist, separate them into a separate
			// message
			if((hostedResIdList != null && !hostedResIdList.isEmpty())
					|| (!this.getMetricNames().isEmpty())) {
				// System.out.println("== Seperating out resource list and metrics."); // DEBUG
				if(hostedResIdList != null && !hostedResIdList.isEmpty()) {
					splitMessage.setHostedResIdList(hostedResIdList);
				}
				
				if(!this.getMetricNames().isEmpty()) {
					for(String metricName : this.getMetricNames()) {
						splitMessage.addTrustMetric(metricName, this.getTrustMetric(metricName));
					}
				}
				
				splitMessage.setUrlPrefix(getUrlPrefix());
				
				// Generate a new message (the first to actually hold resource information)
				splitMessage = new SearchResponseMessage(getId(), getCommunityId(), getUrlPrefix());
				returnList.add(splitMessage);
			}
			
			// Add all SearchResponses to the newly generated "smaller" messages,
			// until each message approaches the maximum message size
			
			int totalResponseBytes = 0; // The number of bytes used in the current "smaller" message
			for (SearchResponse curResponse : results){
				try {
					int curResponseSize = getSerializedResponseBytes(curResponse);
					if((curResponseSize + totalResponseBytes) > (MAX_MESSAGE_SIZE - HEADER_PADDING_BYTES)
							&& (totalResponseBytes > 0)) {
						// Generate a new response message whenever the maximum message size would be reached,
						// and the message already contains at least 1 response.
						
						// TODO: If the max message size would be exceeded by a single response, the response is
						// added anyway and compression should bring it under the maximum size.
						// Otherwise, nothing can really be done here other than dropping the response entirely
						// (i.e. resources over 64KB would be unsearchable)
						
						/* System.out.println("=== Generating new message (Current: " + totalResponseBytes + " bytes"
								+ "\tTried to add: " + curResponseSize + " bytes"); */ // Debug
						splitMessage = new SearchResponseMessage(getId(), getCommunityId(), getUrlPrefix());
						splitMessage.setUrlPrefix(getUrlPrefix());
						returnList.add(splitMessage);
						totalResponseBytes = 0;
					}
					
					// System.out.println("=== Adding response to current message: " + curResponseSize + " bytes"); // Debug
					splitMessage.addResult(curResponse);
					totalResponseBytes += curResponseSize;
				} catch (IOException e) {
					// Just print the trace and ignore the search response if its size cannot
					// be determined (not sure how this would happen)
					e.printStackTrace();
				}
			}
			
			// System.out.println("=== Size of last message: " + totalResponseBytes + " bytes."); // Debug
			
			return returnList;
		}
	}
}