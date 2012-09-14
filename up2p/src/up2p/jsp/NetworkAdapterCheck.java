package up2p.jsp;

//import java.io.IOException;

//import javax.servlet.RequestDispatcher;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
//import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

//import org.apache.log4j.Logger;

//import up2p.core.NetworkAdapterInfo;
//import up2p.core.ResourceManager;
//import up2p.core.WebAdapter;
//import up2p.servlet.AbstractWebAdapterServlet;

/**
 * Checks if the network adapter for the current community is available. If it
 * is not, the user is forwarded to the community disabled page, otherwise the
 * body of the tag is processed.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class NetworkAdapterCheck extends TagSupport {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Log used by this class. */
   // private static Logger LOG = Logger.getLogger(NetworkAdapterCheck.class);

    /** Used to send to disabled page. */
    private String mode;

    /*
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
    	return EVAL_PAGE;
        /*Removed === Alan 01 feb 2008
         * 
         *HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext
                .getResponse();
        HttpSession session = request.getSession(true);

        // get the WebAdapter
        WebAdapter adapter = (WebAdapter) pageContext.getAttribute("adapter",
                PageContext.APPLICATION_SCOPE);
        //ResourceManager rm = adapter.getResourceManager();
        String currentCommunity = (String) session
                .getAttribute(AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);

        return EVAL_PAGE;
        
        /* 
         * Check if the network adapter is available, otherwise go to community
         * disabled page
         * / 
        boolean disabled = false;
        String providerClass = rm
                .getNetworkAdapterProviderClass(currentCommunity);
        String providerVersion = rm
                .getNetworkAdapterProviderVersion(currentCommunity);
        if (providerClass == null || providerVersion == null)
            disabled = true;
        if (!disabled) {
            NetworkAdapterInfo na = adapter.getNetworkAdapterInfo(
                    providerClass, providerVersion);
            if (na == null)
                disabled = true;
        }
        if (disabled) {
            // forward to the disabled page
            String redirect = response
                    .encodeURL("/disabled.jsp?up2p:community="
                            + currentCommunity + "&mode=" + getMode());
            RequestDispatcher rd = request.getRequestDispatcher(redirect);
            if (rd != null) {
                LOG
                        .debug("NetworkAdapterCheck: Invalid Network Adapter, redirecting "
                                + "to disabled page.");
                try {
                    rd.forward(request, response);
                } catch (ServletException e1) {
                    LOG.error(
                            "NetworkAdapterCheck: Error forwarding to community disabled."
                                    + " Community id " + currentCommunity, e1
                                    .getRootCause());
                } catch (IOException e1) {
                    LOG.error(
                            "NetworkAdapterCheck: Error forwarding to community disabled."
                                    + " Community id " + currentCommunity, e1);
                }
            } else {
                LOG
                        .error("NetworkAdapterCheck: Error getting request dispatcher.");
            }
            return SKIP_PAGE;
        }
        return EVAL_PAGE;*/
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
}