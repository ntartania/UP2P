/**
 * @author Daniel Meyers
 * Created On:    Jan 8, 2004
 * Last Modified: Jan 8, 2004
 * 
 */
package protocol.com.dan.jtella;

import java.util.EventListener;

/**
 * Interface for classes that wish to be notified when the list of currently connected hosts
 * changes in some way.
 */
public interface ConnectedHostsListener extends EventListener {

	/**
	 * Called whenever the list of currently connected hosts changes in some way
	 * 
	 * @param he HostsChangedEvent holding the source of the Event
	 */
	public void hostsChanged(HostsChangedEvent he);
}
