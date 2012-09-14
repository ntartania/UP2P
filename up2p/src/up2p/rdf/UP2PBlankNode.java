package up2p.rdf;

import org.openrdf.model.BNode;

public class UP2PBlankNode extends Up2pRDFResource implements BNode {

	public UP2PBlankNode(String arg) {
		super(arg);

	}

	public String getID() {

		return stringValue;
	}

}
