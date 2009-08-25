/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.demos.polling;

import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.Error;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.patterns.BindingsChecker;
import org.linkedprocess.villein.patterns.PollBindingsPattern;
import org.linkedprocess.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.villein.patterns.SynchronousPattern;
import org.linkedprocess.villein.proxies.*;

import java.util.Properties;
import java.util.Set;

/**
 * This class demonstrates how to use the PollBindingPattern.
 * The following JavaScript job is submitted to a spawned virtual machine:
 * var meter = 0.0;
 * while(true) {
 * meter = meter = 0.00000001;
 * }
 * The meter binding is polled at an interval and checked against some desired binding.
 * When the desired binding is reached, the VM terminates and the binding is printed.
 * The purpose of the PollBindingsPattern is to allow you to monitor the states of variables
 * in a virtual machine as code is executing.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ProgressPolling {

    public static void doProgressPolling(double meterMax, long pollingInterval, String username, String password, String server, int port) throws Exception {

        final Object monitor = new Object();
        Villein villein = new Villein(server, port, username, password);
        villein.createCloudFromRoster();

        //////////////// ALLOCATE FARMS

        System.out.println("Waiting for 1 available farm...");

        //CountrysideProxy testProxy = ResourceAllocationPattern.allocateCountryside(villein.getCloud(), "test_countryside@lanl.linkedprocess.org", -1);
        //Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(testProxy, 1, 20000);
        Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(villein.getCloud(), 1, 20000);
        farmProxies = ResourceAllocationPattern.filterFarmProxiesByPasswordRequired(farmProxies, false);
        if (farmProxies.size() == 0) {
            System.out.println("Could not allocate a password free farm.");
            System.exit(1);
        }
        for (FarmProxy farmProxy : farmProxies) {
            System.out.println("farm allocated: " + farmProxy.getFullJid());
        }

        //////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

        ResultHolder<VmProxy> vmProxyResult = SynchronousPattern.spawnVm(farmProxies.iterator().next(), "javascript", -1);
        System.out.println("virtual machine spawned: " + vmProxyResult.getResult().getVmId());

        //////////////// DISTRIBUTE PROGRESS METER INCREMENTING CODE

        JobStruct jobStruct = new JobStruct();
        jobStruct.setExpression("var meter = 0.0;\n" +
                "while(true) {\n" +
                "\tmeter = meter + 0.00000005;\n" +
                "}");
        vmProxyResult.getResult().submitJob(jobStruct, null, null);

        BindingsChecker bc = new BindingsChecker() {
            public boolean areEquivalent(VmBindings actualBindings, VmBindings desiredBindings) {
                TypedValue actualValue = actualBindings.getTyped("meter");
                TypedValue desiredValue = desiredBindings.getTyped("meter");
                if (actualValue != null) {
                    Double actualDouble = Double.valueOf(actualValue.getValue());
                    Double desiredDouble = Double.valueOf(desiredValue.getValue());
                    System.out.println(actualDouble + " out of " + desiredDouble);
                    return (actualDouble != null && desiredDouble != null && actualDouble >= desiredDouble);
                } else {
                    return false;
                }
            }
        };
        Handler<VmBindings> resultHandler = new Handler<VmBindings>() {
            public void handle(VmBindings vmBindings) {
                System.out.println("progress meter value has been reached: " + vmBindings);
                synchronized (monitor) {
                    monitor.notify();
                }
                System.exit(0);
            }
        };
        Handler<org.linkedprocess.Error> errorHandler = new Handler<Error>() {
            public void handle(Error lopError) {
                System.out.println("an error has occured: " + lopError);
                synchronized (monitor) {
                    monitor.notify();
                }
                System.exit(1);
            }
        };
        PollBindingsPattern pb = new PollBindingsPattern();
        VmBindings desiredBindings = new VmBindings();
        desiredBindings.putTyped("meter", new TypedValue(VmBindings.XMLSchemaDatatype.DOUBLE, "" + meterMax));
        pb.startPattern(vmProxyResult.getResult(), desiredBindings, bc, resultHandler, errorHandler, pollingInterval);
        synchronized (monitor) {
            monitor.wait();
        }
        System.out.println("Terminating: " + vmProxyResult.getResult());
        vmProxyResult.getResult().terminateVm(null, null);
        villein.shutdown();
    }

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.load(ProgressPolling.class.getResourceAsStream("../demo.properties"));
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        String server = props.getProperty("server");
        int port = Integer.valueOf(props.getProperty("port"));

        double meterMax = Double.valueOf(props.getProperty("progressPolling.meterMax"));
        long pollingInterval = Long.valueOf(props.getProperty("progressPolling.pollingInterval"));

        long startTime = System.currentTimeMillis();
        ProgressPolling.doProgressPolling(meterMax, pollingInterval, username, password, server, port);
        System.out.println("Running time: " + (System.currentTimeMillis() - startTime) / 1000.0f + " seconds.");
    }
}
