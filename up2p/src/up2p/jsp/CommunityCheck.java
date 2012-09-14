package up2p.jsp;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

//import up2p.core.ResourceManager;
import up2p.core.UserWebAdapter;
//import up2p.core.WebAdapter;
import up2p.servlet.AbstractWebAdapterServlet;

/**
 * Checks if the current community exists and if not, forwards to an error page.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class CommunityCheck extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Log used by this class. */
    private static Logger LOG = Logger.getLogger(CommunityCheck.class);

    /** Used to send to the error page. */
    private String mode;

    /** Error message to display if community is not found. */
    private String errorMsg;

    /*
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext
                .getResponse();
        HttpSession session = request.getSession(true);

        // get the WebAdapter
        UserWebAdapter adapter = (UserWebAdapter) pageContext.getAttribute("adapter",
                PageContext.APPLICATION_SCOPE);
        //ResourceManager rm = adapter.getResourceManager();

        // get current community
        String currentCommunity = (String) session
                .getAttribute(AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);
        if (request.getParameter("up2p:community") != null) {
            currentCommunity = request.getParameter("up2p:community");
        }
        if (currentCommunity == null || currentCommunity.length() == 0) {
            currentCommunity = adapter.getRootCommunityId();
        }
        session.setAttribute(AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID,
                currentCommunity);

        /* Check if the community is available */
        boolean enabled = adapter.RMisCommunity(currentCommunity);
        if (enabled)
            return EVAL_PAGE;

        // forward to the disabled page
        request.setAttribute("error.mode", getMode());
        request.setAttribute("error.msg", getErrorMsg());
        String redirect = response.encodeURL("/errorPage.jsp");
        RequestDispatcher rd = request.getRequestDispatcher(redirect);
        if (rd != null) {
            LOG.debug("CommunityCheck: Invalid community ID, forwarding to "
                    + "error page.");
            try {
                rd.forward(request, response);
            } catch (ServletException e1) {
                LOG.error("CommunityCheck: Error forwarding to error page.", e1
                        .getRootCause());
            } catch (IOException e1) {
                LOG
                        .error(
                                "CommunityCheck: Error forwarding to error page.",
                                e1);
            }
        } else {
            LOG.error("CommunityCheck: Error getting request dispatcher.");
        }
        return SKIP_PAGE;
    }

    /**
     * Returns the mode.
     * 
     * @return mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     * 
     * @param string mode
     */
    public void setMode(String string) {
        mode = string;
    }

    /**
     * Gets the error message displayed when community is not found.
     * 
     * @return error message for community not found
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Sets the error message displayed when community is not found.
     * 
     * @param string error message to display
     */
    public void setErrorMsg(String string) {
        errorMsg = string;
    }

}