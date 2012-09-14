package up2p.tspace;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import lights.Field;
//import lights.Tuple;
import lights.TupleSpace;
//import lights.interfaces.IField;
import lights.extensions.XMLField;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;
import polyester.Worker;
import up2p.core.CommunityNotFoundException;
import up2p.core.LocationEntry;
//import up2p.core.WebAdapter;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.xml.TransformerHelper;

public abstract class UP2PWorker extends Worker {

	public static Logger LOG;
	public static final String LOGGER = "up2p.tuplespace";



	protected String name;

	//protected ITuple searchtemplate;

	/**lists to manage the tuples that have already been read 
	 *  they are indexed by template (i.e. for each template / 
	 *  query accepted by the worker, there is a list)
	 * 
	 */
	//protected Map<ITuple,List<ITuple>> doneList;

	//not sure if i'll need this
	protected WorkerInbox workerInbox;
	
	/** The system time at which the worker last fetched a tuple. Used to determine work / idle time. */
	private long lastWakeup;
	
	
	//TODO: remove these from UP2PWorker: only the network worker should have a set of search listeners. 
	//All others use the SearchResponseTree
	
	private Map<ITuple,TSScanner> templateScanners;

	public UP2PWorker(ITupleSpace ts){
		super(ts);
		workerInbox = new WorkerInbox();
		LOG = Logger.getLogger(LOGGER);
	
		templateScanners = new HashMap<ITuple, TSScanner>();
		//doneList = new HashMap<ITuple,List<ITuple>>();
		//searchtemplate = TupleFactory.createSearchTemplate();
		lastWakeup = System.currentTimeMillis();
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.SHUTDOWN, 0));
	}

	protected abstract List<ITuple> answerQuery(ITuple template, ITuple query);


	/**
	 * add a query template, and a thread to scan for that template
	 * @param tpl
	 */
	protected void addQueryTemplate(ITuple tpl){
		TSScanner scanner = new TSScanner(tpl, workerInbox,space,name);
		scanner.setDaemon(true);
		templateScanners.put(tpl,scanner);
		scanner.start();
	}
	/* need to set priorities if we want the blinker (main thread of the worker) to have higher priority
	public void start(){
		super.start();
		
	}*/
	public void stop(){
		LOG.debug(name + " terminating threads.");
		super.stop();
		
		// Note: Removed thread termination as all scanner threads
		// are now generated as daemon threads.
	}


	/*Alan july 2011 : removing this method: there should be other ways of getting around the problem of processing one's own queries
	 * 
	 *  adds a tuple to the list of 'done' queries. 
	 *  The idea is that this should be done to avoid the worker 
	 *  outputting a query and attempting to process it himself 
	 * 
	 * @param query the query to be added (then ignored)
	 * /
	protected void ignoreSearchQuery(ITuple query){

		ITuple key = this.searchtemplate; 
		//List<ITuple> list=null;
		if(key!=null && templateScanners.containsKey(key)) {
			TSScanner searchscanner = templateScanners.get(key);
			searchscanner.ignore(query);
			LOG.debug(name+ " added query to be ignored : "+ query);
		}
		
		else 
		{
			LOG.debug(name+ " doesn't have SEARCH among his templates ");
		}
	}*/

	/**
	 * Answer queries. Go through the supported templates, check for a matching query, and answer it
	 *
	 */
	protected void answerQueries() {
		//gets next query pair [template, query] from inbox : the template is necessary for the dynamic agents
		TuplePair querypair = workerInbox.getFirst(); 
		
		LOG.debug(name+" processing query:" + querypair.query );

		String verb = ((Field) querypair.query.get(0)).toString();
		
		if(verb.equalsIgnoreCase(TupleFactory.SHUTDOWN)) {
			LOG.debug(name + " received SHUTDOWN tuple, performing cleanup.");
			shutdownCleanup();
			this.stop();
		} else {
			List<ITuple> ansTuple = answerQuery(querypair.template, querypair.query);
		
			if(ansTuple!=null && //don't think it will be null... but it could be empty.
					ansTuple.size()>0){
		
				//for (ITuple itu: ansTuple){ //output all answers
				try {
					space.outg(ansTuple.toArray(new ITuple[ansTuple.size()]));
				} catch (TupleSpaceException e) {
					LOG.error("TupleSpace Error:"+e);
				}
				//}
				LOG.debug(name+" worker posting "+ansTuple.size()+" answers for query "+querypair.query.toString());
			} else {
				LOG.debug(name+": No immediate answer for query "+querypair.query.toString());
			}
		}
	}


	
	/**
	 *  Method to do synchronous queries on the tuple space -- output query, wait for answer
	 *
	 * @param query a tuple defining the query
	 * @param answerTemplate a tuple template defining the answer
	 * @return the tuple found to match the answer tuple
	 * @throws TupleSpaceException for whatever reason TupleSpace.in(ITuple) may throw an exception
	 */
	/*//protected ITuple synchronousQuery(ITuple query, ITuple answerTemplate) throws TupleSpaceException{
		space.out(query);
		//LOG.debug(name+" waiting for:"+answerTemplate);
		ITuple answer = space.rd(answerTemplate); //listen for answer using template
		//LOG.debug(name+" no longer waiting for:"+answerTemplate + ", got "+ answer);
		return answer;

	}*/

	/**
	 *  Method to do synchronous queries on the tuple space -- output query, wait for answer
	 *  this one should be used to retrieve multiple answers
	 * @param query a tuple defining the query
	 * @param answerTemplate a tuple template defining the answer
	 * @return the tuple found to match the answer tuple
	 * @throws TupleSpaceException for whatever reason TupleSpace.in(ITuple) may throw an exception
	 */
	protected List<ITuple> synchronousMultiQuery(ITuple query, ITuple answerTemplate) throws TupleSpaceException{
		space.out(query);
		ITuple [] answer = space.rdg(answerTemplate); //listen for answer using template
		if (answer==null){
			space.rd(answerTemplate); //block until there is *one* answer
			answer = space.rdg(answerTemplate); //get all answers
		}
		if (answer==null){
			LOG.error("syncMultiQuery got a null answer!");
			return new ArrayList<ITuple>();
		}
		return Arrays.asList(answer);
		//return answer;

	}

	/**only for testing purpose
	 * 
	 * @return the tuple space object!!
	 */
	public ITupleSpace getTS(){
		return space;
	}

	/**
	 *  Method to do asynchronous queries on the tuple space -- output query, register listener
	 *
	 * @param query a tuple defining the query
	 * @param answerTemplate a tuple template defining the answer
	 * @return the tuple found to match the answer tuple
	 * @throws TupleSpaceException for whatever reason TupleSpace.in(ITuple) may throw an exception
	 */
	protected void asynchronousQuery(ITuple query, ITuple answerTemplate, SearchResponseListener listener) throws TupleSpaceException{
		space.out(query);
		//addListener(listener,answerTemplate); // add a listener for this query
	}

	/**
	 *  outputs a tuple [ "Error", exception] so the exception can be handled by user interface
	 * @param e the exception
	 */
	protected void notifyErrorToUser (Exception e){
		if (e!=null)
			try {
				e.printStackTrace();
				String info = e.toString();
				space.out(TupleFactory.createTuple(TupleFactory.NOTIFY_ERROR, info));
			} catch (TupleSpaceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	
	/**
	 * Outputs a tuple [ "Error", errorMessage ] so the exception can be handled by user interface
	 * @param errorMessage The string error message to display to the user.
	 */
	protected void notifyErrorToUser(String errorMessage){
		if (errorMessage!=null)
			try {
				space.out(TupleFactory.createTuple(TupleFactory.NOTIFY_ERROR, errorMessage));
			} catch (TupleSpaceException e1) {
				e1.printStackTrace();
			}
	}

	public void setName(String name){
		this.name = name;
	}
	
	@Override
	public void work() {
		// answer all queries from supported templates
		answerQueries();
		/*synchronized(searchListeners) {
			for (QueryListener ql : searchListeners){

				ITuple[] res=null;
				try { //scan for results to the current queries
					res = space.ing(ql.template);
				} catch (TupleSpaceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (res != null){
					LOG.info(name +": Got "+ res.length+ "answers matching "+ql.template );
					ql.sendResults(res);
				}
			}
		}*/
	}

	/**
	 *  Sends out a search to the tuple space
	 *  The method is asynchronous. (doesn't wait for an answer)
	 *  
	 * @param resourceId
	 * @param communityId
	 * @param xPath
	 * @param extent	Determines what the scope of the search should be.
	 * 					This value should be one of HttpParams.UP2P_SEARCH_ALL,
	 * 					UP2P_SEARCH_NETWORK, or UP2P_SEARCH_LOCAL
	 * @return
	 */
	public void searchWithDOM(String communityId, String xPath, SearchResponseListener listener,
			String qid, int extent) {
		//define a tuple for this query.
		LOG.debug(name + " About to put search tuple: arguments "+communityId+", "+xPath+", "+qid + ", " + extent);

		ITuple t3query = TupleFactory.createSearchTuple(communityId, xPath, qid, extent);
		//ignoreSearchQuery(t3query); //add the query to the list to be ignored so that this worker doesn't try to process it
		//ITuple t7ans = TupleFactory.createSearchReplyTemplateWithDOM(qid);

		try {
			space.out(t3query);
			//asynchronousQuery(t3query, t7ans, listener);
		} catch (TupleSpaceException e) {
			LOG.error(name+" :searchWithDOM: "+ e.toString());
		}
		LOG.debug(name+" sent.");

	}
	
	public List<String> synchronousLocalSearch(String communityId, String xPath)
			throws CommunityNotFoundException {
				LOG.debug(name + ": About to output a synchronous local SEARCH in community "+ communityId);
			
				String[] fields = new String[] {communityId, xPath};
			
				ITuple t4query = TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCH, fields); 
			
				ITuple t2ans =TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCHRESPONSE, fields);
				
				//add a random identifier
				Random rand = new Random();
				String qid = System.currentTimeMillis() + Integer.toString(rand.nextInt(100000));
				//add the field to the query
				t4query.add(new Field().setValue(qid));
				
				t2ans.add(new Field().setType(String.class)); // template is identical + a template field
			
				//query tuple space using synchronous method with multiple answers
			
				List<String> toReturn = new ArrayList<String>();
				try {
					List<ITuple> answers = synchronousMultiQuery(t4query, t2ans);
					
					// KLUDGE: Keep a list of the returned responses and ensure that any duplicates are
					// pruned. This is necessary as simultaneous queries can cause duplicate results.
					List<String> seenAnswers = new ArrayList<String>();
			
					for (ITuple ans : answers){
						IField answerField = ans.get(3);// number of field where the response is
						String stringanswer;
						if (answerField instanceof Field){ //should always be the case but let's be defensive
							stringanswer = ((Field)answerField).getValue().toString();
							//LOG.debug("Got answer for LookupXPath: "+ answer);
							if(!seenAnswers.contains(stringanswer)) {
								toReturn.add(stringanswer);
								seenAnswers.add(stringanswer);
							}
			
							if (stringanswer.equals(TupleFactory.COMMUNITY_NOT_FOUND))
								throw new CommunityNotFoundException(); 					 
			
						}
					}
			
				} catch (TupleSpaceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return toReturn;  	
				
			}

	/**
	 * Performs a synchronous local browse of a community. This returns all resource id's
	 * of resources being hosted in the community, and does so through a single query
	 * to the database.
	 * 
	 * @param communityId	The id of the community to browse
	 * @return	A list of the resource id's within the passed community
	 */
	public List<String> synchronousLocalBrowse(String communityId) throws
		CommunityNotFoundException {
		LOG.debug(name + ": About to output a synchronous local BROWSE in community "+ communityId);
	
		// Generate a random query Id
		Random rand = new Random();
		String qid = System.currentTimeMillis() + Integer.toString(rand.nextInt(100000));
		
		String[] fields = new String[] {communityId, qid};
	
		ITuple t2query = TupleFactory.createTuple(TupleFactory.BROWSELOCALCOMMUNITY, fields); 
	
		ITuple t3ans =TupleFactory.createTuple(TupleFactory.BROWSELOCALCOMMUNITYANSWER, fields);
		t3ans.add(new Field().setType(String.class)); // Template is identical + a template field for response list
	
		// Query tuple space using synchronous method with single answer
	
		List<String> toReturn = new ArrayList<String>();
		
		try {
			space.out(t2query);
			ITuple browseResult = space.rd(t3ans);
			String resultString = ((Field)browseResult.get(3)).getValue().toString();
			if(resultString.equals(TupleFactory.COMMUNITY_NOT_FOUND)) {
				LOG.error("Community Not Found !");
				throw new CommunityNotFoundException(name + ": UP2PWorker.synchronousLocalBrowse - " 
						+ communityId);
			}
			toReturn = Arrays.asList(resultString.split("/"));
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	/**
	 * This method is called when a shutdown tuple is received just before all threads
	 * are terminated. This should be implemented by subclasses to perform any
	 * shutdown procedures that need to be performed before thread termination.
	 */
	public void shutdownCleanup() {
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	//private classes
	//////////////////////////////////
	
	// update 2011 -07 - 15 : all moved to public classes for reusability !
	/////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	

//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 *  a query listener that can be added in order to collect asynchronous search responses and 
	 *  do callbacks to the objects who placed the searches (or their specified listeners)
	 *
	 *Alan 2011-07-12: Obsolete.
	 *
	 *private class QueryListener ...{}
	 *
	 */
	

}