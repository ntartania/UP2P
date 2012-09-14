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

import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Date;

import java.text.DateFormat;

/**
 *  Basic logging
 *
 *
 */
public class Log {
	private static Log log;
	private static RandomAccessFile logFile;
	private static RandomAccessFile errorFile;

	protected Log() {}

	/**
	 *  Access the log
	 *
	 */
	public static Log getLog() {
		if (null == log) {
			log = new Log();
		}

		return log;
	}

	/**
	 *  Logs an unexcpected exception, some exceptions, such as IO problems
	 *  are to be expected
	 *
	 */
	public void logUnexpectedException(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		t.printStackTrace(new PrintWriter(stringWriter));

		logToError("Exception: \n" + stringWriter.toString());
	}

	/**
	 *  Logs an exception
	 *
	 */
	public void log(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		t.printStackTrace(new PrintWriter(stringWriter));

		logToDebug("Exception: \n" + stringWriter.toString());
	}

	/**
	 *  Logs an error
	 *
	 */
	public void logError(String txt) {
		logToError("ERROR: " + txt);
	}

	/**
	 *  Logs a warning
	 *
	 */
	public void logWarning(String txt) {
		logToDebug("WARNING: " + txt);
	}

	/**
	 *  Logs debug information
	 *
	 */
	public void logDebug(String txt) {
		// TODO whether or not this produces output a system property
		logToDebug("DEBUG: " + txt);
	}

	/**
	 *  Logs information
	 *
	 */
	public void logInformation(String txt) {
		logToDebug("INFORMATION: " + txt);
	}

	/**
	 *   Writes a log to the error log
	 *
	 */
	private void logToError(String logString) {
		if (null == errorFile) {
			try {
				errorFile = new RandomAccessFile("jtella.err", "rw");
				errorFile.seek(errorFile.length());
			}
			catch (Exception io) {
				io.printStackTrace();
			}
		}

		log(errorFile, logString);
	}

	/**
	 *   Writes a log to the debug log
	 *
	 */
	private void logToDebug(String logString) {
		if ( null == System.getProperty("JTella.Debug") ) {
			// debug mode off
			return;
		}

		if (null == logFile) {
			// open file
			try {
				logFile = new RandomAccessFile("jtella.log", "rw");
				logFile.seek(logFile.length());
			}
			catch (IOException io) {
				io.printStackTrace();
			}
		}

		log(logFile, logString);
	}

	/**
	 *  Writes the string to the log
	 *
	 */
	private void log(RandomAccessFile file, String logString) {
		Date date = new Date();
		logString =
			"["
				+ DateFormat.getDateInstance(DateFormat.SHORT).format(date)
				+ " - "
				+ DateFormat.getTimeInstance(DateFormat.FULL).format(date)
				+ "] "
				+ logString
				+ "\r\n";

		try {
			synchronized (file) {
				file.write(logString.getBytes());
			}
		}
		catch (IOException io) {
			io.printStackTrace();
		}
	}

	// test
	public static void main(String[] args) {
		getLog().logDebug("Test");
	}
}
