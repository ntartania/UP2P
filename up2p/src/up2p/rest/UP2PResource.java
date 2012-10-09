package up2p.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import up2p.core.DefaultWebAdapter;
import up2p.core.UserWebAdapter;
import up2p.util.Config;

import com.sun.jersey.multipart.MultiPart;



@Path("/up2p")
public class UP2PResource {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	@Context
	ServletContext context;
	
	//the community id
	
	DefaultWebAdapter adapter;
	
	public UP2PResource(){
	//note: can get the adapter now as servlet context is null, at least until a first request has been made.
	}
	
	/**
	 * access to UP2P
	 */
	private void getAdapter() {
		if (adapter!= null)
			return;
		if (context==null)
			System.out.println("servlet context is null!");
        Object o = context.getAttribute("adapter");
        if (o != null)
            adapter = ((UserWebAdapter) o).getDefWebAdapter();
        else {
        	System.out.println( "UP2Presource init: servlet not null, adapter null");
        	adapter = null;
        }
    }
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHTMLHello() {
		getAdapter();
		return "<html><title>UP2P REST Interface</title>"
		+ "<body><h1>UP2P REST Interface</h1>" +
				"<h2>Available URIs:</h2><br/>" +
				" /community/[cid], = GET returns community contents (root community is "+adapter.getRootCommunityId()+"), POST new documents there (multipart with attachments) <br/>" +
				"/community/[cid]/[docid] =view a document, request TEXT/XML to leave out the attachments or multipart/mixed for the full shebang <br/>" +
				 "/search/ = POST a query, GET the current results for a queryid (short polling), DELETE a queryid to close search session and results set <br/> "+
				 "/connections/[active OR hostcache OR blacklist]= GET to view connections of a particular type, POST to blacklist or cache a connection <br/>"+ 
				"</body>";
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXMLHello() {
		getAdapter();
		String rootCommunityId = adapter.getRootCommunityId();
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?><up2p>");
		Config config = adapter.getConfig();
		Map<String,String> props =config.listProperties();
		props.put("root.community.id", rootCommunityId);
		for(String pname:props.keySet())
			sb.append("<ConfigProperty name=\""+ pname+"\" value =\""+props.get(pname)+"\" />");
		sb.append("</up2p>");
		return sb.toString();
	}
	
	
	
	@Path("community/{community :[a-f0-9]*}")
	public CommunityResource getCommunity(
			@PathParam("community") String co) {
		getAdapter();	
		return new CommunityResource(uriInfo, request, co, adapter);
	}
	
	
	@Path("connections/{which}") 
	public ConnectionListResource getConnectionList(
			@PathParam("which") String type) {
		//TODO: if "which" matches a specific connection identifier, we can return that connection resource instead of blacklist/etc.
		getAdapter();
	 return new ConnectionListResource(type);
	}
	
	@Path("download/")
	public DownloadQueueResource getDownloads() {
		//TODO: if "which" matches a specific connection identifier, we can return that connection resource instead of blacklist/etc.
		getAdapter();
	 return new DownloadQueueResource(adapter);
	}
	
	@Path("search")
	public SearchResource getSearch() {
		System.out.println("Identified search request" );
		getAdapter();
		return new SearchResource(adapter);
	}
}
