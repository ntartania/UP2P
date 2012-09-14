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

import java.util.LinkedList;

/**
 *  A Queue supporting standard enqueue/dequeue operations
 *  with a maximum permitted size. Having a maximum size
 *  guards against unrestricted memory usage
 *
 */
public class BoundedQueue extends java.lang.Object {
	private int maxSize;
	private LinkedList queue;

	/**
	 *  Constructs a <code>BoundedQueue</code> with a max size
	 *
	 *  @param maxSize size for queue
	 */
	public BoundedQueue(int maxSize) {
		this.maxSize = maxSize;
		queue = new LinkedList();
	}

	/**
	 * Enqueue an object
	 *
	 * @return true if enqueue worked, false if queue is full
	 */
	public synchronized boolean enqueue(Object o) {
		if (queue.size() > maxSize) {
			return false;
		}

		queue.add(o);
		return true;
	}

	/**
	 * Removes an object from the queue
	 *
	 */
	public synchronized Object dequeue() {
		return queue.remove(0);
	}

	/**
	 *  Check if the queue is empty
	 *
	 *  @return true if empty, false otherwise
	 */
	public synchronized boolean empty() {
		return queue.size() == 0;
	}
}
