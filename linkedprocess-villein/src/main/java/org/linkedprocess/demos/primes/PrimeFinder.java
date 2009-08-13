package org.linkedprocess.demos.primes;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.patterns.SynchronousPattern;
import org.linkedprocess.xmpp.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.LinkedProcess;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import java.util.*;

/**
 * User: marko Date: Jul 28, 2009 Time: 11:35:49 AM
 */
public class PrimeFinder {


	public static List<Integer> findPrimesUsingLop(int startInteger, int endInteger, int farmCount, int vmsPerFarm, String username, String password, String server, int port) throws Exception {


		XmppVillein villein = new XmppVillein(server, port, username, password);
		villein.createLopCloudFromRoster();
        SynchronousPattern sp = new SynchronousPattern();
        ScatterGatherPattern sg = new ScatterGatherPattern();

        //////////////// WAIT FOR ACTIVE FARMS

        System.out.println("Waiting for " + farmCount + " available farms...");
        sp.waitForFarms(villein.getLopCloud(), farmCount, 10000);
        for (FarmProxy farm : villein.getLopCloud().getFarmProxies()) {
			System.out.println("found farm: " + farm.getFullJid());
		}

        //////////////// SPAWN VIRTUAL MACHINES ON ACTIVE FARMS

        Set<VmProxy> vmProxies = sg.scatterSpawnVm(villein.getLopCloud().getFarmProxies(), "groovy", vmsPerFarm);
        System.out.println(vmProxies.size() + " virtual machines have been spawned...");


        //////////////// DISTRIBUTE PRIME FINDER FUNCTION DEFINITION

        Map<VmProxy, JobStruct> vmJobMap = new HashMap<VmProxy, JobStruct>();
        for(VmProxy vmProxy : vmProxies) {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setExpression(LinkedProcess.convertStreamToString(PrimeFinder.class.getResourceAsStream("findPrimes.groovy")));
            vmJobMap.put(vmProxy, jobStruct);
        }

        System.out.println("Scattering find primes function definition jobs...");
        vmJobMap = sg.scatterSubmitJob(vmJobMap);


        //////////////// DISTRIBUTE PRIME FINDER FUNCTION CALLS

        int intervalInteger = Math.round((endInteger - startInteger) / vmJobMap.keySet().size());
        int currentStartInteger = startInteger;
        for(VmProxy vmProxy : vmJobMap.keySet()) {
            int currentEndInteger = currentStartInteger + intervalInteger;
            if(currentEndInteger > endInteger)
                currentEndInteger = endInteger;
            JobStruct jobStruct = new JobStruct();
            jobStruct.setExpression("findPrimes(" + currentStartInteger + ", " + currentEndInteger + ")");
            vmJobMap.put(vmProxy, jobStruct);
            currentStartInteger = currentEndInteger + 1;
        }
        System.out.println("Scattering find primes function call jobs...");
        vmJobMap = sg.scatterSubmitJob(vmJobMap);


        //////////////// TERMINATE ALL SPAWNED VIRTUAL MACHINES

        System.out.println("Terminating virtual machines...");
        for(VmProxy vmProxy : vmJobMap.keySet()) {
            vmProxy.terminateVm(null, null);
        }


        //////////////// SORT AND DISPLAY JOB RESULT PRIME VALUES

        System.out.println("Gathering find primes function results...");
        ArrayList<Integer> primes = new ArrayList<Integer>();
        for(JobStruct jobStruct : vmJobMap.values()) {
            if(jobStruct.wasSuccessful()) {
                for(String primeString : jobStruct.getResult().replace("[","").replace("]","").split(",")) {
                    if(!primeString.trim().equals(""))
                        primes.add(Integer.valueOf(primeString.trim()));
                }
            } else {
                System.out.println("Job " + jobStruct.getJobId() + " was unsuccessful.");
            }
        }
        Collections.sort(primes);
        return primes;
    }

    public static Object findPrimesUsingLocalMachine(int startInteger, int endInteger) throws Exception {
        ScriptEngineManager sm = new ScriptEngineManager();
        ScriptEngine groovy = sm.getEngineByName("groovy");
        groovy.eval(LinkedProcess.convertStreamToString(PrimeFinder.class.getResourceAsStream("findPrimes.groovy")));
        return groovy.eval("findPrimes(" + startInteger + ", " + endInteger +  ")");
    }

    public static void main(String[] args) throws Exception {

        int startInteger = 1;
        int endInteger = 1000;
        int farmCount = 3;
        int vmsPerFarm = 1;
        
        long startTime = System.currentTimeMillis();
        System.out.println("Prime LoP results: " + PrimeFinder.findPrimesUsingLop(startInteger, endInteger, farmCount, vmsPerFarm, "linked.process.1", "linked12", "lanl.linkedprocess.org", 5222));
        System.out.println("Running time: " + ((float)System.currentTimeMillis() - (float)startTime)/1000.0f + " seconds.");
        startTime = System.currentTimeMillis();
        System.out.println("Prime local results: " + PrimeFinder.findPrimesUsingLocalMachine(startInteger, endInteger));
        System.out.println("Running time: " + ((float)System.currentTimeMillis() - (float)startTime)/1000.0f + " seconds.");
    }


}
