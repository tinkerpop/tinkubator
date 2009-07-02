package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException;
import gov.lanl.cnls.linkedprocess.os.errors.UnsupportedScriptEngineException;
import gov.lanl.cnls.linkedprocess.os.errors.VMAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.VMSchedulerIsFullException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerIsFullException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:15:27 PM
 */
public class VMScheduler {
    private static final Logger LOGGER
            = LinkedProcess.getLogger(VMScheduler.class);

    private final SimpleBlockingQueue<VMWorker> workerQueue;
    private final Map<String, VMWorker> workersByJID;
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final int maxWorkers;
    private final VMResultHandler resultHandler;
    private final LopStatusEventHandler eventHandler;
    private final int numberOfSequencers;
    private LinkedProcess.FarmStatus status;

    private long jobsReceived = 0;
    private long jobsCompleted = 0;

    /**
     * Creates a new virtual machine scheduler.
     *
     * @param resultHandler a handler for results produced by the scheduler
     */
    public VMScheduler(final VMResultHandler resultHandler,
                       final LopStatusEventHandler eventHandler) {
        LOGGER.info("instantiating VMScheduler");

        this.resultHandler = new ResultCounter(resultHandler);
        this.eventHandler = eventHandler;

        Properties props = LinkedProcess.getProperties();

        maxWorkers = new Integer(props.getProperty(
                LinkedProcess.MAX_VIRTUAL_MACHINES_PER_SCHEDULER));

        long timeSlice = new Long(props.getProperty(
                LinkedProcess.ROUND_ROBIN_TIME_SLICE));

        workerQueue = new SimpleBlockingQueue<VMWorker>();
        workersByJID = new HashMap<String, VMWorker>();

        // A single source for workers.
        VMSequencerHelper source = createSequencerHelper();

        numberOfSequencers = new Integer(props.getProperty(
                LinkedProcess.MAX_CONCURRENT_WORKER_THREADS));

        for (int i = 0; i < numberOfSequencers; i++) {
            new VMSequencer(source, timeSlice);
        }

        setSchedulerStatus(LinkedProcess.FarmStatus.ACTIVE);
    }

    /**
     * Adds a job to the queue of the given machine.
     *
     * @param machineJID the JID of the virtual machine to execute the job
     * @param job        the job to execute
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMWorkerIsFullException
     *          if the VM in question has a full queue
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException
     *          if no such VM exists
     */
    public synchronized void scheduleJob(final String machineJID,
                                         final Job job) throws VMWorkerIsFullException, VMWorkerNotFoundException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        jobsReceived++;

        VMWorker w = getWorkerByJID(machineJID);

        // FIXME: this call may block for as long as one timeslice.
        //        This wait could probably be eliminated.
        if (!w.addJob(job)) {
            throw new VMWorkerIsFullException(machineJID);
        }

