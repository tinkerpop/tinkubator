package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

/**
 * Author: josh
* Date: Jun 25, 2009
* Time: 4:00:44 PM
*/
class VMSequencer {
    private boolean idle;
    private VMScheduler virtualMachineScheduler;
    private final long timeSlice;

    public VMSequencer(final long timeSlice) {
        this.timeSlice = timeSlice;
        idle = true;
    }

    public boolean isIdle() {
        return idle;
    }

    public void startJob(final VMWorker w,
                         final Job job) {
        if (!isIdle()) {
            throw new IllegalStateException("can only start new jobs while idle");
        }

        w.addJob(job);
        w.work(timeSlice);
    }

    private class SequencerRunnable implements Runnable {

        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
