 package up2p.jsp;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

//import up2p.core.InitializerThread;
import up2p.core.WebAdapter;
import up2p.core.UserWebAdapter;
import up2p.servlet.AbstractWebAdapterServlet;

/**
 * Implements the layout of the JSP pages.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class LayoutTag extends TagSupport {
    /** Log for this class. */
    private static Logger LOG = Logger.getLogger(LayoutTag.class);

    /** Page title. */
    private String title;

    /** Display mode: create, search, view, download */
    private String mode;

    /** Refresh for the page in seconds. Uses META refresh tag in HTML. */
    private String refresh;
    
    /** The javascript file to attach in the head of the page */
    private String jscript;

    /**
     * Inserts the header for each page.
     * 
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext
                .getResponse();
        HttpSession session = request.getSession(true);

        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // check if initialized
        UserWebAdapter adapter = (UserWebAdapter) pageContext.getAttribute("adapter",
                PageContext.APPLICATION_SCOPE);
        if (adapter.getInitialized() != 4) {
            try {
                response.sendRedirect(response.encodeRedirectURL("init.jsp"));
            } catch (IOException e) {
                LOG.error("Error in LayoutTag.", e);
            }
            return Tag.SKIP_BODY;
        }

        String currentCommunity = (String) session
                .getAttribute(AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);

        // set the current community in the request
        request.setAttribute(AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID,
                currentCommunity);

        String layoutMode = getMode();
        
        if (layoutMode == null) {
            layoutMode = "view";
            setMode(layoutMode);
        }
        
        // Set the header image to display for the active community
        // (default to header_logo.png in the U-P2P root directory)
        String headerLogo = adapter.RMgetCommunityHeaderLogo(currentCommunity);
        if(headerLogo == null) {
        	headerLogo = "/" + adapter.getUrlPrefix() + "/header_logo.png";
        }
        request.setAttribute("up2p.layout.header", headerLogo);
        
        
        Iterator stylesheets = null;
        if (layoutMode.equals("search")) {
            stylesheets = adapter.RMgetCommunitySearchStylesheet(currentCommunity);
        } else if (layoutMode.equals("search_results")) { 
            stylesheets = adapter.RMgetCommunitySearchResultsStylesheet(currentCommunity);
        } else if (layoutMode.equals("create")) { 
            stylesheets = adapter.RMgetCommunityCreateStylesheet(currentCommunity);
        } else if (layoutMode.equals("home")) { 
            stylesheets = adapter.RMgetCommunityHomeStylesheet(currentCommunity);
        } else {
            stylesheets = adapter.RMgetCommunityDisplayStylesheet(currentCommunity);
        }
        
        // set the CSS stylesheets for the <head> section of the output
        request.setAttribute("up2p.layout.stylesheets", stylesheets);

        // set the title
        if (title != null)
            request.setAttribute("up2p.layout.title", "U-P2P: " + title);
        else
            request.setAttribute("up2p.layout.title", "U-P2P");

        // set the mode for display
        request.setAttribute("up2p.layout.mode", getMode());

        // set the refresh rate
        if (refresh != null)
            request.setAttribute("up2p.layout.refresh", getRefresh());
        
        // set the javascript attachment(s)
        String jscriptString = " ";
        if (jscript != null) {
        	jscriptString += getJscript();
        }
        if(layoutMode.equals("search_results")) {
	        Iterator<String> communityJavascript = adapter
	        		.RMgetCommunitySearchResultsJavascript(currentCommunity);
	        while(communityJavascript.hasNext()) {
	        	jscriptString += " " + communityJavascript.next();
	        }
        }
        if(!jscriptString.equals(" ")) {
        	request.setAttribute("up2p.layout.jscript", 
        			jscriptString.substring(1)); // Get rid of leading space
        }
        
        // load the header
        try {
            ServletConfig config = pageContext.getServletConfig();
            RequestDispatcher rd = config.getServletContext()
                    .getRequestDispatcher(
                            adapter.getConfigProperty("up2p.jsp.header",
                                    "/header.jsp"));
            rd.include(request, response);
        } catch (IOException e) {
            LOG.error("IO error including when header.", e);
        } catch (ServletException e) {
            LOG.error("ServletException when including header.", e
                    .getRootCause());
            e.printStackTrace();
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Inserts the footer and closing HTML.
     * 
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext
                .getResponse();
        JspWriter out = pageContext.getOut();
        try {
            out.flush();
        } catch (IOException e) {
            LOG.error("Error flushing output.", e);
        }

        // get the WebAdapter
        UserWebAdapter adapter = (UserWebAdapter) pageContext.getAttribute("adapter",
                PageContext.APPLICATION_SCOPE);

        // load the footer
        try {
            ServletConfig config = pageContext.getServletConfig();
            RequestDispatcher rd = config.getServletContext()
                    .getRequestDispatcher(
                            adapter.getConfigProperty("up2p.jsp.footer",
                                    "/footer.jsp"));
            rd.include(request, response);
        } catch (Exception e) {
            LOG.error("Error including header.", e);
        }
        return EVAL_PAGE;
    }

    /**
     * Gets the page title.
     * 
     * @return page title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the page title.
     * 
     * @param string page title
     */
    public void setTitle(String string) {
        title = string;
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
     * Returns the refresh rate of the page in seconds.
     * 
     * @return refresh rate of the page in seconds
     */
    public String getRefresh() {
        return refresh;
    }

    /**
     * Sets the refresh rate of the page in seconds.
     * 
     * @param string refresh rate of the page in seconds
     */
    public void setRefresh(String string) {
        refresh = string;
    }

    /**
     * Sets the javascript file to be attached to the page.
     * 
     * @param jscript	the javascript file to be attached to the page
     */
	public void setJscript(String jscript) {
		this.jscript = jscript;
	}

	/**
	 * Returns the javascript file attached to the page.
	 * 
	 * @return the javascript file attached to the page
	 */
	public String getJscript() {
		return jscript;
	}
}