package up2p.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lights.TupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import up2p.core.DefaultWebAdapter;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.tspace.BasicWorker;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PAgentFactory;
import up2p.util.PairList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The Graph Query servlet handles complex graph
 * queries, by generating a series of related
 * sub-query tuples.
 * 
 * @author Alexander Craig
 * @version May 11th, 2010
 */
public class GraphQueryServlet extends AbstractWebAdapterServlet {
	
	/** Stores the sets of parameters for each individual sub-query. */
	private List<QueryParamSet> queryData;
	
	/** Stores the resource id of the document to begin the complex query from. */
	private String resourceId;
	
	/** Constructs the servlet. */
    public GraphQueryServlet() {
        super();
        queryData = new ArrayList<QueryParamSet>();
    }

    /*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doQuery(req, resp);
    }

    /*
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doQuery(req, resp);
    }
    
    /**
     * Processes a complex query by producing a series
     * of connected tuples.
     * 
     * @param request inbound request made to the servlet
     * @param response outbound response written to the client
     */
    protected void doQuery(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    	
		// Log the request
		LOG.info("GraphQueryServlet: Graph query received");
		
		// Ensure any data from the last request has been cleared
		queryData.clear();

		// Prepare the parameters to be iteratively read
		PairList paramMap = null;
		paramMap = copyParameters(request);
		 
		Iterator<String> typeIter = paramMap.getValues("up2p:queryType"); // TODO: Add these to HttpParams
		Iterator<String> commIdIter = paramMap.getValues("up2p:queryCommId");
		Iterator<String> linkIter = paramMap.getValues("up2p:queryXPath");
 
    
		// Parse the parameters into a list of QueryParamSets which so they can be used to generate
		// the tuple space agents.
		while (typeIter.hasNext()) {
			if(!commIdIter.hasNext() || !linkIter.hasNext()) {
				throw new ServletException("Graph SubQuery is missing a parameter " +
						"(must have triplets of query type, community id and XPath).");
			}
			
			QueryParamSet query = new QueryParamSet(typeIter.next(), commIdIter.next(), linkIter.next());
			LOG.info("GraphQueryServlet: Built param set: " + query.getQueryType() + "/" + query.getCommunityId()
					+ "/" + query.getLinkXPath());
			queryData.add(query);
		}
		
		// Get the resourceId of the query end point
		resourceId = paramMap.getValue("up2p:queryResId"); // TODO: Add to HttpParams
		if (resourceId == null) {
			throw new ServletException("No document was specified to begin complex search.");
		}
		LOG.info("GraphQueryServlet: End point resource Id: " + resourceId);
		
		// Generate a random number to start incrementing query id's from
		int queryId =new Random().nextInt(1000000);
		int firstQueryId = queryId;
		int finalQueryId = queryId + queryData.size();
        ArrayList<BasicWorker> tspaceAgents = new ArrayList<BasicWorker>();
        
        // Determine if a recursive query has been specified
		boolean recursiveSearch;
		String recursiveSearchParam = paramMap.getValue(HttpParams.UP2P_RECURSIVE);
		if (recursiveSearchParam == null) {
			recursiveSearch = false;
		} else {
			recursiveSearch = true;
			LOG.info("GraphQueryServlet: Search is flagged as recursive, using QID: " + queryId);
		}
        
		if (recursiveSearch) {
	        
	        // Generate the recursive search agent
	        tspaceAgents.add(queryData.get(0).generateRecursiveAgent(queryId));
	        
		} else {
			
			// Determine if the search results should be transitive (i.e. results from each stage of
			// the complex query should be part of the final result)
			boolean transSearch;
			String transSearchParam = paramMap.getValue(HttpParams.UP2P_TRANSITIVE);
			if (transSearchParam == null) {
				transSearch = false;
			} else {
				transSearch = true;
				LOG.info("GraphQueryServlet: Search is flagged as transitive, output is set to: " + finalQueryId);
			}
	        
	        // Generate all the required tuple space agents
	        for(QueryParamSet subQuery : queryData) {
	        	if(transSearch) {
	        		tspaceAgents.add(subQuery.generateTransitiveAgent(queryId, queryId + 1, finalQueryId));
	        	} else {
	        		tspaceAgents.add(subQuery.generateBasicAgent(queryId));
	        	}
	        	queryId++;
	        }
			
		}
        
		// Add the finalizer agent (necessary when the last step of the graph navigation is an "Object" link)
        if ((!recursiveSearch && queryData.get(queryData.size() - 1).getQueryType().equals("Object")) ||
        		(recursiveSearch && queryData.get(0).getQueryType().equals("Object"))) {
            LOG.info("GraphQueryServlet: Adding Finalizer agent\n\tId: " + firstQueryId);
            tspaceAgents.add(UP2PAgentFactory.createFinalizerAgent(adapter.getDefWebAdapter().getTS(), Integer.toString(firstQueryId)));
        }
        
        // Set the adapter to show the results of the final agent
		LOG.info("GraphQueryServlet: Setting search results to show query id: " + queryId);
		adapter.setCurrentSearch(Integer.toString(queryId), true);
        
        // Start the agents
        LOG.info("GraphQueryServlet: Starting query agents");
        for(BasicWorker agent : tspaceAgents) {
            agent.start();
        }
		
        // Add the trigger tuple to the tuple space
        LOG.info("GraphQueryServlet: Generating trigger tuple, id: " + firstQueryId);
        ITuple trigger = TupleFactory.createTuple(new String[]{TupleFactory.COMPLEXQUERY, resourceId, Integer.toString(firstQueryId)});
        try {
			adapter.getDefWebAdapter().getTS().out(trigger);
			LOG.info("GraphQueryServlet: Added trigger tuple to tuple space.");
		} catch (TupleSpaceException e1) {
			LOG.error("GraphQueryServlet: Trigger tuple failed to start.");
			e1.printStackTrace();
		}
		
		// Redirect the user to the results
        String redirect = response.encodeURL("/displayResults.jsp?up2p:asynch=true");
        LOG.info("GraphQueryServlet Redirecting to " + redirect);
        RequestDispatcher rd = request.getRequestDispatcher(redirect);
        rd.forward(request, response);
	}
    
