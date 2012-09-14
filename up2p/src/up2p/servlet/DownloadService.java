package up2p.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import up2p.core.WebAdapter;
import up2p.core.Core2Repository;
import up2p.core.UserWebAdapter;

/**
 * Provides download service for the DownloadServlet to retrieve files shared by
 * the local system or proxied from an external source.
 * 
 * @author Neal Arthorne
 * @author adavoust
 * @version 1.1
 */
public interface DownloadService {

    /** Class name of default implementation for the DownloadService. */
    public static String DEFAULT_PROVIDER = "up2p.servlet.DefaultDownloadService";
    

    /**
     * Service a download request for a resource or attachment that may reside
     * on the local computer or be proxied by the implementation.
     * 
     * @param request original HTTP request for the download
     * @param response HTTP response where the resource or attachment is written
     * to
     * @param adapter the WebAdapter in use by the DownloadServlet
     * @param filterType filter type for replacing attachment URLs in XML
     * resources with HTTP (default is u-p2p)
     * @param communityId id of the community from which the resource or
     * attachment is to be retrieved
     * @param resourceId id of resource to be retrieved
     * @param attachName name of the attachment to be retrieved
     * @param context	The servlet context which should be used to determine mime-types
     * 								for served resources.
     * @throws ServletException if an error occurs in servicing the request
     * @throws IOException if an error occurs in servicing the request
     */
    public void doDownloadService(HttpServletRequest request,
            HttpServletResponse response, 
            String filterType, String communityId, String resourceId,
            String attachName, ServletContext context) throws ServletException, IOException;
}