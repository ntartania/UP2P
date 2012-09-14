/**
 * 
 */
package up2p.tspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import up2p.core.CommunityNotFoundException;

import lights.Field;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

/**
 * The TrustWorker agent is responsible for processing NETRESOURCELIST
 * tuples from the tuple space and using the contained information to calculate
 * trust metrics. These trust metrics are placed back in to the tuple space as
 * TRUSTMETRIC tuples, which can then be matched and used by other agents
 * in the system.
 * 
 * @author Alexander Craig
 * @version June 11th, 2010
 *
 */
public class TrustWorker extends UP2PWorker {
	/** 
	 * A map of lists of cached local resources keyed by
	 * community ID. The cache should be cleared whenever a 
	 * PUBLISH or REMOVE occurs on the community.
	 */
	Map<String, List<String>> cachedLocalResLists;

	/**
	 * Generates a new instance of TrustWorker.
	 * @param ts	The tuplespace for this agent to interact with
	 */
	public TrustWorker(ITupleSpace ts) {
		super(ts);
		name = "TRSTWK"; // Trust Worker
		
		cachedLocalResLists = new HashMap<String, List<String>>();
		
		// Three fields for NETRESOURCELIST: <peerIpPort, communityId, resourceList>
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.NETRESOURCELIST, 3));
		
		// Two fields for REMOVE: <comId, resId>
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.REMOVE, 2));

		// Three fields for PUBLISH: <comId, resId, xmlNode>
		addQueryTemplate(TupleFactory.createPublishTemplate());
	}

	/**
	 * @see up2p.tspace.UP2PWorker#answerQuery(lights.interfaces.ITuple, lights.interfaces.ITuple)
	 */
	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple query) {
		List<ITuple> ansTuple= new ArrayList<ITuple>();
		
		// Get the verb for the matched tuple
		String verb = ((Field) query.get(0)).toString();
		
		if (verb.equals(TupleFactory.NETRESOURCELIST)) {
			// A list of network resources from another peer 
			// Calculate the Jaccard distance
			
			String peerIpPort = ((Field) query.get(1)).toString();
			String communityId = ((Field) query.get(2)).toString();
			String netResListString = ((Field) query.get(3)).toString();
			
			// Add all the network resource id's to a List so they can be compared with
			// the local list
			List<String> netResList = Arrays.asList(netResListString.split("/"));;
			
			List<String> localResList = null;
			
			// Check if the local resource list is cached.
			synchronized(cachedLocalResLists) {
				localResList = cachedLocalResLists.get(communityId);
			}
			
			// If the resource list was not cached, perform a synchronous browse
			// on the database.
			if(localResList == null) {
				try {
					LOG.debug(name + ": Querying database for contents of community: " + communityId);
					localResList = synchronousLocalBrowse(communityId);
					synchronized(cachedLocalResLists) {
						cachedLocalResLists.put(communityId, localResList);;
					}
				} catch (CommunityNotFoundException e) {
					LOG.error(name + ": Received metric for invalid community, discarding.");
					return null;
				}  
			}
			
			// Calculate the Jaccard distance (intersection of shared resources / union of shared resources)
			int intersectionSize = 0;
			for(String localId : localResList) {
				if(netResList.contains(localId)) {
					intersectionSize++;
					// System.out.println("=== INTERSECTION: " + localId + " ==="); // DEBUG PRINTING
				}
			}
			int unionSize = localResList.size() + netResList.size() - intersectionSize;
			
			double jaccard;
			if(unionSize == 0 || intersectionSize == 0) {
				jaccard = 0; 
			} else {
				jaccard = (double)intersectionSize / (double)unionSize;
			}
			String jaccardString = Double.toString(jaccard * 100);
			
			// Trim to 3 decimal places
			if (jaccardString.length() > jaccardString.indexOf(".") + 3) {
				jaccardString = jaccardString.substring(0, jaccardString.indexOf(".") + 3);
			}
			
			// jaccardString += "%"; // Leave as a numeric value for now, easier to handle on client side
			
			LOG.debug(name + ": Jaccard Distance: " + peerIpPort + " - " + communityId
					+ "\n" + intersectionSize + " / " + unionSize + " = " + jaccardString);
			
			// Add the resulting metric tuple to the tuplespace
			ITuple jaccardTuple = TupleFactory.createTuple(TupleFactory.TRUSTMETRIC,
					new String[] {peerIpPort, communityId, "Jaccard Distance", jaccardString});
			ansTuple.add(jaccardTuple);
			
		} else if (verb.equals(TupleFactory.REMOVE)
				|| verb.equals(TupleFactory.PUBLISH))
		{
			String comId = ((Field) query.get(1)).toString();
			LOG.debug(name + ": Invalidating cache for community: " + comId);
			synchronized(cachedLocalResLists) {
				cachedLocalResLists.remove(comId);
			}
		}

		return ansTuple;
	}
}
