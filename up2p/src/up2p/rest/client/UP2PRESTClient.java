package up2p.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import up2p.core.WebAdapter;
import up2p.rest.IPPort;
import up2p.xml.TransformerHelper;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

/** a client class to access UP2P's REST interface*/
public class UP2PRESTClient {
	
	public WebResource wr;
	private Client client;
	
	/**
	 * create a client indicating the full URL of the "root" resource in UP2P 
	 * URL format should be: http://[host]:[port]/[webapp_prefix]/rest/up2p 
	 * @param weburl
	 */
	public UP2PRESTClient(String weburl){
	
	this.client = Client.create();
	this.wr = client.resource(weburl);
	
	
	}
	
	public UP2PRESTClient(String host, int port, String prefix){
		this("http://"+host+":"+ String.valueOf(port) + "/"+prefix+"/rest/up2p");
	}
	
	//retrieves configuration info from UP2P
	private void init(){
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		UP2PRESTClient cc= new UP2PRESTClient("http://localhost:8080/up2p/rest/up2p");
		cc.init();
	}
	
	
	///////////////////////////////////////////////////////////////
	//important methods
	/////////////////////////////////////////////////////////////
	
	private String rootCommunityId = "TO BE SET";
	
	/** set the root community id for UP2P
	 * TODO: I should be able to retrieve this directly from UP2P
	 * */
	public void setRootCommunityID(String id){
		rootCommunityId= id;
	}
	
	private String getXMLAnswer(String[] pathParameters){ 
		WebResource target= wr;
		for (String param: pathParameters)
			target = target.path(param);
				
		String xmlRes = target.accept(MediaType.TEXT_XML).get(String.class);
		//TODO: check if ok!
		return xmlRes;//System.out.println(xmlRes);
		
	}
	
	private ClientResponse postXMLDoc(WebResource r, String doc) throws Exception {
		
		ClientResponse response = r.type(MediaType.TEXT_XML)
								   .post(ClientResponse.class, doc);
		checkResponseStatus(response, ClientResponse.Status.CREATED);
		return response;
	}
	
	/** check for ok status*/
	private static void checkResponseStatus(ClientResponse response) throws Exception{
		checkResponseStatus(response, ClientResponse.Status.OK);
	}
	
	/** check for some particular status (eg. "CREATED") */
	private static void checkResponseStatus(ClientResponse response, ClientResponse.Status expected) throws Exception{
		if (!response.getClientResponseStatus().equals(expected))
			throw new Exception(" Error "+response.getClientResponseStatus().getStatusCode()+"  :"+" " + response.getClientResponseStatus().getReasonPhrase());

	}

	/** Invokes the http delete on a specific resource 
	 * @throws Exception */
	private void deleteResource(String [] path) throws Exception{
		
		WebResource target= wr;
		for (String param: path)
			target = target.path(param);
				
		ClientResponse resp=target.delete(ClientResponse.class); //TODO: check status?
		checkResponseStatus(resp,ClientResponse.Status.OK);
	}
	///////////////////////////public methods ////////////////////////////

	/**
	 * "unfriend" a peer: will stop routing queries to that peer, but incoming connection 
	 * attempts from that peer will still be accepted 
	 * @param com the guid of the peer
	 * @throws Exception 
	 */
	public void unFriend(String straciatellaId) throws Exception{
		deleteResource(new String[]{"connections", "hostCache", straciatellaId});
		//TODO: check that connection gets dropped automatically
	}
	
	/**
	 * Add a friend, identified by its gnutellaId, and/or by one or several IP-port combinations.
	 * One of the two parameters may be null (or empty in the case of locations).
	 * The peer will be added to the hostcache, and if the peer is on the blacklist, it is removed from the blacklist.
	 * @param straciatellaId the peer guid, or null if it is not provided
	 * @param locations a list of locations (IP-port) where to find the peer (may be null or empty)
	 * @throws Exception  if the request causes an error
	 * 
	 */
	public URI addFriend(String stracciatellaId, List<IPPort> locations) throws Exception {
		
		StringBuilder doc=new StringBuilder();
		boolean notnull=false;
		doc.append( "<?xml version=\"1.0\"?><up2pConnection>");
		if(stracciatellaId!=null){
			doc.append( "<GUID>");
			doc.append(stracciatellaId);
			doc.append("</GUID>" );
			notnull =true;
		}
		if(locations!=null && locations.size()>0){
			notnull=true;
			for(IPPort ipp:locations){
				doc.append("<IPPort ip=\""+ ipp.IP+"\" port=\"");
				doc.append(ipp.port);
				doc.append("\"/>"); 
			}
		}		
		if(notnull)
			return postXMLDoc(wr.path("connections").path("hostcache"), doc.toString()).getLocation();
		else
			throw new IllegalArgumentException("neither GUID or IPPort was given for method addFriend()");
	
	}
	
