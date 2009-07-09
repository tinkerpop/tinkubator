package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

import java.util.logging.Logger;

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
        //LOGGER.info("instantiating VMSequencer");

        this.sequencerHelper = sequencerHelper;
        this.timeSlice = timeSlice;

        // Must set status to ACTIVE before spawning the thread.
        status = Status.ACTIVE;

        Thread sequencerThread = new Thread(new SequencerRunnable(), nextThreadName());
        sequencerThread.start();
    }

    ////////////////////////////////////////////////////////////////////////////

    private void executeForTimeSlice() {
        //LOGGER.info("getting worker...");
        // Note: thread may block while waiting for a worker to become available.
        VMWorker w = sequencerHelper.getWorker();
        //LOGGER.info("...got worker: " + w);

        // This sequencer is terminated by the receipt of a null worker.
        if (VMWorker.SCHEDULER_TERMINATED_SENTINEL == w) {
            //LOGGER.info("VMSequencer has received SCHEDULER_TERMINATED_SENTINEL");
            status = Status.TERMINATED;
            return;
        }

        if (!w.canWork()) {
            // This will only occur if a worker added to the queue has
            // has subsequently had all of its jobs aborted.
            return;
        }

        boolean idle = w.work(timeSlice);
        //LOGGER.info("idle: " + idle);
        sequencerHelper.putBackWorker(w, idle);
    }

    private class SequencerRunnable implements Runnable {
        public SequencerRunnable() {
            //LOGGER.info("instantiating SequencerRunnable");
        }

        public void run() {
            //LOGGER.info("running SequencerRunnable");

            try {
                // Break out when the sequencer is terminated.
                while (Status.ACTIVE == status) {
                    executeForTimeSlice();
                }
                //LOGGER.info("SequencerRunnable is terminating");
            } catch (Throwable t) {
                LOGGER.severe("sequencer runnable died with error: " + t.toString());
                t.printStackTrace();
            }
        }
    }
}
