package up2p.core;
/*
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import up2p.repository.DatabaseAdapter;
import up2p.repository.Repository;
import up2p.repository.ResourceEntry;
import up2p.search.SearchQuery;
import up2p.search.SearchResponse;
import up2p.search.SearchResponseListener;
import up2p.servlet.DownloadService;
*/
/**
 * The core of U-P2P that glues together the <code>Repository</code> and
 * <code>NetworkAdapters</code> and provides support to Java ServerPages
 * (JSPs). Responsible for initializing U-P2P and coordinating the publishing,
 * removing, viewing and searching of resources.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface WebAdapter {
   
////////////////////////////////////////////////////////////
	 /** Location of default create stylesheet. */
   public static final String CONFIG_DEFAULT_CREATE = "up2p.defaultCreate.location";


   /**
    * Config property for class implementing the DownloadService used by the
    * DownloadServlet.
    */
   public static final String CONFIG_DOWNLOAD_SERVICE_PROVIDER = "up2p.servlet.downloadService.provider";

   /** Initialization refresh delay. */
   public static final String CONFIG_INIT_REFRESH = "up2p.init.refresh";

   /** MIME support file name. */
   public static final String CONFIG_MIME_SUPPORT = "up2p.mime.config";

   /**
    * Name of preferred interface to be used by U-P2P. If this property is not
    * set, it defaults to the first non-loopback interface found by
    * enumeration.
    */
   public static final String CONFIG_PREFERRED_NETIFACE_NAME = "up2p.network.preferredInterfaceName";

   /** Config property for class implementing the XML Repository. */
   public static final String CONFIG_REPOSITORY_PROVIDER = "up2p.repository.provider";

   /** Default maximum number of results to return for search query. */
   public static final String CONFIG_SEARCH_MAX_RESULTS = "up2p.search.maxResults";

   /** Maximum timeout for searches. */
   public static final String CONFIG_SEARCH_TIMEOUT = "up2p.seach.timeout";

   /** Encoding scheme for outgoing XML resources */
   public static final String DEFAULT_ENCODING = "UTF-8";

   /**
    * Name of the Log4J logger used for the WebAdapter.
    */
   public static final String LOGGER = "up2p.webAdapter";

   /**
    * Name of the Log4J configuration file for configuring all the loggers for
    * U-P2P. Configured in up2p.servlet.AccessFilter.
    */
   public static final String LOG_CONFIG_FILE = "up2p.log4j.properties";

   /**
    * The system property name that defines the root directory for U-P2P.
    */
   public static final String UP2P_HOME = "up2p.home";
   
   //methods
   public int getPort();
   
   public String getHost();
   
   /**
    * @return	The URL prefix for this instance of U-P2P.
    * 			Ex. If U-P2P is available at "http://hostname:8080/up2p/",
    * 			this will return "up2p"
    */
   public String getUrlPrefix();
}