package gov.lanl.cnls.linkedprocess.os;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Author: josh
 * Date: Jun 29, 2009
 * Time: 3:21:06 PM
 */
public class VMSchedulerTest extends TestCase {
    private final VMScheduler.VMResultHandler resultHandler = createResultHandler();
    private final Map<String, JobResult> resultsByID = new HashMap<String, JobResult>();
    private final Object waitMonitor = "";
    private VMScheduler scheduler;
    private Random random = new Random();
    private String vmType = "JavaScript";

    public void setUp() {
        resultsByID.clear();
    }

    public void tearDown() {
    }

    public void testCreateAndShutDownScheduler() {
        scheduler = new VMScheduler(resultHandler);
        assertEquals(VMScheduler.SchedulerStatus.ACTIVE, scheduler.getSchedulerStatus());
        scheduler.shutDown();
    }

    public void testCreateVM() throws ServiceRefusedException {
        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        scheduler.addMachine(vm1, vmType);
        scheduler.shutDown();
    }

    public void testAddMultipleVMs() throws ServiceRefusedException {
        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        String vm2 = randomJID();
        scheduler.addMachine(vm1, vmType);
        assertEquals(VMScheduler.VMStatus.ACTIVE, scheduler.getVMStatus(vm1));
        scheduler.addMachine(vm2, vmType);
        assertEquals(VMScheduler.VMStatus.ACTIVE, scheduler.getVMStatus(vm2));
        assertEquals(VMScheduler.SchedulerStatus.ACTIVE, scheduler.getSchedulerStatus());
        scheduler.shutDown();
    }

    public void testSchedulerStatusAfterShutdown() {
        scheduler = new VMScheduler(resultHandler);
        scheduler.shutDown();
        assertEquals(VMScheduler.SchedulerStatus.TERMINATED, scheduler.getSchedulerStatus());
    }

    public void testVMStatusAfterTermination() throws ServiceRefusedException {
        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        scheduler.addMachine(vm1, vmType);
        assertEquals(VMScheduler.VMStatus.ACTIVE, scheduler.getVMStatus(vm1));
        scheduler.removeMachine(vm1);
        assertEquals(VMScheduler.VMStatus.DOES_NOT_EXIST, scheduler.getVMStatus(vm1));
        scheduler.shutDown();
    }

    public void testVMStatusAfterSchedulerShutDown() throws ServiceRefusedException {
        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        scheduler.addMachine(vm1, vmType);
        assertEquals(VMScheduler.VMStatus.ACTIVE, scheduler.getVMStatus(vm1));
        scheduler.shutDown();
        assertEquals(VMScheduler.VMStatus.DOES_NOT_EXIST, scheduler.getVMStatus(vm1));
    }

    public void testAddJob() throws Exception {
        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        scheduler.addMachine(vm1, vmType);
        Job job = randomJob(vm1, "1 + 1;");
        scheduler.addJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        JobResult result = resultsByID.get(job.getJobID());
        assertEquals(JobResult.ResultType.NORMAL_RESULT, result.getType());
        // Note: not "2", but "2.0", as the resulting Object is a Double (for
        // some reason).  This is not particularly important for the test.
        assertEquals("2.0", result.getExpression());
        assertNull(result.getException());
        scheduler.shutDown();
    }

    public void testLongRunningJob() throws Exception {
        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        scheduler.addMachine(vm1, vmType);
        Job job = randomJob(vm1, "var p=1; for (i=0; i<100000; i++) {p *= 7; p /= 7;} p;");
        scheduler.addJob(vm1, job);
        scheduler.waitUntilFinished();
        assertEquals(1, resultsByID.size());
        JobResult result = resultsByID.get(job.getJobID());
        assertEquals(JobResult.ResultType.NORMAL_RESULT, result.getType());
        // Note: not "2", but "2.0", as the resulting Object is a Double (for
        // some reason).  This is not particularly important for the test.
        assertEquals("1.0", result.getExpression());
        assertNull(result.getException());
        scheduler.shutDown();
    }

    public void testAddMultipleJobs() throws Exception {
        JobResult result;

        scheduler = new VMScheduler(resultHandler);
        String vm1 = randomJID();
        scheduler.addMachine(vm1, vmType);
        Job job1 = randomJob(vm1, "0 + 1;");
        Job job2 = randomJob(vm1, "0 + 2;");
        scheduler.addJob(vm1, job1);
        scheduler.addJob(vm1, job2);
        scheduler.waitUntilFinished();
        assertEquals(2, resultsByID.size());
        result = resultsByID.get(job1.getJobID());
        assertEquals(JobResult.ResultType.NORMAL_RESULT, result.getType());
        assertEquals("1.0", result.getExpression());
        assertNull(result.getException());
        result = resultsByID.get(job2.getJobID());
        assertEquals(JobResult.ResultType.NORMAL_RESULT, result.getType());
        assertEquals("2.0", result.getExpression());
        assertNull(result.getException());
        scheduler.shutDown();
    }

    private VMScheduler.VMResultHandler createResultHandler() {
        return new VMScheduler.VMResultHandler() {

            public void handleResult(JobResult result) {
                resultsByID.put(result.getJob().getJobID(), result);
            }
        };
    }

    private String randomJID() {
        return "a" + random.nextInt(100000) + "@example.com";
    }

    private Job randomJob(final String vmJID,
                          final String expression) {
        String appJID = "?";
        String iqID = "job" + random.nextInt(100000);
        return new Job(vmJID, appJID, iqID, expression);
    }
}
