/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.demos.primes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.patterns.BindingsChecker;
import org.linkedprocess.xmpp.villein.patterns.PollBindingsPattern;
import org.linkedprocess.xmpp.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.xmpp.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.ResultHolder;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;

/**
 * PrimeFinder will find the set of all prime values between some start and end
 * integer range. The integer range is segmented into intervals that are
 * dependent upon how many virtual machines are spawned. The integer ranges are
 * distributed to the spawned virtual machines for primality testing. The
 * virtual machines execute Groovy code and create an array of all primes found
 * in their interval range. The results are then returned to the PrimeFinder
 * class and the results are sorted and displayed.
 * 
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PrimeFinderAsynchWithProgress {

	private static Map<VmProxy, JobStruct> vmJobMap;
	private static long startTime;
	private static Object monitor = new Object();
	private static int meterMax = 100;
	private static long pollingInterval = 500;

	public static void findAsynch(int startInteger, int endInteger,
			int farmCount, int vmsPerFarm, String username, String password,
			String server, int port) throws Exception {

		XmppVillein villein = new XmppVillein(server, port, username, password);
		villein.createLopCloudFromRoster();

		// ////////////// ALLOCATE FARMS

		System.out.println("Waiting for " + farmCount + " available farms...");
		Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(
				villein.getLopCloud(), farmCount, 200000);
		for (FarmProxy farmProxy : farmProxies) {
			System.out.println("farm allocated: " + farmProxy.getFullJid());
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
			jobStruct.setExpression(LinkedProcess
					.convertStreamToString(PrimeFinderAsynchWithProgress.class
							.getResourceAsStream("findPrimesWithProgress.groovy")));
			vmJobMap.put(vmProxyResult.getResult(), jobStruct);
		}

		System.out
				.println("Scattering find primes function definition jobs...");
		vmJobMap = ScatterGatherPattern.scatterSubmitJob(vmJobMap, -1);

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
		 ScatterGatherPattern.scatterSubmitJob(vmJobMap, new
		 Handler<Map<VmProxy, JobStruct>>() {
					
		
		 public void handle(Map<VmProxy, JobStruct> t) {
		 // TODO Auto-generated method stub
		 //////////////// TERMINATE ALL SPAWNED VIRTUAL MACHINES
						
		 System.out.println("Terminating virtual machines...");
		 ScatterGatherPattern.scatterTerminateVm(vmJobMap.keySet());
						
		 //////////////// SORT AND DISPLAY JOB RESULT PRIME VALUES
						
		 System.out.println("Gathering find primes function results...");
		 ArrayList<Integer> primes = new ArrayList<Integer>();
		 for(JobStruct jobStruct : vmJobMap.values()) {
		 if(jobStruct.wasSuccessful()) {
		 for(String primeString :
		 jobStruct.getResult().replace("[","").replace("]","").split(",")) {
		 if(!primeString.trim().equals(""))
		 primes.add(Integer.valueOf(primeString.trim()));
		 }
		 } else {
		 System.out.println("Job " + jobStruct.getJobId() +
		 " was unsuccessful.");
		 }
		 }
		 Collections.sort(primes);
		 System.out.println("Running time: " + (System.currentTimeMillis() -
		 startTime)/1000.0f + " seconds.");
		 System.out.println("Prime finder results: " + primes);
						
		 }
		
		 });
		
		for (ResultHolder<VmProxy> vmProxyResult : vmProxies) {
			addVmProgress(vmProxyResult.getResult());
		}

	}

	private static void addVmProgress(VmProxy vmProxy) {
		BindingsChecker bc = new BindingsChecker() {
			public boolean areEquivalent(VMBindings actualBindings,
					VMBindings desiredBindings) {
				TypedValue actualValue = actualBindings.getTyped("meter");
				TypedValue desiredValue = desiredBindings.getTyped("meter");
				if (actualValue != null) {
					Double actualDouble = Double
							.valueOf(actualValue.getValue());
					Double desiredDouble = Double.valueOf(desiredValue
							.getValue());
					System.out.println(actualDouble + " out of "
							+ desiredDouble);
					return (actualDouble != null && desiredDouble != null && actualDouble >= desiredDouble);
				} else {
					return false;
				}
			}
		};
		Handler<VMBindings> resultHandler = new Handler<VMBindings>() {

			public void handle(VMBindings vmBindings) {
				System.out.println("progress meter value has been reached: "
						+ vmBindings);
				synchronized (monitor) {
					monitor.notify();
				}
				System.exit(0);
			}
		};
		Handler<LopError> errorHandler = new Handler<LopError>() {
			public void handle(LopError lopError) {
				System.out.println("an error has occured: " + lopError);
				synchronized (monitor) {
					monitor.notify();
				}
				System.exit(1);
			}
		};
		PollBindingsPattern pb = new PollBindingsPattern();
		VMBindings desiredBindings = new VMBindings();
		try {
			desiredBindings.putTyped("meter", new TypedValue(
					VMBindings.XMLSchemaDatatype.DOUBLE, "" + meterMax));
			pb.startPattern(vmProxy, desiredBindings, bc, resultHandler, errorHandler, pollingInterval);
			synchronized(monitor) {
				monitor.wait();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}
	public static void main(String[] args) throws Exception {
		int startInteger = 1;
		int endInteger = 50000;
		int farmCount = 1;
		int vmsPerFarm = 1;
		String username = "linked.process.1@gmail.com";
		String password = "linked12";
		String server = "talk.l.google.com";
//        String username = "linked.process.1";
//        String password = "linked12";
//        String server = "lanl.linkedprocess.org";


		startTime = System.currentTimeMillis();
		PrimeFinderAsynchWithProgress.findAsynch(startInteger, endInteger,
				farmCount, vmsPerFarm, username, password, server, 5222);
	}

}
