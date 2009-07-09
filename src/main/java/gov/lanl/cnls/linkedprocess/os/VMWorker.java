package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.errors.JobAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException;
import gov.lanl.cnls.linkedprocess.security.VMSandboxedThread;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * An object with an internal thread which is capable of executing scripts
 * (via ScriptEngine) within that thread, pausing them if they take longer
 * to execute than a given time slice, then resuming execution at a later
 * time.
 * Note: relies on the deprecated methods Thread.suspend and Thread.resume.
 * Deadlocks are avoided by preventing the internal thread from obtaining a
 * lock on any object apart from two special monitors, neither of which can
 * be locked at the time the thread is suspended.
 * <p/>
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:15:41 PM
 */
public class VMWorker {
    public static final VMWorker SCHEDULER_TERMINATED_SENTINEL = new VMWorker();

    private enum Status {
        ACTIVE_INPROGRESS,
        ACTIVE_SUSPENDED,
        IDLE_WAITING,
        IDLE_FINISHED,
        TERMINATED
    }

    private static final Logger LOGGER = LinkedProcess.getLogger(VMWorker.class);

    private final BlockingQueue<Job> jobQueue;
    private final VMScheduler.VMResultHandler resultHandler;
    private final ScriptEngine scriptEngine;
    private final Thread workerThread;
    private final long maxTimeSpentPerJob;

    private Status status;

    private Job latestJob;
    private JobResult latestResult;

    private final Object
            timeoutMonitor = "",
            workerWaitMonitor = "";

    private static long threadID = 0;

    private static synchronized String nextThreadName() {
        return "LOP VM worker thread #" + ++threadID;
    }

    /**
     * Dummy constructor to create a sentinel value in VMScheduler.
     */
    private VMWorker() {
        jobQueue = null;
        resultHandler = null;
        scriptEngine = null;
        workerThread = null;
        maxTimeSpentPerJob = 0;
    }

    /**
     * Creates a new virtual machine worker.
     *
     * @param scriptEngine  the ScriptEngine with which to evaluate expressions
     * @param resultHandler a handler for job results
     */
    public VMWorker(final ScriptEngine scriptEngine,
                    final VMScheduler.VMResultHandler resultHandler) {
        LOGGER.info("instantiating VMWorker using engine type '"
                + scriptEngine.getFactory().getEngineName() + "'");

        this.scriptEngine = scriptEngine;
        this.resultHandler = resultHandler;

        maxTimeSpentPerJob = new Long(LinkedProcess.getProperties().getProperty(
                LinkedProcess.MAX_TIME_SPENT_PER_JOB));
        
        int capacity = new Integer(LinkedProcess.getProperties().getProperty(
                LinkedProcess.MESSAGE_QUEUE_CAPACITY));
        jobQueue = new LinkedBlockingQueue<Job>(capacity);

        workerThread = new VMSandboxedThread(new WorkerRunnable(), nextThreadName());
        workerThread.start();

        status = Status.IDLE_WAITING;
    }

    /**
     * @return whether the worker currently has work to do (either a suspended
     *         job in progress, or pending jobs in the queue)
     */
    public synchronized boolean canWork() {
        switch (status) {
            case ACTIVE_SUSPENDED:
                // Still working on the last job.
                return true;
            case IDLE_WAITING:
                // Are there any pending jobs?
                return 0 != jobQueue.size();
            default:
                throw new IllegalArgumentException("can't check for new work in status: " + status);
        }
    }

    /**
     * Adds a job to the queue.
     * Note: this alone does not cause the worker to become active.
     *
     * @param job the job to add
     * @return whether the job has been added to the worker's queue (if not,
     *         then the queue is full)
     * @throws gov.lanl.cnls.linkedprocess.os.errors.JobAlreadyExistsException if a job with the given ID is already active or in the queue
     */
    public synchronized boolean addJob(final Job job) throws JobAlreadyExistsException {
        LOGGER.info("adding job: " + job);

        switch (status) {
            case ACTIVE_SUSPENDED:
            case IDLE_WAITING:
                if (jobQueue.contains(job)) {
                    throw new JobAlreadyExistsException(job);
                }
                return jobQueue.offer(job);
            default:
                throw new IllegalStateException("can't add jobs in status: " + status);
        }
    }

