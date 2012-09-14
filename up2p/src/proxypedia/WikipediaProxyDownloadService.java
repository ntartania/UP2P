package proxypedia;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import proxypedia.converter.WikiMediaToCreoleConverter;
import up2p.core.AttachmentNotFoundException;
import up2p.core.LocationEntry;
import up2p.core.WebAdapter;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.servlet.AbstractWebAdapterServlet;
import up2p.servlet.DownloadServlet;
import up2p.util.FileUtil;
import up2p.util.Hash;
import up2p.xml.DOMTransformer;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.AttachmentListFilter;
import up2p.xml.filter.AttachmentReplacer;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.FileAttachmentFilter;
import up2p.xml.filter.SerializeFilter;
import up2p.util.XMLUtil;

import up2p.core.Core2Repository;


import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.apache.xindice.util.XindiceException;
import org.apache.xindice.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.wikimodel.wem.WikiParserException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import up2p.servlet.DownloadService;

public class WikipediaProxyDownloadService implements DownloadService {

	private static final String WikipediaExportPrefixURL = "http://en.wikipedia.org/wiki/Special:Export/";
	private static final String WikipediaHTMLPrefixURL = "http://en.wikipedia.org/wiki/";
	
	private static String url1 = "http://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=";
	private static String url2 = "&srwhat=text&format=xml";

	//the xpath to the set of search results in a wikipedia API response.
	private static String resultSelectorXPath = "/api/query/search/p"; 
	
	//public static String XSLT_WIKIPEDIA = "myxslt.xsl";
    
	private Logger LOG;
	
	//private File xsltfile;
	
	private WebAdapter adapter;
	
