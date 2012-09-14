package up2p.xml.filter;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import up2p.core.NetworkAdapterInfo;

/**
 * Catches information from parsing the XML file defining a
 * <code>NetworkAdapter</code>. Information on the adapter and its provider
 * class are stored as a <code>NetworkAdapterInfo</code> object in the
 * properties table of the <code>ResourceFilterChain</code>.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class NetworkAdapterFilter extends BaseResourceFilter {
    public static final String NETWORK_ADAPTER_INFO = "up2p.xml.filter.NetworkAdapterFilter";

    private static final String ELEMENT_ROOT = "NetworkAdapter";

    private static final String ELEMENT_PROVIDER = "provider";

    private static final String ELEMENT_DESC = "description";

    private static final String ELEMENT_REQ = "requirements";

    private static final String ELEMENT_PARAM = "param";

    /* Tracks what element is being processed. */
    private Stack currentOpenElement;

    /*
     * The adapter info that gets stored in the filter chain as a property.
     */
    private NetworkAdapterInfo info;

    /**
     * Creates the filter.
     */
    public NetworkAdapterFilter() {
        currentOpenElement = new Stack();
    }

    /*
     * @see org.xml.sax.ContentHandler#startElement(String, String, String,
     * Attributes)
     */
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes attributes) throws SAXException {
        if (localName.equals(ELEMENT_ROOT)) {
            // create the info
            info = new NetworkAdapterInfo();
            // store info in the chain
            chain.setProperty(NETWORK_ADAPTER_INFO, info);
            // set the title
            info.setTitle(attributes.getValue("", "title"));
        } else if (localName.equals(ELEMENT_PROVIDER)) {
            processProvider(attributes);
        } else if (localName.equals(ELEMENT_PARAM)
                && currentOpenElement.peek().equals(ELEMENT_ROOT)) {
            // Param within the root element.
            // Add the parameter to the network adapter info
            String name = attributes.getValue("", "name");
            String value = attributes.getValue("", "value");
            info.setParameter(name, value);
            String desc = attributes.getValue("", "description");
            if (desc != null)
                info.setParameterDescription(name, desc);
        } else if (localName.equals(ELEMENT_REQ)
                && currentOpenElement.peek().equals(ELEMENT_PROVIDER)) {
            // found requirements nested in provider
            if (attributes.getValue("", "up2pVersion") != null)
                info.setUp2pVersion(attributes.getValue("", "up2pVersion"));
        }

        // push the current element name onto the stack
        currentOpenElement.push(localName);

        super.startElement(namespaceURI, localName, qName, attributes);
    }

    /**
     * Processes attributes of the provider element (but not its children).
     * 
     * @param attributes the attribute of the provider element
     */
    private void processProvider(Attributes attributes) {
        // The schema looks like this:
        //	<xsd:attribute name="providerJarURL" use="required"
        // type="xsd:string"/>
        //	<xsd:attribute name="providerClass" use="required"
        // type="xsd:string"/>
        //	<xsd:attribute name="providerAuthor" use="optional"
        // type="xsd:string"/>
        //	<xsd:attribute name="providerEmail" use="optional"
        // type="xsd:string"/>
        //	<xsd:attribute name="providerURL" use="optional" type="xsd:anyURI"/>
        //	<xsd:attribute name="providerHelpURL" use="optional"
        // type="xsd:anyURI"/>
        //	<xsd:attribute name="providerVersion" use="optional"
        // type="xsd:string"/>
        info.setProviderJarURL(attributes.getValue("", "providerJarURL"));

        info.setProviderClass(attributes.getValue("", "providerClass"));

        // optional attributes
        String providerAuthor = attributes.getValue("", "providerAuthor");
        if (providerAuthor != null)
            info.setProviderAuthor(providerAuthor);

        String providerEmail = attributes.getValue("", "providerEmail");
        if (providerEmail != null)
            info.setProviderEmail(providerEmail);

        String providerURL = attributes.getValue("", "providerURL");
        if (providerURL != null)
            info.setProviderURL(providerURL);

        String providerHelpURL = attributes.getValue("", "providerHelpURL");
        if (providerHelpURL != null)
            info.setProviderHelpURL(providerHelpURL);

        String providerVersion = attributes.getValue("", "providerVersion");
        if (providerVersion != null)
            info.setProviderVersion(providerVersion);
    }

    /*
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        currentOpenElement.pop();
        super.endElement(namespaceURI, localName, qName);
    }

    /**
     * Both 'description' and 'requirements' have character content that needs
     * capturing.
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (currentOpenElement.peek().equals(ELEMENT_DESC)) {
            // we are in the description element
            String desc = info.getDescription();
            if (desc == null)
                info.setDescription(new String(ch, start, length));
            else
                info.setDescription(desc + new String(ch, start, length));
        } else if (currentOpenElement.peek().equals(ELEMENT_REQ)) {
            // we are in the requirements element
            String req = info.getRequirements();
            if (req == null)
                info.setRequirements(new String(ch, start, length));
            else
                info.setRequirements(req + new String(ch, start, length));
        }
        super.characters(ch, start, length);
    }
}