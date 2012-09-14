package up2p.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;


/** represents a UP2P/Gnutella connection
 * 
 * TODO: 
 * - handle multiple possible known ip/port combinations?
 * - document gnutellaId as primary key
 * - handle status (active...)
 * 
 * */
public class UP2PConnection {

	private Set<IPPort> addresses;
	private String gnutellaId; // if known this is like a primary key.

	
	public UP2PConnection(String IP, int port){
		this.addresses = new HashSet<IPPort>();
		addresses.add(new IPPort(IP, port));		
	}
	
	public UP2PConnection(String gnutella){
		this.addresses = new HashSet<IPPort>();
		this.gnutellaId=gnutella;		
	}
	
	public synchronized void addIPPort(IPPort e){
		addresses.add(e);	
	}
	public synchronized void addIPPort(String IP, int port){
		addIPPort(new IPPort(IP,port));
	}
	
	public void setGnutellaId (String id){
		gnutellaId=id;
	}
	
	public String getGnutellaId() {	
		return gnutellaId;
	}
	
	//TODO: theyare deemed equal if they have the same GnutellaId; this maybe a problem
	public boolean equals(Object other){
		if (other instanceof UP2PConnection){
			if (this.gnutellaId.equals(((UP2PConnection)other).gnutellaId))
				return true;
		}
		return false;
	}
	
	//TODO:relying on the gnutella id to make a hashcode.
	public int hashCode(){
		return gnutellaId.hashCode();
	}
	
	/** copy to a new list then return, avoids concurrent modification*/
	public synchronized List<IPPort> getKnownAddresses(){
		List<IPPort> toreturn = new ArrayList<IPPort>();
		toreturn.addAll(addresses);
		return toreturn; 
	}
	
	/**
	 * 
	 * @return an xml representation of the connection and its status 
	 */
	public String toXMLString(){
		StringBuilder sb = new StringBuilder();
		sb.append("<up2pConnection><knownIPPorts>");
		for (IPPort ipp: addresses){
			sb.append("<IPPort><IP>"+ipp.IP+"</IP><port>"+ipp.port+"</port></IPPort>");
		}
		sb.append("</knownIPPorts>");
		sb.append("<GnutellaId>"+gnutellaId+"</GnutellaId></up2pConnection>");
		return sb.toString();
	}

	
	
	
}