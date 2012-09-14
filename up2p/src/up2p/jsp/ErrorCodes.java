package up2p.jsp;

/**
 * Error codes for operations performed in JSPs. JSPs provide a map to JSP tags
 * that contains error messages for each error that can occur on the page. This
 * allows all error messages to be written in the JSPs and localized if
 * necessary.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class ErrorCodes {
    /** Attribute name where the error code is stored for access by the JSPs. */
    public static final String ERROR_CODE = "error.code";

    /**
     * Attribute name where the exception that caused the error is stored, if it
     * is available.
     */
    public static final String ERROR_EXCEPTION = "error.exception";

    /** An invalid community was specified in the request. */
    public static final Integer COMMUNITY_NOT_FOUND = new Integer(100);

    /** The XML Schema for the community was not found. */
    public static final Integer COMMUNITY_SCHEMA_NOT_FOUND = new Integer(101);

    /** Error occured in rendering the output of the create page. */
    public static final Integer CREATE_OUTPUT_ERROR = new Integer(102);

    /** Create page (XSLT or HTML) was not found. */
    public static final Integer CREATE_PAGE_NOT_FOUND = new Integer(103);

    /** Default stylesheet required to render a default page was not found. */
    public static final Integer DEFAULT_STYLESHEET_NOT_FOUND = new Integer(104);

    /** Error occured in rendering the output of the default stylesheet. */
    public static final Integer DEFAULT_STYLESHEET_OUTPUT_ERROR = new Integer(
            105);

    /** Error occured in rendering the output of the search page. */
    public static final Integer SEARCH_OUTPUT_ERROR = new Integer(106);

    /** Search page (XSLT or HTML) was not found. */
    public static final Integer SEARCH_PAGE_NOT_FOUND = new Integer(107);
    
    /** Error occured in rendering the output of the home page. */
    public static final Integer HOME_OUTPUT_ERROR = new Integer(108);
    
    /** Home page (XSLT or HTML) was not found. */
    public static final Integer HOME_PAGE_NOT_FOUND = new Integer(109);
}