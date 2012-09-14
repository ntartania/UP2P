package up2p.repository;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.xindice.client.xmldb.DatabaseImpl;
import org.apache.xindice.xml.dom.DOMParser;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

import org.apache.xindice.client.xmldb.services.*;


/**
 * Starts, stops and maintains a reference to the currently active database used
 * for parts of U-P2P and the default Repository implementation.
 */
public class DatabaseAdapter {
	/** A default database Id to use if none is explicitly provided */
	public static final String DB_DEFAULT_ID = "xindice";
	
	/** A default database port to use if none is explicitly provided */
	public static final int DB_DEFAULT_PORT = 8080;
	
	/** A default root collection name to use if none is explicitly provided */
	public static final String DB_DEFAULT_COLLECTION = "up2p";
	
    /** Class name of the database driver implementation. */
    public static final String DB_DRIVER = "org.apache.xindice.client.xmldb.DatabaseImpl";

    /** Name of the community Collection. */
    public static final String DB_COMMUNITY = "community";

    /** Name of the community file map Collection. */
    public static final String DB_COMMUNITY_FILE_MAP = "filemap";

    /** Name of the configuration collection. */
    public static final String DB_CONFIG = "config";

    /** Id of the database used by this adapter. */
    protected String databaseId;
    
    /** The port to attempt a database connection on (localhost is always the assumed host) */
    protected int databasePort;
    
    /** The name of the root collection used by this adapter */
    protected String rootCollectionId;

    /** Logger used by the database. */
    private Logger LOG;
    
    private Collection rootCollection;