    /**
     * The QueryParamSet class stores a triplet of query parameters which should be
     * used to generate a complex sub-query
     * 
     * @author Alexander Craig
     * @version May 11th 2010
     */
    private class QueryParamSet {
    	/** The "type" of the query (either "Object" or "Subject") */
    	private String queryType;
    	/** The community id for the search to take place in */
    	private String communityId;
    	/** The XPath of the element which contains the link uri */
    	private String linkXPath;
    	
    	/** 
    	 * Generates a new QueryParamSet with the specified parameters.
    	 * These parameters should remain static once the object is instantiated.
    	 * 
    	 * @param type	The "type" of the query (either "Object" or "Subject")
    	 * @param commId	The community id for the search to take place in
    	 * @param xPath	The XPath of the element which contains the link uri
    	 */
    	public QueryParamSet(String type, String commId, String xPath) {
    		queryType = type;
    		communityId = commId;
    		linkXPath = xPath;
    	}

    	/**
    	 * @return	The "type" of the query (either "Object" or "Subject")
    	 */
		public String getQueryType() {
			return queryType;
		}

		/**
		 * @return	The community id for the search to take place in
		 */
		public String getCommunityId() {
			return communityId;
		}

		/**
		 * @return	The XPath of the element which contains the link uri
		 */
		public String getLinkXPath() {
			return linkXPath;
		}
		
		/** 
		 * Generates a non-transitive complex query agent to handle a tuple with the query params specified.
		 * @param inputQueryId	The input query Id for the subquery. The output will be the input incremented by 1.
		 * @return	The agent to handle the specified subquery.
		 */
		public BasicWorker generateBasicAgent(int inputQueryId) {
			return generateTransitiveAgent(inputQueryId, inputQueryId + 1, inputQueryId + 1);
		}
		
		/** 
		 * Generates a recursive agent which both accepts input and posts results under the specified query id.
		 * @param inputQueryId	The input and output queryId for the recursive agent.
		 * @return	The agent to handle the specified subquery.
		 */
		public BasicWorker generateRecursiveAgent(int queryId) {
			// The recursive agent is implemented by using the same query id for both
			// input and output tuples
			
			// TODO: Not sure if cycles will be infinitely processed or if already processed tuples
			// are already ignored. Need to check this!
			
			return generateTransitiveAgent(queryId, queryId, queryId);
		}
		
		/** 
		 * Generates a transitive complex query agent to handle a tuple with the query params specified.
		 * @param inputQueryId	The input query Id for the subquery. The output will be the input incremented by 1.
		 * @param transQueryId	The transitive output id.
		 * @return	The agent to handle the specified subquery.
		 */
		public BasicWorker generateTransitiveAgent(int inputQueryId, int outputQueryId, int transQueryId) {
			BasicWorker returnAgent;
			
            if(getQueryType().equals("Object")) {
                returnAgent = UP2PAgentFactory.createTripleAgent(adapter.getDefWebAdapter().getTS(), UP2PAgentFactory.OOO, getCommunityId(), 
                   getLinkXPath(), Integer.toString(inputQueryId), Integer.toString(outputQueryId), Integer.toString(transQueryId));
                LOG.info("GraphQueryServlet: Adding Object Query Agent\n\tType: " + getQueryType() + "\n\tComm Id: " + getCommunityId()
                		+ "\n\tLink XPath: " + getLinkXPath() + "\n\tIn QueryId:" + inputQueryId + "\tOut QueryId: " + outputQueryId);
            } else {
            	returnAgent = UP2PAgentFactory.createTripleAgent(adapter.getDefWebAdapter().getTS(), UP2PAgentFactory.SSS, getCommunityId(), 
                        getLinkXPath(), Integer.toString(inputQueryId), Integer.toString(outputQueryId), Integer.toString(transQueryId));
            	LOG.info("GraphQueryServlet: Adding Subject Query Agent\n\tType: " + getQueryType() + "\n\tComm Id: " + getCommunityId()
                		+ "\n\tLink XPath: " + getLinkXPath() + "\n\tIn QueryId:" + inputQueryId + "\tOut QueryId: " + outputQueryId);
            }
            
            // TODO: Code assumes "type" will always be "Object" or "Subject", should add error handling for other cases just in case.
            
            return returnAgent;
		}
    }
}
