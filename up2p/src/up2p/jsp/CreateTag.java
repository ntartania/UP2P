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
 * Implements the JSP tag for the create page. Body of the tag contains error
 * processing code, so if no errors occur, <code>SKIP_BODY</code> is returned.
 * Otherwise an attribute in the request is set with the error code found in
 * <code>ErrorCodes</code> class.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class CreateTag extends AbstractTag {
    /**serial version
	 * 
	 */
	private static final long serialVersionUID = 20080515L;

	/** Log for this class. */
    private static Logger LOG = Logger.getLogger(CreateTag.class);

    /**
     * Special Create JSP page for the Root Community. Handles configuring
     * Network Adapters.
     */
    private static final String DEFAULT_COMMUNITY_CREATE_JSP = "community-create.jsp";

    /** Name of configuration property where Root Community create JSP is found. */
    private static final String COMMUNITY_CREATE_JSP = "community.root.create";

    /** Creates the tag. */
    public CreateTag() {
        super();
    }

    /*
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        JspWriter out = pageContext.getOut();

        // get the WebAdapter
        if (adapter == null)
            adapter = (UserWebAdapter) pageContext.getAttribute("adapter",
                    PageContext.APPLICATION_SCOPE);
        //ResourceManager rm = adapter.getResourceManager(); //removed for refactor

        // get the current community
        String currentCommunity = (String) request.getSession().getAttribute(
                AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);

        // log multipart requests
        if (FileUploadBase.isMultipartContent(request))
            LOG.debug("CreateTag Received a multipart request in community id "
                    + currentCommunity + ".");
        else
            LOG.debug("CreateTag Received a non-multipart request in "
                    + "community id " + currentCommunity + ".");

        // check if requested community exists
        /*if (!rm.isCommunity(currentCommunity)) {
            LOG.debug("CreateTag Received create request from invalid "
                    + "community id: " + currentCommunity);
            // set the error code and process body to display it
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.COMMUNITY_NOT_FOUND);
            return EVAL_BODY_INCLUDE;
        }*/

        // if the root community, forward to community-create.jsp
        if (currentCommunity.equals(adapter.getRootCommunityId())) {
            String communityCreatePage = adapter.getConfigProperty(
                    COMMUNITY_CREATE_JSP, DEFAULT_COMMUNITY_CREATE_JSP);
            LOG.debug("CreateTag Create request is in Root Community. "
                    + "Forwarding to " + communityCreatePage + ".");
            try {
                pageContext.forward(communityCreatePage);
                return SKIP_BODY;
            } catch (ServletException e) {
                LOG.error("CreatePage: Servlet error when forwarding to Root "
                        + "Community " + "create page.", e.getRootCause());
            } catch (IOException e) {
                LOG.error("Create Page: IO error when forwarding to Root "
                        + "Community " + "create page.", e);
            }
            // error was caught so process body
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.CREATE_OUTPUT_ERROR);
            return EVAL_BODY_INCLUDE;
        }

        // render either the create page or use the default stylesheet
        String createPage = adapter.RMgetCreateLocation(currentCommunity);
        String defaultPage = adapter.getRootPath()
                + File.separator
                + adapter.getConfigProperty(WebAdapter.CONFIG_DEFAULT_CREATE,
                        null);

        // include the upload form for uploading resources
        try {
            pageContext.include("upload.jsp");
        } catch (ServletException e1) {
            LOG.error("Create Page: Error including upload.jsp.", e1
                    .getRootCause());
            // caught an error so process body
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.CREATE_OUTPUT_ERROR);
            return EVAL_BODY_INCLUDE;
        } catch (IOException e1) {
            LOG.error("Create Page: Error include upload.jsp.", e1);
            // caught an error so process body
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.CREATE_OUTPUT_ERROR);
            return EVAL_BODY_INCLUDE;
        }

        if (createPage != null && (!createPage.equals(""))) {
            // render create page
            try {
                return renderPage(createPage, out, currentCommunity, "Create",
                        request);
            } catch (IOException e) {
                LOG.error("Create Page: An error occured in outputing the"
                        + " create page for community " + currentCommunity, e);
            } catch (SAXException e) {
                LOG.error("Create Page: An error occured in rendering the XSLT"
                        + " create page for community " + currentCommunity, e);
            }
            request.setAttribute(ErrorCodes.ERROR_CODE,
                    ErrorCodes.CREATE_OUTPUT_ERROR);
            return EVAL_BODY_INCLUDE;
        }

        // render default stylesheet
        LOG.debug("CreateTag: About to exit and render def stylesheet");
        return renderDefaultStylesheet(request, currentCommunity, defaultPage,
                adapter.RMgetSchemaLocation(currentCommunity), out, "create", adapter);
    }
}