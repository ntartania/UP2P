package up2p.rest;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import up2p.core.UserWebAdapter;

public class SearchResource {

	//private String communityId;
	private UserWebAdapter adapter;
	
	public SearchResource(UserWebAdapter adapter) {
		
		this.adapter=adapter;
	}

	@POST //start a search 
	@Consumes(MediaType.TEXT_XML)
	public Response addresource(InputStream is) {
		try{
			String xmlinput;
			try {
				xmlinput= new java.util.Scanner(is).useDelimiter("\\A").next();
			} catch (java.util.NoSuchElementException e) {
				xmlinput= "";
			}

			//TODO: parse the xml and start a search with the right criteria
			
			String query=null;
			String comid = null;
			String[] theterms=xmlinput.split("[><]");
			//<community>abcd</community><query>duh</query>
			for (int i=0;i<theterms.length;i++){
				if (theterms[i].equals("community")){
					comid = theterms[i+1];
				}
				else 
				if (theterms[i].equals("query")){
					query = theterms[i+1];
				}
			}
			if (query!= null){
			
			System.out.println("community="+comid+ "query="+query);
			
			/* ********* searching in UP2P [returns query id] *********** 
			 * note: there are alternative search behaviors, where one can specify the qid, local/global, etc.*/
			String respid = adapter.searchG(comid, query);
			
			respid= "up2p:search/"+ respid;

			return Response.created(URI.create(respid)).build();
			} else
			throw new WebApplicationException(new Exception ("community or query not found in search message"), Response.Status.BAD_REQUEST);	
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Path("{searchid}")
	public SearchSessionResource getSearchSession(@PathParam("searchid") String sid) {
		return new SearchSessionResource(sid, adapter);
	}
	
}
