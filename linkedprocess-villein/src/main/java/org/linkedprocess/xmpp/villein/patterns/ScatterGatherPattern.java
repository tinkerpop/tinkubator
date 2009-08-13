package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.LinkedProcess;

import java.util.*;
import java.util.logging.Logger;

public class ScatterGatherPattern {

    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);
    private final Object monitor = new Object();
    private boolean timedout;

    private void monitorSleep(final long timeout) {
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

    public Set<VmProxy> scatterSpawnVm(final Collection<FarmProxy> farmProxies, final String vmSpecies, final int vmsPerFarm, final long timeout) throws TimeoutException {
        final Set<VmProxy> vmProxies = new HashSet<VmProxy>();
        final List<Object> counter = new ArrayList<Object>();


        for (FarmProxy farmProxy : farmProxies) {
            for (int i = 0; i < vmsPerFarm; i++) {
                Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
                    public void handle(VmProxy vmProxy) {
                        vmProxies.add(vmProxy);
                        counter.add(new Object());
                        if (counter.size() == (farmProxies.size() * vmsPerFarm)) {
                            synchronized (monitor) {
                                timedout = false;
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
                                timedout = false;
                                monitor.notify();
                            }
                        }
                    }
                };
                farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);
            }
        }

        this.timedout = true;
        while (counter.size() < (farmProxies.size() * vmsPerFarm)) {
            this.monitorSleep(timeout);
            if (this.timedout)
                throw new TimeoutException("scatter spawn_vm timedout after " + timeout + "ms.");
        }
        return vmProxies;
    }

    public void scatterTerminateVm(Collection<VmProxy> vmProxies) {
        for (VmProxy vmProxy : vmProxies) {
            vmProxy.terminateVm(null, null);
        }
    }

    public Map<VmProxy, JobStruct> scatterSubmitJob(final Map<VmProxy, JobStruct> vmJobMap, long timeout) throws TimeoutException {

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
                public void handle(JobStruct jobStruct) {
                    vmJobMap.put(vmProxy, jobStruct);
                    if (ScatterGatherPattern.areComplete(vmJobMap.values())) {
                        synchronized (monitor) {
                            timedout = false;
                            monitor.notify();
                        }
                    }
                }
            };
            vmProxy.submitJob(vmJobMap.get(vmProxy), submitJobHandler, submitJobHandler);
        }
        this.timedout = true;
        while (!ScatterGatherPattern.areComplete(vmJobMap.values())) {
            this.monitorSleep(timeout);
            if (this.timedout)
                throw new TimeoutException("scatter submit_job timedout after " + timeout + "ms.");

        }
        return vmJobMap;
    }

    private static boolean areComplete(Collection<JobStruct> jobStructs) {
        for (JobStruct jobStruct : jobStructs) {
            if (!jobStruct.isComplete())
                return false;
        }
        return true;
    }
}

