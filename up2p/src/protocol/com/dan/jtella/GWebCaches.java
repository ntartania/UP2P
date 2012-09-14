/**
 * @author Daniel Meyers
 * Created On:    Dec 15, 2003
 * Last Modified: Dec 15, 2003
 * 
 */

package protocol.com.dan.jtella;

/**
 * Contains a list of GWebCaches that are to be tried in a servent bootstrap attempt.
 */
public class GWebCaches {

	private static final String[] urls =
		new String[] {}; //place a list of GWebCache URLS in this list if you want to use it.

	/**
	 * Gets the list of GWebCache addresses
	 * 
	 * @return String array of GWebCache HTTP addresses
	 */
	public static String[] getDefaultGWebCaches() {
		return urls;
	}
}
