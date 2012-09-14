package proxypedia;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

//import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
//import org.apache.xerces.parsers.DOMParser;
import org.apache.xindice.util.XindiceException;
import org.apache.xindice.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import proxypedia.converter.WikiMediaToCreoleConverter;
import up2p.util.XMLUtil;
import up2p.xml.DOMTransformer;
import up2p.xml.TransformerHelper;

public class Proxypedia {

	private static String url1 = "http://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=";
	private static String url2 = "&srwhat=text&format=xml";
	
	public static void main(String[] args) throws Exception{
		  testdownloading();
		  //testSearch();
	  }
	
	public static void testdownloading() throws Exception{
		String url = "http://en.wikipedia.org/wiki/Special:Export/Wales"; //urlmap.getURL(resourceId); //this is a wikipedia article URL (using Special:Export) stored in the URL Map at query time
		
		//////////////////////////////////////////////////////
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		// Create a method instance.
		GetMethod method = new GetMethod(url);

		// Provide custom retry handler is necessary
		((HttpMethodBase)method).getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(3, false));

		System.out.println("starting...");
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

			if(retrievedDocument.hasChildNodes()){
				NodeList nl = retrievedDocument.getChildNodes();
				for(int i=0;i<nl.getLength();i++){
					System.out.println(nl.item(0).getNodeName()+"\n");
				}
			} else {System.out.println("No child nodes !!!!!!!!!");}
				
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
			System.out.println("Wiki Content of the page:\n");
			
			System.out.println("How many results:"+result.getLength());
			//System.out.println("How many results (file):"+res2.getLength());
			
			String finalContent ="";
			WikiMediaToCreoleConverter wmc = new WikiMediaToCreoleConverter();
			
			for(int i=0;i<result.getLength();i++){
				//TODO : if isTitle then filter
				//TODO : build new DOM

				Node eachelement = result.item(i);
				
				String wikitext = eachelement.getTextContent();
				
				///////preprocessing begins here !!! -----------------------------------------------------------------
				
				Pattern linkInALink = Pattern.compile("\\[\\[[^\\]]*(\\[\\[.*\\]\\]).*\\]\\]");// regex matches: [[ (not ])* [[ any* ]] any * ]] the central [[any*]] is grouped.
				Pattern innerlink = Pattern.compile("\\[\\[[^\\[]*?\\]\\]");
				Matcher m = linkInALink.matcher(wikitext);
				System.out.println("*** Links inside links preprocessing ***");
				
				String processed = new String(""); //this will contain the full text after the links are processed
				int mindex = 0; //this index follows the pattern patching
				while(m.find()) {
					System.out.println(m.group()+"\n Inner Links:");
		
					String toreplace = m.group();
					Matcher m2 = innerlink.matcher(toreplace);
					processed = processed + wikitext.substring(mindex, m.start());
					
					int currentindex = 0;
					String replaced = new String("");
					while (m2.find()){
						
						String linktext = m2.group().substring(2, m2.group().length()-2);
						String[] sp = linktext.split("\\|");
						//System.out.println( linktext + "split length:"+ sp.length);
						if (sp.length==2){
							linktext = sp[1];
						}
						replaced = replaced + toreplace.substring(currentindex,m2.start()) + linktext;// + rep.substring(m2.end());
						currentindex = m2.end();
						//System.out.println(replaced);
						/*System.out.println(m2.start()+ " "+ m2.end());
						
					System.out.println(m2.group().substring(2, m2.group().length()-2));
					System.out.println("before:"+rep.substring(0,m2.start())); /*
					
					+ m2.group().substring(2, m2.group().length()-2)
					
					System.out.println("after:"+rep.substring(m2.end()));*/
					}
					replaced = replaced + toreplace.substring(currentindex);
					//System.out.println(replaced);
					processed = processed + replaced;
					mindex = m.end();
				}
				processed = processed + wikitext.substring(mindex);
				
				//find the last occurence of a sequence of links (these finish every wikipedia page and we want to leave them out : category + pages in other languages)
				Pattern linksequence = Pattern.compile("(\\[\\[[^\\[\\]]*\\]\\][\r\n]*)+");
				Matcher fm = linksequence.matcher(wikitext.substring(mindex));
				int tail=0;
				while (fm.find()){
					if (fm.hitEnd())
					{
						tail = fm.group().length();
					}
				}
				//System.out.println("tail of the wikitext:"+ processed.substring(processed.length()-tail) +"\n ================");
				processed = processed.substring(0,processed.length()-tail);
				
			/////preprocessing ends here !!! -------------------------------------------------------------------------------------------------------------
				//System.out.println("This is the wikitext converted:");
				//DOMTransformer.prettyPrintNode(eachelement,System.out);
				//if (eachelement.hasChildNodes())
				finalContent= wmc.convert(new StringReader(processed));
				//System.out.println(finalContent);
			}
			result = retrievedDocument.getElementsByTagName("timestamp");
			String timestamp = new String(result.item(0).getTextContent());
			System.out.println("Timestamp : "+ timestamp);
			result = retrievedDocument.getElementsByTagName("title");
			String pageTitle = new String(result.item(0).getTextContent());
			System.out.println("Title : "+ pageTitle);
			
			/////////// get attachments from parsed wikitext
			List<String> atlist = new ArrayList<String>();
			atlist.addAll(wmc.getImageLinks());
			
			//TODO: replace these with proper links as expected in up2p attachments
						
			////////// remove unsupported formatting
			
