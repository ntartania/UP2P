package up2p.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import up2p.core.UserWebAdapter;

/**
 * Listens for the shutdown event and notifies the adapter to shutdown its
 * connections and persist any necessary data.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class ShutdownListener implements ServletContextListener {

    /**
     * Constructs and empty listener.
     */
    public ShutdownListener() {
    }

    /*
     * @see javax.servlet.ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent e) {
    }

    /*
     * @see javax.servlet.ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent e) {
    	System.out.println("Starting U-P2P shutdown.");
        Object o = e.getServletContext().getAttribute("adapter");
        if (o != null) {
            ((UserWebAdapter) o).shutdown();
        } else {
            System.err.println("Error shutting down U-P2P. WebAdapter"
                    + " not found in servlet context.");
        }
        
        // Allow shutdown event to propagate through the system before
        // Tomcat attempts to terminate
        try {
			Thread.sleep(1000);
		} catch (InterruptedException err) {
			err.printStackTrace();
		}
		
        System.out.println("U-P2P shutdown complete.");
    }

}