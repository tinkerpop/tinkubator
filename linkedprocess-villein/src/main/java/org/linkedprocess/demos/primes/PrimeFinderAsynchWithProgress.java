/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.demos.primes;

import java.io.IOException;
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

import org.jivesoftware.smack.XMPPException;
import org.linkedprocess.LinkedProcess;

import org.linkedprocess.farm.os.TypedValue;
import org.linkedprocess.farm.os.VmBindings;
import org.linkedprocess.farm.os.errors.InvalidValueException;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.villein.patterns.TimeoutException;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.JobProxy;
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

	public static Map<VmProxy, JobProxy> vmJobMap;
	public static Set<FarmProxy> farmProxies;
	public static Set<ResultHolder<VmProxy>> vmProxies;
	public static Villein villein;
	public static ExecutorService pool;

	public static void findAsynch(int startInteger, int endInteger,
			int farmCount, int vmsPerFarm, String username, String password,
			String server, int port, Double meterMax, long pollingInterval, final long startTime) throws Exception {

		init(username, password, server, port);

		// ////////////// ALLOCATE FARMS

		allocateFarms(farmCount);

		// ////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

		spawnVms(vmsPerFarm);

		// ////////////// DISTRIBUTE PRIME FINDER FUNCTION DEFINITION

		scatterPrimeCalcFunction();
		// ////////////// DISTRIBUTE VM BINDINGS
		scatterVmBindings();
		// ////////////// DISTRIBUTE PRIME FINDER FUNCTION CALLS

		scatterWorkerJob(startInteger, endInteger, startTime, new Handler<Map<VmProxy, JobProxy>>() {

			public void handle(Map<VmProxy, JobProxy> t) {
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
				for (JobProxy jobProxy : vmJobMap.values()) {
					if (jobProxy.wasSuccessful()) {
						for (String primeString : jobProxy.getResult()
								.replace("[", "").replace("]", "")
								.split(",")) {
							if (!primeString.trim().equals(""))
								primes.add(Integer.valueOf(primeString
										.trim()));
						}
					} else {
						System.out.println("Job "
								+ jobProxy.getJobId()
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

		poll(meterMax, pollingInterval);
	}

	public static void init(String username, String password, String server,
			int port) throws XMPPException {
		villein = new Villein(server, port, username, password);
		villein.createCloudFromRoster();
	}

	public static void poll(Double meterMax, long pollingInterval)
			throws InterruptedException {
		Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		for (ResultHolder<VmProxy> vmProxyResult : vmProxies) {
			tasks.add(Executors.callable(new VmPollProgressTask(vmProxyResult
					.getResult(), meterMax, pollingInterval)));
		}
		startPolling(tasks);
	}

	public static void startPolling(Collection<Callable<Object>> tasks) {
		pool = Executors.newFixedThreadPool(vmProxies.size());
		try {
			List<Future<Object>> invokeAll = pool.invokeAll(tasks);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void scatterWorkerJob(int startInteger, int endInteger,
			final long startTime, Handler<Map<VmProxy, JobProxy>> handler) {
		int intervalInteger = Math.round((endInteger - startInteger)
				/ vmJobMap.keySet().size());
		int currentStartInteger = startInteger;
		for (VmProxy vmProxy : vmJobMap.keySet()) {
			int currentEndInteger = currentStartInteger + intervalInteger;
			if (currentEndInteger > endInteger)
				currentEndInteger = endInteger;
			JobProxy jobProxy = new JobProxy();
			jobProxy.setExpression("findPrimes(" + currentStartInteger + ", "
					+ currentEndInteger + ")");
			vmJobMap.put(vmProxy, jobProxy);
			currentStartInteger = currentEndInteger + 1;
		}

		System.out.println("Scattering find primes function call jobs...");
		ScatterGatherPattern.scatterSubmitJob(vmJobMap,
				handler);
	}

	public static void scatterVmBindings() throws InvalidValueException {
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
	}

	public static void scatterPrimeCalcFunction() throws IOException,
			TimeoutException {
		vmJobMap = new HashMap<VmProxy, JobProxy>();
		for (ResultHolder<VmProxy> vmProxyResult : vmProxies) {
			JobProxy jobProxy = new JobProxy();
			jobProxy
					.setExpression(LinkedProcess
							.convertStreamToString(PrimeFinderAsynchWithProgress.class
									.getResourceAsStream("findPrimesWithProgress.groovy")));
			vmJobMap.put(vmProxyResult.getResult(), jobProxy);
		}

		System.out
				.println("Scattering find primes function definition jobs...");
		vmJobMap = ScatterGatherPattern.scatterSubmitJob(vmJobMap, -1);
	}

	public static void spawnVms(int vmsPerFarm) throws TimeoutException {
		vmProxies = ScatterGatherPattern
				.scatterSpawnVm(farmProxies, "groovy", vmsPerFarm, -1);
		System.out.println(vmProxies.size()
				+ " virtual machines have been spawned...");
	}

	public static void allocateFarms(int farmCount) throws TimeoutException {
		System.out.println("Waiting for " + farmCount + " available farms...");
		farmProxies = ResourceAllocationPattern.allocateFarms(
				villein.getCloud(), farmCount, 200000);
		for (FarmProxy farmProxy : farmProxies) {
			System.out.println("farm allocated: " + farmProxy.getFullJid());
		}
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
