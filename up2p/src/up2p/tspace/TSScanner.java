package up2p.tspace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;
//import up2p.tspace.UP2PWorker.Inbox;
//import up2p.tspace.UP2PWorker.TuplePair;

/**
 *  this class is a thread that scans the tuplespace with a specific template, 
 *  and drops the tuples to be processed in an "Inbox" object, which is a message queue 
 * @author alan
 *
 */
public class TSScanner extends Thread {

	
	public static Logger LOG;
	public static final String LOGGER = "up2p.tuplespace";
	
	//template that this particular thread will be looking for
	private ITuple template ;
	//list of "already seen" tuples matching the template, not to be re-processed
	private Set<ITuple> doneList;
	//flag to know that the thread should stop
	private boolean shutdownflag;
	//where to drop newly read tuples
	protected WorkerInbox inbox; 
	// Flag that is set when thread termination completes
	private boolean hasTerminated;
	
	private String name; // a name for logging
	
	private ITupleSpace space;
	
	/**
	 * constructor
	 * @param tuple template that this scanner looks for
	 * @param inbox inbox where the new tuples are stored
	 * @param space  the tuple space we're interacting with
	 * @param name the name of the worker that this scanner scans for, just for logging purpose
	 */
	public TSScanner(ITuple tuple, WorkerInbox inbox, ITupleSpace space, String name){
		super("TSSCanner/"+tuple.get(0));
		LOG = Logger.getLogger(LOGGER);
		this.space = space;
		this.name= name;
		template = tuple;
		doneList= new HashSet<ITuple>();
		shutdownflag = false;
		hasTerminated = false;
		this.inbox=inbox;
	}
	
	/**
	 * @return	True if the thread has finished terminating (run method returned)
	 */
	public boolean hasTerminated() {
		return hasTerminated;
	}
	
	public void ignore(ITuple query) {
		doneList.add(query);
	}

	/**
	 * Collects tuples matching the input template.
	 * If these tuples have been recently read, then we drop them
	 * field 0 is a string "Search", field 1 is a formal field of class "SearchString"
	 *
	 * @return an array of tuples
	 */
	protected List<ITuple> scanForQueries(boolean checkDuplicates) {

		//template for the queries to be answered

		//retrieve results, see if they match
		ITuple[] allqueries = null;
		List<ITuple> toReturn = new ArrayList<ITuple>();
		//LOG.debug(name+ " worker looking for work...");
		
		try {
			//LOG.debug(name+"/"+template.get(0)+ " looking for work...");
			allqueries = space.rdg(template); // read query
			//TODO: how to handle the problem of seeing several times the same query

			if (allqueries==null){ //if there's no matching tuples, then we want to wait rather than looping
				//we can also empty the donelist:
				doneList.clear();
				//LOG.debug(name+"/"+template.get(0)+ "going into WAIT mode...");
				space.rd(template); //wait until one matching tuple shows up (do not save read tuple since we'll get it right after)
				LOG.debug(name+": scanner "+ template+ " reading something!!");						
				allqueries= space.rdg(template); //re-scan in order to get all the matching tuples (in case several were output at the same time)
			}
		} catch (TupleSpaceException e) {
			return toReturn;
		}
		

		Set<ITuple> seen = new HashSet<ITuple>();

		if (allqueries !=null) { //if we didn't find any queries we may as well leave!

			Set<ITuple> readqueries = doneList;

			for (ITuple q : allqueries){
				ITuple qt= (ITuple)q;

				if (readqueries!= null && readqueries.contains(qt)) //check if we've had this query recently
				{seen.add(qt); //if we've seen it then we will put it in the cache
				//LOG.debug(name+" ignoring tuple:"+qt);

				}
				else{
					toReturn.add(qt); // if not we will process it

				}

			}

			/*we now only store in the cache the queries that we found this time around.
	    			Queries that we've seen three rounds before then disappeared should not be in the cache
			 */
			seen.addAll(toReturn); //get the full list of queries we just picked up (allqueries except in List format rather than [])


		}

		//this must be done whether or not we've found tuples! if we haven't found any then there are not more "seen" tuples, nothing to ignore next time there actually are any tuples...
		doneList= seen; // make that the new cache.

		//TODO: add a check mechanism to ensure that the doneList does not explode in size
		return toReturn; //we will now return the tuples that are new this time around for processing
	}
	
	/**
	 * run: scan the tuplespace for tuples matching the template, filter out those that have been seen recently,
	 * and place the remaining ones to the "inbox" of this worker. Loop until the thread is told to shutdown using the mystop() method
	 * @throws InterruptedException 
	 */
	
	public void run() {
		while(!shutdownflag){
			List<ITuple> newtuples = scanForQueries(true);
			List<TuplePair> pairs = new ArrayList<TuplePair>();
			for (ITuple tup : newtuples)
				pairs.add(new TuplePair(template, tup));
			if(!newtuples.isEmpty())
				inbox.put(pairs);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		hasTerminated = true;
	}
	
	/**
	 * stop this thread (could not call it stop because that's a deprecated & final method
	 * the thread will not stop immediately but finish one iteration of scanning for queries, etc..
	 */
	public void mystop(){ 
		shutdownflag=true;
	}
}