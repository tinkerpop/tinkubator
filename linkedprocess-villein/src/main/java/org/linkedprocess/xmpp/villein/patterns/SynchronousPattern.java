package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.LopCloud;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;
import org.jivesoftware.smack.packet.Packet;

import java.util.logging.Logger;
import java.util.Set;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern {
    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);

    private JobStruct jobStructTemp;
    private LinkedProcess.JobStatus jobStatusTemp;
    private VmProxy vmProxyTemp;
    private LopError lopErrorTemp;
    private VMBindings vmBindingsTemp;
    private Object objectTemp;
    private boolean timedout;

    private final Object monitor = new Object();

    private void nullifyTemps() {
        this.jobStructTemp = null;
        this.jobStatusTemp = null;
        this.vmProxyTemp = null;
        this.lopErrorTemp = null;
        this.vmBindingsTemp = null;
        this.objectTemp = null;
        this.timedout = true;
    }

    private void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if((System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    private void monitorSleep(final long timeout) {
            try {
                synchronized (monitor) {
                    if(timeout > 0)
                        monitor.wait(timeout);
                    else
                        monitor.wait();
                }
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
    }

    public VmProxy spawnVm(final FarmProxy farmProxy, final String vmSpecies, final long timeout) throws TimeoutException, CommandException {
        this.nullifyTemps();

        Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
            public void handle(VmProxy vmProxy) {
                vmProxyTemp = vmProxy;
                farmProxy.addVmProxy(vmProxy);
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);

        while(null == this.vmProxyTemp && null == this.lopErrorTemp) {
            this.monitorSleep(timeout);
            if(timedout)
                throw new TimeoutException("spawn_vm timedout after " + timeout + "ms.");
        }
        if(this.lopErrorTemp != null) {
            throw new CommandException(this.lopErrorTemp);
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
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        if (null == jobStruct.getJobId()) {
            jobStruct.setJobId(Packet.nextID());
        }

        vmProxy.submitJob(jobStruct, submitJobHandler, submitJobHandler);
        while (null == this.jobStructTemp) {
            this.monitorSleep(timeout);
            if(timedout)
                throw new TimeoutException("submit_job timedout after " + timeout + "ms.");
        }
        return this.jobStructTemp;
    }

    public LinkedProcess.JobStatus pingJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, CommandException {
        this.nullifyTemps();
        Handler<LinkedProcess.JobStatus> resultHandler = new Handler<LinkedProcess.JobStatus>() {
            public void handle(LinkedProcess.JobStatus jobStatus) {
                jobStatusTemp = jobStatus;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        vmProxy.pingJob(jobStruct, resultHandler, errorHandler);
        while(null == this.jobStatusTemp && null == this.lopErrorTemp) {
            this.monitorSleep(timeout);
            if(timedout)
                throw new TimeoutException("ping_job timedout after " + timeout + "ms.");
        }
        if(this.lopErrorTemp != null) {
            throw new CommandException(this.lopErrorTemp);
        }

        return this.jobStatusTemp;

    }

    public JobStruct abortJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, CommandException {
        this.nullifyTemps();
        Handler<JobStruct> resultHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructTemp = jobStruct;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        vmProxy.abortJob(jobStruct, resultHandler, errorHandler);
        while(null == this.jobStructTemp && null == this.lopErrorTemp) {
            this.monitorSleep(timeout);
            if(timedout)
                throw new TimeoutException("abort_job timedout after " + timeout + "ms.");
        }
        if(this.lopErrorTemp != null) {
            throw new CommandException(this.lopErrorTemp);
        }

        return this.jobStructTemp;

    }

    public void setBindings(final VmProxy vmProxy, VMBindings vmBindings, final long timeout) throws TimeoutException, CommandException {
        this.nullifyTemps();
        Handler<VMBindings> resultHandler = new Handler<VMBindings>() {
            public void handle(VMBindings vmBindings) {
                vmBindingsTemp = vmBindings;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        vmProxy.setBindings(vmBindings, resultHandler, errorHandler);
        while(null == this.vmBindingsTemp && null == this.lopErrorTemp) {
            this.monitorSleep(timeout);
            if(this.timedout)
                throw new TimeoutException("set manage_bindings timedout after " + timeout + "ms.");
        }
        if(this.lopErrorTemp != null) {
            throw new CommandException(this.lopErrorTemp);
        }
    }

    public VMBindings getBindings(final VmProxy vmProxy, Set<String> bindingNames, final long timeout) throws TimeoutException, CommandException {
        this.nullifyTemps();
        Handler<VMBindings> resultHandler = new Handler<VMBindings>() {
            public void handle(VMBindings vmBindings) {
                vmBindingsTemp = vmBindings;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        vmProxy.getBindings(bindingNames, resultHandler, errorHandler);
        while(null == this.vmBindingsTemp && null == this.lopErrorTemp) {
            this.monitorSleep(timeout);
            if(this.timedout)
                throw new TimeoutException("get manage_bindings timedout after " + timeout + "ms.");
        }
        if(this.lopErrorTemp != null) {
            throw new CommandException(this.lopErrorTemp);
        }

        return this.vmBindingsTemp;

    }

    public void terminateVm(final VmProxy vmProxy, final long timeout) throws TimeoutException, CommandException {
        this.nullifyTemps();
        Handler<Object> resultHandler = new Handler<Object>() {
            public void handle(Object object) {
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorTemp = lopError;
                timedout = false;
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        vmProxy.terminateVm(resultHandler, errorHandler);
        while(null == this.objectTemp && null == this.lopErrorTemp) {
            this.monitorSleep(timeout);
            if(this.timedout)
                throw new TimeoutException("terminate_vm timedout after " + timeout + "ms.");
        }
        if(this.lopErrorTemp != null) {
            throw new CommandException(this.lopErrorTemp);
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
