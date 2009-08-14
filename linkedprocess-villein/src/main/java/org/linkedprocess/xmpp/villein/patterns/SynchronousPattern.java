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
import java.util.List;
import java.util.ArrayList;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern {
    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);

    private void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if ((System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    private void monitorSleep(final Object monitor, final long timeout) {
        try {
            synchronized (monitor) {
                if (timeout > 0)
                    monitor.wait(timeout);
                else
                    monitor.wait();
            }
        } catch (InterruptedException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    public VmProxy spawnVm(final FarmProxy farmProxy, final String vmSpecies, final long timeout) throws TimeoutException, CommandException {
        final Object monitor = new Object();
        final Holder<VmProxy> vmProxyHolder = new Holder<VmProxy>();
        final Holder<LopError> lopErrorHolder = new Holder<LopError>();

        Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
            public void handle(VmProxy vmProxy) {
                vmProxyHolder.store(vmProxy);
                farmProxy.addVmProxy(vmProxy);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorHolder.store(lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);
        this.monitorSleep(monitor, timeout);
        if (vmProxyHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("spawn_vm timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return vmProxyHolder.retrieve();
    }

    public JobStruct submitJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException {
        final Object monitor = new Object();
        final Holder<JobStruct> jobStructHolder = new Holder<JobStruct>();

        Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructHolder.store(jobStruct);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        vmProxy.submitJob(jobStruct, submitJobHandler, submitJobHandler);
        this.monitorSleep(monitor, timeout);
        if (jobStructHolder.isEmpty())
            throw new TimeoutException("submit_job timedout after " + timeout + "ms.");

        return jobStructHolder.retrieve();
    }

    public LinkedProcess.JobStatus pingJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, CommandException {
        final Object monitor = new Object();
        final Holder<LinkedProcess.JobStatus> jobStatusHolder = new Holder<LinkedProcess.JobStatus>();
        final Holder<LopError> lopErrorHolder = new Holder<LopError>();

        Handler<LinkedProcess.JobStatus> resultHandler = new Handler<LinkedProcess.JobStatus>() {
            public void handle(LinkedProcess.JobStatus jobStatus) {
                jobStatusHolder.store(jobStatus);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorHolder.store(lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        vmProxy.pingJob(jobStruct, resultHandler, errorHandler);
        this.monitorSleep(monitor, timeout);
        if (jobStatusHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("ping_job timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return jobStatusHolder.retrieve();
    }

    public JobStruct abortJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, CommandException {
        final Object monitor = new Object();
        final Holder<JobStruct> jobStructHolder = new Holder<JobStruct>();
        final Holder<LopError> lopErrorHolder = new Holder<LopError>();

        Handler<JobStruct> resultHandler = new Handler<JobStruct>() {
            public void handle(JobStruct jobStruct) {
                jobStructHolder.store(jobStruct);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorHolder.store(lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        vmProxy.abortJob(jobStruct, resultHandler, errorHandler);

        this.monitorSleep(monitor, timeout);
        if (jobStructHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("abort_job timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return jobStructHolder.retrieve();

    }

    public void setBindings(final VmProxy vmProxy, VMBindings vmBindings, final long timeout) throws TimeoutException, CommandException {
        final Object monitor = new Object();
        final Holder<VMBindings> vmBindingsHolder = new Holder<VMBindings>();
        final Holder<LopError> lopErrorHolder = new Holder<LopError>();

        Handler<VMBindings> resultHandler = new Handler<VMBindings>() {
            public void handle(VMBindings vmBindings) {
                vmBindingsHolder.store(vmBindings);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorHolder.store(lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        vmProxy.setBindings(vmBindings, resultHandler, errorHandler);

        this.monitorSleep(monitor, timeout);
        if (vmBindingsHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("set manage_bindings timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

    }

    public VMBindings getBindings(final VmProxy vmProxy, Set<String> bindingNames, final long timeout) throws TimeoutException, CommandException {
        final Object monitor = new Object();
        final Holder<VMBindings> vmBindingsHolder = new Holder<VMBindings>();
        final Holder<LopError> lopErrorHolder = new Holder<LopError>();

        Handler<VMBindings> resultHandler = new Handler<VMBindings>() {
            public void handle(VMBindings vmBindings) {
                vmBindingsHolder.store(vmBindings);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorHolder.store(lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        vmProxy.getBindings(bindingNames, resultHandler, errorHandler);

        this.monitorSleep(monitor, timeout);
        if (vmBindingsHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("get manage_bindings timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return vmBindingsHolder.retrieve();

    }

    public void terminateVm(final VmProxy vmProxy, final long timeout) throws TimeoutException, CommandException {
        final Object monitor = new Object();
        final Holder<Object> objectHolder = new Holder<Object>();
        final Holder<LopError> lopErrorHolder = new Holder<LopError>();

        Handler<Object> resultHandler = new Handler<Object>() {
            public void handle(Object object) {
                objectHolder.store(object);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                lopErrorHolder.store(lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
        vmProxy.terminateVm(resultHandler, errorHandler);

        this.monitorSleep(monitor, timeout);
        if (objectHolder.isEmpty() && lopErrorHolder.isEmpty())
                throw new TimeoutException("terminate_vm timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());
    }

    public void waitForFarms(final LopCloud lopCloud, final int minimumFarms, final long timeout) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            if (lopCloud.getFarmProxies().size() >= minimumFarms)
                return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    private class Holder<T> {
        private T object = null;

        public void store(T t) {
            this.object = t;
        }

        public T retrieve() {
            return this.object;
        }

        public boolean isEmpty() {
            return (this.object == null);
        }
    }

}