	private URLMapper urlmap;
    
    
    /**
     * Constructs a proxy download service.
     *  
     */
    public WikipediaProxyDownloadService(WebAdapter repAdapter) {
    	adapter = repAdapter;
    	//xsltfile = new File(XSLT_WIKIPEDIA);
        LOG = Logger.getLogger(WebAdapter.LOGGER);
        
        urlmap = new URLMapper();
    }
    
      
	/**
     * @see up2p.servlet.DownloadService#doDownloadService(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     * 
     * 
     * Here is the method that receives a request for a file (to download).
     * The requested wikipedia article is first be obtained via an http connection to the "real wikipedia",
     *  then the wikitext is translated to creole syntax and the XML format changed to UP2Pedia. 
     */
    public void doDownloadService(HttpServletRequest request, 
    		HttpServletResponse response,
    		String filterType, String communityId, String resourceId,
    		String attachName, ServletContext context) throws ServletException, IOException {

    	LOG.debug("ProxyDownloadService: getting: rid :"+ resourceId+", attach name:"+ attachName+"==");
    	
    	String url ="";
    	if (attachName == null) { ////downloading a wiki article

    		url = urlmap.getURL(resourceId); //this is a wikipedia article URL (using Special:Export) stored in the URL Map at query time
    		LOG.debug("[Getting XML resource] URL ="+url);
    		
//////////////////////////////////////////////////////
    		// Create an instance of HttpClient.
    		HttpClient client = new HttpClient();

    		// Create a method instance.
    		GetMethod method = new GetMethod(url);

    		// Provide custom retry handler is necessary
    		((HttpMethodBase)method).getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
    				new DefaultHttpMethodRetryHandler(3, false));


    		// Execute the method.
    		int statusCode = client.executeMethod(method);

    		/*if (statusCode != HttpStatus.SC_OK) {
    			response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			return;
    		}*/

    		// Read the response and parse to DOM
    		InputStream incomingstream;
    		try {
    			incomingstream = method.getResponseBodyAsStream();

    			DOMParser parser = new DOMParser();

    			parser.parse(incomingstream);

    			Document retrievedDocument = parser.getDocument();

    			//DOMTransformer.prettyPrint(retrievedDocument);

    		/*	if(retrievedDocument.hasChildNodes()){
    				NodeList nl = retrievedDocument.getChildNodes();
    				for(int i=0;i<nl.getLength();i++){
    					System.out.println(nl.item(0).getNodeName()+"\n");
    				}
    			} else {System.out.println("No child nodes !!!!!!!!!");}
    			*/	
    			/*
    			//System.out.println(retrievedDocument.getChildNodes())
    			//TODO: here is where the received document should be processed to produce the outgoing doc.
    			String resultSelectorXPath = "/mediawiki"; //Note : the xpath selection doesn't work properly because of the attributes in the "mediawiki" element 
    			XPathExpression expr = XMLUtil.makeXPathFromString(resultSelectorXPath);
    			NodeList result = (NodeList) expr.evaluate(retrievedDocument, XPathConstants.NODESET);
    			*/
    			NodeList result = retrievedDocument.getElementsByTagName("text"); // that's the tag containing the wikitext 
    				//XMLUtil.getElementContentFromXPath(retrievedDocument, XMLUtil.makeXPathFromString("//text/"));
    			
    			/* 
    			//other document
    			DOMParser parser2 = new DOMParser();
    			parser2.parse(new FileInputStream(new File("wiki.xml")));
    			Document doc2 = parser2.getDocument(); 
    			NodeList res2 = (NodeList) expr.evaluate(doc2, XPathConstants.NODESET);
    			*/
    			//System.out.println("Wiki Content of the page:\n");
    			
    			//System.out.println("How many results:"+result.getLength());
    			//System.out.println("How many results (file):"+res2.getLength());
    			
    			String finalContent ="";
    			WikiMediaToCreoleConverter wmc = new WikiMediaToCreoleConverter();
    			
    			for(int i=0;i<result.getLength();i++){
    				

    				Node eachelement = result.item(i);
    				
    				String wikitext = wmc.preprocess(eachelement.getTextContent()); //pre-process step to remove links inside image captions (not accepted by parser)
    				
    				//System.out.println("This is the wikitext converted:");
    				//DOMTransformer.prettyPrintNode(eachelement,System.out);
    				//if (eachelement.hasChildNodes())
    				
    				finalContent= wmc.convert(new StringReader(wikitext));
    				LOG.debug("Wikitext : "+finalContent.substring(0,Math.min(400, finalContent.length()))+"[...]");
    			}
    			result = retrievedDocument.getElementsByTagName("timestamp");
    			String timestamp = new String(result.item(0).getTextContent());
    			LOG.debug("Timestamp : "+ timestamp);
    			result = retrievedDocument.getElementsByTagName("title");
    			String pageTitle = new String(result.item(0).getTextContent());
    			LOG.debug("Title : "+ pageTitle);
    			
    			/////////// get attachments from parsed wikitext
    			List<String> atlist = new ArrayList<String>();
    			atlist.addAll(wmc.getImageLinks());
    			
    			//TODO: replace these with proper links as expected in up2p attachments
    						
    			    			
    			///////// build final XML DOM tree
    			Document doc = TransformerHelper.newDocument(); 

               //Creating the XML tree

                //create the root element and add it to the document : Note : follows UP2Pedia format
                Element root = doc.createElement("article");
                doc.appendChild(root);

                //create child element, add an attribute, and add to root
                Element att = doc.createElement("attachments");
                root.appendChild(att);
                //the attachments themselves will be added to the DOM tree once they are confirmed to be available -- below
                                
                // create the<title> node
                Element titleelement = doc.createElement("title");
                root.appendChild(titleelement);
                Text titletext = doc.createTextNode(pageTitle);
                titleelement.appendChild(titletext);

             // create the<parentURI> node
    			Element parentURIelement = doc.createElement("parentUri");
    			root.appendChild(parentURIelement);
    			Text URItext = doc.createTextNode("up2p:"+communityId+"/"+resourceId); //up2p URI syntax
    			parentURIelement.appendChild(URItext);
    			
    			// create the<ancestry> node
    			Element ancestryelement = doc.createElement("ancestry");
    			root.appendChild(ancestryelement);
    			Element ancestorelement = doc.createElement("uri");
    			ancestryelement.appendChild(ancestorelement);
    			
    			Text URItext2 = doc.createTextNode("up2p:"+communityId+"/"+resourceId);
    			ancestorelement.appendChild(URItext2);
    			
                // create the<timestamp> node
                Element timestampelement = doc.createElement("timestamp");
                root.appendChild(timestampelement);
                Text timestamptext = doc.createTextNode(timestamp);
                timestampelement.appendChild(timestamptext);

                //create the content node
                Element content = doc.createElement("content");
                root.appendChild(content);
                //put the wikitext there
                Text text = doc.createTextNode(finalContent);
                content.appendChild(text);
                ////////////////////////////////// xml tree done
    			
                //DOMTransformer.prettyPrint(doc);
    			
                //GET and store locally the images embedded in the page.
                //1 - remove "Special export and obtain URL for HTML wikipedia page
                String urlraw = url.replace("Special:Export/", ""); 
                GetMethod method2 = new GetMethod(urlraw);
        	    
        	    // Provide custom retry handler is necessary
        	    ((HttpMethodBase)method2).getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        	    		new DefaultHttpMethodRetryHandler(3, false));

        	    
        	      // Execute the method.
        	     statusCode = client.executeMethod(method2);
        	     if (statusCode != HttpStatus.SC_OK) {
        	        System.err.println("Method failed: " + method.getStatusLine());
        	      }
        	      //Read the response body.
        	      String responseHTML = method2.getResponseBodyAsString();
        	      
        	      LinkGetter lg = new LinkGetter();
        	      
        	      List<String> actualURLs = lg.getLinks(responseHTML);
        	      
        	      LOG.debug("Image links:");
        	      // match attachments to links in HTML
        	      for (String name: atlist){
        	    	  LOG.debug("finding link for:"+ name);
        	    	  boolean found = false;
        	    	  for(String link: actualURLs) // files in attachment list
        	    	  {
        	    		  if (link.contains(name.replace(' ', '_'))){
        	    			  //System.out.println(name);
        	    			  LOG.debug("found: "+link);

        	    			  urlmap.addAttachmentURL(resourceId, URLEncoder.encode(name,"UTF-8"), link);
        	    			  LOG.debug("Added to AttachmentMap:"+resourceId+"[name]"+URLEncoder.encode(name,"UTF-8")+"[link]"+link);
        	    			  found =true;
        	    			  break;
        	    		  }
        	    		  
        	    	  }
        	    	  if(!found) LOG.debug("(Not found)");
        	    	  else { // the attachment was found, so we include it in the DOM tree

        	    		  //add full up2p attachment syntax to the link
        	    		  String fullsyntax = "attach://"+ adapter.getHost()+":"+ String.valueOf(adapter.getPort())+"/up2p/community/"+communityId+"/"+resourceId+"/"+URLEncoder.encode(name,"UTF-8");
        	    		  // place in DOM
        	    		  Element e = doc.createElement("filename");
        	    		  att.appendChild(e);			//attach to attachmentlist node
        	    		  Text t = doc.createTextNode(fullsyntax);
        	    		  e.appendChild(t);

        	    	  }
        	      }
        	      
        	      response.setCharacterEncoding("UTF-8");
        	      TransformerHelper.encodedTransform(doc, "UTF-8", response.getOutputStream(), true);
        	        //incomingstream = method2.getResponseBodyAsStream();

    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (XindiceException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (SAXException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		catch (WikiParserException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
/////////////////////////////////////////////////////
    		
    	} else { ///// downloading an attachment identified by its name

    		
    		/*
    		 *   get attachment URL, open connection
    		 *   send file directly, writeStreamToStream
    		 */
    		url = urlmap.getAttachmentURL(resourceId,attachName);//URLEncoder.encode(attachName,"UTF-8")); //this is a URL for an attachment (file) store on the wikimedia commons site
    		
    		LOG.debug("Getting file with URL:"+ url);
    		
    		if(url==null){
    			LOG.error("attachment not found in map ! trying to get: "+ resourceId+"/"+attachName);//URLEncoder.encode(attachName,"UTF-8"));
    			//LOG.debug("URLMap contains:"+ urlmap.toString());
    			
    		}
    		//////////////////////////////////////////////////////
    		// Create an instance of HttpClient.
    		HttpClient client = new HttpClient();

    		// Create a method instance.
    		GetMethod method = new GetMethod(url);

    		// Provide custom retry handler is necessary
    		((HttpMethodBase)method).getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
    				new DefaultHttpMethodRetryHandler(3, false));


    		// Execute the method.
    		int statusCode = client.executeMethod(method);

    		if (statusCode != HttpStatus.SC_OK) {
    			response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			return;
    		}

    		// Read the response and parse to DOM
    		InputStream incomingstream = method.getResponseBodyAsStream();
    		
    		FileUtil.writeStreamToStream(incomingstream, response.getOutputStream(),LOG);
    		
    		//the attachment should now be returned to the requesting servlet
    	}
    }
    	
    

   

    /** method to query wikipedia and translate the result as if they were UP2Pedia resources 
     *@param comId should be the communityId of UP2Pedia 
     * @param xpath xpath search expression as generated by UP2P
     * @param qid query identifier
     * @return a list of searchresponses giving documents matching the query
     * 
     */
    public List<SearchResponse> search(String comId, String xpath, String qid) {
    	
    	String querystring;
    	List<SearchResponse> toReturn = new LinkedList<SearchResponse>();
    	boolean isTitle=false; // is this a query on the title ?
    	try {

    		//TODO : build wikipedia URL from xpath query.
    		if (xpath.startsWith("/article[title")){
    			//title query TODO
    			querystring = xpath.substring(99, xpath.indexOf('\'', 100));
    			isTitle = true;
    		}else if (xpath.startsWith("/article[content")){
    			//text query TODO
    			querystring = xpath.substring(101, xpath.indexOf('\'', 102));
    		} else {
    			//we don't understand... just forget about it.
    			return toReturn;
    		}

    		String url = url1+URLEncoder.encode(querystring,"UTF-8")+url2;
    		
    		LOG.info("About to query Wikipedia API with URL:" + url);
    		
   		
    		// Create an instance of HttpClient.
    		HttpClient client = new HttpClient();

    		// Create a method instance.
    		GetMethod method = new GetMethod(url);

    		// Provide custom retry handler is necessary
    		((HttpMethodBase)method).getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
    				new DefaultHttpMethodRetryHandler(3, false));


    		// Execute the method.
    		int statusCode = client.executeMethod(method);

    		if (statusCode != HttpStatus.SC_OK) {
    			System.err.println("Method failed: " + method.getStatusLine());
    		}

    		// Read the response and parse to DOM
    		InputStream incomingstream = method.getResponseBodyAsStream();
    		DOMParser parser = new DOMParser();
    		parser.parse(incomingstream);
    		Document retrievedDocument = parser.getDocument();

    		//DOMTransformer.prettyPrint(retrievedDocument);

    		NodeList result = (NodeList) XMLUtil.makeXPathFromString(resultSelectorXPath).evaluate(retrievedDocument, XPathConstants.NODESET);

    		if (result == null){ //search did not return anything
    			return toReturn;
    		}
    		String title;
    		String timestamp;
    		String snippet;
    		for(int i=0;i<result.getLength();i++){
    			

    			Node eachelement = result.item(i);
    			/*System.out.println("This is my result :"+ i);
   					DOMTransformer.prettyPrintNode(eachelement,System.out);
   					System.out.println("node name:"+ eachelement.getNodeName());
   					System.out.println("node value:"+ eachelement.getNodeValue());
    			 */
    			NamedNodeMap attribs = eachelement.getAttributes();
    			if (attribs == null)
    				break;
    			//gambling that the response format is known
    			title = attribs.getNamedItem("title").getNodeValue();
    			snippet = attribs.getNamedItem("snippet").getNodeValue();
    			//TODO: filter snippet for XML formatting and "querymatch"
    			timestamp = attribs.getNamedItem("timestamp").getNodeValue();

    			////////////// filter : if we're doing a title search, make sure the title *does* match the search
    			if(isTitle && !title.toLowerCase().contains(querystring.toLowerCase())){
    				LOG.info("Query result : title"+ title+" doesn't match "+querystring);
    				continue; // skip this search result
    			}
    			
    			//create the new Document

    			Document doc = TransformerHelper.newDocument(); 

    			////////////////////////
    			//Creating the XML tree

    			//create the root element and add it to the document : Note : follows UP2Pedia format
    			Element root = doc.createElement("article");
    			doc.appendChild(root);

    			//create child element, add an attribute, and add to root
    			Element att = doc.createElement("attachments");
    			root.appendChild(att);

    			// create the<title> node
    			Element titleelement = doc.createElement("title");
    			root.appendChild(titleelement);
    			Text titletext = doc.createTextNode(title);
    			titleelement.appendChild(titletext);

    			// create the<timestamp> node
    			Element timestampelement = doc.createElement("timestamp");
    			root.appendChild(timestampelement);
    			Text timestamptext = doc.createTextNode(timestamp);
    			timestampelement.appendChild(timestamptext);

    			//create the content node
    			Element content = doc.createElement("content");
    			root.appendChild(content);
    			//put the snippet there
    			Text text = doc.createTextNode(snippet);
    			content.appendChild(text);
    			////////////////////////////////// xml tree done, except ancestry attributes added after hash is generated

    		//////generate hash and store in local "database"
    			//get an inputstream on the DOM to pipe to the hash generating methods
    			StringWriter xmlAsWriter = new StringWriter();
    			TransformerHelper.plainTransform(doc, xmlAsWriter);
    			ByteArrayInputStream docstream = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8"));

    			// generate hash
    			String hash = Hash.hexString(Hash.getMD5Digest(docstream));
    			//add ancestry stuff in DOM tree
    			// create the<parentURI> node
    			Element parentURIelement = doc.createElement("parentUri");
    			root.appendChild(parentURIelement);
    			Text URItext = doc.createTextNode(hash);
    			parentURIelement.appendChild(URItext);
    			
    			// create the<ancestry> node
    			Element ancestryelement = doc.createElement("ancestry");
    			root.appendChild(ancestryelement);
    			Element ancestorelement = doc.createElement("uri");
    			ancestryelement.appendChild(ancestorelement);
    			
    			Text URItext2 = doc.createTextNode(hash);
    			ancestorelement.appendChild(URItext2);
    			
    			
    			//map to real wikipedia page in "proxy" DB :
    			urlmap.addURL(hash, makeWikipediaURL(title));
    			
    			//add to results
    			SearchResponse toAdd = new SearchResponse(hash, //resourceId 
    						title,     	//title
    						comId,  // community id : given in the search 
    						title.substring(0, Math.min(title.length(), 10)).replace(" ", "_")+"_"+hash.substring(0,5)+".xml", // filename 
    						new LocationEntry[]{new LocationEntry(adapter.getHost(), adapter.getPort(), "up2p")},  ///download location
    						false); // legacy useless "isVirtual" parameter
    			toAdd.addResourceDOM(doc);
    			
    			toReturn.add(toAdd);
    			LOG.info("WPDownloadService:: One search response added: "+ title);
    		}

    	} catch(IOException e)
    	{
    		//do nothing
    	} catch (XindiceException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (SAXException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (XPathExpressionException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	
 catch (NoSuchAlgorithmException e) {
			// thrown by the Md5 hashing method
			e.printStackTrace();
		}
 
 //return the list of searchresponses
 	return toReturn;
    }
    
    
    private String getResourceURL(String communityId, String resourceId) {
        return "http://" + adapter.getHost() + ":" + adapter.getPort() + "/up2p/community/"
                + communityId + "/" + resourceId;
    }
    
    private String makeWikipediaURL(String title){
    	
			return WikipediaExportPrefixURL+title.replace(' ', '_');
		
    }
}

