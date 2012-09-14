package up2p.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import up2p.core.WebAdapter;

/**
 * A search query that contains a list of search terms where each term is an
 * XPath paired with a string value that must be contained in the target
 * document. Optionally, a query can be created by specifying a complete XPath
 * query string.
 * 
 * @author Neal Arthorne
 * @author Alexander Craig
 * @version 1.1
 */
public class SearchQuery {
    private static Logger LOG;

    /**
     * Removes illegal characters that cause problems when placed in an XPath
     * query expression and replaces them with the question mark character.
     * 
     * @param input query string to fix
     * @return query string with problem characters replaced
     */
    public static String normalizeQueryString(String input) {
        StringBuffer output = new StringBuffer(input);
        for (int i = 0; i < output.length(); i++) {
            if (output.charAt(i) == '\'' || output.charAt(i) == '"'
                    || output.charAt(i) == '<' || output.charAt(i) == '>') {
                output.setCharAt(i, '?');
            }
        }
        return output.toString();
    }

    /**
     * Takes an xpath and a value and generates a case-insensitive substring
     * search expression (using translate() and contains()). Recognizes *
     * as a special wild card character which means "all results".
     * 
     * @param xpath	The xpath for the string to check against
     * @param value	The value of the substring that should be found
     * @return An XPath query for the substring search
     */
    public static String subStringSearch(String xpath, String value) {
    	// Note: Xindice does not support * wildcards in strings as eXist did
    	// To emulate functionality, translate() and contains() are being used to perform
    	// case-insensitivity and substring searches respectively.
    	
    	if (value.equals("*")) {
    		return xpath + "[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '')]";
    	} else {
    		return xpath + "[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" 
    		+ normalizeQueryString(value).toLowerCase() + "')]";
    	}
    }
    
    /**
     * Takes an xpath and a value and generates a case sensitive string
     * search expression (must be an exact match). Does not recognize any
     * wildcard characters.
     * 
     * @param xpath	The xpath for the string to check against
     * @param value	The value of the string that should be found
     * @return An XPath query for the string search
     */
    public static String exactStringSearch(String xpath, String value) {
    	return xpath + "[.='" + value + "']";
    }

    /** Max results to be returned by query. */
    private int maxResults = 100;

    /** Map of operators indexed by XPath. */
    private Map<String,SearchOperator> operators;

    /** Maps of operands indexed by XPath. */
    private Map<String,String> ops;

    /** XPath query string generated once all operands have been added. */
    private String query;
    
    /** 
     * Determines whether a query should be generated as a substring match or an
     * exact match. Defaults to a substring search.
     */
    private boolean exactMatch;

    /**
     * Constructs an empty search query.
     */
    public SearchQuery() {
        ops = new HashMap<String,String>();
        operators = new HashMap<String,SearchOperator>();
        LOG = Logger.getLogger(WebAdapter.LOGGER);
        exactMatch = false;
    }

    /**
     * Constructs a search query using the given XPath query.
     * 
     * @param xPathQuery the XPath query to use
     */
    public SearchQuery(String xPathQuery) {
        query = xPathQuery;
        exactMatch = false;
    }
    
    /**
     * Sets whether the query will be generated as a substring match or an
     * exact match. Defaults to substring search for a newly constructed query.
     * @param exactMatch	True if the match should be exact, false for a sub-string search
     */
    public void setExactMatch(boolean exactMatch) {
    	this.exactMatch = exactMatch;
    }

    /**
     * Convenience method for adding an AND term to the query with a value match
     * the contents of the XPath location denoted by the given XPath.
     * 
     * @param xpath a valid XPath location
     * @param value the value that must match at the given XPath location
     */
    public void addAndOperand(String xpath, String value) {
        addOperand(xpath, value, new SearchOperator(SearchOperator.AND));
    }

    /**
     * Adds a term to the query with a value that must match the contents of the
     * XPath location denoted by the given XPath and appended to the current
     * expression using the given SearchOperator.
     * 
     * @param xpath a valid XPath location
     * @param value the value that must match at the given XPath location
     * @param searchOperator the operator to use
     */
    public void addOperand(String xpath, String value,
            SearchOperator searchOperator) {
        ops.put(xpath, value);
        operators.put(xpath, searchOperator);
    }

    /**
     * Convenience method for adding an OR term to the query with a value that
     * must match the contents of the XPath location denoted by the given XPath.
     * 
     * @param xpath a valid XPath location
     * @param value the value that must match at the given XPath location
     */
    public void addOrOperand(String xpath, String value) {
        addOperand(xpath, value, new SearchOperator(SearchOperator.OR));
    }

