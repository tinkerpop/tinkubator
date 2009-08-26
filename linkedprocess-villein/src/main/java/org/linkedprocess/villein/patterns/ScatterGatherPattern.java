/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.patterns;

import org.linkedprocess.Error;
import org.linkedprocess.*;
import org.linkedprocess.farm.os.VmBindings;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.VmProxy;

import java.util.*;
import java.util.logging.Logger;

/**
 * The ScatterGatherPattern is useful for distributing commands across a collection of resources in an LoP cloud and handling their results when all commands are complete.
 * ScatterGatherPattern has both synchronous and asynchronous versions of its methods.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ScatterGatherPattern {

    private static final Logger LOGGER = LinkedProcess.getLogger(SynchronousPattern.class);

    /**
     * Puts a monitor on wait for a certain number of milliseconds.
     *
     * @param monitor the monitor object to wait
     * @param timeout the number of milliseconds to wait (use -1 to wait indefinately)
     */
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

    /**
     * Determines if all the JobStructs in the Collection are complete.
     *
     * @param jobProxies the JobStructs to check
     * @return false if at least one of the JobStructs is not complete
     */
    private static boolean areComplete(Collection<JobProxy> jobProxies) {
        for (JobProxy jobProxy : jobProxies) {
            if (!jobProxy.isComplete())
                return false;
        }
        return true;
    }

    /**
     * Scatters spawn_vm commands to a Collection of FarmProxies and waits for all commands to complete.
     *
     * @param farmProxies the proxies to the farms where a virtual machine spawn is desired
     * @param vmSpecies   the species of the virtual machine to spawn
     * @param vmsPerFarm  the number of virtual machines to spawn on each farm
     * @param timeout     the number of milliseconds to spend on this scatter before a TimeoutException is thrown (use -1 to wait indefinately)
     * @return a set of virtual machine proxies that were spawned from this scatter
     * @throws TimeoutException is thrown when the scatter takes longer than the provided timeout in milliseconds
     */
    public static Set<ResultHolder<VmProxy>> scatterSpawnVm(final Collection<FarmProxy> farmProxies, final String vmSpecies, final int vmsPerFarm, final long timeout) throws TimeoutException {
        final Set<ResultHolder<VmProxy>> resultHolders = new HashSet<ResultHolder<VmProxy>>();
        final Object monitor = new Object();

        for (final FarmProxy farmProxy : farmProxies) {
            for (int i = 0; i < vmsPerFarm; i++) {
                Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
                    public void handle(VmProxy vmProxy) {
                        resultHolders.add(new ResultHolder<VmProxy>(vmProxy));
                        if (resultHolders.size() == (farmProxies.size() * vmsPerFarm)) {
                            synchronized (monitor) {
                                monitor.notify();
                            }
                        }
                    }
                };

                Handler<Error> errorHandler = new Handler<Error>() {
                    public void handle(Error lopError) {
                        resultHolders.add(new ResultHolder<VmProxy>(lopError));
                        if (resultHolders.size() == (farmProxies.size() * vmsPerFarm)) {
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
        if (resultHolders.size() != (farmProxies.size() * vmsPerFarm))
            throw new TimeoutException("scatter spawn_vm timedout after " + timeout + "ms.");

        return resultHolders;
    }

    /**
     * Scatters spawn_vm commands to a Collection of FarmProxies and makes use of a result handler when the commands are complete.
     *
     * @param farmProxies   the proxies of the farms to spawn virtual machines on
     * @param vmSpecies     vmSpecies the species of the virtual machine to spawn
     * @param vmsPerFarm    the number of virtual machines to spawn on each farm
     * @param resultHandler the handler of the results (can be null)
     */
    public void scatterSpawnVm(final Collection<FarmProxy> farmProxies, final String vmSpecies, final int vmsPerFarm, final Handler<Set<ResultHolder<VmProxy>>> resultHandler) {
        final Set<ResultHolder<VmProxy>> resultHolders = new HashSet<ResultHolder<VmProxy>>();
        for (final FarmProxy farmProxy : farmProxies) {
            for (int i = 0; i < vmsPerFarm; i++) {
                Handler<VmProxy> spawnResultHandler = new Handler<VmProxy>() {
                    public void handle(VmProxy vmProxy) {
                        resultHolders.add(new ResultHolder<VmProxy>(vmProxy));
                        if (resultHolders.size() == vmsPerFarm * farmProxies.size()) {
                            resultHandler.handle(resultHolders);
                        }
                    }
                };
                Handler<Error> spawnErrorHandler = new Handler<Error>() {
                    public void handle(Error lopError) {
                        resultHolders.add(new ResultHolder<VmProxy>(lopError));
                        if (resultHolders.size() == vmsPerFarm * farmProxies.size()) {
                            resultHandler.handle(resultHolders);
                        }
                    }
                };
                farmProxy.spawnVm(vmSpecies, spawnResultHandler, spawnErrorHandler);
            }
        }
    }

    /**
     * Scatters terminate_vm commands to a Collection of VmProxies and makes not use of handlers or waiting.
     *
     * @param vmProxies the proxies of the virtual machines to terminate
     */
    public static void scatterTerminateVm(Collection<VmProxy> vmProxies) {
        for (VmProxy vmProxy : vmProxies) {
            vmProxy.terminateVm(null, null);
        }
    }

    /**
     * Scatters submit_job commands to a set of VmProxies and waits for all commands to complete.
     *
     * @param vmJobMap a mapping from a VmProxy to the JobStruct that it should evaluate
     * @param timeout  the number of milliseconds to spend on this scatter before a TimeoutException is thrown (use -1 to wait indefinately)
     * @return a mapping from a VmProxy to the JobStruct that it evaluated with its result or error
     * @throws TimeoutException is thrown when the scatter takes longer than the provided timeout in milliseconds
     */
    public static Map<VmProxy, JobProxy> scatterSubmitJob(final Map<VmProxy, JobProxy> vmJobMap, long timeout) throws TimeoutException {
        final Object monitor = new Object();

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<JobProxy> submitJobHandler = new Handler<JobProxy>() {
                public void handle(JobProxy jobStruct) {
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

    /**
     * Scatters submit_job commands to a set of VmProxies and makes use of a result handler when the commands are complete.
     *
     * @param vmJobMap      a mapping from a VmProxy to the JobStruct that it should evaluate
     * @param resultHandler the handler of the results (can be null)
     */
    public static void scatterSubmitJob(final Map<VmProxy, JobProxy> vmJobMap, final Handler<Map<VmProxy, JobProxy>> resultHandler) {
        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<JobProxy> submitJobHandler = new Handler<JobProxy>() {
                public void handle(JobProxy jobStruct) {
                    vmJobMap.put(vmProxy, jobStruct);
                    if (ScatterGatherPattern.areComplete(vmJobMap.values())) {
                        resultHandler.handle(vmJobMap);
                    }
                }
            };
            vmProxy.submitJob(vmJobMap.get(vmProxy), submitJobHandler, submitJobHandler);
        }
    }

    /**
     * Scatters abort_job commands to a set of VmProxies and waits for all commands to complete.
     *
     * @param vmJobMap a mapping from a VmProxy to the JobStruct that should be aborted (requires jobId be set)
     * @param timeout  the number of milliseconds to spend on this scatter before a TimeoutException is thrown (use -1 to wait indefinately)
     * @return a set of job id results
     * @throws TimeoutException is thrown when the scatter takes longer than the provided timeout in milliseconds
     */
    public static Set<ResultHolder<String>> scatterAbortJob(final Map<VmProxy, JobProxy> vmJobMap, long timeout) throws TimeoutException {
        final Object monitor = new Object();

        final Set<ResultHolder<String>> resultHolders = new HashSet<ResultHolder<String>>();

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<String> abortResultHandler = new Handler<String>() {
                public void handle(String jobId) {
                    resultHolders.add(new ResultHolder<String>(jobId));
                    if (resultHolders.size() == vmJobMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };

            Handler<Error> abortErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.add(new ResultHolder<String>(lopError));
                    if (resultHolders.size() == vmJobMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };
            vmProxy.abortJob(vmJobMap.get(vmProxy), abortResultHandler, abortErrorHandler);
        }

        ScatterGatherPattern.monitorSleep(monitor, timeout);
        if (resultHolders.size() != vmJobMap.size())
            throw new TimeoutException("scatter abort_job timedout after " + timeout + "ms.");
        return resultHolders;

    }

    /**
     * Scatters abort_job commands to a set of VmProxies and makes use of a result handler when the commands are complete.
     *
     * @param vmJobMap      a mapping from a VmProxy to the JobStruct that should be aborted (requires jobId be set)
     * @param resultHandler the handler of the job id results (can be null)
     */
    public static void scatterAbortJob(final Map<VmProxy, JobProxy> vmJobMap, final Handler<Set<ResultHolder<String>>> resultHandler) {
        final Set<ResultHolder<String>> resultHolders = new HashSet<ResultHolder<String>>();

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<String> abortResultHandler = new Handler<String>() {
                public void handle(String jobId) {
                    resultHolders.add(new ResultHolder<String>(jobId));
                    if (resultHolders.size() == vmJobMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };

            Handler<Error> abortErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.add(new ResultHolder<String>(lopError));
                    if (resultHolders.size() == vmJobMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };
            vmProxy.abortJob(vmJobMap.get(vmProxy), abortResultHandler, abortErrorHandler);
        }
    }

    /**
     * Scatters ping_job commands to a set of VmProxies and waits for all commands to complete.
     *
     * @param vmJobMap a mapping from a VmProxy to the JobStruct that should be pinged (requires jobId be set)
     * @param timeout  the number of milliseconds to spend on this scatter before a TimeoutException is thrown (use -1 to wait indefinately)
     * @return a set of job status results
     * @throws TimeoutException is thrown when the scatter takes longer than the provided timeout in milliseconds
     */
    public static Set<ResultHolder<LinkedProcess.JobStatus>> scatterPingJob(final Map<VmProxy, JobProxy> vmJobMap, long timeout) throws TimeoutException {
        final Object monitor = new Object();
        final Set<ResultHolder<LinkedProcess.JobStatus>> resultHolders = new HashSet<ResultHolder<LinkedProcess.JobStatus>>();

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<LinkedProcess.JobStatus> pingResultHandler = new Handler<LinkedProcess.JobStatus>() {
                public void handle(LinkedProcess.JobStatus jobStatus) {
                    resultHolders.add(new ResultHolder<LinkedProcess.JobStatus>(jobStatus));
                    if (resultHolders.size() == vmJobMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };

            Handler<Error> pingErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.add(new ResultHolder<LinkedProcess.JobStatus>(lopError));
                    if (resultHolders.size() == vmJobMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };
            vmProxy.pingJob(vmJobMap.get(vmProxy), pingResultHandler, pingErrorHandler);
        }

        ScatterGatherPattern.monitorSleep(monitor, timeout);
        if (resultHolders.size() != vmJobMap.size())
            throw new TimeoutException("scatter ping_job timedout after " + timeout + "ms.");
        return resultHolders;

    }

    /**
     * Scatters ping_job commands to a set of VmProxies and makes use of a result handler when the commands are complete.
     *
     * @param vmJobMap      a mapping from a VmProxy to the JobStruct that should be pinged (requires jobId be set)
     * @param resultHandler the handler of the job status results (can be null)
     */
    public static void scatterPingJob(final Map<VmProxy, JobProxy> vmJobMap, final Handler<Set<ResultHolder<LinkedProcess.JobStatus>>> resultHandler) {
        final Set<ResultHolder<LinkedProcess.JobStatus>> resultHolders = new HashSet<ResultHolder<LinkedProcess.JobStatus>>();

        for (final VmProxy vmProxy : vmJobMap.keySet()) {
            Handler<LinkedProcess.JobStatus> pingResultHandler = new Handler<LinkedProcess.JobStatus>() {
                public void handle(LinkedProcess.JobStatus jobStatus) {
                    resultHolders.add(new ResultHolder<LinkedProcess.JobStatus>(jobStatus));
                    if (resultHolders.size() == vmJobMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };

            Handler<Error> pingErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.add(new ResultHolder<LinkedProcess.JobStatus>(lopError));
                    if (resultHolders.size() == vmJobMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };
            vmProxy.pingJob(vmJobMap.get(vmProxy), pingResultHandler, pingErrorHandler);
        }
    }

    /**
     * Scatter get-based manage_bindings commands to a set of VmProxies and waits for all commands to complete.
     *
     * @param vmBindingNamesMap a mapping from a VmProxy to the binding names that should be retrieved
     * @param timeout           the number of milliseconds to spend on this scatter before a TimeoutException is thrown (use -1 to wait indefinately)
     * @return a mapping from a VmProxy to the results of its get manage_bindings
     * @throws TimeoutException is thrown when the scatter takes longer than the provided timeout in milliseconds
     */
    public static Map<VmProxy, ResultHolder<VmBindings>> scatterGetBindings(final Map<VmProxy, Set<String>> vmBindingNamesMap, long timeout) throws TimeoutException {
        final Object monitor = new Object();
        final Map<VmProxy, ResultHolder<VmBindings>> resultHolders = new HashMap<VmProxy, ResultHolder<VmBindings>>();
        for (final VmProxy vmProxy : vmBindingNamesMap.keySet()) {
            Handler<VmBindings> scatterResultHandler = new Handler<VmBindings>() {
                public void handle(VmBindings vmBindings) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(vmBindings));
                    if (resultHolders.size() == vmBindingNamesMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };

            Handler<Error> scatterErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(lopError));
                    if (resultHolders.size() == vmBindingNamesMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };
            vmProxy.getBindings(vmBindingNamesMap.get(vmProxy), scatterResultHandler, scatterErrorHandler);
        }

        ScatterGatherPattern.monitorSleep(monitor, timeout);
        if (resultHolders.size() != vmBindingNamesMap.size())
            throw new TimeoutException("scatter get manage_bindings timedout after " + timeout + "ms.");
        return resultHolders;

    }

    /**
     * Scatter get-based manage_bindings comannds to a set of VmProxies and make use of a result handler when the commands are complete.
     *
     * @param vmBindingNamesMap a mapping from a VmProxy to the binding names that should be retrieved
     * @param resultHandler     the handler of the results (can be null)
     */
    public static void scatterGetBindings(final Map<VmProxy, Set<String>> vmBindingNamesMap, final Handler<Map<VmProxy, ResultHolder<VmBindings>>> resultHandler) {
        final Map<VmProxy, ResultHolder<VmBindings>> resultHolders = new HashMap<VmProxy, ResultHolder<VmBindings>>();

        for (final VmProxy vmProxy : vmBindingNamesMap.keySet()) {
            Handler<VmBindings> bindingsResultHandler = new Handler<VmBindings>() {
                public void handle(VmBindings vmBindings) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(vmBindings));
                    if (resultHolders.size() == vmBindingNamesMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };

            Handler<Error> bindingsErrorHandler = new Handler<Error>() {
                public void handle(org.linkedprocess.Error lopError) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(lopError));
                    if (resultHolders.size() == vmBindingNamesMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };
            vmProxy.getBindings(vmBindingNamesMap.get(vmProxy), bindingsResultHandler, bindingsErrorHandler);
        }
    }


    /**
     * Scatter set-based manage_bindings commands to a set of VmProxies and waits for all commands to complete.
     *
     * @param vmBindingsMap a mapping from a VmProxy to the bindings that should be set
     * @param timeout       the number of milliseconds to spend on this scatter before a TimeoutException is thrown (use -1 to wait indefinately)
     * @return a mapping from a VmProxy to the results of its get manage_bindings
     * @throws TimeoutException is thrown when the scatter takes longer than the provided timeout in milliseconds
     */
    public static Map<VmProxy, ResultHolder<VmBindings>> scatterSetBindings(final Map<VmProxy, VmBindings> vmBindingsMap, long timeout) throws TimeoutException {
        final Object monitor = new Object();
        final Map<VmProxy, ResultHolder<VmBindings>> resultHolders = new HashMap<VmProxy, ResultHolder<VmBindings>>();
        for (final VmProxy vmProxy : vmBindingsMap.keySet()) {
            Handler<VmBindings> scatterResultHandler = new Handler<VmBindings>() {
                public void handle(VmBindings vmBindings) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(vmBindings));
                    if (resultHolders.size() == vmBindingsMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };

            Handler<Error> scatterErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(lopError));
                    if (resultHolders.size() == vmBindingsMap.size()) {
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                }
            };
            vmProxy.setBindings(vmBindingsMap.get(vmProxy), scatterResultHandler, scatterErrorHandler);
        }

        ScatterGatherPattern.monitorSleep(monitor, timeout);
        if (resultHolders.size() != vmBindingsMap.size())
            throw new TimeoutException("scatter set manage_bindings timedout after " + timeout + "ms.");
        return resultHolders;

    }

    /**
     * Scatter set-based manage_bindings comannds to a set of VmProxies and make use of a result handler when the commands are complete.
     *
     * @param vmBindingsMap a mapping from a VmProxy to the bindings that should be set
     * @param resultHandler the handler of the results (can be null)
     */
    public static void scatterSetBindings(final Map<VmProxy, VmBindings> vmBindingsMap, final Handler<Map<VmProxy, ResultHolder<VmBindings>>> resultHandler) {
        final Map<VmProxy, ResultHolder<VmBindings>> resultHolders = new HashMap<VmProxy, ResultHolder<VmBindings>>();

        for (final VmProxy vmProxy : vmBindingsMap.keySet()) {
            Handler<VmBindings> bindingsResultHandler = new Handler<VmBindings>() {
                public void handle(VmBindings vmBindings) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(vmBindings));
                    if (resultHolders.size() == vmBindingsMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };

            Handler<Error> bindingsErrorHandler = new Handler<Error>() {
                public void handle(Error lopError) {
                    resultHolders.put(vmProxy, new ResultHolder<VmBindings>(lopError));
                    if (resultHolders.size() == vmBindingsMap.size()) {
                        resultHandler.handle(resultHolders);
                    }
                }
            };
            vmProxy.setBindings(vmBindingsMap.get(vmProxy), bindingsResultHandler, bindingsErrorHandler);
        }
    }

}

