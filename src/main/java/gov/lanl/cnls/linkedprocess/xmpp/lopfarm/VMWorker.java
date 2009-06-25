package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:15:41 PM
 */
public class VMWorker {
    private enum State {
        BUSY_ACTIVE,
        BUSY_SUSPENDED,
        IDLE_NOJOBS,
        IDLE_READYTOWORK,
        IDLE_RESULTISREADY,
        TERMINATED
    }

    private static final Logger LOGGER = LinkedProcess.getLogger(VMWorker.class);

    private final LoPQueue<Job> jobQueue;
    private final VMScheduler.VMResultHandler resultHandler;
    private final ScriptEngine scriptEngine;
    private final Thread workerThread;
    private State state;

    private Job currentJob;
    private JobResult latestResult;

    private final Object
            timeoutMonitor = "",
            workerWaitMonitor = "";

    public VMWorker(final ScriptEngine scriptEngine,
                    final VMScheduler.VMResultHandler resultHandler) {
        this.scriptEngine = scriptEngine;
        this.resultHandler = resultHandler;

        int capacity = new Integer(LinkedProcess.getProperties().getProperty(
                LinkedProcess.MESSAGE_QUEUE_CAPACITY));
        jobQueue = new LoPQueue<Job>(capacity);

        workerThread = new Thread(new WorkerRunnable());
        workerThread.start();

        state = State.IDLE_NOJOBS;
    }

    public synchronized boolean canWork() {
        return (State.IDLE_READYTOWORK == state
                || State.BUSY_SUSPENDED == state);
    }

    // Note: adding a job does not necessarily cause the worker to become active.
    public synchronized void addJob(final Job job) {
        switch (state) {
            case IDLE_NOJOBS:
                currentJob = job;
                state = State.IDLE_READYTOWORK;
                break;
            case IDLE_READYTOWORK:
                jobQueue.offer(job);
                break;
            case BUSY_SUSPENDED:
                jobQueue.offer(job);
                break;
            default:
                throw new IllegalStateException("jobs cannot be added in state: " + state);
        }
    }

    public synchronized void work(final long timeout) {
        switch (state) {
            case IDLE_READYTOWORK:
                synchronized (timeoutMonitor) {
                    timeoutMonitor.notify();
                }
            case BUSY_SUSPENDED:
                workerThread.resume();
                break;
            default:
                throw new IllegalStateException("can't begin new work in state: " + state);
        }

        // Break out when the time slice has expired or the monitor has been notified.
        try {
            synchronized (timeoutMonitor) {
                timeoutMonitor.wait(timeout);
            }
        } catch (InterruptedException e) {
            LOGGER.error("interrupted unexpectedly");
            System.exit(1);
        }

        // Suspend the thread regardless of what state we're in.
        workerThread.suspend();

        switch (state) {
            case TERMINATED:
                return;
            case BUSY_ACTIVE:
                state = State.BUSY_SUSPENDED;
                return;
            case IDLE_RESULTISREADY:
                resultHandler.handleResult(latestResult);
                // Advance to the wait()
                workerThread.resume();
                if (0 == jobQueue.size()) {
                    state = State.IDLE_NOJOBS;
                } else {
                    currentJob = jobQueue.poll();
                    state = State.IDLE_READYTOWORK;
                }
                break;
            default:
                throw new IllegalStateException("state should not occur after work window: " + state);
        }
    }

    /**
     * Terminates execution of the current job and prevents further jobs.
     * This method may be called at any time, in any thread.
     */
    public void cancel() {
        state = State.TERMINATED;
        workerThread.interrupt();
        timeoutMonitor.notify();
    }

    ////////////////////////////////////////////////////////////////////////////

    private void evaluate(final Job request) {
        try {
            Object returnObject = scriptEngine.eval(request.getExpression());

            if (null != returnObject && !(returnObject instanceof String)) {
                LOGGER.error("object returned by ScriptEngine.eval is not of the expected type java.lang.String");
                System.exit(1);
            }

            String returnvalue = (null == returnObject)
                    ? "" : (String) returnObject;

            yieldResult(request, returnvalue);
        } catch (ScriptException e) {
            yieldError(request, e);
        }
    }

    private void yieldError(final Job job,
                            final ScriptException exception) {
        latestResult = new JobResult(job, exception);
        state = State.IDLE_RESULTISREADY;
    }

    private void yieldResult(final Job job,
                             final String resultExpression) {
        latestResult = new JobResult(job, resultExpression);
        state = State.IDLE_RESULTISREADY;
    }

    private class WorkerRunnable implements Runnable {

        public void run() {
            // Break out when the worker is terminated.
            while (State.TERMINATED != state) {
                // Two states are possible here: IDLE_READYTOWORK and (on the first iteration only) IDLE_NOJOBS
                if (State.IDLE_READYTOWORK == state) {
                    state = State.BUSY_ACTIVE;

                    evaluate(currentJob);
                    // At this point, two states are possible: IDLE_RESULTISREADY and IDLE_ERRORISREADY

                    synchronized (timeoutMonitor) {
                        // Notify the parent thread that a result is available.
                        timeoutMonitor.notify();
                    }
                }

                try {
                    synchronized (workerWaitMonitor) {
                        workerWaitMonitor.wait();
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("worker thread has been interrupted unexpectedly");
                    return;
                }
            }
        }
    }
}
