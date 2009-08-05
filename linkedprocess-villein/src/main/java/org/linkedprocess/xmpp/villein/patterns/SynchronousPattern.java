package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Job;

import java.util.Collection;
import java.util.Set;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 1:56:00 PM
 */
public class SynchronousPattern extends VilleinPattern {


    public SynchronousPattern(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    private void checkTimeout(long startTime, long timeout) throws WaitTimeoutException {
        if((System.currentTimeMillis() - startTime) > timeout && timeout != -1)
                throw new WaitTimeoutException("Waiting timed out at " + timeout + " milliseconds.");
    }

    public void waitFromFarms(int numberOfFarms, long timeout, long sleepTime) throws WaitTimeoutException {
        long currentTime = System.currentTimeMillis();
        int x = 0;
        while (x < numberOfFarms) {
            checkTimeout(currentTime, timeout);
            x = this.xmppVillein.getFarmStructs().size();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }
            }
        }
    }

    public void waitFromVms(int numberOfVms, long timeout, long sleepTime) throws WaitTimeoutException {
        long currentTime = System.currentTimeMillis();
        int x = 0;
        while (x < numberOfVms) {
            checkTimeout(currentTime, timeout);
            x = this.xmppVillein.getVmStructs().size();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }
            }
        }
    }

    public Collection<Job> waitForJobs(Set<String> jobIds, long timeout, long sleepTime) throws WaitTimeoutException {
        long currentTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(currentTime, timeout);
            Collection<Job> jobs = this.xmppVillein.getJobs(jobIds);
            if (jobs.size() == jobIds.size()) {
                return jobs;
            }
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }
            }
        }
    }

}
