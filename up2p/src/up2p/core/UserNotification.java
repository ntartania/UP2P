package up2p.core;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A UserNotification represents a message that should be asynchronously
 * displayed to the user of the U-P2P system. The UserNotification object
 * stores the notification to display, as well as a "timeout" time at
 * which the notification should be removed from the system.
 * 
 * @author Alexander Craig
 */
public class UserNotification {
	/** The number of milliseconds a notification should remain in the system (1800000 = 1/2 hour) */
	public static long  DEFAULT_TIMEOUT_MILLIS = 1800000;
	
	/** The text message to display to the user */
	private String message;
	
	/** The text message of the notification with a timestamp included */
	private String timestampedMessage;
	
	/** The time at which the notification should be removed from the system */
	private Date timeoutTime;
	
	/** The time at which the notification was generated */
	private Date generationTime;
	
	/**
	 * Generates a new notification using the default timeout time (specified in
	 * DEFAULT_TIMEOUT_MILLIS).
	 * 
	 * @param message	The message the notification should contain.
	 */
	public UserNotification(String message) {
		this(message, DEFAULT_TIMEOUT_MILLIS);
	}


	/**
	 * Generates a new notification with a specified timeout interval.
	 * 
	 * @param message	The message the notification should contain.
	 * @param timeoutMillis	The number of milliseconds the notification should
	 * 										remain in the system.
	 */
	public UserNotification(String message, long timeoutMillis) {
		generationTime = new Date();
		timeoutTime = new Date(generationTime.getTime() + timeoutMillis);
		this.message = message;
		this.timestampedMessage = "[" + (new SimpleDateFormat("h:mm:ss a").format(generationTime))
			+ "] " + message;
	}
	
	/** @return The text string of the notification */
	public String getMessage() {
		return message;
	}
	
	/** @return The text string of the notification prepended with a timestamp */
	public String getTimestampedMessage() {
		return timestampedMessage;
	}
	
	/** @return true if the current time is past the timeout time of the notification */
	public boolean isExpired() {
			return ((new Date()).after(timeoutTime));
	}

}
