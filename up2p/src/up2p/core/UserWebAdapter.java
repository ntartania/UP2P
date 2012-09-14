package up2p.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Vector;

//import javax.servlet.RequestDispatcher;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
//import org.w3c.dom.Document;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmldb.api.base.XMLDBException;

//import up2p.repository.DatabaseAdapter;
//import up2p.repository.Repository;
//import up2p.repository.ResourceEntry;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.servlet.DownloadService;
import up2p.servlet.HttpParams;
import up2p.xml.TransformerHelper;

import up2p.xml.DOMTransformer;
//import up2p.util.NetUtil;

/**
 * This class acts as an adapter to the DefaultWebAdapter.
 * The goal is to decouple the user interface from the
 * web adapter as much as possible.
 * 
 * It contains only the methods which have a specific jsp/servlet-oriented implementation
 * acts as proxy for everything else
 * 
 * @author babak
 * @author alan
 *
 */
public class UserWebAdapter  {
	
	////////////////////////////////////////////////////////////
	//TODO: place here the configuration strings. 
	/** Location of default display stylesheet. */
	public static final String CONFIG_DEFAULT_DISPLAY = "up2p.defaultDisplay.location";

	/** Location of default search stylesheet. */
	public static final String CONFIG_DEFAULT_SEARCH = "up2p.defaultSearch.location";
	
	/** Location of the default home page (uses XSL to generate a homepage from the community schema) */
	public static final String CONFIG_DEFAULT_HOME = "up2p.defaultHome.location";

	private static final String CONFIG_DEFAULT_COMMUNITY_DISPLAY = "up2p.defaultCommunityDisplay.location";

	private static final String CONFIG_DEFAULT_RESULTS_DISPLAY = "up2p.defaultSearchResultsDisplay.location";
	////////////////////////////////////////////////////////////
	
	private DefaultWebAdapter defWebAdapter;
	private String rootpath;
	//stores the id of the latest repository search
	private String currentSearchId; 
	
	/** Logger used by this adapter. */
    public static Logger LOG;
	
	public UserWebAdapter(String up2pPath, String urlPrefix, int configPort)  {
		try{
			DefaultWebAdapter defwa = new DefaultWebAdapter(up2pPath, urlPrefix, configPort);
			setDefWebAdapter(defwa);
			//defWebAdapter.setHost(NetUtil.getFirstNonLoopbackAddress().getHostAddress());
		}
		catch (IOException e) {
			DefaultWebAdapter.LOG.error("IO Exception creating the default Web Adapter");
		}
		
		rootpath = up2pPath;
		// load the logger
        LOG = Logger.getLogger(WebAdapter.LOGGER);
        
	    LOG.info("UserWebAdapter initialized");
	}

	/**Resource Manager Methods replacing indirect calls through get RM from servlets, etc.
	 * ============================================================================
	 * 
	 */
	public String RMgetCreateLocation(String cid){
		return defWebAdapter.getCreateLocation(cid);
	}
	
	public String RMgetSchemaLocation(String communityId) {
		return defWebAdapter.getSchemaLocation(communityId);
	}
	
	/**
	 * @param cid	The community ID of the community being displayed
	 * @return	Returns the location to the HTML home page for the community, or
	 * null if none was specified.
	 */
	public String RMgetCommunityHomeLocation(String cid){
		return defWebAdapter.getCommunityHomeLocation(cid);
	}
	
	/**
	 * @param cid	The community ID of the community being displayed
	 * @return	Returns an iterator of stylesheets that should be included
	 * in the HTML home page.
	 */
	public Iterator<String> RMgetCommunityHomeStylesheet(String cid){
		return defWebAdapter.getCommunityHomeStylesheet(cid);
	}
	
	public Iterator<String> RMgetCommunitySearchStylesheet(String communityId) {
		return defWebAdapter.getCommunitySearchStylesheet(communityId);
	}
	
	public Iterator<String> RMgetCommunitySearchResultsStylesheet(String communityId) {
		return defWebAdapter.getCommunitySearchStylesheet(communityId);
	}
	
	public Iterator<String> RMgetCommunitySearchResultsJavascript(String communityId) {
		return defWebAdapter.getCommunitySearchResultsJavascript(communityId);
	}
	
