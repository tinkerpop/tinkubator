/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.os;

import org.linkedprocess.LinkedProcess;

import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 25, 2009
 * Time: 4:00:44 PM
 */
class VmSequencer {
    private static final Logger LOGGER
            = LinkedProcess.getLogger(VmSequencer.class);

    private enum Status {
        ACTIVE, TERMINATED
    }

    private Status status;
    private final long timeSlice;
    private final VmScheduler.VmSequencerHelper sequencerHelper;

    private static long threadID = 0;

    private static synchronized String nextThreadName() {
        return "LoP VM sequencer thread #" + ++threadID;
    }

    /**
     * Creates a new sequencer for VM jobs.
     *
     * @param sequencerHelper a source for workers ready to execute jobs.  When
     *                        this sequencer receives a null from this source, it terminates.
     * @param timeSlice       the time slice in which to execute jobs
     */
    public VmSequencer(final VmScheduler.VmSequencerHelper sequencerHelper,
                       final long timeSlice) {
        //LOGGER.info("instantiating VMSequencer");

        this.sequencerHelper = sequencerHelper;
        this.timeSlice = timeSlice;

        // Must set status to ACTIVE before spawning the thread.
        status = Status.ACTIVE;

        Thread sequencerThread = new Thread(new SequencerRunnable(), nextThreadName());
        // Sequencer threads have less priority than the scheduler's thread.
        sequencerThread.setPriority(Thread.currentThread().getPriority() - 1);
        sequencerThread.start();
    }

    ////////////////////////////////////////////////////////////////////////////

    private void executeForTimeSlice() {
        //LOGGER.info("getting worker...");
        // Note: thread may block while waiting for a worker to become available.
        VmWorker w = sequencerHelper.getWorker();
        //LOGGER.info("...got worker: " + w);

        // This sequencer is terminated by the receipt of a null worker.
        if (VmWorker.SCHEDULER_TERMINATED_SENTINEL == w) {
            //LOGGER.info("VMSequencer has received SCHEDULER_TERMINATED_SENTINEL");
            status = Status.TERMINATED;
            return;
        }

        // Occasionally, a sequencer will pull a worker from the queue while it
        // is in the process of being terminated.  When this happens, simply skip
        // this time slice;
        if (VmWorker.Status.TERMINATED == w.status) {
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
            // Break out when the sequencer is terminated.
            while (Status.ACTIVE == status) {
                try {
                    executeForTimeSlice();
                    //LOGGER.info("SequencerRunnable is terminating");
                } catch (Throwable t) {
                    // Log the error, but attempt to recover.
                    LOGGER.severe("sequencer runnable died with error: " + t.toString());
                    t.printStackTrace();
                }
            }
        }
    }
}
