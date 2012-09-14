package up2p.servlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Enumeration;
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
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import up2p.core.DefaultWebAdapter;
import up2p.core.UserWebAdapter;
import up2p.core.WebAdapter;
import up2p.repository.DatabaseAdapter;
import up2p.util.Config;
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
public class AccessFilter implements Filter {
	/** File path to the config file for the web adapter (used to read login credentials) */
	public static final String WEB_ADAPTER_CONFIG_FILEPATH = 
		DefaultWebAdapter.class.getPackage().getName()
		.replace('.', File.separatorChar)
		+ File.separator + DefaultWebAdapter.WEBADAPTER_CONFIG;
	
    /** IP address for the local loop (127.0.0.1). */
    public static final String LOOPBACK = "127.0.0.1";
    public static final String LOOPBACKIPv6 = "0:0:0:0:0:0:0:1";

	private static final String LOGGER = "up2p.servlet";

    private UserWebAdapter adapter; //not sure to what kind of adapter this servlet should be tied

    /** 
     * WebAdapter configuration file, should not be accessed 
     * until the adapter has been initialized.
     */
    private Config webConfig;
    
    private ServletContext context;

    private String localAddr;

    private boolean remoteEnabled;
    
    /**
     * Flag set by the config file and used to determine if user
     * account authentication should be enabled.
     **/
    private boolean userAuthEnabled;

