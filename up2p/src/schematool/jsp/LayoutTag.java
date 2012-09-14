package schematool.jsp;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Implements a layout for SchemaTool with a header and footer.
 * 
 * @author Neal Arthorne
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class LayoutTag extends TagSupport {
	/** Page title. */
	private String title;

	/**
	 * Inserts the header for each page.
	 *  
	 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		HttpServletRequest request =
			(HttpServletRequest) pageContext.getRequest();
		HttpServletResponse response =
			(HttpServletResponse) pageContext.getResponse();
		HttpSession session = request.getSession(true);

		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");

		// set the title
		if (getTitle() != null)
			request.setAttribute("title", getTitle());

		// load the header
		ServletConfig config = pageContext.getServletConfig();
		RequestDispatcher rd =
			config.getServletContext().getRequestDispatcher("/header.jsp");
		try {
			rd.include(request, response);
		} catch (ServletException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Inserts the footer and closing HTML.
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		HttpServletRequest request =
			(HttpServletRequest) pageContext.getRequest();
		HttpServletResponse response =
			(HttpServletResponse) pageContext.getResponse();
		JspWriter out = pageContext.getOut();
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// load the footer
		ServletConfig config = pageContext.getServletConfig();
		RequestDispatcher rd =
			config.getServletContext().getRequestDispatcher("/footer.jsp");
		try {
			rd.include(request, response);
		} catch (ServletException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return EVAL_PAGE;
	}

	/**
	 * Gets the title for the HTML page.
	 * 
	 * @return page title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title for the HTML page.
	 * 
	 * @param pageTitle title for the HTML page
	 */
	public void setTitle(String pageTitle) {
		title = pageTitle;
	}
}
