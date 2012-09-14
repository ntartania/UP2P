package up2p.rest;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;


import up2p.core.UserWebAdapter;

import com.sun.jersey.api.NotFoundException;

public class DownloadResource {
	
		private String downloadid;
		private UserWebAdapter adapter;
		
		//constructor for a specific download
		public DownloadResource(String sid, UserWebAdapter adapter){
			downloadid=sid;		
			this.adapter = adapter;
		}
		
		//get: get the status of the download
		@GET
		@Produces(MediaType.TEXT_XML)
		public String getResource(){
			
			System.out.println("Getting status for did="+downloadid);
			StringBuilder sb= new StringBuilder();
			sb.append("<?xml version=\"1.0\"?><up2pDownload id=\"");
			sb.append(downloadid);
			sb.append("\">");
						
			String stat =adapter.getDownloadStatus(downloadid); //get the status
			
			if (stat.equalsIgnoreCase("unknown")){
				throw new NotFoundException("The download with that ID is unknown.");
			}
				sb.append("<status>"+stat+"</status>");
				sb.append("</up2pDownload>");
			
			return sb.toString();
		}
	
		
		@DELETE
		@Consumes(MediaType.TEXT_XML)
		public Response forgetDownload(){
			//conceptually, remove download id from saved downloads with status.
			
			return Response.ok().build();
		}
}
