package schematool.jsp;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A custom JSP tag for conditional processing of a tag
 * body based on the presence of a request parameter.
 * Empty strings are not counted as valid parameter values.
 * Optionally, the tag body will only be processed if the
 * parameter matches a given value or length.
 *
 * @author Neal Arthorne
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class IfParameterTag extends TagSupport {

	private String param;
	private String value;
	private int minLength;

	/**
	 * Sets the name of the parameter to validate.
	 * 
	 * @param parameter the name of the parameter
	 */
	public void setParameter(String parameter) {
		param = parameter;
	}

	/**
	 * Sets the minimum length for a valid value.
	 * 
	 * @param length an integer expressed as a string that
	 * is the minimum length of the value
	 */
	public void setMinLength(String length) {
		try {
			minLength = Integer.parseInt(length);
		} catch (NumberFormatException e) {
			minLength = 0;
		}
	}

	/**
	 * Sets the acceptable value for the parameter.
	 * 
	 * @param value the only valid value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Does the parameter validation.
	 * 
	 * @return <code>EVAL_BODY_INCLUDE</code> if the validation was
	 * successful, <code>SKIP_BODY</code> otherwise
	 */
	public int doStartTag() throws JspException {
		ServletRequest request = pageContext.getRequest();
		String submittedValue = request.getParameter(param);

		if (submittedValue != null && submittedValue.length() > 0) {
			if (value != null) {
				// user has specified a value to match
				return value.equals(submittedValue)
					? EVAL_BODY_INCLUDE
					: SKIP_BODY;
			} else if (minLength > 0) {
				// user has specified a minimum length
				return submittedValue.length() >= minLength
					? EVAL_BODY_INCLUDE
					: SKIP_BODY;
			} else {
				return EVAL_BODY_INCLUDE;
			}
		}
		return SKIP_BODY;
	}
}