    /**
     * Works on the current job for at most a given window of time.  If the job
     * is finished during this time, its result will be handled.  Otherwise, the
     * job will be suspended, to be resumed on a subsequent call to work().
     * Note: This method should only be called when the value of canWork() is true.
     *
     * @param timeout the length of the time window
     * @return whether the worker is now idle (has no more work to do)
     */
    public synchronized boolean work(final long timeout) {
        LOGGER.fine("working...");

        switch (status) {
            case ACTIVE_SUSPENDED:
                status = Status.ACTIVE_INPROGRESS;
                resumeWorkerThread();
                break;
            case IDLE_WAITING:
                if (0 == jobQueue.size()) {
                    throw new IllegalStateException("no jobs available. Call canWork() to avoid this condition.");
                }
                latestJob = jobQueue.poll();

                status = Status.ACTIVE_INPROGRESS;
                notifyWorkerThread();
                break;
            default:
                throw new IllegalStateException("can't begin new work in status: " + status);
        }

        // Break out when the time slice has expired or the monitor has been notified.
        try {
            synchronized (timeoutMonitor) {
                // Check whether the job has already completed since it was
                // started or resumed above.  There is still a very slight
                // chance of a race condition in which which the thread finishes
                // and is then forced to wait.  The only consequence of this
                // would be a wasted execution window.
                if (Status.ACTIVE_INPROGRESS == status) {
                    timeoutMonitor.wait(timeout);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.fine("interrupted unexpectedly");
            System.exit(1);
        }

        // Suspend the thread immediately, regardless of what status we're in.
        suspendWorkerThread();

        LOGGER.fine("...done working");

        switch (status) {
            case ACTIVE_INPROGRESS:
                // Note: the job may have finished before the timeout, but in that case
                //       the "time spent" value will never be used.
                latestJob.increaseTimeSpent(timeout);

                if (latestJob.getTimeSpent() >= maxTimeSpentPerJob) {
                    yieldTimeoutResult(latestJob, maxTimeSpentPerJob);
                    interruptWorkerThread();
                    status = Status.IDLE_WAITING;
                    resumeWorkerThread();
                    return 0 == jobQueue.size();
                } else {
                    status = Status.ACTIVE_SUSPENDED;
                    return false;
                }
            case IDLE_FINISHED:
                resultHandler.handleResult(latestResult);

                // Advance to the wait()
                status = Status.IDLE_WAITING;
                resumeWorkerThread();
                // The worker isn't really "idle" if there are jobs in its queue.
                return 0 == jobQueue.size();
            default:
                throw new IllegalStateException("status should not occur at the end of a work window: " + status);
        }
    }

    /**
     * Terminates execution of the current job and prevents further jobs.
     * This method may be called at any time, in any thread.  If it is called
     * while a job is being executed, the job will continue executing for the
     * remainder of the window, but it will not complete normally unless it
     * does so within that window.  Nor will additional jobs be processed.
     */
    public synchronized void terminate() {
        LOGGER.info("terminating VMWorker");

        switch (status) {
            case ACTIVE_SUSPENDED:
                // Cause the worker thread to die.
                status = Status.TERMINATED;
                interruptWorkerThread();

                // Put the current job back in the queue to be aborted along
                // with the others.
                jobQueue.offer(latestJob);
                break;
            case IDLE_WAITING:
                status = Status.TERMINATED;
                notifyWorkerThread();
                break;
            case TERMINATED:
                // Been there, done that.
                return;
            default:
                throw new IllegalStateException("cannot terminate with status: " + status);
        }

        // Cancel all jobs in the queue.
        for (Job j : jobQueue) {
            JobResult abortedJob = new JobResult(j);
            resultHandler.handleResult(abortedJob);
        }
    }

    /**
     * Cancels a specific job.
     *
     * @param jobID the ID of the job to be aborted
     * @throws gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException
     *          if no such job exsts
     */
    public synchronized void abortJob(final String jobID) throws JobNotFoundException {
        LOGGER.info("aborting job " + jobID);

        switch (status) {
            case ACTIVE_SUSPENDED:
                if (latestJob.getJobId().equals(jobID)) {
                    // Cause the worker thread to cease execution of the current
                    // job and wait.
                    status = Status.IDLE_WAITING;
                    interruptWorkerThread();

                    // Put the current job in the queue to be discovered and
                    // aborted.
                    jobQueue.offer(latestJob);
                }
                break;
            case IDLE_WAITING:
                // Nothing to do.
                break;
            default:
                throw new IllegalStateException("can't abort job with status: " + status);
        }

        // Look for the job in the queue and remove it if present.
        // FIXME: inefficient
        for (Job j : jobQueue) {
            if (j.getJobId().equals(jobID)) {
                JobResult abortedJob = new JobResult(j);
                resultHandler.handleResult(abortedJob);
                jobQueue.remove(j);
                return;
            }
        }

        throw new JobNotFoundException(jobID);
    }

    public synchronized boolean jobExists(final String jobID) {
        switch (status) {
            case ACTIVE_SUSPENDED:
                return jobID.equals(latestJob.getJobId())
                        || jobQueueContains(jobID);
            case IDLE_WAITING:
                return jobQueueContains(jobID);
            default:
                throw new IllegalStateException("can't check job status in status: " + status);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Causes the worker runnable to stop waiting.
     */
    private void notifyWorkerThread() {
        synchronized (workerWaitMonitor) {
            workerWaitMonitor.notify();
        }
    }

    /**
     * Causes the worker runnable to abandon execution of a script.
     */
    private void interruptWorkerThread() {
        workerThread.interrupt();
    }

    @SuppressWarnings({"deprecation"})
    private void suspendWorkerThread() {
        workerThread.suspend();
    }

    @SuppressWarnings({"deprecation"})
    private void resumeWorkerThread() {
        workerThread.resume();
    }

    ////////////////////////////////////////////////////////////////////////////

    private boolean jobQueueContains(final String jobID) {
        for (Job j : jobQueue) {
            if (j.getJobId().equals(jobID)) {
                return true;
            }
        }

        return false;
    }

    private void evaluate(final Job request) {
        try {
            String expression = request.getExpression();
            LOGGER.fine(expression);
            Object returnObject = scriptEngine.eval(expression);

            // Note: the return object is not necessarily a string.  It may,
            // for instance, be a Double which needs to be converted to a
            // String.
            String returnvalue = (null == returnObject)
                    ? "" : returnObject.toString();

            yieldResult(request, returnvalue);
        } catch (ScriptException e) {
            yieldError(request, e);
        }
    }

    private void yieldError(final Job job,
                            final ScriptException exception) {
        latestResult = new JobResult(job, exception);
    }

    private void yieldResult(final Job job,
                             final String expression) {
        latestResult = new JobResult(job, expression);
    }

    private void yieldTimeoutResult(final Job job, final long timeout) {
        latestResult = new JobResult(job, timeout);
    }

    private class WorkerRunnable implements Runnable {

        public void run() {
            // Break out when the worker is terminated.
            while (Status.TERMINATED != status) {
                try {
                    if (Status.ACTIVE_INPROGRESS == status) {
                        evaluate(latestJob);
                        status = Status.IDLE_FINISHED;

                        synchronized (timeoutMonitor) {
                            // Notify the parent thread that a result is available.
                            timeoutMonitor.notify();
                        }
                    }

                    synchronized (workerWaitMonitor) {
                        workerWaitMonitor.wait();
                    }
                } catch (InterruptedException e) {
                    // Ignore and continue.  The point was to break out of the
                    // body of the loop.
                } catch (SecurityException e) {
                    // TODO
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO: stack trace
                    LOGGER.severe("worker runnable died with error: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

}