package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;

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
    protected long totalTime = 0;

    // TODO: private boolean timedOut;
    private JobStruct jobStructTemp;
    private VmProxy vmProxyTemp;
    private XMPPError xmppErrorTemp;

    private final Object pollingMonitor = new Object();

    public SynchronousPattern(final long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public void setPollingInterval(final long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public long getPollingInterval() {
        return this.pollingInterval;
    }

    private void nullifyTemps() {
        this.jobStructTemp = null;
        this.vmProxyTemp = null;
        this.xmppErrorTemp = null;
    }

    private void checkTimeout(final long startTime, final long timeout) throws WaitTimeoutException {
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
                LOGGER.warning(e.getMessage());
            }
        }
    }

    public JobStruct submitJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws WaitTimeoutException {
        this.nullifyTemps();
        Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructTemp = jobStruct;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        if (null == jobStruct.getJobId()) {
            jobStruct.setJobId(Packet.nextID());
        }

        vmProxy.submitJob(jobStruct, submitJobHandler, submitJobHandler);
        long startTime = System.currentTimeMillis();
        while (null == this.jobStructTemp) {
            this.checkTimeout(startTime, timeout);
            this.pollingSleep();
        }
        return this.jobStructTemp;
    }

    public VmProxy spawnVm(final FarmProxy farmProxy, final String vmSpecies, final long timeout) throws WaitTimeoutException, OperationException {
        this.nullifyTemps();
        Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
            public void handle(VmProxy vmProxy) {
                vmProxyTemp = vmProxy;
                farmProxy.addVmProxy(vmProxy);
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        Handler<XMPPError> errorHandler = new Handler<XMPPError>() {
            public void handle(XMPPError xmppError) {
                xmppErrorTemp = xmppError;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };

        farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);
        long startTime = System.currentTimeMillis();
        while(null == this.vmProxyTemp && null == this.xmppErrorTemp) {
            this.checkTimeout(startTime, timeout);
            this.pollingSleep();
        }
        if(this.xmppErrorTemp != null) {
            throw new OperationException(this.xmppErrorTemp);
        } else {
            return this.vmProxyTemp;
        }
    }

    public JobStruct abortJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws WaitTimeoutException, OperationException {
        this.nullifyTemps();
        Handler<JobStruct> resultHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructTemp = jobStruct;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        Handler<XMPPError> errorHandler = new Handler<XMPPError>() {
            public void handle(XMPPError xmppError) {
                xmppErrorTemp = xmppError;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };

        vmProxy.abortJob(jobStruct, resultHandler, errorHandler);
        long startTime = System.currentTimeMillis();
        while(null == this.jobStructTemp && null == this.xmppErrorTemp) {
            this.checkTimeout(startTime, timeout);
            this.pollingSleep();
        }
        if(this.xmppErrorTemp != null) {
            throw new OperationException(this.xmppErrorTemp);
        } else {
            return this.jobStructTemp;
        }  
    }


}
