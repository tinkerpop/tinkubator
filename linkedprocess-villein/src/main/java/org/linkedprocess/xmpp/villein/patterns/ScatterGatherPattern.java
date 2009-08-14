/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;

import java.util.*;
import java.util.logging.Logger;

/**
 * The ScatterGatherPattern is useful for distributing commands across a collection of resources in an LoP cloud and handling their results when all commands are complete.
 * ScatterGatherPattern has both synchronous and asynchronous versions of its methods.
 *
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class ScatterGatherPattern {

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

    private static boolean areComplete(Collection<JobStruct> jobStructs) {
        for (JobStruct jobStruct : jobStructs) {
            if (!jobStruct.isComplete())
                return false;
        }
        return true;
    }

    public static Set<VmProxy> scatterSpawnVm(final Collection<FarmProxy> farmProxies, final String vmSpecies, final int vmsPerFarm, final long timeout) throws TimeoutException {
        final Set<VmProxy> vmProxies = new HashSet<VmProxy>();
        final List<Object> counter = new ArrayList<Object>();
        final Object monitor = new Object();

        for (FarmProxy farmProxy : farmProxies) {
            for (int i = 0; i < vmsPerFarm; i++) {
                Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
                    public void handle(VmProxy vmProxy) {
                        vmProxies.add(vmProxy);
                        counter.add(new Object());
                        if (counter.size() == (farmProxies.size() * vmsPerFarm)) {
                            synchronized (monitor) {
                                monitor.notify();
                            }
                        }
                    }
                };

                Handler<LopError> errorHandler = new Handler<LopError>() {
                    public void handle(LopError lopError) {
                        counter.add(new Object());
                        if (counter.size() == (farmProxies.size() * vmsPerFarm)) {
                            synchronized (monitor) {
                                monitor.notify();
                            }
                        }
                    }
                };
                farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);
            }
        }

        ScatterGatherPattern.monitorSleep(monitor, timeout);
        if (counter.size() != (farmProxies.size() * vmsPerFarm))
            throw new TimeoutException("scatter spawn_vm timedout after " + timeout + "ms.");

        return vmProxies;
    }

    public static void scatterTerminateVm(Collection<VmProxy> vmProxies) {
        for (VmProxy vmProxy : vmProxies) {
            vmProxy.terminateVm(null, null);
        }
    }

    public static Map<VmProxy, JobStruct> scatterSubmitJob(final Map<VmProxy, JobStruct> vmJobMap, long timeout) throws TimeoutException {
        final Object monitor = new Object();

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
                public void handle(JobStruct jobStruct) {
                    vmJobMap.put(vmProxy, jobStruct);
                    if (ScatterGatherPattern.areComplete(vmJobMap.values())) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };
            vmProxy.submitJob(vmJobMap.get(vmProxy), submitJobHandler, submitJobHandler);
        }

        ScatterGatherPattern.monitorSleep(monitor, timeout);
        if (!ScatterGatherPattern.areComplete(vmJobMap.values()))
            throw new TimeoutException("scatter submit_job timedout after " + timeout + "ms.");

        return vmJobMap;
    }

    public static void scatterSubmitJob(final Map<VmProxy, JobStruct> vmJobMap, final Handler<Map<VmProxy, JobStruct>> resultHandler) {
        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
                public void handle(JobStruct jobStruct) {
                    vmJobMap.put(vmProxy, jobStruct);
                    if (ScatterGatherPattern.areComplete(vmJobMap.values())) {
                        resultHandler.handle(vmJobMap);
                    }
                }
            };
            vmProxy.submitJob(vmJobMap.get(vmProxy), submitJobHandler, submitJobHandler);
        }
    }
}

