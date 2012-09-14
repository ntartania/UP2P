package up2p.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lights.Field;
import lights.extensions.XMLField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.TupleSpaceException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import up2p.repository.DatabaseAdapter;
import up2p.repository.Repository;
import up2p.repository.ResourceEntry;
import up2p.repository.ResourceList;
import up2p.servlet.DefaultDownloadService;
import up2p.servlet.DownloadService;
import up2p.servlet.HttpParams;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PWorker;
import up2p.util.Config;
import up2p.util.NetUtil;
import up2p.xml.TransformerHelper;

/**
 * Implementation of a <code>WebAdapter</code> to mask all repository functionalities.
 * 
 * @author Alan Davoust
 * @version 2.0.2
 */
public class Core2Repository implements WebAdapter {
	
    /** Logger used by this adapter. */
    public static Logger LOG;

    /** The configuration used for this adapter. */
    protected Config config;

    /** Collection where all community collections are stored. */
    protected Collection communityCollection;
    
    private String rootCommunityId;
    
    /*
     * Configuration file name. Config file is found in same directory as this
     * class.
     * /
    private static String WEBADAPTER_CONFIG = "WebAdapter.properties";
    */

    /**
     * True if the DownloadService implementation service is configured, false
     * otherwise.
     */
    private boolean downloadServiceConfigured;

    /**
     * Implements the DownloadService used by the DownloadServlet.
     */
    protected DownloadService downloadServiceProvider;
    
    /* Initializer thread. 
    private InitializerThread initThread;*/

    /** Stores the local IP. */
    private String localHost;

    /** Stores the local port used by U-P2P. */
    private int localPort;

    /**
     * File mapper used to map resource IDs to files and attachments on the
     * local file system.
     */
    protected FileMapper mapper;

    /**
     * Adapter for the XML database.
     */
    private DatabaseAdapter dbAdapter;

    /** The local XML repository. */
    private Repository repository;

    /**
     * True if the repository has been started and configured, false otherwise.
     */
    private boolean repositoryConfigured;
    
    /**
     * Path to the root directory of the up2p client as returned by the Servlet
     * container.
     */
    private static String rootPath;
    
    /** The database ID to use with this instance of U-P2P (from config file) */
    private String dbId;
    
    /** The port to attempt a database connection on (localhost is always the assumed host) */
    private int dbPort;
    
    /** The root collection name to use for this instance of U-P2P (from config file) */
    private String rootCollectionName;
    
    /**
     * Identifier that should follow the hostname to access this instance of U-P2P.
     * This should usually be the same as the directory name in which up2p is deployed.
     */
    private String urlPrefix;


    /**
     * here is the tuple space stuff!!! -----------------------------------------------------
     */
	private RepositoryWorker localWorker;

    /**
     *  Worker that listens for requests in the tuple space.
     * @author alan
     *
     */
	private class RepositoryWorker extends UP2PWorker {

		   
		public RepositoryWorker(ITupleSpace ts){
			super(ts);
			
			name= "REPOW"; //a name for this worker
			
			//queryTemplates = field inherited from abstract superclass
			
			//add a tuple = template for SearchXPath queries (3 arguments)
			
			addQueryTemplate(TupleFactory.createSearchTemplate());
			//add a tuple = template for Remove queries (2 arguments)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.REMOVE, 2));

			//add a template for publish, accepting a communityId, a ResourceId, and an XML Node
			addQueryTemplate(TupleFactory.createPublishTemplate());
		
			//add a tuple = template for FileMap queries: 3 arguments : map main resource
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.FILEMAP, 3));

