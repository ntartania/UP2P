package up2p.tspace.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lights.TupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.TupleSpaceException;
import up2p.tspace.*;
import up2p.xml.DOMTransformer;
import up2p.xml.TransformerHelper;

public class TestXMLAgents {

	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException{
		
			
		if (args.length <= 0) {
		      System.out.println("Usage: java TestXMLAgents URL");
		      return;
		    }
		    
		    String url = args[0];
		    
		    String xslt = "";
		    if (args.length ==2) {
		    	xslt = args[1];
		    	
		    	System.out.println("using files :" +url +","+ xslt);
		    	testTransform(url, xslt);
		    }
		    
		    //testAgent(url);
		    
		    //testParsing(url);
	
		
	}
	
	private static void testParsing(String url) throws FileNotFoundException, SAXException, IOException {
		// TODO Auto-generated method stub
		
		//Document xml =	TransformerHelper.parseXMLtoExternalForm(new FileInputStream( new File(url)));
	}

	private static void testTransform(String url, String xslurl) throws IOException {
		Document xml = DOMTransformer.parseFileToDOM(url);
		File xslt = new File(xslurl);
		
		
		Map<String,String> paramTable = new HashMap<String,String>();
        // put the URI to the resource into the parameters sent to the
        // stylesheet
        paramTable.put("up2p-link", "duh"
                + "/view.jsp?up2p:community=" + "duh" + "&up2p:resource="
                + "chabada");
        paramTable.put("up2p-community-id", "duhcid");
        paramTable.put("up2p-resource-id", "duhresid");
        paramTable.put("up2p-root-community-id", "duhn");
        paramTable.put("up2p-base-url", "http://" +"duhdf" + ":" + "deuh"
                + "/up2p/");
        paramTable.put("up2p-filename", "duwerh");
        paramTable.put("up2p-resource-title", "yellow");
        paramTable.put("oddColor", "yellow");
        paramTable.put("evenColor", "blue");
        Node result = null;
        try{
		result = TransformerHelper.transform(xml,xslt,paramTable);
        }
        catch(DOMException de){
        	System.out.println("check the XSL templates and make sure they all output an XML TREE with a proper root.");
        	de.printStackTrace();
        }
		
		if (result instanceof Document)
			DOMTransformer.prettyPrint((Document)result); //outputs nice XML to the std out
		else
		
			System.out.println("Result not a Document");
		FileWriter out = new FileWriter(new File("transformresult.html"));
		TransformerHelper.encodedTransform(((Document)result).getDocumentElement(), "UTF-8", out, true);
		
	}
	
	

	private static void testAgent(String url) {
		
		TupleSpace ts = new TupleSpace();
		

		Document xmldefinition = DOMTransformer.parseFileToDOM(url);
		BasicWorker worker = UP2PAgentFactory.createWorkerFromXML(xmldefinition, ts);
		System.out.println(worker.listInstructions());
/*		
		Document resourceXML=null;
		try {
			resourceXML = TransformerHelper.parseXML(new FileInputStream( new File(url)));
			Element el = resourceXML.getDocumentElement(); 
			Object[] fields = new Object[]{"jeoulk", "resid", el};
			ITuple myt2 = TupleFactory.createTuple(TupleFactory.PUBLISH, fields);
			System.out.println("other tuple:"+myt2.toString());
			
			System.out.println("match: " + myt.matches(myt2)+ " || "+ myt2.matches(myt));
			
			ITuple excep = TupleFactory.createTuple(TupleFactory.NOTIFY_ERROR, new Exception());
			System.out.println("exception tuple:"+excep.toString());
			ts.out(excep);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		}*/
	}
	
}
