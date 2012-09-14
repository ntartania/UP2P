package up2p.search;

/**
 * Receives responses from search dispatched to the peer network.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface SearchResponseListener {

    /**
     * Receives zero or more search responses.
     * 
     * @param responses responses to a search query
     */
    public void receiveSearchResponse(SearchResponse[] responses);
    
    //public void receiveSearchResponse(SearchResponse SingleResponse);
}