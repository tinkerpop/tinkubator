package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.jivesoftware.smack.packet.Packet;

import java.util.*;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern {

    protected long pollingSleepTime;
    protected Map<String, JobStruct> jobMap = new HashMap<String, JobStruct>();

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
                XmppVillein.LOGGER.warning(e.getMessage());
            }
        }
    }



    public JobStruct submitJob(VmProxy vmProxy, JobStruct jobStruct) {
        Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobMap.put(jobStruct.getJobId(), jobStruct);
            }
        };
        if(null == jobStruct.getJobId()) {
            jobStruct.setJobId(Packet.nextID());
        }
        vmProxy.submitJob(jobStruct, submitJobHandler, submitJobHandler);
        while(null == jobMap.get(jobStruct.getJobId())) {
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
