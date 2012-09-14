package up2p.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
//import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.apache.commons.fileupload.DiskFileUpload;
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.FileUploadBase;
//import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import up2p.core.UserWebAdapter;
import up2p.core.WebAdapter;
import up2p.util.PairList;

/** changed jan 03, 2008, now servlets use UserWebAdapter class
 * TODO: also need to change the init.jsp to reflect that in <jsp:useBean> declaration ?
 * Base class for servlets that use a == USER == WebAdapter stored in the application
 * context.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class AbstractWebAdapterServlet extends HttpServlet {

    /**
     * The id under which the current community id is stored in the session.
     */
    public static final String CURRENT_COMMUNITY_ID = "up2p.display.community.id.current";

    /**
     * The id under which the SearchQuery object for the last search is stored.
     */
    public static final String CURRENT_SEARCH_ID = "up2p.search.current";

    /**
     * The id under which the current display mode (search, create, view etc) is
     * stored in the user session.
     */
    public static final String DISPLAY_MODE = "up2p.display.mode";

    /**
     * The id under which status messages for the Create and Search pages are
     * stored.
     */
    public static final String DISPLAY_STATUS = "up2p.display.status";

    /**
     * Header used in the HTTP response to allow the downloader to obtain the
     * original filename. The header value will be the filename with no path
     * information.
     */
    public static final String LOCATION_HEADER = "Content-Location";

    /** Name of the logger used by all servlets. */
    public static final String LOGGER = "up2p.servlet";

    /**
     * The id for the attribute that describes the status of the network the
     * user is trying to search on.
     */
    public static final String SEARCH_NETWORK_EXCEPTION = "up2p.search.networkException";

    /**
     * The id under which the search results will be stored in the user session.
     */
    public static final String SEARCH_RESULTS = "up2p.search.results";

    /** Maximum size file that can be uploaded. */
    public static final long UPLOAD_MAX_SIZE = 500000000; // 500 MB

    /**
     * Threshold for when uploaded data changes from being stored in memory to
     * being stored on disk.
     */
    public static final int UPLOAD_THRESHOLD = 10000; // 10 KB

    /**
     * Copies all the parameters in the request into a PairList.
     * 
     * @param request HTTP request with parameters
     * @return list with all the parameters and values
     */
    public static PairList copyParameters(HttpServletRequest request) {
        PairList requestParams = new PairList();

        // go through all the parameters and add all their values to
        // the pair list
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = (String) params.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            for (int i = 0; i < paramValues.length; i++) {
                requestParams.addValue(paramName, paramValues[i]);
            }
        }
        return requestParams;
    }

    /**
     * Writes all request parameters to the logs.
     * 
     * @param request HTTP request to the servlet
     * @param logFile log file
     */
    public static void debugParameters(HttpServletRequest request,
            Logger logFile) {
        Enumeration<String> params = request.getParameterNames();
        logFile.debug("Debugging request parameters:");
        if (!params.hasMoreElements()) {
            logFile.debug("Request has no parameters.");
            return;
        }

        while (params.hasMoreElements()) {
            String paramName = (String) params.nextElement();
            String values[] = request.getParameterValues(paramName);
            String debugParam = "[" + paramName + "=";
            for (int i = 0; i < values.length; i++) {
                debugParam += values[i];
                if (i < values.length - 1)
                    debugParam += ",";
            }
            debugParam += "]";
            logFile.debug(debugParam);
        }
    }

    /**
     * Returns a list of parameter-value pairs indexed by parameter name and
     * parsed from a multipart HTTP request. If the request is not multipart an
     * empty list will be returned.
     * 
     * @param request request to parse for multipart parameters
     * @param uploadHandler handler for multipart requests
     * @return parsed parameters or an empty list if the request is not
     * multipart or an error occurs when parsing the request
     * 
     * update : added parameter tempDir which indicates where to create temp files for attachments.
     * =>Attachments are stored at UP2P home.
     * =>the attachment URL is replaced by only the filename.
     * 
     * TODO: here a "multipart param" is just converted to its filename. The attachment never goes through properly.
     */
    public static PairList getMultipartParameters(HttpServletRequest request,
            ServletFileUpload uploadHandler, Logger log, String tempDir) {
    	
        PairList params = new PairList();
        
        // Holds a list of filename transformations that need to occur in each parameter.
        // Each key represents the original uploaded filename, and the value for the key
        // represents the name the file was saved as in the up2p database.
        //
        PairList nameReplace = new PairList();
        
        // Holds the final parameters after any required name substitution 
        PairList finalParams = new PairList();
        
        // A list of all file names that have been succesfully copied to the community directory
        // in this particular upload request.
        ArrayList<String> uploadedFiles = new ArrayList<String>();
        
        // Ensure the provided temporary directory actually exists
        File tempDirFile = new File(tempDir);
        if(!tempDirFile.exists()) {
        	tempDirFile.mkdir();
        }
        
        if (!ServletFileUpload.isMultipartContent(request)){ 	
        	log.error("AbstractWebAdapterServlet:Error : multipart request not recognized as multipart.");
        	return params;
        }
            

        Iterator<FileItem> i;
        try {
            List filist =uploadHandler.parseRequest(request); 
            log.debug("AbsServlet::parsed request");
            i = filist.iterator();
        } catch (FileUploadException e) {
            // failed to parse the request
        	log.error("AbstractWebAdapterServlet:Error : failed to parse the request.");
            return params;
        }
        log.info("AbstractWebAdapterServlet: about to parse multipart params");
        // go through all the items and add them as parameters
        while (i.hasNext()) {
        	log.info("AbstractWebAdapterServlet: one param found...");
            FileItem item = i.next();
            

            // Check if the uploaded file name is unique to this request
            boolean isUnique = true;
            for(String uploadName : uploadedFiles) {
            	if(uploadName.equals(item.getName())) {
            		isUnique = false;
            		break;
            	}
            }
            
            if (item.isFormField()) {
                                log.info("Adding multipart form field param "
                                        + item.getFieldName() + " value " + item.getString());
                params.addValue(item.getFieldName(), item.getString());
            } else {
            	/*
            	 * TODO here we must create a new file on the disk in tempDir.
            	 * for the uploaded attachment
            	 */
                log.info("Adding multipart param "
                        + item.getFieldName() + " value " + item.getName());
                String tempFileName = item.getName();
                log.debug("abstractServlet: getMultipartParams: Dir name: " + tempDir + " Filename: "+tempFileName);
                
                if (item.getSize()>0){ // this because upload fields where no file was uploaded cause problems
                	log.debug("AbstractServlet: getMultipartParams: size: "+Long.toString(item.getSize()));
                	
                	// Adds original file name to the set of final parameters
                	params.addValue(item.getFieldName(), item.getName());
                	
                	// Only copy the file the the community directory if the same file has not already
                	// been uploaded in this request.
                	if(isUnique) {
	                	/* in Internet Explorer the full path is taken for filename*/
	                	File IEFile = new File(tempFileName);
	                	File TempFile;
	                	if (!IEFile.isAbsolute()) { //this means that we only have the file name, as in firefox
	                		log.debug("File "+ IEFile.getAbsolutePath()+ " (not absolute)");
	                		TempFile= new File(tempDir, tempFileName);
	                	}
	                	else{
	                		log.debug("File "+ IEFile.getAbsolutePath()+ " absolute, using name "+ IEFile.getName());
	                		TempFile= new File(tempDir, IEFile.getName());
	                		tempFileName = IEFile.getName();
	                	}
	
	                	try{
	                		log.debug("Writing to disk: " + TempFile.getName());
	                		item.write(TempFile);
	                	}
	                	catch(Exception e){
	                		e.printStackTrace();
	                		log.error(e.getMessage());
	                		log.error("Error writing file: "+TempFile.getAbsolutePath());
	                	}
	                	try{
	                		log.debug("CreateServlet: raw (canonical) attachment path: "+TempFile.getCanonicalPath());
	                	}
	                	catch (Exception e3){
	                		log.error("Error!! Can't get canonical path");
	                	}
	                	
	                	// Add the original file name to the list of uploaded files for this request
	                	uploadedFiles.add(item.getName());
	                	
	                	// Setup any parameter modifications required by renaming an attachments
	                	try {
	                		log.debug("AbstractWAServlet: Adding: " + item.getFieldName() + " : " + item.getName() + " to param list.");
	                		log.debug("AbstractWAServlet: file URL: "+ TempFile.toURI().toURL().toString()+ "[saved as 'file:" + TempFile.getName()+"']");
	                		
	                		// Now add the name substitution pair that will change the filename as well as any references to the file name + add the "file:" prefix
	                		nameReplace.addValue(item.getName(), TempFile.getName());
	                		
	            			log.debug("AbstractWAServlet: Adding substitution pair -> Original: " + item.getName() + " New: " + TempFile.getName());
	                	} catch (Exception e){
	                		log.error("Error making URL!!!!!!!!!!!!!!");
	                	}
	                } else {
	                	log.info("AbstractWebAdapterServlet: Duplicate uploaded file name: " + item.getName() +
        					" detected, discarding duplicate uploaded file.");
	                }
                }
                
                else{
                	log.debug("++ no file here ++");
                	params.addValue(item.getFieldName(), tempFileName); //add nothing (in fact tempName is "")
                }
            }
        }
        
        /** Perform final name substitution in parameters */
        Iterator<String> paramKeys = params.keySet().iterator();
        
        while(paramKeys.hasNext()) {
        	String paramKey = paramKeys.next();
        	Iterator <String> paramIter = params.getValues(paramKey);
        	while(paramIter.hasNext()) {
        		String param = paramIter.next();

	        	Iterator<String> replaceKeys = nameReplace.keySet().iterator();
	        	while(replaceKeys.hasNext()) {
	            	String originalName = replaceKeys.next();
	            	String replaceName = nameReplace.getValue(originalName);
	            	// Strip the "file:" prefix if it already exists
	            	param = param.replace("file:" + originalName, originalName);
	            	param = param.replace(originalName, "file:" + replaceName);
	        	}
	        	
	        	log.debug("AbstractWAServlet: Adding final param: " + paramKey + " -> " + param);
	        	finalParams.addValue(paramKey, param);
        	}
        }

        
        return finalParams;
    }

    
    /**
     * Returns the current community ID.
     * 
     * @param session the current user session
     * @return ID of the community currently in use
     */
    protected static String getCurrentCommunityId(HttpSession session) {
        Object o = session.getAttribute(CURRENT_COMMUNITY_ID);
        if (o != null)
            return (String) o;
        return null;
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

    
    /**
     * Returns the JSP page <code>header.jsp</code> and the given error string
     * to the response.
     * 
     * @param request request message that generated the error
     * @param response response to write the error message to
     * @param errorMsg HTML error message (or plain string)
     * @param mode the current mode (create, search, view)
     * @throws IOException if an error occurs when forwarding the request to the
     * error page
     * @throws ServletException if an error occurs when forwarding the reques to
     * the error page
     */
    protected static void writeError(HttpServletRequest request,
            HttpServletResponse response, String errorMsg, String mode)
            throws IOException, ServletException {
        request.setAttribute("error.msg", errorMsg);
        request.setAttribute("error.mode", mode);

        RequestDispatcher rd = request.getRequestDispatcher("/errorPage.jsp");
        rd.forward(request, response);
    }

    /**
     * Writes output to the HTTP response.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param outputText text to write to output
     * @throws IOException if an error occurs writing to the output stream
     * @throws ServletException if an error occurs in the servlet
     */
    protected static void writeOutput(HttpServletRequest request,
            HttpServletResponse response, String outputText)
            throws IOException, ServletException {
        PrintWriter out = response.getWriter();
        RequestDispatcher rd = request.getRequestDispatcher("/header.jsp");
        rd.include(request, response);

        // write out the message
        out.println(outputText);
        // close the html tags
        out.println("</body></html>");
    }

    /** The == USER SIDE == adapter used by the Servlets. */
    protected UserWebAdapter adapter;

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
            adapter = (UserWebAdapter) o;
        else {
        	adapter = null;
        }
    }

    /**
     * Stores the servlet context for later use in the servlet service methods.
     * 
     * @param config the servlet configuration
     */
    public void init(ServletConfig config) {
        context = config.getServletContext();
        adapter = null;
    }

    /**
     * Initializes the user session.
     * 
     * update 080501:added set host / set port
     * 
     * @param request HTTP request
     */
    protected void initSession(HttpServletRequest request) {
        session = request.getSession(true);
        // Set the server port if not already set
        if (adapter.getPort() == 0)
            adapter.setPort(request.getServerPort());
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
    	if(adapter == null) { getAdapter(); }
    	super.service(req, resp);
    }
}