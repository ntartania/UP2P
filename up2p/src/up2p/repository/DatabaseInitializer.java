package up2p.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.xindice.client.xmldb.services.DatabaseInstanceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

import com.sun.security.auth.login.ConfigFile;

import up2p.core.CommunityFileMap;
import up2p.core.WebAdapter;
import up2p.repository.test.TestDefaultRepository;
import up2p.util.FileUtil;
import up2p.xml.TransformerHelper;
import up2p.xml.Util;

/**
 * Initializes the database from scratch by storing a fresh copy of the root community, 
 * the configuration collection and the file mapper. Should not be used on an existing
 * database
 * 
 * @author Neal Arthorne
 * @author Alexander Craig
 * 
 * @version 1.1
 */
public class DatabaseInitializer {

    /** Name of the property file used to configure the initial db creation. */
    public static final String CONFIG_PROP_FILE = "databaseConfig.properties";

    private Collection communityCollection;

    private Collection configCollection;

    /** Configuration file for initializing the database. */
    private Properties databaseConfig;

    /** Adapter for the database implementation. */
    private DatabaseAdapter dbAdapter;

    /** XMLDB id for the database. */
    private String dbId;
    
    /** The name of the root collection to generate */
    private String rootCollName;

    /** Collection for the File Map. */
    private Collection fileMapCollection;

    protected Collection rootCollection;

    private String rootCommunityId;
    
    private String up2pRootPath;
    
