package up2p.servlet;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filters incoming requests (from other peers) for downloads of resources and
 * attachments and returns the local file found in the file mapper.
 * 
 * 
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DownloadServlet extends AbstractWebAdapterServlet {
    /**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/** XML content-type for HTTP Response. */
    public static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";

    /**
     * Default URL scheme for attachment links in outgoing resources is u-p2p.
     */
    public static final String DEFAULT_ATTACHMENT_SCHEME = "attach";

    /** Encoding used when XML files are downloaded (UTF-8). */
    public static final String ENCODING = "UTF-8";

    /*
     * Looks up the request path in the local file mapper and returns the
     * resource or attachment in the HTTP response. If the resource is not
     * found, the request returns a <code> 404 File Not Found </code> code.
     * 
     * @see javax.servlet.http.HttpServlet#doGet
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    	String communityId = null, resourceId = null, attachName = null;
    	
    	// Get the user's session (used to find community automatically)
        HttpSession reqSession = request.getSession();
    	
    	// disable caching
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");

        // set filter type if present
        String filterType = null;
        
        // use whatever URL scheme is specified in filter parameter,
        // otherwise replace URL schemes with u-p2p
        filterType = request.getParameter("filter");
        if (filterType == null)
            filterType = DEFAULT_ATTACHMENT_SCHEME;

        // get request path
        String path = request.getPathInfo();
        if (path == null) {
            debugDownload(request, response);
            return;
        }
        
        LOG.debug("DownloadServlet Received download request. Path " + path
                + " Filter: " + filterType);
        
        StringTokenizer pathElements = new StringTokenizer(path, "/");

        try {
        	if (request.getServletPath().equals("/comm_attach")) {
        		// CASE: "/comm_attach/*" URL
        		// Serve a file from the community attachment folder of the current community
        		
        		/*
        		// Debug printing
        		Enumeration<String> attributes = reqSession.getAttributeNames();
        		while(attributes.hasMoreElements()) {
        			String attrName = attributes.nextElement();
        			LOG.debug("DownloadServlet: " + attrName + " -> " + reqSession.getAttribute(attrName));
        		}
        		*/
        		
        		// Use root community as community id, map resource id to the current active community
        		communityId = adapter.getRootCommunityId();
        		resourceId = getCurrentCommunityId(reqSession);
        		attachName = pathElements.nextToken(); // Must have an attachment name if /comm_attach/ is used
        		
        		// Ensure that a valid community id is stored in the user session
        		if(resourceId == null || resourceId.equals("")) {
        			throw new NoSuchElementException();
        		}
        		
        	} else {
        		
        		// Assumes all other access is through "/community" path, may need to change if more
        		// special download URL's are added
        		
        		// Get community id, resource id and attachment name (if it exists)
                communityId = pathElements.nextToken();
                resourceId = pathElements.nextToken();
                if (pathElements.hasMoreTokens()) {
                    attachName = pathElements.nextToken();
                }
        	}
        	
        	LOG.debug("DownloadServlet: Servlet accessed by " + request.getServletPath()
    				+ ", accessing community: " + communityId + " with res Id: " + resourceId);
            
        } catch (NoSuchElementException e) {
        	
            // Not enough elements in the requested path, refuse the request
            LOG.debug("DownloadServlet " + getInfo(request)
                    + "Invalid community id and/or resource id.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
            
        }

        // Service the request using the Download Service
        adapter.getDownloadService().doDownloadService(request, response,
                 filterType, communityId, resourceId, attachName, context);
    }

    /**
     * Logs debugging messages for the request.
     * 
     * @param request the download request
     * @param response the HTTP response
     * @throws IOException thrown if sendError fails
     */
    protected void debugDownload(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // log weird request
        LOG.error("DownloadServlet received an invalid request.");
        LOG.error("DownloadServlet path info=" + request.getPathInfo());
        LOG.error("DownloadServlet context path=" + request.getContextPath());
        LOG.error("DownloadServlet servlet path=" + request.getServletPath());
        LOG.error("DownloadServlet request URI=" + request.getRequestURI());
        LOG.error("DownloadServlet query string=" + request.getQueryString());
        
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}