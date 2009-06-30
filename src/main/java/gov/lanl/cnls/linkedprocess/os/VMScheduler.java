package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:15:27 PM
 */
public class VMScheduler {
    private static final Logger LOGGER
            = LinkedProcess.getLogger(VMScheduler.class);

    private final BlockingQueue<VMWorker> workerQueue;
    private final Set<VMWorker> idleWorkerPool;
    private final Map<String, VMWorker> workersByJID;
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final int maxWorkers;
    private final VMResultHandler resultHandler;
    private final int numberOfSequencers;
    private LinkedProcess.FarmStatus status;


    private long jobsReceived = 0;
    private long jobsCompleted = 0;

    /**
     * Creates a new virtual machine scheduler.
     *
     * @param resultHandler a handler for results produced by the scheduler
     */
    public VMScheduler(final VMResultHandler resultHandler) {
        LOGGER.info("instantiating VMScheduler");

        this.resultHandler = new ResultCounter(resultHandler);

        workerQueue = new LinkedBlockingQueue<VMWorker>();
        idleWorkerPool = new HashSet<VMWorker>();
        workersByJID = new HashMap<String, VMWorker>();

        status = LinkedProcess.FarmStatus.ACTIVE;

        Properties props = LinkedProcess.getProperties();

        maxWorkers = new Integer(props.getProperty(
                LinkedProcess.MAX_VIRTUAL_MACHINES_PER_SCHEDULER));

        long timeSlice = new Long(props.getProperty(
                LinkedProcess.ROUND_ROBIN_TIME_SLICE));

        // A single source for workers.
        VMSequencerHelper source = createSequencerHelper();

        numberOfSequencers = new Integer(props.getProperty(
                LinkedProcess.MAX_CONCURRENT_WORKER_THREADS));

        for (int i = 0; i < numberOfSequencers; i++) {
            new VMSequencer(source, timeSlice);
        }
    }

    /**
     * Adds a job to the queue of the given machine.
     *
     * @param machineJID the JID of the virtual machine to execute the job
     * @param job        the job to execute
     * @throws ServiceRefusedException if, for any reason, the job cannot be
     *                                 accepted
     */
    public synchronized void scheduleJob(final String machineJID,
                                         final Job job) throws ServiceRefusedException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        jobsReceived++;

        VMWorker w = getWorkerByJID(machineJID);

        // FIXME: this call may block for as long as one timeslice.
        //        This wait could probably be eliminated.
        w.addJob(job);

