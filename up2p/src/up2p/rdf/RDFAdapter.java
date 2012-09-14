package up2p.rdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lights.Field;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import up2p.core.CommunityNotFoundException;
import up2p.core.ResourceNotFoundException;
import up2p.search.SearchQuery;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PWorker;

import name.levering.ryan.sparql.common.RdfSource;


/**
 * RdfSource implementation to adapt UP2P as an RDF database.  
 * @author alan
 *
 */
public class RDFAdapter implements RdfSource {

	private RDFWorker myworker;
	//private ITupleSpace myts; 
	
	public RDFAdapter(ITupleSpace ts){
	
		myworker = new RDFWorker(ts);
		myworker.start();
	}
	
	
	/*
	 * assumptions: 
	 * 
	 * To map UP2P documents to RDF triples, we have 
	 * subject is a URI (or BNode -- what's a BNode in UP2P ?)
	 * predicate is a URI -- may be :
	 * 						a up2p:commid/./full/xpath : we define the syntax up2p:[communityid]/./[xpath] as the URI for Xpath-defined properties in a particular community 
	 * 						a special URI for computable or other types of predicates up2p:thepredicatecommunity/somepredicate
	 * object is a Literal or a URI (up2p:comid/resid) (or a BNode : ?)
	 * 
	 * we know how to manage : 
	 * 
	 * (a) null ; up2p:commid/./xpath ; Value : search community commid for xpath=Value
	 * (b) URI1 ; up2p:commid/./xpath ; null : [commid must match commid of URI1] lookup xpath in document URI1
	 * 
	 * (c) null ; up2p:commid/./xpath ; null : get all docs of commid, for each doc lookup xpath  
	 * 
	 * (d) URI1 ; up2p:commid/./xpath; Value : process as (b), if Value is found in results return an iterator over the single statement.
	 * 
	 * problem : how to manage a null [wildcard] predicate efficiently ?
	 * - index all values [leaves] of an XML document. Problem : an index has a certain amount of redundancy w.r.t the original collection.
	 * 
	 *  Then we can manage :
	 *  
	 * (e) URI1 ; null ; value : indexlookup value, filter by community/resource as found from URI1, return values
	 * (f) null ; null ; value : indexlookup value, no filter, return all triples. 
	 *  
	 *  We still don't manage : 
	 *  
	 * (g) URI1 ; null ; null : this would require storing all (relevant) xpaths for each community, then to be processed as follows:
	 *  					for all xpaths of community URI1.comm lookup xpath in document URI1.
	 *  
	 * (h) null; null ; null : this would require repeating the above for all documents of all communities.
	 * 
	 *    
	 *    ==========================================
	 *    
	 *    Phase II : adding inner blank nodes (inner nodes of the XML tree) : a blank node can uniquely be identified by :
	 *    a document URI + the unique shortest and unambiguous xpath to the node.
	 *    This URI scheme can be used for inner nodes, but in fact will identify the subtree
	 *    Using the # symbol in the URI would work.
	 *    
	 */	
	
	
	/**
     * Gets all statements with a specific subject, predicate and/or object. All
     * three parameters may be null to indicate wildcards.
     * The graph parameter is not currently used. In the future may be used in the sense that a document could be a graph, 
     * and each subtree a resource. (this is accepted XML to RDF mapping)
     * 
     * possibility: for highly complex XML documents (a full shakespeare play, a chemical molecule...) could a single 
     * document be a graph, i.e. "triple" queries could be interpreted in the context of a document? what would subjects / objects be?
     * 
     * @param subj subject of pattern
     * @param pred predicate of pattern
     * @param obj object of pattern
     * @param graph the context with which to match the statements against : not used at this point (phase I)
     * @return iterator over statements
     */
    public Iterator<Statement> getStatements(Value subj, URI pred, Value obj, URI graph){
    	return getDefaultStatements(subj, pred, obj);
    }