	/**
	 * Blacklist a peer identified by its GUID
	 * @param straciatellaId the peer guid
	 * @throws Exception if the request causes an error
	 */
	public URI blacklist(String stracciatellaId) throws Exception{
		
		StringBuilder doc=new StringBuilder();
		doc.append( "<?xml version=\"1.0\"?><up2pConnection>");
		if(stracciatellaId!=null){
			doc.append( "<GUID>");
			doc.append(stracciatellaId);
			doc.append("</GUID>" );
			return postXMLDoc(wr.path("connections").path("blacklist"), doc.toString()).getLocation();
		}
		else
			throw new IllegalArgumentException("missing GUID for method blacklist()");
	
	}
	
	/**
	 * delete a specified resource
	 * note: if the resource refers to a community, its contents will also be deleted
	 * @param com the community id
	 * @param res the resource id
	 * @throws Exception if the request causes an error
	 */
	public void unBlacklist(String straciatellaId) throws Exception{
		deleteResource(new String[]{"connections", "blacklist", straciatellaId});
	}
	
	
	/**
	 * delete a specified resource
	 * note: if the resource refers to a community, its contents will also be deleted
	 * @param com the community id
	 * @param res the resource id
	 * @throws Exception if the request causes an error
	 */
	public void deleteResource(String com, String res) throws Exception{
		deleteResource(new String[]{" community", com, res});
	}
	
	/**
	 *  retrieve a specified resource
	 * @param com community identifier
	 * @param id document identifier
	 * @return
	 */
   public String getResourceXML(String com, String id) {
		
		return getXMLAnswer(new String[]{"community", com, id});
	}
	
   
   /**
	 *  List the contents of a community
	 * @param com community identifier
	 * @return the list of resources in XML format
	 */
  public String getCommunityContents(String com) {
		
		return getXMLAnswer(new String[]{"community" , com});
	}
  
  /**
   * get all the communities
   * @return an XML document (string) listing all the communities
   */
  public String listCommunities(){
	  return getCommunityContents(rootCommunityId);
  }
	
	/**
	 *  retrieves a resource and stores the 
	 * @param com
	 * @param id
	 * @param filename filename to save the XML resource
	 * @param resourceDir (possibly temporary) directory to save the XML resource
	 * @param attachDir (possibly temporary) directory to save the attachments 
	 * @return list of files: first file is XML document, all others are attachments, in the order that they appear in the document
	 * @throws Exception if the webservice responds with a non-OK status
	 */
	public List<File> getResourceWithAttachments(String com, String id, String filename, File resourceDir, File attachDir) throws Exception {
		List<File> flist = new LinkedList<File>();
		String res = getResourceXML(com,id);
		//TODO: what the character encoding?
		InputStream myInputStream = IOUtils.toInputStream(res, "UTF-8");
		AttachmentParser parser = new AttachmentParser();
		List<String> attachments = parser.parse(myInputStream);
		WebResource target= wr;
		if (com!="")
			{target = target.path(com);
			if (id!=""){
				target = target.path(id);
			}
		}
		//write XML resource to file
		File outfile = new File(resourceDir, filename); //create file [aname] in directory [attachDir]
		FileOutputStream fos = new FileOutputStream(outfile);
		IOUtils.copy(myInputStream, fos); //write to file
		fos.close();
		flist.add(outfile);
		//retrieve attachments and write them to file
		//TODO: could optimize this in a multi-threaded way.
		for (String aname:attachments){
			outfile = new File(attachDir, aname); //create file [aname] in directory [attachDir]
			ClientResponse response = target.path(aname).get(ClientResponse.class); //get the file
			checkResponseStatus(response);
			fos = new FileOutputStream(outfile);
			IOUtils.copy(response.getEntityInputStream(), fos);
			fos.close();
			flist.add(outfile);				
		}
		return flist;
	}
	
