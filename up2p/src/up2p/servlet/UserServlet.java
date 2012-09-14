package up2p.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * The UserServlet is responsible for managing all incoming requests which
 * deal with user authentication and account creation. This means that all
 * submissions from the account creation and login pages should be directed
 * to this servlet.
 * 
 * @author Alexander Craig
 */
public class UserServlet extends AbstractWebAdapterServlet {
	/*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	if(req.getParameter(HttpParams.UP2P_LOGOUT) != null) {
    		req.getSession().removeAttribute("up2p:loggedin");
    		((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp)
        			.encodeRedirectURL("/" + adapter.getUrlPrefix() + "/login.jsp"));
    		return;
    	}
    	
    	// Just attempt a redirect to the index page for all other GET requests
    	((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp)
    			.encodeRedirectURL("/" + adapter.getUrlPrefix()));
    }

    /*
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException 
    {
    	if(adapter.getUsername() != null) {
    		LOG.error("UserServlet: New user generation requested when peer already has user account configured.");
    		((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp)
        			.encodeRedirectURL("login.jsp"));
    		return;
    	}
    	
    	if(req.getParameter(HttpParams.UP2P_USERNAME) != null
    			&& req.getParameter(HttpParams.UP2P_PASSWORD) != null) 
    	{
    		LOG.info("UserServlet: Got request for new user account with name: " + req.getParameter("up2p:username"));
    		generateUserAccount(req, resp);
    		req.getSession().setAttribute("up2p:loggedin", "true");
    		((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp)
        			.encodeRedirectURL("/" + adapter.getUrlPrefix()));
    	} else {
    		LOG.error("UserServlet: New account creation request did not specify username and password.");
    		((HttpServletResponse)resp).sendRedirect(((HttpServletResponse)resp)
        			.encodeRedirectURL("newaccount.jsp"));
    	}
    }
    
    private void generateUserAccount(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
    	LOG.debug("UserServlet: Generating hash...");
		HexBinaryAdapter hexConverter = new HexBinaryAdapter();
		
		// Get the salt bytes
		String saltHex = adapter.getSaltHex();
		byte[] saltBytes = hexConverter.unmarshal(saltHex);
		
		// Generate and save the actual hash using 1000 iterations
		// of SHA-1
		byte[] passwordBytes = req.getParameter(HttpParams.UP2P_PASSWORD).getBytes("UTF-8");
		byte[] saltedPassBytes = new byte[saltBytes.length + passwordBytes.length];
		System.arraycopy(saltBytes, 0, saltedPassBytes, 0, 8);
		System.arraycopy(passwordBytes, 0, saltedPassBytes, 8, passwordBytes.length);
		
		try {
			MessageDigest sha1Encrypter = MessageDigest.getInstance("SHA-1");
			byte[] sha1Hash = sha1Encrypter.digest(saltedPassBytes);
			
			// Perform the hash digest 999 more times (for a total of 1000 iterations)
			for(int i = 1; i < 1000; i++) {
				sha1Hash = sha1Encrypter.digest(sha1Hash);
			}
			
			// Get the hex string for the hash
			String hashHex = hexConverter.marshal(sha1Hash);
			
			// Write out the hash to file
			adapter.setUser(req.getParameter(HttpParams.UP2P_USERNAME), hashHex);
			LOG.info("Generated new user: " + adapter.getUsername());
		} catch (NoSuchAlgorithmException e) {
			LOG.error("SHA-1 digest not supported on this platform, could not create user account.");
		}
    }
}
