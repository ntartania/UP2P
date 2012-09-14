package up2p.jsp;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import up2p.core.UserWebAdapter;
import up2p.core.WebAdapter;
import up2p.servlet.AbstractWebAdapterServlet;

/**
 * Implements the JSP tag for the search page. Body of the tag contains error
 * processing code, so if no errors occur, <code>SKIP_BODY</code> is returned.
 * Otherwise an attribute in the request is set with the error code found in
 * <code>ErrorCodes</code> class.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class SearchTag extends AbstractTag {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Log for this class. */
    private static Logger LOG = Logger.getLogger(SearchTag.class);

    /*
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        HttpSession session = request.getSession(true);
        JspWriter out = pageContext.getOut();

        // get the WebAdapter
        if (adapter == null)
            adapter = (UserWebAdapter) pageContext.getAttribute("adapter",
                    PageContext.APPLICATION_SCOPE);
       // ResourceManager rm = adapter.getResourceManager();

        // get the current community
        String currentCommunity = (String) session
                .getAttribute(AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);

        // check if requested community exists//TODO find another way of dealing with the error
        /*if (!rm.isCommunity(currentCommunity)) {
            request.setAttribute("error.code", ErrorCodes.COMMUNITY_NOT_FOUND);
            return EVAL_BODY_INCLUDE;
        }*/ 

        // render either the search page or use the default stylesheet
        String stylePage = adapter.RMgetCommunitySearchLocation(currentCommunity);
        String defaultPage = adapter.getRootPath()
                + File.separator
                + adapter.getConfigProperty(UserWebAdapter.CONFIG_DEFAULT_SEARCH,
                        null);
        LOG.debug("search page:|"+ stylePage +"|");
        if (stylePage != null && (!stylePage.equals(""))) {
            // render search page
            try {
                return renderPage(stylePage, out, currentCommunity, "Search",
                        request);
            } catch (IOException e) {
                LOG.error("Search Page: An error occured in outputing the "
                        + "search page for community " + currentCommunity, e);
            } catch (SAXException e) {
                LOG.error("Search Page: An error occured in rendering the XSLT"
                        + " search page for community " + currentCommunity, e);
            }
            request.setAttribute("error.code", ErrorCodes.SEARCH_OUTPUT_ERROR);
            return EVAL_BODY_INCLUDE;
        }

        
        // render default stylesheet
        return renderDefaultStylesheet(request, currentCommunity, defaultPage,
                adapter.RMgetSchemaLocation(currentCommunity), out, "search", adapter);
    }
}