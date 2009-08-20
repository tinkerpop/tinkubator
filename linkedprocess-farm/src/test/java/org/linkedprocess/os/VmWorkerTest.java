package org.linkedprocess.os;

import junit.framework.TestCase;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LinkedProcessFarm;

import javax.script.ScriptEngine;

/**
 * Author: josh
 * Date: Aug 20, 2009
 * Time: 11:53:03 AM
 */
public class VmWorkerTest extends TestCase {
    public static void main(final String[] args) throws Exception {
        new VmWorkerTest().findContextSwitchingTime();    
    }

    public void testNothing() {
        // This will pass!
    }

    public void findContextSwitchingTime() throws Exception {
        ScriptEngine engine = LinkedProcessFarm.getScriptEngineManager().getEngineByName(LinkedProcess.JAVASCRIPT);
        VmScheduler.VmResultHandler handler = new VmScheduler.VmResultHandler() {
            public void handleResult(final JobResult result) {
                // Ignore.
            }
        };
        VmWorker w = new VmWorker(engine, handler);
        String shortProgram = "42;";
        String infiniteProgram = "while (true) {}";
        Job job = new Job(null, null, null, shortProgram);
        w.submitJob(job);
        w.work(1000);

        job = new Job(null, null, null, infiniteProgram);
        w.submitJob(job);

        // Load job initially.  Any overhead after this point is context-switching overhead.
        w.work(500);

        long startTime = System.currentTimeMillis();
        int iterations = 1000;
        long waitTime = 5;
        for (int i = 0; i < iterations; i++) {
            w.work(waitTime);
        }
        long endTime = System.currentTimeMillis();

        double contextSwitchingTime = ((endTime - startTime) / (double) iterations) - waitTime;

        System.out.println("context switching time (using " + iterations + " iterations): " + contextSwitchingTime);
    }
}
