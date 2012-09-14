package up2p.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;



//import up2p.core.UserWebAdapter;

import stracciatella.Connection;
import stracciatella.ConnectionData;
import stracciatella.Host;
import up2p.peer.jtella.HostCacheParser;
import up2p.peer.jtella.JTellaAdapter;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.FileAttachmentFilter;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;

/** a class that represents a list of connections, either the hostcache, or the active connections, or the blacklist*/
public class ConnectionListResource {

	enum ConnectionType{ HOSTCACHE, ACTIVE, BLACKLIST};
	
	private ConnectionType ctype;
	
	
	
	public ConnectionListResource(String type) {
		if (type.equalsIgnoreCase("hostcache")){
			ctype= ConnectionType.HOSTCACHE;	
		} else if (type.equalsIgnoreCase("active")){
			ctype= ConnectionType.ACTIVE;	
		} else if(type.equalsIgnoreCase("blacklist")){
			ctype= ConnectionType.BLACKLIST;	
		} else
		 throw new NotFoundException("Unknown connection type:"+ type);
		
	}

	@POST //add a new connection
	@Consumes(MediaType.TEXT_XML)
	public Response addresource(InputStream is) {
		//active connections can't be added.
		if (ctype==ConnectionType.ACTIVE)
			return Response.status(Response.Status.BAD_REQUEST).entity("Cannot force active connections. Add connection to hostcache instead.").type(MediaType.TEXT_PLAIN).build();
		try{
			String myres;
			try {
				myres= new java.util.Scanner(is).useDelimiter("\\A").next();
			} catch (java.util.NoSuchElementException e) {
				myres= "";
			}

			if (ctype==ConnectionType.BLACKLIST){ //TODO: allow specification by current IP/port instead of direct GUID usage
				String guid=null;
				String ipAddress="";
				int port = 0;
				
				String[] theterms=myres.split("[><]"); //split the xml input by tags
				//<IPAddress>134.117.60.64</IPAddress><port>6346</port>
				//OR  <GUID>gsdfg4s5dfg46df54g6df54g6df5</GUID>
				 
				
				for (int i=0;i<theterms.length;i++){

					if (theterms[i].equalsIgnoreCase("GUID")){
						guid = theterms[i+1];
					}  else 	if (theterms[i].equalsIgnoreCase("IPAddress")){
						ipAddress = theterms[i+1];
					} else	if (theterms[i].equalsIgnoreCase("port")){
						port = Integer.parseInt(theterms[i+1]);
					}

				}
				
				if(guid !=null){
				JTellaAdapter.getConnection().blacklistPeer(guid);
				} else{ //we should have IP/port
					guid=JTellaAdapter.getConnection().blacklistPeerFromIPPort(ipAddress, port);
				}
				if (guid==null){ //not found!
					return Response.status(Response.Status.BAD_REQUEST).entity("No existing connection found with IP/port:"+ipAddress+"/"+port).type(MediaType.TEXT_PLAIN).build();
				}
				String respid= "up2p:connection/blacklist/"+guid;//TODO: figure out a shortened GUID 
				URI myuri = URI.create(respid);

				System.out.println("URI of created res= "+ myuri.toASCIIString());
				return Response.created(myuri).build();
			}
				
			if (ctype==ConnectionType.HOSTCACHE)
			{
				String ipAddress=null;
				int port = 0;
				String[] theterms=myres.split("[><]"); //split the xml input by tags
				//<IPAddress>134.117.60.64</IPAddress><port>6346</port>
				for (int i=0;i<theterms.length;i++){

					if (theterms[i].equalsIgnoreCase("IPAddress")){
						ipAddress = theterms[i+1];
					} else
						if (theterms[i].equalsIgnoreCase("port")){
							port = Integer.parseInt(theterms[i+1]);
						} 

				}
				//TODO: add IP/port to connection manager, with some sort of indication that whoever answers is a friend...
				//Host host = new Host(ipAddress, port,0,0); //ignore sharedfilecount/sharedfilesize
				JTellaAdapter.getConnection().addFriend(ipAddress, port);
				String respid= "up2p:connection/hostcache/"+ipAddress+"/"+port;//TODO: figure out a URI for connections 
				URI myuri = URI.create(respid);

				System.out.println("URI of created res= "+ myuri.toASCIIString());

				return Response.created(myuri).build();

			}

		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
		//if we get here we didn't recognize any of the connection types
		return Response.status(Response.Status.BAD_REQUEST).entity("Connection Type not recognized from request:"+ctype).type(MediaType.TEXT_PLAIN).build();
	}

	
	
	
	/////////////////////////////////////////////////////////////////
	//get: get the full collection
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getResource(){
		
		String torespond="<?xml version=\"1.0\"?><up2pConnectionList>"; //TODO: format this

		String reslist = "";
		if (ctype==ConnectionType.ACTIVE){
			//Map<IPPort,UP2PConnection> active = FakeUP2P.getInstance().getActiveConnections();
			
			Map<IPPort,UP2PConnection> active = new HashMap<IPPort,UP2PConnection>();
			
			List<Connection> openConnections = JTellaAdapter.getConnection().getActiveConnections();
			ConnectionData gnutellaSettings = JTellaAdapter.getConnection().getConnectionData();

			
			for(Connection c : openConnections) {
				if(c.getStatus() == Connection.STATUS_OK) {
					IPPort ipp = new IPPort(c.getConnectedServentIP(),c.getConnectedServentPort()); 
					UP2PConnection upc = new UP2PConnection(c.getConnectedServentIP(), c.getConnectedServentPort());
					upc.setGnutellaId(c.getConnectedServentIdAsString());
					active.put(ipp, upc);
				}
			}
			
			StringBuilder sb = new StringBuilder();
			
			for (IPPort ipp: active.keySet()){
				UP2PConnection upc = active.get(ipp);
				String xmlrep = "<ActiveConnection><IP>"+ipp.IP+"</IP><port>"+ipp.port+"</port>"+upc.toXMLString()+"</ActiveConnection>";
				sb.append(xmlrep);
			}			
			//TODO: return an xml list of all active connections
			reslist = sb.toString();
		}
		else if (ctype==ConnectionType.BLACKLIST){
			List<String> blacklist = JTellaAdapter.getConnection().getBlackList();
			
			StringBuilder sb = new StringBuilder();

			for (String guid: blacklist){
				
				String xmlrep = "<GnutellaId>"+guid+"</GnutellaId>";
				sb.append(xmlrep);
			}			
			//TODO: return an xml list of all active connections
			reslist = sb.toString();
		}
		else if (ctype==ConnectionType.HOSTCACHE){
			//get hostcache from JTellaAdapter, then use HostCacheParser to get XML out of it
			reslist = HostCacheParser.toXMLString(JTellaAdapter.getConnection().getHostCache()); 
			 
			reslist= reslist.substring(reslist.indexOf('<',2));// remove main <?xml...?> header
		}
		torespond = torespond+reslist +"</up2pConnectionList>"; 
		return torespond;
	}
	
	//accessing a specific connection (get / delete)
	@Path("{guid}") //TODo: can specify regex format
	public ConnectionResource getDoc(
			@PathParam("guid") String guid) {
		return new ConnectionResource(ctype, guid);
	}

	
/*	@Path("search")
	public SearchResource getSearch() {
		System.out.println("Identified search request in community "+comid);
		return new SearchResource(comid);
	}

	
	//accessing specific resources (get or delete)
	@Path("{docid : [a-f0-9]*}")
	public DocumentResource getDoc(
			@PathParam("docid") String docid) {
		return new DocumentResource(uriInfo, request, comid, docid);
	}
*/

}
