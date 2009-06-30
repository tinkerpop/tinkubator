package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.apache.log4j.Logger;

/**
 * Author: josh
 * Date: Jun 25, 2009
 * Time: 4:00:44 PM
 */
class VMSequencer {
    private static final Logger LOGGER
            = LinkedProcess.getLogger(VMSequencer.class);

    private enum Status {
        ACTIVE, TERMINATED
    }

    private Status status;
    private final long timeSlice;
    private final Thread sequencerThread;
    private final VMScheduler.VMSequencerHelper sequencerHelper;

    private static long threadID = 0;

    private static synchronized String nextThreadName() {
        return "LOP VM sequencer thread #" + ++threadID;
    }

    /**
     * Creates a new sequencer for VM jobs.
     *
     * @param sequencerHelper a source for workers ready to execute jobs.  When
     *                     this sequencer receives a null from this source, it terminates.
     * @param timeSlice    the time slice in which to execute jobs
     */
    public VMSequencer(final VMScheduler.VMSequencerHelper sequencerHelper,
                       final long timeSlice) {
        LOGGER.info("instantiating VMSequencer");

        this.sequencerHelper = sequencerHelper;
        this.timeSlice = timeSlice;

        sequencerThread = new Thread(new SequencerRunnable(), nextThreadName());
        sequencerThread.start();

        status = Status.ACTIVE;
    }

    ////////////////////////////////////////////////////////////////////////////

    private void executeForTimeSlice() {
        LOGGER.debug("getting worker...");
        // Note: thread may block while waiting for a worker to become available.
        VMWorker w = sequencerHelper.getWorker();
        LOGGER.debug("...got worker: " + w);

        // This sequencer is terminated by the receipt of a null worker.
        if (VMWorker.SCHEDULER_TERMINATED_SENTINEL == w) {
            LOGGER.info("VMSequencer has received SCHEDULER_TERMINATED_SENTINEL");
            status = Status.TERMINATED;
            return;
        }

        if (!w.canWork()) {
            throw new IllegalStateException("worker has no jobs");
        }

        boolean idle = w.work(timeSlice);
        LOGGER.debug("idle: " + idle);
        sequencerHelper.putBackWorker(w, idle);
    }

    private class SequencerRunnable implements Runnable {
        public SequencerRunnable() {
            LOGGER.debug("instantiating SequencerRunnable");
        }

        public void run() {
            LOGGER.debug("running SequencerRunnable");

            try {
                // Break out when the sequencer is terminated.
                while (Status.ACTIVE == status) {
                    executeForTimeSlice();
                }
                LOGGER.info("SequencerRunnable is terminating");
            } catch (Exception e) {
                // TODO: stack trace in log message
                LOGGER.error("sequencer runnable died with error: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
