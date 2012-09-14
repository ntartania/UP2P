package up2p.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.w3c.dom.Document;

import com.sun.jersey.api.NotFoundException;


import up2p.core.LocationEntry;
import up2p.core.UserWebAdapter;
import up2p.core.WebAdapter;
import up2p.search.SearchResponse;
import up2p.xml.TransformerHelper;

public class SearchSessionResource {
	private String searchId;
	private UserWebAdapter adapter;
	
	//constructor for a specific search
	public SearchSessionResource(String sid, UserWebAdapter adapter){
		searchId=sid;		
		this.adapter = adapter;
	}
	
	
	private String DOM2String(Document doc){
		//transform Document into String
				StringWriter sw = new StringWriter();
				try {
					TransformerHelper.encodedTransform(doc,
							WebAdapter.DEFAULT_ENCODING, sw, false);
				} catch (IOException e) {
					e.printStackTrace();
					throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);

				}
				
				return sw.toString();
	}
	
	//get: get the search results for this searchid
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getResource(){
		
		System.out.println("Getting results for sid="+searchId);
		StringBuilder sb= new StringBuilder();
		sb.append("<?xml version=\"1.0\"?><up2pSearch>");
		
		
		List<SearchResponse> resp= adapter.getSearchResults(searchId);
		
		
		//Map<String,String> resmap = FakeUP2P.getInstance().getSearchResults(searchId); //get whatever searchresults are available.
		
		/*if (resmap==null){
			throw new NotFoundException("The search session with that ID doesn't exist or is closed.");

		}*/
			for (SearchResponse sr : resp){
				sb.append("<result><uri>up2p:");
				sb.append(sr.getCommunityId());
				sb.append("/");
				sb.append(sr.getId());
				sb.append("</uri><locations>");
				for (LocationEntry le : sr.getLocations()){
					sb.append("<location>");
					sb.append(le.getLocationString());
					sb.append("</location>");
					//TODO: add trust metrics
				}
				sb.append("</locations>");
				sb.append("<doc>");
				String xmlrep = DOM2String(sr.getResourceDOM());
				sb.append(xmlrep.substring(xmlrep.indexOf('<', 2)));//substring removes the "<?xml version...?> header
				sb.append("</doc></result>");
			}
			sb.append("</up2pSearch>");
		
		return sb.toString();
	}

	@DELETE
	public Response deleteResource() {
		//[TODO: close & delete search session]
		return Response.ok().build();
	}

}
