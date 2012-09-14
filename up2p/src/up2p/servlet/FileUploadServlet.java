package up2p.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;

/**
 * Used to test file uploading to servlets.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class FileUploadServlet extends HttpServlet {

    /*
     * @see javax.servlet.http.HttpServlet#doPost
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        // create the upload handler
        FileUpload fup = new FileUpload();
        fup.setSizeMax(1000000);

        response.setContentType("text/html");
        // parse the multipart request
        try {
            boolean isMultipart = FileUploadBase.isMultipartContent(request);
            out.print("<html><body><p>Is Multipart content: " + isMultipart
                    + "</p>");
            if (isMultipart) {
                List items = fup.parseRequest(request);
                Iterator i = items.iterator();
                while (i.hasNext()) {
                    FileItem fileItem = (FileItem) i.next();
                    out.println("<b>Field name:</b> " + fileItem.getFieldName()
                            + " <b>Name:</b> " + fileItem.getName()
                            + " <b>IsFormField:</b> " + fileItem.isFormField()
                            + " <b>Size:</b> " + fileItem.getSize()
                            + " <b>IsInMemory:</b> " + fileItem.isInMemory()
                            + "<br>");
                }
            }
            out.println("</body></html>");
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
    }
}