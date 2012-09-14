package up2p.tspace;

//import lights.Field;
import java.util.List;

import lights.Tuple;
//import lights.TupleSpace;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

//import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import up2p.servlet.HttpParams;
//import org.w3c.dom.traversal.NodeFilter;
//import org.w3c.dom.traversal.NodeIterator;

public class UP2PAgentFactory {

public static final int LITTERAL = 0;
public static final int FORMAL = 1;
public static final int VARIABLE = 2;

//different types of triples in complex queries
public static final int SSS = 0;
public static final int OOO = 1;
public static final int FIN = 2;
/*public static final int OSV = 3;
public static final int SOC = 4;
public static final int SOV = 5;
public static final int OOC = 6;
public static final int OOV = 7;*/




	public static BasicWorker createWorkerFromXML(Document xmldefinition, ITupleSpace ts) {

		BasicWorker agent = new BasicWorker(ts);

		//DocumentImpl traversal = new DocumentImpl();
		Node root = xmldefinition.getFirstChild();
		String name = root.getAttributes().getNamedItem("name").getNodeValue();
		
		agent.setName(name);

		NodeList ruleList = root.getChildNodes(); // the list of all rules
		
		Node currentRule;
		TupleInstruction headInstruction, nextInstruction;
		//for each rule:
		for (int i=0; i<ruleList.getLength(); i++){
			currentRule=ruleList.item(i);
			
			Node headNode = currentRule.getFirstChild();
			//if name is not "head" then throw an error
			
			headInstruction = createTupleInstructionFromXML(headNode);
			agent.newRule(headInstruction);
				
			Node tailNode = currentRule.getLastChild();
			//if name is not "tail" then throw an error
			NodeList tailSet = tailNode.getChildNodes(); 
			
			//loop through the instructions in the tail
			Node currentNode;
			for (int j=0; j<tailSet.getLength(); j++){
				currentNode=tailSet.item(j);

				nextInstruction = createTupleInstructionFromXML(currentNode); 
				//current node is the subtree of current <Tuple> element

				agent.addInstruction(headInstruction.getTuple(), nextInstruction);
			}
		}
		
		return agent;
	}


	
	private static TupleInstruction createTupleInstructionFromXML(Node tupleNode) {
		
		String verb = "";
		try {
			// The current node is an element Tuple with an attribute called  "verb", we read it and remove it
			verb = tupleNode.getAttributes().getNamedItem("verb").getNodeValue(); 
		} catch (DOMException e){
			//error: no attribute ?
		}
		
		NodeList fieldNodes = tupleNode.getChildNodes(); 
		Node currentNode;
		ITuple tuple = new Tuple();
		for (int i=0; i<fieldNodes.getLength(); i++){
			currentNode=fieldNodes.item(i);

			String fieldType = "";
			try {
				//There should be an attribute indicating the type of field (formal or litteral)
				fieldType = currentNode.getAttributes().getNamedItem("type").getNodeValue(); 

				IField newfield = null;
				int fieldtype;
				if (fieldType.equalsIgnoreCase("litteral"))
					fieldtype = LITTERAL; //default
				else if (fieldType.equalsIgnoreCase("formal"))
					fieldtype = FORMAL; 
				else if (fieldType.equalsIgnoreCase("variable"))
					fieldtype = VARIABLE;
				else 
					fieldtype = 99; //error

				newfield = createField(currentNode.getFirstChild().getNodeValue(), fieldtype); // the only child should be the tex tof the littereal node

				tuple.add(newfield); 

			} catch (DOMException e){
				//error: no attribute ?
			}


		}
		return new TupleInstruction(verb, tuple);
	}

	/**
	 * create a tuple field  
	 * @param nodeValue the text representing a litteral (for litteral-type fields), or a variable name (for formal fields and our additional "variable" type field. 
	 * @param type the type of field to implement
	 * @return
	 */
	private static IField createField(String nodeValue, int type) {
		
		NameValueField field = new NameValueField();
		
		switch (type){
		case FORMAL: // a formal field here allow to read a string value and store it in the provided variable name 
			field.setType(String.class); //we assume it's always a string
			field.setVarName(nodeValue); // the given "value" is the variable name
			break;
		case LITTERAL: 
			field.setValue(nodeValue);
			break;
		case VARIABLE:  // we will obtain a litteral field with a non null varName, which means it needs some completion
			field.setType(String.class); 
			field.setValue(nodeValue); 
			field.setVarName(nodeValue); // the given "value" is the variable name
			break;	
		default:
			field.setValue("ERROR");
		}
		
		return field;
	}

	/**
	 * create a list of agents to process a query made up of triples 
	 * @param LinkCommunityId
	 * @return
	 */
	public static List<BasicWorker> createComplexQueryAgent(Document xmldefinition, ITupleSpace ts){
///////////do something with path ! see SearchQuery.java /////////////////
		return null;
	}
	
