package protocol.com.dan.jtella;

import stracciatella.Connection;

public class DroppedConnectionEvent extends HostsChangedEvent {

	public DroppedConnectionEvent(Connection source) {
		super(source);
	}

}