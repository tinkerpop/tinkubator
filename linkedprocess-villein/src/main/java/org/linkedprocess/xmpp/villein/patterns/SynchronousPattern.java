package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.Packet;

import java.util.*;
import java.util.logging.Logger;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern {
    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);
    
    protected long pollingInterval;
    protected long pollingTimeout = -1;
    protected long totalTime = 0;

    protected Map<String, JobStruct> jobMap = new HashMap<String, JobStruct>();

    private final Object pollingMonitor = "";

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public long getPollingInterval() {
        return this.pollingInterval;
    }

    public long getPollingTimeout() {
        return pollingTimeout;
    }

    public void setPollingTimeout(long pollingTimeout) {
        this.pollingTimeout = pollingTimeout;
    }

    private void checkTimeout(long startTime, long timeout) throws WaitTimeoutException {
        long runningTime = System.currentTimeMillis() - startTime;
        if (runningTime > timeout && timeout != -1)
            throw new WaitTimeoutException("Waiting timed out at " + runningTime + " of " + timeout + ".");
    }

    private void pollingSleep() {
        if (this.pollingInterval > 0) {
            try {
                synchronized (pollingMonitor) {
                    pollingMonitor.wait(this.pollingInterval);
                }
            } catch (InterruptedException e) {
                XmppVillein.LOGGER.warning(e.getMessage());
            }
            totalTime += pollingInterval;
        }
    }

    public JobStruct submitJob(VmProxy vmProxy, JobStruct jobStruct) {
        Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobMap.put(jobStruct.getJobId(), jobStruct);
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        if (null == jobStruct.getJobId()) {
            jobStruct.setJobId(Packet.nextID());
        }
        // FIXME: why the same handler for results and errors?
        vmProxy.submitJob(jobStruct, submitJobHandler, submitJobHandler);
        while (null == jobMap.get(jobStruct.getJobId())) {
            if (pollingTimeout >= 0 && totalTime >= pollingTimeout) {
                LOGGER.warning("timed out waiting for a job result");
                return null;
            }
            this.pollingSleep();
        }
        jobStruct = jobMap.get(jobStruct.getJobId());
        jobMap.remove(jobStruct.getJobId());
        return jobStruct;
    }

    /*public VmProxy spawnVm(FarmProxy farmProxy, String vmSpecies) {
        farmProxy.spawnVm(vmSpecies, null, null);
        return null;
    }*/


}
