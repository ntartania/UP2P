package up2p.jsp;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import up2p.core.WebAdapter;
import up2p.core.UserWebAdapter;
import up2p.servlet.AbstractWebAdapterServlet;

/**
 * Implements the JSP tag for the home page. Body of the tag contains error
 * processing code, so if no errors occur, <code>SKIP_BODY</code> is returned.
 * Otherwise an attribute in the request is set with the error code found in
 * <code>ErrorCodes</code> class.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class HomeTag extends AbstractTag {
    /** Serial version */
	private static final long serialVersionUID = 20080515L;

	/** Log for this class. */
    private static Logger LOG = Logger.getLogger(HomeTag.class);

    /**
     * Special Create JSP page for the Root Community. Handles configuring
     * Network Adapters.
     */
    private static final String DEFAULT_COMMUNITY_HOME_XSLT = "default-community-home.xsl";

    /** Creates the tag. */
    public HomeTag() {
        super();
    }

    /*
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        JspWriter out = pageContext.getOut();

        // Get the WebAdapter
        if (adapter == null) {
            adapter = (UserWebAdapter) pageContext.getAttribute("adapter",
                    PageContext.APPLICATION_SCOPE);
        }

        // Get the current community
        String currentCommunity = (String) request.getSession().getAttribute(
                AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);


        // Check if requested community exists
        /*
        if (!rm.isCommunity(currentCommunity)) {
            LOG.debug("HomeTag Received create request from invalid "
                    + "community id: " + currentCommunity);
            // set the error code and process body to display it
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.COMMUNITY_NOT_FOUND);
            return EVAL_BODY_INCLUDE;
        }
        */

        // Render either the home page or use the default stylesheet
        String homePage = adapter.RMgetCommunityHomeLocation(currentCommunity);
        String defaultPage = adapter.getRootPath()
                + File.separator
                + adapter.getConfigProperty(UserWebAdapter.CONFIG_DEFAULT_HOME, null);

        if (homePage != null && (!homePage.equals(""))) {
            // Render home page
            try {
                return renderPage(homePage, out, currentCommunity, "Home",
                        request);
            } catch (IOException e) {
                LOG.error("Home Page: An error occured in outputing the"
                        + " home page for community " + currentCommunity, e);
            } catch (SAXException e) {
                LOG.error("Home Page: An error occured in rendering the XSLT"
                        + " home page for community " + currentCommunity, e);
            }
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.HOME_OUTPUT_ERROR);
            return EVAL_BODY_INCLUDE;
        } else {        	
	        // No home page specified, render using default stylesheet
	        LOG.debug("HomeTag: About to exit and render default stylesheet.");
	        return renderDefaultStylesheet(request, currentCommunity, defaultPage,
	                adapter.RMgetSchemaLocation(currentCommunity), out, "home", adapter);
        }
    }
}