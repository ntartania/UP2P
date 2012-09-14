package up2p.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import up2p.core.AttachmentNotFoundException;
import up2p.core.CommunityNotFoundException;
//import up2p.core.FileMapper;
import up2p.core.ResourceNotFoundException;
import up2p.core.WebAdapter;
//import up2p.core.UserWebAdapter;
import up2p.core.Core2Repository;
//import up2p.core.Core2Network;
import up2p.util.FileUtil;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.AttachmentListFilter;
import up2p.xml.filter.AttachmentReplacer;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.FileAttachmentFilter;
import up2p.xml.filter.SerializeFilter;

/**
 * Default implementation of the DownloadService. It services requests for
 * shared resources and attachments by performing a lookup in the FileMapper and
 * returning the appropriate local file.
 * 
 * @author Neal Arthorne
 * @author Alan Davoust
 * @version 2.0
 */
public class DefaultDownloadService implements DownloadService {
    private Logger LOG;
    
    private Core2Repository adapter;

    /**
     * Constructs a default download service.
     *  
     */
    public DefaultDownloadService(Core2Repository repAdapter) {
    	adapter = repAdapter;
        LOG = Logger.getLogger(AbstractWebAdapterServlet.LOGGER);
    }

    /*
     * @see up2p.servlet.DownloadService#doDownloadService(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public void doDownloadService(HttpServletRequest request,
            HttpServletResponse response,
            String filterType, String communityId, String resourceId,
            String attachName, ServletContext context) throws ServletException, IOException {
    	
    	LOG.debug("DefDownloadService: executing ");
    	LOG.debug("Got download request:\n" + filterType + "/" + communityId + "/" + resourceId
    			+ "\nAttach name: " + attachName
    			+ "\nRequest from: " + request.getRemoteAddr());

        // Local file to be retrieved from file mapper
        File realFile = null;

        // Check file mapper
        if (attachName == null) {
        	// Resource file request
        	realFile = adapter.getLocalFile(communityId, resourceId);
        } else {
        	// Attachment file request
        	try {
        		realFile = adapter.getLocalFileAttachment(communityId, resourceId,
        				attachName);
        	} catch (AttachmentNotFoundException e1) {
        		LOG.error("DefDownloadService:"
        				+ AbstractWebAdapterServlet.getInfo(request)
        				+ "Downloading of attachment " + attachName
        				+ " in resource id " + resourceId + " in community id "
        				+ communityId + " failed. Attachment not found.");
        	}
        }

        // check for no mapping //TODO: change this to getting the file from a single place
        if (realFile == null) {
            // No mapping was found
            LOG.debug("DefDownloadService:"
                    + AbstractWebAdapterServlet.getInfo(request)
                    + "File request is neither a mapped resource or attachment." 
                    + "\nResource id " + resourceId
                    + "\nCommunity id " + communityId 
                    + "\nPath " + request.getPathInfo());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (!realFile.exists()) {
            // File mapped but not found
            LOG.error("DefDownloadService:"
                    + AbstractWebAdapterServlet.getInfo(request)
                    + "Mapping of " + resourceId + " to "
                    + realFile.getAbsolutePath()
                    + " is incorrect. File not found.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // set the filename in the response
        response.setHeader(AbstractWebAdapterServlet.LOCATION_HEADER, realFile
                .getName());
                
        // Set content type header
        response.setHeader("Content-Type", context.getMimeType(realFile.getName()));

        // return the real file
        if (attachName == null) {
            try {
                // filter the outgoing resource
                response.setContentType(DownloadServlet.XML_CONTENT_TYPE);
                LOG.info("DefDownloadService: "
                        + AbstractWebAdapterServlet.getInfo(request)
                        + "Sending mapped resource " + realFile.getName());

                // create an XML reader and source, and a filter chain
                XMLReader reader = TransformerHelper.getXMLReader();
                DefaultResourceFilterChain chain = new DefaultResourceFilterChain();
               
                // create filter to output to the response stream
                SerializeFilter outFilter = new SerializeFilter(response
                        .getOutputStream(), true, DownloadServlet.ENCODING);
                chain.addFilter(outFilter);

                // process the outgoing file
                chain.doFilter(reader, new InputSource(new FileInputStream(
                        realFile)));
            } catch (IOException e) {
                LOG.error("DefDownloadService: "
                        + AbstractWebAdapterServlet.getInfo(request)
                        + "Error occured in download of resource.", e);
            } catch (SAXException e) {
                LOG.error("DefDownloadService: "
                        + AbstractWebAdapterServlet.getInfo(request)
                        + "Error occured outputting resource.", e);
            }
        } else {
            if (realFile.exists()) {
                // return the attachment
                LOG.info("DefDownloadService: "
                        + AbstractWebAdapterServlet.getInfo(request)
                        + "Sending mapped attachment " + realFile.getName());
                //response.setHeader("Content-Disposition", "attachment;
                // filename=" + realFile.getName());
                response.setHeader("Content-Length", String.valueOf(realFile
                        .length()));

                FileUtil.writeFileToStream(response.getOutputStream(),
                        realFile, true);

                LOG.debug("DefDownloadService: "
                        + AbstractWebAdapterServlet.getInfo(request)
                        + "Finished sending attachment " + realFile.getName());
            } else {
                LOG.error("Attempt to download attachment "
                        + realFile.toString() + " failed.");
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
}