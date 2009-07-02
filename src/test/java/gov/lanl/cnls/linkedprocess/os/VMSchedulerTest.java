package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Author: josh
 * Date: Jun 29, 2009
 * Time: 3:21:06 PM
 */
public class VMSchedulerTest extends TestCase {
    private static final int MAX_RANDOM_INT = 100000;
    private static final String VM_TYPE = "JavaScript";

    private final VMScheduler.VMResultHandler resultHandler = createResultHandler();
    private final VMScheduler.LopStatusEventHandler eventHandler = createEventHandler();
    private final Map<String, JobResult> resultsByID = new HashMap<String, JobResult>();
    private final List<LinkedProcess.FarmStatus> farmStatusEvents = new LinkedList<LinkedProcess.FarmStatus>();
    private final List<String> vmStatusEventJIDs = new LinkedList<String>();
    private final List<LinkedProcess.VMStatus> vmStatusEventTypes = new LinkedList<LinkedProcess.VMStatus>();
    private VMScheduler scheduler;
    private Random random = new Random();

    public void setUp() {
        resultsByID.clear();
        farmStatusEvents.clear();
        vmStatusEventTypes.clear();
    }

    public void tearDown() {
    }

    public void testCreateAndShutDownScheduler() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        assertEquals(LinkedProcess.FarmStatus.ACTIVE, scheduler.getSchedulerStatus());
        scheduler.shutDown();
    }

    public void testCreateVM() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        scheduler.shutDown();
    }

    public void testAddMultipleVMs() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        String vm2 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        assertEquals(LinkedProcess.VMStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.spawnVirtualMachine(vm2, VM_TYPE);
        assertEquals(LinkedProcess.VMStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm2));
        assertEquals(LinkedProcess.FarmStatus.ACTIVE, scheduler.getSchedulerStatus());
        scheduler.shutDown();
    }

    public void testSchedulerStatusAfterShutdown() {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        scheduler.shutDown();
        assertEquals(LinkedProcess.FarmStatus.TERMINATING, scheduler.getSchedulerStatus());
    }

    public void testVMStatusAfterTermination() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        assertEquals(LinkedProcess.VMStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.terminateVirtualMachine(vm1);
        assertEquals(LinkedProcess.VMStatus.DOES_NOT_EXIST, scheduler.getVirtualMachineStatus(vm1));
        scheduler.shutDown();
    }

    public void testVMStatusAfterSchedulerShutDown() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        assertEquals(LinkedProcess.VMStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.shutDown();
        assertEquals(LinkedProcess.VMStatus.DOES_NOT_EXIST, scheduler.getVirtualMachineStatus(vm1));
    }

    public void testAddJob() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job = randomShortRunningJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertNormalResult(job);
        scheduler.shutDown();
    }

    public void testLongRunningJob() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertNormalResult(job);
        scheduler.shutDown();
    }

    public void testAddMultipleJobs() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job1 = randomShortRunningJob(vm1);
        Job job2 = randomShortRunningJob(vm1);
        scheduler.scheduleJob(vm1, job1);
        scheduler.scheduleJob(vm1, job2);
        scheduler.waitUntilFinished();
        assertEquals(2, resultsByID.size());
        assertNormalResult(job1);
        assertNormalResult(job2);
        scheduler.shutDown();
    }

    public void testAddConcurrentLongRunningJobs() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job1 = randomLongRunningJob(vm1);
        Job job2 = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm1, job1);
        scheduler.scheduleJob(vm1, job2);
        scheduler.waitUntilFinished();
        assertEquals(2, resultsByID.size());
        assertNormalResult(job1);
        assertNormalResult(job2);
        scheduler.shutDown();
    }

    public void testInvalidExpression() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job = randomInvalidJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        JobResult result = resultsByID.get(job.getJobId());
        assertEquals(JobResult.ResultType.ERROR, result.getType());
        assertNotNull(result.getException());
        assertEquals(null, result.getExpression());
        scheduler.shutDown();
    }

    public void testValidButExceptionGeneratingExpression() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job = randomValidButExceptionGeneratingJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertErrorResult(job);
        scheduler.shutDown();
    }

    public void testAbortLongRunningJob() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        Job job = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.abortJob(vm1, job.getJobId());
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertCancelledResult(job);
        scheduler.shutDown();
    }

    public void testMultipleVMs() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, VM_TYPE);
        String vm2 = randomJID();
        scheduler.spawnVirtualMachine(vm2, VM_TYPE);
        String vm3 = randomJID();
        scheduler.spawnVirtualMachine(vm3, VM_TYPE);
        Job job1 = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm1, job1);
        Job job2 = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm1, job2);
        Job job3 = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm2, job3);
        Job job4 = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm2, job4);
        Job job5 = randomLongRunningJob(vm1);
        scheduler.scheduleJob(vm2, job5);
        scheduler.waitUntilFinished();
        assertEquals(5, resultsByID.size());
        assertNormalResult(job1);
        assertNormalResult(job2);
        assertNormalResult(job3);
        assertNormalResult(job4);
        assertNormalResult(job5);
        scheduler.shutDown();
    }

    public void testSchedulerAndVMStatus() throws Exception {
        String[] vmJIDs = new String[VMScheduler.MAX_VM];

        // Instantiate scheduler.
        scheduler = new VMScheduler(resultHandler, eventHandler);
        assertEquals(1, farmStatusEvents.size());
        assertEquals(LinkedProcess.FarmStatus.ACTIVE, farmStatusEvents.get(0));
        assertEquals(LinkedProcess.FarmStatus.ACTIVE, scheduler.getSchedulerStatus());
        assertEquals(0, vmStatusEventTypes.size());

        // Add some VMs
        for (int i = 0; i < VMScheduler.MAX_VM; i++) {
            String jid = randomJID();
            vmJIDs[i] = jid;
            scheduler.spawnVirtualMachine(jid, VM_TYPE);
            int index = vmStatusEventTypes.size() - 1;
            assertEquals(jid, vmStatusEventJIDs.get(index));
            assertEquals(LinkedProcess.VMStatus.ACTIVE, vmStatusEventTypes.get(index));
            assertEquals(LinkedProcess.VMStatus.ACTIVE, scheduler.getVirtualMachineStatus(jid));

            // The very last VM added should make the scheduler full.
            if (VMScheduler.MAX_VM - 1 == i) {
                assertEquals(2, farmStatusEvents.size());
                assertEquals(LinkedProcess.FarmStatus.ACTIVE_FULL, farmStatusEvents.get(1));
                assertEquals(LinkedProcess.FarmStatus.ACTIVE_FULL, scheduler.getSchedulerStatus());
            } else {
                assertEquals(1, farmStatusEvents.size());
                assertEquals(LinkedProcess.FarmStatus.ACTIVE, scheduler.getSchedulerStatus());
            }
        }

        // Remove a VM
        int vmEventsBefore = vmStatusEventJIDs.size();
        int removedVMIndex = vmJIDs.length / 2;
        scheduler.terminateVirtualMachine(vmJIDs[removedVMIndex]);
        int index = vmStatusEventTypes.size() - 1;
        assertEquals(vmJIDs[removedVMIndex], vmStatusEventJIDs.get(index));
        assertEquals(LinkedProcess.VMStatus.DOES_NOT_EXIST, vmStatusEventTypes.get(index));
        assertEquals(LinkedProcess.VMStatus.DOES_NOT_EXIST, scheduler.getVirtualMachineStatus(vmJIDs[removedVMIndex]));
        assertEquals(3, farmStatusEvents.size());
        assertEquals(LinkedProcess.FarmStatus.ACTIVE, farmStatusEvents.get(2));
        assertEquals(LinkedProcess.FarmStatus.ACTIVE, scheduler.getSchedulerStatus());

        // Shut down scheduler.
        scheduler.shutDown();
        assertEquals(4, farmStatusEvents.size());
        assertEquals(LinkedProcess.FarmStatus.TERMINATING, farmStatusEvents.get(3));
        assertEquals(LinkedProcess.FarmStatus.TERMINATING, scheduler.getSchedulerStatus());
        //assertEquals(0, vmStatusEventTypes.size());

        // All remaining VMs should have been automatically shut down (in no particular order).
        assertEquals(vmEventsBefore + VMScheduler.MAX_VM, vmStatusEventJIDs.size());
        List<String> vmJIDsColl = new ArrayList<String>(vmJIDs.length);
        Collections.addAll(vmJIDsColl, vmJIDs);
        for (int j = 1 + vmEventsBefore; j < vmStatusEventJIDs.size(); j++) {
            String jid = vmStatusEventJIDs.get(j);
            assertTrue(vmJIDsColl.contains(jid));
            assertEquals(LinkedProcess.VMStatus.DOES_NOT_EXIST, vmStatusEventTypes.get(j));
            assertEquals(LinkedProcess.VMStatus.DOES_NOT_EXIST, scheduler.getVirtualMachineStatus(jid));
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private VMScheduler.VMResultHandler createResultHandler() {
        return new VMScheduler.VMResultHandler() {

            public void handleResult(JobResult result) {
                resultsByID.put(result.getJob().getJobId(), result);
            }
        };
    }

    private VMScheduler.LopStatusEventHandler createEventHandler() {
        return new VMScheduler.LopStatusEventHandler() {

            public void schedulerStatusChanged(LinkedProcess.FarmStatus newStatus) {
                farmStatusEvents.add(newStatus);
            }

            public void virtualMachineStatusChanged(String vmJID, LinkedProcess.VMStatus newStatus) {
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
        assertEquals(null, result.getExpression());
    }

    private void assertCancelledResult(final Job job) {
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

    private Job randomInvalidJob(final String vmJID) {
        return randomJob(vmJID, "0...0;");
    }

    private Job randomValidButExceptionGeneratingJob(final String vmJID) {
        return randomJob(vmJID, "idontexist[2] = 0;");
    }
}
