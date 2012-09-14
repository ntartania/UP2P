package up2p.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import stracciatella.Connection;
import stracciatella.ConnectionData;
import stracciatella.ConnectionList;
import stracciatella.Host;
import stracciatella.StracciatellaConnection;
import up2p.peer.jtella.HostCacheParser;
import up2p.peer.jtella.JTellaAdapter;

/**
 * The ConfigServlet is responsible for making changes to the
 * configuration options of U-P2P when prompted by the client
 * interface. For example, the ConfigServlet should handle the
 * addition of removal of hosts to/from the static host cache,
 * and the configuration of a public IP/port to advertise to
 * other peers.
 * 
 * @author Alexander Craig
 */
public class ConfigServlet extends AbstractWebAdapterServlet {
	private StracciatellaConnection gnutella;
	//note: all this updated for new protocol Stracciatella
	
	/**
     * Creates the servlet.
     */
    public ConfigServlet() {
    	super();
    	gnutella = null;
    }
    
	/*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    		throws ServletException, IOException {
    	doConfig(req, resp);
    }

    /*
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
    	doConfig(req, resp);
    }
    
    /**
     * Parses incoming parameters from a client request, and dispatches
     * configuration requests
     */
	protected void doConfig(HttpServletRequest request,
    			HttpServletResponse response) throws ServletException, IOException 
    {
		if(gnutella == null) {
			gnutella = JTellaAdapter.getConnection();
		}
		
		if(request.getParameter(HttpParams.UP2P_SET_MAX_INCOMING) != null
				|| request.getParameter(HttpParams.UP2P_SET_MAX_OUTGOING) != null)
		{
			ConnectionData gnutellaSettings = gnutella.getConnectionData();
			
			if(request.getParameter(HttpParams.UP2P_SET_MAX_INCOMING) != null) {
				try {
					int maxIncoming = Integer.parseInt(request.getParameter(HttpParams.UP2P_SET_MAX_INCOMING));
					maxIncoming = maxIncoming > 0 ? maxIncoming : 0;
					LOG.debug("ConfigServlet: Setting max incoming connections to: " + maxIncoming);
					gnutellaSettings.setIncommingConnectionCount(maxIncoming);
				} catch (NumberFormatException e) {
					LOG.error("ConfigServlet: Invalid value passed for max incoming connections: " 
							+ request.getParameter(HttpParams.UP2P_SET_MAX_INCOMING));
				}
			}
			
			if(request.getParameter(HttpParams.UP2P_SET_MAX_OUTGOING) != null) {
				try {
					int maxOutgoing = Integer.parseInt(request.getParameter(HttpParams.UP2P_SET_MAX_OUTGOING));
					maxOutgoing = maxOutgoing > 0 ? maxOutgoing : 0;
					LOG.debug("ConfigServlet: Setting max outgoing connections to: " + maxOutgoing);
					gnutellaSettings.setOutgoingConnectionCount(maxOutgoing);
				} catch (NumberFormatException e) {
					LOG.error("ConfigServlet: Invalid value passed for max outgoing connections: " 
							+ request.getParameter(HttpParams.UP2P_SET_MAX_OUTGOING));
				}
			}
		}
		
		if(request.getParameter(HttpParams.UP2P_ADD_HOST) != null) {
			// Host Additions
			for(String host : request.getParameterValues(HttpParams.UP2P_ADD_HOST)) {
				// Attempt to parse out the IP / Port
				// TODO: Need better validation here
				LOG.debug("ConfigServlet Got request to add: \"" + host + "\" from host cache.");
				
				if(validateHostString(host)) {
					String[] splitHost = host.split(":");
					int port = Integer.parseInt((splitHost[1]));
					if(splitHost[0].equalsIgnoreCase("localhost") || splitHost[0].equalsIgnoreCase("127.0.0.1")) {
						LOG.debug("ConfigServlet Add request specified host \"" + host + "\", blocking addition of localhost.");
						String notificationString = "The host cache should not contain references to the localhost (or 127.0.0.1). "
							+ "If you are trying to add another peer hosted on the same server, please use the IP address of the "
							+ "server.";
						adapter.addNotification(notificationString);
					} else {
						gnutella.addFriend(splitHost[0], port);
						
						LOG.debug("ConfigServlet Host \"" + host + "\" successfully added to host cache.");
					}
				}
			}
		}
		
		if(request.getParameter(HttpParams.UP2P_REMOVE_HOST) != null) {
			// Host Removals
			for(String host : request.getParameterValues(HttpParams.UP2P_REMOVE_HOST)) {
				// Attempt to parse out the IP / Port
				// TODO: Need better validation here
				LOG.debug("ConfigServlet Got request to remove: \"" + host + "\" from host cache.");

				if(validateHostString(host)) {
					String[] splitHost = host.split(":");
					int port = Integer.parseInt((splitHost[1]));
					//hostCache.getHosts();
					if(gnutella.unFriend(splitHost[0], port)) {
						LOG.debug("ConfigServlet Host \"" + host + "\" successfully removed from host cache.");
					} else {
						LOG.debug("ConfigServlet Host \"" + host + "\" was not found in host cache.");
					}
				}
			}
		}
		
		//note: we support this as legacy.
		//in future, allowed actions are friend/unfriend/blacklist/unblacklist
		if(request.getParameter(HttpParams.UP2P_DROP_CONNECTION) != null) {
			// Host Removals
			String dropHost = request.getParameter(HttpParams.UP2P_DROP_CONNECTION);

			LOG.debug("ConfigServlet: Got request to drop active connection to: " + dropHost);
			String[] splitHost = dropHost.split(":");
			int port = Integer.parseInt((splitHost[1]));
			gnutella.unFriend(splitHost[0], port); //update: now dropping a connection and removing a friend have become the same thing.
			
		}
		
		// Redirect the request to the network status page
		String redirect = response.encodeURL("network-status.jsp");
        RequestDispatcher rd = request.getRequestDispatcher(redirect);
        if (rd != null) {
            LOG.debug("ConfigServlet Redirecting to " + redirect);
            rd.forward(request, response);
        } else {
            LOG.error("ConfigServlet Error getting request dispatcher.");
        }
    }
	
