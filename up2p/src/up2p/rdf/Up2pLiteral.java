package up2p.rdf;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

public class Up2pLiteral extends Up2pRDFValue implements Literal {

	
	public Up2pLiteral(String val){
		super(val);
	}
	
	public Up2pLiteral(){
		this("");
	}

	public URI getDatatype() { //TODO : maybe the default community should be some real document, or the "black hole" document 
		return new Up2pURI("up2p:00000000000000000000000000000000/00000000000000000000000000000000");
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return toString();
	}

	public String getLanguage() {
		// TODO Auto-generated method stub
		return "undefined";
	}
}
