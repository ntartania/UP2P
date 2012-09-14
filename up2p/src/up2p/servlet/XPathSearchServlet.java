package up2p.servlet;

//import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import up2p.bridge.Bridge;
//import up2p.bridge.XPathQueryTranslation;
//import up2p.core.InvalidNetworkAdapter;
//import up2p.core.NetworkAdapterException;
import up2p.core.WebAdapter;
import up2p.repository.Repository;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;

/**
 * Accepts a complete XPath query expression, forwards it to the WebAdapter and
 * stores the results in the ServletContext (application scope).
 * 
 * <p>
 * Expects the following parameters:
 * <ul>
 * <li>{@link up2p.servlet.HttpParams#UP2P_XPATH_SEARCH
 * up2p.servlet.HttpParams.UP2P_XPATH_SEARCH} - a complete XPath expression that
 * will be used to search the network
 * <li>{@link up2p.servlet.HttpParams#UP2P_SEARCH_EXTENT
 * up2p.servlet.HttpParams.UP2P_SEARCH_EXTENT} - an optional parameter to
 * specify a local, network or combined search. The default behavior is to
 * search both the local database and the network.
 * </ul>
 * 
 * @see <a href="http://www.w3.org/TR/1999/REC-xpath-19991116">XML Path Language
 * (XPath) Version 1.0 </a>
 * @author Neal Arthorne
 * @version 1.0
 */
public class XPathSearchServlet extends AbstractWebAdapterServlet  {

    /**
	 * serial version (just to suppress warning)
	 */
	private static final long serialVersionUID = 1L;

	/** Default value for maximum number of results to return from a search. */
    public static final int DEFAULT_MAX_RESULTS = 100;

    private static final String MODE = "search";

    /**
     * Returns the extent of the search (local, network or both) for the current
     * search request in integer form.
     * 
     * @param request request made to this servlet
     * @return extent mode as defined in this class
     */
    private int getExtent(HttpServletRequest request) {
        int extent = HttpParams.UP2P_SEARCH_ALL;
        String extentStr = request.getParameter(HttpParams.UP2P_SEARCH_EXTENT);
        if (extentStr == null)
            return extent;
        try {
            extent = Integer.parseInt(extentStr);
        } catch (NumberFormatException e) {
            extent = HttpParams.UP2P_SEARCH_ALL;
        }
        return extent;
    }

    /**
     * Configures and returns the maximum number of search results that should
     * be returned for the current search.
     * 
     * @param request request made to the servlet
     * @return maximum results to return
     */
    private int getMaxResults(HttpServletRequest request) {
        // get configured maximum search results or use 100 as default
        int configuredMax = adapter.getConfigPropertyAsInt(
                WebAdapter.CONFIG_SEARCH_MAX_RESULTS, DEFAULT_MAX_RESULTS);
        int maxResults = configuredMax;

        // get max results from parameter if available
        String maxResultsStr = request
                .getParameter(HttpParams.UP2P_MAX_RESULTS);
        if (maxResultsStr != null) {
            try {
                maxResults = Integer.parseInt(maxResultsStr);
            } catch (NumberFormatException e) {
                // parameter was not a number
                LOG.warn("XPathSearchServlet maxResults was in invalid"
                        + "integer: " + maxResultsStr);
                maxResults = configuredMax;
            }
        }
        // set within range [1,configuredMax]
        if (maxResults < 1 || maxResults > configuredMax)
            maxResults = configuredMax;
        return maxResults;
    }
    
