package up2p.tspace;

import java.util.ArrayList;
import java.util.List;

import up2p.repository.Repository;
import up2p.servlet.HttpParams;

import lights.Field;
import lights.TupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.IValuedField;
import lights.interfaces.TupleSpaceException;

public class UP2PFunctionWorker extends UP2PWorker {
	
	private static int BEGINCOMM = 5;
	private static int ENDCOMM = 37;
	private static int BEGINRES = 38;
	
	
	public UP2PFunctionWorker(ITupleSpace ts){
		super(ts);
		name = "BuiltInFunctions";
		
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.GET_COMMFROMURI, 2));
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.GET_RESID_FROM_URI, 2));
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.CONCAT, 3));
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.CONCAT, 4));
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.CONCAT, 5));
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.CONCAT, 6));
		
		//addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.CR_TO_URI, 3));
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.URI_TO_CR, 2)); //dereference URI
	}
	
	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple query) {

		List<ITuple> anslist = new ArrayList<ITuple>();

		try {
			String verb = (String)((Field)query.get(0)).getValue();
			space.in(query); //remove query from tspace
			if (verb.equals(TupleFactory.CONCAT)) {
				/*SPEC:
				input : (CONCAT, x1, x2,... xn, qid)
				output: (CONCAT, x1, x2,... xn, x1+x2+...+xn, qid)
				*/
				String concatenation = "";
				//loop through all fields except first and last, concatenate to a single string
				String[] responsefields = new String[query.length()+1];
				responsefields[0] = TupleFactory.CONCATANSWER;
				
				for (int i = 1; i<query.length()-1; i++){
					String input = (String)((Field)query.get(i)).getValue();
					concatenation += input;
					responsefields[i] = input; // place input string again in output
				}
				//place concatenation in second last field of answer
				responsefields[query.length()-1] =concatenation;
				//get qid and place last in answer
				String qid = (String)((Field)query.get(query.length()-1)).getValue();
				responsefields[query.length()] = qid;
				               
				//create response
				anslist.add(TupleFactory.createTuple(responsefields));
				
			}
			else if (verb.equals(TupleFactory.GET_COMMFROMURI) || verb.equals(TupleFactory.GET_RESID_FROM_URI)) {


				IValuedField f = (Field)query.get(1);
				String URI = (String)f.getValue();

				IValuedField f2 = (Field)query.get(2);
				String qid = (String)f2.getValue();

				String communityId = URI.substring(BEGINCOMM, ENDCOMM);
				String resID = URI.substring(BEGINRES);
				String answerverb = "";
				String answerfield = "";
				if (verb.equals(TupleFactory.GET_COMMFROMURI)){
					answerverb = TupleFactory.COMMUNITY_FROM_URI;
					answerfield = communityId;
				}
				else {
					answerverb= TupleFactory.RESID_FROM_URI; 
					answerfield = resID;
				}
				//this is the answer to the query
				anslist.add(TupleFactory.createTuple(answerverb, new String[]{answerfield,qid}));
			}
			else if (verb.equals(TupleFactory.URI_TO_CR)) { //dereference URI
				//spec: in   (URI TO CR, uri, id)
				//		out  (searchXpath, [commid], resourceid=[resid], id)  
				//      out  (URI TO CR resp, URI, [commid],[resid], id)
				IValuedField f = (Field)query.get(1);
				String URI = (String)f.getValue();

				IValuedField f2 = (Field)query.get(2);
				String qid = (String)f2.getValue();

				String communityId = URI.substring(BEGINCOMM, ENDCOMM);
				String resID = URI.substring(BEGINRES);
				
				//send out search for document
				anslist.add(TupleFactory.createSearchTuple(communityId, Repository.RESOLVE_URI+resID, 
						qid+System.currentTimeMillis(), HttpParams.UP2P_SEARCH_ALL));
				//respond by C+R for caller
				anslist.add(TupleFactory.createTuple(TupleFactory.URI_TO_CR_RESP, new String[]{URI, communityId, resID, qid}));
				
			}

		} catch (TupleSpaceException e) { //catches exceptions for TS operation
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return anslist;

	}

}
