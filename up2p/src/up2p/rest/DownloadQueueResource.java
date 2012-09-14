package up2p.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

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
import javax.ws.rs.core.Response;


import up2p.core.UserWebAdapter;

/*** this class represents the UP2P download queue*/
public class DownloadQueueResource {

//private String communityId;
	
	
	private UserWebAdapter adapter;
	
	public DownloadQueueResource(UserWebAdapter adapter) {
		this.adapter=adapter;
	}

	@POST //request a download TODO: define expected XML format
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
			
			String resid=null;
			String comid=null;
			String[] theterms=xmlinput.split("[><]");
			//<community>abcd</community><resource>ddbb</resource>
			for (int i=0;i<theterms.length;i++){
				if (theterms[i].equals("community")){
					comid = theterms[i+1];
				}
				else 
				if (theterms[i].equals("resource")){
					resid = theterms[i+1];
				}
			}
			if (comid!= null && resid !=null){
			
			System.out.println("community="+comid+ "resource="+resid);
			String did= adapter.asyncDownload(comid, resid); //starts a download (will happen asynchronously)
			
			String respid= "up2p:download/"+did; 

			return Response.created(URI.create(respid)).build();
			} else
			throw new WebApplicationException(new Exception ("community or resource not found in download request"), Response.Status.BAD_REQUEST);	
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getResource(){
		
		String torespond="<?xml version=\"1.0\"?><up2pDownloadQueue>";

		List<String> reslist = adapter.listDownloads();
		for (String r: reslist){
			torespond = torespond+"<download>"+r+"</download>"; //TODO: <download id="1234"><com><res><status></download>
		}
		torespond = torespond+"</up2p>";
		return torespond;
	}
	
	@Path("{downloadid}")
	public DownloadResource getDownloadRes(@PathParam("downloadid") String did) {
		return new DownloadResource(did, adapter);
	}
	
}
