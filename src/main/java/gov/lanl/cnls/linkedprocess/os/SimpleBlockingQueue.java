package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 26, 2009
 * Time: 4:28:59 PM
 */
public class SimpleBlockingQueue<T> {
    private static final Logger LOGGER
            = LinkedProcess.getLogger(SimpleBlockingQueue.class);

    // Larger value avoids excessive context-switching overhead while waiting.
    // Smaller value decreases the wait if the loop misses its notify event.
    private static final long WAIT_MILLIS = 100;

    private final Queue<T> queue;
    private final Object blockingMonitor = "";

    public SimpleBlockingQueue() {
    	//LOGGER.info("CONSTRUCTED");
        queue = new LinkedList<T>();
    }

    // Synchronized so that threads are served in FIFO order.
    public synchronized T take() throws InterruptedException {
        //LOGGER.info("" + Thread.currentThread() + ": taking from queue");

        // Break out when the queue is non-empty.
        while (true) {
            synchronized (queue) {
                if (0 < queue.size()) {
                    //LOGGER.info("" + Thread.currentThread() + ": got it!");
                    return queue.poll();
                }
            }

            //LOGGER.info("" + Thread.currentThread() + ": waiting on empty queue");
            synchronized (blockingMonitor) {
                // The limited wait ensures that the waiting thread will
                // eventually be able to poll the queue once it is
                // non-empty, even if timing caused the notify event
                // to be missed.
                blockingMonitor.wait(WAIT_MILLIS);
            }
        }
    }

    public boolean offer(final T t) {
        boolean b;
        synchronized (queue) {
            b = queue.offer(t);
        }

        if (b) {
            // Notify any currently waiting thread that an item is available in
            // the queue.
            synchronized (blockingMonitor) {
                blockingMonitor.notify();
            }
        }

        return b;
    }

    public boolean offerDistinct(final T t) {
        boolean b;
        synchronized (queue) {
            b = !queue.contains(t) && queue.offer(t);
        }

        if (b) {
            // Notify any currently waiting thread that an item is available in
            // the queue.
            synchronized (blockingMonitor) {
                blockingMonitor.notify();
            }
        }

        return b;
    }

    public void clear() {
        synchronized (queue) {
            queue.clear();
        }
    }

    public boolean contains(final T t) {
        synchronized (queue) {
            return queue.contains(t);
        }
    }

    public int size() {
        synchronized (queue) {
            return queue.size();
        }
    }

    public boolean remove(final T t) {
        synchronized (queue) {
            return queue.remove(t);
        }
    }
}