	/**
	 * Checks to ensure that a provided string of the format "<Hostname>:<port>"
	 * is valid according to RFC's 952 and 1123
	 * @param hostString	The string to check validity of ("<Hostnae>:<port>")
	 * @return	True if the hostname/port is valid, false if not
	 */
	public boolean validateHostString(String hostString) {
		LOG.debug("ConfigServlet Checking validity of hostname/port: \"" + hostString + "\"");
		String[] splitHostString = hostString.split(":");
		
		// splitHostString[0] should now contain the hostname, and 
		// splitHostString[1] should contain the port
		if(splitHostString.length != 2) {
			LOG.debug("ConfigServlet \"" + hostString + "\" did not provide a valid port.");
			adapter.addNotification("\"" + hostString + "\" did not provide a valid port.");
			return false;
		}
	
		// Ensure the port number is valid
		try {
			Integer.parseInt((splitHostString[1]));
		} catch (NumberFormatException e) {
			LOG.debug("ConfigServlet Port \"" + splitHostString[1] + "\" could not be parsed "
					+ "as a valid port number");
			adapter.addNotification("Port \"" + splitHostString[1] + "\" could not be parsed "
					+ "as a valid port number");
			return false;
		}
		
		// Ensure the hostname is valid
		String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		String validHostnameRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";
		
		if(splitHostString[0].matches(validIpAddressRegex)
				|| splitHostString[0].matches(validHostnameRegex)) 
		{
			return true;
		} else {
			LOG.debug("ConfigServlet Hostname \"" + splitHostString[0] + "\" was not recognized as a valid IP or hostname.");
			adapter.addNotification("Hostname \"" + splitHostString[0] + "\" was not recognized as a valid IP or hostname.");
		}
		
		return false;
	}
}
