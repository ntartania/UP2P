package up2p.jsp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
//import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import javax.xml.transform.TransformerException;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import up2p.core.DefaultWebAdapter;
import up2p.core.WebAdapter;
import up2p.core.UserWebAdapter;
import up2p.servlet.AbstractWebAdapterServlet;
import up2p.util.FileUtil;
import up2p.xml.TransformerHelper;

/**
 * Provides supports for search and create tags.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class AbstractTag extends TagSupport {
    /** Log used by this class. */
    private static Logger LOG = Logger.getLogger(AbstractTag.class);

    /** WebAdapter used by the Search and Create tags. */
    protected UserWebAdapter adapter;

    /** File upload handler. */
    protected DiskFileUpload uploadHandler;

    public AbstractTag() {
        uploadHandler = new DiskFileUpload();
        uploadHandler
                .setSizeThreshold(AbstractWebAdapterServlet.UPLOAD_THRESHOLD);
        uploadHandler.setSizeMax(AbstractWebAdapterServlet.UPLOAD_MAX_SIZE);
    }

    /**
     * Renders the default stylesheet for a community if it does not have a
     * search, create, or home page of its own.
     * 
     * @param request original page request
     * @param currentCommunity community id
     * @param defaultPage default stylesheet
     * @param schemaLoc location of the current community XML schema
     * @param out JSP output
     * @param displayMode "search", "create", or "home"
     * @param adapter	The WebAdapter that should be used to determine the U-P2P root path
     * 					and root community ID
     * @return true if the page renders without errors, false otherwise
     */
    protected static int renderDefaultStylesheet(HttpServletRequest request,
            String currentCommunity, String defaultPage, String schemaLoc,
            JspWriter out, String displayMode, UserWebAdapter adapter) {
        // Render the page using the default stylesheet and
        // the community schema

        // Get the default stylesheet
        File defaultStylesheet = new File(defaultPage);
        if (!defaultStylesheet.exists()) {
            LOG.error(displayMode + "  Page: Default stylesheet not found. "
                    + defaultStylesheet.getAbsolutePath());
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.DEFAULT_STYLESHEET_NOT_FOUND);
            return EVAL_BODY_INCLUDE;
        }
        
        // Create a map for passing parameters to the default stylesheet
        Map<String,String> params = new HashMap<String,String>();
        params.put("up2p-community-name", currentCommunity);
        params.put("up2p-base-url", "/" + adapter.getUrlPrefix() + "/");
        params.put("up2p-root-community-id", adapter
                .getRootCommunityId());
        params.put("up2p-current-community-id", currentCommunity);
        params.put("up2p-mode", displayMode);
        
        // For the search and create pages, the community schema should be used
        // to generate result HTML
        if(displayMode.equalsIgnoreCase("search") || displayMode.equalsIgnoreCase("create")) {
	        // Get the community schema
	        File communitySchema = null; // will be initialized below by either a URI or a string.
	        LOG.debug("Abstract Tag :: renderdefstylesheet: schema location " + schemaLoc);
	        if (schemaLoc.startsWith("file:")){
	        	try {
	        		communitySchema = new File(new URL(schemaLoc).toURI());
	        		LOG.debug("Abstract Tag :: render def stylesheet : schema file path: " + communitySchema.getPath());
	        	}catch(URISyntaxException e1){
	        		try{
	        			communitySchema = new File(new URL(schemaLoc).getPath());}
	        		catch (MalformedURLException e) {
	        			e.printStackTrace();
	        		}
	        	}
	        	catch (MalformedURLException e) {
	        		e.printStackTrace();
	        	}
	        }
	        else{
	        	communitySchema = new File(schemaLoc);
	        }
	        
	        // Convert relative to absolute path
	        // TODO: may be able to remove this
	        if (!communitySchema.isAbsolute()) {
	        	communitySchema = new File(DefaultWebAdapter.getRootPath()
	        			+ File.separator + communitySchema.getPath());
	        }
	        
	        if (!communitySchema.exists()) {
	        	LOG.error(displayMode + " Page: Community schema not found. "
	        			+ communitySchema.getAbsolutePath());
	        	request.setAttribute(ErrorCodes.ERROR_CODE,
	        			ErrorCodes.COMMUNITY_SCHEMA_NOT_FOUND);
	        	return EVAL_BODY_INCLUDE;
	        }
	
	        // Transform the community schema with the default stylesheet
	        try {
	            up2p.xml.TransformerHelper.transform(communitySchema,
	                    defaultStylesheet, out, params);
	            // No errors so skip the body
	            return SKIP_BODY;	// Success
	        } catch (FileNotFoundException e) {
	            LOG.error("Error rendering default stylesheet.", e);
	        } catch (SAXException e) {
	            LOG.error("Error rendering default stylesheet.", e);
	        } catch (IOException e) {
	            LOG.error("Error rendering default stylesheet.", e);
	        } catch (TransformerException e) {
	            LOG.error("Error parsing the XSLT stylesheet.", e);
	        }
	        
	        // Caught an error so process body
	        request.setAttribute(ErrorCodes.ERROR_CODE,
	                ErrorCodes.DEFAULT_STYLESHEET_OUTPUT_ERROR);
	        return EVAL_BODY_INCLUDE;
	        
        } else if (displayMode.equalsIgnoreCase("home")) {
        	// For the home page, the community definition should be used as the
        	// base for the XSL transform
        	// For the home page, the community definition of the community should
    		// be used as the base for the XSL transform
    		Node commDefinition = adapter.getResourceAsDOM(adapter.getRootCommunityId(), currentCommunity);
    		if(commDefinition == null) {
    			// Can't perform the XSL transform if the community definition could not be found
    			request.setAttribute(ErrorCodes.ERROR_CODE,
                        ErrorCodes.COMMUNITY_NOT_FOUND);
    			return EVAL_BODY_INCLUDE;
    		} else {
    			if (commDefinition instanceof Document){ // Should be the case 
	    			// Transform the community definition
	    			Node transformResult = TransformerHelper.attachmentReplace((Document)commDefinition, "community/" + adapter.getRootCommunityId() 
	    					+ "/" + currentCommunity + "/");
	    			transformResult = TransformerHelper.transform(commDefinition, defaultStylesheet, params);
            		try {
            			transformResult = TransformerHelper.attachmentReplace((Document)transformResult, "community/" + adapter.getRootCommunityId() 
            					+ "/" + currentCommunity + "/");
						TransformerHelper.encodedTransform(((Document)transformResult).getDocumentElement(), "UTF-8", out, true);
						return SKIP_BODY;	// Success
					} catch (IOException e) {
						LOG.error("IOException performing XSL transform.");
	            		request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.HOME_OUTPUT_ERROR);
	        			return EVAL_BODY_INCLUDE;
					}
            	} else {
            		LOG.error("Node transform did not result in a Document object.");
            		request.setAttribute(ErrorCodes.ERROR_CODE,
                            ErrorCodes.HOME_OUTPUT_ERROR);
        			return EVAL_BODY_INCLUDE;
            	}
    		}
        }
        
        LOG.error("renderDefaultStylesheet: An invalid display mode was specified.");
        return EVAL_BODY_INCLUDE; // Invalid mode was specified
    }

    /**
     * Renders the search, home, or create page for a community.
     * 
     * @param page file name for the page to render (XSLT or HTML)
     * @param out writer for rendering output to and for displaying error
     * messages
     * @param currentCommunity id of the current community
     * @param modeString mode of the page (Search, Create or Home)
     * @param request HTTP request used for returning error codes
     * @return <code>EVAL_BODY_INCLUDE</code> when an error occurs, otherwise
     * <code>SKIP_BODY</code>
     * @throws IOException if an error occurs when sending output
     * @throws SAXException if an error occurs in a transform
     */
    protected int renderPage(String page, JspWriter out,
            String currentCommunity, String modeString,
            HttpServletRequest request) throws IOException, SAXException {
        
    	LOG.debug("Abstract Tag :: renderPage: page location : " + page);
       
    	//-------------
    	File styleFile = null;
    	try{
    		styleFile = adapter.getAttachmentFile(adapter.getRootCommunityId(), currentCommunity, page);
    		LOG.debug("Abstract Tag :: renderPage: page file :"+ styleFile.getAbsolutePath());
    	} catch(FileNotFoundException e) {
    		// Specified page didn't exist
    		LOG.error(e);
            LOG.error(modeString + " page not found: "
                    + page);
            if (modeString.equalsIgnoreCase("create")) {
                request.setAttribute(ErrorCodes.ERROR_CODE,
                        ErrorCodes.CREATE_PAGE_NOT_FOUND);
    		} else if (modeString.equalsIgnoreCase("search")) {
                request.setAttribute(ErrorCodes.ERROR_CODE,
                        ErrorCodes.SEARCH_PAGE_NOT_FOUND);
    		} else if (modeString.equalsIgnoreCase("home")){
    			request.setAttribute(ErrorCodes.ERROR_CODE,
                        ErrorCodes.HOME_PAGE_NOT_FOUND);
            }
            return EVAL_BODY_INCLUDE;
        }

        // Render XSLT or output plain HTML
        if (styleFile.getName().toLowerCase().endsWith(".xsl")) {
        	// Note: This isn't actually used usually since the search, home, and
        	// create pages are normally HTML
            
            // Setup parameters for the XSL transform
            Map<String,String> paramTable = new HashMap<String,String>();
            paramTable.put("up2p-base-url", "/" + adapter.getUrlPrefix() + "/");
            paramTable.put("up2p-root-community-id", adapter
                    .getRootCommunityId());
            paramTable.put("up2p-current-community-id", currentCommunity);
            paramTable.put("up2p-xsl-filename", styleFile.getName());
            paramTable.put("up2p-community-name", currentCommunity);
            // Add parameters passed to the page
            Enumeration requestParams = request.getParameterNames();
            while (requestParams.hasMoreElements()) {
                String param = (String) requestParams.nextElement();
                paramTable.put(param, request.getParameter(param));
            }

            LOG.debug("Rendering " + modeString
                    + " page using XSLT stylesheet "
                    + styleFile.getAbsolutePath());
            
            // Transform page for output
            try {
            	if(modeString.equalsIgnoreCase("search") ||
            			modeString.equalsIgnoreCase("create")) {
            		// For the search or create pages, use the context servlet
            		// to get a full listing of the community to transform
                    String contextUrl = "context/navigation.xml";
                    TransformerHelper.transform(contextUrl, styleFile, out,
                            paramTable);
            	} else if (modeString.equalsIgnoreCase("home")) {
            		// For the home page, the community definition of the community should
            		// be used as the base for the XSL transform
            		Node commDefinition = adapter.getResourceAsDOM(adapter.getRootCommunityId(), currentCommunity);
            		if(commDefinition == null) {
            			// Can't perform the XSL transform if the community definition could not be found
            			request.setAttribute(ErrorCodes.ERROR_CODE,
                                ErrorCodes.COMMUNITY_NOT_FOUND);
            			return EVAL_BODY_INCLUDE;
            		} else {
            			if (commDefinition instanceof Document) { // Should be the case 
	            			// Transform the community definition
                    		Node transformResult = TransformerHelper.attachmentReplace((Document)commDefinition, "community/" + adapter.getRootCommunityId() 
                					+ "/" + currentCommunity + "/");
                    		transformResult = TransformerHelper.transform(commDefinition, styleFile, paramTable);
                    		TransformerHelper.encodedTransform(((Document)transformResult).getDocumentElement(), "UTF-8", out, true);
                    		return SKIP_BODY; // Success
                    	} else {
                    		LOG.error("Node transform did not result in a Document object.");
                    		request.setAttribute(ErrorCodes.ERROR_CODE,
                                    ErrorCodes.HOME_OUTPUT_ERROR);
                			return EVAL_BODY_INCLUDE;
                    	}
            		}
            	}
            } catch (TransformerException e) {
                // Caught an error so enter an error code
                if (e.getCause().getClass().equals(FileNotFoundException.class)) {
                    if(modeString.equalsIgnoreCase("create")) {
	                	request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.CREATE_PAGE_NOT_FOUND);
                    } else if (modeString.equalsIgnoreCase("search")) {
                    	request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.SEARCH_PAGE_NOT_FOUND);
                    } else if (modeString.equalsIgnoreCase("home")) {
                    	request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.HOME_PAGE_NOT_FOUND);
                    }
                } else {
                	if(modeString.equalsIgnoreCase("create")) {
	                	request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.CREATE_OUTPUT_ERROR);
                    } else if (modeString.equalsIgnoreCase("search")) {
                    	request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.SEARCH_OUTPUT_ERROR);
                    } else if (modeString.equalsIgnoreCase("home")) {
                    	request.setAttribute(ErrorCodes.ERROR_CODE,
	                            ErrorCodes.HOME_OUTPUT_ERROR);
                    }
                }
                
                // Store the exception for retrieval in the JSP
                request.setAttribute(ErrorCodes.ERROR_EXCEPTION, e);

                // Process the body to display the error message
                return EVAL_BODY_INCLUDE;
            }
        } else {
        	// Fetched page is HTML
            LOG.debug("Writing out HTML " + modeString + " page "
                    + styleFile.getAbsolutePath());
            FileUtil.writeFileToWriter(out, styleFile, false);
        }
        return SKIP_BODY;
    }
}