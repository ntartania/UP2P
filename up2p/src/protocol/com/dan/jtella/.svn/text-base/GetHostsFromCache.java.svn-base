/**
 * @author Daniel Meyers
 * Created On:    Dec 15, 2003
 * Last Modified: Dec 15, 2004
 * 
 */

package protocol.com.dan.jtella;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.util.Vector;

import org.apache.log4j.Logger;

import protocol.com.kenmccrary.jtella.HostCache;
import protocol.com.kenmccrary.jtella.ConnectionList;
import protocol.com.kenmccrary.jtella.ConnectionData;

import protocol.com.dan.jtella.GWebCaches;

/**
 * Connects to GWebCaches for bootstrapping. Downloads initial hosts for connection attempts.
 */
public class GetHostsFromCache extends Thread {
	
	// Name of logger used
	public static final String LOGGER = "protocol.com.dan.jtella";
	// Instance of logger
	public static Logger LOG = Logger.getLogger(LOGGER);
	
	
	/***********************************************
	 * File where the GWebCaches should be listed!!
	 * **********************************************/
	private static final String GWEBCACHES_FILE = "gwebcaches.list";
	
	// For requesting hosts, not any other data
	private static final String hostRequestString = "hostfile=1";
	// For requesting urls of other GWebCaches, not any other data
	private static final String urlRequestString = "urlfile=1";
	// True if the object should stop running
	private boolean shutdownFlag = false;
	// To fill with hosts from GWebCaches
	private HostCache hostCache;
	// Lists currently connected hosts
	private ConnectionList connectionList;
	// Contains useful information
	private ConnectionData connectionData;
	// Vector of GWebCache addresses to attempt connections on
	private Vector<String> urls;

	/**
	 * Sets up the GetHostsFromCache instance.
	 * 
	 * @param hostCache The HostCache to put hosts gained fom GWebCaches into
	 * @param connectionList The list of currently connected hosts
	 */
	public GetHostsFromCache(
		HostCache hostCache,
		ConnectionList connectionList,
		ConnectionData connectionData) {

		super("GetHostsFromCache");
		this.hostCache = hostCache;
		this.connectionList = connectionList;
		this.connectionData = connectionData;
		this.urls = new Vector<String>(1, 1);

		// Ensure we don't spend too long attempting to connect 
		// to the GWebCache if there is a problem
		System.setProperty("sun.net.client.defaultConnectTimeout", "4000");
		System.setProperty("sun.net.client.defaultReadTimeout", "1000");
	}