	/**
	 *  retrieve a single attachment to a specified directory
	 * @param com community
	 * @param id resource id
	 * @param filename name of the attachment
	 * @param attachDir directory to save the file
	 * @return the file
	 * @throws Exception if the webservice responds with a non-OK status
	 */
	public File getAttachment(String com, String id, String filename, File attachDir) throws Exception {
		
		WebResource target= wr;
		if (com!="")
			{target = target.path(com);
			if (id!=""){
				target = target.path(id);
			}
		}
	
		ClientResponse response = target.path(filename).get(ClientResponse.class); //get the file
		checkResponseStatus(response); //check that response is OK
		File outfile = new File(attachDir, filename);
		try{
		FileOutputStream	fos = new FileOutputStream(outfile);
		IOUtils.copy(response.getEntityInputStream(), fos);
		fos.close();
		} catch(FileNotFoundException fnfe){
			//shouldn't happen, as we just created the file
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return outfile;
	}
	
	/**
	 *  retrieve an attachment and get an inputstream to its content
	 * @param com community
	 * @param id resource id
	 * @param filename name of the attachment
	 * @return inputstream to the file content
	 * @throws Exception based on webservice response if the response wasn't "OK"
	 */
	public InputStream getAttachmentAsStream(String com, String id, String filename) throws Exception {
		//TODO: throw exception if the file wasn't found
		WebResource target= wr;
		if (com!="")
			{target = target.path(com);
			if (id!=""){
				target = target.path(id);
			}
		}
	
		ClientResponse response = target.path(filename).get(ClientResponse.class); //get the file
		checkResponseStatus(response);
		return response.getEntityInputStream();
	}
	
		
	/**
	 *  retrieves a resource, stores resource in a filename named [resourceid].xml, along with all attachments, in provided directory
	 * @param com
	 * @param resourceid
	 * @param dir (possibly temporary) directory to save everything 
	 * @return
	 * @throws Exception
	 */
	public List<File> getResourceWithAttachments(String com, String resourceid, File dir) throws Exception {
		return getResourceWithAttachments(com,resourceid,resourceid+".xml", dir, dir);
	}
	
	/**
	 *  Publish an XML document (with no attachments) to a specified community
	 * @param com the community id
	 * @param doc the document (a string)
	 * @return the U-P2P URI to the document: up2p:[communityId]/[resourceid]
	 * @throws Exception if UP2P replies with a non-ok status
	 */
	public URI publishDoc(String com, String doc) throws Exception{
		ClientResponse response = postXMLDoc(wr.path("community").path(com),doc); //TODO: check if we have the word "community" in the REST URL
		checkResponseStatus(response, ClientResponse.Status.CREATED);
		URI uri = response.getLocation();
		return uri;
	}
	
	/**TODO publish a document from an XML file
	 * any attachments are expected to be found in the same directory as the document
	 * @param com
	 * @param resfile
	 * @return
	 */
	public URI publishDoc(String com, File resfile){
		throw new UnsupportedOperationException( "TBD");
	}
	
	/** Publish a document from a DOM tree, without attachments
	 * no attachments are expected
	 *  @param com communityId
	 * @param dom XML Document
	* @return URI for published resource
	 * @throws Exception 
	 */
	public URI publishDoc(String com, Document dom) throws Exception{
		
		return publishDoc(com, DOM2String(dom));
	}
	
	/**
	 * utility method to get the XML as a string, from a given DOM
	 * @param dom
	 * @return
	 */
	private static String DOM2String(Document dom){
		StringWriter sw = new StringWriter();
		try {
			TransformerHelper.encodedTransform(dom.getDocumentElement(),
					WebAdapter.DEFAULT_ENCODING, sw, false);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);

		}
		return sw.toString();
	}
	
