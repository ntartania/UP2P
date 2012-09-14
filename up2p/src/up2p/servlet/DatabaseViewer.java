package up2p.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xml.serialize.XMLSerializer;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

import up2p.xml.TransformerHelper;
import up2p.xml.Util;

/**
 * Views the contents of the XML repository.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DatabaseViewer extends AbstractWebAdapterServlet {
    private XMLSerializer serializer;

    /**
     * Displays the XML db as XML in the browser. Used for inspecting the state
     * of the database.
     * 
     * @param request request
     * @param response response
     * @throws ServletException if an error occurs writing to the response
     * @throws IOException if an error occurs writing to the response
     */
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        // create the Xerces serializer
        serializer = new XMLSerializer(response.getWriter(), TransformerHelper
                .getOutputFormat(TransformerHelper.PLAIN, false, true, true));
        serializer.asDOMSerializer();

        // output the collections
       displayAllCollections(response.getWriter());
    }

    /**
     * Displays all the collections in the database associated with the web
     * adapter.
     * 
     * @param out the writer to display the output to
     */
    public void displayAllCollections(PrintWriter out) {
        try {
            String collectionUrl = "xmldb:xindice://localhost:8080/db/";
            Collection root = DatabaseManager.getCollection(collectionUrl);
            Util.displayCollection(root, out, 0, serializer);
        } catch (XMLDBException e) {
            LOG.fatal("DatabaseViewer Could not retrieve root collection.");
        }
    }

}