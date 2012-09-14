package up2p.rdf;

import org.openrdf.model.Value;

/**
 * local implementation of the openrdf Value interface,
 *
 */
public class Up2pRDFValue implements Value {

	protected String stringValue;
	
	public Up2pRDFValue(String val){
		stringValue = val;
	}
	
	public Up2pRDFValue(){
		this("");
	}

	public String toString(){
		return stringValue;
	}
	
	public void setValue(String val){
		stringValue = val;
	}
	
	public boolean equals(Object other){
		if (other instanceof Up2pRDFValue) {
			Up2pRDFValue v = (Up2pRDFValue)other;
			if (v.toString().equals(stringValue))
				return true;
		}
		return false;
	}
	
}
