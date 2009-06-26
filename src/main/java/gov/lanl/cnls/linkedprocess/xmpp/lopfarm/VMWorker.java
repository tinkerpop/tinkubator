package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * An object with an internal thread which is capable of executing scripts
 * (via ScriptEngine) within that thread, pausing them if they take longer
 * to execute than a given time slice, then resuming execution at a later
 * time.
 * Note: relies on the deprecated methods Thread.suspend and Thread.resume.
 * Deadlocks are avoided by preventing the internal thread from obtaining a
 * lock on any object apart from two special monitors, neither of which can
 * be locked at the time the thread is suspended.
 *
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

    /**
     * Creates a new virtual machine worker.
     * @param scriptEngine the ScriptEngine with which to evaluate expressions
     * @param resultHandler a handler for job results
     */
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

    /**
     * @return whether the worker currently has work to do (either a suspended
     * job in progress, or pending jobs in the queue)
     */
    public synchronized boolean canWork() {
        return (State.IDLE_READYTOWORK == state
                || State.BUSY_SUSPENDED == state);
    }

    /**
     * Adds a job to the queue.
     * Note: this alone does not cause the worker to become active.
     * @param job the job to add
     */
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

    /**
     * Work on the current job for at most a given window of time.  If the job
     * is finished during this time, its result will be handled.  Otherwise, the
     * job will be suspended, to be resumed on a subsequent call to work().
     * Note: This method should only be called when the value of canWork() is true.
     * @param timeout the length of the time window
     */
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

                // Load the next job, if there is one.
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
    public synchronized void cancel() {
        switch (state) {
            case BUSY_SUSPENDED:
                workerThread.interrupt();
                // Put the current job in the queue to be cancelled.
                jobQueue.offer(currentJob);
                break;
            case IDLE_NOJOBS:
                // Nothing to do.
                break;
            case IDLE_READYTOWORK:
                // Put the current job in the queue to be cancelled.
                jobQueue.offer(currentJob);
                break;
            case TERMINATED:
                // Been there, done that.
                return;
            default:
                throw new IllegalStateException("cannot cancel from state: " + state);
        }

        state = State.TERMINATED;

        // Cancel any jobs in the queue.
        for (Job j : jobQueue.asCollection()) {
            JobResult cancelledJob = new JobResult(j);
            resultHandler.handleResult(cancelledJob);
        }
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
                    // At this point, the only possible state is IDLE_RESULTISREADY

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