	/**
	 *  create a single-rule agent to handle a triple 
	 * @param type type of triple (SSS or OOO) [see documentation]
	 * @param community community to search [applicable for type SSS]
	 * @param path xpath to search : takes format (eg) "PLAY/TITLE"
	 * @param input input queryid 
	 * @param output output queryid
	 * @param transOutput A second output query id used for transitive complex searches
	 * @return An agent with a single rule 
	 */
	public static BasicWorker createTripleAgent(ITupleSpace ts, int type, String community, String path, 
				String input, String output, String transOutput){
		
		
		BasicWorker agent = new BasicWorker(ts);

		agent.setName("queryAgent");

//////////////////////
		//first head : if previous answer arrives as community + resource
		//head = rd [searchresponse, ?c, ?r, string, string, string, input] --input is queryId

		ITuple headCR = new Tuple();

		headCR.add(createField(TupleFactory.SEARCHXPATHANSWER, LITTERAL));
		headCR.add(createField("community", FORMAL)); // formal field assigning the read value to variable "community"
		headCR.add(createField("resid", FORMAL)); // formal field assigning the read value to variable "resid"
		headCR.add(createField("string1", FORMAL)); // formal field (title)
		headCR.add(createField("string2", FORMAL)); // formal field (filename) 
		headCR.add(createField("string3", FORMAL)); // formal field (location)
		headCR.add(createField(input, LITTERAL)); //litteral string field with value: the input query id

		//create a rule starting with this tuple (head)
		agent.newRule(new TupleInstruction("read", headCR));

		//second head: if previous answer arrives as lookupSR
		
		ITuple headURI = new Tuple();
		//"LookupSearchResponseAnswer"; resourceId; communityId ; xPath; answervalue; qid
		headURI.add(createField(TupleFactory.LOOKUPSEARCHRESP_ANS, LITTERAL));
		headURI.add(createField("residnotused", FORMAL)); // formal field 
		headURI.add(createField("comidnotused", FORMAL)); // formal field 
		headURI.add(createField("xpathnotused", FORMAL)); // formal field 
		headURI.add(createField("uri1", FORMAL)); // formal field
		headURI.add(createField(input, LITTERAL)); //litteral string field with value: the input query id

		//create a rule starting with this tuple (head)
		agent.newRule(new TupleInstruction("read", headURI));
		
		//third head: if we're at the start : input will arrive as "ComplexQuery", inputURI, id0
		
		ITuple headURIStart = new Tuple();
		//"LookupSearchResponseAnswer"; resourceId; communityId ; xPath; answervalue; qid
		headURIStart.add(createField(TupleFactory.COMPLEXQUERY, LITTERAL));
		headURIStart.add(createField("uri1", FORMAL)); // formal field
		headURIStart.add(createField(input, LITTERAL)); //litteral string field with value: the input query id

		//create a rule starting with this tuple (head)
		agent.newRule(new TupleInstruction("read", headURIStart));
		
		///////////////////////
		
		switch (type){
		case SSS:
		
			//path needs to be made a xpath search predicate form
			while (path.startsWith("/")){
				path = path.substring(1);
			}
			String pathbeginning =	path.substring(0, path.indexOf("/"));
			String pathEnd = path.substring(path.indexOf("/"));
			path = "/"+pathbeginning + "[."+pathEnd+" = '";
			//obtain form like "PLAY[./TITLE = '"
			
			//tail for headCR: 
			//1: concatenate up2p:C/R to get URI -------------------------
			
			//  out [CRtoURI, $c, $r, input]
			ITuple ruletuple = new Tuple();
			ruletuple.add(createField(TupleFactory.CONCAT, LITTERAL));
			ruletuple.add(createField("up2p:", LITTERAL));
			ruletuple.add(createField("community", VARIABLE));
			ruletuple.add(createField("/", LITTERAL));
			ruletuple.add(createField("resid", VARIABLE));
			ruletuple.add(createField(input, LITTERAL));

			agent.addInstruction(headCR, new TupleInstruction("out", ruletuple));
			//    in [CRtoURIResp, $c, $r, ?URI]
			ruletuple=new Tuple(); //clear existing tuple
			ruletuple.add(createField(TupleFactory.CONCATANSWER, LITTERAL));
			ruletuple.add(createField("up2p:", LITTERAL));
			ruletuple.add(createField("community", VARIABLE));
			ruletuple.add(createField("/", LITTERAL));
			ruletuple.add(createField("resid", VARIABLE));
			ruletuple.add(createField("uri1", FORMAL));
			ruletuple.add(createField(input, LITTERAL)); //id

			agent.addInstruction(headCR, new TupleInstruction("in", ruletuple));

			//we now have variable "uri1", either from this concatenation, or from headURI / headURI Start  
			
			// 2: concatenate path and uri to make xpath search expression  ----------
			//this applies to all heads, so we clone tuple and make instruction for all 3 rules.			
			
			//out [concat, path, $URI, "]", input]--------------
			ruletuple=new Tuple();
			ruletuple.add(createField(TupleFactory.CONCAT, LITTERAL));
			ruletuple.add(createField(path, LITTERAL));
			ruletuple.add(createField("uri1", VARIABLE));
			ruletuple.add(createField("']", LITTERAL)); // completes path predicate expression
			ruletuple.add(createField(input, LITTERAL)); //id

			//add this instruction to all three heads
			agent.addInstruction(headCR, new TupleInstruction("out", ruletuple));
			ITuple ruletuple2 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURI, new TupleInstruction("out", ruletuple2));
			ITuple ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("out", ruletuple3));

