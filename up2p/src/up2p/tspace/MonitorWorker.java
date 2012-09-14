package up2p.tspace;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lights.Field;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

public class MonitorWorker extends UP2PWorker {


	private class IPPort{
		public String IP;
		public int port;
		IPPort(String i, int p){
			IP=i;
			port = p;
		}
		@Override
		public int hashCode(){
			return IP.hashCode()*31 +port ;
		}
	}
	
	String networkId; // an identifier to be used in communication with the monitor = the Gnutella Servent Id
	String myIPPort; // a string identifying who the 
	Set<IPPort> UDPlisteners;
	DatagramSocket clientSocket;
	//Long basetime;

	


	public MonitorWorker(ITupleSpace ts, String whoamI, String gServentId) throws SocketException{
		super(ts);
		name = "UDPMonitor";
		networkId = gServentId; //this is the gnutella listen port for this up2p instance.
		myIPPort = whoamI;
		//basetime = System.currentTimeMillis();
	
		UDPlisteners = new HashSet<IPPort>();
		clientSocket = new DatagramSocket();
		
		//TODO:add templates for all "macro" activity : publish, remove, search 
		addQueryTemplate(TupleFactory.createSearchReplyTemplate()); // searchresponse
		addQueryTemplate(TupleFactory.createSearchTemplate()); //search
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.REMOVE, 2));
		addQueryTemplate(TupleFactory.createPublishTemplate());
		addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.NOTIFYCONNECTION, 4));

		/*try {
			out2file = new FileWriter("QUERYLOGS.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//TODO: add a template for lookups
	}

	@Override
	protected List<ITuple> answerQuery(ITuple template_not_used, ITuple tu) {
		Long mytime = System.currentTimeMillis();//-basetime;
		// TODO Auto-generated method stub
		List<ITuple> ansTuple= new ArrayList<ITuple>(); //will be created by the factory according to the query
		String verb = ((Field) tu.get(0)).toString(); //what did we just read?

		if (verb.equals(TupleFactory.SEARCHXPATHANSWER)){ //a searchResponse!
			//each tuple : result of search
			String comId = ((Field) tu.get(1)).toString();
			String resId = ((Field) tu.get(2)).toString();
			String title = ((Field) tu.get(3)).toString();
			//String fname = ((Field) tu.get(4)).toString();
			String location = ((Field) tu.get(5)).toString();
			String qid = ((Field) tu.get(6)).toString();
			
			//output UDP packet to listener(s)
			
			if (location.startsWith("localhost")){//notify only of local search results
				String notification = "" + mytime +"\t"+networkId+"\t QUERYHIT \t["+qid + "]\t"+ comId +"\t " + resId +"\t("+ title+ ")\n";	
				notifyListeners(notification);
			}
			
			 
			
		} else if (verb.equals(TupleFactory.SEARCHXPATH)){ //a search : distinguish between events "QUERY" and "QUERY_REACHES_PEER"!
			//each tuple : result of search
			String comId = ((Field) tu.get(1)).toString();
			String xpath = ((Field) tu.get(2)).toString();
			String qid = ((Field) tu.get(3)).toString();
			String extent = ((Field) tu.get(4)).toString();
			
			if(extent.equals("0"))
				notifyListeners(mytime +"\t"+networkId+"\t QUERY \t["+qid + "]\t"+" Community:"+ comId +"\tQuery:" + xpath +"\t extent:"+extent+".\n");
			else 
				notifyListeners(mytime +"\t"+networkId+"\t QUERY_REACHES_PEER \t["+qid + "] .\n");

			
		} else if (verb.equals(TupleFactory.REMOVE)) {
				
				String comId = ((Field) tu.get(1)).toString();
				String resId = ((Field) tu.get(2)).toString();
				
				notifyListeners(mytime +"\t"+networkId+"\tREMOVE \t"+comId + "\t"+resId+" .\n");
				
		} else if (verb.equals(TupleFactory.PUBLISH)) {
			
			String comId = ((Field) tu.get(1)).toString();
			String resId = ((Field) tu.get(2)).toString();
			
			notifyListeners(mytime +"\t"+networkId+"\tPUBLISH \t"+comId + "\t"+resId+" .\n");
			
	}		 else if (verb.equals(TupleFactory.NOTIFYCONNECTION)){ //a new connection, or a connectin being closed
		//each tuple : IP, port, connectionType[OUTGOING / INCOMING], opening [true / false]
		String IP = ((Field) tu.get(1)).toString();
		//String port = ((Field) tu.get(2)).toString();
		String ctype = ((Field) tu.get(3)).toString();
		String opening = ((Field) tu.get(4)).toString();
		if(ctype.equalsIgnoreCase("OUTGOING") && opening.equalsIgnoreCase("true"))// notify of opening outgoing connections, and closing incoming connections
		
			notifyListeners(mytime +"\t"+networkId+"\t CONNECT \t"+IP +"\n");
		else if(ctype.equalsIgnoreCase("INCOMING") && opening.equalsIgnoreCase("false")) 
			notifyListeners(mytime +"\t"+networkId+"\tDISCONNECT \t"+IP +"\n");

		
	} 	
		return ansTuple; //in fact we don't need to return anything.
	}

	public void addUDPListener(String IP, int port){
		
		
		//first, try sending a message to the new host, if it works, we add it to the permanent listeners list.
		try {
			byte[] sendData = new String(System.currentTimeMillis()+"\t"+networkId+" ONLINE\t"+myIPPort+"\n").getBytes();
			
				InetAddress inet = InetAddress.getByName(IP); // get host
		      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,inet, port);
		      clientSocket.send(sendPacket); // send UDP packet to host

		      UDPlisteners.add(new IPPort(IP,port)); //if we get here, the networking was successful...
		      
			LOG.info("MonitorWorker: host "+IP+"/"+port +"added to monitor list.");
		} catch (UnknownHostException e) { //if an exception occurs we don't add the host to the listeners set.
			
			LOG.error("MonitorWorker"+e.getLocalizedMessage());
			LOG.error("MonitorWorker: host "+IP+"/"+port +" *not* added to monitor list.");
			
		} catch (IOException e) {
			LOG.error("MonitorWorker"+e.getLocalizedMessage());
			LOG.error("MonitorWorker: host "+IP+"/"+port +" *not* added to monitor list.");
		}

		
	}
	
	/**
	 * shutting down: here we notify the monitoring system that we're going offline.
	 */
	public void shutdownCleanup() { 
	notifyListeners(System.currentTimeMillis() +"\t"+networkId+"\t OFFLINE\n");	
	}

	private void notifyListeners(String message){
		 
	      byte[] sendData = message.getBytes();

		for (IPPort i_p : UDPlisteners){ //for all listeners...
			
			  InetAddress inet;
			try {
				inet = InetAddress.getByName(i_p.IP); // get host
			      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,inet, i_p.port);
			      clientSocket.send(sendPacket); // send UDP packet to host

			} catch (UnknownHostException e) {
				
				LOG.error("MonitorWorker"+e.getLocalizedMessage());
				continue;
			} catch (IOException e) {
				LOG.error("MonitorWorker"+e.getLocalizedMessage());
				continue;
			}
		}
		
	}

}



