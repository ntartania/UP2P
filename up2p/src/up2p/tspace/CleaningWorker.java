package up2p.tspace;

//import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
import java.util.Set;

import org.apache.log4j.Logger;

import lights.Field;
import lights.Tuple;
//import lights.TupleSpace;
import lights.extensions.XMLField;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
//import lights.interfaces.TupleSpaceException;
import polyester.Worker;

public class CleaningWorker extends Worker {

	public static Logger LOG;
	public static final String LOGGER = "up2p.tuplespace";
	
	private static final int DEF_TUPLE_LIFETIME = 4000;

	/**
	 * how long a tuple stays in the TS
	 */
	private int tupletime;
	
	/**
	 * lists the tuples to be removed with the time they were first seen 
	 */
	private Map<ITuple,Long> toRemove;
	
	//private List<ITuple> latestEntry;
	
	
	private static ITuple universalTemplate = new Tuple();
	
	public CleaningWorker(ITupleSpace ts){
		this(ts, DEF_TUPLE_LIFETIME);
	}
	
	public CleaningWorker(ITupleSpace ts, int delay){
		super(ts);
		LOG = Logger.getLogger(LOGGER);
		tupletime = delay;
		toRemove = new HashMap<ITuple,Long>();
	}
	
	
	@Override
	public void work() {
		//input all the tuples matching the current template
		boolean terminateFlag = false;
		ITuple[] listuple;
		Long firstSeen;
		try {
			listuple = space.rdg(universalTemplate);
			if (listuple== null){
				LOG.debug("CLEANING AGENT:  TupleSpace empty.");
				Thread.sleep(tupletime + 1);
				return;
			}
			
			Long currentTime = new Long(System.currentTimeMillis());
			
			//List<ITuple> newlist = new ArrayList<ITuple>();
			if (listuple!= null){ 
				LOG.debug("CLEANING AGENT:  *****list********"+ listuple.length);
				Set<ITuple> tupleset = new HashSet<ITuple>();
				for (ITuple it : listuple)
					tupleset.add(it);
				LOG.debug("CLEANING AGENT:  ******set*******"+ tupleset.size());
				//// Now we've only got one copy of each tuple to remove !
				
				for (ITuple it: tupleset){
					// Check to see if Shutdown tuple was received, and schedule termination if so
					if(((Field)it.get(0)).toString().equalsIgnoreCase(TupleFactory.SHUTDOWN)) {
						terminateFlag = true;
						LOG.debug("CLEANING AGENT: Got shutdown tuple, terminating after this cleanup cycle.");
					}
					
					if(toRemove.containsKey(it)){
						firstSeen = toRemove.get(it);
						if (currentTime - firstSeen > tupletime){//if the tuple has been there more than the allowed time, we remove it.
							LOG.debug("CleaningAgent about to remove (Observed for " + (currentTime - firstSeen) + " ms): " + it);
							/*
							 * this short section is for testing purposes:
							 */
							for (int i= 0; i<it.length();i++)
								if(it.get(i) instanceof XMLField){
									LOG.debug("got an XMLF");
									break;
								}
		
								
							if(space.ing(it)==null)
								LOG.debug("CleaningAgent could not remove: " + it); 
							else
								LOG.debug("CleaningAgent removes: " + it);
							if(toRemove.remove(it)==null)
								LOG.debug("Cleaning Agent could not remove "+ it+ " from its map"); // remove the tuple from the list
						}
					} else //the tuple has appeared recently!
					{
						toRemove.put(it, new Long(currentTime)); //insert the tuple with the time object (cloned just in case)
						LOG.debug("CleaningWorker found new tuple: " + it + "; total number of tuples remembered: "
								+ toRemove.size());
					}

				}
				LOG.debug("CLEANING AGENT:  DONE REMOVAL--------");
				
				if(terminateFlag) {
					LOG.debug("CLEANING AGENT: Terminating thread.");
					this.stop();
				}
			}
			
			//add some sleep because the super class worker is just too fast
			Thread.sleep(tupletime + 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("CleaningWorker:"+e);
		}
		

		
	}

	

}
