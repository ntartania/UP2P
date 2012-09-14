package up2p.xml.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Replaces a list of attachments with a given list of replacement links.
 * Attachments are counted in the order in which they appear in the document,
 * including attachment links that appear as whitespace seperated lists in
 * attributes.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class AttachmentReplacer extends BaseResourceFilter {
	
	/*
	public static AttachmentReplacer createUrlEncoderFilter(
			AttachmentListFilter attachListFilter) {
		// create empty list that matches list from given filter
        List<String> names = attachListFilter.getAttachmentList();
        List<String> urlNames = new ArrayList<String>();
        
        for (String attachName : names){
        	urlNames.add()
        }

        return new AttachmentReplacer(attachListFilter.getAttachmentList(),
                shortnames, attachListFilter.getAttachPrefix());
	}
	*/

    /**
     * Creates an attachment replacer that replaces the links in the given
     * filter with empty strings. This effectively removes the links.
     * NOTE: This method is only usable once the input filter has been applied (doFilter)
     * 
     * @param attachListFilter filter that contains a list of attachment links
     * to remove
     * @return replacer that will replace the attachment links by just the filenames
     */
    public static AttachmentReplacer createRemovalFilter (
            AttachmentListFilter attachListFilter) {
        // create empty list that matches list from given filter
        List<String> names = attachListFilter.getAttachmentList();
        List<String> shortnames = new ArrayList<String>();
        
        for (int i=0;i<names.size();i++){
        	// Just keep the attachment name -- can't use the getNames() method because they might not be in order
        	
        	// TODO: the replaceAll expression doesn't make sense to me -- I assume the pattern on the left will only be matched once 
        	// so what is the point of replacing it with the first match?
        	
        	// Answer: This is part of the name mangling scheme. The regex will replace any instance of "_up2p_<number>.extension"
        	// with just ".extension". This expression also supports files with multiple extensions (ex. test.zip.bak)
        	shortnames.add(names.get(i).substring(names.get(i).lastIndexOf("/")+1));
        }

        return new AttachmentReplacer(attachListFilter.getAttachmentList(),
                shortnames, attachListFilter.getAttachPrefix());
    }

    /** Refers to the attachment currently being processed. */
    private int attachmentCounter;

    /** Ordered list of attachment links. */
    private List<String> attachmentLinks;

    /** Prefix used to look for attachment links. */
    private String attachPrefix;

    /** Offset for when links are split across two character events. */
    private int splitOffset;

    /**
     * List of original attachment links that will be used to compare to the
     * links found by this filter.
     */
    private List<String> watchLinks;

    /**
     * Replaces the attachment links in the ordered watch list with the
     * attachment links in the ordered replacement list. SAX character events
     * that generate the text for one link may be split across two events so we
     * need the full list of attachment links to watch for to make sure we are
     * seeing the whole link.
     * 
     * 
     * @param watchList list of attachment links in the order in which they
     * appear in the document
     * @param replacementLinks list of replacement links in the order in which
     * they appear in the document
     */
    public AttachmentReplacer(List<String> watchList, List<String> replacementLinks) {
        this(watchList, replacementLinks,
                AttachmentListFilter.DEFAULT_ATTACH_PREFIX);
    }

    /**
     * Replaces the attachment links in the ordered watch list with the
     * attachment links in the ordered replacement list.
     * 
     * @param watchList list of attachment links in the order in which they
     * appear in the document
     * @param replacementLinks list of replacement links in the order in which
     * they appear in the document
     * @param attachmentPrefix the prefix with which each attachment link starts
     */
    public AttachmentReplacer(List<String> watchList, List<String> replacementLinks,
            String attachmentPrefix) {
        watchLinks = watchList;
        attachmentLinks = replacementLinks;
        attachPrefix = attachmentPrefix;
    }

    /*
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String charData = new String(ch, start, length).trim();

        // match character data to the attachment prefix
        if (charData.startsWith(attachPrefix)
                && attachmentCounter < attachmentLinks.size()) {
            // start of attachment link
            String fullLink = (String) watchLinks.get(attachmentCounter);
            /*if (!fullLink.startsWith(attachPrefix))
                fullLink = attachPrefix + fullLink;*/
            String replaceData = (String) attachmentLinks
                    .get(attachmentCounter);

            //here I expect the received charData to be shorter !
            if (charData.length()>fullLink.length()){
            	if (charData.contains(fullLink)){
            		// need to return the original char array but
                    // with our new value inserted
                    StringBuffer output = new StringBuffer(new String(ch));
                    // remove the old characters
                    output.delete(start, start + length);
                    // insert the new characters
                    output.insert(start, replaceData);
                    // return the char array with the new value
                    super.characters(output.toString().toCharArray(), start,
                            replaceData.length());

                    // increase attachment counter
                    attachmentCounter++;
            	}
            	}

            // if received data is same length as watch data, we can
            // replace directly
            else if (charData.length() == fullLink.length()) {
                // need to return the original char array but
                // with our new value inserted
                StringBuffer output = new StringBuffer(new String(ch));
                // remove the old characters
                output.delete(start, start + length);
                // insert the new characters
                output.insert(start, replaceData);
                // return the char array with the new value
                super.characters(output.toString().toCharArray(), start,
                        replaceData.length());

                // increase attachment counter
                attachmentCounter++;
            } else {
                // characters we received are first part of a link
                splitOffset = length;
                // return first portion of data
                StringBuffer output = new StringBuffer(new String(ch));
                // remove the old characters
                output.delete(start, start + length);
                // insert the new characters
                if (replaceData.length() < length) {
                    // replacement data is shorter than end of character data
                    output.insert(start, replaceData);
                    // return the char array with the new value
                    super.characters(output.toString().toCharArray(), start,
                            replaceData.length());
                } else {
                	
                    output.insert(start, replaceData.substring(0, length));
                    // return the char array with the new value
                    super.characters(output.toString().toCharArray(), start,
                            length);
                }
            }
        } else if (splitOffset > 0) {
            // Link is split across the SAX read buffer (2048 bytes)
            // so two character events are generated for one link.
            // This is the second event.
            String replaceData = (String) attachmentLinks
                    .get(attachmentCounter);
            // Return second portion of data
            StringBuffer output = new StringBuffer(new String(ch));
            // remove the old characters - assume it's all char data given
            output.delete(start, start + length);
            // insert the new characters
            if (replaceData.length() > splitOffset) {
                output.insert(start, replaceData.substring(splitOffset));//
                // return the char array with the new value
                super.characters(output.toString().toCharArray(), start,
                        replaceData.length()- splitOffset);// 
            } else {
                // replacement data is smaller than offset so don't send
                // any character data onwards
                super.characters(output.toString().toCharArray(), start, 0);
            }
            // reset split offset
            splitOffset = 0;
            // increase attachment counter
            attachmentCounter++;
        } else
            super.characters(ch, start, length);
    }

    /*
     * @see org.xml.sax.ContentHandler#startElement(String, String, String,
     * org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        // create a copy of the given attributes
        AttributesImpl attsCopy = new AttributesImpl(atts);

        // record if any attributes have been modified
        boolean attributesChanged = false;

        // look for attachments in the attributes
        /*
         * Attributes may have whitespace-separated lists of attachments so we
         * parse the attribute value and process each one individually.
         */
        for (int i = 0; i < attsCopy.getLength(); i++) {
            // get the attribute value
            String attrValue = attsCopy.getValue(i);

            // check if it starts with the attachment prefix
            if (attrValue.startsWith(attachPrefix)) {
                attributesChanged = true;
                // attachment(s) found

                // create buffer for writing back a list of values
                StringBuffer compoundAttrValue = new StringBuffer();

                // parse list of attachments
                StringTokenizer tokens = new StringTokenizer(attrValue);
                while (tokens.hasMoreTokens()) {
                    String singleValue = tokens.nextToken();
                    // replace the link
                    if (singleValue.startsWith(attachPrefix)) {
                        // get the replacement attachment link
                        String replaceData = (String) attachmentLinks
                                .get(attachmentCounter);

                        // append processed value to a buffer so we can write
                        // back new value list
                        compoundAttrValue.append(replaceData);
                        if (tokens.hasMoreTokens())
                            compoundAttrValue.append(" ");
                        attachmentCounter++;
                    }
                }
                // write processed values to attributes object
                attsCopy.setValue(i, compoundAttrValue.toString().trim());
            }
        }

        if (attributesChanged) {
            // return event with modified attribute values
            super.startElement(namespaceURI, localName, qName, attsCopy);
        } else
            // return original event
            super.startElement(namespaceURI, localName, qName, atts);
    }
}