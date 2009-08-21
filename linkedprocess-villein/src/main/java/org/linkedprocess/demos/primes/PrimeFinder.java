/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.demos.primes;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.LopVillein;
import org.linkedprocess.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.JobStruct;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.VmProxy;

import java.util.*;

/**
 * PrimeFinder will find the set of all prime values between some start and end integer range.
 * The integer range is segmented into intervals that are dependent upon how many virtual machines are spawned.
 * The integer ranges are distributed to the spawned virtual machines for primality testing.
 * The virtual machines execute Groovy code and create an array of all primes found in their interval range.
 * The results are then returned to the PrimeFinder class and the results are sorted and displayed.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PrimeFinder {


    public static List<Integer> findPrimesUsingLop(int startInteger, int endInteger, int farmCount, int vmsPerFarm, String username, String password, String server, int port) throws Exception {


        LopVillein villein = new LopVillein(server, port, username, password);
        villein.createLopCloudFromRoster();

        //////////////// ALLOCATE FARMS

        System.out.println("Waiting for " + farmCount + " available farms...");
        Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(villein.getLopCloud(), farmCount, 20000);
        for (FarmProxy farmProxy : farmProxies) {
            System.out.println("farm allocated: " + farmProxy.getJid());
        }

        //////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

        Set<ResultHolder<VmProxy>> vmProxies = ScatterGatherPattern.scatterSpawnVm(farmProxies, "groovy", vmsPerFarm, -1);
        System.out.println(vmProxies.size() + " virtual machines have been spawned...");

        //////////////// DISTRIBUTE PRIME FINDER FUNCTION DEFINITION

        Map<VmProxy, JobStruct> vmJobMap = new HashMap<VmProxy, JobStruct>();
        for (ResultHolder<VmProxy> vmProxyResult : vmProxies) {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setExpression(LinkedProcess.convertStreamToString(PrimeFinder.class.getResourceAsStream("findPrimes.groovy")));
            vmJobMap.put(vmProxyResult.getResult(), jobStruct);
        }

        System.out.println("Scattering find primes function definition jobs...");
        vmJobMap = ScatterGatherPattern.scatterSubmitJob(vmJobMap, -1);


        //////////////// DISTRIBUTE PRIME FINDER FUNCTION CALLS

        int intervalInteger = Math.round((endInteger - startInteger) / vmJobMap.keySet().size());
        int currentStartInteger = startInteger;
        for (VmProxy vmProxy : vmJobMap.keySet()) {
            int currentEndInteger = currentStartInteger + intervalInteger;
            if (currentEndInteger > endInteger)
                currentEndInteger = endInteger;
            JobStruct jobStruct = new JobStruct();
            jobStruct.setExpression("findPrimes(" + currentStartInteger + ", " + currentEndInteger + ")");
            vmJobMap.put(vmProxy, jobStruct);
            currentStartInteger = currentEndInteger + 1;
        }
        System.out.println("Scattering find primes function call jobs...");
        vmJobMap = ScatterGatherPattern.scatterSubmitJob(vmJobMap, -1);


        //////////////// TERMINATE ALL SPAWNED VIRTUAL MACHINES

        System.out.println("Terminating virtual machines...");
        ScatterGatherPattern.scatterTerminateVm(vmJobMap.keySet());

        //////////////// SHUT DOWN THE VILLEIN

        System.out.println("Shutting down the villein...");
        villein.shutdown();

        //////////////// SORT AND DISPLAY JOB RESULT PRIME VALUES

        System.out.println("Gathering find primes function results...");
        ArrayList<Integer> primes = new ArrayList<Integer>();
        for (JobStruct jobStruct : vmJobMap.values()) {
            if (jobStruct.wasSuccessful()) {
                for (String primeString : jobStruct.getResult().replace("[", "").replace("]", "").split(",")) {
                    if (!primeString.trim().equals(""))
                        primes.add(Integer.valueOf(primeString.trim()));
                }
            } else {
                System.out.println("Job " + jobStruct.getJobId() + " was unsuccessful.");
            }
        }
        Collections.sort(primes);
        return primes;
    }


    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.load(PrimeFinder.class.getResourceAsStream("../demo.properties"));
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        String server = props.getProperty("server");
        int port = Integer.valueOf(props.getProperty("port"));

        int startInteger = Integer.valueOf(props.getProperty("primeFinder.startInteger"));
        int endInteger = Integer.valueOf(props.getProperty("primeFinder.endInteger"));
        int farmCount = Integer.valueOf(props.getProperty("primeFinder.farmCount"));
        int vmsPerFarm = Integer.valueOf(props.getProperty("primeFinder.vmsPerFarm"));

        long startTime = System.currentTimeMillis();
        System.out.println("Prime LoP results: " + PrimeFinder.findPrimesUsingLop(startInteger, endInteger, farmCount, vmsPerFarm, username, password, server, port));
        System.out.println("Running time: " + (System.currentTimeMillis() - startTime) / 1000.0f + " seconds.");

    }


}
