/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;

import java.util.Set;
import java.util.logging.Logger;

/**
 * SynchronousPattern provides methods that wait for commands to complete before continuing. 
 * This pattern runs against the design philosophy of XMPP in which communication should be asynchronous.
 * However, practically speaking, there are many situations where it is easier to wait for the command to complete then to deal with handlers.
 * All of the methods provided by this pattern allow for a timeout value to be provided.
 * If the command takes longer than this timeout value, then a TimeoutException is thrown.
 *
 *
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern {
    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);

    private static void monitorSleep(final Object monitor, final long timeout) {
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

    public static VmProxy spawnVm(final FarmProxy farmProxy, final String vmSpecies, final long timeout) throws TimeoutException, CommandException {
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
        SynchronousPattern.monitorSleep(monitor, timeout);
        if (vmProxyHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("spawn_vm timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return vmProxyHolder.retrieve();
    }

    public static JobStruct submitJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException {
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
        SynchronousPattern.monitorSleep(monitor, timeout);
        if (jobStructHolder.isEmpty())
            throw new TimeoutException("submit_job timedout after " + timeout + "ms.");

        return jobStructHolder.retrieve();
    }

    public static LinkedProcess.JobStatus pingJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, CommandException {
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
        SynchronousPattern.monitorSleep(monitor, timeout);
        if (jobStatusHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("ping_job timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return jobStatusHolder.retrieve();
    }

    public static JobStruct abortJob(final VmProxy vmProxy, final JobStruct jobStruct, final long timeout) throws TimeoutException, CommandException {
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

        SynchronousPattern.monitorSleep(monitor, timeout);
        if (jobStructHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("abort_job timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return jobStructHolder.retrieve();

    }

    public static void setBindings(final VmProxy vmProxy, VMBindings vmBindings, final long timeout) throws TimeoutException, CommandException {
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

        SynchronousPattern.monitorSleep(monitor, timeout);
        if (vmBindingsHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("set manage_bindings timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

    }

    public static VMBindings getBindings(final VmProxy vmProxy, Set<String> bindingNames, final long timeout) throws TimeoutException, CommandException {
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

        SynchronousPattern.monitorSleep(monitor, timeout);
        if (vmBindingsHolder.isEmpty() && lopErrorHolder.isEmpty())
            throw new TimeoutException("get manage_bindings timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());

        return vmBindingsHolder.retrieve();

    }

    public static void terminateVm(final VmProxy vmProxy, final long timeout) throws TimeoutException, CommandException {
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

        SynchronousPattern.monitorSleep(monitor, timeout);
        if (objectHolder.isEmpty() && lopErrorHolder.isEmpty())
                throw new TimeoutException("terminate_vm timedout after " + timeout + "ms.");

        if (!lopErrorHolder.isEmpty())
            throw new CommandException(lopErrorHolder.retrieve());
    }

    private static class Holder<T> {
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
