package up2p.rest;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;

//import presto.FakeUP2P;
import up2p.core.DefaultWebAdapter;
import up2p.core.UserWebAdapter;
import up2p.core.WebAdapter;
import up2p.util.Hash;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.FileAttachmentFilter;

public class CommunityResource {

	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@Context
	ServletContext context;
	
	//the community id
	String comid;
	DefaultWebAdapter adapter; //TODO: I could change this to be a DefWebAdapter, I would need a different accessfilter (initializes with UserWA)

	
	public CommunityResource(UriInfo uriInfo2, Request request2, String co, DefaultWebAdapter adapter) {
		this.request =request2;
		this.comid = co;
		this.uriInfo=uriInfo2;
		this.adapter=adapter;
		
	}

	
	
	private String makeMD5FileName(String input) throws UnsupportedEncodingException{
		String newname ="tempname";
		InputStream istr = new ByteArrayInputStream(input.getBytes("UTF-8")); //TODO: there might be a better way of getting the bytes from the other is above
        try {
			newname = Hash.hexString(Hash.getMD5Digest(istr));
			//LOG.debug("obtained hash filename:"+ newname);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//LOG.error("create servlet:"+ e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return newname;
	}
	
	private boolean writeToFile(String input, File outputFile) throws IOException{
		 BufferedWriter fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
	        fileOut.write(input);
	        fileOut.close();
	        return true;//TODO: catch  errors and return false, do something about it...
	}
	
	@POST //add a new document to this community (no attachments)
	@Consumes(MediaType.TEXT_XML)
	public Response addresource(InputStream is) {
		try{
			String myres;
			try {
				myres= new java.util.Scanner(is).useDelimiter("\\A").next();
			} catch (java.util.NoSuchElementException e) {
				myres= "";
			}

			//Generate name for resource file
			String newname=makeMD5FileName(myres);
            //Store the XML in a file in correct storage dir	
			File resourceFile = new File(DefaultWebAdapter.getStorageDirectory(comid), newname);
			if (!writeToFile(myres,resourceFile)){
				throw new IOException("something went wrong trying to write the XML to a resource file to the community directory");
			}
			
			String respid = adapter.publish(comid, resourceFile, null);  //note: no attachment directory
			
			System.out.println("posted a doc to community "+comid+"; response="+ respid);
			
			URI myuri = URI.create("up2p:"+comid+"/"+respid); //uri of created resource 
			
			System.out.println("URI of created res= "+ myuri.toASCIIString());

			return Response.created(myuri).build();
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	/////////////////////////post a document with attachments, using multipart
	
	@POST
	@Consumes("multipart/mixed")
	public Response addResourceWithAttachments(MultiPart multiPart) {
		
		
		System.out.println("Community resource got multipart POST ---------");
		// First part contains a STRING (XML doc)
		String doc = multiPart.getBodyParts().get(0).getEntityAs(String.class);


		List<String> filenames;
		File tempdirectory = new File("./mytemp/"); //TODO: change the temp directory?
		String respid ="";
		if (!tempdirectory.exists()){
			tempdirectory.mkdir();
		}
		try {
			filenames = parseForAttachments(doc);

			//sanity check: multiPart.getBodyParts().size() should be the number of attachments +1 (+1 for the XML)
			if (filenames.size()!=multiPart.getBodyParts().size()-1){
				System.out.println("CommunityResource:: multiparts = wrong attachment list size: filenames size="+filenames.size()+"multiparts="+multiPart.getBodyParts().size());
			} else
				System.out.println("CommunityResource:: number of attachments="+filenames.size());

			for (int k =1; k<=filenames.size();k++){

				// get the kth part which is the content of the kth attachment (kth filename)
				BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(k).getEntity();

				// boolean isProcessed = true;

				InputStream source = bpe.getInputStream();
				File outfile = new File(tempdirectory,filenames.get(k-1)); //directory + filename TODO: isn't the filename here "file:dudu.rrr"?
				OutputStream out = new FileOutputStream(outfile);
				System.out.println( "POST Document multipart:writing file:"+ outfile.getCanonicalPath());
				IOUtils.copy(source,out);
				out.close();
			}	
		} catch (Exception e) {
			String message = e.getMessage();
			System.out.println(e);
			return Response.status(Response.Status.BAD_REQUEST).entity("Failed to process attachments. Reason : " + message).type(MediaType.TEXT_PLAIN).build();
			//throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
		
		//when we reach this point, all the attachments have been saved in the temp directory.

		try{
			//Generate name for resource file
			String newname=makeMD5FileName(doc);
			//Store the XML in a file in correct storage dir	
			File resourceFile = new File(DefaultWebAdapter.getStorageDirectory(comid), newname);
			if (!writeToFile(doc,resourceFile)){
				throw new IOException("something went wrong trying to write the XML to a resource file to the community directory");
			}

			respid = adapter.publish(comid, resourceFile, tempdirectory);  //note: tempdirectory= attachment directory

		} catch (Exception e) {
			String message = e.getMessage();
			System.out.println(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to publish doc to UP2P. Reason : " + message).type(MediaType.TEXT_PLAIN).build();
			//throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
		//we can now publish the document
		
		
		 
		System.out.println("posted a doc to community "+comid+"; new id="+ respid);		
		URI myuri = URI.create("up2p:"+comid+"/"+respid); //uri of created resource 
		System.out.println("URI of created res= "+ myuri.toASCIIString());

		return Response.created(myuri).build();
		
		//return Response.status(Response.Status.ACCEPTED).entity("Attachements processed successfully.").type(MediaType.TEXT_PLAIN).build();
	}
	
	///////////////// find the attachments in a document. Uses up2p code for parsing the xml.
	public static List<String> parseForAttachments(String doc) throws SAXException, IOException{
		XMLReader reader = TransformerHelper.getXMLReader();
		DefaultResourceFilterChain chain = new DefaultResourceFilterChain();
		FileAttachmentFilter attachListFilter = new FileAttachmentFilter("file:");
		chain.addFilter(attachListFilter);
		chain.doFilter(reader, new InputSource(new StringReader(doc)));
		//List<String> toreturn = new ArrayList<String>();
		
		//toreturn.addAll(attachListFilter.getNameToLinkMap().keySet()); //get all attachment names
		//TODO: what do these links look like? I think it will be file:thing.ext
		return attachListFilter.getAttachmentList();
	}
	
	/////////////////////////////////////////////////////////////////
	//get: get the full collection : you can't get the full collection with the attachments... or else it would be huge? or allow it?
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getResource(){
		
		Document myResponse= (Document) adapter.getCommunityAsDOM(comid, false);

		//transform Document into String
		StringWriter sw = new StringWriter();
		try {
			TransformerHelper.encodedTransform(myResponse.getDocumentElement(),
					WebAdapter.DEFAULT_ENCODING, sw, false);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);

		}
		String torespond= sw.toString();
				
		return torespond;
	}

	//post: publish something to the community -- post a search in this community?
	
	

	
	//accessing specific resources (get or delete)
	@Path("{docid : [a-f0-9]*}")
	public DocumentResource getDoc(
			@PathParam("docid") String docid) {
		return new DocumentResource(uriInfo, request, comid, docid, adapter);
	}
	
}
