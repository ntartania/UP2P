package proxypedia;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import up2p.core.UserWebAdapter;
import up2p.servlet.DownloadServlet;

public class ProxyDownloadServlet extends HttpServlet {
	
	 /** Name of the logger used by all servlets. */
    public static final String LOGGER = "up2p.servlet";
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
    
    /**
     * Stores the servlet context for later use in the servlet service methods.
     * 
     * @param config the servlet configuration
     */
    public void init(ServletConfig config) {
        context = config.getServletContext();
        adapter = null;
        LOG.info("WP download servlet initialized");
    }
    
    /** The WikipediaProxyWebAdapter. */
    protected WikipediaProxyWebAdapter adapter;

    /** Context given to the Servlet on initialization. */
    protected ServletContext context;

    /** Logger used by all servlets. */
    protected Logger LOG = Logger.getLogger(LOGGER);

    /** User HTTP session. */
    protected HttpSession session;
    
    /**
     * Get the adapter from the application context if it exists. If not, the
     * adapter reference is set to null
     */
    protected void getAdapter() {
        Object o = context.getAttribute("adapter");
        if (o != null)
            adapter = (WikipediaProxyWebAdapter) o;
        else {
        	adapter = null;
        }
    }
    
    /**
     * Fetches the webAdapter (if it has not been fetched already), and forwards requests
     * to the relevant doXXX methods if so.
     */
    protected void service(HttpServletRequest req, HttpServletResponse resp)
    	throws ServletException, java.io.IOException {
    	
    	// Note: Adapter must exist at this point otherwise the AccessFilter would have
    	// redirected the servlet request to init.jsp. Therefore we can safely fetch the Adapter
    	// and assume it will be non-null when service is called.
    	LOG.info("Got a Service call!");
    	if(adapter == null) { getAdapter(); }
    	LOG.info("About to process it");
    	 doGet(req, resp);
    }

	/** copied from superclass, except that the request is not serviced directly but an intermediate step is used to store the  URLs*/
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
	        	
	        		
	        		// Assumes all other access is through "/community" path, may need to change if more
	        		// special download URL's are added
	        		
	        		// Get community id, resource id and attachment name (if it exists)
	                communityId = pathElements.nextToken();
	                resourceId = pathElements.nextToken();
	                if (pathElements.hasMoreTokens()) {
	                    attachName = pathElements.nextToken();
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
	    
	    /**
	     * Returns a string identifying the user making the request using their IP
	     * address.
	     * 
	     * @param request request to the Servlet
	     * @return a string identifying the request
	     */
	    public static String getInfo(HttpServletRequest request) {
	        return "(IP:" + request.getRemoteAddr() + ") ";
	    }
}
