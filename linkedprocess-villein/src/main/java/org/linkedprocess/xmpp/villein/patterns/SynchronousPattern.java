package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.structs.Job;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern extends VilleinPattern {

    protected long pollingSleepTime;

    public SynchronousPattern(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void setPollingSleepTime(long pollingSleepTime) {
        this.pollingSleepTime = pollingSleepTime;
    }

    public long getPollingSleepTime() {
        return this.pollingSleepTime;
    }

    private void checkTimeout(long startTime, long timeout) throws WaitTimeoutException {
        long runningTime = System.currentTimeMillis() - startTime;
        if (runningTime > timeout && timeout != -1)
            throw new WaitTimeoutException("Waiting timed out at " + runningTime + " of " + timeout + ".");
    }

    private void pollingSleep() {
        if (this.pollingSleepTime > 0) {
            try {
                Thread.sleep(this.pollingSleepTime);
            } catch (InterruptedException e) {
                XmppVillein.LOGGER.severe(e.getMessage());
            }
        }
    }

    public void waitFromFarms(int numberOfFarms, long timeout) throws WaitTimeoutException {
        long currentTime = System.currentTimeMillis();
        int x = 0;
        while (x < numberOfFarms) {
            checkTimeout(currentTime, timeout);
            x = this.xmppVillein.getFarmStructs().size();
            this.pollingSleep();
        }
    }

    public void waitFromVms(int numberOfVms, long timeout) throws WaitTimeoutException {
        long currentTime = System.currentTimeMillis();
        int x = 0;
        while (x < numberOfVms) {
            checkTimeout(currentTime, timeout);
            x = this.xmppVillein.getVmStructs().size();
            this.pollingSleep();
        }
    }

    public Collection<Job> waitForJobs(Set<String> jobIds, long timeout) throws WaitTimeoutException {
        long currentTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(currentTime, timeout);
            Collection<Job> jobs = this.xmppVillein.getJobs(jobIds);
            if (jobs.size() == jobIds.size()) {
                return jobs;
            }
            this.pollingSleep();
        }
    }

    public Job waitForJob(VmStruct vmStruct, String expression, String jobId, long timeout) throws WaitTimeoutException {
        this.xmppVillein.sendSubmitJob(vmStruct, expression, jobId);
        long currentTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(currentTime, timeout);
            Job job = vmStruct.getJob(jobId);
            if (null != job)
                return job;
        }
    }

    public Collection<Job> distributeJobAndWait(Set<VmStruct> vmStructs, String expression, long timeout) throws WaitTimeoutException {
        Set<String> jobIds = new HashSet<String>();
        for (VmStruct vmStruct : vmStructs) {
            String jobId = XmppVillein.generateRandomJobId();
            jobIds.add(jobId);
            this.xmppVillein.sendSubmitJob(vmStruct, expression, jobId);
        }
        return this.waitForJobs(jobIds, timeout);
    }

}