	public Iterator<String> RMgetCommunityCreateStylesheet(String communityId) {
		return defWebAdapter.getCommunityCreateStylesheet(communityId);
	}
	
	public Iterator<String> RMgetCommunityDisplayStylesheet(String communityId) {
		return defWebAdapter.getCommunityDisplayStylesheet(communityId);
	}
	
	public String RMgetCommunitySearchLocation(String communityId) {
		return defWebAdapter.getSearchLocation(communityId);
	}
	
	public String RMgetCommunityHeaderLogo(String communityId) {
		return defWebAdapter.getCommunityHeaderLogo(communityId);
	}
	
	public boolean RMisCommunity(String CommunityId){
		return defWebAdapter.isResourceLocal(getRootCommunityId(), CommunityId);
	}
	
	/*
	 * called by:
	 * view.jsp
	 * update 2010-04-15 : not any more ? removing it for a try 
	 * /
	public int RMgetResourceCount(String communityId){
		return defWebAdapter.RMgetResourceCount(communityId);
	}*/
	/*called by:
	 * header.jsp
	 * displayResults.jsp
	 */
	public String RMgetCommunityTitle(String communityId) {
		return defWebAdapter.getCommunityTitle(communityId);
	}

	
	/*
	 * called by:
	 * overwrite.jsp
	 *  view.jsp
	 */
	public String RMgetResourceTitle(String resourceId, String communityId) {
		return defWebAdapter.getResourceTitle(resourceId, communityId);
	}
	/*should be ok to remove
	public String RMgetCommunityName(String communityId) {
		// TODO Auto-generated method stub
		return defWebAdapter.getCommunityName(communityId);
	}*/
	
	/** WA methods
	 * ==========================================================================
	 * 
	 */
	
	public void setDefWebAdapter(DefaultWebAdapter defWebAdapter) {
		this.defWebAdapter = defWebAdapter;
	}


	public DefaultWebAdapter getDefWebAdapter() {
		return defWebAdapter;
	}
	
	/**
     * @return	The list of notifications currently pending for the user.
     */
    public List<UserNotification> getNotifications() {
    	return defWebAdapter.getNotifications();
    }
    
    /**
     * Resets the list of notifications (should be used once the user acknowledges
     * reading the messages).
     */
    public void clearNotifications() {
    	defWebAdapter.clearNotifications();
    }
    
