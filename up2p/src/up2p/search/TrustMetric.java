package up2p.search;

import up2p.core.LocationEntry;

/** 
 * The TrustMetric class represents a single calculated trust metric
 * value. This is composed of the peerId (IP:port/urlPrefix) the metric is valid for,
 * the community the metric is valid for, and the name and value of the
 * metric.
 * 
 * @author Alexander Craig
 * @version June 11th, 2010
 */
public class TrustMetric {
	private String peerId;
	private String communityId;
	private String name;
	private String value;
	
	/**
	 * Generates a new TrustMetric
	 * @param peerId	The peerId of the metric (peer IP:port/urlPrefix)
	 * @param communityId	The community ID of the resource the metric applies to
	 * @param name	The name of the metric
	 * @param value	The value of the metric
	 */
	public TrustMetric(String peerId, String communityId, String name, String value) {
		this.peerId = peerId;
		this.communityId = communityId;
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Compares this TrustMetric to a SearchResponse, and adds the metric to the
	 * location entries of the response (if applicable).
	 * @param response	The SearchResponse to update
	 */
	public void setTrustResponseMetrics(SearchResponse response) {
		if(response.getCommunityId().equals(communityId)) {
			for(LocationEntry loc : response.getLocations()) {
				if(loc.getLocationString().equals(peerId)) {
					loc.addTrustMetric(name, this);
				}
			}
		}
	}
	
	/**
	 * @return	The name of this trust metric
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return	The value of this trust metric
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @return	The community Id of this trust metric
	 */
	public String getCommunityId() {
		return communityId;
	}
	
	/**
	 * @return	The peer IP/Port of this trust metric
	 */
	public String getPeerId() {
		return peerId;
	}
	
	/**
	 * @param other The other trust metric to compare against
	 * @return	true if the peer, community Id and name of the metric are the same
	 * 					(does not check the value of the metric)
	 */
	public boolean isSameMetricType(TrustMetric other) {
		return (getPeerId().equals(other.getPeerId())
				&& getCommunityId().equals(other.getCommunityId())
				&& getName().equals(other.getName()));
	}

}