			//    in [concatR, ?conc, input]------------------
			ruletuple=new Tuple();
			ruletuple.add(createField(TupleFactory.CONCATANSWER, LITTERAL));
			ruletuple.add(createField(path, LITTERAL));
			ruletuple.add(createField("uri1", VARIABLE));
			ruletuple.add(createField("']", LITTERAL));
			ruletuple.add(createField("concat", FORMAL));
			ruletuple.add(createField(input, LITTERAL));

			//add this instruction to all three heads
			agent.addInstruction(headCR, new TupleInstruction("in", ruletuple));
			ruletuple2 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURI, new TupleInstruction("in", ruletuple2));
			ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("in", ruletuple3));

			//3: output search ----------------------------------------------
			//   out [search, community, $concat, output]
			ruletuple = new Tuple();
			ruletuple.add(createField(TupleFactory.SEARCHXPATH, LITTERAL));
			ruletuple.add(createField(community, LITTERAL));
			ruletuple.add(createField("concat", VARIABLE));
			ruletuple.add(createField(output, LITTERAL)); //query id for output
			ruletuple.add(createField(Integer.toString(HttpParams.UP2P_SEARCH_ALL), LITTERAL)); // Extent of the search
			
			agent.addInstruction(headCR, new TupleInstruction("out", ruletuple));
			ruletuple2 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURI, new TupleInstruction("out", ruletuple2));
			ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("out", ruletuple3));
			
			if(transOutput != null && !transOutput.equals(output)) {
				ruletuple = new Tuple();
				ruletuple.add(createField(TupleFactory.SEARCHXPATH, LITTERAL));
				ruletuple.add(createField(community, LITTERAL));
				ruletuple.add(createField("concat", VARIABLE));
				ruletuple.add(createField(transOutput, LITTERAL)); //query id for output
				ruletuple.add(createField(Integer.toString(HttpParams.UP2P_SEARCH_ALL), LITTERAL)); // Extent of the search

				agent.addInstruction(headCR, new TupleInstruction("out", ruletuple));
				ruletuple2 = (ITuple)ruletuple.clone();
				agent.addInstruction(headURI, new TupleInstruction("out", ruletuple2));
				ruletuple3 = (ITuple)ruletuple.clone();
				agent.addInstruction(headURIStart, new TupleInstruction("out", ruletuple3));
			}
			
			//--------------------------------
			//second rule to cover the case where the answer comes from a lookup
			break;

		case OOO:
			
			//tail for headURI / headURIStart :
			
			//1- dereference URI
			
			ruletuple=new Tuple();
			ruletuple.add(createField(TupleFactory.URI_TO_CR, LITTERAL));
			ruletuple.add(createField("uri1", VARIABLE));
			ruletuple.add(createField(input+"DEREF", LITTERAL)); //id //change qid so things don't get mixed up
			// 1 (ans) obtain comm + res as SR
			
			agent.addInstruction(headURI, new TupleInstruction("out", ruletuple));
			ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("out", ruletuple3));
			
			//2- get response of dereferencing
			
			//   in [concat, path, input, "]", input]
			ruletuple=new Tuple();
			ruletuple.add(createField(TupleFactory.URI_TO_CR_RESP, LITTERAL));
			ruletuple.add(createField("uri1", VARIABLE)); // formal field 
			ruletuple.add(createField("community", FORMAL)); // formal field 
			ruletuple.add(createField("resid", FORMAL)); // formal field 
			ruletuple.add(createField(input+"DEREF", LITTERAL)); // formal field
			
			agent.addInstruction(headURI, new TupleInstruction("in", ruletuple));
			ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("in", ruletuple3));
			
			//3 -- get searchresponse saying that the doc was found
			
			//    in [searchxpathanswer, $com, $res ... ]
			ruletuple=new Tuple();
			//"LookupSearchResponseAnswer"; resourceId; communityId ; xPath; answervalue; qid
			ruletuple.add(createField(TupleFactory.SEARCHXPATHANSWER, LITTERAL));
			ruletuple.add(createField("community", VARIABLE)); // communityid we're looking for
			ruletuple.add(createField("resid", VARIABLE)); // resource that we're waiting for 
			ruletuple.add(createField("1notused", FORMAL)); // title 
			ruletuple.add(createField("2notused", FORMAL)); // filename
			ruletuple.add(createField("3notused", FORMAL)); // location
			ruletuple.add(createField("4notused", FORMAL)); // qid
			
			agent.addInstruction(headURI, new TupleInstruction("read", ruletuple));
			ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("read", ruletuple3));

			//--- URI is dereferenced!!
			
			//   "LookupSearchResponse"; $resid; $community ; path; output)
			ruletuple = new Tuple();
			ruletuple.add(createField(TupleFactory.LOOKUPSEARCHRESP, LITTERAL));
			ruletuple.add(createField("resid", VARIABLE));
			ruletuple.add(createField("community", VARIABLE));
			ruletuple.add(createField(path, LITTERAL));
			ruletuple.add(createField(output, LITTERAL)); //query id for output

			agent.addInstruction(headCR, new TupleInstruction("out", ruletuple));
			ruletuple2 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURI, new TupleInstruction("out", ruletuple2));
			ruletuple3 = (ITuple)ruletuple.clone();
			agent.addInstruction(headURIStart, new TupleInstruction("out", ruletuple3));
			
			if(transOutput != null && !transOutput.equals(output)) {
				ruletuple = new Tuple();
				ruletuple.add(createField(TupleFactory.LOOKUPSEARCHRESP, LITTERAL));
				ruletuple.add(createField("resid", VARIABLE));
				ruletuple.add(createField("community", VARIABLE));
				ruletuple.add(createField(path, LITTERAL));
				ruletuple.add(createField(transOutput, LITTERAL)); //query id for output

				agent.addInstruction(headCR, new TupleInstruction("out", ruletuple));
				ruletuple2 = (ITuple)ruletuple.clone();
				agent.addInstruction(headURI, new TupleInstruction("out", ruletuple2));
				ruletuple3 = (ITuple)ruletuple.clone();
				agent.addInstruction(headURIStart, new TupleInstruction("out", ruletuple3));
			}
			
			break;
			
		default:
			//error
			break;
		}

		return agent;
	}
	
	/**
	 *  create a single-rule agent to handle a triple 
	 * @param type type of triple (SSS or OOO) [see documentation]
	 * @param community community to search [applicable for type SSS]
	 * @param path xpath to search : takes format (eg) "PLAY/TITLE"
	 * @param input input queryid 
	 * @param output output queryid
	 * @param  
	 * @param transOutput A second output query id used for transitive complex searches
	 * @return An agent with a single rule 
	 */
	public static BasicWorker createTripleAgent(ITupleSpace ts, int type, String community, String path, 
				String input, String output) {
		return createTripleAgent(ts, type, community, path, input, output, null);
	}
	
	/**
	 * create an agent that finalizes complex queries where the last term 
	 * is found as a URI. 
	 * The agent just waits for results as URIs and dereferences them. 
	 * @param ts : tuple space for the agent
	 * @param queryid : identifier of the last queries to wait for.
	 * @return
	 */
	public static BasicWorker createFinalizerAgent(ITupleSpace ts, String queryId){
		
		BasicWorker agent = new BasicWorker(ts);

		agent.setName("queryFinalizer");
		
		ITuple headURI = new Tuple();
		
		//"LookupSearchResponseAnswer"; resourceId; communityId ; xPath; answervalue; qid
		headURI.add(createField(TupleFactory.LOOKUPSEARCHRESP_ANS, LITTERAL));
		headURI.add(createField("residnotused", FORMAL)); // formal field 
		headURI.add(createField("comidnotused", FORMAL)); // formal field 
		headURI.add(createField("xpathnotused", FORMAL)); // formal field 
		headURI.add(createField("uri1", FORMAL)); // formal field
		headURI.add(createField(queryId, LITTERAL)); //litteral string field with value: the input query id

		//create a rule starting with this tuple (head)
		agent.newRule(new TupleInstruction("read", headURI));
		
		//tail for headURI / headURIStart :
		
		//1- dereference URI
		
		ITuple ruletuple=new Tuple();
		ruletuple.add(createField(TupleFactory.URI_TO_CR, LITTERAL));
		ruletuple.add(createField("uri1", VARIABLE));
		ruletuple.add(createField(queryId+"DEREF", LITTERAL)); //id //change qid so things don't get mixed up
		// 1 (ans) obtain comm + res as SR
		
		agent.addInstruction(headURI, new TupleInstruction("out", ruletuple));
		
		//now the final results will have many different query identifiers.
		//solution: truncate the identifiers from "DEREF" in search response repository 
		
	return agent;	
	}


}