    /**
     * Constructs an access filter.
     */
    public AccessFilter() {
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
    	Logger LOG = Logger.getLogger(LOGGER);

    	// Redirect any requests other than those required for the initialization page
    	// to the initialization page if the adapter has not been initialized yet
        if (adapter == null) {
        	if(req instanceof HttpServletRequest) {
                HttpServletRequest hreq = (HttpServletRequest)req;
                if(!hreq.getRequestURI().endsWith("/init.jsp") &&
                		!hreq.getRequestURI().endsWith("/style.css") &&
                		!hreq.getRequestURI().endsWith("/header_logo.png")) {
                	((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp).encodeRedirectURL("init.jsp"));
                	return;
                }
        	}
        } else {
	    	// Set the server port if not already set
	        if (adapter.getPort() == 0)
	            adapter.setPort(req.getServerPort());
        }
        //TODO: why cast here?
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        
        // Always allow connections to the community servlet regardless of force local IP
        // setting, so that other peers can always download files
        if(request.getServletPath().equals("/community")) 
        {
        	chain.doFilter(request, response);
        	return;
        }
        
        //Allow remote connections for push when the push has been triggered in push servlet.-------------------------------------------------------
        if(request.getRequestURI().startsWith("/rest/up2p/community")) 
        { //this is a publish, check for remote push
        	String sender = request.getRemoteAddr(); // get remote IP address
        	//note: this will filter at IP-address level, not full peerId. //TODO: better security
        	String[] pathsplit = request.getRequestURI().split("/");
        	String communityId=pathsplit[pathsplit.length-1];//communityId is the last element in the URL 
        	        if(adapter.checkForPush(sender, communityId)) // we check whether this pair IP/communityId matches an expected PUSH
        	        	chain.doFilter(request, response);
        	return;
        } //end exception for remote PUSH.--------------------------------------------------------------------------------------------------------------
        
        
        
        String blockreason = "request blocked: ";
        
		// Allow the request through only if it matches the localhost address
        // or remoteAccess is allowed
        if (remoteEnabled
                || request.getRemoteAddr().equals(LOOPBACK)
                || request.getRemoteAddr().equals(LOOPBACKIPv6)
                || request.getRemoteAddr().equals(localAddr))
        {
        	if(!userAuthEnabled) {
        		// If user authentication is disabled, all requests can be serviced
        		// at this point.
        		chain.doFilter(req, resp);
        		return;
        	}
        	
        	// If a user account has been configured and the correct username / password is
        	// provided, set the user's logged in status to true.
    		if(adapter.getUsername() != null
    				&& request.getSession().getAttribute("up2p:loggedin") == null
    				&& adapter.getUsername().equals(request.getParameter("up2p:username"))
    				&& adapter.getPasswordHashHex().equals(request.getParameter("up2p:password"))) 
    		{
    			request.getSession().setAttribute("up2p:loggedin", "true");
        	}
    		
        	// Always allow access to the attached resources for the login, init,
    		// and create account pages
        	if(request.getRequestURI().endsWith("/init.jsp")
        			|| request.getRequestURI().endsWith("/jquery-1.5.1.min.js")
        			|| request.getRequestURI().endsWith("/sha1.js")
            		|| request.getRequestURI().endsWith("/style.css")
            		|| request.getRequestURI().endsWith("/header_logo.png")) 
        	{
            	chain.doFilter(request, response);
            	return;
            }
        	
        	// Check if a user has been generated for this U-P2P instance, and
        	// redirect the request to the account creation page if not.
        	if(adapter.getUsername() == null) {
        		
        		// If this is already a request for the account creation page, let it
        		// pass through
        		if(request.getServletPath().equals("/user")
                		|| request.getRequestURI().endsWith("/newaccount.jsp"))
            	{
                	chain.doFilter(request, response);
                	return;
                } else {
	        		((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp).encodeRedirectURL("newaccount.jsp"));
	        		return;
                }
        		
        	} else {
	        	// A user has been configured, now check if the request is coming from
	        	// an authenticated session, and service the request if so
	        	if(request.getSession().getAttribute("up2p:loggedin") != null) {
	        		chain.doFilter(req, resp);
	        		return;
	        	}
	        	
	        	if(request.getRequestURI().endsWith("/login.jsp"))
	        	{
	        		// Allow the request to pass through if it is directed to the login page.
	            	chain.doFilter(request, response);
	            	return;
	            } else {
		        	// A user account has been configured, and this request did not
		        	// successfully authenticate. Direct the request to the login page.
		        	((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp).encodeRedirectURL("login.jsp"));
		    		return;
	            }
        	}
        }
        else {
        	//LOG.debug("blocking!!");
        	LOG.error("AccessFilter - REQUEST BLOCKED from: "+ req.getRemoteAddr() 
        			+ ":" + req.getRemotePort());
        	LOG.error("AccessFilter - REQUEST BLOCKED from [httpServletRequest version]: "+ request.getRemoteAddr() 
        			+ ":" + request.getRemotePort());
        	 if (!remoteEnabled)
        		 blockreason +="remote access not allowed; ";
        	 if (!request.getRemoteAddr().equals(LOOPBACK))
        		 blockreason +="remote address not recognized as IPV4 loopback";
             if (! request.getRemoteAddr().equals(LOOPBACKIPv6))
            	 blockreason +="remote address not recognized as IPV6 loopback";
             if (!request.getRemoteAddr().equals(localAddr))
            	 blockreason +="remote address not recognized as local IP (local IP="+localAddr+")";
             
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
    }

    /*
     * @see javax.servlet.Filter#init(FilterConfig)
     */
    public void init(final FilterConfig config) throws ServletException {
        context = config.getServletContext();
        adapter = null;
        
        //System.out.println("TEstingforTraces");
        // set the directory path for up2p
        String up2pPath = context.getRealPath("/");
        final String rootPath = (up2pPath.endsWith("/") || up2pPath.endsWith("\\")) ? up2pPath
                .substring(0, up2pPath.length() - 1)
                : up2pPath;
                
        final String urlPrefix = rootPath.contains("\\") ? rootPath.substring(rootPath.lastIndexOf("\\") + 1)
        			: (rootPath.contains("/") ? rootPath.substring(rootPath.lastIndexOf("/") + 1) : rootPath);
        
        System.setProperty(WebAdapter.UP2P_HOME, rootPath);
        
        // Required for Log4j to differentiate between instances
        System.setProperty(WebAdapter.UP2P_HOME + "." + urlPrefix, rootPath);
        System.out.println("Set system property \"" + WebAdapter.UP2P_HOME + "." + urlPrefix
        		+ "\" = " + rootPath);

        // load Log4j property file
        String logPropFileName = UserWebAdapter.class.getPackage().getName()
                .replace('.', File.separatorChar)
                + File.separator + WebAdapter.LOG_CONFIG_FILE;
        InputStream propStream = UserWebAdapter.class.getClassLoader()
                .getResourceAsStream(logPropFileName);
        if (propStream == null) {
            System.err.println("Error initializing U-P2P logging. Unable"
                    + " to load Log4J properties from " + logPropFileName);
            return;
        }
        Properties logProps = new Properties();
        try {
            logProps.load(propStream);
        } catch (Exception e) {
            System.err.println("Error initializing U-P2P logging.");
            e.printStackTrace(System.err);
            return;
        }

        // configure Log4j
        PropertyConfigurator.configure(logProps);
        
        System.out.println("U-P2P deployment dir: " + rootPath);
        
        // KLUDGE: The Tomcat and servlet API do not specify any way to force
        // webapps to load in a certain order. Because of this, Xindice may be loaded
        // after UP2P. Since UP2P assumes the local repository is always available, a
        // separate thread is started which will poll the database until it is
        // available before the UserWebAdapter is initialized.
        
        new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					webConfig = new Config(WEB_ADAPTER_CONFIG_FILEPATH);
					
					int dbPort;
					try {
						dbPort = Integer.parseInt(webConfig.getProperty("up2p.database.port", "8080"));
					} catch (NumberFormatException e) {
						dbPort = 8080;
					}
					
					System.out.println("Waiting for database to initialize (port " + dbPort + ").");
					Thread.sleep(200);
					while (!DatabaseAdapter.isAvailable("xindice", dbPort)) {
						Thread.sleep(1000);
					}
					System.out.println("Database initialized.");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Get the configured IP and port for Tomcat. Note that the IP address should never
				// actually be used in U-P2P and remains for legacy reasons, but the port must be valid.
		        String configAddr = config.getInitParameter("force_IP");
		        String configPortStr = config.getInitParameter("tomcat_port");
		        int configPort = 0;
		        try {
		        	configPort = Integer.parseInt(configPortStr);
		        } catch (NumberFormatException e) {
		        	System.err.println("WARNING: Configured tomcat port in web.xml could not be parsed as "
		        			+ " an integer. Defaulting to port 8080.");
		        	configPort = 8080;
		        }
		      
				// get the WebAdapter from the servlet context or create it
		        Object o = context.getAttribute("adapter");
		        if (o != null)
		            adapter = (UserWebAdapter) o;
		        else {
		        	System.out.println("Creating UserWebAdapter");
		            adapter = new UserWebAdapter(rootPath, urlPrefix, configPort);
		        	
		            context.setAttribute("adapter", adapter);
		        }
		        
		        // Configure remote access
		        String configStr= config.getInitParameter("allow_remote_access"); //get init parameter from web.xml file
		        if (configStr != null && configStr.equalsIgnoreCase("true"))
		            remoteEnabled = true;
		        else {
		        	System.out.println("Remote access not allowed / config string read: " + configStr);
		            remoteEnabled = false;
		        }
		        
		        if (configAddr != null){
		             adapter.setHost(configAddr); //set the provided IP address to the U-P2P system.
		             //System.out.println("=========setting host:"+configAddr);
		             localAddr = configAddr; //set the filtering address
		        }
		        
		        String authStr= config.getInitParameter("user_authentication");
		        if (authStr != null && authStr.equalsIgnoreCase("true"))
		            userAuthEnabled = true;
		        else {
		        	System.out.println("WARNING: User authentication disabled. See web.xml to activate.");
		            userAuthEnabled = false;
		        }
		        
		        System.out.println("U-P2P instance \"" + adapter.getUrlPrefix() + "\": Initialization complete.");
			}
        	
        }).start();  
    }

}