        enqueueWorker(w);
    }

    /**
     * Removes or cancels a job.
     *
     * @param machineJID the machine who was to have received the job
     * @param jobID      the ID of the specific job to be removed
     * @throws gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException
     *          if no job with the specified ID exists
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException
     *          if no VM worker with the specified JID exists
     */
    public synchronized void abortJob(final String machineJID,
                                      final String jobID) throws VMWorkerNotFoundException, JobNotFoundException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        VMWorker w = getWorkerByJID(machineJID);

        // FIXME: this call may block for as long as one timeslice.
        //        This wait could probably be eliminated.
        w.abortJob(jobID);
    }

    /**
     * Creates a new virtual machine.
     *
     * @param machineJID the intended JID of the virtual machine
     * @param scriptType the type of virtual machine to create
     * @throws gov.lanl.cnls.linkedprocess.os.errors.UnsupportedScriptEngineException
     *          if the given script engine is not supported
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMAlreadyExistsException
     *          if a VM with the given JID already exists in this scheduler
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMSchedulerIsFullException
     *          if the scheduler cannot create additional virtual machines
     */
    public synchronized void spawnVirtualMachine(final String machineJID,
                                                 final String scriptType) throws VMAlreadyExistsException, UnsupportedScriptEngineException, VMSchedulerIsFullException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        LOGGER.info("attempting to add machine of type " + scriptType + " with JID '" + machineJID + "'");

        if (LinkedProcess.FarmStatus.ACTIVE_FULL == status) {
            throw new VMSchedulerIsFullException();
        }

        if (null == machineJID || 0 == machineJID.length()) {
            throw new IllegalArgumentException("null or empty machine ID");
        }

        if (null == scriptType || 0 == scriptType.length()) {
            throw new UnsupportedScriptEngineException(scriptType);
        }

        if (null != workersByJID.get(machineJID)) {
            throw new VMAlreadyExistsException(machineJID);
        }

        ScriptEngine engine = manager.getEngineByName(scriptType);
        if (null == engine) {
            throw new UnsupportedScriptEngineException(scriptType);
        }

        VMWorker w = new VMWorker(engine, resultHandler);

        workersByJID.put(machineJID, w);
        if (maxWorkers == workersByJID.size()) {
            setSchedulerStatus(LinkedProcess.FarmStatus.ACTIVE_FULL);
        }

        setVirtualMachineStatus(machineJID, LinkedProcess.VMStatus.ACTIVE);
    }

    /**
     * Destroys an already-created virtual machine.
     *
     * @param machineJID the JID of the virtual machine to destroy
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException
     *          if a VM worker with the JID does not exist
     */
    public synchronized void terminateVirtualMachine(final String machineJID) throws VMWorkerNotFoundException {
        if (LinkedProcess.FarmStatus.TERMINATED == status) {
            throw new IllegalStateException("scheduler has been terminated");
        }

        LOGGER.fine("removing VM with JID '" + machineJID + "'");
        VMWorker w = getWorkerByJID(machineJID);

        workersByJID.remove(machineJID);
        workerQueue.remove(w);

        w.terminate();
        setVirtualMachineStatus(machineJID, LinkedProcess.VMStatus.DOES_NOT_EXIST);

        if (maxWorkers > workersByJID.size()) {
            setSchedulerStatus(LinkedProcess.FarmStatus.ACTIVE);
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
     * @throws gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException
     *          if no VM worker with the given JID exists
     */
    public synchronized LinkedProcess.JobStatus getJobStatus(final String machineJID,
                                                             final String iqID) throws VMWorkerNotFoundException {
        VMWorker w = workersByJID.get(machineJID);

        if (null == w) {
            throw new VMWorkerNotFoundException(machineJID);
        }

        return (w.jobExists(iqID))
                ? LinkedProcess.JobStatus.IN_PROGRESS
                : LinkedProcess.JobStatus.DOES_NOT_EXIST;
    }

    /**
     * Shuts down all active virtual machines and cancels all jobs.
     */
    public synchronized void shutDown() {
        LOGGER.info("shutting down VMScheduler");

        workerQueue.clear();
        LOGGER.info("1");
        for (int i = 0; i < numberOfSequencers; i++) {
            // Add sentinel values to the queue, which will be retrieved by the
            // sequencers and cause them to terminate.  A null value cannot be
            // used, due to the specification of BlockingQueue.
            workerQueue.offer(VMWorker.SCHEDULER_TERMINATED_SENTINEL);
        }

        for (String machineJID : workersByJID.keySet()) {
            VMWorker w = workersByJID.get(machineJID);
            w.terminate();
            setVirtualMachineStatus(machineJID, LinkedProcess.VMStatus.DOES_NOT_EXIST);
        }
        workersByJID.clear();

        setSchedulerStatus(LinkedProcess.FarmStatus.TERMINATING);
    }

    /**
     * Waits until all pending and currently executed jobs have finished.  This
     * is a convenience method (for unit tests) which should be used with
     * caution.  Because the method is synchronized, you could wait indefinitely
     * on a job which never finishes, with no chance of terminating the job.
     *
     * @throws InterruptedException if the Thread is interrupted while waiting
     */
    public synchronized void waitUntilFinished() throws InterruptedException {
        // Busy wait until the number of jobs completed catches up with the
        // number of jobs received.  Even failed jobs, cancelled jobs, and jobs
        // whose virtual machine has been terminated produce a result which is
        // counted.
        while (jobsCompleted < jobsReceived) {
            Thread.sleep(100);
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
            public VMWorker getWorker() {
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
                if (!idle) {
                    enqueueWorker(w);
                }
            }
        };
    }

    private void enqueueWorker(final VMWorker w) {
        LOGGER.info("enqueueing worker: " + w);
        // Add the worker to the queue, unless it is already present.  This
        // check prevents clients from benefitting from aggressive behavior,
        // making very frequent requests to the same VM: the scheduler is fair
        // with respect to VMs.  Note, however, that the client may simply
        // spawn more VMs for greater throughput with respect to its competitors
        // on the machine.
        workerQueue.offerDistinct(w);
        LOGGER.info("...done (workerQueue.size() = " + workerQueue.size() + ")");
    }

    private VMWorker getWorkerByJID(final String machineJID) throws VMWorkerNotFoundException {
        VMWorker w = workersByJID.get(machineJID);

        if (null == w) {
            throw new VMWorkerNotFoundException(machineJID);
        }

        return w;
    }

    private void setSchedulerStatus(final LinkedProcess.FarmStatus newStatus) {
        status = newStatus;
        eventHandler.schedulerStatusChanged(status);
    }

    private void setVirtualMachineStatus(final String machineJID,
                                         final LinkedProcess.VMStatus newStatus) {
        eventHandler.virtualMachineStatusChanged(machineJID, newStatus);
    }

    ////////////////////////////////////////////////////////////////////////////

    public interface VMResultHandler {
        void handleResult(JobResult result);
    }

    public interface VMSequencerHelper {
        VMWorker getWorker();

        void putBackWorker(VMWorker w, boolean idle);
    }

    public interface LopStatusEventHandler {
        void schedulerStatusChanged(LinkedProcess.FarmStatus newStatus);

        void virtualMachineStatusChanged(String vmJID, LinkedProcess.VMStatus newStatus);
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
