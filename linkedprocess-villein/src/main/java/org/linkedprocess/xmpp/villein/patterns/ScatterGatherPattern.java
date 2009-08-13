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

    public Set<VmProxy> scatterSpawnVm(Collection<FarmProxy> farmProxies, String vmSpecies, int vmsPerFarm) {
        final Set<VmProxy> vmProxies = new HashSet<VmProxy>();
        final List<Object> counter = new ArrayList<Object>();

        for (FarmProxy farmProxy : farmProxies) {
            for (int i = 0; i < vmsPerFarm; i++) {
                Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
                    public void handle(VmProxy vmProxy) {
                        vmProxies.add(vmProxy);
                        counter.add(new Object());
                    }
                };

                Handler<LopError> errorHandler = new Handler<LopError>() {
                    public void handle(LopError lopError) {
                        counter.add(new Object());
                    }
                };
                farmProxy.spawnVm(vmSpecies, resultHandler, errorHandler);
            }
        }

        while (counter.size() < (farmProxies.size() * vmsPerFarm)) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
        return vmProxies;
    }


    public Map<VmProxy, JobStruct> scatterSubmitJob(final Map<VmProxy, JobStruct> vmJobMap) {

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<JobStruct> submitJobHandler = new Handler<JobStruct>() {
                public void handle(JobStruct jobStruct) {
                    vmJobMap.put(vmProxy, jobStruct);
                }
            };
            vmProxy.submitJob(vmJobMap.get(vmProxy), submitJobHandler, submitJobHandler);
        }
        while (!ScatterGatherPattern.areComplete(vmJobMap.values())) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
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