    /**
     * Creates an initialization object for creating the XML database.
     * 
     * @param databaseId id of the database to create ('up2p' for the U-P2P
     * client)
     * @param rootCollectionId	The name of the root collection under which all
     * 							other collections should be generated.
     * @param rootPath	The root path of U-P2P. This is used for copying initial documents
     * 					to the community directory.
     * @param adapter	The database adapter to use for communication with
     * 					the database. If this parameter is null, a new adapter
     * 					will be generated.
     */
    public DatabaseInitializer(String databaseId, String rootCollectionId, String rootPath, DatabaseAdapter adapter) {
        dbId = databaseId;
        rootCollName = rootCollectionId;
        up2pRootPath = rootPath;
        
        // Create properties table
        databaseConfig = new Properties();
        
        // Get properties file
        // Note: Assumes root dir is "apache-tomcat/bin"
        
        File dbPropFile = new File(up2pRootPath + "/data/init/" 
        		+ CONFIG_PROP_FILE);
        System.out.println("Database Initializer using config file: " 
        		+ dbPropFile.getAbsolutePath());
        try {
            // load properties file
            InputStream is = new FileInputStream(dbPropFile);
            databaseConfig.load(is);
        } catch (IOException e) {
            System.err
                    .println("Error loading properties for database configuration.");
            System.err.println("Attempted to load from: " + dbPropFile.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }
        
        if (databaseConfig == null) {
            System.err
                    .println("Error loading properties for database configuration.");
            System.err.println("Attempted to load from: " + dbPropFile.getAbsolutePath());
            System.exit(1);
        }

        dbAdapter = adapter;
    }
    
    /**
     * Performs the complete database initialization procedure. This should only
     * be called when the required database does not already exist, and no further
     * calls to the database initializer should be required after this function
     * has executed.
     * 
     * @throws IOException
     */
    public void initializeDatabase() throws IOException {
    	rootCommunityId = databaseConfig.getProperty("community.RootCommunity.id");
    	
    	// Determine the number and names of communities that should be generated
    	int numCommunities = Integer.parseInt(databaseConfig.getProperty("community.communityCount", "1"));
    	ArrayList<String> communityNames = new ArrayList<String>();
    	for(int i = 1; i <= numCommunities; i++) {
    		String commName = databaseConfig.getProperty("community.communityName" + i, "");
    		if(!commName.equals("")) {
    			communityNames.add(commName);
    		}
    	}
    	
    	startDatabase();
        createInitialCollections();
        
    	// Generate the required entries in the database
        storeResources(communityNames);
        storeFileMap(communityNames);
        
        storeConfiguration();
        // enumDatabase(); // DEBUG
        testGetConfig();
    }

    /**
     * Removes all resources and collections from the XML database.
     *  
     */
    public void clearDatabase() {
        try {
            // remove resources
            String[] resList = rootCollection.listResources();
            for (int i = 0; i < resList.length; i++) {
                String res = resList[i];
                System.out.println("Removing resource " + res);
                rootCollection.removeResource(rootCollection.getResource(res));
            }

            // remove collections
            String[] colList = rootCollection.listChildCollections();
            for (int j = 0; j < colList.length; j++) {
                String col = colList[j];
                System.out.println("Removing collection " + col);
                getCollManager(rootCollection).removeCollection(col);
            }
        } catch (XMLDBException e) {
            System.err.println("XMLDBException errorCode=" + e.errorCode
                    + " vendorErrorCode=" + e.vendorErrorCode);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Creates a file map entry for a given resource, as defined in the database
     * initialization properties file.
     * 
     * @param xmlStub XML stub in the file map for the community
     * @param configName name of the resource as used in the properties file
     * (e.g. 'resource.RootCommunity')
     * @param resourceId id of the resource whose entry is being created
     * @throws IOException 
     */
    private void createCommunityFileMapEntry(Node xmlStub, String configName,
            String resourceId) throws IOException {
    	
        // Get the document used to create the node
        Document doc = xmlStub.getOwnerDocument();
        
        // Get sharedResources node
        Element sharedResources = (Element) xmlStub.getFirstChild();

        File resourceFile = new File(up2pRootPath + "/data/init/" 
        		+ databaseConfig.getProperty(configName + ".def"));
        
        // Create the community directory
        File communityDir = new File(up2pRootPath + "/community");
        if(!communityDir.exists()) {
        	communityDir.mkdir();
        }
        
        // Create the directory for the current community
        File attachDir = new File(up2pRootPath + "/community/" + resourceId);
        if(!attachDir.exists()) {
        	attachDir.mkdir();
        }
        
        
        // Copy resource file from "/data/init" to the directory "/community/[resId]"
        File copyTarget = new File(up2pRootPath + "/community/" + rootCommunityId + "/" + resourceFile.getName());
        System.out.println("Copying community definition: " + resourceFile.getName());
        
        if (!copyTarget.getParentFile().exists()) {
        	copyTarget.getParentFile().mkdir();
        }
        copyTarget.createNewFile();
    
		//copy to the new file using static utility function from FileUtil
		FileOutputStream outStream;
		outStream = new FileOutputStream(copyTarget);
		FileUtil.writeFileToStream(outStream,resourceFile, true);
		
		resourceFile = copyTarget;
		
        
        int attachCount = Integer.parseInt(databaseConfig
                .getProperty(configName + ".attachCount"));
        Element resourceElement = doc.createElement("resource");
        resourceElement.setAttribute("id", resourceId);
        resourceElement.setAttribute("location", resourceFile.getName());
        resourceElement.setAttribute("size", String.valueOf(resourceFile
                .length()));
        resourceElement
                .setAttribute("attachCount", String.valueOf(attachCount));
        sharedResources.appendChild(resourceElement);

        // Add attachments
        Element attachElement;
        File attachFile;
        for (int i = 0; i < attachCount; i++) {
        	
        	attachElement = doc.createElement("attachment");
            attachFile = new File(up2pRootPath + "/data/init/" + resourceId + "/"
            		+ databaseConfig.getProperty(configName
                    + ".attach" + String.valueOf(i + 1)));
            
        	//--- copy attachment to the appropriate directory using static utility function from FileUtil
            File attachCopy = new File(up2pRootPath + "/community/" + rootCommunityId + "/" + resourceId + "/" + attachFile.getName());
            System.out.println("Copying attachment: " + attachFile.getName());
            
            if(!attachCopy.getParentFile().exists()) {
            	attachCopy.getParentFile().mkdir();
            }
            attachCopy.createNewFile();
            
    		outStream = new FileOutputStream(attachCopy);
    		FileUtil.writeFileToStream(outStream,attachFile, true);
    		
    		attachFile = attachCopy;
    		
            //------------
            attachElement.setAttribute("name", "file:" + attachFile.getName());
            attachElement.setAttribute("location", attachFile.getName());
            attachElement.setAttribute("size", String.valueOf(attachFile
                    .length()));
            resourceElement.appendChild(attachElement);
            // Special case - copy the community-create.jsp to the "web" folder of U-P2P
            if(configName.equals("community.RootCommunity") 
            		&& attachFile.getName().equals("community-create.jsp"))
            {
            	System.out.println("Special case: Copying root community creation page.");
	            File createJspCopy = new File(up2pRootPath + "/" + attachFile.getName());
	            System.out.println("Copying community-create.jsp from: " + attachFile.getAbsolutePath()
	            		+ "\nTo: " + createJspCopy.getAbsolutePath());
	            createJspCopy.createNewFile();
	            
	    		outStream = new FileOutputStream(createJspCopy);
	    		FileUtil.writeFileToStream(outStream, attachFile, true);
            }
        }
    }

    /**
     * Creates the initial collections of community, filemap and config.
     *  
     */
    private void createInitialCollections() {
        try {
        	// Create the root for this instance of U-P2P
        	System.out.println("Creating /db/" + rootCollName);
        	rootCollection = getCollManager(rootCollection).createCollection(rootCollName);
        	
            // Create the community collection
            System.out.println("Creating /db/" + rootCollName + "/community");
            communityCollection = getCollManager(rootCollection)
                    .createCollection("community");
            
            // Create the filemap collection
            System.out.println("Creating /db/" + rootCollName + "/filemap");
            fileMapCollection = getCollManager(rootCollection)
                    .createCollection("filemap");
            
            // Create the config collection
            System.out.println("Creating /db/" + rootCollName + "/config");
            configCollection = getCollManager(rootCollection).createCollection(
                    "config");
            
        } catch (XMLDBException e) {
            System.err.println("XMLDBException errorCode=" + e.errorCode
                    + " vendorErrorCode=" + e.vendorErrorCode);
            e.printStackTrace();
        }
    }

    /**
     * Dumps the database to <code>System.out</code>.
     *  
     */
    private void enumDatabase() {
        try {
            System.out.println("Dumping Database.");
            Util.displayCollection(rootCollection, new PrintWriter(System.out,
                    true), 0, Util.getDOMSerializer(System.out));
        } catch (XMLDBException e) {
            System.err.println("XMLDBException errorCode=" + e.errorCode
                    + " vendorErrorCode=" + e.vendorErrorCode);
            e.printStackTrace();
        }
    }

    /**
     * Get the Collection Management Service for a given collection.
     * 
     * @param collection collection for which the service is needed
     * @return collection service or <code>null</code> if an error occurs
     */
    private CollectionManagementService getCollManager(Collection collection) {
        try {
            return (CollectionManagementService) collection.getService(
                    "CollectionManagementService", "1.0");
        } catch (XMLDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Starts the database.
     * Note: Protected as it is required by the DBDumper subclass
     */
    protected void startDatabase() {
        try {
            Class<?> cl = Class.forName(DatabaseAdapter.getDriverClass());
            Database database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");
            database.setProperty("database-id", dbId);
            DatabaseManager.registerDatabase(database);

            // Get the main root collection of the entire xindice install
            rootCollection = database.getCollection(dbAdapter.getDatabaseUrl(), "admin",
                    null);
        } catch (XMLDBException e) {
            System.err.println("XMLDBException errorCode=" + e.errorCode
                    + " vendorErrorCode=" + e.vendorErrorCode);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shuts down the database using the DatabaseInstanceManager.
     */
    public void stopDatabase() {
        try {
            // shutdown the database gracefully
            DatabaseInstanceManager manager = (DatabaseInstanceManager) rootCollection
                    .getService("DatabaseInstanceManager", "1.0");
            manager.shutdown();
            System.out.println("Shutting down database.");
        } catch (XMLDBException e) {
            System.err.println("XMLDBException errorCode=" + e.errorCode
                    + " vendorErrorCode=" + e.vendorErrorCode);
            e.printStackTrace();
        }
    }

    /**
     * Stores U-P2P configuration in the database.
     */
    private void storeConfiguration() {
        try {
        	System.out.println("Generating configuration node.");
        	
            // Create map for root community
            XMLResource document = (XMLResource) configCollection
                    .createResource("up2pConfig", "XMLResource");
            
            // Create root node
            Document doc = TransformerHelper.newDocument();
            Element root = doc.createElement("up2p");
            doc.appendChild(root);

            // Add the configuration node
            Element configNode = doc.createElement("configuration");
            root.appendChild(configNode);

            // Create setting for Root Community id
            Element setting = doc.createElement("setting");
            setting.setAttribute("name", "up2p.root.id");
            setting.setAttribute("value", rootCommunityId);
            configNode.appendChild(setting);

         
            // Store the new config document
            doc.normalize();
            document.setContentAsDOM(doc.getDocumentElement());
            configCollection.storeResource(document);
            
            System.out.println("Stored configuration data.");
        } catch (XMLDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create file map for initial resources.
     * @throws IOException 
     */
    private void storeFileMap(List<String> communityNames) throws IOException {
        try {
        	// First, set up the file map for the root community
        	XMLResource document = (XMLResource) fileMapCollection
            .createResource(rootCommunityId, "XMLResource");
        	Node xmlStub = CommunityFileMap.getXMLStub("RootCommunity",
                    rootCommunityId);
        	((Element) xmlStub).setAttribute("resources", String.valueOf(communityNames.size()));
        	
        	for(String commName : communityNames) {
        		String communityId = databaseConfig.getProperty("community." + commName + ".id");
	
	            // Create the entry for the community in the filemap
	            createCommunityFileMapEntry(xmlStub, "community." + commName,
	                    communityId);
        	}
        	
        	// store the file map entries in the database
            document.setContentAsDOM(xmlStub);
            fileMapCollection.storeResource(document);
            
            // Now, create the (empty) file map for all other communities
            for(String commName : communityNames) {
            	if(commName.equals("RootCommunity")) {
            		continue;
            	}
            	
            	String communityId = databaseConfig.getProperty("community." + commName + ".id");
            	XMLResource commDocument = (XMLResource) fileMapCollection
        				.createResource(communityId, "XMLResource");
            	Node commXmlStub = CommunityFileMap.getXMLStub(commName,
            			communityId);
            	commDocument.setContentAsDOM(commXmlStub);
                fileMapCollection.storeResource(commDocument);
            }
        } catch (XMLDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores the initial resources in the root and NetworkAdapter communities.
     * @param	communityNames	A list of community names to read from the config file and
     * 							publish to the database.
     */
    private void storeResources(List<String> communityNames) {
        try {
        	System.out.println("Creating /db/" + rootCollName + "/community/" + rootCommunityId);
            Collection rootCommunityCollection = getCollManager(
                    communityCollection).createCollection(rootCommunityId);
            
        	for(String commName : communityNames) {
        		File communityDef = new File(up2pRootPath + "/data/init/" + databaseConfig.getProperty("community." + commName + ".def"));
        		String communityId = databaseConfig.getProperty("community." + commName + ".id");
        		
        		if(!communityId.equals(rootCommunityId)) {
	        		System.out.println("Creating /db/" + rootCollName + "/community/" + communityId);
	                Collection newCommunityCollection = getCollManager(
	                        communityCollection).createCollection(communityId);
        		}
        		
        		BufferedReader reader = new BufferedReader(new FileReader(communityDef));
                String line  = null;
                StringBuilder stringBuilder = new StringBuilder();
                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                
                XMLResource document = (XMLResource) rootCommunityCollection
		                .createResource(communityId, "XMLResource");
		        document.setContent(stringBuilder.toString());
		        rootCommunityCollection.storeResource(document);
        	}
        } catch (XMLDBException e) {
            System.err.println("XMLDBException errorCode=" + e.errorCode
                    + " vendorErrorCode=" + e.vendorErrorCode);
            e.printStackTrace();
    	} catch (IOException e) {
			//  Auto-generated catch block
			e.printStackTrace();
    	}
    }

    /**
     * Used to test if configuration data is available from the XML database.
     */
    private void testGetConfig() {
        try {
        	System.out.println("Database Initializer: Testing database configuration..");
        	System.out.println("Getting config from collection: " +  dbAdapter.getConfigUrl());
            Collection configCollection = DatabaseManager.getCollection(
                    dbAdapter.getConfigUrl(), "admin", null);
            XPathQueryService qService = (XPathQueryService) configCollection
                    .getService("XPathQueryService", "1.0");
            ResourceSet result = qService.queryResource("up2pConfig",
                    "/up2p/configuration/setting[@name='up2p.root.id']/@value");
            if (result.getSize() == 0) {
                System.err.println("No result returned from config.");
                return;
            }
            
            XMLResource firstResult = (XMLResource) result.getResource(0);
            Node resultDom = firstResult.getContentAsDOM().getFirstChild();
            // System.out.println("Got result: " + (String) firstResult.getContent()); // DEBUG
            System.out.println("Root Community id from config: "
                    + resultDom.getAttributes().getNamedItem("value").getNodeValue());
        } catch (XMLDBException e) {
            e.printStackTrace();
        }
    }

}