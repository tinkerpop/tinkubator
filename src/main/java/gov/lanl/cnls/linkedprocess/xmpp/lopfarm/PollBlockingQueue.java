package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Author: josh
 * Date: Jun 26, 2009
 * Time: 4:28:59 PM
 */
public class PollBlockingQueue<T> {
    private static final Logger LOGGER
            = LinkedProcess.getLogger(PollBlockingQueue.class);

    private final Queue<T> queue;
    private boolean blocking;
    private final Object pollBlockingMonitor = "";

    public PollBlockingQueue() {
        queue = new LinkedList<T>();
        blocking = true;
    }

    // Synchronized so that threads have to wait their turn for a call.
    public synchronized T poll() {
        if (blocking && 0 == queue.size()) {
            synchronized (pollBlockingMonitor) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    LOGGER.error("thread interrupted unexpectedly in queue");
                    System.exit(1);
                }
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

    public void stopBlocking() {
        blocking = false;
        notifyMonitor();
    }

    public boolean remove(final T t) {
        synchronized (queue) {
            return queue.remove(t);
        }
    }

    private void notifyMonitor() {
        synchronized (pollBlockingMonitor) {
            pollBlockingMonitor.notify();
        }
    }
}