    /*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doXPathSearch(req, resp);
    }

    /*
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doXPathSearch(req, resp);
    }

    /**
     * Forwards an XPath query to the WebAdapter.
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doXPathSearch(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        // Check parameters
    	// Use the communityId specified in the request, or if none was specified fall
    	// back to the session active community.
    	
    	String communityId = null;
    	if(request.getParameter(HttpParams.UP2P_COMMUNITY) != null) {
    		communityId = request.getParameter(HttpParams.UP2P_COMMUNITY);
    		LOG.debug("XPathSearchServlet: Got community ID from request parameter: " + communityId);
    	}
        if(communityId == null) {
        	getCurrentCommunityId(request.getSession());
        	LOG.debug("XPathSearchServlet: Got community ID from user session: " + communityId);
        }
        
        if ((communityId == null) || communityId.length() == 0) {
            LOG.warn("XPathSearchServlet Request is missing the ID of the"
                    + " community.");
            writeError(request, response,
                    "<p><b>XPathSearchServlet:</b> Request is missing the ID of"
                            + " the community.</p>", MODE);
            return;
        }

        // initialize the user session
        initSession(request);

        // get the search query
        SearchQuery currentSearchQuery = null;
        String xPathExpression = request
                .getParameter(HttpParams.UP2P_XPATH_SEARCH);

        // if there is a search query in the user session, use it instead
        // of the compiled query
        if (request.getSession().getAttribute(CURRENT_SEARCH_ID) != null) {
            currentSearchQuery = (SearchQuery) request.getSession()
                    .getAttribute(CURRENT_SEARCH_ID);
            xPathExpression = currentSearchQuery.getQuery();
        }

        // if no XPath is given, reply with an error
        if (xPathExpression == null || xPathExpression.length() == 0) {
            writeOutput(request, response, "<p><b>XPathSearchServlet</b></p>"
                    + "<p><form action=\"XPathSearch\" method=\"post\">"
                    + "Enter an XPath query:<br>" + "<textarea name=\""
                    + HttpParams.UP2P_XPATH_SEARCH
                    + "\" cols=\"50\" rows=\"5\"></textarea><br>"
                    + "<input type=\"submit\" value=\"Search\"></form></p>");
            return;
        }

        // get maximum search results
        int maxResults = DEFAULT_MAX_RESULTS;
        if (currentSearchQuery != null)
            maxResults = currentSearchQuery.getMaxResults();
        else
            getMaxResults(request);

        // get extent of the search (i.e. where the search should be sent)
        int extent = getExtent(request);

        LOG.info("XPathSearchServlet Search request received in community "
                + communityId + " with query " + xPathExpression
                + " maxResults " + maxResults + " extent " + extent);
        
        // Create a search query
        SearchQuery query = null;
        if (currentSearchQuery != null)
            query = currentSearchQuery;
        else
            query = new SearchQuery(xPathExpression);
        query.setMaxResults(maxResults);
        
        // Forward to display results
        
        // Note: The user is forwarded to the display results before the search is actually launched
        // to ensure that the user does not hang at the search initiation page while the local database
        // query is executed.
        
        LOG.debug("---forward to display results---");
        StringBuffer redirect = new StringBuffer("/displayResults.jsp");
        
        // Append optional parameters
        redirect.append("?" + HttpParams.UP2P_ASYNCH_SEARCH + "=true");
        redirect.append("&" + HttpParams.UP2P_SEARCH_EXTENT + "=" + extent);
        
        String redirectURL = response.encodeURL(redirect.toString());
        RequestDispatcher rd = request.getRequestDispatcher(redirectURL);
        if (rd != null) {
            LOG.debug("XPathSearchServlet Redirecting to display results at "
                    + redirectURL);
            rd.forward(request, response);
        } else {
            LOG.error("XPathSearchServlet Error getting request dispatcher.");
        }
        
        // Dispatch search
        LOG.debug("---output search to the user WA ---");
        String qId = session.getId()+new Date().getTime();
        adapter.search(communityId, query, qId, extent);
        
        // Launch any provided direct resource Id resolution queries using the same community 
        // and query Id
        String[] resolveResIds = request.getParameterValues(HttpParams.UP2P_RID_SEARCH);
        if(resolveResIds != null) {
	        for(String rId : resolveResIds) {
	        	adapter.search(communityId, new SearchQuery(Repository.RESOLVE_URI + rId), qId, extent);
	        }
        }
    }
}