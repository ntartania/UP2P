package console;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;


import lights.Field;
import lights.extensions.XMLField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import up2p.core.LocationEntry;
import up2p.search.SearchResponse;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PWorker;
import up2p.xml.TransformerHelper;

public class QueryLogWorker extends UP2PWorker {

	FileWriter out2file;
	Long basetime;

	public QueryLogWorker(ITupleSpace ts){
		super(ts);
		name = "QueryLogger";
		basetime = System.currentTimeMillis();
	
		addQueryTemplate(TupleFactory.createSearchReplyTemplateWithDOM());
		addQueryTemplate(TupleFactory.createSearchTemplate());
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.COMPLEXQUERY, 2));

		try {
			out2file = new FileWriter("QUERYLOGS.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: add a template for lookups
	}

	@Override
	protected List<ITuple> answerQuery(ITuple template_not_used, ITuple tu) {
		Long mytime = System.currentTimeMillis()-basetime;
		// TODO Auto-generated method stub
		List<ITuple> ansTuple= new ArrayList<ITuple>(); //will be created by the factory according to the query
		String verb = ((Field) tu.get(0)).toString(); //what did we just read?

		if (verb.equals(TupleFactory.SEARCHXPATHANSWER)){ //a searchResponse!
			//each tuple : result of search
			String comId = ((Field) tu.get(1)).toString();
			String resId = ((Field) tu.get(2)).toString();
			String title = ((Field) tu.get(3)).toString();
			//String fname = ((Field) tu.get(4)).toString();
			//String location = ((Field) tu.get(5)).toString();
			String qid = ((Field) tu.get(6)).toString();

			try {
				out2file.write(mytime+"\t ANSWER \t"+qid + "\t"+"Got async Answer:"+ comId +", " + resId +", "+ title+".\n");
				out2file.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// indicate the location for the file (URL)

			
		} else if (verb.equals(TupleFactory.SEARCHXPATH)){ //a searchResponse!
			//each tuple : result of search
			String comId = ((Field) tu.get(1)).toString();
			String xpath = ((Field) tu.get(2)).toString();
			String qid = ((Field) tu.get(3)).toString();
			//String fname = ((Field) tu.get(4)).toString();
			

			try {
				out2file.write(mytime +"\t OUTPUT \t"+qid + "\t"+" query "+ comId +", " + xpath +".\n");
				out2file.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// indicate the location for the file (URL)
			
			
		}else if (verb.equals(TupleFactory.COMPLEXQUERY)){
			try {
				out2file.write(mytime +"\t QUERY TRIGGERED.\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ansTuple; //in fact we don't need to return anything.
	}

}



