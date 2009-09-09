package org.linkedprocess.farm.os;

import junit.framework.TestCase;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.errors.JobAlreadyExistsException;
import org.linkedprocess.farm.os.errors.JobNotFoundException;
import org.linkedprocess.farm.os.errors.VmAlreadyExistsException;
import org.linkedprocess.farm.os.errors.VmNotFoundException;

import java.util.*;

/**
 * Author: josh
 * Date: Jun 29, 2009
 * Time: 3:21:06 PM
 */
public class VmSchedulerTest extends TestCase {
    private static final int MAX_RANDOM_INT = 100000;

    private final VmScheduler.VmResultHandler resultHandler = createResultHandler();
    private final VmScheduler.LopStatusEventHandler eventHandler = createEventHandler();
    private final Map<String, JobResult> resultsByID = new HashMap<String, JobResult>();
    private final List<LinkedProcess.Status> farmStatusEvents = new LinkedList<LinkedProcess.Status>();
    private final List<String> vmStatusEventJIDs = new LinkedList<String>();
    private final List<LinkedProcess.Status> vmStatusEventTypes = new LinkedList<LinkedProcess.Status>();
    private VmScheduler scheduler;
    private Random random = new Random();

    public void setUp() {
        // Note: calling a LinkedProcess method simply ensures that its static initializer (part of whose job is
        //       to pre-load classes for scheduler threads) has already executed.
        LinkedProcess.getConfiguration();

        resultsByID.clear();
        farmStatusEvents.clear();
        vmStatusEventTypes.clear();
    }

    public void tearDown() {
    }

