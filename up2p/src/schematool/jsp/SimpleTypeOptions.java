package schematool.jsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import schematool.core.ResourceSchema;
import schematool.core.SimpleType;

/**
 * A custom JSP tag for creating HTML <code>&lt;option&gt;</code>
 * tags for the set of Simple Types stored in the
 * <code>ResourceSchema</code> in the user's session.</p>
 * <p>The motivation for this tag is to avoid writing iteration
 * code every time a listbox needs to be displayed with all the
 * currently available Simple Types.</p>
 * 
 * <p>This tag only displays <code>&lt;option&gt;</code> tags and not the
 * enclosing <code>&lt;select&gt;</code> tags. For example, the output
 * would resemble:</p>
 * <pre>
 * &lt;option value="decimal"&gt;Decimal Number&lt;/option&gt;
 * &lt;option value="positiveInteger"&gt;Positive Integer&lt;/option&gt;
 * &lt;option value="myDerivedType"&gt;myDerivedType&lt;/option&gt;
 * </pre>
 * 
 *
 * @author Neal Arthorne
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class SimpleTypeOptions extends TagSupport {
	private String schemaId = "schema";
	private String selected = "";
	private ResourceSchema schema;
	private boolean firstBlank = false;
	private static SimpleTypeComparator COMP = new SimpleTypeComparator();

	/**
	 * Sets the key under which the current schema is stored in the user
	 * session.
	 * 
	 * @param schemaName the id for the curreny schema in the user session
	 */
	public void setSchemaId(String schemaName) {
		schemaId = schemaName;
	}

	/**
	 * Set to 'true' if the first option in the listbox
	 * is to have a blank value. Use this feature if you have listboxes
	 * that are optionally submitted with a form.
	 * 
	 * @param blank a <code>String</code> equal to 'true' to insert a blank,
	 * otherwise false is assumed and a blank value is not inserted
	 */
	public void setFirstBlank(String blank) {
		if (blank.equals("true"))
			firstBlank = true;
		else
			firstBlank = false;
	}

	/**
	 * Specify the value that should be the currently
	 * selected value in the listbox.
	 * 
	 * @param selectedValue the value to select
	 */
	public void setSelectedValue(String selectedValue) {
		selected = selectedValue;
	}

	/**
	 * Specify the value that should be the currently
	 * selected item in the listbox by specifying the
	 * name of a request parameter found in the current
	 * <code>HttpServletRequest</code>.
	 *  
	 * @param selectedReqest a parameter whose value can be found
	 * in the current request
	 */
	public void setSelectedRequestName(String selectedRequest) {
		selected = pageContext.getRequest().getParameter(selectedRequest);
		if (selected == null || selected.length() == 0)
			selected = "";
	}

	/**
	 * Iterates over all built-in and user-defined Simple Types and prints
	 * them out using their type names as values and short names in HTML
	 * <code>&lt;option&gt;</code> tags.
	 * 
	 * @return Returns <code>SKIP_BODY</code>
	 * @throws JspException when an error occurs in printing with the JSP
	 * writer
	 */
	public int doStartTag() throws JspException {
		JspWriter out = pageContext.getOut();
		// list types
		Iterator<SimpleType> typeList = getTypes();
		String sel = null;
		while (typeList.hasNext()) {
			SimpleType sType = typeList.next();
			if (selected.equals(sType.getName()))
				sel = " selected";
			else
				sel = "";
			try {
				if (firstBlank) {
					// output a blank value if the option has been set
					out.println("<option value=\"\"> -- Types -- </option>");
					firstBlank = false;
				}
				out.println(
					"<option value=\""
						+ sType.getName()
						+ "\""
						+ sel
						+ ">"
						+ sType.getShortName()
						+ "</option>");
			} catch (java.io.IOException e) {
				throw new JspException(e.getMessage());
			}
		}
		return SKIP_BODY;
	}

	/** Returns the list of SimpleType objects available for use in
	 * SchemaTool. */
	private Iterator<SimpleType> getTypes() {
		ArrayList<SimpleType> typeList = new ArrayList<SimpleType>();

		// built-in types
		Enumeration<SimpleType> builtTypes = ResourceSchema.getBuiltInTypes().elements();
		while (builtTypes.hasMoreElements()) {
			typeList.add((SimpleType) builtTypes.nextElement());
		}

		// insert user-defined types
		ResourceSchema schema =
			(ResourceSchema) pageContext.getAttribute(
				schemaId,
				PageContext.SESSION_SCOPE);
		Enumeration<SimpleType> types = schema.getSimpleTypes();
		while (types.hasMoreElements()) {
			typeList.add((SimpleType) types.nextElement());
		}

		// sort the list
		Collections.sort(typeList, COMP);

		return typeList.iterator();
	}
}

/**
 * Implements a comparator that compares short names of SimpleTypes.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
class SimpleTypeComparator implements Comparator {
	//		negative is less than, zero is equal, positive is greater than
	public int compare(Object object1, Object object2) {
		try {
			return ((SimpleType) object1).getShortName().compareTo(
				((SimpleType) object2).getShortName());
		} catch (ClassCastException e) {
			System.err.println("Error comparing SimpleTypes.");
			e.printStackTrace();
		}
		return 0;
	}
}