package up2p.rdf;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class Up2pRDFStatement implements Statement {

	Resource subject;
	URI predicate;
	Value object;
	
	public Up2pRDFStatement(Resource subj, URI pred, Value obj){
		subject = subj;
		predicate = pred;
		object = obj;
	}
	
	@Override
	public Resource getContext() {
		// TODO Auto-generated method stub
		//TODO: the context of a statement is the graph that it's found in ?
		return null;
	}

	@Override
	public Value getObject() {
		
		return object;
	}

	@Override
	public URI getPredicate() {

		return predicate;
	}

	@Override
	public Resource getSubject() {

		return subject;
	}

}