    public void testCreateAndShutDownScheduler() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getSchedulerStatus());
        scheduler.shutdown();
    }

    public void testCreateVm() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        scheduler.shutdown();
    }

    public void testAddMultipleVms() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        String vm2 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.spawnVirtualMachine(vm2, LinkedProcess.JAVASCRIPT);
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getVirtualMachineStatus(vm2));
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getSchedulerStatus());
        scheduler.shutdown();
    }

    public void testSchedulerStatusAfterShutdown() {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        scheduler.shutdown();
        assertEquals(LinkedProcess.Status.INACTIVE, scheduler.getSchedulerStatus());
    }

    public void testVmStatusAfterTermination() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.terminateVm(vm1);
        assertEquals(LinkedProcess.Status.INACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.shutdown();
    }

    public void testVmStatusAfterSchedulerShutDown() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.shutdown();
        assertEquals(LinkedProcess.Status.INACTIVE, scheduler.getVirtualMachineStatus(vm1));
    }

    public void testAddJob() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job = randomShortRunningJob(vm1);
        scheduler.submitJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertNormalResult(job);
        scheduler.shutdown();
    }

    public void testLongRunningJob() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job = randomLongRunningJob(vm1);
        scheduler.submitJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertNormalResult(job);
        scheduler.shutdown();
    }

    public void testAddMultipleJobs() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job1 = randomShortRunningJob(vm1);
        Job job2 = randomShortRunningJob(vm1);
        scheduler.submitJob(vm1, job1);
        scheduler.submitJob(vm1, job2);
        scheduler.waitUntilFinished();
        assertEquals(2, resultsByID.size());
        assertNormalResult(job1);
        assertNormalResult(job2);
        scheduler.shutdown();
    }

    public void testAddConcurrentLongRunningJobs() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job1 = randomLongRunningJob(vm1);
        Job job2 = randomLongRunningJob(vm1);
        scheduler.submitJob(vm1, job1);
        scheduler.submitJob(vm1, job2);
        scheduler.waitUntilFinished();
        assertEquals(2, resultsByID.size());
        assertNormalResult(job1);
        assertNormalResult(job2);
        scheduler.shutdown();
    }

    public void testInvalidExpression() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job = randomInvalidJob(vm1);
        scheduler.submitJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertErrorResult(job);
        scheduler.shutdown();
    }

    public void testValidButExceptionGeneratingExpression() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job = randomValidButExceptionGeneratingJob(vm1);
        scheduler.submitJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertErrorResult(job);
        scheduler.shutdown();
    }

    public void testAbortJob() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        Job job1, job2;

        // Add a job and immediately abort it
        job1 = randomInfiniteJob(vm1);
        scheduler.submitJob(vm1, job1);
        scheduler.abortJob(vm1, job1.getJobId());
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertAbortedResult(job1);

        // Add a job and wait until it is in progress before aborting it.
        job1 = randomInfiniteJob(vm1);
        scheduler.submitJob(vm1, job1);
        Object o = "";
        synchronized (o) {
            o.wait(100);
        }
        scheduler.abortJob(vm1, job1.getJobId());
        scheduler.waitUntilFinished();
        assertEquals(2, resultsByID.size());
        assertAbortedResult(job1);

        // Make sure other jobs can still complete normally.
        job2 = this.randomShortRunningJob(vm1);
        scheduler.submitJob(vm1, job2);
        scheduler.waitUntilFinished();
        assertEquals(3, resultsByID.size());
        assertNormalResult(job2);

        // Add two jobs, then abort them
        job1 = randomInfiniteJob(vm1);
        job2 = randomInfiniteJob(vm1);
        scheduler.submitJob(vm1, job1);
        scheduler.submitJob(vm1, job2);
        synchronized (o) {
            o.wait(100);
        }
        scheduler.abortJob(vm1, job1.getJobId());
        scheduler.abortJob(vm1, job2.getJobId());
        scheduler.waitUntilFinished();
        assertEquals(5, resultsByID.size());
        assertAbortedResult(job1);
        assertAbortedResult(job2);

        scheduler.shutdown();
    }

    public void testMultipleVms() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
        String vm2 = randomJID();
        scheduler.spawnVirtualMachine(vm2, LinkedProcess.JAVASCRIPT);
        String vm3 = randomJID();
        scheduler.spawnVirtualMachine(vm3, LinkedProcess.JAVASCRIPT);
        Job job1 = randomLongRunningJob(vm1);
        scheduler.submitJob(vm1, job1);
        Job job2 = randomLongRunningJob(vm1);
        scheduler.submitJob(vm1, job2);
        Job job3 = randomLongRunningJob(vm1);
        scheduler.submitJob(vm2, job3);
        Job job4 = randomLongRunningJob(vm1);
        scheduler.submitJob(vm2, job4);
        Job job5 = randomLongRunningJob(vm1);
        scheduler.submitJob(vm2, job5);
        scheduler.waitUntilFinished();
        assertEquals(5, resultsByID.size());
        assertNormalResult(job1);
        assertNormalResult(job2);
        assertNormalResult(job3);
        assertNormalResult(job4);
        assertNormalResult(job5);
        scheduler.shutdown();
    }

    public void testSchedulerAndVmStatus() throws Exception {
        String[] vmJIDs = new String[VmScheduler.MAX_VM];

        // Instantiate scheduler.
        scheduler = new VmScheduler(resultHandler, eventHandler);
        assertEquals(1, farmStatusEvents.size());
        assertEquals(LinkedProcess.Status.ACTIVE, farmStatusEvents.get(0));
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getSchedulerStatus());
        assertEquals(0, vmStatusEventTypes.size());

        // Add some VMs
        for (int i = 0; i < VmScheduler.MAX_VM; i++) {
            String jid = randomJID();
            vmJIDs[i] = jid;
            scheduler.spawnVirtualMachine(jid, LinkedProcess.JAVASCRIPT);
            int index = vmStatusEventTypes.size() - 1;
            assertEquals(jid, vmStatusEventJIDs.get(index));
            assertEquals(LinkedProcess.Status.ACTIVE, vmStatusEventTypes.get(index));
            assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getVirtualMachineStatus(jid));

            // The very last Vm added should make the scheduler full.
            if (VmScheduler.MAX_VM - 1 == i) {
                assertEquals(2, farmStatusEvents.size());
                assertEquals(LinkedProcess.Status.BUSY, farmStatusEvents.get(1));
                assertEquals(LinkedProcess.Status.BUSY, scheduler.getSchedulerStatus());
            } else {
                assertEquals(1, farmStatusEvents.size());
                assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getSchedulerStatus());
            }
        }

        // Remove a VM
        int vmEventsBefore = vmStatusEventJIDs.size();
        int removedVmIndex = vmJIDs.length / 2;
        scheduler.terminateVm(vmJIDs[removedVmIndex]);
        int index = vmStatusEventTypes.size() - 1;
        assertEquals(vmJIDs[removedVmIndex], vmStatusEventJIDs.get(index));
        assertEquals(LinkedProcess.Status.INACTIVE, vmStatusEventTypes.get(index));
        assertEquals(LinkedProcess.Status.INACTIVE, scheduler.getVirtualMachineStatus(vmJIDs[removedVmIndex]));
        assertEquals(3, farmStatusEvents.size());
        assertEquals(LinkedProcess.Status.ACTIVE, farmStatusEvents.get(2));
        assertEquals(LinkedProcess.Status.ACTIVE, scheduler.getSchedulerStatus());

        // Shut down scheduler.
        scheduler.shutdown();
        assertEquals(4, farmStatusEvents.size());
        assertEquals(LinkedProcess.Status.INACTIVE, farmStatusEvents.get(3));
        assertEquals(LinkedProcess.Status.INACTIVE, scheduler.getSchedulerStatus());
        //assertEquals(0, vmStatusEventTypes.size());

        // All remaining VMs should have been automatically shut down (in no particular order).
        assertEquals(vmEventsBefore + VmScheduler.MAX_VM, vmStatusEventJIDs.size());
        List<String> vmJIDsColl = new ArrayList<String>(vmJIDs.length);
        Collections.addAll(vmJIDsColl, vmJIDs);
        for (int j = 1 + vmEventsBefore; j < vmStatusEventJIDs.size(); j++) {
            String jid = vmStatusEventJIDs.get(j);
            assertTrue(vmJIDsColl.contains(jid));
            assertEquals(LinkedProcess.Status.INACTIVE, vmStatusEventTypes.get(j));
            assertEquals(LinkedProcess.Status.INACTIVE, scheduler.getVirtualMachineStatus(jid));
        }
    }

    public void testJobStatus() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);

        scheduler.shutdown();
    }

    public void testStatusErrors() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);

        // Try to spawn a VM with a duplicate VM.
        try {
            scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);
            assertTrue(false);
        } catch (VmAlreadyExistsException e) {
        }

        scheduler.terminateVm(vm1);

        // Try to terminate a non-existent virtual machine.
        try {
            scheduler.terminateVm(vm1);
            assertTrue(false);
        } catch (VmNotFoundException e) {
        }

        // Try to assign a job to a non-existent Vm.
        Job job1 = randomInfiniteJob(vm1);
        try {
            scheduler.submitJob(vm1, job1);
            assertTrue(false);
        } catch (VmNotFoundException e) {
        }

        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);

        // Try to check on a job that doesn't exist
        try {
            scheduler.getJobStatus(vm1, job1.getJobId());
            assertTrue(false);
        } catch (JobNotFoundException e) {
        }

        // Try to abort a non-existent job.
        try {
            scheduler.abortJob(vm1, job1.getJobId());
            assertTrue(false);
        } catch (JobNotFoundException e) {
        }

        // Try to schedule a job with a duplicate ID (before the original job has finished)
        scheduler.submitJob(vm1, job1);
        try {
            scheduler.submitJob(vm1, job1);
            assertTrue(false);
        } catch (JobAlreadyExistsException e) {
        }

        scheduler.shutdown();
    }

    public void testSecurity() throws Exception {
        scheduler = new VmScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, LinkedProcess.JAVASCRIPT);

