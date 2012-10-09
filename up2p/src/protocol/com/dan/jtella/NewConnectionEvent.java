package protocol.com.dan.jtella;

import stracciatella.Connection;

public class NewConnectionEvent extends HostsChangedEvent {

	public NewConnectionEvent(Connection source) {
		super(source);
	}

}