        LOGGER.fine("enqueueing worker: " + w);
        enqueueWorker(w);
    }

    /**
     * Removes or cancels a job.
     *
     * @param machineJID the machine who was to have received the job
     * @param jobID      the ID of the specific job to be removed
     * @throws ServiceRefusedException if the job is not found
     */
    public synchronized void abortJob(final String machineJID,
                                      final String jobID) throws ServiceRefusedException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        VMWorker w = getWorkerByJID(machineJID);

        // FIXME: this call may block for as long as one timeslice.
        //        This wait could probably be eliminated.
        w.cancelJob(jobID);
    }

    /**
     * Creates a new virtual machine.
     *
     * @param machineJID the intended JID of the virtual machine
     * @param scriptType the type of virtual machine to create
     * @throws ServiceRefusedException if, for any reason, the machine cannot be created
     */
    public synchronized void spawnVirtualMachine(final String machineJID,
                                                 final String scriptType) throws ServiceRefusedException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        LOGGER.info("attempting to add machine of type " + scriptType + " with JID '" + machineJID + "'");

        if (LinkedProcess.FarmStatus.ACTIVE_FULL == status) {
            throw new ServiceRefusedException("too many active virtual machines");
        }

        if (null == machineJID || 0 == machineJID.length()) {
            throw new IllegalArgumentException("null or empty machine ID");
        }

        // TODO: check whether the scriptType is one of the allowed types
        if (null == scriptType || 0 == scriptType.length()) {
            throw new IllegalArgumentException("null or empty virtual machine scriptType");
        }

        if (null != workersByJID.get(machineJID)) {
            throw new ServiceRefusedException("machine with ID '" + machineJID + "' already exists");
        }

        ScriptEngine engine = manager.getEngineByName(scriptType);
        if (null == engine) {
            throw new ServiceRefusedException("unsupported script type: " + scriptType);
        }

        VMWorker w = new VMWorker(engine, resultHandler);

        workersByJID.put(machineJID, w);
        if (maxWorkers == workersByJID.size()) {
            status = LinkedProcess.FarmStatus.ACTIVE_FULL;
        }

        LOGGER.fine("adding worker to idle pool: " + w);
        idleWorkerPool.add(w);
    }

    /**
     * Destroys an already-created virtual machine.
     *
     * @param machineJID the JID of the virtual machine to destroy
     * @throws ServiceRefusedException if, for any reason, the virtual machine
     *                                 cannot be destroyed
     */
    public synchronized void terminateVirtualMachine(final String machineJID) throws ServiceRefusedException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        LOGGER.fine("removing VM with JID '" + machineJID + "'");
        VMWorker w = getWorkerByJID(machineJID);

        workersByJID.remove(machineJID);
        workerQueue.remove(w);

        idleWorkerPool.remove(w);

        w.terminate();

        if (maxWorkers > workersByJID.size()) {
            status = LinkedProcess.FarmStatus.ACTIVE;
        }
    }

    /**
     * @return the status of this scheduler
     */
    public synchronized LinkedProcess.FarmStatus getSchedulerStatus() {
        return status;
    }

    /**
     * @param machineJID the JID of the virtual machine of interest
     * @return the status of the given virtual machine
     */
    public synchronized LinkedProcess.VMStatus getVirtualMachineStatus(final String machineJID) {
        VMWorker w = workersByJID.get(machineJID);
        return (null == w)
                ? LinkedProcess.VMStatus.DOES_NOT_EXIST
                : LinkedProcess.VMStatus.ACTIVE;
    }

    /**
     * @param machineJID the JID of the machine to execute the job
     * @param iqID       the ID of the job of interest
     * @return the status of the given job
     */
    public synchronized LinkedProcess.JobStatus getJobStatus(final String machineJID,
                                               final String iqID) {
        VMWorker w = workersByJID.get(machineJID);

        // TODO: distinguish between non-existent VM and non-existent job.
        return (null != w && w.jobExists(iqID))
                ? LinkedProcess.JobStatus.IN_PROGRESS
                : LinkedProcess.JobStatus.DOES_NOT_EXIST;
    }

    /**
     * Shuts down all active virtual machines and cancels all jobs.
     */
    public synchronized void shutDown() {
        LOGGER.info("shutting down VMScheduler");

        workerQueue.clear();
        for (int i = 0; i < numberOfSequencers; i++) {
            try {
                // Add sentinel values to the queue, which will be retrieved by the
                // sequencers and cause them to terminate.  A null value cannot be
                // used, due to the specification of BlockingQueue.
                workerQueue.put(VMWorker.SCHEDULER_TERMINATED_SENTINEL);
            } catch (InterruptedException e) {
                LOGGER.fine("thread interrupted while shutting down scheduler");
                System.exit(1);
            }
        }

        for (VMWorker w : workersByJID.values()) {
            w.terminate();
        }

        workersByJID.clear();

        status = LinkedProcess.FarmStatus.TERMINATED;
    }

    /**
     * Waits until all pending and currently executed jobs have finished.  This
     * is a convenience method (for unit tests) which should be used with
     * caution.  Because the method is synchronized, you could wait indefinitely
     * on a job which never finishes, with no chance of terminating the job.
     * @throws InterruptedException if the Thread is interrupted while waiting
     */
    public synchronized void waitUntilFinished() throws InterruptedException {
        // Busy wait until the number of jobs completed catches up with the
        // number of jobs received.  Even failed jobs, cancelled jobs, and jobs
        // whose virtual machine has been terminated produce a result which is
        // counted.
        while (jobsCompleted < jobsReceived) {
            Thread.currentThread().sleep(100);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /*
    private VMResultHandler createResultHandler() {
        return new VMResultHandler() {
            public void handleResult(final JobResult result) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }      */

    private VMSequencerHelper createSequencerHelper() {
        return new VMSequencerHelper() {
            public synchronized VMWorker getWorker() {
                try {
                    return workerQueue.take();
                } catch (InterruptedException e) {
                    LOGGER.severe("thread interrupted unexpectedly in queue");
                    System.exit(1);
                    return null;
                }
            }

            public void putBackWorker(final VMWorker w,
                                      final boolean idle) {
                if (idle) {
                    putBackIdleWorker(w);
                } else {
                    enqueueWorker(w);
                }
            }
        };
    }

    private void putBackIdleWorker(final VMWorker w) {
        // TODO: synchronization
        idleWorkerPool.add(w);
    }

    private void enqueueWorker(final VMWorker w) {
        LOGGER.severe("enueueing worker: " + w);
        // If the worker is in the idle pool, add it to the queue instead.
        // Otherwise, don't move it.
        if (idleWorkerPool.contains(w)) {
            idleWorkerPool.remove(w);
        }
        try {
            workerQueue.put(w);
        } catch (InterruptedException e) {
            LOGGER.severe("thread interrupted unexpectedly in queue");
            System.exit(1);
        }
        LOGGER.severe("...done (workerQueue.size() = " + workerQueue.size() + ")");
    }

    private VMWorker getWorkerByJID(final String machineJID) throws ServiceRefusedException {
        VMWorker w = workersByJID.get(machineJID);

        if (null == w) {
            throw new ServiceRefusedException("no such machine: '" + machineJID + "'");
        }

        return w;
    }

    ////////////////////////////////////////////////////////////////////////////

    public interface VMResultHandler {
        void handleResult(JobResult result);
    }

    public interface VMSequencerHelper {
        VMWorker getWorker();

        void putBackWorker(VMWorker w, boolean idle);
    }

    private class ResultCounter implements VMResultHandler {
        private final VMResultHandler innerHandler;
        private final Object monitor = "";

        public ResultCounter(final VMResultHandler innerHandler) {
            this.innerHandler = innerHandler;
        }

        public void handleResult(JobResult result) {
            synchronized (monitor) {
                jobsCompleted++;
            }

            innerHandler.handleResult(result);
        }
    }
}
