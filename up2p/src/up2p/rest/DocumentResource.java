package up2p.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import up2p.core.DefaultWebAdapter;
import up2p.core.UserWebAdapter;
import up2p.core.WebAdapter;
import up2p.xml.TransformerHelper;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.MultiPartMediaTypes;

//represents a up2p document, the resource class needed for REST
public class DocumentResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String comid;
	String docid;
	UserWebAdapter adapter;
	
	public DocumentResource(UriInfo uriInfo, Request request, String comid, String rid, UserWebAdapter adapter) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.comid = comid;
		this.docid = rid;
		this.adapter=adapter;
	}
	
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getResource(){
		String fulluri = "up2p:"+comid+"/"+docid;
		
		Node myResponse= adapter.getResourceAsDOM(comid, docid);
		if (myResponse==null){
			throw new NotFoundException("No such resource:"+ comid + " / "+docid);
		}
     
		//transform Document into String
		StringWriter sw = new StringWriter();
		try {
			TransformerHelper.encodedTransform(myResponse,
					WebAdapter.DEFAULT_ENCODING, sw, false);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);

		}
		
		return sw.toString();
	}
	
	@GET
	@Produces("multipart/mixed")
    public Response getDocumentWithAttachments()
    {
        
		String torespond=getResource(); //the other GET above.
		if (torespond==null){
			throw new NotFoundException("No such resource.");
		}
		try{
		
        List<String> attachs = CommunityResource.parseForAttachments(torespond);
		
        MultiPart multiPart = new MultiPart().
                bodyPart(new BodyPart(torespond, MediaType.TEXT_XML_TYPE));
        
        for (String name: attachs){ // add body parts for each attachment. Note: the nth body part matches the nth parsed attachment link in the xml
        	File f = adapter.getAttachmentFile(comid, docid, name);
            multiPart = multiPart.bodyPart(new BodyPart(new FileInputStream(f), MediaType.APPLICATION_OCTET_STREAM_TYPE));
        }
        return Response.ok(multiPart, MultiPartMediaTypes.MULTIPART_MIXED_TYPE).build();
		} catch(Exception e){
			throw new NotFoundException("No such resource,"+e.getLocalizedMessage()); //TODO: change to a server error exception 
		}
        
	}
	
	
	@DELETE
	public Response deleteResource() {
		adapter.remove(comid, docid);
		return Response.ok().build();
	}
	
	@Path("attach")
	public AttachmentListResource getAttachment() {
		System.out.println("Identified attachment request in community /doc "+comid+ "/"+ docid);
		return new AttachmentListResource(comid, docid);
	}

	
	
}
