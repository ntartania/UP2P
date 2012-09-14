package up2p.peer.jtella;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import up2p.search.SearchQuery;
import up2p.xml.TransformerHelper;

/**
 * Message sent from client to server to request a search.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class SearchRequestMessage extends GenericPeerMessage {
    private SearchQuery query;

    private String communityId;

    /** Tags for XML search requests */
    public static final String X_SEARCH_REQUEST = "searchRequest";

    public static final String X_SEARCH_QUERY = "searchQuery";

    public static final String X_SEARCH_ID = "id";

    public static final String X_COMMUNITY = "communityId";

    public static final String X_MAX_RESULTS = "maxResults";

    /** Error message returned when a parsing error occurs */
    public static final String ERROR_MSG = "Error parsing a Search Request";

    /**
     * Constructs an empty request.
     */
    public SearchRequestMessage() {
        super(SEARCH_REQUEST);
    }

    /**
     * Constructs a search request with the given community id, query and the
     * maximum number of results that should be returned.
     * 
     * @param uniqueId unique id for the search request
     * @param communityId id of the community to search in
     * @param searchQuery search query to execute
     */
    public SearchRequestMessage(String uniqueId, String communityId,
            SearchQuery searchQuery) {
        super(SEARCH_REQUEST);
        id = uniqueId;
        this.communityId = communityId;
        query = searchQuery;
    }

    /**
     * Calls the above constructor hen it has a string query as input
     * @param uniqueId unique id for the search request
     * @param communityId id of the community to search in
     * @param searchQuery XPath search query to execute (String)
     */
    public SearchRequestMessage(String uniqueId, String communityId,
            String searchQueryString) {
    this(uniqueId,communityId, new SearchQuery(searchQueryString));
    }
    
    

    /**
     * Parses a search request from the given XML. The node given must be the
     * root of the UP2P message and it's first child is the search request tag.
     * 
     * @param xmlNode the XML containing the search request
     * @return the search request
     * @throws MalformedPeerMessageException on a badly formed or invalid
     * message
     */
    public static SearchRequestMessage parse(Node xmlNode)
            throws MalformedPeerMessageException {
        SearchQuery q = new SearchQuery();
        String communityId = null;
        String searchId = null;

        // check first node name
        try {

            if (xmlNode.getNodeName().equals(GenericPeerMessage.X_UP2P_MESSAGE)) {
                Node currentNode = xmlNode.getFirstChild();
                // check node type to avoid mixed content
                while (currentNode != null
                        && currentNode.getNodeType() != Node.ELEMENT_NODE)
                    currentNode = currentNode.getNextSibling();
                Element current = (Element) currentNode;

                // community name
                communityId = current.getAttribute(X_COMMUNITY);

                // id
                searchId = current.getAttribute(X_SEARCH_ID);

                // maxResults
                if (current.hasAttribute(X_MAX_RESULTS))
                    q.setMaxResults(Integer.parseInt(current
                            .getAttribute(X_MAX_RESULTS)));

                // get search query node
                currentNode = currentNode.getFirstChild();
                while (currentNode != null
                        && currentNode.getNodeType() != Node.ELEMENT_NODE)
                    currentNode = currentNode.getNextSibling();
                current = (Element) currentNode;

                // get the text node that is the query
                currentNode = current.getFirstChild();
                if (currentNode.getNodeType() != Node.TEXT_NODE)
                    throw new MalformedPeerMessageException(ERROR_MSG);
                q.setQuery(currentNode.getTextContent());
            } else
                throw new MalformedPeerMessageException(ERROR_MSG);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MalformedPeerMessageException(ERROR_MSG);
        }
        return new SearchRequestMessage(searchId, communityId, q);
    }

    /*
     * @see up2p.peer.generic.message.SerializableMessage#serialize()
     */
    public Node serialize() {
        // create the XML DOM
        Document document = TransformerHelper.newDocument();

        // create root node
        Element root = document
                .createElement(GenericPeerMessage.X_UP2P_MESSAGE);
        document.appendChild(root);

        Element current = document.createElement(X_SEARCH_REQUEST);
        current.setAttribute(X_SEARCH_ID, id);
        current.setAttribute(X_MAX_RESULTS, String.valueOf(query
                .getMaxResults()));
        current.setAttribute(X_COMMUNITY, communityId);
        root.appendChild(current);

        // create searchQuery node
        root = current;
        current = document.createElement(X_SEARCH_QUERY);
        root.appendChild(current);

        // add XPath query
        Text queryStr = document.createTextNode(query.getQuery());
        current.appendChild(queryStr);

        // return the root element of the document
        return document.getDocumentElement();
    }

    /**
     * Returns the query contained in this search request.
     * 
     * @return the search query
     */
    public SearchQuery getQuery() {
        return query;
    }

    /**
     * Returns the community id.
     * 
     * @return community id
     */
    public String getCommunityId() {
        return communityId;
    }

    /**
     * Sets the community id.
     * 
     * @param communityId id of the community
     */
    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    /**
     * Sets the query.
     * 
     * @param query The query to set
     */
    public void setQuery(SearchQuery query) {
        this.query = query;
    }

}