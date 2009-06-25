package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:15:27 PM
 */
public class VMScheduler {

    private final Queue<VMWorker> roundRobinQueue;
    private final Map<String, VMWorker> machinesByID;
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final VMSequencer[] sequencers;
    private final int maxWorkers;

    public enum FarmStatus { ACTIVE }  // TODO
    public enum VMStatus { ACTIVE, DOES_NOT_EXIST }
    public enum JobStatus { RUNNING, DOES_NOT_EXIST }

    public VMScheduler() {
        roundRobinQueue = new LinkedList<VMWorker>();
        machinesByID = new HashMap<String, VMWorker>();

        Properties props = LinkedProcess.getProperties();

        maxWorkers = new Integer(props.getProperty(
                LinkedProcess.MAX_VIRTUAL_MACHINES_PER_SCHEDULER));

        long timeSlice = new Long(props.getProperty(
                LinkedProcess.ROUND_ROBIN_TIME_SLICE));

        int n = new Integer(props.getProperty(
                LinkedProcess.MAX_CONCURRENT_WORKER_THREADS));
        sequencers = new VMSequencer[n];
        for (int i = 0; i < n; i++) {
            sequencers[i] = new VMSequencer(timeSlice);
        }
    }

    public synchronized void addJob(final String machineJID,
                                    final Job job) throws ServiceRefusedException {
        VMWorker w = machinesByID.get(machineJID);
        if (null == w) {
            throw new ServiceRefusedException("no such machine: '" + machineJID + "'");
        }

        for (VMSequencer t : sequencers) {
            if (t.isIdle()) {
                t.startJob(w, job);
                return;
            }
        }

        //...
    }

    public synchronized void removeJob(final String machineJID,
                                       final Job job) {

    }

    public synchronized boolean addMachine(final String machineJID,
                                           final String type) throws ServiceRefusedException {
        if (machinesByID.size() == maxWorkers) {
            throw new ServiceRefusedException("too many active virtual machines");
        }

        if (null == machineJID || 0 == machineJID.length()) {
            throw new IllegalArgumentException("null or empty machine ID");
        }

        // TODO: check whether the type is one of the allowed types
        if (null == type || 0 == type.length()) {
            throw new IllegalArgumentException("null or empty virtual machine type");
        }

        if (null != machinesByID.get(machineJID)) {
            throw new ServiceRefusedException("machine with ID '" + machineJID + "' already exists");
        }

        ScriptEngine engine = manager.getEngineByName(type);

        VMWorker m = new VMWorker(engine, createResultHandler());
        machinesByID.put(machineJID, m);
        return roundRobinQueue.offer(m);
    }

    public synchronized boolean removeMachine(final String machineJID) throws ServiceRefusedException {
        if (null == machinesByID.get(machineJID)) {
            throw new ServiceRefusedException("no such machine: '" + machineJID + "'");
        }

        //...

        return true;
    }

    public FarmStatus getFarmPresence() {
        //...
        return null;
    }

    public VMStatus getVMPresence(final String machineJID) {
        //...
        return null;
    }

    public JobStatus getJobStatus(final String iqID) {
        //...
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////

    private VMResultHandler createResultHandler() {
        return new VMResultHandler() {
            public void handleResult(final JobResult result) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    private synchronized void assignNextJob() {

    }

    private void handleJobReceived() {

    }

    private void handleJobFinished() {

    }

    ////////////////////////////////////////////////////////////////////////////

    public class ServiceRefusedException extends Exception {
        public ServiceRefusedException(final String msg) {
            super(msg);
        }
    }

    public interface VMResultHandler {
        void handleResult(JobResult result);
    }
}
