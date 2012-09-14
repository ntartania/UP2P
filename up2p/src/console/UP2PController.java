package console;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import lights.TupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import up2p.core.DefaultWebAdapter;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.tspace.BasicWorker;
import up2p.tspace.TupleFactory;
//import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PAgentFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class UP2PController implements SearchResponseListener {

	//config values
	public static final String defaultWorkpath = "M:\\up2p-code\\UP2PStandalone\\up2p"; 
	public static final String defaultIpAddress = "134.117.60.83";
	public static final int defaultListenPort = 8080;
	
	private String workpath;
	private String ipAddress;
	private int listenport;
	
	private DefaultWebAdapter WA;
	
	private UP2PGUI gui;
	
	private QueryLogWorker Myquerylogger;
	
	public UP2PController(String path, String IP, int port){
		
		workpath = path;
		ipAddress = IP;
		this.listenport = port;
		
		try {
			WA = new DefaultWebAdapter(workpath, port);
			
			//set IP and port
			WA.setHost(ipAddress);
			WA.setPort(listenport);
			
			Myquerylogger = new QueryLogWorker(WA.getTS());
			Myquerylogger.start();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		gui = new UP2PGUI("The UP2P baboon circus", this);
		
		
		/* Instantiate an anonymous subclass of WindowAdapter, and register it
         * as the frame's WindowListener.
         * windowClosing() is invoked when the frame is in the process of being
         * closed to terminate the application.
         */
		gui.addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						WA.shutdown();
						System.exit(0);
					}
				}
		);

		// Size the window to fit the preferred size and layout of its
		// subcomponents, then show the window.
		gui.pack();
		gui.setVisible(true);

	}
	
	/** default constructor
	 * 
	 */
	public UP2PController() {
	this(defaultWorkpath, defaultIpAddress, defaultListenPort);
	}
	
	/** default constructor 1
	 * 
	 */
	public UP2PController(String path) {
	this(path, defaultIpAddress, defaultListenPort);
	}
	/* default constructor 2
	 * 
	 * /
	public UP2PController(String path, String IP) {
	this(path, IP, defaultListenPort);
	}
	*/
	
	/**
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.setProperty("up2p.home", defaultWorkpath);
		PropertyConfigurator.configure("up2p.log4j.properties");
		
		UP2PController up2p = null;
		if (args.length==0){ //nothing is provided : use default values
			System.out.println("Usage: UP2PController [path [IPAddress port]] \n Using default values.");
			up2p = new UP2PController();	
		} else if (args.length ==1) {
			up2p = new UP2PController(args[0]);
		} 
		else if (args.length==3){
			up2p = new UP2PController(args[0], args[1], Integer.parseInt(args[2]));
		}
		else {
			System.out.println("Invalid number of arguments! \n Usage: UP2PController [path [IPAddress port]] \n Using default values.");
			up2p = new UP2PController();	
		}
		
		up2p.startTesting();

	}
	
	/**throw a few tests into the WebAdapter*/
	private void startTesting(){
		
		gui.start();
		
		gui.UserOut("Hello. I will now get the list of communities ("+WA.getRootCommunityId()+")");
		Iterator<String> iter = WA.browse(WA.getRootCommunityId()).iterator(); //search for all the communities
		
		while(iter.hasNext()){
			gui.UserOut(iter.next());
		}
		
		//WA.search("dcd983ed56b8f5bc142c1d3c27a42f72", new SearchQuery("/actor[./name &= 'Jo*']"), "dfdstgeeqid");
		
		///////////////
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		//	/ ******** my queries ********
		

		BasicWorker bill = UP2PAgentFactory.createTripleAgent(WA.getTS(), UP2PAgentFactory.OOO, "dcd983ed56b8f5bc142c1d3c27a42f72", "/actor/film", "firstQID", "secondQID");
		BasicWorker bob = UP2PAgentFactory.createTripleAgent(WA.getTS(), UP2PAgentFactory.SSS, "dcd983ed56b8f5bc142c1d3c27a42f72", "/actor/film", "secondQID", "thirdQID");
		BasicWorker fred = UP2PAgentFactory.createTripleAgent(WA.getTS(), UP2PAgentFactory.OOO, "dcd983ed56b8f5bc142c1d3c27a42f72", "/actor/film", "thirdQID", "fourthQID");
		BasicWorker tommy = UP2PAgentFactory.createTripleAgent(WA.getTS(), UP2PAgentFactory.SSS, "dcd983ed56b8f5bc142c1d3c27a42f72", "/actor/film", "fourthQID", "fifthQID");
		BasicWorker lina = UP2PAgentFactory.createTripleAgent(WA.getTS(), UP2PAgentFactory.OOO, "dcd983ed56b8f5bc142c1d3c27a42f72", "/actor/film", "fifthQID", "sixQID");
		//BasicWorker patricia = UP2PAgentFactory.createTripleAgent(WA.getTS(), UP2PAgentFactory.SSS, "dcd983ed56b8f5bc142c1d3c27a42f72", "/actor/film", "sixQID", "sevenQID");

		BasicWorker superman = UP2PAgentFactory.createFinalizerAgent(WA.getTS(), "sixQID");
		superman.start();
		
		bill.start();
		bob.start();
		fred.start();
		tommy.start();
		lina.start();
		//patricia.start();
		
	ITuple trigger = TupleFactory.createTuple(new String[]{TupleFactory.COMPLEXQUERY, "up2p:dcd983ed56b8f5bc142c1d3c27a42f72/94e4b7066c25bc47a8c979fae2e32232", "firstQID"});
		
		try {
			WA.getTS().out(trigger);
		} catch (TupleSpaceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//gui.UserOut("get bibster");
		//WA.getResourceAsDOM("14736132261e113a511a93f8980d8968", "c6e65e0b0f82baf36d7c9fecfc76b392");
		
//		WA.search("7d0cd686878de096ccbc41d4ecbcdb83", new SearchQuery("ResourceId=cec0789ea1db4106db73689bb6c3b5e9"), this, "noid");
		
		
		//System.out.println(bob.listInstructions());
		//System.out.println(bill.listInstructions());
		//Scanner keyboard = new Scanner(System.in);
		
		//gui.UserOut("I will start agent Bob");
		//BasicWorker bob = startAgent(new File("Agent1.xml"), WA.getTS());
		
		/*gui.UserOut("I will start agent Simon");
		startAgent(new File("test/agentVariables2.xml"), WA.getTS());*/
		
		
		//List<String> stringtuple = new ArrayList<String>();
		/*while(true){
			System.out.print("Write a tuple to inject: first give number of fields, then separate fields by spaces >");  // Troll asks for name.

			int n =keyboard.nextInt();
			for (int i = 0; i<n; i++){
				stringtuple.add(keyboard.next());
			}
			System.out.println();
			ITuple tuple = TupleFactory.createTuple(stringtuple);
			stringtuple.clear();
			
			System.out.println("Tuple created:"+ tuple.toString());
			injectTuple(tuple);
			System.out.println("Tuple injected!");
		}*/
	}
	
	public void injectTuple(ITuple t){
		try {
				WA.getTS().out(t);
			} catch (TupleSpaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public BasicWorker startAgent(File inputfile, ITupleSpace ts){
		Document xmldefinition = null;


		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();

			// Check for the traversal module
			DOMImplementation impl = parser.getDOMImplementation();
			if (!impl.hasFeature("traversal", "2.0")) {
				System.out.println(
						"A DOM implementation that supports traversal is required."
				);  
				return null;
			}


			FileInputStream stream = new FileInputStream(inputfile);

			/*if (inputfile.canRead()){
	    	  System.out.println("I can read the file");
	      }*/

			// Read the document
			xmldefinition = parser.parse(stream); 

			//System.out.println("node name: "+xmldefinition.getNodeName() + " getvalue: "+ xmldefinition.getNodeValue() + "child" + xmldefinition.getFirstChild().toString());


		}
		catch (SAXException e) {
			System.out.println(e);
			//System.out.println(url + " is not well-formed.");
		}
		catch (IOException e) { 
			System.out.println(
					"Due to an IOException, the parser could not check something" 
			); 
		}
		catch (FactoryConfigurationError e) { 
			System.out.println("Could not locate a factory class"); 
		}
		catch (ParserConfigurationException e) { 
			System.out.println("Could not locate a JAXP parser"); 
		}
		BasicWorker worker = null;
		if(xmldefinition != null) {
			worker = UP2PAgentFactory.createWorkerFromXML(xmldefinition, ts);
			worker.start();}
		else
			System.out.println("Some error in getting xml definition for worker");

		return worker;
	}

	/**
	 * direct access to the tuplespace (for GUI purpose)
	 * @return
	 */
	public ITupleSpace getTupleSpace() {
		return WA.getTS();
	}

	//@Override
	public void receiveSearchResponse(SearchResponse[] responses) {
		
		for (SearchResponse resp: responses){
			Logger.getLogger("up2p.experiments").info(resp.toString()+"\n");	
			}
		gui.searchResults(responses);
		
	}

}