//        String expr = "var url = \"http://example.org/\";" +
//                "var xmlhttp=new XMLHttpRequest();" +
//                "xmlhttp.onreadystatechange=state_Change;" +
//                "xmlhttp.open(\"GET\",url,true);" +
//                "xmlhttp.send(null);";
        //String expr = "java.io.File";
        //String expr = "importPackage(java.io)\n" + "File";
        //String expr = "java.lang.Math.PI;";
        //String expr = "f = new Packages.java.io.File(\"test.txt\");";
        String expr = "java.lang.System.shutdownFarm(0);";
        Job job1 = randomJob(vm1, expr);
        scheduler.submitJob(vm1, job1);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertPermissionDeniedResult(job1);
//        assertEquals("foo", resultsByID.get(job1.getJobId()).getException().getMessage());
//        assertEquals("foo", resultsByID.get(job1.getJobId()).getExpression());

        scheduler.shutdown();
    }

////////////////////////////////////////////////////////////////////////////

    private VmScheduler.VmResultHandler createResultHandler() {
        return new VmScheduler.VmResultHandler() {

            public void handleResult(JobResult result) {
                resultsByID.put(result.getJob().getJobId(), result);
            }
        };
    }

    private VmScheduler.LopStatusEventHandler createEventHandler() {
        return new VmScheduler.LopStatusEventHandler() {

            public void schedulerStatusChanged(LinkedProcess.Status newStatus) {
                farmStatusEvents.add(newStatus);
            }

            public void virtualMachineStatusChanged(String vmJID, LinkedProcess.Status newStatus) {
                vmStatusEventJIDs.add(vmJID);
                vmStatusEventTypes.add(newStatus);
            }
        };
    }

    private void assertNormalResult(final Job job) {
        JobResult result = resultsByID.get(job.getJobId());
        assertEquals(JobResult.ResultType.NORMAL_RESULT, result.getType());
// Note: not "1", but "1.0", as the resulting Object is a Double (for
// some reason).  This is not particularly important for the test.
        assertEquals("1.0", result.getExpression());
        assertNull(result.getException());
    }

    private void assertErrorResult(final Job job) {
        JobResult result = resultsByID.get(job.getJobId());
        assertEquals(JobResult.ResultType.ERROR, result.getType());
        assertNotNull(result.getException());
        assertNull(result.getExpression());
    }

    private void assertPermissionDeniedResult(final Job job) {
        JobResult result = resultsByID.get(job.getJobId());
        assertEquals(JobResult.ResultType.ERROR, result.getType());
        assertNotNull(result.getException());
        System.out.println("" + result.getException());
        assertTrue(result.getException() instanceof SecurityException);
        assertNull(result.getExpression());
    }

    private void assertAbortedResult(final Job job) {
        JobResult result = resultsByID.get(job.getJobId());
        assertEquals(JobResult.ResultType.ABORTED, result.getType());
        assertNull(result.getException());
        assertEquals(null, result.getExpression());
    }

    private String randomJID() {
        return "a" + random.nextInt(MAX_RANDOM_INT) + "@example.com";
    }

    private Job randomJob(final String vmJID,
                          final String expression) {
        String appJID = "?";
        String iqID = "job" + random.nextInt(MAX_RANDOM_INT);
        return new Job(vmJID, appJID, iqID, expression);
    }

    private Job randomShortRunningJob(final String vmJID) {
        return randomJob(vmJID, "1 + 0;");
    }

    private Job randomLongRunningJob(final String vmJID) {
        return randomJob(vmJID, "var p=1; for (i=0; i<100000; i++) {p *= 7; p /= 7;} p;");
    }

    private Job randomInfiniteJob(final String vmJID) {
        return randomJob(vmJID, "while (true) {var x = 1;}");
    }

    private Job randomInvalidJob(final String vmJID) {
        return randomJob(vmJID, "0...0;");
    }

    private Job randomValidButExceptionGeneratingJob(final String vmJID) {
        return randomJob(vmJID, "idontexist[2] = 0;");
    }
}