			//add a tuple = template for FileMap queries : 4 arguments = attachments
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.FILEMAP, 4));
			
			//add a tuple = template for Get Local queries : 2 arguments = comId, resId (3 + qid)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.GETLOCAL, 3));
			
			//add a tuple = template for Get Local Community queries : 3 arguments = comId, titleXPath, populate flag (3 + qid)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.GETLOCALCOMM, 4));

			//add a tuple = template for LookupXPath queries (4 arguments) (3 + a qid)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.LOOKUPXPATH, 4));
			
			//add a template for synchronous local search (2 args + qid= 3)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.LOCALSYNCSEARCH, 3));
			
			// Add a template for synchronous local community browse (<comId, qId>)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.BROWSELOCALCOMMUNITY, 2));
			
			// Add a template for synchronous file path lookup (3 args: community Id, resource Id, qId)
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.GETRESOURCEFILEPATHS, 3));

		}
		
		/**
		 * Initiates an update of the sub-network list whenever a community is added or deleted to/from the local
		 * repository. This function outputs a tuple containing the root community ID, which the Core2Network
		 * worker will use to browse the root community and pass the list of subscribed communities
		 * to the JTellaAdapter..
		 */
		public void initiateSubnetUpdate() {
			try {
				this.space.out(TupleFactory.createTuple(TupleFactory.UPDATESUBNETS, new String[]{rootCommunityId}));
				LOG.debug(name + ": Initiated sub-network update.");
			} catch (TupleSpaceException e) {
				LOG.error(name + ": Tuplespace error occured trying to output UpdateSubnets tuple.");
			}
		}
		
		
		/**
		 * method answerQuery : overrides abstract method in superclass. In this case the template is not used. It's enough with the query.
		 */
    	public List<ITuple> answerQuery(ITuple template_not_used, ITuple query){ //template is ignored
			
			List<ITuple> ansTuple= new ArrayList<ITuple>(); //will be created by the factory according to the query
			String verb = ((Field) query.get(0)).toString();
			
			if (verb.equals(TupleFactory.LOOKUPXPATH)){
				//extract the three arguments of the query
				String resId = ((Field) query.get(1)).toString();
				String comId = ((Field) query.get(2)).toString();
				String xpath = ((Field) query.get(3)).toString();
				String qId = ((Field) query.get(4)).toString();

				//call the regular method
				try{
				List<String> ans = lookupXPathLocation(resId,comId,xpath);

				if (ans.isEmpty()){ //we still want to return something so that the synchronous lookup doesn't get stuck
					ansTuple.add(TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, new String[]{resId,comId,xpath, qId, ""}));
				} else
					//create a tuple with the answer(s)
					for (String s:ans) { 
						ansTuple.add(TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, new String[]{resId,comId,xpath, qId, s}));
						LOG.debug("TS:Repository has answer:"+s);
					}				
				
				}catch (ResourceNotFoundException rnf){
					//notify of error

					ansTuple.add(TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, new String[]{resId,comId,xpath, qId, TupleFactory.RESOURCE_NOT_FOUND}));
					LOG.debug("LookupXpath::Repository worker says: resource not found :"+resId);
				}
				catch (CommunityNotFoundException cnf){
					//notify of error
					ansTuple.add(TupleFactory.createTuple(TupleFactory.LOOKUPXPATHANSWER, new String[]{resId,comId,xpath, qId, TupleFactory.COMMUNITY_NOT_FOUND}));
					LOG.debug("LookupXpath::Repository worker says: community not found"+comId);
				}
			
			}
			
			// XPath search query
			else if (verb.equals(TupleFactory.SEARCHXPATH) || verb.equals(TupleFactory.LOCALSYNCSEARCH)){
				
				try {
					// Only handle local sync seaches, and queries whose extent 
					// includes the local repository
    				if(verb.equals(TupleFactory.LOCALSYNCSEARCH)
    						|| Integer.parseInt(((Field) query.get(4)).toString()) == HttpParams.UP2P_SEARCH_ALL 
    						|| Integer.parseInt(((Field) query.get(4)).toString()) == HttpParams.UP2P_SEARCH_LOCAL) {
    					
    					// Note: The reading of the extent field is performed in the conditional because
    					// local sync searches don't have an extent field. This should probably be refactored at some
    					// point
    					
    					String comId = ((Field) query.get(1)).toString();
    					String xpath = ((Field) query.get(2)).toString();
    					String qid =""; 
    					if (verb.equals(TupleFactory.SEARCHXPATH)) //no qid for synchronous local search
    						 qid = ((Field) query.get(3)).toString();

    					LOG.info(name + "Performing a search on the local database. Query: "+ xpath);

    			        List<XMLResource> results;
    			        
    			        try {
    			            // do the search
    			            results = searchRepository(comId, xpath, 10000); //TODO: change this magic number 100
    			            LOG.info("Repository returned " + results.size() + " results.");
    			            
    			            if(!results.isEmpty()) {
    				            String titleLoc = null;
    				            List<String> temp = lookupXPathLocation(comId, rootCommunityId, "/community/titleLocation"); 
    				    		if (!temp.isEmpty()) {  // a hack because now lookup returns multiple answers
    				    			titleLoc = temp.get(0); 
    				    			LOG.info("Got title location: " + titleLoc);
    				        	}

    				            String title;
    				            String location = "localhost" + ":" + getPort() + "/" + getUrlPrefix();
    				            String filename ="";
    				            Document resourceDOM;

    				            for (XMLResource resource : results){ //only the resourceIds of the results
    				            	String resId = resource.getDocumentId();
    				            	
    				            	if(verb.equals(TupleFactory.LOCALSYNCSEARCH)) {
    				            		ansTuple.add(TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCHRESPONSE, new String[] {comId, xpath, resId}));
    				            		continue;
    				            	}
    				            	
    				            	title = getResourceTitle(resource.getContentAsDOM(), titleLoc);
    				            	File f = getLocalFile(comId, resId);
    				            	       	
    				            	// Log error if no valid file is found
    				            	if (f!=null) {
    				            		filename = f.getName();
    				            	} else {
    				            		LOG.error(name + " - AnswerQuery() SEARCHXPATH: WARNING: Filename is null.");
    				            	}
    				            	
    				            	resourceDOM = getResourceAsDOM(resource, titleLoc, true);
    				            	if (resourceDOM==null){ //to do : improve this
    				            		resourceDOM= TransformerHelper.newDocument();
    				            		resourceDOM.appendChild(resourceDOM.createElement("ERROR"));
    				            	}
    				            	
    			            		//output a second tuple containing metadata -- switched the order in an attempt to get complex queries running right 
    			            		ansTuple.add(TupleFactory.createSearchReplyWithDOM(comId, resId, title, filename, location, qid, resourceDOM));
    			            		//this is the response to a search: resourceId, title, URL
    			            		//the search response without metadata was removed : it will be output by the search response manager. 
    			            		//Agents interested in searchresponses will be advised of their existence after the metadata has been stored.
    			            		//ansTuple.add(TupleFactory.createSearchReply(comId, id, title, filename, location, qid));

    			            		ansTuple.add(TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCHRESPONSE, new String[] {comId, xpath, resId}));
    				            }
    			            } else if (results.isEmpty() && verb.equals(TupleFactory.LOCALSYNCSEARCH)){
    			            	ansTuple.add(TupleFactory.createTuple(TupleFactory.LOCALSYNCSEARCHRESPONSE, new String[] {comId, xpath, ""}));
    			            } else {
    			            	ansTuple.add(TupleFactory.createTuple(TupleFactory.SEARCHXPATH_NORESULT, qid));
    			            }
    			            
    			        }
    			        catch (Exception e2){
    			        	LOG.error(name+e2);
    			        	e2.printStackTrace();
    			        }
    				}
    			} catch (NumberFormatException e) {
    				LOG.error(name + ": Search extent could not be determined, discarding.");
    			}
    			
			} else if (verb.equals(TupleFactory.REMOVE)) {
				
				String comId = ((Field) query.get(1)).toString();
				String resId = ((Field) query.get(2)).toString();

				LOG.info("Removing resource from DB: "+ resId);
				try {
				remove(comId,resId);
				} catch(CommunityNotFoundException e){
					this.notifyErrorToUser(e);
				} catch (ResourceNotFoundException e) {
					// TODO Auto-generated catch block
					this.notifyErrorToUser(e);
				}
				
			}
			
			else if (verb.equals(TupleFactory.PUBLISH)){ //-------------------------------------------------------------------------
				
				String comId = ((Field) query.get(1)).toString();
				String resId = ((Field) query.get(2)).toString();
				Object o = ((XMLField) query.get(3)).getValue();

				LOG.info(name+ " will now publish "+ comId+ "/"+ resId);
				Node xmlnode=null;
				if (o instanceof Document) {
					xmlnode = (Node)o;
					try {
						LOG.debug(name+ "  trying..." );
						store(xmlnode, resId, comId);
						LOG.debug(name+ "  done!!..." );
					} catch (Exception e) {
						notifyErrorToUser(e); //outputs an error notification tuple (method in superclass)
					}
				}
				else
					notifyErrorToUser(new IllegalArgumentException("publish: the provided argument is not XML"));
				//this won't happen since the template calls for Element
				
			//nothing to return 	
				
			} else if (verb.equals(TupleFactory.FILEMAP)){
				String comId = ((Field) query.get(1)).toString();
				String resId = ((Field) query.get(2)).toString();
				
				String filepath = ((Field) query.get(3)).toString();
				
				try {
					addResource(comId, resId,filepath);

					if (query.length() == 5) { //length is 5
						String [] attachments = ((Field) query.get(4)).toString().split("\\.\\.\\.");
						for(int i = 0; i < attachments.length - 1; i += 2) {
							try {
								addAttachment(comId, resId, attachments[i], attachments[i+1]);
							} catch (FileNotFoundException e) {
								notifyErrorToUser("A required attachment (" + attachments[i] + ") could not be located. " +
										"Please delete and reupload the resource with the required file attached (Resource: up2p:" 
										+ comId + "/" + resId + ").");
							}
						}
					}

				} catch (FileNotFoundException e) {
					LOG.error("Error mapping resource: "+ e);
					notifyErrorToUser(e);
				} catch (DuplicateResourceException e) {
					// TODO Auto-generated catch block
					LOG.error("Resource "+ e.getResourceId() + "already mapped to a different file :" + e.getDuplicateFile().getName());
					notifyErrorToUser(e);
				} catch (CommunityNotFoundException e) {
					LOG.error("CommunityNotFound"+ e);
					notifyErrorToUser(e);
				} catch (ResourceNotFoundException e) {
					LOG.error("ResourceNotFound"+ e);
					notifyErrorToUser(e);
				}

			}
			 else if (verb.equals(TupleFactory.GETLOCAL)){

				String comId = ((Field) query.get(1)).toString();
				String resId = ((Field) query.get(2)).toString();

				
				try {
					
					Document doc = getResourceAsDOM(comId,resId, false); //false: no trimming to size
					ITuple response = TupleFactory.createTuple(new String []{TupleFactory.GETLOCALRESPONSE, comId, resId});
					//if (doc instanceof Document) {//should be the case
					if(doc==null){
						doc = TransformerHelper.newDocument();
						//doc.appendChild(doc.createElement("ERROR"));
					} 
					response.add(new XMLField((Document)doc));
					ansTuple.add(response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					notifyErrorToUser(e);
				} 
				
				//}
				//else {
				//notifyErrorToUser(new Exception("Internal DB error: retrieved resource from DB was not a DOM Document"));
				//LOG.debug("type of non-Document node:" + doc.getNodeType() + " class: "+ doc.getClass()+"tostring: "+ doc.toString());
				//}
			} else if (verb.equals(TupleFactory.GETLOCALCOMM)){

				String comId = ((Field) query.get(1)).toString();
				String titleXPath = ((Field) query.get(2)).toString();
				boolean populate = ((Field) query.get(3)).toString().equals(Boolean.toString(true));
				String qId = ((Field) query.get(4)).toString();

				
				try {
					Document doc = getCommunityAsDOM(comId, titleXPath, populate);
					ITuple response = TupleFactory.createTuple(new String []{TupleFactory.GETLOCALCOMMRESPONSE, comId, titleXPath, Boolean.toString(populate), qId});
					
					//if (doc instanceof Document) {//should be the case
					if(doc==null){
						doc = TransformerHelper.newDocument();
						//doc.appendChild(doc.createElement("ERROR"));
					} 
					
					response.add(new XMLField((Document)doc));
					ansTuple.add(response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					notifyErrorToUser(e);
				}
			}
			else if (verb.equals(TupleFactory.BROWSELOCALCOMMUNITY)){
				
				String comId = ((Field) query.get(1)).toString();
				String qid = ((Field) query.get(2)).toString();

				LOG.info(name + ": Performing a browse on the community: " + comId);

		        List<String> results;
		        
		        try {
		            // do the search
		        	String resultString = "";
		        	try {
		        		results = repository.browseCommunity(comId);
		        		LOG.info("Repository community browse returned " + results.size() + " results.");
			            for(String id : results) {
			            	resultString += "/" + id;
			            }
			            
			            if(!resultString.equals("")) {
			            	resultString = resultString.substring(1);
			            }
		        	} catch (CommunityNotFoundException e) {
			        	resultString = TupleFactory.COMMUNITY_NOT_FOUND;
			        }
		            
		            ansTuple.add(TupleFactory.createTuple(TupleFactory.BROWSELOCALCOMMUNITYANSWER, 
		            		new String[] {comId, qid, resultString}));
		        }
		        catch (Exception e){
		        	LOG.error(name + ": "+e);
		        	e.printStackTrace();
		        }
		        
			} else if (verb.equals(TupleFactory.GETRESOURCEFILEPATHS)){
				//extract the three arguments of the query
				String comId = ((Field) query.get(1)).toString();
				String resId = ((Field) query.get(2)).toString();
				String qId = ((Field) query.get(3)).toString();

				//call the regular method
				try{
					String resFilePath = getResourceFilepath(comId, resId);
					String attachString = getAttachmentsFilepath(comId, resId);
					ansTuple.add(TupleFactory.createTuple(TupleFactory.GETRESOURCEFILEPATHSRES, new String[]{resFilePath, attachString, qId}));

				} catch (ResourceNotFoundException rnf){
					// Notify of error
					LOG.error(name + ": GetResourceFilePath could not find resource.");
					ansTuple.add(TupleFactory.createTuple(TupleFactory.GETRESOURCEFILEPATHSRES, new String[]{TupleFactory.RESOURCE_NOT_FOUND, 
							TupleFactory.RESOURCE_NOT_FOUND, qId}));
				}
			
			}
			return ansTuple;
			
		}
    	
    	/**
    	 * @see UP2PWorker.shutdownCleanup()
    	 */
    	public void shutdownCleanup() {
    		Core2Repository.this.shutdown();
    	}
		    	
    } //end of class RepositoryWorker
	
	
	/**
     * Constructs an adapter.
     * 
     * @param up2pPath path to the root directory of the client deployment in
     * the Servlet container (e.g. ../webapps/up2p)
     */
     public Core2Repository(String path, String urlPrefix, Config config) {
    	 
		//TODO: remove the stuff below from DEfaultWebAdapter
		// New logger
		LOG = Logger.getLogger(WebAdapter.LOGGER);
		rootPath = path; //get path from DefaultWebAdapter (constructor arg)
		setUrlPrefix(urlPrefix);
		
		this.config = config;
		// Get info used in XML database configuration
		dbId = config.getProperty("up2p.database.id", "xindice");
		try {
			dbPort = Integer.parseInt(config.getProperty("up2p.database.port", "8080"));
		} catch (NumberFormatException e) {
			LOG.error("Could not read database port, attempting DB connection on default port (8080).");
			dbPort = 8080;
		}
		rootCollectionName = config.getProperty("up2p.database.rootName", "up2p");
		 
		initXMLDatabase();
		// Create repository
		initRepository();
		
		// Get community collection (also for queries)
		try {
			String comurl = dbAdapter.getCommunityUrl();
			LOG.info("ResourceManager communityURL:"+ comurl);
			communityCollection = DatabaseManager.getCollection(comurl, "admin", null);
		} catch (XMLDBException e) {
			LOG.error("ResourceManager Error getting community collection.", e);
		}
		
		/*
		 * Initialize the FileMapper. Must be initialized after the repository
		 * because the FileMap resides in the repository XML database.
		 * 
		 * @see up2p.core.FileMapper#loadCommunityMaps
		 */
		mapper = new FileMapper(getDbAdapter());
		
		   
		// log initial message
		LOG.info(new java.util.Date().toString()
				+ "Adapter to repository initialized.");
    }

     /**
      * Fetches the DOM tree of a resource (directly from the database)
      * @param comId
      * @param resId
      * @param trimtoSize if the resource needs to be limited to 65kbytes (for search response message)
      * @return
      * @throws SAXException
      * @throws IOException
      */
     private Document getResourceAsDOM(String comId, String resId, boolean trimToSize) 
     		throws CommunityNotFoundException, ResourceNotFoundException, XMLDBException {
    	 
    	 Document document = null;
    	 
    	// Get the resource from the database
		try {
			XMLResource resource = repository.getResource(comId, resId);
			return getResourceAsDOM(resource, getResourceTitle(resId, comId), trimToSize);
		} catch (CommunityNotFoundException e) {
			LOG.error("Core2Respository.getResourceAsDOM(): Failed to find community.");
			LOG.error(e.getMessage());
			throw e;
		} catch (ResourceNotFoundException e) {
			LOG.error("Core2Respository.getResourceAsDOM(): Failed to find requested resource.");
			LOG.error(e.getMessage());
			throw e;
		} catch (XMLDBException e) {
			LOG.error("Core2Respository.getResourceAsDOM(): XMLDB Error fetching community / resource.");
			LOG.error(e.getMessage());
			throw e;
		}
	}
     

     /**
      * Fetches the title of a resource by performing an XPath match on a local XMLResource
      * object (no database access)
      * @param resource	The resource to fetch the title for
      * @param titleLoc	The XPath to the resource title
      * @return	The title of the resource
      */
     private String getResourceTitle(Node resource, String titleLoc) {
    	 Object title = null;
    	 try {
				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression xPathExpression = xPath.compile(titleLoc);
				title = xPathExpression.evaluate(resource,  XPathConstants.STRING);
			} catch (XPathExpressionException e) {
				title = "[No Element at Specified Title XPath]";
			}
		return (String)title;
     }
     
     
     /**
      * Gets the DOM tree of a resource from an XMLResource object (no database access)
      * @param XMLResource	The resource to build a DOM for
      * @param titleLoc	The XPath to the title of the resource
      * @param trimtoSize if the resource needs to be limited to 65kbytes (for search response message)
      * @return A document node containing the DOM of the passed XMLResource
      */
     private Document getResourceAsDOM(XMLResource resource, String titleLoc, boolean trimtoSize) 
     		throws XMLDBException {
    	 Document document = null;
    	 String resourceString = resource.getContent().toString();
	
		// TODO: This probably doesn't really represent the byte size of the XML resource,
		//             figure out a better way to do this.
		if(resourceString.length() < 63000 || !trimtoSize) {
			// Case 1: XML resource is already small enough or trimming is disabled, get entire resource DOM
		
			// Convert the document into a Document object compatible with the rest of the system
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = null;
			try {
				db = dbf.newDocumentBuilder();
			}  catch (ParserConfigurationException e) {}
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(resourceString));
			try {
				document = db.parse(is);
			} catch (Exception e) {}
		} else {
			// Case 2: Resource needs to be trimmed, just return the title for now
			document = TransformerHelper.newDocument();

			Element newel = document.createElement("Title");
			newel.setNodeValue(getResourceTitle(resource.getContentAsDOM(), titleLoc));
			
			document.appendChild(newel);
			return document;
		}
		
		// Return the fetched resource
		return document;
	}
     
     /**
 	 * Construct a DOM tree representing the community contents.
 	 * 
 	 * @param communityId	The community to construct a DOM for
 	 * @param populate If true, also return the DOM for each resource in the community
 	 * @return a DOM containing a root node called "community" with its "id" and "title" as attributes,
 	 * and a child for each resource, called "resource", with its "id" and "title" as attributes. If requested each
 	 * resource element will have a single child which is the root element of the resource DOM. 
 	 */
    private Document getCommunityAsDOM(String comId, String titleXPath, boolean populate) throws XMLDBException {
 		try {
 		    // Generate the document to return
 		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		    dbf.setNamespaceAware(true); 
 		    
			DocumentBuilder db = null;
			try {
				db = dbf.newDocumentBuilder();
			}  catch (ParserConfigurationException e) {}
			
			Document returnDoc = db.newDocument();
			Element rootNode = returnDoc.createElement("community");
			returnDoc.appendChild(rootNode);
			
			// Get the list of resources from the database (either entire DOM or titles based on populate flag)
 			Collection col =
 				DatabaseManager.getCollection(dbAdapter.getCommunityUrl() + "/" + comId);
 			 XPathQueryService service =
 	 		      (XPathQueryService) col.getService("XPathQueryService", "1.0");
 			 
 			ResourceSet resultSet = null;
 			if (populate) {
 				resultSet = service.query("/*"); // Get DOM of all resources in the community
 			} else {
 				resultSet = service.query(titleXPath); // Get just the resource titles
 			}
 		   
			// For each resource in the community
 		    ResourceIterator results = resultSet.getIterator();
 		    while (results.hasMoreResources()) {
				Resource res = results.nextResource();
				  
				// Create a new "resource" element
				Element resourceEle = returnDoc.createElement("resource");
				 
				// Parse the resource into a standard Document object
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader((String) res.getContent()));
				Document resDoc = null;
				
				try {
					resDoc = db.parse(is);
				} catch (Exception e) {
					LOG.error("getCommunityAsDOM(): Parsing error.");
					e.printStackTrace();
				}
				
				/*
				// DEBUG PRINTING
				try {
	 				TransformerFactory tf = TransformerFactory.newInstance();
	 				Transformer trans = tf.newTransformer();
	 				StringWriter sw = new StringWriter();
	 				trans.transform(new DOMSource(resDoc), new StreamResult(sw));
	 				System.out.println("\n\n=== RES DOC START ===\n" + sw.toString() + "\n=== RES DOC END ===\n\n");
	 			} catch (Exception e) {}
				*/
				
				if(populate) {
					// Resource DOM was requested
					
					// Add the resource ID of the resource to the resource element
					resourceEle.setAttribute("id", resDoc.getDocumentElement().getAttribute("src:key"));
					
					// Get the title of the resource from the DOM, and add it to the resource document object
					XPath xpath = XPathFactory.newInstance().newXPath();
					try {
						
					    XPathExpression expr = xpath.compile(titleXPath);
					    Object result = expr.evaluate(resDoc, XPathConstants.NODE);
					    Node titleResult = (Node) result;
					    
					    if (titleResult != null && titleResult.hasChildNodes() && titleResult.getFirstChild().getNodeType() == Node.TEXT_NODE) {
					    	resourceEle.setAttribute("title", titleResult.getFirstChild().getNodeValue());
					    } else if (titleResult != null) {
					    	resourceEle.setAttribute("title", titleResult.getNodeValue());
					    }
					} catch (XPathExpressionException e) {
						LOG.error("getCommunityAsDOM(): XPath error.");
						e.printStackTrace();
					}
					
					// Add the resource DOM to the resource element
					// KLUDGE: XINDICE SPECIFIC
					//				 - Xindice metadata must be stripped from returned resource DOM
					Element resourceDom = (Element) returnDoc.importNode(resDoc.getDocumentElement(), true);
					resourceDom.removeAttribute("xmlns:src");
					resourceDom.removeAttribute("src:col");
					resourceDom.removeAttribute("src:key");
					resourceEle.appendChild(resourceDom);
					
				} else {
					// Add the resource ID of the resource to the resource element
					if(resDoc.getDocumentElement().hasAttribute("xq:key")) {
						resourceEle.setAttribute("id", resDoc.getDocumentElement().getAttribute("xq:key"));
					} else {
						resourceEle.setAttribute("id", resDoc.getDocumentElement().getAttribute("src:key"));
					}
					
					// Resource DOM was not requested, just add title attribute
		 			String attrName = titleXPath.substring(titleXPath.lastIndexOf("/") + 1);
		 			if(attrName.startsWith("@")) {
		 				attrName = attrName.substring(1);
		 			}

		 			if(resDoc.getDocumentElement().hasChildNodes() 
		 					&& resDoc.getDocumentElement().getFirstChild().getNodeType() == Node.TEXT_NODE) {
		 				resourceEle.setAttribute("title", resDoc.getDocumentElement().getFirstChild().getNodeValue());
		 			} else {
						Node attrNode = resDoc.getDocumentElement().getAttributes().getNamedItem(attrName);
						if(attrNode != null) {
							resourceEle.setAttribute("title", attrNode.getNodeValue());
						}
		 			}
				}
				
				// Add the resource element to the root element
				rootNode.appendChild(resourceEle);
 		    }
 		    
 		    /*
 		    // DEBUG PRINTING
 			try {
 				TransformerFactory tf = TransformerFactory.newInstance();
 				Transformer trans = tf.newTransformer();
 				StringWriter sw = new StringWriter();
 				trans.transform(new DOMSource(returnDoc), new StreamResult(sw));
 				System.out.println("\n\n=== COMM DOM START ===\n" + sw.toString() + "\n=== COMM DOM END ===\n\n");
 			} catch (Exception e) {}
 			*/
 			
 			// Return the fetched resource
 			return returnDoc;
 			
 		} catch (XMLDBException e) {
 			LOG.error("Core2Respository.getCommunityAsDOM(): Failed to find community.");
 			LOG.error(e.getMessage());
 			throw e;
 		}
     }
     

	/**
      * just a proxy for a search coming from the Tuple space and going to the repository
      * 
      * */
    private List<XMLResource> searchRepository(String comId, String xpath, int i) {
    	/*
    	 * we make a difference between proper xpath searches to be applied to the full collection and a search to know if the Resource [rid]
    	 * is found in the collection, in which case we return a list containing a single string: the resource id
    	 */
    	//try{
    	
    	List<XMLResource> toReturn = new ArrayList<XMLResource>();
    	
    		if (xpath.startsWith(Repository.RESOLVE_URI)){
    			
    			String rid = xpath.substring(Repository.RESOLVE_URI.length());
    			LOG.debug("Core2Repository: URI resolution query identified! resID="+rid);
    			
    			if(repository.communityHasResource(comId,rid)){
    				XMLResource resource = null;
    				try {
    					resource = repository.getResource(comId, rid);
    				} catch (Exception e) { return toReturn; };
    				
    				toReturn.add(resource);
    				return toReturn;
    				
    			} else
    				return toReturn;
    		}
    		else {
    			try {
					return repository.search(comId, xpath, i);
				} catch (CommunityNotFoundException e) {
					LOG.error("searchRepository: Community not found "+e.getMessage());
				}	
				return toReturn;
    		}
		
    	/*} catch (CommunityNotFoundException e) {
    		return new ArrayList<String>();
    	}*/
	}

    /*
     * @see up2p.core.WebAdapter#getDbAdapter()
     */
    private DatabaseAdapter getDbAdapter() {
        return dbAdapter;
    }

    /**
     * returns the IP on which this client is running
     */
    public String getHost() {
        return localHost;
    }

    /**
     * Gets the port on which the up2p client is listening
     */
    public int getPort() {
        return localPort;
    }
    
    /**
     * @see up2p.core.WebAdapter#getUrlPrefix()
     */
    public String getUrlPrefix() {
    	return urlPrefix;
    }
    
    /**
     * @see up2p.core.WebAdapter#getRootPath()
     */
    public static String getRootPath() {
    	return rootPath;
    }

    /**
     * Translates from a relative path to a real path using the root directory
     * of the up2p application as a base.
     * 
     * @param filePath path of a file relative to the webserver context
     * @return the full path to the file
     */
    private String getRealFile(String filePath) {
        return rootPath + File.separator + filePath;
    }

    /*
     * @see up2p.core.WebAdapter#getRepository()
     */
    private Repository getRepository() {
        if (repository == null || !repositoryConfigured)
            initRepository();
        return repository;
    }

    /*
     * @see up2p.core.WebAdapter#getResourceManager()
     * /
    public ResourceManager getResourceManager() {
        return resourceManager;
    }*/

    /*
     * @see up2p.core.WebAdapter#getResourceURL(String, String)
     */
    private String getResourceURL(String communityId, String resourceId) {
        return "http://" + getHost() + ":" + getPort() + "/" + getUrlPrefix() + "/community/"
                + communityId + "/" + resourceId;
    }
    
    /*
     * @see up2p.core.WebAdapter#getDownloadService()
     */
    public DownloadService getDownloadService() {
        if (downloadServiceProvider == null || !downloadServiceConfigured)
            initDownloadService();
        return downloadServiceProvider;
    }
    
    /**
     * Initializes the DownloadService implementation using the settings in the
     * configuration properties file.
     * 
     * update: just starts a defaultDownloadService
     */
    private void initDownloadService() {
    	
    	downloadServiceProvider = new DefaultDownloadService(this);
    	
        
    }
    
    /**
     * Gets a local file from its community/resource mapping via the fileMapper
     * 
     * @param communityId
     * @param resourceId
     * @return local file
     */
    public File getLocalFile(String communityId, String resourceId){
    	File toreturn = null;
    	try{
    		toreturn =  mapper.getResourceMapping(communityId, resourceId);
    		
    		if (!toreturn.isAbsolute()){
    			LOG.debug("Core2rep::getlocalFile:" + toreturn.getPath());
    			return DefaultWebAdapter.getResourceFile(communityId, "file:" + toreturn.getPath());
    		}
    		else 
    			return toreturn;
    	}
    	catch (Exception e)
    	{return null;}
    }
    
    /**
     * Returns the absolute file path of a resource.
     * @param communityId	The community ID of the specified resource
     * @param resourceId	The resource ID of the specified resource
     * @return The absolute file path of the resource (as a string)
     * @throws ResourceNotFoundException 
     */
    public String getResourceFilepath(String communityId, String resourceId) 
    	throws ResourceNotFoundException {
    	File resFile = getLocalFile(communityId, resourceId);
    	if(resFile == null) { throw new ResourceNotFoundException(communityId + "/" + resourceId); }
    	return resFile.getAbsolutePath();
    }
    
    /**
     * Returns a string of the absolute filepaths of all attachments for a specified resource.
     * @param communityId	The community Id of the resource
     * @param resourceId	The resource Id of the resource
     * @return	A string of all absolute filepaths separated by ":::", or "" if no
     * 			attachments exist.
     * @throws ResourceNotFoundException 
     */
    public String getAttachmentsFilepath(String communityId, String resourceId) throws ResourceNotFoundException {
    	String returnPaths = "";
	    	try {
	    	Iterator<String> attachmentNames = mapper.getAttachmentNames(communityId, resourceId);
	    	while(attachmentNames.hasNext()) {
	    		returnPaths += "..." + getLocalFileAttachment(communityId, resourceId, 
	    				attachmentNames.next()).getAbsolutePath();
	    	}
	    	
	    	if(returnPaths.length() > 0) {
	    		return returnPaths.substring(3);
	    	}
	    	
	    	return returnPaths;
	    } catch (Exception e) {
	    	throw new ResourceNotFoundException(communityId + "/" + resourceId);
	    }
    	
    }
    
    /** Gets an attachment for a local file from its community/resource mapping via the fileMapper
     * 
     * @param communityId
     * @param resourceId
     * @param attachname : the attachment name
     * @return local file containing the attachment
     */
	public File getLocalFileAttachment(String communityId, String resourceId,
			String attachName)throws AttachmentNotFoundException{
		try {
			return DefaultWebAdapter.getAttachmentFile(communityId, resourceId, "file:"+attachName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return null;
		//return mapper.getAttachmentMapping(communityId, resourceId, attachName);
		
	}
    
    
    /**
     * Initializes the XML database. The XML database is always started because
     * its used by the ResourceManager, FileMapper etc. and possibly by the
     * Repository implementation.
     *  
     */
    private void initXMLDatabase() {
        LOG.info("Initializing database with id: \"" + dbId + "\" and root collection name: \""
        		+ rootCollectionName + "\"");

        // Start the XML database
        dbAdapter = new DatabaseAdapter(dbId, dbPort, rootCollectionName, getRootPath(), Repository.LOGGER);
    }

    /**
     * Initialize the Repository implementation.
     */
    private void initRepository() {
        // reset configuration flag
        repositoryConfigured = false;

        // get class loader - implementing class must be in classpath
        ClassLoader loader = DefaultWebAdapter.class.getClassLoader();
        // get implementing class name
        String repProviderClassName = config
                .getProperty(WebAdapter.CONFIG_REPOSITORY_PROVIDER);
        // log an error if retrieved class name is invalid
        if (repProviderClassName == null) {
            LOG.info("No provider class for the repository was found in "
                    + "the configuration properties file. Using default "
                    + "value of " + Repository.DEFAULT_REPOSITORY_PROVIDER
                    + ".");
            repProviderClassName = Repository.DEFAULT_REPOSITORY_PROVIDER;
        }

        try {
            // load the class
            Class<?> repProvider = loader.loadClass(repProviderClassName);
            Object repositoryObj = repProvider.newInstance();
            // check if created object is the right type
            if (repositoryObj instanceof Repository) {
                repository = (Repository) repositoryObj;
                repository.configureRepository(this, dbAdapter);
                // success
                LOG.info("Loaded Repository provider " + repProviderClassName
                        + ".");
                repositoryConfigured = true;
            } else {
                LOG.error("Error initializing Repository. Instantiated"
                        + "class does not implement Repository interface.");
            }
        } catch (ClassNotFoundException e) {
            LOG.error("Error initializing Repository. Implementation must"
                    + " be in the classpath.", e);
        } catch (InstantiationException e) {
            LOG.error("Error initializing Repository.", e);
        } catch (IllegalAccessException e) {
            LOG.error("Error initializing Repository.", e);
        }
    }

   

    /**
     *  map a resource to a file
     * @param communityId
     * @param resourceId
     * @param resourceFilePath this is in fact the file name only
     * @throws FileNotFoundException
     * @throws DuplicateResourceException
     * @throws CommunityNotFoundException
     */
    private void addResource(String communityId, String resourceId,
			String resourceFilePath) throws FileNotFoundException, DuplicateResourceException, CommunityNotFoundException {
    	
    	// resource file should be in the storage directory of the community
    	File tomap = new File(DefaultWebAdapter.getStorageDirectory(communityId), resourceFilePath);
    	
    	LOG.debug("Core2Rep: mapping file:"+ tomap.getAbsolutePath());
    	addResource(communityId, resourceId, tomap);
    	
    }
    
    private void addResource(String communityId, String resourceId,
			File resourceFile) throws FileNotFoundException, DuplicateResourceException, CommunityNotFoundException {
    	
    	try {
            mapper.getResourceMapping(communityId, resourceId);
            
            // If a mapping is found throw a duplicate resource exception
            throw new DuplicateResourceException(resourceId, resourceFile);
            
        } catch (ResourceNotFoundException e) {
        	// Mapping was not found, go ahead and add the new resource
        }
    	
    	mapper.addResource(communityId, resourceId, resourceFile);
		
	}

	private void addAttachment(String communityId, String resourceId,
			String name, String attachpath) throws FileNotFoundException, ResourceNotFoundException, CommunityNotFoundException {
    	mapper.addAttachment(communityId, resourceId,
                name, new File(attachpath)); //get a file from the attachment path 
		
	}
    
	/**
     * @see up2p.core.WebAdapter#remove(String, String)
     * 
     */
    private void remove(String communityId, String resourceId) throws CommunityNotFoundException, ResourceNotFoundException
             {
        LOG.debug("Core2Repository::remove(c,r):Removing resource " + resourceId + " from community id "
                + communityId);

        // prevent removal of root community
        if (resourceId.equals(rootCommunityId))
            throw new CommunityNotFoundException(
                    "The Root Community cannot be removed.");

        // make a list of resource to remove
        ResourceList removeList = new ResourceList();

        // if removing a community, we unshare all its resources too
        if (communityId.equals(rootCommunityId)) {
        	String cId = resourceId;//resourceId because it is the community being removed!!
            Iterator<String> communityList = getResources(cId);
            while (communityList.hasNext()) {
                String resId = (String) communityList.next();
                removeList.addResource(resId, cId, mapper
                        .getResourceMapping(cId, resId), 
                        "title Removed", getResourceURL(
                        cId, resId));
            }
            
            //TODO: also remove the FileMap part of the XMLDB 
            
        }

        // add the resource itself to the list of resources to remove
        ResourceEntry toRemove = new ResourceEntry(resourceId, communityId,
                mapper.getResourceMapping(communityId, resourceId),
                "title Removed",
                getResourceURL(communityId, resourceId));
        removeList.addResource(toRemove);


        // iterate over removed resources and remove them from the repository
        // and mapper
        Iterator<String> removeIdIterator = removeList.iterator();
        File remfile = null;
        Iterator<String> remit;
        while (removeIdIterator.hasNext()) {
            String removeId = removeIdIterator.next();
            String removeCommunityId = removeList.getCommunityId(removeId);

            // remove from rep and map
            repository.remove(removeId, removeCommunityId);
            
            //remove all files from disk
            
            remit = mapper.getAttachmentNames(removeCommunityId, removeId);
            //remove all attachments from disk
            String attachName;
            while (remit.hasNext()){
            	attachName= remit.next();
            	LOG.debug(" - attachment to remove:"+attachName);
            	try {
					remfile=getLocalFileAttachment(removeCommunityId, removeId, attachName);
				} catch (AttachmentNotFoundException e) {
					LOG.error(e);
					continue;
				}
            	
            	if (!remfile.delete()){
                	LOG.error("error deleting file for attachment in resource "+removeId);
                }	
            	
            	//remove attachment mapping from filemapper
            	try {
					mapper.removeAttachment(removeCommunityId, removeId, attachName);
				} catch (AttachmentNotFoundException e) {
					LOG.error(e);
				}
				
				// Delete the attachment directory if no other attachments exist
				if (remfile.getParentFile().list().length == 0) {
					remfile.getParentFile().delete();
				}
            }
            
            remfile = mapper.getResourceMapping(removeCommunityId, removeId);
            if (!remfile.isAbsolute()){ //this should be the case: we now only store file names
            	remfile = new File(DefaultWebAdapter.getStorageDirectory(removeCommunityId),remfile.getName());
            	LOG.debug("Core2repository: file to remove:"+ remfile.getAbsolutePath());
            }
            if (!remfile.delete()){
            	LOG.error("error deleting file for resource "+removeId);
            } else
            {
            	LOG.debug("file "+remfile.getAbsolutePath()+ " deleted for rid "+ removeId);
            }
            mapper.removeResource(removeCommunityId, removeId);
        }

        /*
         * If the resource is a community itself, remove the community's
         * resources from the mapper and repository
         */
        if (communityId.equals(rootCommunityId)) {
            // remove community network adapter from active adapters
           // networkAdapterManager.removeActiveNetworkAdapter(resourceId);

            // remove from repository
            repository.removeCommunity(resourceId);

            // remove the community from mapper
            mapper.removeCommunity(resourceId);

            LOG.debug("Removed collection " + resourceId
                    + " from the root community.");
            
            updateSubnets();
        }


        
    }

    
   /**
    * this is a hack (duplicated code!!) added because the search only returns id without any other parameters
    * 
    * TODO: replace with extra argument to searchLocal() (input the titlelocation) so that the search knows what to return
    * @param resourceId
    * @param communityId
    * @return
    */
    private String getResourceTitle(String resourceId, String communityId) {

    	String titleLoc=null;
    	try {
    		List<String> temp = lookupXPathLocation(communityId, rootCommunityId,
    		"/community/titleLocation"); 
    		if (!temp.isEmpty())  // a hack because now lookup returns multiple answers
    			titleLoc = temp.get(0); 

    		if (titleLoc == null){
    			LOG.error("getResourceTitle " + resourceId + " : "+ communityId +": title location not set");
    			return "[Unknown Title]";
    		}    

    		return lookupXPathLocation(resourceId, communityId, titleLoc).get(0);
    	}
    	catch(Exception e){
    		LOG.error(e);
    	}
    	return "[Error getting title]";
    }

    private List<String> lookupXPathLocation(String cid, String rid, String xpath) 
    throws CommunityNotFoundException, ResourceNotFoundException {
    	
    	return repository.lookupXPathLocation(cid, rid, xpath);
    	
    }
    
	/*
     * @see up2p.core.WebAdapter#setHost(String)
     */
    public void setHost(String host) {
        try {
            InetAddress newHost = InetAddress.getByName(host);
            if (newHost.isLoopbackAddress()) {
                // change loopback to real IP
                // check if preferred interface is set
                String preferredName = config.getProperty(
                        CONFIG_PREFERRED_NETIFACE_NAME, null);
                if (preferredName != null) {
                    newHost = NetUtil.getFirstInetAddress(preferredName);
                } else {
                    newHost = NetUtil.getFirstNonLoopbackAddress();
                }
            }
            if (newHost != null) {
                localHost = newHost.getHostAddress();
                LOG.info("Host address set to '" + localHost + "'.");
            } else {
                // enumeration failed or something went wrong
                localHost = InetAddress.getLocalHost().getCanonicalHostName();
                LOG.error("An error occured in getting the real IP for the"
                        + " loopback interface. Setting address to host name '"
                        + localHost + "'.");
            }
        } catch (UnknownHostException e) {
            LOG.error("An error occurs in getting the address of host '" + host
                    + "'.", e);
        }
    }

    /*
     * @see up2p.core.WebAdapter#setPort(int)
     */
    public void setPort(int port) {
        localPort = port;
    }
    
    /**
     * Sets the urlPrefix that should be used to generate links which refer
     * to this instance of U-P2P
     * @param urlPrefix	The urlPrefix that should be used to generate links which refer
     * 					to this instance of U-P2P
     */
    public void setUrlPrefix(String urlPrefix) {
    	this.urlPrefix = urlPrefix;
    }

    /*
     * @see up2p.core.WebAdapter#shutdown()
     */
    public void shutdown() {
        LOG.info(new java.util.Date().toString()
                + " Shutting down the WebAdapter of U-P2P.");
        // unpublish all resources from the network
 //       removeAllResources(); // NETWORK
        repository.shutdown();
    }


	/**
     * Initialize the local tsworker and initiate a subnet update
     */
    public void initializeTS(ITupleSpace ts) {
    	
    	//tspace = ts; I don't actually need to keep a ref to the tuplespace... just to the worker.
    	localWorker = new RepositoryWorker(ts);
    	localWorker.start();
    	LOG.debug("Core2Repository : tuple space worker started.");
    } 
    
    /**
     * Returns a list of resources shared in the given community.
     * 
     * @param communityId id of the community
     * @return list of <code>String</code> resource ids
     */
    private Iterator<String> getResources(String communityId) {
        try {
            Collection commCol = communityCollection
                    .getChildCollection(communityId);
            if (commCol != null) {
                // get all resource ids from the collection and return
                // an iterator
                return Arrays.asList(commCol.listResources()).iterator();
            }
            LOG.error("ResourceManager getResources Collection not found for "
                    + "community " + communityId);
        } catch (XMLDBException e) {
            LOG.error(
                    "ResourceManager getResources Error getting resources in "
                            + "community " + communityId, e);
        }
        return new ArrayList<String>().iterator();
    }
   
	public void createCommunity(String communityName, String id ) {
		
		repository.createCommunity(id);
        // add community name mapping
        mapper.addCommunity(communityName, id);
        LOG.info("Publish: Community added to file mapper.");
        updateSubnets();
	}

	/**
	 * Stores an XML document in the DB under communityId/resourceId
	 * @param documentNode the XML DOM
	 * @param resourceId 
	 * @param communityId
	 */
	private void store(Node documentNode, String resourceId, String communityId) throws DuplicateResourceException, CommunityNotFoundException {

		repository.store(documentNode, resourceId, communityId);
		// special case for communities

		if (communityId.equals(rootCommunityId)) {

			LOG.debug("Publish: Resource id '" + resourceId + "' is a community.");

			// store in db if the community does not already exist
			//problem: since we just published the resource, this method is not usable. We'll just assume it's not there yet 
			//if (!isResourceLocal(getRootCommunityId(), id)) {
			createCommunity("NONAME",resourceId); //removed: resourceManager.getCommunityName(id) //Note: we don't use the name, so I put "NONAME"
			LOG.info("Publish: Created new community in repository "
						+ "with id " + resourceId);
		}	
	}

	/**
	 * set root community Id
	 * 
	 * @param cid
	 */
	public void setRootCommunityId(String cid) {
		rootCommunityId = cid;
		
	}

	/**
	 *  Retrieves a configuration value stored in the repository.
	 * @param pname
	 * @return
	 */
	public String getConfigurationValue(String pname) {
		
		return repository.getConfigurationValue(pname);
	}
    
	/**
	 * Starts a sub-network update by calling on the repository agent to output
	 * an UpdateSubnets tuple.
	 */
	public void updateSubnets() {
		localWorker.initiateSubnetUpdate();
	}
      
    
}