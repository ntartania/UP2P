package up2p.tspace;

import java.util.LinkedList;
import java.util.List;

import lights.interfaces.ITuple;


/**
 * Inbox is a class that implements a synchronized message queue, 
 * of the producer / consumer pattern. Producers are TS Scanners, and the consumer 
 * is the main thread of this worker 
 * @author adavoust
 *
 */

public class WorkerInbox {
	//queue of tuples in the order that they arrived (implement as LinkedList?)
	private LinkedList<TuplePair> todoQueue; 

	/**
	 * constructor. All that is needed is a linked list for the message queue
	 */
	public WorkerInbox(){
		todoQueue = new LinkedList<TuplePair>();	
	}

	public synchronized TuplePair getFirst() {
		while (todoQueue.isEmpty()) {
			try {
				// long sleepStart = System.currentTimeMillis();
				// System.out.println("<----- SLEEP: " + name + " (worked " + (System.currentTimeMillis() - lastWakeup) + " millis)");

				// Wait here until the inbox contains any tuples to process
				wait();

				// System.out.println("=====> AWOKE: " + name + " (slept " + (System.currentTimeMillis() - sleepStart) + " millis)");
				// LOG.debug(name + " waking up!");
				// lastWakeup = System.currentTimeMillis();
			} catch (InterruptedException e) { }
		}

		return todoQueue.removeFirst();

		// Finished up here so notify waiting threads.
		//notifyAll(); not necessary?
		//return cavity;
	}
	
	public synchronized List<TuplePair> getAll() {
		while (todoQueue.isEmpty()) {
			try {
				// long sleepStart = System.currentTimeMillis();
				// System.out.println("<----- SLEEP: " + name + " (worked " + (System.currentTimeMillis() - lastWakeup) + " millis)");

				// Wait here until the inbox contains any tuples to process
				wait();

				// System.out.println("=====> AWOKE: " + name + " (slept " + (System.currentTimeMillis() - sleepStart) + " millis)");
				// LOG.debug(name + " waking up!");
				// lastWakeup = System.currentTimeMillis();
			} catch (InterruptedException e) { }
		}

		List<TuplePair> templist = todoQueue;
		todoQueue = new LinkedList<TuplePair>();
		return templist;

		// Finished up here so notify waiting threads.
		//notifyAll(); not necessary?
		//return cavity;
	}


	public synchronized void put(List<TuplePair> tuplepairs ) {
		//appends the new tuples at the end of the queue
		todoQueue.addAll(tuplepairs);
		notifyAll(); //notify the waiting thread. At this point there's only one, but perhaps there will be several
	}


}
