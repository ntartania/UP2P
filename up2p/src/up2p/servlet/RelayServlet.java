package up2p.servlet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * The RelayServlet is responsible for relaying HTTP community servlet download requests 
 * between clients that would be otherwise unable to communicate directly. Connections 
 * must be initiated as a two step process. First, the peer intending to serve content must 
 * send a request to the relay servlet which provides a relay identifier and a URL to direct 
 * relay requests to (hostname : port / urlPrefix). This should occur when the serving peer 
 * receives a PUSH message and is unable to establish a direct connection. The relay 
 * identifier should then be sent through the Gnutella network to the downloading peer 
 * (using the reverse route of the PUSH message), along with the URL 
 * (hostname : port / urlPrefix) of the relay peer. The downloading peer should then send 
 * requests to the servlet relay on the relay peer, including the relay identifier as a 
 * parameter of the request (along with the resource ID, community ID, and filename of the 
 * desired download). If the relay identifier has been previously registered by another peer 
 * all requests sent to the relay servlet will be relayed to the community servlet of the 
 * serving peer, and responses will be forwarded back to the downloading peer. Relay identifiers 
 * should be random and mostly unique (at least for the duration of a single file transfer).
 * 
 * @author Alexander Craig
 */
public class RelayServlet extends AbstractWebAdapterServlet {
	
	/**
	 * A map of peer URLs (hostname:port/urlPrefix) keyed by relay identifiers.
	 * Serving peers must register with the relay servlet (and be added to this map)
	 * before requests can be forwarded to them.
	 */
	private Map<Integer, String> relayUrlMap;
	
	public RelayServlet() {
		relayUrlMap = new HashMap<Integer, String>();
		
		// TESTING: Add a test mapping
		// relayUrlMap.put(1234, "http://localhost:8080/up2p/");
	}
	
	/*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	
    	// Service registration of new peer mappings (request should be made by the peer
    	// that intends to serve files)
    	if(req.getParameter(HttpParams.UP2P_REGISTER_RELAY) != null
    			&& req.getParameter(HttpParams.UP2P_RELAY_IDENTIFIER) != null) {
    		String peerUrl = req.getParameter(HttpParams.UP2P_REGISTER_RELAY);
    		
    		// Add the http: prefix if it wasn't provided
    		if(!peerUrl.startsWith("http://")) {
    			peerUrl = "http://" + peerUrl;
    		}
    		
    		try {
    			int relayIdentifier = Integer.parseInt(req.getParameter(HttpParams.UP2P_RELAY_IDENTIFIER));
    			synchronized(relayUrlMap) {
    				relayUrlMap.put(relayIdentifier, peerUrl);
    			}
    			LOG.info("RelayServlet: Added mapping for relay identifier: " + relayIdentifier
    					+ " -> " + peerUrl);
    		} catch (NumberFormatException e) {
    			LOG.error("RelayServlet: Invalid relay identifier specified.");
    			return;
    		}
    		resp.setStatus(HttpServletResponse.SC_OK);
    		return;
    	}
    	
    	// Service requests that should be considered for forwarding to a previously 
    	// mapped peer URL
    	if(req.getParameter(HttpParams.UP2P_RELAY_IDENTIFIER) != null) {
    		// Get the relay identifier, and the associated mapped URL
    		int relayIdentifier;
    		try {
    			relayIdentifier = Integer.parseInt(req.getParameter(HttpParams.UP2P_RELAY_IDENTIFIER));
    		} catch (NumberFormatException e) {
    			LOG.error("RelayServlet: Provided relay identifier could not be parsed as an integer.");
    			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			return;
    		}
    		
    		String mappedUrl = relayUrlMap.get(relayIdentifier);
    		if(mappedUrl == null) {
    			LOG.error("RelayServlet: Got a relay request for unmapped relay identifier: " + relayIdentifier);
    			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			return;
    		}
    		
    		// Hmm... how to figure out which part of the URL show be maintained?
    		// For now, just assume all relay requests will be used for file downloads
    		// and work with that
    		
    		// Get the community ID, resource ID, and optional attachment name, and build
    		// the URL / query string for the forwarded request
    		String communityId = req.getParameter(HttpParams.UP2P_COMMUNITY);
    		String resourceId = req.getParameter(HttpParams.UP2P_RESOURCE);
    		String attachName = req.getParameter(HttpParams.UP2P_FILENAME);
    		if(communityId == null || resourceId == null) {
    			LOG.error("RelayServlet: Relay request did not specify a valid community or resource ID.");
    			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			return;
    		}
    		String urlSuffix = "/community/" + communityId + "/" + resourceId;
    		if(attachName != null) {
    			urlSuffix += "/" + attachName;
    		}
    		LOG.debug("RelayServet: Got relay request (Relay ID: " + relayIdentifier + "):\n" 
    				+ req.getRequestURL() + "?" + req.getQueryString());
    		LOG.debug("Serving request through URL: " + mappedUrl + urlSuffix);
    		
    		// Execute the request
    		HttpClient http = new DefaultHttpClient();
    		HttpGet relayRequest = new HttpGet(mappedUrl + urlSuffix);
    		HttpResponse relayResponse = http.execute(relayRequest);
    		
    		// Examine the response status
    		if(relayResponse.getStatusLine().getStatusCode() == 200) {
    			// Relay was successful
    			LOG.info("RelayServlet: Successful request: " + mappedUrl + urlSuffix);
    			if(relayResponse.getLastHeader("Content-Type") != null) {
    				resp.setContentType(relayResponse.getLastHeader("Content-Type").getValue());
    			}
    			
    			// Get hold of the response entity (file contents)
        		HttpEntity entity = relayResponse.getEntity();

        		// If the response does not enclose an entity, there is no need
        		// to worry about connection release
        		if (entity != null) {
        			DataOutputStream respOutStream = new DataOutputStream(resp.getOutputStream());
        		    DataInputStream relayInStream = new DataInputStream(entity.getContent());
        		    
        		    try {
        		    	while(true) {
        		    		// EOFException should terminate this loop
        		    		respOutStream.writeByte(relayInStream.readByte());
        		    	}
        		    } catch (EOFException e) {
    					// Response is complete, just proceed to finally block
    				} catch (IOException e) {
        		        LOG.error("RelayServlet: IOException copying relay data.");
        		    } catch (RuntimeException e) {
        		    	relayRequest.abort();
        		    } finally {
        		        // Closing the input stream will trigger connection release
        		        relayInStream.close();
        		        respOutStream.close();
        		    }
        		    return;
        		}
    		} else {
    			// If relay was not successful, just return the status code
    			resp.setStatus(relayResponse.getStatusLine().getStatusCode());
    			LOG.error("RelayServlet: Failed request: " + mappedUrl + urlSuffix
    					+ "\nStatus: " + relayResponse.getStatusLine());
    		}
    	}
    }

    /*
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	LOG.warn("RelayServlet: Got POST request, handling as GET.");
    	doGet(req, resp);
    }
}
