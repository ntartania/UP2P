package up2p.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import up2p.core.DefaultWebAdapter;
import up2p.core.UserWebAdapter;

import com.sun.jersey.api.NotFoundException;

public class AttachmentListResource {

	private String comid;
	private String resid;
	
	public AttachmentListResource(String comid, String docid) {
		this.comid=comid;
		this.resid =docid;
		
	}
	
	//accessing specific resources (get or delete)
	@GET
	@Path("{filename}")
	public Response getAttachment(
			@PathParam("filename") String fname) {
		File attachdir = new File(DefaultWebAdapter.getAttachmentStorageDirectory(comid, resid));
		File file = new File(attachdir,fname); // the file fname within the directory attachdir
		try {
			return Response.ok().entity(new FileInputStream(file)).build(); // return the file
		} catch (FileNotFoundException e) {
			throw new NotFoundException("No such resource,"+e.getLocalizedMessage()); //TODO: change to a server error exception?
		}


	}
	 
	
}
