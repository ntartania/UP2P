package up2p.servlet;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import up2p.core.WebAdapter;
import up2p.util.DateSupport;
import up2p.util.FileUtil;
import up2p.util.Hash;
import up2p.util.PairList;
import up2p.xml.TransformerHelper;

/**
 * Handles the creation of U-P2P shared objects by submitting an HTML form with
 * XPath parameter names and values. An object is populated with the given
 * XPaths and written to a file in the community directory. A request is then
 * sent to the {@link up2p.servlet.UploadServlet UploadServlet}to process the
 * newly created file.
 * 
 * <h3>Required parameters
 * <h3>
 * <ul>
 * <li>up2p:filename - OPTIONAL - file name where resource is saved
 * </ul>
 * 
 * <h3>Special parameters populated at runtime</h3>
 * <ul>
 * <li>up2p:attach-name-n - XPath where name of nth attachment will be inserted
 * <li>up2p:attach-size-n - XPath where size in bytes of nth attachment will be
 * inserted
 * <li>up2p:attach-mimeType-n - XPath where MIME type of nth attachment will be
 * inserted
 * <li>up2p:attach-mimeDesc-n - XPath where MIME short description for nth
 * attachment will be inserted <br>
 * <li>up2p:date-n - XPath where nth parsed ISO date will be inserted
 * <li>up2p:date-day-n - Value for day of month for nth date
 * <li>up2p:date-month-n - Value for month for nth date, either [1-12], full
 * month name or first three letters of month
 * <li>up2p:date-year-n - Value for year for nth date <br>
 * <li>up2p:current-date - XPath where current date in ISO8601 format will be
 * inserted
 * <li>up2p:current-dateTime - XPath where current date and time in ISO8601
 * format will be inserted
 * <li>up2p:current-time - XPath where current time in ISO8601 format will be
 * inserted
 * <li>up2p:prune - Supported values:
 * <ul>
 * <li><b>elements </b>- Prunes any empty elements from the generated XML that
 * have no text descendents but may have attributes
 * <li><b>bare-elements </b>- Prunes any elements from the generated XML that
 * have no text descendents and no attributes
 * </ul>
 * <li>up2p:community-id - Inserts the community ID at the given XPath location
 * <li>up2p:community-title - Inserts the community title at the given XPath
 * location
 * <li>up2p:community-link - Inserts a link to this U-P2P client that will
 * display the community where the created resource is shared.
 * <li>up2p:xmlns - Creates a namespace to prefix mapping for a namespace used
 * in the created document. Format of the parameter value is prefix:namespaceURI
 * (e.g. dc:http://purl.org/dc/elements/1.1/). Appropriate namespace declaration
 * attributes will be inserted in the root node. A value of :namespaceURI sets
 * the default namespace for the document.
 * </ul>
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class CreateServlet extends AbstractWebAdapterServlet {
	
	
    /**
	 * default serial version for compliance with interface Serializable
	 */
	private static final long serialVersionUID = 1L;

	/** Mode used for this servlet (create). */
    private static final String MODE = "create";

    /** Namespace for xmls: attribute nodes set in a DOM. */
    public static final String XML_NS = "http://www.w3.org/2000/xmlns/";

    /**
     * Prunes elements from the given node that contain no text node descendents
     * and optionally, contain no attributes.
     * 
     * @param rootNode XML DOM tree to prune
     * @param onlyBare if <code>true</code> only elements with no atttributes
     * will be pruned, otherwise any element without descendants will be pruned
     */
    public static void pruneElements(Node rootNode, boolean onlyBare) {
        // create a walker for the document
        DocumentTraversal doc = (DocumentTraversal) rootNode.getOwnerDocument();
        TreeWalker walker = doc.createTreeWalker(rootNode,
                NodeFilter.SHOW_ATTRIBUTE | NodeFilter.SHOW_COMMENT
                        | NodeFilter.SHOW_CDATA_SECTION | NodeFilter.SHOW_TEXT
                        | NodeFilter.SHOW_ELEMENT, null, false);

        Node currentNode = walker.getCurrentNode();
        while (currentNode != null) {
            currentNode.normalize();
            // if element with no children
            if (currentNode.getNodeType() == Node.ELEMENT_NODE
                    && !currentNode.hasChildNodes()) {
                // check if element is bare
                if (!onlyBare || (onlyBare && !currentNode.hasAttributes())) {
                    // delete and go to parent
                    Node previous = walker.previousNode();
                    Node parent = currentNode.getParentNode();
                    parent.removeChild(currentNode);
                    currentNode = previous;
                } else
                    currentNode = walker.nextNode();
            } else
                currentNode = walker.nextNode();
        }
    }

    /**
     * Strips the XML comment symbols from the start and end of a string and
     * returns the content of the comment.
     * 
     * @param input text with comment indicators <code>&lt;!--</code> or
     * <code>&amp;lt;!--</code>
     * @return comment text without comment symbols
     */
    private static String stripComment(String input) {
        if (input.startsWith("<!--") && input.endsWith("-->"))
            return input.substring(4, input.length() - 3);
        else if (input.startsWith("&lt;!--") && input.endsWith("--&gt;"))
            return input.substring(7, input.length() - 6);
        return input;
    }

    /**
     * Overrides the parent method to add HTML and servlet context.
     * 
     * @see up2p.servlet.AbstractWebAdapterServlet#writeError(HttpServletRequest,
     * HttpServletResponse, String, String)
     */
    protected static void writeError(HttpServletRequest request,
            HttpServletResponse response, String errorMsg) throws IOException,
            ServletException {
        AbstractWebAdapterServlet.writeError(request, response,
                "<p><b>Error:</b> " + errorMsg + "</p>", MODE);
    }

    /** File upload handler. */
    private ServletFileUpload uploadHandler;

    /**
     * Creates the servlet.
     */
    public CreateServlet() {
        super();
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(UPLOAD_THRESHOLD); /* the unit is bytes */

        //File repositoryPath = new File("/temp");
        //diskFileItemFactory.setRepository(repositoryPath);

        uploadHandler = new ServletFileUpload(diskFileItemFactory);
        uploadHandler.setSizeMax(UPLOAD_MAX_SIZE);
        uploadHandler.setHeaderEncoding("ISO-8859-1");
    }

    
    
    
    /*
     * @see javax.servlet.http.HttpServlet#doPost
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    	HttpSession reqSession = request.getSession();

        // Log multipart requests
        if (FileUploadBase.isMultipartContent(request))
            LOG.debug("CreateServlet Create request has multipart parameters.");
        else
            LOG.debug("CreateServlet Create request has no multipart parameters.");

        // create a list to hold all parameters and values
        PairList paramMap = null;

        /*
         * We need access to all the parameters regardless of whether they were
         * posted as multipart encoded form fields or just regular parameters.
         * So we extract parameters for either type of request and put them both
         * into a PairList. We then deal only with the PairList instead of the
         * HTTPServletRequest object or multipart data. A multipart request can
         * only be parsed once so paramMap is used multiple times below.
         */
        
        // Generate a temporary directory to store resource assets until publish copies them
        // to their final destination
        String tempDirName = UUID.randomUUID().toString();
        File tempDir = new File(adapter.getRootPath() + 
        		File.separator + "temp" + File.separator + tempDirName);
        tempDir.getParentFile().mkdir(); // Create the temp folder if it does not already exist
        while(tempDir.exists()) {
        	tempDirName = UUID.randomUUID().toString();
            tempDir = new File(adapter.getRootPath() + 
            		File.separator + "temp" + File.separator + tempDirName);
        }
        tempDir.mkdir();
        LOG.info("CreateServlet: Using temporary directory: " + tempDir.getPath());
    	
        // Extract multipart form data if needed, otherwise just copy parameters from request
        if (FileUploadBase.isMultipartContent(request)){
        	// Note: getMultipartParams copies files to the temporaryDirectory
        	paramMap = getMultipartParameters(request, uploadHandler, LOG, tempDir.getPath());
        	LOG.debug("CreateServlet Parsed " + paramMap.size()
                    + " multi-part request parameters.");
        } else {
        	paramMap = copyParameters(request);
        	LOG.debug("CreateServlet Parsed " + paramMap.size()
                    + "non-multipart request parameters.");
        }    
        
        if (paramMap.size() == 0) {
            if (request.getAttribute("up2p.create.parameters") == null) {
                LOG.warn("CreateServlet Request to create a resource has no"
                        + " parameters.");
            } else {
                // try to get parameters passed in request attribute
                /*
                 * This happens with community-create.jsp because it uses
                 * multipart parameters that are parsed in the JSP and cannot be
                 * parsed again in the forwarded request. All other communities
                 * are forwarded directly to the CreateServlet and should have
                 * their request parameters intact (multipart or otherwise).
                 */
                paramMap = (PairList) request
                        .getAttribute("up2p.create.parameters");
            }
        }
        
        // Now that parameters have been read, check for a community ID in the
        // request, and fall back on the user session community if none was provided.
        // If a valid community can not be determined, delete the temporary folder
        // and write an error.
        String communityId = paramMap.getValue(HttpParams.UP2P_COMMUNITY);
        if(communityId == null) {
        	LOG.info("Request did not provide a community ID, using the user session community.");
        	communityId = getCurrentCommunityId(request.getSession());
        } else {
        	// Ensure that the user session community ID matches the created resource
        	// or else the upload servlet won't be able to handle it
        	request.getSession().setAttribute(CURRENT_COMMUNITY_ID, communityId);
        }
        
        // Check that a community ID was determined
        if (communityId == null || communityId.length() == 0) {
            LOG.warn("CreateServlet Current community ID is missing from"
                    + "the user session.");
            for(File f : tempDir.listFiles()) {
	        	f.delete();
	        }
	        tempDir.delete();
            writeError(request, response, "The current community is unknown."
                    + " Please select a community before performing "
                    + "any actions.");
            return;
        }
        
        // Check that the determined community ID is valid
        if (!adapter.RMisCommunity(communityId)) {
            LOG.warn("CreateServlet Create request is for an"
                    + " invalid community ID: " + communityId);
            for(File f : tempDir.listFiles()) {
	        	f.delete();
	        }
	        tempDir.delete();
            writeError(request, response, "The community ID " + communityId
                    + " was not found.");
            return;
        }
        
        LOG.info("CreateServlet Creating a resource in community "
                + adapter.RMgetCommunityTitle(communityId)
                + ".");

        // Get filename
        String fileName = paramMap.getValue(HttpParams.UP2P_FILENAME);
        if (fileName == null || fileName.length() == 0) {
            LOG.warn("CreateServlet Request is missing the file name"
                    + " for the new resource.");
            //create a name by hashing the upload content
            String myString = paramMap.toString();
            InputStream is = new ByteArrayInputStream(myString.getBytes("UTF-8"));
            try {
				fileName = Hash.hexString(Hash.getMD5Digest(is)).substring(15);
				LOG.debug("obtained hash filename:"+ fileName);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				LOG.error("create servlet:"+ e);
				e.printStackTrace();
			}
            /*
            writeError(request, response,
                    "The file name is missing for the new resource.");
            return;*/
        }
        LOG.debug("CreateServlet File name parameter: " + fileName);

        // Create map to store namespaces
        Map namespaces = new HashMap();
        
        // Process raw parameters into XPath value pairs with tags replaced
        // appropriately
        PairList createParams = processParameters(paramMap, communityId,
                namespaces);

        // Check that there are some parameters present
        if (createParams.size() == 0 && paramMap.getValue(HttpParams.UP2P_RAW_XML) == null) {
            LOG.warn("CreateServlet Request does not have any parameter XPath"
                    + " value pairs for creating the XML resource.");
            writeError(request, response,
                    "No values were entered to create a new resource.");
            return;
        }
        
        Document document = null;
        
        // Check to see if a raw xml parameter has been specified
        String rawXmlFeed = paramMap.getValue(HttpParams.UP2P_RAW_XML);
        
        if(rawXmlFeed != null) {
        	
        	// If a raw xml parameter was specified build the DOM object from the specified string
        	try {
        		// Parse the raw xml string into a document object
        		document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
        				new InputSource(new StringReader(rawXmlFeed.trim())));
        	} catch (SAXException e) {
        		writeError(request, response,
						"An error occured in parsing the uploaded raw xml: "
								+ e.getMessage());
        		return;
        	} catch (Exception e) {
        		writeError(request, response,
						"An unexpected error occured: "
								+ e.getMessage());
        		return;
        	}
        	
        	// TODO: Improve error handling
        	
        	LOG.debug("CreateServlet populated XML document from raw xml.");
        	
        } else {

	        // create the XML document
	        document = TransformerHelper.newDocument();
	        populateDocument(document, createParams, namespaces);
	
	        // prune elements if necessary
	        String pruning = request.getParameter("up2p:prune");
	        if (pruning != null && pruning.equals("elements"))
	            pruneElements(document.getDocumentElement(), false);
	        else if (pruning != null && pruning.equals("bare-elements"))
	            pruneElements(document.getFirstChild(), true);
	
	        LOG.debug("CreateServlet Populated XML document from key/value pairs.");
	        
        }
        
        // Normalize file name
        fileName = FileUtil.normalizeFileName(fileName);
        if (!fileName.endsWith(".xml") && !fileName.endsWith(".XML"))
            fileName = fileName + ".xml";
        
        //TODO: File should not be created here but in the Repository
        
        // create the file for the object
        File outputFile = new File(adapter.getStorageDirectory(communityId), 
        			fileName);
        // Check if the file exists and rename the resource if necessary
        outputFile = FileUtil.createUniqueFile(outputFile);

        // Create parent dir if needed
        if (!outputFile.getParentFile().mkdirs())
            LOG.error("CreateServlet Error creating parent directory "
                    + "for upload." + " Save path "
                    + outputFile.getAbsolutePath());
    	
        if (outputFile.exists() && !outputFile.delete()) {
        	// TODO: Ensure that this condition can never occur and remove this code
        	// (FileUtil.createUniqueFile should ensure that the file does not exist)
            LOG.error("CreateServlet Tried to overwrite file "
                    + outputFile.getAbsolutePath()
                    + " but the file could not be deleted.");
        }

        // Write the file to the log and to disk
        LOG.debug("CreateServlet Writing XML file to disk: " + outputFile.getName());
 
        StringWriter sw = new StringWriter();
        TransformerHelper.encodedTransform(document.getDocumentElement(),
                WebAdapter.DEFAULT_ENCODING, sw, false);
        
        // Excessive logging
        // if (LOG.isDebugEnabled()) {
        // 	LOG.debug(sw.getBuffer().toString().replaceAll("[\f\r\n]{1,2}", ""));
        // }
        
        BufferedWriter fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        fileOut.write(sw.getBuffer().toString());
        fileOut.close();
        
        /// Changing to use only the filename (to match what the upload servlet would get from a direct upload)

        String fileurl = outputFile.getName();
        
        // Forward to Upload servlet (with up2p:filename and up2p:editresource parameters)
        String paramString = HttpParams.UP2P_FILENAME + "=" + URLEncoder.encode(fileurl, "UTF-8");
        
        Iterator <String> filenameIter = paramMap.getValues(HttpParams.UP2P_FILENAME);
        if(filenameIter != null) {
	        filenameIter.next(); // Skip first value (may have been modified and already included)
	        while(filenameIter.hasNext()) {
	        	paramString += "&" + HttpParams.UP2P_FILENAME + "=" + URLEncoder.encode(filenameIter.next(), "UTF-8");
	        }
        }
        
        // Forward relevant parameters
        if(paramMap.get(HttpParams.UP2P_EDIT_RESOURCE) != null) {
        	paramString += "&" + HttpParams.UP2P_EDIT_RESOURCE + "=" + paramMap.getValue(HttpParams.UP2P_EDIT_RESOURCE);
        }
        if(paramMap.get(HttpParams.UP2P_FETCH_XML) != null) {
        	paramString += "&" + HttpParams.UP2P_FETCH_XML + "=" + paramMap.getValue(HttpParams.UP2P_FETCH_XML);
        }
        
        // Use the user session to pass the temporary directory to the file upload servlet
        reqSession.setAttribute("up2p:attachdir", tempDir);
        
        String redirect = response.encodeURL("upload?" + paramString);
        RequestDispatcher rd = request.getRequestDispatcher(redirect);
        if (rd != null) {
            LOG.debug("CreateServlet Redirecting to " + redirect);
            rd.forward(request, response);
        } else {
            LOG.error("CreateServlet Error getting request dispatcher.");
        }
    }

    /*
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) {
    	super.init(config);
    }

    /**
     * Given a list of single-node, simple XPath locations, this method
     * populates the given DOM with the values given for each location and
     * recreates the ancestors of each location in the XML tree.
     * 
     * <p>
     * A simple XPath does not use axes or predicates with the exception of a
     * number to identify the position of a node among siblings. For example,
     * <code>bib/book[2]/title</code> uses <code>[2]</code> to identify the
     * location as the second <code>book</code> element under <code>bib</code>.
     * Any other predicates, functions, node type tests or wildcards are not
     * supported.
     * 
     * <p>
     * The abbreviated attribute syntax '@' must be used to identify attributes
     * and only in the last step of the location path. e.g.
     * <code>bib/book/@creator</code>. The non-abbreviated axis identifier
     * for attributes (e.g. attribute::) and all other axes are not supported.
     * 
     * <p>
     * Refer to <a href="http://www.w3.org/TR/1999/REC-xpath-19991116">The XPath
     * specification </a> for details on XPath 1.0.
     * 
     * @param document the document to use to create the tree
     * @param parameters a table of simple XPath locations and value pairs
     * @param namespaces map of namespace prefixes to namespace URIs
     * @throws DOMException if an error occurs when adding nodes to the document
     */
    public void populateDocument(Document document, PairList parameters,
            Map<String,String> namespaces) throws DOMException {
        boolean namespacesSet = false;
        Iterator<String> keys = parameters.keySet().iterator();
        while (keys.hasNext()) {
            String xpath = (String) keys.next();
            LOG.debug("CreateServlet Populating XPath " + xpath);

            // append values together
            Iterator<String> values = parameters.getValues(xpath);
            StringBuffer valueBuffer = new StringBuffer();
            while (values.hasNext()) {
                String value = (String) values.next();
                valueBuffer.append(value);
                if (values.hasNext())
                    valueBuffer.append(" ");
            }
            
            String value = valueBuffer.toString();

            // strip leading order brackets
            if (xpath.startsWith("{"))
                xpath = xpath.substring(xpath.indexOf("}") + 1);

            // strip leading slashes
            while (xpath.startsWith("/"))
                xpath = xpath.substring(1);

            // create root node if necessary
            String rootNode = null;
            if (xpath.indexOf("/") > -1)
                rootNode = xpath.substring(0, xpath.indexOf("/"));
            else
                rootNode = xpath; // XPath denotes a single node
            if (document.getDocumentElement() == null) {
                // check if the node has a namespace prefix
                int colonIdx = rootNode.indexOf(':');
                if (colonIdx > -1) {
                    // create node with NS prefix
                    String eleNamespace = (String) namespaces.get(rootNode
                            .substring(0, colonIdx));
                    if (eleNamespace != null)
                        document.appendChild(document.createElementNS(
                                eleNamespace, rootNode));
                    else
                        LOG.error("CreateServlet Error creating element "
                                + rootNode + " with unknown namespace prefix.");
                } else
                    document.appendChild(document.createElement(rootNode));
            }

            Element currentNode = document.getDocumentElement();
            // set the namespace bindings if not set
            if (!namespacesSet) {
                Iterator<String> nsPrefix = namespaces.keySet().iterator();
                while (nsPrefix.hasNext()) {
                    String prefix = nsPrefix.next();
                    String nsValue = namespaces.get(prefix);
                    currentNode.setAttributeNS(XML_NS, "xmlns:" + prefix,
                            nsValue);
                }
                namespacesSet = true;
            }

            // split xpath string and create the tree in the
            // result document
            StringTokenizer tokens = new StringTokenizer(xpath, "/");
            if (!tokens.hasMoreTokens()) {
                LOG.error("CreateServlet Error parsing XPath " + xpath
                        + " with value " + value);
                break;
            }
            tokens.nextToken(); // discard root node
            while (tokens.hasMoreTokens()) {
                String elementName = tokens.nextToken();
                if (elementName.startsWith("@")) {
                    elementName = elementName.substring(1);
                    // attribute
                    int colonIdx = elementName.indexOf(':');
                    if (colonIdx > -1) {
                        // create attribute with namespace prefix
                        String prefix = elementName.substring(0, colonIdx);
                        String attrNamespace = (String) namespaces.get(prefix);
                        if (attrNamespace != null)
                            currentNode.setAttributeNS(attrNamespace,
                                    elementName, value);
                        else
                            LOG.error("CreateServlet Error creating attribute "
                                    + elementName
                                    + " with unknown namespace prefix.");
                    } else {
                        // create attribute in default namespace
                        currentNode.setAttribute(elementName, value);
                    }
                } else if (elementName.indexOf("[") > -1
                        && elementName.endsWith("]")) {
                    // Contains [#] to specify which node to select.
                    // e.g. para[2] selects the second 'para' child of
                    // the context node

                    // get the element name without the child selector
                    String elementNameNoSelect = elementName.substring(0,
                            elementName.indexOf("["));

                    // get the number between the brackets
                    String childNumStr = elementName.substring(elementName
                            .indexOf("[") + 1, elementName.length() - 1);
                    int childSelected = 1;
                    try {
                        childSelected = Integer.parseInt(childNumStr);
                    } catch (NumberFormatException e) {
                        childSelected = 1; // parse failed
                    }

                    // check if that many children exist
                    NodeList descendants = currentNode
                            .getElementsByTagName(elementNameNoSelect);
                    if (descendants.getLength() >= childSelected) {
                        // node already exists
                        currentNode = (Element) descendants
                                .item(childSelected - 1);
                    } else {
                        // create all the necessary nodes
                        int numNodesToCreate = childSelected
                                - descendants.getLength();
                        for (int j = 0; j < numNodesToCreate; j++) {
                            int colonIdx = elementNameNoSelect.indexOf(':');
                            if (colonIdx > -1) {
                                // create element with namespace
                                String eleNamespace = (String) namespaces
                                        .get(elementNameNoSelect.substring(0,
                                                colonIdx));
                                if (eleNamespace != null)
                                    currentNode.appendChild(document
                                            .createElementNS(eleNamespace,
                                                    elementNameNoSelect));
                                else
                                    LOG.error("CreateServlet Error "
                                            + "creating element "
                                            + elementNameNoSelect
                                            + " with unknown namespace"
                                            + " prefix.");
                            } else
                                currentNode.appendChild(document
                                        .createElement(elementNameNoSelect));
                        }
                        // select the correct node and set it to the current
                        descendants = currentNode
                                .getElementsByTagName(elementNameNoSelect);
                        currentNode = (Element) descendants
                                .item(childSelected - 1);
                    }

                    // set text value of the text or comment node
                    if (!tokens.hasMoreTokens()) {
                        if (value.startsWith("<!--")
                                || value.startsWith("&lt;!--"))
                            currentNode.appendChild(document
                                    .createComment(stripComment(value)));
                        else
                            currentNode.appendChild(document
                                    .createTextNode(value));
                    }
                } else {
                    // element
                    NodeList descendants = currentNode
                            .getElementsByTagName(elementName);
                    if (descendants.getLength() > 0) {
                        // existing node was found
                        currentNode = (Element) descendants.item(0);
                    } else {
                        // node not found so create it
                        int colonIdx = elementName.indexOf(':');
                        if (colonIdx > -1) {
                            // create element with namespace
                            String eleNamespace = (String) namespaces
                                    .get(elementName.substring(0, colonIdx));
                            if (eleNamespace != null) {
                                Element newChild = document.createElementNS(
                                        eleNamespace, elementName);
                                currentNode.appendChild(newChild);
                                currentNode = newChild;
                            } else
                                LOG.error("CreateServlet Error "
                                        + "creating element " + elementName
                                        + " with unknown namespace"
                                        + " prefix.");
                        } else {
                            // create element with default namespace
                            Element newChild = document
                                    .createElement(elementName);
                            currentNode.appendChild(newChild);
                            currentNode = newChild;
                        }
                    }
                    // set text value of the text or comment node
                    if (!tokens.hasMoreTokens()) {
                        if (value.startsWith("<!--")
                                || value.startsWith("&lt;!--"))
                            currentNode.appendChild(document
                                    .createComment(stripComment(value)));
                        else
                            currentNode.appendChild(document
                                    .createTextNode(value));
                    }
                }
            }
        }
    }

    /**
     * Process parameters in the HTTP request and return a list of XPath value
     * pairs. Handles all the special up2p: tags available to the create form.
     * 
     * @param paramMap list with parameter value pairs from the HTTP request
     * @param communityId id of the community where the resource is being
     * created (used for special tags)
     * @param namespaces Map of namespace prefixes to namespace URIs. The map
     * entries are entered in this method and used later in populateDocument.
     * @return a list of XPath and value pairs
     * 
     * 
     */
    private PairList processParameters(PairList paramMap, String communityId,
            Map<String,String> namespaces) {
        // create list of parameter pairs
        PairList createParams = new PairList();

        // create list for attachments
        Set<String> attachmentFile = new HashSet<String>();

        // create list for dates
        ArrayList<String> dates = new ArrayList<String>();

        // create calendar with current date and time (used for date tags)
        Calendar currentCal = Calendar.getInstance();

        // go through all the parameters in the request
        Iterator<String> paramIterator = paramMap.keySet().iterator();
        while (paramIterator.hasNext()) {
            String param = paramIterator.next();

            // repeat for each value of the parameter
            Iterator<String> values = paramMap.getValues(param);
            while (values.hasNext()) {
                String paramValue = (String) values.next();
                LOG.debug("CreateServlet: next param value:"+paramValue);
                if (paramValue.length() > 0) {
                    if (param.startsWith("up2p:")) {
                        // process special parameters

                        /*
                         * For attachments, put empty placeholder in XPath
                         * location to preserve order of XPath locations. It
                         * will be filled in after all attachments are known.
                         * Parameter names should be up2p:attach-...-x where x
                         * is a positive integer.
                         */
                        if (param.startsWith("up2p:attach-size")
                                || param.startsWith("up2p:attach-name")
                                || param.startsWith("up2p:attach-mimeType"))
                            createParams.addValue(paramValue, "");
                        else if (param.startsWith("up2p:date")) {
                            // only store up2p:date-n where n is a positive
                            // integer
                            if (!param.startsWith("up2p:date-month")
                                    && !param.startsWith("up2p:date-day")
                                    && !param.startsWith("up2p:date-year")) {
                                createParams.addValue(paramValue, "");
                                // record the date XPath so it can be processed
                                // later
                                dates.add(paramValue);
                            }
                        } else if (param.equals("up2p:current-date")) {
                            createParams.addValue(paramValue, DateSupport
                                    .getISO8601Date(currentCal));
                        } else if (param.equals("up2p:current-dateTime")) {
                            createParams.addValue(paramValue, DateSupport
                                    .getISO8601DateTime(currentCal));
                        } else if (param.equals("up2p:current-time")) {
                            createParams.addValue(paramValue, DateSupport
                                    .getISO8601Time(currentCal));
                        } else if (param.equals("up2p:community-id")) {
                            // insert current community id
                            createParams.addValue(paramValue, communityId);
                        } else if (param.equals("up2p:community-title")) {
                            // insert current community title
                            createParams.addValue(paramValue, adapter.RMgetResourceTitle(
                                            communityId,
                                            adapter.getRootCommunityId()));
                        } else if (param.equals("up2p:community-link")) {
                            // insert current community title
                            createParams.addValue(paramValue,
                                    "/up2p/view.jsp?up2p:community="
                                            + communityId);
                        } else if (param.equals("up2p:xmlns")) {
                            // XML namespace tag
                            // Format of the parameter value is
                            // prefix:namespaceURI
                            int colonIdx = paramValue.indexOf(':');
                            String prefix = paramValue.substring(0, colonIdx);
                            String namespaceURI = paramValue
                                    .substring(colonIdx + 1);
                            namespaces.put(prefix, namespaceURI);
                        }
                    } else {
                        // check if it is a file and convert filename
                        // into URL format file://
                        File attachment = new File(paramValue);
                        if (attachment.isFile() && attachment.canRead()) {
                            LOG.debug("CreateServlet Found attachment in"
                                    + " parameter " + param + " with value "
                                    + paramValue);
                            attachmentFile.add(param);
                            try {
                                paramValue = "file://"
                                        + attachment.getCanonicalPath();
                            } catch (IOException e) {
                                // failed to get file
                                LOG.error("CreateServlet Failed to get "
                                        + "canonical path name for "
                                        + "attachment: "
                                        + attachment.getAbsolutePath());
                            }
                        }

                        // add the parameter to the list
                        createParams.addValue(param, paramValue);
                    }
                }
            }
        }

        // process attachments
        Iterator<String> attachments = attachmentFile.iterator();
        int attachCounter = 1;

        if (!attachments.hasNext())
            LOG.debug("CreateServlet No attachments found for processing.");

        // process attachments
        while (attachments.hasNext()) {
            String fileXPath = (String) attachments.next();
            String value = createParams.getValue(fileXPath);
            LOG.info("CreateServlet Processing attachment XPath " + fileXPath
                    + " value " + value);

            File attachFile = new File(value.substring(7));
            if (attachFile.isFile() && attachFile.canRead()) {
                // confirmed attachment file
                LOG.debug("CreateServlet Found attachment file "
                        + attachFile.getName()
                        + ". Processing attachment tags.");
                // get XPath location for attributes

                // file size
                String attachAttribute = paramMap.getValue("up2p:attach-size-"
                        + attachCounter);
                if (attachAttribute != null && attachAttribute.length() > 0) {
                    String fileSize = String.valueOf(attachFile.length());
                    LOG.debug("CreateServlet Setting attachment size "
                            + fileSize + " XPath " + attachAttribute);
                    createParams.setValue(attachAttribute, fileSize);
                }

                // file name
                attachAttribute = paramMap.getValue("up2p:attach-name-"
                        + attachCounter);
                if (attachAttribute != null && attachAttribute.length() > 0) {
                    LOG.debug("CreateServlet Setting attachment name "
                            + attachFile.getName() + " XPath "
                            + attachAttribute);
                    createParams
                            .setValue(attachAttribute, attachFile.getName());
                }

                // MIME type
                String mimeStr = "application/octet-stream";
                attachAttribute = paramMap.getValue("up2p:attach-mimeType-"
                        + attachCounter);
                if (attachAttribute != null && attachAttribute.length() > 0) {
                    mimeStr = context.getMimeType(attachFile.getName());
                    LOG.debug("CreateServlet Setting attachment MIME type "
                            + mimeStr + " XPath " + attachAttribute);
                    createParams.setValue(attachAttribute, mimeStr);
                }
                
            } else
                LOG.debug("CreateServlet Attachment file "
                        + attachFile.getAbsolutePath() + " not found.");
            attachCounter++;
        }

        // remove all unused attach-...-x tags
        int numberOfUsedTags = attachCounter;
        int usedTagCounter;
        String[] attachTagNames = { "up2p:attach-size-", "up2p:attach-name-",
                "up2p:attach-mimeType-", "up2p:attach-mimeDesc-" };
        for (int i = 0; i < attachTagNames.length; i++) {
            // reset tag counter
            usedTagCounter = numberOfUsedTags;
            String attachTag = attachTagNames[i] + usedTagCounter;
            while (paramMap.containsKey(attachTag)) {
                // get all values for this tag
                Iterator attachTagValues = paramMap.getValues(attachTag);
                while (attachTagValues.hasNext()) {
                    createParams.remove(attachTagValues.next());
                }

                // move to up2p:attach-...-x+1
                usedTagCounter++;
                // get tag name
                attachTag = attachTagNames[i] + usedTagCounter;
            }
        }

        // process dates
        Iterator<String> dateIterator = dates.iterator();
        int dateCounter = 1;
        while (dateIterator.hasNext()) {
            String dateXPath = dateIterator.next();
            // find the day
            String day = paramMap.getValue("up2p:date-day-" + dateCounter);
            // find the month
            String month = paramMap.getValue("up2p:date-month-" + dateCounter);
            // find the year
            String year = paramMap.getValue("up2p:date-year-" + dateCounter);

            // only process dates with a valid year
            if (year != null && year.length() > 0) {
                String parsedDate = DateSupport.parseDate(day, month, year);
                LOG.debug("CreateServlet Parsed date " + dateCounter
                        + " XPath " + dateXPath + ", day " + day + ", month "
                        + month + ", year " + year + ", parsed: " + parsedDate);
                // set the date value
                createParams.setValue(dateXPath, parsedDate);
            } else {
                // invalid year
                createParams.remove(dateXPath);
            }
            dateCounter++;
        }

        // Process keys to correct the {} ordering (if it is used). Otherwise
        // keys may not sort properly, e.g. 10 is lexically less than 2
        StringBuffer keyBuffer = null;
        // Cannot modify the list when iterating so we use a temp map
        // to store the changes
        Map<String,Object> newInsertions = new HashMap<String,Object>(); // will insert these keys

        // go through and check every key for an order prefix {n}
        Iterator<String> keyIterator = createParams.keySet().iterator();
        while (keyIterator.hasNext()) {
            String xPathKey = (String) keyIterator.next();
            if (xPathKey.startsWith("{")) {
                // parse number and make it five digits
                try {
                    keyBuffer = new StringBuffer(String.valueOf(Integer
                            .parseInt(xPathKey.substring(1, xPathKey
                                    .indexOf("}")))));
                    if (keyBuffer.length() > 5)
                        LOG.error("CreateServlet Abnormal key found with "
                                + "number " + keyBuffer.toString());
                    while (keyBuffer.length() < 5)
                        keyBuffer.insert(0, '0');
                    keyBuffer.insert(0, "{");
                    keyBuffer.append("}"
                            + xPathKey.substring(xPathKey.indexOf("}") + 1));

                    // record new key for insertion later
                    // Note that the PairList iterator does not support
                    // additions to the list while iterating
                    LOG.debug("CreateServlet Adding new key/value pair "
                            + keyBuffer.toString() + " "
                            + createParams.get(xPathKey));
                    newInsertions.put(keyBuffer.toString(), createParams
                            .get(xPathKey));

                    // remove old key through iterator method
                    keyIterator.remove();
                } catch (NumberFormatException e) {
                    // number is weird so don't change the key
                    LOG.warn("CreateServlet Error sorting key " + xPathKey);
                }
            }
        }
        // insert new keys if any were changed
        if (newInsertions.size() > 0) {
            LOG.debug("CreateServlet Inserting new map values.");
            createParams.putAll(newInsertions);
        }
        return createParams;
    }
}