    /**
     * Adds a new notification to the list of pending notifications.
     * @param notification	The new notification to add.
     */
    public void addNotification(String notification) {
    	defWebAdapter.addNotification(notification);
    }

    
	//called by: view.jsp
	public Iterator<String> browse(String communityId) {
		return defWebAdapter.browse(communityId).iterator();
	}
	//called by: header.jsp
	public Iterator<String> browseCommunities() {
		return defWebAdapter.browseCommunities().iterator();
	}
	/*
     * @see up2p.core.WebAdapter#displayResource(String, String, java.io.Writer)
     */
    public void displayResource(String resourceId, String communityId,
            HttpServletRequest request, Writer out) throws IOException  {
 
    	LOG.debug("DefWebAdapter::displayResource: entering");
        
        /*/ get the URL to the resource
        String resourceURL = "http://" + getHost() + ":" + getPort()
                + "/up2p/community/" + communityId + "/" + resourceId
                + "?filter=http";*/

        // get any parameters to pass to the stylesheet
        Enumeration<String> en = request.getParameterNames();
        Map<String,String> paramTable = new HashMap<String,String>();
        // put the URI to the resource into the parameters sent to the
        // stylesheet
        paramTable.put("up2p-link", request.getContextPath()
                + "/view.jsp?up2p:community=" + communityId + "&up2p:resource="
                + resourceId);
        paramTable.put("up2p-community-id", communityId);
        paramTable.put("up2p-resource-id", resourceId);
        File dir = new File(DefaultWebAdapter.getStorageDirectory(communityId));
        paramTable.put("up2p-community-dir", dir.toURI().toURL().toExternalForm());
        paramTable.put("up2p-root-community-id", getRootCommunityId());
        paramTable.put("up2p-base-url", "/" + getUrlPrefix() + "/");
        
       // paramTable.put("up2p-filename", resourceFile.getName());
        String resTitle = defWebAdapter.getResourceTitle(resourceId,
                communityId);
        /*if (resTitle == null) {
            // set resource title to file name
            resTitle = resourceFile.getName();
        }*/
        paramTable.put("up2p-resource-title", resTitle);
        // any parameters passed in the request that aren't up2p-related
        // are also passed to the stylesheet
        while (en.hasMoreElements()) {
            String paramName = (String) en.nextElement();
            if (!paramName.startsWith("up2p:")) {
                paramTable.put(paramName, request.getParameter(paramName));
            }
        }
        // do the transform
        File displayXSL= null;
		try {
			displayXSL = getStyleSheet(communityId, "DISPLAY");
		} catch (IOException e1) {
			LOG.error("UserWA: displayresource"+ e1);
		}

        Node docNode = defWebAdapter.getResourceAsDOM(communityId, resourceId);
        
        if (docNode instanceof Document){
        	Document d= (Document) docNode;
        	
        	if(d.getDocumentElement()==null){
        		LOG.error("DEFWA: DisplayResource:: Error getting document: not found");
        	}
			docNode= TransformerHelper.attachmentReplace(d, "community/" + communityId + "/" + resourceId + "/");
			LOG.debug("UserWA::displayResource: transformed attachments!");
        } else {
        	LOG.debug("UserWA::displayResource: could not transform attachments! docNode not a Document:"+ docNode.getClass());
        }
        
        try{
        	LOG.debug("to be transformed: "+ docNode.toString()+ " class: "+ docNode.getClass());
        	LOG.debug("stylesheet:"+ displayXSL.getPath());
        	Node transformResult;
        	
        	transformResult = TransformerHelper.transform(docNode, displayXSL,paramTable);
        	
        	
        	if (transformResult instanceof Document){ //should be the case 
        		LOG.debug(((Document)transformResult).getDocumentElement().toString());
        		TransformerHelper.encodedTransform(((Document)transformResult).getDocumentElement(), "UTF-8", out, true);//.transform(resourceURL, displayXSL, out, paramTable);
        		
        	}
        	else { 
        		out.write("Error in UserWebAdapter.displayResource: result of XSL transform is not a document, it's a " + transformResult.getClass().getCanonicalName());
        		out.flush(); //output error msg to screen
        		LOG.error("Error in UserWebAdapter.displayResource: result of XSL transform is not a document:");
        	}
        	
        } catch(IOException e){
        	LOG.debug("UserWA::displayResource:Error: "+ e.toString());
        	e.printStackTrace();
        	//throw(e);
        } catch(NullPointerException e2){
        	LOG.debug("UserWA::displayResource:Error: "+ e2.toString());
        	e2.printStackTrace();
        	DOMTransformer.prettyPrint((Document)docNode);
    	
        }


    }

