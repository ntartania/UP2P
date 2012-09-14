package up2p.repository.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import up2p.core.CommunityNotFoundException;
import up2p.core.DuplicateResourceException;
import up2p.core.ResourceNotFoundException;
import up2p.repository.DatabaseAdapter;
import up2p.repository.DefaultRepository;
import up2p.repository.Repository;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.AttachmentListFilter;
import up2p.xml.filter.AttachmentReplacer;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.DigestFilter;
import up2p.xml.filter.ValidationFilter;

/**
 * Tests the default repository implementation.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class TestDefaultRepository extends TestCase {
    /** The repository used for all tests. */
    private static DefaultRepository REP;

    /** Resource ID of the Root Community. */
    private static String ROOT_ID;

    /** Resource ID of the NetworkAdapter Community. */
    private static String NETWORK_ADAPTER_COMMUNITY_ID;

    /** Resource ID of the test stamp community. */
    private static String STAMP_COMMUNITY_ID;

    /** Resource ID of a test stamp. */
    private static String STAMP_ID;

    /**
     * Creates a test case with the given name.
     * 
     * @param name the name of the test case
     */
    public TestDefaultRepository(String name) {
        super(name);
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
    }

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() {
        if (REP == null) {
            // start the XML database
            DatabaseAdapter dbAdapter = new DatabaseAdapter("xindice", 8080, "up2p",
            		System.getProperty("user.dir"), Repository.LOGGER);

            // create the DefaultRepository
            REP = new DefaultRepository();
            REP.setDbAdapter(dbAdapter);
            REP.configureRepository(null, dbAdapter);
        }

        try {
            if (ROOT_ID == null)
                ROOT_ID = generateResourceId(new File(System
                        .getProperty("user.dir")
                        + File.separator
                        + "community"
                        + File.separator
                        + "community" + File.separator + "community.xml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void debugLogging() {
        // logging check
        //System.out.println(
        //	"Class path: " + System.getProperty("java.class.path"));
        Logger rootLog = LogManager.getRootLogger();
        System.out.println("Root Logger level: " + rootLog.getLevel());
        Enumeration rootAppEnum = rootLog.getAllAppenders();
        while (rootAppEnum.hasMoreElements()) {
            Appender app = (Appender) rootAppEnum.nextElement();
            System.out.print("Appender: " + app.getName());
            if (app instanceof FileAppender)
                System.out.println(" File: " + ((FileAppender) app).getFile());
            else
                System.out.println(" class: " + app.getClass());
        }

        // all the other loggers
        Enumeration logEnum = LogManager.getCurrentLoggers();
        while (logEnum.hasMoreElements()) {
            Logger log = (Logger) logEnum.nextElement();
            System.out.println("Logger name: " + log.getName()
                    + " additivity: " + log.getAdditivity() + " level: "
                    + log.getLevel());
            Enumeration logAppEnum = log.getAllAppenders();
            while (logAppEnum.hasMoreElements()) {
                Appender app = (Appender) logAppEnum.nextElement();
                System.out.print("Appender: " + app.getName());
                if (app instanceof FileAppender)
                    System.out.println(" File: "
                            + ((FileAppender) app).getFile());
                else
                    System.out.println();
            }
        }
    }

    public void testCreateCommunity() {
        //debugLogging();
        REP.createCommunity(ROOT_ID);
        System.out.println("Root Community id " + ROOT_ID);
    }

    public void testStoreResource() {
        File resourceFile = new File(System.getProperty("user.dir")
                + File.separator + "community" + File.separator + "community"
                + File.separator + "community.xml");
        try {
			REP.store(ROOT_ID, ROOT_ID, resourceFile);
		} catch (DuplicateResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommunityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void testStoreResourceFailure() {
        File resourceFile = new File("foo");
        try {
            REP.store("xyz", "abc", resourceFile);
            fail("Test to store a resource in an unknown community failed.");
        } catch (ResourceNotFoundException e) {
        } catch (DuplicateResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommunityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void testStoreResourceAsDOM() throws Throwable {
        // parse xml document
        File xmlFile = new File(System.getProperty("user.dir") + File.separator
                + "community" + File.separator + "NetworkAdapter"
                + File.separator + "NetworkAdapter.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(xmlFile);

        // digest the test resource and get its ID
        NETWORK_ADAPTER_COMMUNITY_ID = generateResourceId(xmlFile);

        // store it using the DOM interface
        REP.store(document.getDocumentElement(), NETWORK_ADAPTER_COMMUNITY_ID,
                ROOT_ID);

        System.out.println("NetworkAdapter Community id "
                + NETWORK_ADAPTER_COMMUNITY_ID);
    }

    public void testSearch() {
        SearchQuery q = new SearchQuery();
        q.addAndOperand("community/keywords", "P2P root community");
        q.addAndOperand("community/description", "root community");
        
        
        
        
        SearchResponse[] results = null;//REP.search(ROOT_ID, q.getQuery(),q.getMaxResults());
        assertTrue(results.length > 0);
        Object firstResourceResult = results[0].getId();
        assertTrue(firstResourceResult instanceof String);
        assertEquals(ROOT_ID, (String) firstResourceResult);
    }

    public void testSearchFailure() {
        SearchQuery q = new SearchQuery();
        q.addAndOperand("community/keywords", "U-P2P root");
        q.addAndOperand("community/description", "root");
        try {
            REP.search("abc", q.getQuery(),q.getMaxResults());
            fail("Test to search in an unknown community failed.");
        } catch (CommunityNotFoundException e) {
        }
    }

    public void testisValid() throws Throwable {
        assertTrue(REP.isValid(ROOT_ID, ROOT_ID));
    }

    public void testRetrieveFailure() {
        assertFalse(REP.isValid("abc", ROOT_ID));
        assertFalse(REP.isValid(ROOT_ID, "foo"));
    }

    public void testStampCommunity() throws Throwable {
        File resourceFile = new File(System.getProperty("user.dir")
                + File.separator + "test" + File.separator + "stamps"
                + File.separator + "stamps.xml");

        // store a test resource so it can be removed
        // digest the community file to get the proper ID
        STAMP_COMMUNITY_ID = generateResourceId(resourceFile);

        // store the community
        REP.store(STAMP_COMMUNITY_ID, ROOT_ID, resourceFile);

        // create the community
        REP.createCommunity(STAMP_COMMUNITY_ID);

        // check if it's there
        assertTrue(REP.isValid(ROOT_ID, STAMP_COMMUNITY_ID));

        // add a resource to the community
        resourceFile = new File(System.getProperty("user.dir") + File.separator
                + "test" + File.separator + "stamps" + File.separator + "2002"
                + File.separator + "2002_Olympic_Winter_Games.xml");
        if (!resourceFile.exists())
            fail("Resource file required for test is missing: "
                    + resourceFile.getAbsolutePath());

        STAMP_ID = generateResourceId(resourceFile);
        REP.store(STAMP_ID, STAMP_COMMUNITY_ID, resourceFile);

        // check if it's there
        assertTrue(REP.isValid(STAMP_COMMUNITY_ID, STAMP_ID));
    }

    public void testRemoveResource() throws CommunityNotFoundException, ResourceNotFoundException {
        // remove the test resource from the stamp community
        REP.remove(STAMP_ID, STAMP_COMMUNITY_ID);

        // check if the stamp resource is there
        assertFalse(REP.isValid(STAMP_COMMUNITY_ID, STAMP_ID));

        // remove the stamp community completely
        REP.removeCommunity(STAMP_COMMUNITY_ID);

        // check if the stamp community is there
        assertFalse(REP.isValid(STAMP_COMMUNITY_ID, "xyz"));

        // remove the stamp community from the root community
        REP.remove(STAMP_COMMUNITY_ID, ROOT_ID);

        assertFalse(REP.isValid(ROOT_ID, STAMP_COMMUNITY_ID));
    }

    public void testRemoveResourceFailure() throws CommunityNotFoundException, ResourceNotFoundException {
        try {
            REP.remove("xyz", "abc");
            fail("Test to remove from an unknown community failed.");
        } catch (CommunityNotFoundException e) {
        }

        try {
            REP.remove("xyz", ROOT_ID);
            fail("Test to remove an unknown resource failed.");
        } catch (ResourceNotFoundException e) {
        }
    }

    public void testStoreNetworkAdapters() throws Throwable {
        // store the generic central peer-to-peer adapter
        File resourceFile = new File(System.getProperty("user.dir")
                + File.separator + "community" + File.separator
                + "NetworkAdapter" + File.separator
                + "GenericCentralPeerToPeer.xml");
        if (!resourceFile.exists())
            fail("Required file: " + resourceFile.getName() + " is not found.");
        String p2pAdapterId = generateResourceId(resourceFile);
        System.out.println("Generic Peer-to-Peer Adapter id " + p2pAdapterId);

        REP.store(p2pAdapterId, NETWORK_ADAPTER_COMMUNITY_ID, resourceFile);

        assertTrue(REP.isValid(NETWORK_ADAPTER_COMMUNITY_ID, p2pAdapterId));
    }

    public void testDatabaseDump() {
        REP.dumpDatabase();
    }

    /**
     * Gets the Resource ID of a file using the same method as in
     * up2p.core.DefaultWebAdapter#publish(String,File,boolean)
     * 
     * @param resourceFile
     * @return id of the resource
     * @throws FileNotFoundException if there is an error opening the resource file
     * @throws SAXException if there is an error parsing the resource file
     * @throws IOException if there is an error reading the resource file
     * 
     * @see up2p.core.DefaultWebAdapter#publish(String,File,boolean)
     */
    public static String generateResourceId(File resourceFile)
            throws FileNotFoundException, SAXException, IOException {
        XMLReader reader = TransformerHelper.getXMLReader();

        // create a filter chain for capturing attachment links
        DefaultResourceFilterChain chain = new DefaultResourceFilterChain();
        // add a validation filter to catch errors
        chain.addFilter(new ValidationFilter());
        // add filter to catch attachment links
        AttachmentListFilter attachListFilter = new AttachmentListFilter(
                "file://");
        chain.addFilter(attachListFilter);
        // process chain to get attachment links
        chain.doFilter(reader, new InputSource(
                new FileInputStream(resourceFile)));

        // create new filter chain
        chain = new DefaultResourceFilterChain();
        // add a filter to remove attachment links
        chain.addFilter(AttachmentReplacer
                .createRemovalFilter(attachListFilter));
        // add a digest filter to generate the resource ID
        DigestFilter digestFilter = new DigestFilter();
        chain.addFilter(digestFilter);

        // execute the filter chain
        chain.doFilter(reader, new InputSource(
                new FileInputStream(resourceFile)));
        // get the id for the resource
        return (String) chain.getProperty(DigestFilter.HASH_PROPERTY);
    }
}