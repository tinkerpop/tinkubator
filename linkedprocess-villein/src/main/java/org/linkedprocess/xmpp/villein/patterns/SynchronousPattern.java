package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.LopCloud;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.Packet;

import java.util.logging.Logger;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern {
    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);

    private JobStruct jobStructTemp;
    private VmProxy vmProxyTemp;
    private LopError lopErrorTemp;
    private boolean timedout;

    private final Object pollingMonitor = new Object();

    private void nullifyTemps() {
        this.jobStructTemp = null;
        this.vmProxyTemp = null;
        this.lopErrorTemp = null;
        this.timedout = true;
    }

    private void pollingSleep(final long timeout) {
            try {
                synchronized (pollingMonitor) {
                    if(timeout > 0)
                        pollingMonitor.wait(timeout);
                    else
                        pollingMonitor.wait();
                }
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
    }

    public VmProxy spawnVm(final FarmProxy farmProxy, final String vmSpecies, final long timeout) throws TimeoutException, OperationException {
        this.nullifyTemps();
        Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
            public void handle(VmProxy vmProxy) {
                vmProxyTemp = vmProxy;
                farmProxy.addVmProxy(vmProxy);
                timedout = false;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };

        farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);

        while(null == this.vmProxyTemp && null == this.lopErrorTemp) {
            this.pollingSleep(timeout);
            if(timedout)
                throw new TimeoutException("spawn_vm timed out.");
        }
        if(this.lopErrorTemp != null) {
            throw new OperationException(this.lopErrorTemp);
        } else {
            return this.vmProxyTemp;
        }
    }

    public JobStruct submitJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException {
        this.nullifyTemps();
        Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructTemp = jobStruct;
                timedout = false;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        if (null == jobStruct.getJobId()) {
            jobStruct.setJobId(Packet.nextID());
        }

        vmProxy.submitJob(jobStruct, submitJobHandler, submitJobHandler);
        while (null == this.jobStructTemp) {
            this.pollingSleep(timeout);
            if(timedout)
                throw new TimeoutException("submit_job timed out.");
        }
        return this.jobStructTemp;
    }

    public JobStruct abortJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, OperationException {
        this.nullifyTemps();
        Handler<JobStruct> resultHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructTemp = jobStruct;
                timedout = false;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (pollingMonitor) {
                    pollingMonitor.notify();
                }
            }
        };

        vmProxy.abortJob(jobStruct, resultHandler, errorHandler);
        while(null == this.jobStructTemp && null == this.lopErrorTemp) {
            this.pollingSleep(timeout);
            if(timedout)
                throw new TimeoutException("abort_job timed out.");
        }
        if(this.lopErrorTemp != null) {
            throw new OperationException(this.lopErrorTemp);
        } else {
            return this.jobStructTemp;
        }  
    }

    private void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if((System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    public void waitForFarms(final LopCloud lopCloud, final int minimumFarms, final long timeout) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while(true) {
            checkTimeout(startTime, timeout);
            if(lopCloud.getFarmProxies().size() >= minimumFarms)
                return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

}
