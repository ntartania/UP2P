package up2p.xml.filter;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;



/**
 * Captures and filters attachment links that begin with the given prefix and
 * stores them as key/value pairs where the key is the attachment name and the
 * value is the complete attachment URL without the prefix. A 'path' attribute
 * associated with an element will be stored for lookup using the attachment
 * name, if it is found.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class FileAttachmentFilter extends AttachmentListFilter {

   
    /**
     * Constructs the filter with an empty attachment list.
     */
    public FileAttachmentFilter() {
        super("file:");
    }

    /**
     * Constructs the filter with an empty attachment list.
     * 
     * @param attachmentPrefix prefix for attachments
     */
    public FileAttachmentFilter(String attachmentPrefix) {
        super(attachmentPrefix);
        
    }

        
    /**
     * Processes the full attachment link now that it has been buffered from one
     * or more calls to <code>characters</code>.
     * Method modified by Alan sept 5th 2008, to consider only file: URLS.
     * The link stored in the nameToLinkMap is the URL.toString()
     * 
     * @param attachmentText the text containing the full link to the attachment
     * including the prefix
     * @return attachment name
     */
    protected String processAttachment(String attachmentText) {
        // store the attachment name and text
    	//this is the filename
    	String attachName;
    	if (attachmentText.lastIndexOf("/")==-1) //case where the url is file:fname.ext
    		attachName = attachmentText.substring(getAttachPrefix().length());
    	else
    		attachName = attachmentText.substring(attachmentText
    				.lastIndexOf("/") + 1);
        
        
        try {
			attachmentLinkList.add(new URL(attachmentText).getPath());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
		}
		
        // store in map for lookup by attachment name
        if (!nameToLinkMap.containsKey(attachName))
        	nameToLinkMap.put(attachName, attachmentText);
        	/*
			try {
				nameToLinkMap.put(URIUtil.decode(attachName, "UTF-8"), attachmentText);
			} catch (URIException e) {
				// Ignore decoding and print a stack trace if URI decoding fails
				nameToLinkMap.put(attachName, attachmentText);
				e.printStackTrace();
			}
			*/
        return attachName;
    }

    }