    /**
     * Builds an XPath search expression from the terms of the search query.
     * 
     * @return the entire XPath query
     */
    protected String buildXPathQuery() {
        // sample XPath:
        // /community[name &= 'xeno' and category &= 'philosophy']
        StringBuffer queryStr = new StringBuffer();
        Iterator<String> i = getSearchXPaths();

        String firstStep = null;
        String leadingSlashes = "/";
        
        while (i.hasNext()) {
            StringBuffer xpath = new StringBuffer((String) i.next());
            String originalXPath = xpath.toString();
            String value = getSearchValue(originalXPath);
            
            // Set the total XPath to relative if any of the provided paths are relative
            // TODO: Should really verify that all provided paths are either relative or
            // non-relative, but not both.
            if(originalXPath.startsWith("//")) {
            	leadingSlashes = "//";
            }

            /*
             * Get the first location step from the XPath which should be the
             * same for all XPaths.
             */
            while (xpath.charAt(0) == '/')
                xpath.deleteCharAt(0);
            
            String tempStep = xpath.substring(0, xpath.indexOf("/"));
            if (firstStep != null) {
                if (!firstStep.equals(tempStep)) {
                    LOG
                            .warn("SearchQuery Found mismatched first location step: '"
                                    + tempStep
                                    + "' should be '"
                                    + firstStep
                                    + "'");
                    // add first step to fit in with the rest of the XPaths
                    xpath = new StringBuffer(firstStep + "/" + xpath.toString());
                }
            } else {
                firstStep = tempStep;
        	}

            // strip first location step
            xpath.delete(0, firstStep.length() );//+ 1);
            
            // All that follows will be in the boolean expression to be evaluated
            
            // Strip /@ in attributes to @
            if (xpath.substring(0,1).equals("/")) {
            	xpath.deleteCharAt(0);
            }

            // add each term to the query
            if (xpath.length() > 0 && value != null && value.length() > 0) {
                // add the operator
                if (queryStr.length() != 0) {
                    SearchOperator op = (SearchOperator) operators
                            .get(originalXPath);
                    queryStr.append(" " + op.getOp() + " ");
                }
                
                // add the term
                if(exactMatch) {
	                queryStr.append(exactStringSearch(xpath.toString(), value));
                } else {
                	queryStr.append(subStringSearch(xpath.toString(), value));
                }
            }
        }
        
        // insert first step and closing brackets
        queryStr.insert(0, leadingSlashes + firstStep + "[");
        queryStr.append("]");
        
        LOG.info("Generated query: " + queryStr);
        return queryStr.toString();
    }

    /**
     * Returns the maximum number of results that should be returned by this
     * query.
     * 
     * @return max number of results
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Returns the number of XPath value pairs in this search.
     * 
     * @return number of operands (XPath-value pair) in this search
     */
    public int getOperandCount() {
        return ops.size();
    }

    /**
     * Returns the operator for an XPath term. If operator was not specified
     * when the operand was added, it will be an AND operator.
     * 
     * @param xPath XPath for the operator
     * @return the operator or <code>null</code> if the XPath was not found
     */
    public SearchOperator getOperator(String xPath) {
        if (operators.containsKey(xPath))
            return (SearchOperator) operators.get(xPath);
        return null;
    }

    /**
     * Returns the complete XPath query as specified in the constructor or as
     * assembled from the operands added.
     * 
     * @return the full XPath query
     */
    public String getQuery() {
        if (query != null)
            return query;
        return buildXPathQuery();
    }
    
    /**
     * Returns a value for a specific search XPath.
     * 
     * @param xPath the XPath for which the value will be returned
     * @return the value for the XPath or <code>null</code> if not found
     */
    public String getSearchValue(String xPath) {
        Object o = ops.get(xPath);
        if (o != null)
            return (String) o;
        return null;
    }

    /**
     * Returns the list of search XPaths.
     * 
     * @return a list of <code>String</code> s denoting XPaths
     */
    public Iterator<String> getSearchXPaths() {
        return ops.keySet().iterator();
    }

    /**
     * Set the maximum number of results that should be returned by this query.
     * 
     * @param maxResults maximum number of results to return
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Sets the entire XPath query, overriding a query that would be otherwise
     * generated from a list of added operands.
     * 
     * @param xPathQuery the new XPath query
     */
    public void setQuery(String xPathQuery) {
        query = xPathQuery;
    }
}