package up2p.servlet;

/**
 * Defines a number of HTTP request parameters used to pass information between
 * Servlets and JSPs.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class HttpParams {
	/**
	 * Parameter used to signal to the upload servlet that it should respond
	 * in XML format to the upload request, providing the success value of the
	 * upload, and either a resource ID for the published resource or an 
	 * error message describing why the upload failed.
	 */
	public static final String UP2P_FETCH_XML = "up2p:fetchxml";
	
	/**
	 * Parameters used by the ConfigServlet to set the maximum number of outgoing
	 * Gnutella connections for the peer.
	 */
	public static final String UP2P_SET_MAX_OUTGOING = "up2p:maxoutgoing";
	
	/**
	 * Parameters used by the ConfigServlet to set the maximum number of incoming
	 * Gnutella connections for the peer.
	 */
	public static final String UP2P_SET_MAX_INCOMING = "up2p:maxincoming";
	
	/**
	 * Parameter used to register a new relay mapping with the relay servlet.
	 * The request should also contain a relay identifier parameter, and the
	 * value of this parameter should provide the URL that the relay identifier
	 * should map to.
	 */
	public static final String UP2P_REGISTER_RELAY = "up2p:registerrelay";
	
	/**
	 * Parameter used by the relay servlet to map requests to a specific peer.
	 * This parameter should be included with registration requests, and with all
	 * requests which should be forwarded through the relay.
	 */
	public static final String UP2P_RELAY_IDENTIFIER = "up2p:relayidentifier";
	
	/**
	 * Parameter name used to supply a username for user authentication.
	 */
	public static final String UP2P_USERNAME = "up2p:username";
	
	/**
	 * Parameter name used to supply a password for user authentication.
	 * When generating a new user account, this should be used to pass the plain
	 * text password. In all other cases, this should pass the SHA-1 x1000 digest
	 * of the password and the provided salt.
	 */
	public static final String UP2P_PASSWORD = "up2p:password";
	
	/**
	 * Parameter name used to indicate to the UserServlet that the current session
	 * should be invalidated (logged out)
	 */
	public static final String UP2P_LOGOUT = "up2p:logout";
	
	/**
	 * Used in the create servlet, this parameter specifies that it's associated value is
	 * an Xml string that represents the entire document model to be instantiated. All
	 * other passed create parameters are ignored.
	 */
	public static final String UP2P_RAW_XML = "up2p:rawxml";
	
	/**
	 * Parameter name for specifying that a request came from a client side
	 * XMLHttpRequest object. These requests typically do not need to be redirected
	 * to viewing pages as they will not be seen by the user.
	 */
	public static final String UP2P_XMLHTTP = "up2p:xmlhttp";
	
    /**
     * Parameter name for specifying an asynchronous loading of search results.
     * If true, the search results are received asynchronously from the Network
     * Adapter and therefore the search results page will be refreshed.
     * Otherwise the search results page will be static.
     */
    public static final String UP2P_ASYNCH_SEARCH = "up2p:asynch";

    /** Parameter name for a bridge. */
    public static final String UP2P_BRIDGE = "up2p:bridge";

    /** Parameter name identifying a U-P2P community (up2p:community). */
    public static final String UP2P_COMMUNITY = "up2p:community";

    /** Parameter name identifying an upload file name (up2p:filename). */
    public static final String UP2P_FILENAME = "up2p:filename";

    /**
     * Parameter name for the maximum number of results that a search should
     * return (up2p:maxResults).
     */
    public static final String UP2P_MAX_RESULTS = "up2p:maxResults";

    /**
     * Parameter name for the offset into the result set to retrieve when
     * displaying results(up2p:offset). Default is zero or no offset.
     */
    public static final String UP2P_OFFSET = "up2p:offset";

    /**
     * Parameter name for the id of a resource (up2p:resource).
     */
    public static final String UP2P_RESOURCE = "up2p:resource";
    
    /**
     * Parameter name for the id of a resource title (up2p:resourcetitle).
     */
    public static final String UP2P_RESOURCE_TITLE = "up2p:resourcetitle";

    /**
     * Parameter name for the numbered search result to download using the
     * Network Adapter for the community (up2p:result).
     */
    public static final String UP2P_RESULT = "up2p:result";
    
    /**
     * Parameter name to signify that an uploaded file is a batch resource file
     * (up2p:batch)
     */
    public static final String UP2P_BATCH = "up2p:batch";

    /**
     * Parameter passed to XPathSearchServlet to signify that the search should
     * be for the local database, the network or both (up2p:extent). Its value
     * should be one of
     * {@link up2p.servlet.HttpParams#UP2P_SEARCH_ALL UP2P_SEARCH_ALL},
     * {@link up2p.servlet.HttpParams#UP2P_SEARCH_LOCAL UP2P_SEARCH_LOCAL}or
     * {@link up2p.servlet.HttpParams#UP2P_SEARCH_NETWORK UP2P_SEARCH_NETWORK}.
     */
    public static final String UP2P_SEARCH_EXTENT = "up2p:extent";

    /** Search both local database and the network. */
    public static final int UP2P_SEARCH_ALL = 0;
    
    /** Only search the local database. */
    public static final int UP2P_SEARCH_LOCAL = 1;

    /** Only search the network. */
    public static final int UP2P_SEARCH_NETWORK = 2;

    /**
     * Parameter name for a complete XPath expression search (up2p:XPathSearch).
     */
    public static final String UP2P_XPATH_SEARCH = "up2p:XPathSearch";
    
    /** 
     * Parameter name for resource Ids passed to the SearchServlet to resolve and
     * include in search results.
     */
    public static final String UP2P_RID_SEARCH = "up2p:residsearch";

    /**
     * Parameter name for an XQuery search (up2p:xquery).
     */
    public static final String UP2P_XQUERY_SEARCH = "up2p:xquery";

    /**
     * Parameter name for specifying a remote peer id (IP address and port).
     * This is primarily used in the retrieve servlet for launching download
     * requests.
     */
	public static final String UP2P_PEERID = "up2p:peerid";
	
	/** Used by the GraphQueryServlet to specify a recursive query. */
	public static final String UP2P_RECURSIVE = "up2p:searchrecursive";
	
	/** Used by the GraphQueryServlet to specify a transitive query. */
	public static final String UP2P_TRANSITIVE = "up2p:searchtransitive";
	
	/**
	 * Used to specify a request which is a Gnutella PUSH file transfer. These
	 * are used when a direct outgoing file transfer fails, and the remote
	 * node must initiate the file transfer connection (usually due to firewalls)
	 */
	public static final String UP2P_PUSH = "up2p:pushupload";
	
	/**
	 * Used by the Upload servlet, this parameter specifies that the uploaded
	 * resource is an edited version of the resource with the specified resource
	 * id (in the same community as the new upload). The attachments for the specified
	 * resource will be copied to the temporary directory of the new upload and published
	 * (unless a new file with the same name is explicitly uploaded to replace the old).
	 */
	public static final String UP2P_EDIT_RESOURCE = "up2p:editresource";
	
	/**
	 * Special parameter used by the retrieve servlet to force a PUSH message to be
	 * sent (skips the standard download procedure). This is primarily used for testing.
	 */
	public static final String UP2P_PUSH_TEST = "up2p:pushtest";
	
	/**
	 * Flag read by the Search servlet to indicate a search that should be performed as an
	 * exact string match rather than a case insensitive substring match (the default behaviour).
	 * Note that this setting applies to an entire query, and not a specific XPath in the query.
	 */
	public static final String UP2P_EXACT_STRING_MATCH = "up2p:exactsearchmatch";
	
	/**
	 * Parameter used by the ConfigServlet to indiciate that a new host should be
	 * added to the host cache. The value of the parameter should be the IP / port
	 * of the host to add in the format
	 * "<IP Address>:<Port>"
	 * 
	 * ex:
	 * up2p:addhost=123.234.123.234:6346
	 */
	public static final String UP2P_ADD_HOST = "up2p:addhost";
	
	/**
	 * Parameter used by the ConfigServlet to indiciate that a  host should be
	 * removed from the host cache. The value of the parameter should be the IP / port
	 * of the host to add in the format
	 * "<IP Address>:<Port>"
	 * 
	 * ex:
	 * up2p:removehost=123.234.123.234:6346
	 */
	public static final String UP2P_REMOVE_HOST = "up2p:removehost";
	
	/**
	 * Parameter used by the ConfigServlet to indicate that an active connection
	 * should be dropped. The value of the parameter should be the listen string
	 * of the connection to drop.
	 * 
	 * ex:
	 * up2p:dropconnection=inm-04.sce.carleton.ca:6346
	 */
	public static final String UP2P_DROP_CONNECTION ="up2p:dropconnection";
	
	/**
	 * Flag used by the SearchServlet to indicate that search results should be retrieved
	 * without any custom XSL processing. This is useful for queries where the result
	 * is intended for machine consumption.
	 */
	public static final String UP2P_SKIP_XSL = "up2p:skipxsl";
	
	/**
	 * Flag used by the SearchServlet to force a GET request to launch a search
	 * query (rather than returning results). This is primarily used to allow
	 * easy launching of queries from the browser address bar (since POST requests
	 * can not be as easily generated). 
	 */
	public static final String UP2P_LAUNCH_SEARCH = "up2p:launchsearch";
	
	/**
	 * Parameter name used to specify which operator should be used to combine
	 * search terms when submitting a query to the SearchServlet. Valid values
	 * are "AND" and "OR". If this parameter is not included in the request,
	 * the default is "AND".
	 */
	public static final String UP2P_SEARCH_OPERATOR = "up2p:searchoperator";
	
	/**
	 * Flag used to specify to the search and retrieve servlets that a request should not
	 * change the current community ID. This is typically used by communities which
	 * run asynchronous queries on other communities, but will handle the rendering
	 * of results client side.
	 */
	public static final String UP2P_BACKGROUND_REQUEST = "up2p:backgroundrequest";
}