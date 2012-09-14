package protocol.com.kenmccrary.jtella;

/**
 * A SubNetSearchSession represents a GNUtella search session on 
 * a network that support SUBNETQUERY messages. This behaves identically
 * to the standard JTella SearchSession, but produces SUBNETQUERY messages
 * rather than QUERY messages.
 * 
 * @author Alexander Craig
 * @author July 2010
 */
public class SubNetSearchSession extends SearchSession {

	/** The sub-network identifier for this session to search on */
	private String subNetworkIdentifier;
	
	/**
	 * Generates a new SubNetSearchSession.
	 * @param query	The query criteria
	 * @param queryType	The type of query (see SubNetSearchMessage)
	 * @param subNetId 	The sub-network identifier to search on
	 * @param maxResults	The maximum number of returned results
	 * @param minSpeed	The minimum accepted connection speed for responses
	 * @param connection	The connection to the Gnutella network
	 * @param router	The router for the Gnutella network
	 * @param receiver	The object to receive call backs when responses arrive.
	 */
	SubNetSearchSession(String query, int queryType, String subNetId, int maxResults,
			int minSpeed, GNUTellaConnection connection, Router router,
			MessageReceiver receiver) {
		super(query, queryType, maxResults, minSpeed, connection, router, receiver);
		this.subNetworkIdentifier = subNetId;
	}
	
	/**
	 * Fetches the sub-network identifier for this query.
	 * @return The sub-network identifier for this query
	 */
	public String getSubNetIdentifier() {
		return subNetworkIdentifier;
	}
	
	/**
	 * Generates the Gnutella message that should be sent to the network for this query.
	 * @return The Gnutella message that should be sent to the network for this query
	 */
	protected Message generateNetworkMessage() {
		return new SubNetSearchMessage(getQuery(), getQueryType(), getSubNetIdentifier());
	}

}