	/** publish a document from a DOM tree, with a specified list of attachments
	 * 
	 * @param com communityId
	 * @param dom XML Document
	 * @param attachments list of attachments
	 * @return URI for published resource
	 * @throws Exception 
	 */
	public URI publishDoc(String com, Document dom, List<File> attachments) throws Exception{
		return  publishDoc(com, DOM2String(dom), attachments);
	}
	
	
	/**
	 *  Publish an XML document (with no attachments) to a specified community
	 * @param com the community id
	 * @param doc the document (a string)
	 * @param attachments the list of files to upload as attachments (should be in the same order as references appear in the document)
	 * @return the U-P2P URI to the document: up2p:[communityId]/[resourceid]
	 * @throws Exception if UP2P replies with a non-ok status
	 */
	public URI publishDoc(String com, String doc, List<File> attachments) throws Exception{
		
		ClientResponse response = null;
		WebResource target= wr;

	    //just checking 
	    if (com!="")
			target = target.path("community").path(com);
		else
			throw new Exception( "missing communityId ");

	    if (attachments.size()>0){

	    	// Construct a MultiPart with the different elements
	    MultiPart multiPart = new MultiPart().
	    		//first the XML part
	    bodyPart(new BodyPart(doc, MediaType.TEXT_XML_TYPE)); 
	    //add all attachments
	    for (File attach: attachments)
	    	    multiPart = multiPart.bodyPart(new BodyPart(new FileInputStream(attach), MediaType.APPLICATION_OCTET_STREAM_TYPE));
	    	    
	    	// POST the request		
	    	 response = target.type("multipart/mixed").post(ClientResponse.class, multiPart);
	    } else { //no multipart needed, just send resource as text/xml
	    	response = target.entity(doc, MediaType.TEXT_XML_TYPE).post(ClientResponse.class);
	    }
	    
	    checkResponseStatus(response, ClientResponse.Status.CREATED);
		
		return response.getLocation();
	  }
	
	/**
	 *  post a new search in some particular community
	 * @param com the community
	 * @param query the query string
	 * @return the URI to retrieve search results
	 * @throws Exception if something is wrong with the request (check error message)
	 */
	public URI postSearch(String com, String query) throws Exception{
		String doc = "<?xml version=\"1.0\"?><up2pSearch><community>"+ com+"</community><query>"+ query+"</query><up2pSearch>";
		ClientResponse resp= postXMLDoc(wr.path("search"),doc);
		checkResponseStatus(resp, ClientResponse.Status.CREATED);
		return resp.getLocation();
	}
	
	/** close a search session
	 * search results will be removed 
	 * @throws Exception if the request causes an error
	 * 
	 * */ 
	public void cancelSearch(String queryId) throws Exception{
		deleteResource(new String[]{"search", queryId});	
	}
	
	/** cancel a file transfer 
	 * @throws Exception if the request causes an error
	 * 
	 * */ 
	public void cancelTransfer(String transferId) throws Exception{
		deleteResource(new String[]{"transfer", transferId});	
	}
	
	/**
	 * retrieve the search results for a particular search session
	 * @param sessionId the session identifier
	 * @return the list of search results in an XML document
	 * @throws Exception if something is wrong with the request (check error message)
	 */
	public String getSearchResults(String sessionId) throws Exception{
		
		return this.getXMLAnswer(new String[]{"search", sessionId});
	}
	
	
	
	/* currently, multipart are not supposed to be used in responses..
	 * DO NOT USE THIS METHOD IT WILL NOT RETURN ANYTHING
	 * @param com communityId
	 * @param id resourceId
	 * @param directory directory to save the attachments
	 * @throws Exception 
	 * TODO: this isn't working!
	 * * /
	public List<File> getResourceWithAttachments2(String com, String id, File directory) throws Exception {
		
		WebResource target= wr;
		if (com!="")
			{target = target.path(com);
			if (id!=""){
				target = target.path(id);
			}
		}
				
		ClientResponse response = target.accept(MediaType.valueOf("multipart/mixed")).get(ClientResponse.class); //hack to use multipart/mixed for downloading.
		
		checkResponseStatus(response); //check if OK
		
		InputStream istream= response.getEntityInputStream();
		//TODO: parse this
		
		//I don't know how to parse the multipart!
		
		return new LinkedList<File>();//>xmlRes;//System.out.println(xmlRes);
		
	}*/
	
	
}
