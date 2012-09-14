package proxypedia;
	

	import java.io.File;
	import java.io.IOException;
	import java.io.InputStream;
	import java.util.Properties;

	import javax.servlet.Filter;
	import javax.servlet.FilterChain;
	import javax.servlet.FilterConfig;
	import javax.servlet.ServletContext;
	import javax.servlet.ServletException;
	import javax.servlet.ServletRequest;
	import javax.servlet.ServletResponse;
	import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;

	import org.apache.log4j.PropertyConfigurator;
	import org.apache.log4j.Logger;

	//import up2p.core.UserWebAdapter;
	import up2p.core.WebAdapter;
	//import up2p.repository.DatabaseAdapter;
	import up2p.util.NetUtil;

	/**
	 * A filter that controls who can access the U-P2P client application. By
	 * default only the local host address will be accepted unless the configuration
	 * file is changed to allow access from other addresses. Since AccessFilter is
	 * one of the first parts of the web application to be loaded, it configures
	 * parts of U-P2P including the Log4J configuration and the initial loading of
	 * the DefaultWebAdapter that is stored in the Servlet context.
	 * 
	 * @author Neal Arthorne
	 * @version 1.0
	 */
	public class ProxyPediaAccessFilter implements Filter {
	    /** IP address for the local loop (127.0.0.1). */
	    public static final String LOOPBACK = "127.0.0.1";

		private static final String LOGGER = "up2p.servlet";

	    private WikipediaProxyWebAdapter adapter; //da proxy!

	    private ServletContext context;

	    private String localAddr;

	    private boolean remoteEnabled;
	    
	    private static Logger LOG ;

	    /**
	     * Constructs an access filter.
	     */
	    public ProxyPediaAccessFilter() {
	    	LOG = Logger.getLogger(LOGGER);
	        localAddr = " ";//NetUtil.getFirstNonLoopbackAddress().getHostAddress(); //this doesn't always work (nullpointerexception, maybe the reason is if the computer is not connected to the internet)
	    }

	    /*
	     * @see javax.servlet.Filter#destroy()
	     */
	    public void destroy() {
	    }

	    /*
	     * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse,
	     * FilterChain)
	     */
	    public void doFilter(ServletRequest req, ServletResponse resp,
	            FilterChain chain) throws IOException, ServletException {
	    	//System.out.println("a request to Filter");

	    	// Redirect any requests other than those required for the initialization page
	    	// to the initialization page if the adapter has not been initialized yet
	        if (adapter == null) {
	        	if(req instanceof HttpServletRequest) {
	                HttpServletRequest hreq = (HttpServletRequest)req;
	                if(!hreq.getRequestURI().equals("/up2p/init.jsp") &&
	                		!hreq.getRequestURI().equals("/up2p/style.css") &&
	                		!hreq.getRequestURI().equals("/up2p/header_logo.png")) {
	                	((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp).encodeRedirectURL("/up2p/default.jsp"));
	                	return;
	                }
	        	}
	        } else {
	        	//System.out.println("---step 2--------");
		    	// Set the server name and port
		        if (adapter.getHost() == null)
		            adapter.setHost(req.getServerName());
		        if (adapter.getPort() == 0)
		            adapter.setPort(req.getServerPort());
	        }

	        // allow the request through only if it matches the localhost address
	        // or remoteAccess is allowed
	        HttpServletRequest request = (HttpServletRequest) req;
	        HttpServletResponse response = (HttpServletResponse) resp;
	        
	        //System.out.println("---step 3--------"); 
	       LOG.debug("filtering request: "+request.getMethod() +" "+ request.getRequestURL());
	        if (remoteEnabled
	                || request.getRemoteAddr().equals(LOOPBACK)
	                || request.getRemoteAddr().equals(localAddr)
	                || (request.getContextPath() + request.getServletPath())
	                        .equals("/up2p/community")) // allows resource downloads
	        {
	        	LOG.debug("letting through!");
	            chain.doFilter(request, response);
	        }
	        else {
	        	LOG.debug("blocking!!");
	        	LOG.error("remote enabled:"+ remoteEnabled);
	            response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        }
	    }

	    /*
	     * @see javax.servlet.Filter#init(FilterConfig)
	     */
	    public void init(final FilterConfig config) throws ServletException {
	        context = config.getServletContext();
	        adapter = null;
	        LOG = Logger.getLogger(LOGGER);
	        
	       //System.out.println("TEstingforTraces");
	        // set the directory path for up2p
	        String up2pPath = context.getRealPath("/");
	        final String rootPath = (up2pPath.endsWith("/") || up2pPath.endsWith("\\")) ? up2pPath
	                .substring(0, up2pPath.length() - 1)
	                : up2pPath;
	        System.setProperty(WebAdapter.UP2P_HOME, rootPath);

	        //System.out.println("TEstingforTraces2");
	        // load Log4j property file
	        String logPropFileName = WikipediaProxyWebAdapter.class.getPackage().getName()
	                .replace('.', File.separatorChar)
	                + File.separator + WebAdapter.LOG_CONFIG_FILE;
	        InputStream propStream = WikipediaProxyWebAdapter.class.getClassLoader()
	                .getResourceAsStream(logPropFileName);
	        //System.out.println("TEstingforTraces333");
	        if (propStream == null) {
	            System.err.println("Error initializing U-P2P logging. Unable"
	                    + " to load Log4J properties from " + logPropFileName);
	            return;
	        }
	        Properties logProps = new Properties();
	        //System.out.println("TEstingforTraces444444444444");
	        try {
	            logProps.load(propStream);
	        } catch (Exception e) {
	            System.err.println("Error initializing U-P2P logging.");
	            e.printStackTrace(System.err);
	            return;
	        }

	        // configure Log4j
	        PropertyConfigurator.configure(logProps);
	        
	        System.out.println("U-P2P (proxy) rootpath:"+ rootPath);
	        
	        // KLUDGE: The Tomcat and servlet API do not specify any way to force
	        // webapps to load in a certain order. Because of this, Xindice may be loaded
	        // after UP2P. Since UP2P assumes the local repository is always available, an
	        // artificial 8 second delay is added between UP2P launching and UP2P
	        // attempting a connection to the database.
	        
	        
	        new Thread(new Runnable() {

				@Override
				public void run() {
					
					
					// get the WebAdapter from the servlet context or create it
			        Object o = context.getAttribute("adapter");
			        if (o != null)
			            adapter = (WikipediaProxyWebAdapter) o;
			        else {
			        	//System.out.println("Creating WPWebAdapter");
			            adapter = new WikipediaProxyWebAdapter(rootPath);
			        	
			            context.setAttribute("adapter", adapter);
			        }
			        // configure remote access
			        /*String configStr = adapter.getConfigProperty(
			                WebAdapter.CONFIG_REMOTE_ACCESS, null);*/
			        
			        String configStr= config.getInitParameter("allow_remote_access"); //get init parameter from web.xml file
			        //System.out.println("TEstingforTraces33 "+configStr);
			        if (configStr != null && configStr.equalsIgnoreCase("true"))
			            remoteEnabled = true;
			        else {
			        	System.out.println("Remote access not allowed / config string read: " + configStr);
			            remoteEnabled = false;
			        }
			        
			        /*Set the IP Address from web.xml config file -- to sidestep problems with finding the correct address from the network interfaces*/
			        String configAddr= config.getInitParameter("force_IP"); //get init parameter from web.xml file
			        String configport = config.getInitParameter("tomcat_port");
			        System.out.println("U-P2P Proxypedia: configured host / port: "+configAddr + " "+ configport);
			        if (configAddr != null){
			             adapter.setHost(configAddr); //set the provided IP address to the U-P2P system.
			             localAddr = configAddr; //set the filtering address
			        }
			        if (configport !=null) // give the provided port number
			        	adapter.setPort(Integer.parseInt(configport));
					
				}
			
	        	
	        }).start();

	        
	    }

	}