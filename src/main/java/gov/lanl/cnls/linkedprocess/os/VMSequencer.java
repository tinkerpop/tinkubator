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

    private enum State {
        ACTIVE, TERMINATED
    }

    private State state;
    private final long timeSlice;
    private final Thread sequencerThread;
    private final VMScheduler.VMWorkerSource workerSource;

    /**
     * Creates a new sequencer for VM jobs.
     *
     * @param workerSource a source for workers ready to execute jobs.  When
     *                     this sequencer receives a null from this source, it terminates.
     * @param timeSlice    the time slice in which to execute jobs
     */
    public VMSequencer(final VMScheduler.VMWorkerSource workerSource,
                       final long timeSlice) {
        this.workerSource = workerSource;
        this.timeSlice = timeSlice;

        sequencerThread = new Thread(new SequencerRunnable());
        sequencerThread.start();

        state = State.ACTIVE;
    }

    ////////////////////////////////////////////////////////////////////////////

    private void executeForTimeSlice() {
        // Note: thread may block while waiting for a worker to become available.
        VMWorker w = workerSource.getWorker();

        // This sequencer is terminated by the receipt of a null worker.
        if (null == w) {
            state = State.TERMINATED;
            return;
        }

        if (!w.canWork()) {
            throw new IllegalStateException("worker has no jobs");
        }

        w.work(timeSlice);
    }

    private class SequencerRunnable implements Runnable {

        public void run() {
            try {
                // Break out when the sequencer is terminated.
                while (State.ACTIVE == state) {
                    executeForTimeSlice();
                }
            } catch (Exception e) {
                // TODO: stack trace
                LOGGER.error("sequencer runnable died with error: " + e.toString());
            }
        }
    }
}
