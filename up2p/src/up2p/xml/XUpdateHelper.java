package up2p.xml;

/**
 * Provides helper methods for making XUpdate modification expressions.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class XUpdateHelper {
    /** XUpdate XML Namespace */
    public static final String XUPDATE_NS = "http://www.xmldb.org/xupdate";

    /** Namespace prefix */
    public static final String NS_PREFIX = "xup";

    /**
     * Starts every XUpdate query.
     *
     * @return prefix to modification blob
     */ 
    public static String getModificationPrefix() {
        return "<" + NS_PREFIX + ":modifications version=\"1.0\" " + "xmlns:"
                + NS_PREFIX + "=\"" + XUPDATE_NS + "\">";
    }

    /**
     * Ends every XUpdate query.
     * 
     * @return postfix to modification blob
     */
    public static String getModificationSuffix() {
        return "</" + NS_PREFIX + ":modifications>";
    }

    public static String createAttribute(String attributeName,
            String attributeValue) {
        StringBuffer buffer = new StringBuffer("<");
        buffer.append(NS_PREFIX);
        buffer.append(":attribute name=\"");
        buffer.append(attributeName);
        buffer.append("\">");
        buffer.append(attributeValue);
        buffer.append("</");
        buffer.append(NS_PREFIX);
        buffer.append(":attribute>");
        return buffer.toString();
    }

    public static String appendElement(String select, String elementName,
            String elementValue) {
        StringBuffer buffer = new StringBuffer(getModificationPrefix());
        buffer.append("<");
        buffer.append(NS_PREFIX);
        buffer.append(":append select=\"");
        buffer.append(select);
        buffer.append("\">");
        buffer.append("<");
        buffer.append(NS_PREFIX);
        buffer.append(":element name=\"");
        buffer.append(elementName);
        buffer.append("\">");
        buffer.append(elementValue);
        buffer.append("</");
        buffer.append(NS_PREFIX);
        buffer.append(":element></");
        buffer.append(NS_PREFIX);
        buffer.append(":append>");
        buffer.append(getModificationSuffix());
        return buffer.toString();
    }

    public static String update(String select, String value) {
        StringBuffer buffer = new StringBuffer(getModificationPrefix());
        buffer.append("<");
        buffer.append(NS_PREFIX);
        buffer.append(":update select=\"");
        buffer.append(select);
        buffer.append("\">");
        buffer.append(value);
        buffer.append("</");
        buffer.append(NS_PREFIX);
        buffer.append(":update>");
        buffer.append(getModificationSuffix());
        return buffer.toString();
    }

    public static String remove(String select) {
        StringBuffer buffer = new StringBuffer(getModificationPrefix());
        buffer.append("<");
        buffer.append(NS_PREFIX);
        buffer.append(":remove select=\"");
        buffer.append(select);
        buffer.append("\"/>");
        buffer.append(getModificationSuffix());
        return buffer.toString();
    }

}