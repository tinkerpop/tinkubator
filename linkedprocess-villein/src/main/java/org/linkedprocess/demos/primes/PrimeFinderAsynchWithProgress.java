/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.demos.primes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.linkedprocess.LinkedProcess;

import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.LopVillein;
import org.linkedprocess.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.villein.patterns.TimeoutException;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.JobStruct;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.VmProxy;

/**
 * PrimeFinder will find the set of all prime values between some start and end
 * integer range. The integer range is segmented into intervals that are
 * dependent upon how many virtual machines are spawned. The integer ranges are
 * distributed to the spawned virtual machines for primality testing. The
 * virtual machines execute Groovy code and create an array of all primes found
 * in their interval range. During the execution, the different LoPVMs are
 * polled for the VMBinding "meter" and displayed on the console. When the
 * binding on all VMs has reached 100, the result is displayed
 * 
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Peter Neubauer
 * @version LoPSideD 0.1
 */
public class PrimeFinderAsynchWithProgress {

	private static Map<VmProxy, JobStruct> vmJobMap;

	public static void findAsynch(int startInteger, int endInteger,
			int farmCount, int vmsPerFarm, String username, String password,
			String server, int port, Double meterMax, long pollingInterval, final long startTime) throws Exception {

		LopVillein villein = new LopVillein(server, port, username, password);
		villein.createLopCloudFromRoster();

		// ////////////// ALLOCATE FARMS

		System.out.println("Waiting for " + farmCount + " available farms...");
		Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(
				villein.getLopCloud(), farmCount, 200000);
		for (FarmProxy farmProxy : farmProxies) {
			System.out.println("farm allocated: " + farmProxy.getJid());
		}

		// ////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

		Set<ResultHolder<VmProxy>> vmProxies = ScatterGatherPattern
				.scatterSpawnVm(farmProxies, "groovy", vmsPerFarm, -1);
		System.out.println(vmProxies.size()
				+ " virtual machines have been spawned...");

		// ////////////// DISTRIBUTE PRIME FINDER FUNCTION DEFINITION

		vmJobMap = new HashMap<VmProxy, JobStruct>();
		for (ResultHolder<VmProxy> vmProxyResult : vmProxies) {
			JobStruct jobStruct = new JobStruct();
			jobStruct
					.setExpression(LinkedProcess
							.convertStreamToString(PrimeFinderAsynchWithProgress.class
									.getResourceAsStream("findPrimesWithProgress.groovy")));
			vmJobMap.put(vmProxyResult.getResult(), jobStruct);
		}

		System.out
				.println("Scattering find primes function definition jobs...");
		vmJobMap = ScatterGatherPattern.scatterSubmitJob(vmJobMap, -1);
		// ////////////// DISTRIBUTE VM BINDINGS
		System.out.println("Scattering vm bindings...");
		VmBindings bindings = new VmBindings();
		bindings.putTyped("meter", new TypedValue(
				VmBindings.XMLSchemaDatatype.DOUBLE, "" + 0));
		Map<VmProxy, VmBindings> bindingsMap = new HashMap<VmProxy, VmBindings>();
		for (ResultHolder<VmProxy> proxy : vmProxies) {
			bindingsMap.put(proxy.getResult(), bindings);
		}
		try {
			ScatterGatherPattern.scatterSetBindings(bindingsMap, 20000);
		} catch (TimeoutException tue) {
			//don't do anything, this is from the receiving TODO in LoPVilleinListener
		}
		// ////////////// DISTRIBUTE PRIME FINDER FUNCTION CALLS

		int intervalInteger = Math.round((endInteger - startInteger)
				/ vmJobMap.keySet().size());
		int currentStartInteger = startInteger;
		for (VmProxy vmProxy : vmJobMap.keySet()) {
			int currentEndInteger = currentStartInteger + intervalInteger;
			if (currentEndInteger > endInteger)
				currentEndInteger = endInteger;
			JobStruct jobStruct = new JobStruct();
			jobStruct.setExpression("findPrimes(" + currentStartInteger + ", "
					+ currentEndInteger + ")");
			vmJobMap.put(vmProxy, jobStruct);
			currentStartInteger = currentEndInteger + 1;
		}

		System.out.println("Scattering find primes function call jobs...");
		ScatterGatherPattern.scatterSubmitJob(vmJobMap,
				new Handler<Map<VmProxy, JobStruct>>() {

					public void handle(Map<VmProxy, JobStruct> t) {
						// TODO Auto-generated method stub
						// ////////////// TERMINATE ALL SPAWNED VIRTUAL MACHINES

						System.out.println("Terminating virtual machines...");
						ScatterGatherPattern.scatterTerminateVm(vmJobMap
								.keySet());

						// ////////////// SORT AND DISPLAY JOB RESULT PRIME
						// VALUES

						System.out
								.println("Gathering find primes function results...");
						ArrayList<Integer> primes = new ArrayList<Integer>();
						for (JobStruct jobStruct : vmJobMap.values()) {
							if (jobStruct.wasSuccessful()) {
								for (String primeString : jobStruct.getResult()
										.replace("[", "").replace("]", "")
										.split(",")) {
									if (!primeString.trim().equals(""))
										primes.add(Integer.valueOf(primeString
												.trim()));
								}
							} else {
								System.out.println("Job "
										+ jobStruct.getJobId()
										+ " was unsuccessful.");
							}
						}
						Collections.sort(primes);
						System.out.println("Running time: "
								+ (System.currentTimeMillis() - startTime)
								/ 1000.0f + " seconds.");
						System.out.println("Prime finder results: " + primes);

					}

				});


		// ////////////// START POLLING THE BINDINGS
		ExecutorService pool = Executors.newFixedThreadPool(vmProxies.size());
		Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		for (ResultHolder<VmProxy> vmProxyResult : vmProxies) {
			tasks.add(Executors.callable(new VmPollProgressTask(vmProxyResult
					.getResult(), meterMax, pollingInterval)));
		}
		List<Future<Object>> invokeAll = pool.invokeAll(tasks);
	}

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(PrimeFinder.class.getResourceAsStream("../demo.properties"));
		String username = props.getProperty("username");
		String password = props.getProperty("password");
		String server = props.getProperty("server");
		int port = Integer.valueOf(props.getProperty("port"));

		int startInteger = Integer.valueOf(props
				.getProperty("primeFinder.startInteger"));
		int endInteger = Integer.valueOf(props
				.getProperty("primeFinder.endInteger"));
		int farmCount = Integer.valueOf(props
				.getProperty("primeFinder.farmCount"));
		int vmsPerFarm = Integer.valueOf(props
				.getProperty("primeFinder.vmsPerFarm"));
		Double meterMax = Double
				.valueOf(props.getProperty("progressPolling.meterMax"));
		Long pollingInterval = Long.valueOf(props
				.getProperty("progressPolling.pollingInterval"));

		long startTime = System.currentTimeMillis();
		PrimeFinderAsynchWithProgress.findAsynch(startInteger, endInteger,
				farmCount, vmsPerFarm, username, password, server, port, meterMax, pollingInterval, startTime);
	}

}