    /*
     * @see up2p.core.WebAdapter#displayResource(String, String, java.io.Writer)
     */
    public void displayCommunity(String communityId,
            HttpServletRequest request, Writer out) throws IOException  {
 
    	LOG.debug("DefWebAdapter::displayCommunity: entering");
              

        // get any parameters to pass to the stylesheet
        Enumeration<String> en = request.getParameterNames();
        Map<String,String> paramTable = new HashMap<String,String>();
        // put the URI to the resource into the parameters sent to the
        // stylesheet
        /*paramTable.put("up2p-link", request.getContextPath()
                + "/view.jsp?up2p:community=" + communityId + "&up2p:resource="
                + resourceId);*/
        paramTable.put("up2p-community-id", communityId);
        //paramTable.put("up2p-resource-id", resourceId);
        File dir = new File(DefaultWebAdapter.getStorageDirectory(communityId));
        paramTable.put("up2p-community-dir", dir.toURI().toURL().toExternalForm());
        paramTable.put("up2p-root-community-id", getRootCommunityId());
        paramTable.put("up2p-base-url", "/" + getUrlPrefix() + "/");
        
       // paramTable.put("up2p-filename", resourceFile.getName());
        String resTitle = defWebAdapter.getResourceTitle(communityId, getRootCommunityId());
        /*if (resTitle == null) {
            // set resource title to file name
            resTitle = resourceFile.getName();
        }*/
        paramTable.put("up2p-community-title", resTitle);
        // any parameters passed in the request that aren't up2p-related
        // are also passed to the stylesheet
        while (en.hasMoreElements()) {
            String paramName = (String) en.nextElement();
            if (!paramName.startsWith("up2p:")) {
                paramTable.put(paramName, request.getParameter(paramName));
            }
        }
        

        //get stylesheet
        File displayXSL= null;
		try {
			displayXSL = getStyleSheet(communityId, "COMMUNITY");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//if we're using the default stylesheet we don't want to get all the XML inside the resources
		boolean populate = !(displayXSL.getName().equals(defWebAdapter.config.getProperty(CONFIG_DEFAULT_COMMUNITY_DISPLAY)));
		if (populate)
			LOG.debug("UserWA::displayCommunity: display XSL is not the default, so getting full XML resources");
		else
			LOG.debug("UserWA::displayCommunity: using default XSL.");
		//build community DOM
        Node docNode = getCommunityContentAsDOM(communityId, populate);
        
	     // do the transform
		try{
        	LOG.debug("to be transformed: "+ docNode.toString()+ " class: "+ docNode.getClass());
        	LOG.debug("stylesheet:"+ displayXSL.getPath());
        	Node transformResult;
        	
        	transformResult = TransformerHelper.transform(docNode, displayXSL, paramTable);

        	if (transformResult instanceof Document){ //should be the case 
        		LOG.debug(((Document)transformResult).getDocumentElement().toString());
        		TransformerHelper.encodedTransform(((Document)transformResult).getDocumentElement(), "UTF-8", out, true);
        	} else { 
        		out.write("Error in UserWebAdapter.displayResource: result of XSL transform is not a document, it's a " + transformResult.getClass().getCanonicalName());
        		out.flush(); // Output error msg to screen
        		LOG.error("Error in UserWebAdapter.displayResource: result of XSL transform is not a document:");
        	}
        	
        } catch(IOException e){
        	LOG.debug("UserWA::displayResource:Error: "+ e.toString());
        	e.printStackTrace();
        	//throw(e);
        } catch(NullPointerException e2){
        	LOG.debug("UserWA::displayResource:Error: "+ e2.toString());
        	e2.printStackTrace();
        	DOMTransformer.prettyPrint((Document)docNode);
    	
        }
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
	public Document getCommunityContentAsDOM(String communityId, boolean populate) {
		Document communityDom = (Document)defWebAdapter.getCommunityAsDOM(communityId, populate);
		if(communityDom != null) {
			communityDom.getDocumentElement().setAttribute("id", communityId);
    		communityDom.getDocumentElement().setAttribute("title", RMgetCommunityTitle(communityId));
		}
    	return communityDom;
	}


	/**
	 * construct a DOM tree representing the community Contents
	 * @param communityId
	 * @return
	 */
	public Document getCommunitiesTreeAsDOM() {
		Document contentDom = TransformerHelper.newDocument();
        Element contents = contentDom.createElement("contents");
        contentDom.appendChild(contents);

        // create community nodes with resource children
        Iterator<String> comIt = browseCommunities();
        while (comIt.hasNext()) {
            String comId = comIt.next();
            Document communityDOM= getCommunityContentAsDOM(comId, true);
            //adopt the node from the other document, and add it to the current tree
            contents.appendChild(contentDom.adoptNode(communityDOM.getDocumentElement()));
        }
        
        return contentDom;
	}
	
	/**
	 * @param communityId	The community ID of the resource to fetch
	 * @param resourceId	The resource ID of the resource to fetch
	 * @return	The DOM of the specified resource, or null if the resource could
	 * 			not be found.
	 */
	public Node getResourceAsDOM(String communityId, String resourceId) {
		return defWebAdapter.getResourceAsDOM(communityId, resourceId);
	}

	/**
	 * Get stylesheets (xsl transform files) to display XML resources, such as a  single document, a collection of documents, search results 
	 * @param communityId the community for which the stylesheet is needed
	 * @param type which stylesheet : DISPLAY (to display a single document), RESULT (to display search results), COMMUNITY (to display a collection of resources).
	 * @return the relevant XSL file: the DISPLAY/RESULT/COMMUNITY xsl associated with the community, or the default one if the community one was not found
	 * @throws IOException
	 */
	public File getStyleSheet(String communityId, String type) throws IOException{
		String location = null;

		 if(type.equals("DISPLAY"))// to display resource
		{
			location = defWebAdapter.getDisplayLocation(communityId);
			LOG.debug("getdisplayLocation returned:"+location+ "|");
			if (location == null || location.equals("")) {
				// try to use default display stylesheet
				LOG.debug("using default diplay XSL");
				location = getRealFile(defWebAdapter.config.getProperty(CONFIG_DEFAULT_DISPLAY));
			}
		} else  if(type.equals("COMMUNITY")){
			location = defWebAdapter.getCommunityDisplayLocation(communityId);
			LOG.debug("UserWA:getCommunitydisplayLocation returned:"+location+ "|");
			if (location == null || location.equals("")) {
				// try to use default display stylesheet
				location =defWebAdapter.config.getProperty(CONFIG_DEFAULT_COMMUNITY_DISPLAY);
				LOG.debug("UserWA: default community stylesheet:"+location);
				location = getRealFile(location);

			}
			LOG.debug("UserWA: display Community: using diplay XSL at"+ location );
		} else  if(type.equals("RESULT")) // to display search results
		{
			location = defWebAdapter.getCommunityResultsLocation(communityId);
		
			LOG.debug("UserWA: searchresults display:"+location+ "|");
			if (location == null || location.equals("")) {
				// No transformation is required on the server side if a custom
				// results stylesheet is not provided, just return null in this case
				LOG.debug("UserWA: No search result XSLT was specified, "
						+ "returning null for stylesheet location.");
				return null;
			}
			LOG.debug("UserWA: display Community: using diplay XSL at"+ location );
		}
		else {	
			throw new IOException("DEFWA::getStylesheet:unknown type:"+type);
		}

		if (location == null) {
			// no default stylesheet can be found
			throw new IOException("Display unspecified and default result "
					+ "stylesheet unavailable.");
		}

		// adjust the location if necessary
		//-------------
		   File xslFile = null;

		   try{
			   xslFile = getAttachmentFile(getRootCommunityId(), communityId, location);
		   }

		   catch (FileNotFoundException e){ //what's in here should not be invoked -- there are problems
			   LOG.info(e);

			   if (type.equals("RESULT"))
			   {
				   location = "file:default-result.xsl";
			   }else if (type.equals("DISPLAY")){
				   location = "file:default-resource.xsl";
			   }else if (type.equals("COMMUNITY")){
				   location = "file:default-community-display.xsl";
			   }

			   xslFile = getAttachmentFile(getRootCommunityId(), communityId, location); 

			   if (!xslFile.exists()) {
				   throw new IOException(
						   "The default XSL stylesheet is not found at " +
						   xslFile.getAbsolutePath());
			   }	
		   }
		   return xslFile;
	   }
	public String getConfigProperty(String propertyName, String defaultValue) {
		return defWebAdapter.getConfigProperty(propertyName, defaultValue);
	}

	public int getConfigPropertyAsInt(String propertyName, int defaultValue) {
		return defWebAdapter.getConfigPropertyAsInt(propertyName, defaultValue);
	}

	/**
	 * getInitialized: returns the initialization status... after some changes here we'll consider the system to be always fully initialized. (called by init.jsp)
	 */
	public int getInitialized() {
		return 4;//getDefWebAdapter().getInitialized();
	}

	public File getAttachmentFile(String comId, String rid, String url) throws FileNotFoundException{
		return DefaultWebAdapter.getAttachmentFile(comId, rid, url);
	}
	
	public int getPort() {
		return getDefWebAdapter().getPort();
	}
    

	/*called by:
	 * view.jsp
	 * init.jsp
	 * header.jsp
	 * community-create.jsp
	 */
	public String getRootCommunityId() {
		return getDefWebAdapter().getRootCommunityId();
	}
	
	/**
	 * @return	The URL prefix for this instance of U-P2P.
     * 			Ex. If U-P2P is available at "http://hostname:8080/up2p/",
     * 			this will return "up2p"
	 */
	public String getUrlPrefix() {
		return getDefWebAdapter().getUrlPrefix();
	}
	
	/**
    * KLUDGE: Made this static to avoid major modifications throughout
    * the WebAdapters, but logically it really shouldn't be
    * 
    * @return	The absolute path for the root directory 
    * 			for this instance of U-P2P.
    */
	public String getRootPath() {
		return getDefWebAdapter().getRootPath();
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
	public String publish(String communityId, File resourceFile, File attachmentDir
			) throws SAXParseException, SAXException,
			IOException, DuplicateResourceException, 
			NetworkAdapterException, ResourceNotFoundException {
		return getDefWebAdapter().publish(communityId, resourceFile, attachmentDir);
	}

	
	/*
	 * called by: view.jsp
	 */
	public void remove(String communityId, String resourceId)
			{
		getDefWebAdapter().remove(communityId, resourceId);
	}

	////////////////// new method: tries downloading by different means if possible. Should update its own status in a table.
	public String asyncDownload(String comid, String resid){
		
		return defWebAdapter.asyncDownload(comid, resid);
	}
	

	/**
	 *  Download a resource from a specific peer
	 * @param comId community of the resource
	 * @param resId resource Id
	 * @param filename name of the XML file containing the resource. Not provided in the case of a URI dereferencing.
	 * @param peerid identifier of the peer (normally IP:port of that peer's tomcat server) If not provided (URI dereferencing), then a search is output.
	 * @return a redirect URL for further processing (call of upload servlet, view)
	 * @throws ResourceNotFoundException 
	 */
	public String retrieve(String comId, String resId, String filename, String peerid) throws ResourceNotFoundException {
		
		File resourceFile;
		
		if (filename != null && peerid != null) {//this is a download request for a file from a specific peer
			try {

				resourceFile = getDefWebAdapter().retrieveFromNetwork(comId, resId, filename, peerid);

				if(resourceFile != null) {
					// success
					return "/upload?" + HttpParams.UP2P_COMMUNITY + "="
					+ comId + "&" + HttpParams.UP2P_FILENAME
					+ "=" 
					+ URLEncoder.encode(resourceFile.getName(), "UTF-8");
				} else {
					throw new ResourceNotFoundException(resId);
				}
			} catch (NetworkAdapterException e) {
				// TODO Auto-generated catch block
				// return "/errorPage.jsp?error.msg=NetworkAdapterException";
				throw new ResourceNotFoundException(resId);
			} catch (UnsupportedEncodingException e) {
				return "/errorPage.jsp?error.msg=UnsupportedEncodingException";
			}

		} else{
			//check if we  have it locally
			//if yes, redirect to view with cid, rid.
			if (isResourceLocal(comId, resId)){
				return "/view.jsp?" + HttpParams.UP2P_COMMUNITY + "="
		        + comId + "&" + HttpParams.UP2P_RESOURCE
		        + "=" 
		        + resId;
			}
			
		else {
			//for now: just send search and go to Search Results page.
			//generate a (hopefully unique) identifier for the query. Not essential that it be absolutely crazy unique... 
			String qid = comId.substring(2,8)+resId.substring(2, 8)+System.currentTimeMillis();
			//if we don't even have the community: search for the community, else search for the resource
			if (isResourceLocal(getRootCommunityId(),comId)){ 
				SearchQuery query = new SearchQuery("ResourceId="+resId);
				search(comId, query, qid, HttpParams.UP2P_SEARCH_ALL);
			} else {
				SearchQuery query = new SearchQuery("ResourceId="+comId);
				search(getRootCommunityId(), query, qid, HttpParams.UP2P_SEARCH_ALL);
			}
			//search results page in a "resolve URI" mode
			return "/ResolveURI.jsp";
		}
		/*
		1] check if we have the community 
		 if no:
		 - ask if we want to download the relevant community
		 	- yes : download community / install by shortcutting new community page
		 			jump to [2]*/
		 				/*
		 	- no : cancel and do nothing
		 if yes : [2]
		2] search for resource in community
		 	- found: redirect to upload the new resource or show search results ?
		 	- not found: search results page 
		
			
			
			 
			 */
			//return null;
		}
	}

	
	/**
	 * Determines whether the input resource is locally stored in the referenced community.
     * The local cache is not reset before querying.
     * @param comId Community identifier
     * @param rId resource Identifier
     * @return true is the resource is found locally, false otherwise
     */
	public boolean isResourceLocal(String comId, String rId) {
		return defWebAdapter.isResourceLocal(comId, rId);
	}
	
   /**
    * Determines whether the input resource is locally stored in the referenced community
    * @param comId Community identifier
    * @param rId resource Identifier
    * @param clearCache Determines whether the local cache should be reset before querying
    * @return true is the resource is found locally, false otherwise
    */
   public boolean isResourceLocal(String comId, String rId, boolean clearCache) {
	   return defWebAdapter.isResourceLocal(comId, rId, clearCache);
   }
	
	/**
	 * used to set a search id in the context of a complex query
	 * After this is called, the search results page will get results with the given queryId
	 */
	public void setCurrentSearch(String newid, boolean clear){
		if (clear && currentSearchId!=null)
			defWebAdapter.clearSearchResults(currentSearchId);
		currentSearchId = newid;
		LOG.info("UserWA:current search Id is now:"+newid);
	}
	
	/**
	 *  search
	 * @param communityId
	 * @param query
	 * @param qid
	 * @param extent	Determines what the scope of the search should be.
	 * 					This value should be one of HttpParams.UP2P_SEARCH_ALL,
	 * 					UP2P_SEARCH_NETWORK, or UP2P_SEARCH_LOCAL
	 */
	public void search(String communityId,
			SearchQuery query, String qid, int extent)
			 {
		defWebAdapter.search(communityId, query, qid, extent);
		if (currentSearchId!=null)
			defWebAdapter.clearSearchResults(currentSearchId);
		currentSearchId = qid; //store the id of the latest search
		LOG.info("UserWA:current search Id is now:"+qid);
	}

	
	
	public void setHost(String host) {
		getDefWebAdapter().setHost(host);
	}

	public void setPort(int port) {
		getDefWebAdapter().setPort(port);
	}

	public void shutdown() {
		getDefWebAdapter().shutdown();
	}
	/**
     * Translates from a relative path to a real path using the root directory
     * of the up2p application as a base.
     * 
     * @param filePath path of a file relative to the webserver context
     * @return the full path to the file
     */
    protected String getRealFile(String filePath) {
        return rootpath + File.separator + filePath;
    }
    
	public DownloadService getDownloadService() {
		// TODO Auto-generated method stub
		return getDefWebAdapter().getDownloadService();
	}
	
	/**
	 * called by:
	 * SearchServlet
	 * 
	 * Stores the last query run by the user
	 * @param lastQuery	A map of values keyed by XPath representing the
	 * 					last query
	 */
	public void setLastQuery(Map<String, String> lastQuery) {
		getDefWebAdapter().setLastQuery(lastQuery);
	}
	
	/**
	 * called by:
	 * resultTable.jsp
	 * 
	 * @return	A map of values keyed by XPaths representing the last query
	 * 			initiated by the user.
	 */
	public Map<String, String> getLastQuery() {
		return getDefWebAdapter().getLastQuery();
	}

	/**
	 * called by: 
	 * displayResults.jsp
	 * view.jsp
	 * 
	 * 
	 * @param communityId
	 * @param resourceId
	 * @return
	 * /
	public File getLocalFile(String communityId, String resourceId) {
		// TODO Auto-generated method stub
		return getDefWebAdapter().getLocalFile(communityId, resourceId);
	}*/

	public String getStorageDirectory(String communityId) {
		
		return DefaultWebAdapter.getStorageDirectory(communityId);
	}
	
	/**
     * Defines the Storage directory to be used for the attachments of a resource.
     * @param communityId	The community the resource belongs to
     * @param resourceId	The id of the resource the attachments belong to	
     */ 
	public String getAttachmentStorageDirectory(String communityId, String resourceId) {
		return DefaultWebAdapter.getAttachmentStorageDirectory(communityId, resourceId);
	}

	/**
	 *  to get the responses from the latest user search.
	 *  The query Id is stored in this object.
	 * @return
	 */
	public SearchResponse[] getSearchResults(){
		if (currentSearchId==null) 
			return new SearchResponse[0]; //no search done since startup of the application
		else
			return defWebAdapter.getSearchResults(currentSearchId);
	}
	
	public List<SearchResponse> getSearchResults(String qid){
		SearchResponse[] sr=defWebAdapter.getSearchResults(qid);
		return Arrays.asList(sr);
	}
	

	/**
	 * Sends a push message for the specified resource. This requests that the
	 * remote peer initiate a connection when an outgoing connection cannot be made.
	 * Clears active search responses, and adds a single search response with the
	 * specified parameters. Returns a redirect string to the search results
	 * page so the user can await the pushed resource.
	 * @param communityId	The community Id of the resource
	 * @param resourceId	The resource Id of the resource
	 * @param peerId	The IP/port of the peer serving the resource
	 * @param title		The title of the resource
	 * @param filename	The filename of the resource
	 * @param searchRedirect	True if search results should be cleared and a dummy result
	 * 							generated for the resource. This should only be true if the
	 * 							push request did not originate from a batch download.
	 */
	public String pushRedirect(String communityId, String resourceId, 
			String peerId, String title, String filename, boolean searchRedirect) {
		defWebAdapter.issuePushRequest(communityId, resourceId, peerId);
		
		if(searchRedirect) {
			defWebAdapter.clearSearchResults(currentSearchId);
			defWebAdapter.addDummySearchResponse(communityId, resourceId, 
					peerId, title, filename, currentSearchId);
			return "/displayResults.jsp?" + HttpParams.UP2P_ASYNCH_SEARCH + "=true";
		} else {
			return "";
		}
	}
	
	/**
	 * Fetches a requested community / resource ID based on a specified peer ID
	 * (IP address : port), and removes it from the list of pending requests.
	 * @param peerId	The peer Id (IP:port) of the node to fetch requests for
	 * @return	A requests resource in the string format "communityID/resourceID",
	 * 			or null if no requests are pending for the specified peer Id.
	 */
	public String getFailedTransfer(String peerId) {
		return defWebAdapter.getFailedTransfer(peerId);
	}
	
	/**
	 * Attempts to retrieve a search response for a specific community and
	 * resource ID. This should primarily be used by the retrieve servlet to build a list
	 * of peer identifiers when none were provided with the retrieve request.
	 * @param communityId	The community ID of the specified resource
	 * @param resourceId	The resource ID of the specified resource
	 * @return	A SearchResponse corresponding to the requested resource, or
	 * 			null if no valid response could be found.
	 */
	public SearchResponse getSearchResponse(String communityId, String resourceId) {
		return defWebAdapter.getSearchResponse(communityId, resourceId);
	}
	
	/**
	 * @return The hex string of salt bytes that should be used for user authentication.
	 */
	public String getSaltHex() {
		return defWebAdapter.getSaltHex();
	}
	
	/**
	 * @return	The hex string of the hash used to validate the user's password. Returns null
	 * 			if no user has been set.
	 */
	public String getPasswordHashHex() {
		return defWebAdapter.getPasswordHashHex();
	}
	
	/**
	 * @return	The username that should be used to log in to U-P2P. Returns null
	 * 			if no user has been set.
	 */
	public String getUsername() {
		return defWebAdapter.getUsername();
	}
	
	/**
	 * Sets the username and password hash for the single user of the
	 * U-P2P node
	 * @param username	The username for the account
	 * @param passwordHashHex	A hex string of the user's password's SHA-1 x1000 hash
	 */
	public void setUser(String username, String passwordHashHex) {
		defWebAdapter.setUser(username, passwordHashHex);
	}

	public List<String> listDownloads() {
		
		return defWebAdapter.listDownloads();
	}

	public String getDownloadStatus(String downloadid) {
		
		String stat= defWebAdapter.getDownloadStatus(downloadid);
		if (stat ==null)
			stat = "unknown";
		return stat;
		
	}

	public String searchG(String communityId, String query) {
		
		return defWebAdapter.searchG(communityId, query);
	}

	public void allowPush(String resRequest, String peerId) {
		defWebAdapter.allowPush(resRequest, peerId);
		
	}

	public boolean checkForPush(String sender, String communityId) {
		return defWebAdapter.checkForPush(sender,communityId);
	}
}
