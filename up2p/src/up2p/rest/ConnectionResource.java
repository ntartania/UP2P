package up2p.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import up2p.rest.ConnectionListResource.ConnectionType;


import com.sun.jersey.api.NotFoundException;

public class ConnectionResource {

	private ConnectionType context; //blacklist, active, hostcache...
	private String guid;
	
	public ConnectionResource(ConnectionType ctype, String guid) {
		this.context=ctype;
		this.guid = guid; //TODO: identify whether this is actually a guid or an IP/port identifier
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getResource(){
		String fulluri = "up2p:connection/"+guid;
		String torespond= "<?xml version=\"1.0\"?><up2pConnection>"+guid+"</up2pConnection>"; //TODO: add info: IP, port, status, uptime...
		if (torespond==null){
			throw new NotFoundException("No connection with this id in context "+ context);
		}
		return torespond;
	}
	
	@DELETE
	public Response deleteConnection() {
		//TODO: remove connection from specific context (hostcache, etc.)
		return Response.ok().build();
	}
}
