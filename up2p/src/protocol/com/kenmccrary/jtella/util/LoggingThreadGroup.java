/*
 * Copyright (C) 2000-2001  Ken McCrary
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Email: jkmccrary@yahoo.com
 */
package protocol.com.kenmccrary.jtella.util;

/**
 *  A threadgroup to log unexpected exceptions
 *
 */
public class LoggingThreadGroup extends ThreadGroup {
	public LoggingThreadGroup() {
		super("JTella-ThreadGroup");
	}

	/**
	 *  Logs an uncaught exception
	 *
	 */
	public void uncaughtException(Thread thread, Throwable throwable) {
		if (throwable instanceof ThreadDeath) {
			return;
		}

		System.out.println("*uncaught exception*");
		Log.getLog().logUnexpectedException(throwable);
	}

}