	/**
	 * The main running loop. Iterates through the list of known GWebCaches and retrieves
	 * host IP:port pairs from them
	 */
	public void run() {
		String host = null;
		int position = 0;
		URL GWebCache = null;
		HttpURLConnection connection = null;
		BufferedReader incomingData = null;
		boolean error = false;
		boolean fileNotFound = false;
		boolean fileError = false;

		// Gain access to gwebcaches.list file
		File gWebCachesFile = new File(GWEBCACHES_FILE);

		BufferedReader fileReader = null;

		try {
			fileReader = new BufferedReader(new FileReader(gWebCachesFile));
		}
		catch (FileNotFoundException fnfe) {
			LOG.error("gwebcaches.list does not exist. Using default list.");
			String[] urlsArray = GWebCaches.getDefaultGWebCaches(); //this supposes that a list is hardcoded in file GWebCaches.java
			for (int i = 0; i < urlsArray.length; i++) {
				urls.addElement(urlsArray[i]);
			}

			fileNotFound = true;
		}

		//Read in all the URLs
		if (!fileNotFound) {
			String currentURL = null;
			try {
				currentURL = fileReader.readLine();
			}
			catch (IOException ioe) {
				LOG.error(ioe);
			}

			while (currentURL != null) {
				try {
					urls.addElement(currentURL);
					currentURL = fileReader.readLine();
				}
				catch (IOException ioe) {
					LOG.error(ioe);
				}
			}
		}

		// make sure that some urls exist
		if (urls.size() == 0) {
			shutdownFlag = true;
			LOG.error("No urls exist in gwebcaches.list");
		}
		
		while (!shutdownFlag) {
			host = null;
			connection = null;
			error = false;

			// Create a URL instance representing the GWebCache
			try {
				GWebCache =
					new URL(
						(((String)urls.elementAt(position)).split(" "))[0] +
						"?" +
						"client=JTEL&version=0.8&" +
						hostRequestString);
			}
			catch (MalformedURLException mue) {
				LOG.error(
					"Malformed URL in Default GWebCache list: "
					+ (((String)urls.elementAt(position)).split(" "))[0]);
				if (handleFailure(position)) position = 0;
				error = true;
			}

			// Attempt to set up a connection to the GWebCache using the URL instance
			if (!error) {
				try {
					connection = (HttpURLConnection)GWebCache.openConnection();
					connection.setRequestProperty("Connection", "close");

					LOG.info(
						"Getting input stream for "
						+ (((String)urls.elementAt(position)).split(" "))[0]);
					incomingData =
						new BufferedReader(new InputStreamReader(connection.getInputStream()));
				}
				catch (Exception e) {
					LOG.error("Error getting InputStream");
					if (handleFailure(position)) position = 0;
					try {
						incomingData.close();
					}
					catch (Exception e2) {}
					connection.disconnect();
					error = true;
				}
			}

			// Add the hosts read from the GWebCache to the HostCache
			if (!error) {
				LOG.info(
					"Adding hosts from GWebCache "
					+ (((String)urls.elementAt(position)).split(" "))[0]
					+ " to hostCache");

				try {
					host = incomingData.readLine();

					if (host != null) {
						if (host.toLowerCase().startsWith("error")) {
							LOG.error("'" + host + "' returned");
							if (handleFailure(position)) position = 0;
							try {
								incomingData.close();
							}
							catch (IOException ioe2) {}
							connection.disconnect();
							error = true;
						}
						else {
							while ((host != null) && (!error)) {
								addHost(host);
								try {
									host = incomingData.readLine();
								}
								catch (IOException ioe2) {
									LOG.error(
										"I/O Exception occurred "
										+ "reading from the BufferedReader #1\r\n"
										+ ioe2);
									// Don't handle failure cos this is a means of exiting the
									// loop a lot of the time
									try {
										incomingData.close();
									}
									catch (IOException ioe3) {}
									connection.disconnect();
									error = true;
								}
							}
						}
					}
				}
				catch (IOException ioe) {
					LOG.error(
						"I/O Exception occurred reading from the BufferedReader #2\r\n" + ioe);
					if (handleFailure(position)) position = 0;
					try {
						incomingData.close();
					}
					catch (IOException ioe2) {}
					connection.disconnect();
					error = true;
				}
			}

			// If no error, update cache with IP of this servent,
			// and shift to end of list, so that caches with errors get sifted out as
			// quickly as possible.
			// Also reset failures value if no error
			if (!error) {
				String[] cache = ((String)urls.remove(position)).split(" ");
				
				updateCache(cache[0]);
				
				String newCache = cache[0] + " 0";
				urls.add(newCache);
			}
			
			if ((!error) && (urls.size() < 10)) {
				getCacheAddresses((((String)urls.elementAt(position)).split(" "))[0]);
			}

			// Endless loop. Keep retrying connections
			if (urls.size() == 0) {
				shutdownFlag = true;
				LOG.error("No urls were successful");
			} if (position == (urls.size() - 1))
				position = 0;
			else position++;

			// If the HostCache contains more elements than required to have the requested number
			// of connections then loop here and don't keep trying to add more elements from a 
			// GWebCache
			while ((hostCache.size() > connectionData.getOutgoingConnectionCount())
				&& (!shutdownFlag)
				&& (!error)) {

				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException ie) {
					if (shutdownFlag) {
						break;
					}
				}
			}
		}

		try {
			if (fileReader != null) {
				fileReader.close();
			}
		}
		catch (IOException ioe) {}

