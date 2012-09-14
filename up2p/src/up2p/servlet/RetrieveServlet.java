package up2p.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import up2p.core.LocationEntry;
import up2p.core.NetworkAdapterException;
import up2p.core.ResourceNotFoundException;
import up2p.search.SearchResponse;
import up2p.search.SearchQuery;

/**
 * Retrieves search results from the network. Search results are stored in the
 * user session and the given result parameter determines which result is
 * retrieved.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class RetrieveServlet extends AbstractWebAdapterServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String MODE = "search";
	
	/** 
	 * Specifies the maximum number of peers that the retrieve servlet will
	 * attempt a direct download from before initiating a PUSH transfer
	 */
	private static final int SOURCES_PER_RETRIEVE = 10;

    /*
     * @see javax.servlet.http.HttpServlet#doGet(
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        // DEBUG
    	/*
        if (LOG.isDebugEnabled()) {
            StringBuffer debugStr = new StringBuffer(
                    "RetrieveServlet received request. Parameters: ");
            Enumeration paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = (String) paramNames.nextElement();
                debugStr.append("[" + paramName + "="
                        + request.getParameter(paramName) + "]");
            }
            LOG.debug(debugStr.toString());
        }
        */

        // Get the parameters of the resource to download
        String resId = request.getParameter(HttpParams.UP2P_RESOURCE);
        String comId = request.getParameter(HttpParams.UP2P_COMMUNITY);
        String filename = request.getParameter(HttpParams.UP2P_FILENAME);
        String[] rawPeerIds = request.getParameterValues(HttpParams.UP2P_PEERID);
        
        LOG.debug("RetrieveServlet: Got Request:\nComID: " + comId + "\tResId: " + resId
        		+ "\nFilename: " + filename + "\tSources: " + rawPeerIds);
        
        if(resId == null || comId == null) {
        	String errMsg = "Retrieve request failed to provide community or resource ID.";
	        LOG.error("RetrieveServlet: Request failed to provide community or resource ID.");
			writeError(request, response, errMsg, MODE);
			return;
        }
        
        if(adapter.isResourceLocal(comId, resId)) {
        	// If the resource is already local, redirect to the viewing page
        	LOG.info("RetrieveServlet: Resource " + comId + "/" + resId + " was found locally, "
        			+ " redirecting to viewing page.");
        	String redirect = "view.jsp?" + HttpParams.UP2P_COMMUNITY + "=" + comId + "&"
        		+ HttpParams.UP2P_RESOURCE + "=" + resId;
        	RequestDispatcher rd = request.getRequestDispatcher(redirect);
            if (rd != null) {
                LOG.debug("RetrieveServlet Redirecting to " + redirect);
                rd.forward(request, response);
            } else {
                LOG.error("RetrieveServlet Error getting request dispatcher.");
            }
        }
        
        // Try to build a list of peer identifiers from the request parameters
        List<String> peerIds;
        if(rawPeerIds != null && rawPeerIds.length > 0) {
        	peerIds = Arrays.asList(rawPeerIds);
        } else {
        	peerIds = new ArrayList<String>();
        }
        
        if(peerIds.size() == 0 || filename == null) {
        	LOG.debug("RetrieveServlet: Request did not specify either a download source or a filename, "
        			+ "attempting to load stored search response.");
        	
        	// If no peer id or filename was provided, try to use stored search results
            // to complete missing data.
        	SearchResponse resSearchResult = adapter.getSearchResponse(comId, resId);
        	if(resSearchResult == null || resSearchResult.getLocationCount() == 0) {
        		
        		// No peers could be found, launch a search and redirect the
        		// user to the search results page.
    	        LOG.debug("RetrieveServlet: Request did not specify download locations or a filename, and "
        			+ " the information could not be found in saved search results.");
    	        String redirect;
    	        
				try {
					redirect = adapter.retrieve(comId, resId, null, null);
				} catch (ResourceNotFoundException e) {
					String errMsg = "RetrieveServlet: Specified community and resource ID could not"
							+ "be found";
					LOG.error(errMsg);
    				writeError(request, response, errMsg, MODE);
					return;
				}
				
    	        RequestDispatcher rd = request.getRequestDispatcher(redirect);
    	        if (rd != null) {
    	            LOG.debug("RetrieveServlet Redirecting to " + redirect);
    	            rd.forward(request, response);
    	        } else {
    	            LOG.error("RetrieveServlet Error getting request dispatcher.");
    	        }
    			return;
        	}
        	
        	// Search result was successfully found
        	if(filename == null) {
        		filename = resSearchResult.getFileName();
        		LOG.debug("RetrieveServlet: No filename was provided with the request, "
        				+ "using the stored search response filename (" + filename + ")");
        	}
        	
        	if(peerIds.size() == 0) {
        		LOG.debug("RetrieveServlet: No peer identifiers were provided with the request, "
        				+ "using the stored search response download locations.");
	        	List<LocationEntry> downloadLocations = resSearchResult.getLocations();
	        	while(peerIds.size() < SOURCES_PER_RETRIEVE && !downloadLocations.isEmpty()) {
	        		peerIds.add(downloadLocations.remove(0).getLocationString());
	        	}
        	}
        }
        
        if(request.getParameter(HttpParams.UP2P_BACKGROUND_REQUEST) == null) {
	        String currentCommunity = getCurrentCommunityId(request.getSession());
	        if(currentCommunity != null && !currentCommunity.equals(comId)) {
	        	request.getSession().setAttribute(CURRENT_COMMUNITY_ID, comId);
	        	adapter.addNotification("Retrieve request changed current community to: " 
	        			+ adapter.RMgetCommunityTitle(comId));
	        }
        }
        
        
        // Make sure there's a session running.
        initSession(request);
        
        String redirect = null;
        
        // Check whether a push request was explicitly requested
        boolean usePush = request.getParameter(HttpParams.UP2P_PUSH_TEST) != null &&
			request.getParameter(HttpParams.UP2P_PUSH_TEST).length() > 0;
        
		LOG.debug("RetrieveServlet: About to retrieve from network: " + comId + "/" + resId + "/" + filename);
		
		if(!usePush) {
			// If a push was not explicitly requested, attempt a direct download
        	// from each specified peer id in sequence until the download is successful
			// or all peers are exhausted
			usePush = true;
			
			for(String peerid : peerIds) {
				try {
					LOG.debug("RetrieveServlet: Retrieving from peer: " + peerid);
		        	redirect = adapter.retrieve(comId, resId, filename, peerid);
		            LOG.debug("RetrieveServlet: Successfully retrieved through direct connection.");
		            
		            // Retrieve was successful, clear the push flag and abort all subsequent
		            // download attempts
		            usePush = false;
		            break;
			    } catch(ResourceNotFoundException e) {
			    	// Direct download failed, move on to next peer
			    	LOG.debug("RetrieveServlet: Direct connection to " + peerid + " failed.");
		        	continue;
			    }
			}
		}
	    
	    if(usePush) {
        	// Direct download failed or a push was explicitly requested, try sending PUSH message.
        	// Need to redirect user to search page if this is not a batch request.
        	String title = request.getParameter(HttpParams.UP2P_RESOURCE_TITLE);
        	if(title == null) {
        		title = filename;
        	}
        	
        	LOG.debug("RetrieveServlet: Direct retrieve of resource: \"" + title + "\" (resource ID: " 
        			+ resId + ") failed, issuing a PUSH request.");
        	adapter.addNotification("Direct retrieve of resource: \"" + title + "\" failed, issuing a PUSH request.");
        	
    		// TODO: A push request is currently only sent to the first peer in the list,
    		// 		 We may want to investigate a more robust method of selecting peers to issue
    		//		 push requests to.
    		
    		// Note: The previously used HttpParams.UP2P_XMLHTTP parameter is now ignored, and push
    		//		 requests never generate a search redirect.
        	redirect = adapter.pushRedirect(comId, resId, peerIds.get(0), title, filename, false);
    	}

        RequestDispatcher rd = request.getRequestDispatcher(redirect);
        
        // Use the user session to pass the temporary directory to the file upload servlet
        HttpSession reqSession = request.getSession();
        reqSession.setAttribute("up2p:attachdir", 
        		new File(adapter.getAttachmentStorageDirectory(comId, resId)));
        
        if (rd != null) {
            LOG.debug("RetrieveServlet Redirecting to " + redirect);
            rd.forward(request, response);
        } else {
            LOG.error("RetrieveServlet Error getting request dispatcher.");
        }
    }
    
    /*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }

    /**
     * Formats an error messages for display to the user.
     * 
     * @param response response to write message out to
     * @param errorMsg error message to write
     * @return formatted error message
     */
    private static String formatWebErrorMsg(HttpServletResponse response,
            String errorMsg) {
        return "<p><b>Error:</b> " + errorMsg
                + "</p><p>Go back to <a title=\"Search Results\" href=\""
                + response.encodeURL("displayResults.jsp")
                + "\">Search Results</a>.</p>";
    }

    /**
     * Formats an error message for entry in the logs.
     * 
     * @param errorMsg error message for the log
     * @return message formatted for the log
     */
    private static String formatLogErrorMsg(String errorMsg) {
        return "RetrieveServlet " + errorMsg;
    }
}