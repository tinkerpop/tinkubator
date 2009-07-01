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

    private final Queue<T> queue;
    private final Object blockingMonitor = "";

    public SimpleBlockingQueue() {
        queue = new LinkedList<T>();
    }

    // Synchronized so that threads have to wait their turn for a call.
    public synchronized T take() throws InterruptedException {
        if (0 == queue.size()) {
            synchronized (blockingMonitor) {
                blockingMonitor.wait();
            }
        }

        return queue.poll();
    }

    public boolean offer(final T t) {
        boolean b;
        synchronized (queue) {
            b = queue.offer(t);
        }

        notifyMonitor();

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

    private void notifyMonitor() {
        synchronized (blockingMonitor) {
            blockingMonitor.notify();
        }
    }
}