			///////// build final XML DOM tree
			Document doc = TransformerHelper.newDocument(); 

           //Creating the XML tree

            //create the root element and add it to the document : Note : follows UP2Pedia format
            Element root = doc.createElement("article");
            doc.appendChild(root);

            //create child element, add an attribute, and add to root
            Element att = doc.createElement("attachments");
            root.appendChild(att);
            for (String a: atlist){
            	Element e = doc.createElement("filename");
                att.appendChild(e);			//attach to attachmentlist node
                Text t = doc.createTextNode(a);
                e.appendChild(t);
            }
            
            // create the<title> node
            Element titleelement = doc.createElement("title");
            root.appendChild(titleelement);
            Text titletext = doc.createTextNode(pageTitle);
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
            Text text = doc.createTextNode(finalContent);
            content.appendChild(text);
            ////////////////////////////////// xml tree done
			
            DOMTransformer.prettyPrint(doc);
			
            //GET and store locally the images embedded in the page.
            String urlraw = "http://en.wikipedia.org/wiki/Apple";
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
    	      
    	      System.out.println("Image links:");
    	      // match attachments to links in HTML
    	      for (String name: atlist){
    	    	  System.out.println("finding link for:"+ name);
    	      for(String link: actualURLs) // files in attachment list
    	      {
    	    	  if (link.contains(name.replace(' ', '_'))){
    	    	  //System.out.println(name);
    	    	  System.out.println("found: "+link);

    	    	  //place link in URL mapper
    	    	  }
    	    	  //Pattern p = Pattern.compile("<img>.*</img>");
    	    	  //String[] imagelinks = 
    	    	  //if (responseHTML.contains(name)){
    	    	//	  String before = responseHTML.substring(0, responseHTML.indexOf(name));
    	    		//  link
    	    	  //}
    	      }
    	      }
    	      
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
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	 public static void testSearch(){	  
		  Scanner in = new Scanner(System.in);

		  System.out.print("Enter query :");
	       // Reads the query and builds the URL
	       String searchQuery = in.nextLine();
	       String url = url1+searchQuery+url2; 
	       
		  List<Document> toReturnDocs = new LinkedList<Document>();
	    // Create an instance of HttpClient.
	    HttpClient client = new HttpClient();

	    // Create a method instance.
	    GetMethod method = new GetMethod(url);
	    
	    // Provide custom retry handler is necessary
	    ((HttpMethodBase)method).getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
	    		new DefaultHttpMethodRetryHandler(3, false));

	    try {
	      // Execute the method.
	      int statusCode = client.executeMethod(method);

	      if (statusCode != HttpStatus.SC_OK) {
	        System.err.println("Method failed: " + method.getStatusLine());
	      }

	      // Read the response body.
	     // byte[] responseBody = method.getResponseBody();
	      
	        InputStream incomingstream = method.getResponseBodyAsStream();
	        DOMParser parser = new DOMParser();
			parser.parse(incomingstream);
			
			Document retrievedDocument = parser.getDocument();
			
			DOMTransformer.prettyPrint(retrievedDocument);
		
			if(retrievedDocument.hasChildNodes()){
				NodeList nl = retrievedDocument.getChildNodes();
				for(int i=0;i<nl.getLength();i++){
					System.out.println(nl.item(0).getNodeName()+"\n");
				}
			} else {System.out.println("No child nodes !!!!!!!!!");}
			
			String resultSelectorXPath = "/api/query/search/p"; //the xpath to the set of search results in a wikipedia API response.
			
			NodeList result = (NodeList) XMLUtil.makeXPathFromString(resultSelectorXPath).evaluate(retrievedDocument, XPathConstants.NODESET);
			
			
			
			if (result != null){
				String title;
				String timestamp;
				String snippet;
				for(int i=0;i<result.getLength();i++){
					//TODO : if isTitle then filter
					//TODO : build new DOM

					Node eachelement = result.item(i);
					/*System.out.println("This is my result :"+ i);
					DOMTransformer.prettyPrintNode(eachelement,System.out);
					System.out.println("node name:"+ eachelement.getNodeName());
					System.out.println("node value:"+ eachelement.getNodeValue());
*/
					NamedNodeMap mm = eachelement.getAttributes();
					/*
					if(mm==null) 
						{
						System.out.println("mm is null");
						}
					else {
						System.out.println(mm.item(0).getNodeName()+ ":::"+ mm.item(0).getNodeValue());
					}
					*/
					title = eachelement.getAttributes().getNamedItem("title").getNodeValue();
					snippet = eachelement.getAttributes().getNamedItem("snippet").getNodeValue();
					//TODO: filter snippet for XML formatting and "querymatch"
					timestamp = eachelement.getAttributes().getNamedItem("timestamp").getNodeValue();
					
					//System.out.println("\ntitle : "+title + "\n timestamp : "+ timestamp + "\n snippet : "+snippet);
					
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
		            ////////////////////////////////// xml tree done
		            
		            //add to results
		            toReturnDocs.add(doc);
				}
			}
	      

	      

	    } catch (HttpException e) {
	      System.err.println("Fatal protocol violation: " + e.getMessage());
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.err.println("Fatal transport error: " + e.getMessage());
	      e.printStackTrace();
	    } catch (XindiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	      // Release the connection.
	      method.releaseConnection();
	    }
		//.output our results
		for (Document doc:toReturnDocs){
			DOMTransformer.prettyPrint(doc);
			System.out.println("------------------------------------------------------------------------");
		}
	  }

}
