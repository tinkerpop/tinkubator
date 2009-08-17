/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.demos.polling;

import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.patterns.BindingsChecker;
import org.linkedprocess.xmpp.villein.patterns.PollBindingsPattern;
import org.linkedprocess.xmpp.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.xmpp.villein.patterns.SynchronousPattern;
import org.linkedprocess.xmpp.villein.proxies.*;

import java.util.Set;

/**
 * This class demonstrates how to use the PollBindingPattern.
 * The following JavaScript job is submitted to a spawned virtual machine:
 *   var meter = 0.0;
 *   while(true) {
 *     meter = meter = 0.00000001;
 *   }
 * The meter binding is polled at an interval and checked against some desired binding.
 * When the desired binding is reached, the VM terminates and the binding is printed.
 * The purpose of the PollBindingsPattern is to allow you to monitor the states of variables
 * in a virtual machine as code is executing.
 * 
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ProgressPolling {

    public static void doProgressMeter(double meterMax, long pollingInterval, String username, String password, String server, int port) throws Exception {

        final Object monitor = new Object();
        XmppVillein villein = new XmppVillein(server, port, username, password);
        villein.createLopCloudFromRoster();

        //////////////// ALLOCATE FARMS

        System.out.println("Waiting for 1 available farm...");

        //CountrysideProxy testProxy = ResourceAllocationPattern.allocateCountryside(villein.getLopCloud(), "test_countryside@lanl.linkedprocess.org", -1);
        //Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(testProxy, 1, 20000);
        Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(villein.getLopCloud(), 1, 20000);
        farmProxies = LopCloud.filterFarmProxiesByPasswordRequired(farmProxies, false);
        if(farmProxies.size() == 0) {
            System.out.println("Could not allocate a password free farm.");
            System.exit(1);
        }
        for (FarmProxy farmProxy : farmProxies) {
            System.out.println("farm allocated: " + farmProxy.getFullJid());
        }

        //////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

        ResultHolder<VmProxy> vmProxyResult = SynchronousPattern.spawnVm(farmProxies.iterator().next(), "javascript", -1);
        System.out.println("virtual machine spawned: " + vmProxyResult.getResult().getFullJid());

        //////////////// DISTRIBUTE PROGRESS METER INCREMENTING CODE

        JobStruct jobStruct = new JobStruct();
        jobStruct.setExpression("var meter = 0.0;\n" +
                                "while(true) {\n" +
                                    "\tmeter = meter + 0.00000001;\n" +
                                "}");
        vmProxyResult.getResult().submitJob(jobStruct, null, null);

        BindingsChecker bc = new BindingsChecker() {
            public boolean areEquivalent(VMBindings actualBindings, VMBindings desiredBindings) {
                TypedValue actualValue = actualBindings.getTyped("meter");
                TypedValue desiredValue = desiredBindings.getTyped("meter");
                if(actualValue != null) {
                    Double actualDouble = Double.valueOf(actualValue.getValue());
                    Double desiredDouble = Double.valueOf(desiredValue.getValue());
                    System.out.println(actualDouble + " out of " + desiredDouble);
                    return(actualDouble != null && desiredDouble != null && actualDouble >= desiredDouble);
                } else {
                    return false;
                }
            }
        };
        Handler<VMBindings> resultHandler = new Handler<VMBindings>() {
            public void handle(VMBindings vmBindings) {
                System.out.println("progress meter value has been reached: " + vmBindings);
                synchronized(monitor) {
                    monitor.notify();
                }
                System.exit(0);
            }
        };
        Handler<LopError> errorHandler = new Handler<LopError>() {
            public void handle(LopError lopError) {
                System.out.println("an error has occured: " + lopError);
                synchronized(monitor) {
                    monitor.notify();
                }
                System.exit(1);
            }
        };
        PollBindingsPattern pb = new PollBindingsPattern();
        VMBindings desiredBindings = new VMBindings();
        desiredBindings.putTyped("meter", new TypedValue(VMBindings.XMLSchemaDatatype.DOUBLE, ""+meterMax));
        pb.startPattern(vmProxyResult.getResult(), desiredBindings, bc, resultHandler, errorHandler, pollingInterval);
        synchronized(monitor) {
            monitor.wait();
        }
        vmProxyResult.getResult().terminateVm(null, null);
    }

    public static void main(String[] args) throws Exception {
        double meterMax = 1.0d;
        long pollingInterval = 500;
        String username = "linked.process.1";
        String password = "linked12";
        String server = "lanl.linkedprocess.org";

        long startTime = System.currentTimeMillis();
        ProgressPolling.doProgressMeter(meterMax, pollingInterval, username, password, server, 5222);
        System.out.println("Running time: " + (System.currentTimeMillis() - startTime) / 1000.0f + " seconds.");
    }
}