    /**
     * Gets all statements with a specific subject, predicate and/or object in
     * the default graph of the repository. All three parameters may be null to
     * indicate wildcards. This is only used in SPARQL queries when no graph
     * names are indicated.
     * 
     * @param subj subject of pattern
     * @param pred predicate of pattern
     * @param obj object of pattern
     * @return iterator over statements
     */
    public Iterator<Statement> getDefaultStatements(Value subject, URI predicate, Value object){

    	List<Statement> myanswerList = new LinkedList<Statement>();
    	/*cases
    	 *     	predicate = up2p:commid/?/xpath*/
    	if (predicate != null && predicate instanceof Up2pURI){ //CASE : predicate NOT null and proper URI
    		try {
    			//    			(a) subject = null ==================================================================================== 
    			if (subject == null){

    				boolean check = ((Up2pURI)predicate).isXpathURI(); //should be TRUE, otherwise for now we don't know how to handle this case
    				//TODO: if check is false, raise exception, at least log something.
    				String comid =((Up2pURI)predicate).getCommunity();
    				String xpath = ((Up2pURI)predicate).getXpath();
    				// (a1) object = null ----------------------------------------------------------
    				if(object==null){ 	//case : [?, pred , ?] :get all docs of commid, for each doc lookup xpath		

    					List<String> thedocuments = myworker.synchronousLocalSearch(comid, xpath);

    					for (String docid : thedocuments){ //with each answer build a statement
    						for (String ans : myworker.getXPathLocationAsList(docid, comid, xpath)){ 
    							myanswerList.add(new Up2pRDFStatement(new Up2pURI("up2p:"+comid+"/"+docid), predicate, new Up2pRDFValue(ans))); //here we add each new statement to the list
    						}
    					}

    					//--end case a1 -------------------------------------------------------------------------------

    				} else{//				(a2) object != null -----------------------------------------------------------------
    					SearchQuery query = new SearchQuery();
    					query.addAndOperand(xpath, object.toString());//build xpath query using the object of RDF statement (input param)
    					List<String> docids = myworker.synchronousLocalSearch(comid, query.getQuery()); //comid is extracted from the input predicate

    					for (String d:docids){ //with each answer build a statement
    						myanswerList.add(new Up2pRDFStatement(new Up2pURI("up2p:"+comid+"/" + d), predicate, object));
    					}

    				}

    			} else { //subject not null =================================================================
    				//(b) subject = up2p:com/res
    				if (subject instanceof Up2pURI){
    					boolean check = ((Up2pURI)predicate).isXpathURI() && ((Up2pURI)subject).isDocumentURI(); //should be TRUE, otherwise for now we don't know how to handle this case
    					//TODO: if check is false, raise exception, at least log something.
    					String comid =((Up2pURI)predicate).getCommunity();
    					String xpath = ((Up2pURI)predicate).getXpath();

    					String docid = ((Up2pURI)subject).getResourceId();

    					check = check && (comid.equals(((Up2pURI)subject).getCommunity())); // the community Ids must match

    					// case b1              - object is null -------------------------------

    					if (object == null)
    						for (String ans : myworker.getXPathLocationAsList(docid, comid, xpath)){ 
    							myanswerList.add(new Up2pRDFStatement(new Up2pURI("up2p:"+comid+"/"+docid), predicate, new Up2pRDFValue(ans))); //here we add each new statement to the list
    						}

    					else { //case : subject, predicate, object, all non null ----------------------------
    						List<String> objectlist =  myworker.getXPathLocationAsList(docid, comid, xpath);

    						if (objectlist.contains(object.toString())){
    							myanswerList.add(new Up2pRDFStatement((Up2pURI)subject, predicate, object));
    						}
    					}
    				}

    			} //============================================


    		} catch (CommunityNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (ResourceNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	return myanswerList.iterator();

    }

    /**
     * Gets all the statements, regardless of graph context, with a specific
     * subject, predicate and/or object in the default graph of the repository.
     * All three parameters may be null to indicate wildcards.
     * 
     * @param subj subject of pattern
     * @param pred predicate of pattern
     * @param obj object of pattern
     * @return iterator over statements
     */
    public Iterator<Statement> getStatements(Value subj, URI pred, Value obj){
    	return getDefaultStatements(subj, pred, obj);
    }

    /**
     * Checks whether some statement with a specific subject, predicate and/or
     * object is present in the default graph of the repository. All three
     * parameters may be null to indicate wildcards. This is only used in SPARQL
     * queries when no graph names are indicated.
     * 
     * @param subj subject of statement
     * @param pred predicate of statement
     * @param obj object of statement
     * @return boolean indicating if specified statement is present
     */
    public boolean hasDefaultStatement(Value subj, URI pred, Value obj){
    	//TODO: there would be a faster way (get the first answer rather than all)
    	Iterator<Statement> iter = getDefaultStatements(subj, pred, obj);
    	return iter.hasNext();
        
    }

    /**
     * Checks whether some statement with a specific subject, predicate and/or
     * object is present in any graph of the repository. All three parameters
     * may be null to indicate wildcards.
     * 
     * @param subj subject of statement
     * @param pred predicate of statement
     * @param obj object of statement
     * @return boolean indicating if specified statement is present
     */
    public boolean hasStatement(Value subj, URI pred, Value obj){
    	return hasDefaultStatement(subj,pred,obj);
    }

    /**
     * Checks whether some statement with a specific subject, predicate and/or
     * object is present in the repository. All three parameters may be null to
     * indicate wildcards.
     * 
     * @param subj subject of statement
     * @param pred predicate of statement
     * @param obj object of statement
     * @param graph the context to match the statements against
     * @return boolean indicating if specified statement is present
     */
    public boolean hasStatement(Value subj, URI pred, Value obj, URI graph) {
    	return hasDefaultStatement(subj,pred,obj);
    }

    /**
     * This useful method returns a value factory that is actually used by the
     * data store. This is important for using the same type of value objects as
     * the store uses, so they can be compared with definite results.
     * 
     * @return a value factory that is used to create values in the query
     */
    public ValueFactory getValueFactory() {
    	return Up2pRDFFactory.getSingleton();
    }


private class RDFWorker extends UP2PWorker {

	public RDFWorker(ITupleSpace ts) {
		super(ts);
		
	}

	/**
	 * This method should manage RDF requests of the form <URI, URI, null>
	 * @param communityId
	 * @param xPath
	 * @return
	 * @throws CommunityNotFoundException
	 */
	public List<String> synchronousLocalSearch(String communityId, String xPath) throws CommunityNotFoundException{
		LOG.debug(name + "About to output a synchronous local search in community "+ communityId);

		String[] fields = new String[] {communityId, xPath};

		ITuple t4query = TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCH, fields); 

		ITuple t2ans =TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCHRESPONSE, fields);
		t2ans.add(new Field().setType(String.class)); // template is identical + a template field
		
		//add a random identifier
		String qid = System.currentTimeMillis() +"";
		//add the field to the query
		t4query.add(new Field().setValue(qid));

		//query tuple space using synchronous method with multiple answers

		List<String> toReturn = new ArrayList<String>();
		try {
			List<ITuple> answers = synchronousMultiQuery(t4query, t2ans);

			for (ITuple ans : answers){
				IField answerField = ans.get(3);// number of field where the response is
				String stringanswer;
				if (answerField instanceof Field){ //should always be the case but let's be defensive
					stringanswer = ((Field)answerField).getValue().toString();
					//LOG.debug("Got answer for LookupXPath: "+ answer);
					toReturn.add(stringanswer);

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
	 *  To query a xpath lookup with multiple answers
	 * @param resourceId
	 * @param communityId
	 * @param pathLocation
	 * @return
	 * @throws CommunityNotFoundException 
	 * @throws ResourceNotFoundException 
	 */
	public List<String> getXPathLocationAsList(String resourceId,
			String communityId, String pathLocation) throws CommunityNotFoundException, ResourceNotFoundException {
		//define a tuple for this query.
		LOG.debug(name + "About to query tuple space for LookupXPath: arguments "+resourceId+", "+communityId+", "+ pathLocation);

		String[] fields = new String[] {resourceId, communityId, pathLocation};

		ITuple t4query = TupleFactory.createTuple(TupleFactory.LOOKUPXPATH, fields); 

		ITuple t2ans =TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, fields);
		t2ans.add(new Field().setType(String.class)); // template is identical + a template field 
			//TupleFactory.createQueryTupleTemplate(TupleFactory.LOOKUPXPATHANSWER, 4);////2 fields for the answer template

		//query tuple space using synchronous method with multiple answers

		List<String> toReturn = new ArrayList<String>();
		try {
			List<ITuple> answers = synchronousMultiQuery(t4query, t2ans);

			for (ITuple ans : answers){
				IField answerField = ans.get(4);
				String stringanswer;
				if (answerField instanceof Field){ //should always be the case but let's be defensive
					stringanswer = ((Field)answerField).getValue().toString();
					//LOG.debug("Got answer for LookupXPath: "+ answer);
					toReturn.add(stringanswer);

					if (stringanswer.equals(TupleFactory.RESOURCE_NOT_FOUND))
						throw new ResourceNotFoundException();
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


	
	
	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple query) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
}
