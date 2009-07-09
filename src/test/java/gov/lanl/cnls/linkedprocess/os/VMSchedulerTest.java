package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.errors.JobAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException;
import gov.lanl.cnls.linkedprocess.os.errors.VMAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
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
    private static final String JAVASCRIPT = "JavaScript";

    private final VMScheduler.VMResultHandler resultHandler = createResultHandler();
    private final VMScheduler.LopStatusEventHandler eventHandler = createEventHandler();
    private final Map<String, JobResult> resultsByID = new HashMap<String, JobResult>();
    private final List<LinkedProcess.FarmStatus> farmStatusEvents = new LinkedList<LinkedProcess.FarmStatus>();
    private final List<String> vmStatusEventJIDs = new LinkedList<String>();
    private final List<LinkedProcess.VmStatus> vmStatusEventTypes = new LinkedList<LinkedProcess.VmStatus>();
    private VMScheduler scheduler;
    private Random random = new Random();

    /*
    //public static void main(final String[] args) throws Exception {
    public void testGetEngines() throws Exception {
        System.out.println("################################################################");
        Class c = ScriptEngineFactory.class;
        for (Class c2 : c.getClasses()) {
            System.out.println("class: " + c2);

        }
        for (Class c2 : c.getDeclaredClasses()) {
            System.out.println("declared class: " + c2);

        }
        new TempLoader();
        System.out.println("================================================================");



        ScriptEngineManager manager = new ScriptEngineManager(new TempLoader());
        for (ScriptEngineFactory f : manager.getEngineFactories()) {
            System.out.println("" + f.getEngineName());
        }
        System.out.println("################################################################");
        ScriptEngine engine = manager.getEngineByName(JAVASCRIPT);
        if (null == engine) {
            throw new UnsupportedScriptEngineException(JAVASCRIPT);
        }
    } */

    private class TempLoader extends ClassLoader {
        public TempLoader() throws ClassNotFoundException {
            super(VMSchedulerTest.class.getClassLoader());
            loadClass("com.sun.script.jython.JythonScriptEngine");
        }
    }

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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        scheduler.shutDown();
    }

    public void testAddMultipleVMs() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        String vm2 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        assertEquals(LinkedProcess.VmStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.spawnVirtualMachine(vm2, JAVASCRIPT);
        assertEquals(LinkedProcess.VmStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm2));
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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        assertEquals(LinkedProcess.VmStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.terminateVirtualMachine(vm1);
        assertEquals(LinkedProcess.VmStatus.NOT_FOUND, scheduler.getVirtualMachineStatus(vm1));
        scheduler.shutDown();
    }

    public void testVMStatusAfterSchedulerShutDown() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        assertEquals(LinkedProcess.VmStatus.ACTIVE, scheduler.getVirtualMachineStatus(vm1));
        scheduler.shutDown();
        assertEquals(LinkedProcess.VmStatus.NOT_FOUND, scheduler.getVirtualMachineStatus(vm1));
    }

    public void testAddJob() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        Job job = randomInvalidJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertErrorResult(job);
        scheduler.shutDown();
    }

    public void testValidButExceptionGeneratingExpression() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        Job job = randomValidButExceptionGeneratingJob(vm1);
        scheduler.scheduleJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertErrorResult(job);
        scheduler.shutDown();
    }

    public void testAbortJob() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        Job job = randomInfiniteJob(vm1);
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
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
        String vm2 = randomJID();
        scheduler.spawnVirtualMachine(vm2, JAVASCRIPT);
        String vm3 = randomJID();
        scheduler.spawnVirtualMachine(vm3, JAVASCRIPT);
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
            scheduler.spawnVirtualMachine(jid, JAVASCRIPT);
            int index = vmStatusEventTypes.size() - 1;
            assertEquals(jid, vmStatusEventJIDs.get(index));
            assertEquals(LinkedProcess.VmStatus.ACTIVE, vmStatusEventTypes.get(index));
            assertEquals(LinkedProcess.VmStatus.ACTIVE, scheduler.getVirtualMachineStatus(jid));

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
        assertEquals(LinkedProcess.VmStatus.NOT_FOUND, vmStatusEventTypes.get(index));
        assertEquals(LinkedProcess.VmStatus.NOT_FOUND, scheduler.getVirtualMachineStatus(vmJIDs[removedVMIndex]));
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
            assertEquals(LinkedProcess.VmStatus.NOT_FOUND, vmStatusEventTypes.get(j));
            assertEquals(LinkedProcess.VmStatus.NOT_FOUND, scheduler.getVirtualMachineStatus(jid));
        }
    }

    public void testJobStatus() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);

        scheduler.shutDown();
    }

    public void testStatusErrors() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);

        // Try to spawn a VM with a duplicate VM.
        try {
            scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);
            assertTrue(false);
        } catch (VMAlreadyExistsException e) {
        }

        scheduler.terminateVirtualMachine(vm1);

        // Try to terminate a non-existent virtual machine.
        try {
            scheduler.terminateVirtualMachine(vm1);
            assertTrue(false);
        } catch (VMWorkerNotFoundException e) {
        }

        // Try to assign a job to a non-existent VM.
        Job job1 = randomInfiniteJob(vm1);
        try {
            scheduler.scheduleJob(vm1, job1);
            assertTrue(false);
        } catch (VMWorkerNotFoundException e) {
        }

        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);

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
        scheduler.scheduleJob(vm1, job1);
        try {
            scheduler.scheduleJob(vm1, job1);
            assertTrue(false);
        } catch (JobAlreadyExistsException e) {
        }

        scheduler.shutDown();
    }

    public void testSecurity() throws Exception {
        scheduler = new VMScheduler(resultHandler, eventHandler);
        String vm1 = randomJID();
        scheduler.spawnVirtualMachine(vm1, JAVASCRIPT);

//        String expr = "var url = \"http://example.org/\";" +
//                "var xmlhttp=new XMLHttpRequest();" +
//                "xmlhttp.onreadystatechange=state_Change;" +
//                "xmlhttp.open(\"GET\",url,true);" +
//                "xmlhttp.send(null);";
        //String expr = "java.io.File";
        //String expr = "importPackage(java.io)\n" + "File";
        //String expr = "java.lang.Math.PI;";
        //String expr = "f = new Packages.java.io.File(\"test.txt\");";
        String expr = "java.lang.System.exit(0);";
        Job job1 = randomJob(vm1, expr);
        scheduler.scheduleJob(vm1, job1);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        assertErrorResult(job1);
//        assertEquals("foo", resultsByID.get(job1.getJobId()).getException().getMessage());
//        assertEquals("foo", resultsByID.get(job1.getJobId()).getExpression());

        scheduler.shutDown();
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

            public void virtualMachineStatusChanged(String vmJID, LinkedProcess.VmStatus newStatus) {
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
