package up2p.rdf;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;


public class Up2pRDFFactory implements ValueFactory {

	/**
	 * http://en.wikipedia.org/wiki/Singleton_pattern
	 * 
	 * Singleton pattern used with the UP2P implementation of ValueFactory, so that there's only one instance
	 * of the factory running at any given time.
	 */
	private static class SingletonHolder {
		private final static Up2pRDFFactory thefactory = new Up2pRDFFactory();
	}

	private Up2pRDFFactory(){
		//nothing here
	}

	public static Up2pRDFFactory getSingleton(){

		return SingletonHolder.thefactory;
	}

	public BNode createBNode() {
		return new UP2PBlankNode("");
	}

	public BNode createBNode(String arg0) {
		return new UP2PBlankNode(arg0);
		}

	public Literal createLiteral(String arg0) {
		return new Up2pLiteral(arg0);
	}

	public Literal createLiteral(boolean arg0) {
		// TODO Auto-generated method stub
		return createLiteral(String.valueOf(arg0));
	}

	public Literal createLiteral() {
		// TODO Auto-generated method stub
		return new Up2pLiteral("");
	}

	public Literal createLiteral(int arg0) {
		// TODO Auto-generated method stub
		return createLiteral(String.valueOf(arg0));
	}

	public Literal createLiteral(short arg0) {
		// TODO Auto-generated method stub
		return createLiteral(String.valueOf(arg0));
	}

	public Literal createLiteral(byte arg0) {
		return createLiteral(String.valueOf(arg0));
	}

	public Literal createLiteral(double arg0) {
		return createLiteral(String.valueOf(arg0));	}

	public Literal createLiteral(float arg0) {
		return createLiteral(String.valueOf(arg0));	}

	public Literal createLiteral(String arg0, String arg1) {
		return createLiteral(String.valueOf(arg0));
	}

	public Literal createLiteral(String arg0, URI arg1) {
		return createLiteral(String.valueOf(arg0));
	}

	public Statement createStatement(Resource arg0, URI arg1, Value arg2) {
		return new Up2pRDFStatement(arg0, arg1, arg2);
	}

	public Statement createStatement(Resource arg0, URI arg1, Value arg2,
			Resource arg3) {
		return new Up2pRDFStatement(arg0, arg1, arg2);
	}

	public URI createURI(String arg0) {
		return new Up2pURI(arg0);
	}

	public URI createURI(String arg0, String arg1) {
		return new Up2pURI(arg1); //arg0 must be the namespace
	}

	public Literal createLiteral(long arg0) {

		return createLiteral(String.valueOf(arg0));	
	}
}