		if (urls.size() > 0) {
			BufferedWriter fileWriter = null;

			try {
				fileWriter = new BufferedWriter(new FileWriter(gWebCachesFile));
			}
			catch (IOException ioe) {
				LOG.error(ioe);
				fileError = true;
			}

			if (!fileError) {
				for (int i = 0; i < urls.size(); i++) {
					try {
						fileWriter.write((String)urls.get(i));
						fileWriter.newLine();
					}
					catch (IOException ioe) {
						LOG.error(ioe);
					}
				}
			}

			try {
				fileWriter.close();
			}
			catch (IOException ioe) {}
		}
	}

	/**
	 * Stops the GetHostsFromCache instance from running
	 */
	public void shutdown() {
		shutdownFlag = true;
		interrupt();
	}

	/**
	 * Adds a host to the HostCache.
	 * 
	 * @param hostDetails A String representing the host, formatted as "hostip:port"
	 */
	private void addHost(String hostDetails) {
		LOG.info(hostDetails);
		String ipAddress = "";
		int port = 0;

		if (hostDetails.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}")) {
			String[] hostParts = hostDetails.split(":", 2);

			try {
				ipAddress = hostParts[0];
				port = Integer.parseInt(hostParts[1]);
			}
			catch (Exception e) {
				LOG.error(e);
				return;
			}
			hostCache.addHost(ipAddress, port);
		}
	}

	/**
	 * Handles failure regarding the access of webcaches. If a webcache fails 3 times
	 * or more. It will be removed from the list of webcaches.
	 * 
	 * @param position position of failed webcache
	 * @return true if the webcache was removed
	 */
	private boolean handleFailure(int position) {
		String gWebCache = (((String)urls.elementAt(position)).split(" "))[0];
		String failures = (((String)urls.elementAt(position)).split(" "))[1];
		int failuresInt = Integer.parseInt(failures);
		failuresInt++;

		// If this GWebCache has failed 5 times in a row, remove it
		if (failuresInt >= 3) {
			urls.removeElementAt(position);
			
			return true;
		}
		// Else increment the number of times it has failed
		else {
			urls.removeElementAt(position);
			urls.add(position, gWebCache + " " + failuresInt);
			
			return false;
		}
	}

	// Untested due to cache returning 'OK' and nothing else.
	private void getCacheAddresses(String knownGoodCache) {
		URL GWebCache = null;
		boolean error = false;
		HttpURLConnection connection = null;
		BufferedReader incomingData = null;
		String url = null;
		
		try {
			GWebCache =
				new URL(
					knownGoodCache +
					"?" +
					"client=JTEL&version=0.8&" +
					urlRequestString);
		}
		catch (MalformedURLException mue) {
			LOG.error("Malformed URL : " + knownGoodCache);
			error = true;
		}
		
		if (!error) {
			try {
				connection = (HttpURLConnection)GWebCache.openConnection();
				connection.setRequestProperty("Connection", "close");

				LOG.info("Getting input stream for " + knownGoodCache);
				incomingData =
					new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}
			catch (Exception e) {
				LOG.error("Error getting InputStream");
				try {
					incomingData.close();
				}
				catch (Exception e2) {}
				connection.disconnect();
				error = true;
			}
		}
		
		if (!error) {
			LOG.info(
				"Adding cache URLs from GWebCache " + knownGoodCache + " to hostCache");

			try {
				url = incomingData.readLine();

				if (url != null) {
					if (url.toLowerCase().startsWith("error")) {
						LOG.error("'" + url + "' returned");
						try {
							incomingData.close();
						}
						catch (IOException ioe2) {}
						connection.disconnect();
						error = true;
					}
					else {
						while ((url != null) && (!error)) {
							
							if ((!url.startsWith("http://")) || (!url.startsWith("www."))) {
								urls.addElement(url + " " + 0);
							}
							
							try {
								url = incomingData.readLine();
							}
							catch (IOException ioe2) {
								LOG.error(
									"I/O Exception occurred "
									+ "reading from the BufferedReader #3\r\n"
									+ ioe2);
								try {
									incomingData.close();
								}
								catch (IOException ioe3) {}
								connection.disconnect();
								error = true;
							}
						}
					}
				}
			}
			catch (IOException ioe) {
				LOG.error(
					"I/O Exception occurred reading from the BufferedReader #4\r\n" + ioe);
				try {
					incomingData.close();
				}
				catch (IOException ioe2) {}
				connection.disconnect();
				error = true;
			}
		}
	}
	
	private void updateCache(String knownGoodCache) {
		URL GWebCache = null;
		boolean error = false;
		HttpURLConnection connection = null;
		BufferedReader incomingData = null;
		String response = null;
		
		try {
			GWebCache =
				new URL(
					knownGoodCache +
					"?" +
					"client=JTEL&version=0.8&" +
					"ip=" + connectionData.getGatewayIP() + 
					":" + connectionData.getIncomingPort());
		}
		catch (MalformedURLException mue) {
			LOG.error("Malformed URL : " + knownGoodCache);
			error = true;
		}
		
		if (!error) {
			try {
				connection = (HttpURLConnection)GWebCache.openConnection();
				connection.setRequestProperty("Connection", "close");

				LOG.info("Getting input stream for " + knownGoodCache);
				incomingData =
					new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}
			catch (Exception e) {
				LOG.error("Error getting InputStream");
				try {
					incomingData.close();
				}
				catch (Exception e2) {}
				connection.disconnect();
				error = true;
			}
		}
		
		if (!error) {
			try {
				response = incomingData.readLine();
				
				while ((response != null) && (!error)) {
					LOG.info("updateCache " + knownGoodCache + ": " + response);
					response = incomingData.readLine();
				}
			}
			catch (IOException ioe) {}
		}
	}
}