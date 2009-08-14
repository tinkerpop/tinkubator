package org.linkedprocess.demos.polling;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.patterns.*;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.TypedValue;

import java.util.Set;

/**
 * User: marko
 * Date: Aug 14, 2009
 * Time: 11:32:06 AM
 */
public class ProgressPolling {

    public static void findPrimesUsingLop(double meterMax, long pollingInterval, String username, String password, String server, int port) throws Exception {

        final Object monitor = new Object();
        XmppVillein villein = new XmppVillein(server, port, username, password);
        villein.createLopCloudFromRoster();

        //////////////// ALLOCATE FARMS

        System.out.println("Waiting for 1 available farms...");
        Set<FarmProxy> farmProxies = ResourceAllocationPattern.allocateFarms(villein.getLopCloud(), 1, 20000);
        for (FarmProxy farmProxy : farmProxies) {
            System.out.println("farm allocated: " + farmProxy.getFullJid());
        }

        //////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

        VmProxy vmProxy = SynchronousPattern.spawnVm(farmProxies.iterator().next(), "javascript", -1);
        System.out.println("virtual machine spawned: " + vmProxy.getFullJid());

        //////////////// DISTRIBUTE PROGRESS METER INCREMENTING CODE

        JobStruct jobStruct = new JobStruct();
        jobStruct.setExpression("var meter = 0.0;\n" +
                                "while(true) {\n" +
                                    "\tmeter = meter + 0.0000001;\n" +
                                "}");
        vmProxy.submitJob(jobStruct, null, null);

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
        pb.startPattern(vmProxy, desiredBindings, bc, resultHandler, errorHandler, pollingInterval);
        synchronized(monitor) {
            monitor.wait();
        }
        vmProxy.terminateVm(null, null);
    }

    public static void main(String[] args) throws Exception {
        double meterMax = 1.0d;
        long pollingInterval = 400;
        String username = "linked.process.1";
        String password = "linked12";
        String server = "lanl.linkedprocess.org";

        long startTime = System.currentTimeMillis();
        ProgressPolling.findPrimesUsingLop(meterMax, pollingInterval, username, password, server, 5222);
        System.out.println("Running time: " + (System.currentTimeMillis() - startTime) / 1000.0f + " seconds.");
    }
}
