package up2p.peer.jtella;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import up2p.xml.DOMTransformer;
import up2p.xml.TransformerHelper;
import up2p.search.SearchResponse;

public class TestSearchResponseXML {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws MalformedPeerMessageException 
	 */
	public static void main(String[] args) throws SAXException, IOException, MalformedPeerMessageException {
		
		File input = new File("TestSearchMessage.xml");
		
		Document msg = TransformerHelper.parseXML(input);
		
		System.out.println("input search message:");
		DOMTransformer.prettyPrint(msg);
		
		SearchResponseMessage SRM = SearchResponseMessage.parse(msg.getDocumentElement());
		
		for (SearchResponse s: SRM.getResponses()){
			System.out.println(s.toString());
			System.out.println("------------------");
		}
		
		Node backout = SRM.serialize();
		System.out.println("printing back out after parse/ serialize:");
		DOMTransformer.prettyPrint(backout.getOwnerDocument());
		

	}

}