    /**
     * Constructs an adapter with the given database id.
     * 
     * @param dbId id of the XML database used by this adapter
     * @param dbPort	The port to attempt a database connection on (localhost is always the assumed host)
     * @param rootCollectionName	The name of the root collection to use for this U-P2P instance
     * @param configFile configuration file for the XML database
     * @param rootPath	The root directory of U-P2P. This is purely used for initializing the database
     * 					(when initial documents must be copied to the community directory)
     * @param logName name of the logger to use
     */
    public DatabaseAdapter(String dbId, int dbPort, String rootCollectionName, 
    		String rootPath, String logName) {
        LOG = Logger.getLogger(logName);
        databaseId = dbId;
        databasePort = dbPort;
        rootCollectionId = rootCollectionName;

        // initialize driver
        try {
            Class<?> cl = Class.forName(DatabaseAdapter.getDriverClass());
            org.xmldb.api.base.Database database = (Database) cl.newInstance();
            DatabaseManager.registerDatabase(database);
            
            System.out.println("Initializing database connection to: xmldb:" + dbId + "://localhost:" + databasePort 
            		+ "/" + rootCollectionId);
            
            rootCollection = DatabaseManager.getCollection("xmldb:" + dbId + "://localhost:" + databasePort + "/db/" 
            		+ rootCollectionId + "/", "admin",
                    null);
            while(rootCollection == null) {
            	// Database has not been initialized, initialize it
            	DatabaseInitializer dbInit = new DatabaseInitializer(databaseId, rootCollectionName, rootPath, this);
            	dbInit.initializeDatabase();
            	
            	rootCollection = DatabaseManager.getCollection("xmldb:" + dbId + "://localhost:" + databasePort + "/db/" 
                		+ rootCollectionId + "/", "admin",
                        null);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            LOG.error("Error initializing the repository database.", e);
        }
    }
    
    /**
     * Retrieves the class name of the database implementation.
     * 
     * @return the class name of the dbXml driver
     */
    public static String getDriverClass() {
        return DB_DRIVER;
    }
    
    /**
     * Checks if a database with the specified id is available on the local host.
     * If the database exists but is not initialized, the call will block
     * until the database has been initialized.
     * 
     * @param dbId	The database id to check for
     * @param dbPort	The port to attempt a connection on
     * @return	true if a connection can be made, false if not
     */
	public static boolean isAvailable(String dbId, int dbPort) {
		boolean connectionEstablished = false;
		org.xmldb.api.base.Database database = null;
		
		try {
			// Setup the database connection and register with the DatabaseManager.
			Class<?> cl = Class.forName(DatabaseAdapter.getDriverClass());
			database = (Database) cl.newInstance();
			DatabaseManager.registerDatabase(database);

			// Try to connect to the database.
			// Note: If the database has been launched but has not been initialized yet, this call will block until the
			// database is available.
			Collection dbRoot = DatabaseManager.getCollection("xmldb:" + dbId + "://localhost:" + dbPort + "/db/", "admin",
					null);
	
			// Set the successful connection flag if a collection was successfully returned
			if(dbRoot != null) {
				connectionEstablished = true;
			}
			
		} catch (Exception e) {
		} finally {
			
			// Unregister the database with the DatabaseManager
			try { 
				if(database != null) { DatabaseManager.deregisterDatabase(database); }
			} catch (XMLDBException e) {e.printStackTrace();}
			
		}
		
		return connectionEstablished;
	}
	
	/**
     * Checks if a database with the default id and port is available on the local host.
     * If the database exists but is not initialized, the call will block
     * until the database has been initialized.
     * 
     * @return	true if a connection can be made, false if not
     */
	public static boolean isAvailable() {
		return isAvailable(DB_DEFAULT_ID, DB_DEFAULT_PORT);
	}

    public Collection getRootCollection(){
    	return rootCollection;
    }
    
    /**
     * Returns the path to the community database.
     * 
     * @return path to the community database
     */
    public String getCommunityUrl() {
        return getDatabaseRootUrl() + "/" + DB_COMMUNITY;
    }

    /**
     * Returns the name of the collection where communities are stored.
     * 
     * @return community collection in the database
     */
    public String getCommunityCollection() {
        return DB_COMMUNITY;
    }

    /**
     * Returns the path to the Collection with community file maps.
     * 
     * @return path to the community file maps
     */
    public String getFileMapUrl() {
        return getDatabaseRootUrl() + "/" + DB_COMMUNITY_FILE_MAP;
    }

    /**
     * Returns the path to the root collection of the local database instance.
     * 
     * @return path to the database root collection
     */
    public String getDatabaseRootUrl() {
        return "xmldb:" + databaseId + "://localhost:" + databasePort + "/db/"
        	+ rootCollectionId;
    }
    
    /**
     * Returns the path to the root of the local database instance.
     * NOTE: Returns the path without the "xmldb:" prefix
     * 
     * @return path to the database root
     */
    public String getDatabaseUrl() {
        return databaseId + "://localhost:" + databasePort + "/db/";
    }
    
    /**
     * Creates a collection in the database.
     * 
     * @param collectionName name of the collection to create
     * @param isCommunity	Flag determining if the new collection should be created
     * 						in the "community" collection. If this is set to false,
     * 						the new collection will be created in the root collection for
     * 						this instance of U-P2P.
     * @return the created collection
     * @throws XMLDBException if an error occurs when creating the collection
     */
    public Collection createCollection(String collectionName, boolean isCommunity)
            throws XMLDBException {
    	
    	Collection root;
    	if(isCommunity) {
    		root = DatabaseManager.getCollection(getCommunityUrl(), "admin", null);
    	} else {
    		root = DatabaseManager.getCollection(getDatabaseRootUrl(), "admin", null);
    	}
        
        Collection child;
        try {
        	child = root.getChildCollection(collectionName);
        } catch (XMLDBException e) {
        	child = null;
        }
        
        if (child == null) {
            // create the collection
        	CollectionManager mgtService = (CollectionManager) root
                    .getService("CollectionManagementService", "1.0");
            
        	
            String collectionConfig =
                "<collection compressed=\"true\" " +
                "            name=\"" + collectionName.substring(collectionName.lastIndexOf("/") + 1) + "\">" +
                "   <filer class=\"org.apache.xindice.core.filer.BTreeFiler\"/>" +
                "</collection>";

            try {
	            child = mgtService.createCollection(collectionName,
	            		DOMParser.toDocument(collectionConfig));
	            
	            String elementIndexer = "<index class=\"org.apache.xindice.core.indexer.ValueIndexer\"" +
                " name=\"comm_element_index\" pattern=\"*\" />";
	            String attributeIndexer = "<index class=\"org.apache.xindice.core.indexer.ValueIndexer\"" +
                " name=\"comm_attribute_index\" pattern=\"*@*\" />";
	            
	            CollectionManager childMgtService = (CollectionManager) child
                	.getService("CollectionManagementService", "1.0");
	            childMgtService.createIndexer(DOMParser.toDocument(elementIndexer));
	            childMgtService.createIndexer(DOMParser.toDocument(attributeIndexer));
	            
            } catch (Exception e) {
            	throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, 
            			"MgtService Failed to create collection.\n\tURI:" + collectionName
            			+"\n\tName: " + collectionName.substring(collectionName.lastIndexOf("/") + 1));
            }
        }
        
        return child;
    }

    /**
     * Checks if a collection exists in the database.
     * 
     * @param collectionName name of the collection relative to database root
     * @return <code>true</code> if the collection was found,
     * <code>false</code> otherwise
     * @throws XMLDBException if an error occurs in the database access
     */
    public boolean isCollection(String collectionName) throws XMLDBException {
        Collection root = DatabaseManager.getCollection(getDatabaseRootUrl());
        
        Collection child;
        try {
        	child = root.getChildCollection(collectionName);
        } catch (XMLDBException e) {
        	child = null; // No community was found
        }
        return child != null;
    }

    /**
     * Returns the database URL for the config collection.
     * 
     * @return URL to the config collection
     */
    public String getConfigUrl() {
        return getDatabaseRootUrl() + "/" + DB_CONFIG;
    }
}