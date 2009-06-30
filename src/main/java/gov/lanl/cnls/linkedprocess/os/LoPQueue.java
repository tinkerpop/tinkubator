package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * A thread-safe queue of limited capacity.  After it has reached a certain
 * size, the queue will reject additional offers.
 *
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 3:29:35 PM
 */
public class LoPQueue<T> {
    private static final Logger LOGGER = LinkedProcess.getLogger(LoPQueue.class);

    private final int capacity;
    private final Queue<T> queue;

    public LoPQueue(final int capacity) {
        this.capacity = capacity;

        queue = new LinkedList<T>();
    }

    public synchronized boolean offer(T t) {
        if (queue.size() < capacity) {
            return queue.offer(t);
        } else {
            LOGGER.warning("queue is full and has rejected an item");
            return false;
        }
    }

    public synchronized T peek() {
        return queue.peek();
    }

    public synchronized T poll() {
        return queue.poll();
    }

    public synchronized int size() {
        return queue.size();
    }

    public boolean remove(T t) {
        return queue.remove(t);
    }

    public Collection<T> asCollection() {
        return queue;
    }
}
