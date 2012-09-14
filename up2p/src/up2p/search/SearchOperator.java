package up2p.search;

/**
 * For a standard search query with XPath/value pairs known as operands, a
 * <code>SearchOperator</code> is placed between them to form the complete
 * XPath query.
 * 
 * <p>
 * e.g.
 * <code>stamps/country[. &= "canada"] and stamps/description[. &= "domestic"]</code>
 * 
 * <p>
 * In the above expression, 'and' is the SearchOperator defined by this class.
 * <br>
 * <b>Note: </b> The operators inside predicates (such as the &amp;= operator)
 * are <b>not </b> defined by this class.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class SearchOperator {
    /** A logical AND operator. */
    public static final int AND = 0;

    /** A logical NOT operator. */
    public static final int OR = 1;

    /** Holds the type operator. */
    protected int type;

    /**
     * Creates an operator.
     * 
     * @param opType the type of operator
     */
    public SearchOperator(int opType) {
        type = opType;
    }

    /**
     * Returns the type.
     * 
     * @return an operator type as defined in this class
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type.
     * 
     * @param type type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Returns the operator as a string.
     * 
     * @return the operator
     */
    public String getOp() {
        switch (type) {
        case AND:
            return "and";
        case OR:
            return "or";
        default:
            return "";
        }
    }

    public String toString() {
        return getOp();
    }
}