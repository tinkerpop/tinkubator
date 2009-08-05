package org.linkedprocess.xmpp.villein.patterns;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.linkedprocess.xmpp.villein.Job;
import org.linkedprocess.xmpp.villein.VmStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;
/**
 * 
 * @author peter, marko
 * This class abstracts the pattern of splitting a job into minor parts,
 * deploying worker code on several, non-specific supporting LoPVMs, and execute it in another
 * job referencing the worker method of the main script.
 *
 */
public class ScatterGatherPattern extends VilleinPattern {

    public ScatterGatherPattern(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

	public Collection<Job> deployWorkerJobsAndWait(HashMap<String, String> workerJobs, long timeout, long sleepTime) throws WaitTimeoutException {
		Set<String> jobIds = workerJobs.keySet();
		Iterator<String> jobKeys = jobIds.iterator();
		for (VmStruct vmStruct : this.xmppVillein.getVmStructs()) {
        	String key = jobKeys.next();
			this.xmppVillein.sendSubmitJob(vmStruct, workerJobs.get(key ), key);
        	}
		
		//Collection<Job> jobs = synch.waitForJobs(jobIds, timeout, sleepTime);
		//return jobs;
        return null;
    }


}

