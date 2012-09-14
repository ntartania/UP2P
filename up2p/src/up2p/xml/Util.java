package up2p.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.XMLSerializer;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Utility functions for displaying XML.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class Util {

    /**
     * Displays a single collection and all its resources.
     * 
     * @param col the collection to display
     * @param outStream the stream to display the output to
     * @param indent the amount to indent each nested element
     * @param serializer DOM serializer to use
     * @throws XMLDBException when a database error occurs
     */
    public static void displayCollection(Collection col,
            OutputStream outStream, int indent, DOMSerializer serializer)
            throws XMLDBException {
        displayCollection(col, new PrintWriter(outStream, true), indent,
                serializer);
    }

    /**
     * Displays a single collection and all its resources.
     * 
     * @param col the collection to display
     * @param out the writer to display the output to
     * @param indent the amount to indent each nested element
     * @param serializer DOM serializer to use
     * @throws XMLDBException when a database error occurs
     */
    public static void displayCollection(Collection col, PrintWriter out,
            int indent, DOMSerializer serializer) throws XMLDBException {
        Util.indent(out, indent, ' ');
        out
                .println("<collection name=\"" + col.getName()
                        + "\" resourceCount=\"" + col.getResourceCount()
                        + "\" childrenCount=\"" + col.getChildCollectionCount()
                        + "\">");

        String[] resources = col.listResources();
        for (int i = 0; i < resources.length; i++) {
            Util.indent(out, indent, ' ');
            out.println("<resource id=\"" + resources[i] + "\">");
            XMLResource xmlRes = (XMLResource) col.getResource(resources[i]);
            // workaround for inability of Apache XMLSerializer to correctly
            // serialize a DOM tree provided by the Exist database
            // TODO workaround for Exist DOM serializing
            // This DOES NOT properly escape characters when displaying XML
            out.println(xmlRes.getContentAsDOM().getFirstChild());
            //			try {
            //				serializer.serialize((Element) xmlRes.getContentAsDOM());
            //			} catch (IOException e) {
            //				e.printStackTrace();
            //			} catch (XMLDBException e) {
            //				e.printStackTrace();
            //			}
            out.println("</resource>");
        }

        if (col.getChildCollectionCount() > 0) {
            Util.indent(out, indent, ' ');
            out.println("<childCollections>");
            String[] children = col.listChildCollections();
            indent += 2;
            for (int j = 0; j < children.length; j++) {
                displayCollection(col.getChildCollection(children[j]), out,
                        indent, serializer);
            }
            indent -= 2;
            out.println("</childCollections>");
        }

        Util.indent(out, indent, ' ');
        out.println("</collection>");

    }

    /**
     * Indents the given number of indent characters on the given print writer.
     * 
     * @param out the writer to output the characters to
     * @param indentAmount the number of character to indent
     * @param character the character to use when indenting
     */
    public static void indent(PrintWriter out, int indentAmount, char character) {
        for (int i = 0; i < indentAmount; i++) {
            out.print(character);
        }
    }

    /**
     * Returns a DOM serializer using UTF-8 encoding with no XML declaration.
     * 
     * @param writer place to serialize XML to
     * @return DOM serializer
     */
    public static DOMSerializer getDOMSerializer(Writer writer) {
        XMLSerializer s = new XMLSerializer(writer, TransformerHelper
                .getOutputFormat("UTF-8", false, true, true));
        try {
            return s.asDOMSerializer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * Returns a DOM serializer using UTF-8 encoding with no XML declaration.
     * 
     * @param outStream place to serialize XML to
     * @return DOM serializer
     */
    public static DOMSerializer getDOMSerializer(OutputStream outStream) {
        XMLSerializer s = new XMLSerializer(outStream, TransformerHelper
                .getOutputFormat("UTF-8", false, true, true));
        try {
            return s.asDOMSerializer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

}