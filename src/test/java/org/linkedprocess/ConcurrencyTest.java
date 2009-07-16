package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 3:02:07 PM
 */
public class ConcurrencyTest extends TestCase {
    public void testThreadSuspendSupport() throws Exception {
        TestRunnable r = new TestRunnable();
        Thread t = new Thread(r);
        t.start();

        Thread.currentThread().sleep(100);
        assertTrue(t.isAlive());
        assertFalse(t.isInterrupted());

        t.suspend();
        assertTrue(t.isAlive());

        Thread.currentThread().sleep(100);

        t.resume();
        assertTrue(t.isAlive());

        Thread.currentThread().sleep(100);

        t.interrupt();
        assertTrue(t.isAlive());
        assertTrue(t.isInterrupted());

        r.stop();
    }

    public void testSuspendInactiveThread() throws Exception {

        TestRunnable r = new TestRunnable();
        Thread t = new Thread(r);
        t.start();

        Thread.currentThread().sleep(100);

        assertTrue(r.isRunning());

        r.stop();
        Thread.currentThread().sleep(100);

        //assertFalse(r.isRunning());

        /*
        t.start();
        Thread.currentThread().sleep(100);
        assertTrue(r.isRunning());

        r.stop();
        */
        //  assertFalse(t.isAlive());

        //              t.interrupt();
        //      assertTrue(t.isAlive());
        //      assertTrue(t.isInterrupted());
    }

    private class TestRunnable implements Runnable {
        private boolean keepRunning = true;
        private boolean running = false;

        public void stop() {
            keepRunning = false;
        }

        public void run() {
            running = true;

            while (keepRunning) {
            }

            running = false;
        }

        public boolean isRunning() {
            return running;
        }
    }
}
