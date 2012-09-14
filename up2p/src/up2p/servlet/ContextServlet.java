package up2p.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import up2p.core.ResourceManager;
import up2p.core.UserNotification;
import up2p.core.WebAdapter;
import up2p.xml.TransformerHelper;

/**
 * Provides context information for XSLT stylesheets to use when displaying
 * resources or rendering the Create and Search pages. 
 * 
 * @author Neal Arthorne
 * @author Alexander Craig
 * @version 1.0
 */
public class ContextServlet extends AbstractWebAdapterServlet {

    /**
	 *  serialization number
	 */
	private static final long serialVersionUID = 1L;

	/** Element where nav attributes are set. */
    private Element nav;

    /** Navigation XML. */
    private Document navDom;

    /** Creates the XML for use in the servlet. */
    public ContextServlet() {
        // create navigation DOM
        navDom = TransformerHelper.newDocument();
        Element top = navDom.createElement("up2pContext");
        navDom.appendChild(top);
        nav = navDom.createElement("navigation");
        top.appendChild(nav);
    }

    /**
     * Lists the contents of the resource database with a simple community and
     * resource structure. The id and title of each resource is listed in a
     * hierarchy.
     * 
     * @param request request for the content information
     * @param response response to the client
     * @throws ServletException if an error occurs
     * @throws IOException if an error occurs
     */
    private void doContents(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // make sure adapter is set
        getAdapter();

        // get the local repository
        //ResourceManager rm = adapter.getResourceManager();

        // create the contents DOM
        Document contentDom = TransformerHelper.newDocument();
        Element context = contentDom.createElement("up2pContext");
        contentDom.appendChild(context);
        Element contents = contentDom.createElement("contents");
        context.appendChild(contents);

        // Check if a community parameter was provided, and display
        // only that specific community if so
        if(request.getParameter(HttpParams.UP2P_COMMUNITY) != null) {
        	String communityId = request.getParameter(HttpParams.UP2P_COMMUNITY);
        	if(adapter.RMisCommunity(communityId)) {
        		Document commDom = adapter.getCommunityContentAsDOM(communityId, false);
        		contents.appendChild(contentDom.importNode(commDom.getFirstChild(), true));
        	} else {
        		LOG.error("ContextServlet: Got request for invalid community ID: " + communityId);
        		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        		return;
        	}
        } else {
        	// create community nodes with resource children
        	Iterator<String> communityIt = adapter.browseCommunities();
        	while (communityIt.hasNext()) {
        		contents.appendChild(contentDom.importNode(adapter.getCommunityContentAsDOM(communityIt.next(), false).getFirstChild(), true));
        	}
        }

        // return the DOM
        response.setContentType("text/xml");
        TransformerHelper.encodedTransform(contentDom,
                WebAdapter.DEFAULT_ENCODING, response.getOutputStream(), true);
    }
    
    /**
     * Lists the pending notifications to the user in XML format. If a parameter
     * named "clear" is included in the query string the list of notifications is
     * cleared.
     * 
     * @param request request for the content information
     * @param response response to the client
     * @throws ServletException if an error occurs
     * @throws IOException if an error occurs
     */
    private void doNotifications(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // Make sure adapter is set
        getAdapter();

        // Create the DOM and root node
        Document notificationDom = TransformerHelper.newDocument();
        Element root = notificationDom.createElement("notifications");
        notificationDom.appendChild(root);
        
        if(adapter != null) {
	        // Clear the notifications if a "clear" parameter was sent, otherwise build the notification XML
	        if(request.getQueryString() != null && request.getQueryString().contains("clear")) {
	        	adapter.clearNotifications();
	        } else {
		        // Add a node for each pending notification
		        List<UserNotification> notifications = adapter.getNotifications();
		        for(UserNotification n : notifications) {
		        	Element notificationNode = notificationDom.createElement("notification");
		        	notificationNode.appendChild(notificationDom.createTextNode(n.getTimestampedMessage()));
		        	root.appendChild(notificationNode);
		        }
	        }
        }

        // Transform the DOM and write it to the output stream
        response.setContentType("text/xml");
        TransformerHelper.encodedTransform(notificationDom,
                WebAdapter.DEFAULT_ENCODING, response.getOutputStream(), true);
    }

    /*
     * @see javax.servlet.http.HttpServlet#doGet(
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().endsWith("navigation.xml")) {
            setNoCacheHeaders(request, response);
            doNavigation(request, response);
        } else if (request.getRequestURI().endsWith("contents.xml")) {
            setNoCacheHeaders(request, response);
            doContents(request, response);
        } else if (request.getRequestURI().endsWith("notifications.xml")) {
            setNoCacheHeaders(request, response);
            doNotifications(request, response);
        }
    }

    /**
     * Responds with a simple XML document with navigation information.
     *  note: used by AbstractTag.renderPage
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if an error occurs
     * @throws IOException if an error occurs
     */
    private void doNavigation(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // get adapter from session
        getAdapter();

        // get information from session
        String currentCommunity = getCurrentCommunityId(request.getSession());

        // set attributes for the current navigation context
        nav.setAttribute("currentCommunityId", currentCommunity);
        String baseURL = "/up2p/";
        nav.setAttribute("baseURL", baseURL);
        nav.setAttribute("resourceURL", baseURL + "community/");
        nav.setAttribute("currentCommunityTitle", adapter.RMgetCommunityTitle(currentCommunity));
        nav.setAttribute("contextPath", request.getContextPath());

        // return the nav DOM
        response.setContentType("text/xml");
        TransformerHelper.encodedTransform(navDom, WebAdapter.DEFAULT_ENCODING,
                response.getOutputStream(), true);
    }

    /**
     * Sets headers in the response to prevent caching of the returned page.
     * 
     * @param request HTTP request made to the servlet
     * @param response HTTP response to be sent to the requester
     */
    private void setNoCacheHeaders(HttpServletRequest request,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.setHeader("Pragma", "no-cache");
    }
}