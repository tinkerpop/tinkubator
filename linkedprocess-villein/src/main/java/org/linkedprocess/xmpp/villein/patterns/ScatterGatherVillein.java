package org.linkedprocess.xmpp.villein.patterns;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.villein.FarmStruct;
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
public class ScatterGatherVillein  {

    protected static int DESIRED_FARMS = 1;
    protected static int DESIRED_VMS = 1;
	private XmppVillein villein;
	private SynchronousPattern synch;
    

    public ScatterGatherVillein(String vm_type, int nfOfFarms, int nrOfVMs, long creationTimeout, String username, String password, int port, String server) throws Exception {
        villein = new XmppVillein(server, port, username, password);
        synch = new SynchronousPattern(villein);
        DESIRED_FARMS = nfOfFarms;
        DESIRED_VMS = nrOfVMs;
        villein.createCountrysideStructsFromRoster();
        synch.waitFromFarms(DESIRED_FARMS, creationTimeout, 200);

        for (FarmStruct farmStruct : villein.getFarmStructs()) {
        	villein.sendSpawnVirtualMachine(farmStruct.getFullJid(), vm_type);
        }
        synch.waitFromVms(DESIRED_VMS, creationTimeout, 200);
        int numberOfVms = villein.getVmStructs().size();
        System.out.println("Number of virtual machines spawned: " + numberOfVms);

    }

    public void shutdown() {
    	for (VmStruct vmStruct : villein.getVmStructs()) {
    		villein.sendTerminateVirtualMachine(vmStruct);
    	}
    	villein.clearAllJobs();
    	villein.shutDown(villein.createPresence(LinkedProcess.VilleinStatus.INACTIVE));
    	
    }
    
	/**
	 * dploeys a script on all LoPVMs
	 * @param jobId the id for the job for later reference in the VM
	 * @param script the script to deploy
	 */
	public void deployJob(String jobId, String script) {
		for (VmStruct vmStruct : villein.getVmStructs()) {
        	villein.sendSubmitJob(vmStruct, script, jobId);
        }
	}
	
	/**
	 * adds the specified bindings to all LoPVMs
	 */
	public void setBinding(VMBindings bindings)
	{
		for (VmStruct vmStruct : villein.getVmStructs()) {
			villein.sendManageBindings(vmStruct, bindings, IQ.Type.SET);
        }
	}


	public Collection<Job> deployWorkerJobsAndWait(HashMap<String, String> workerJobs, long timeout, long sleepTime) throws WaitTimeoutException {
		Set<String> jobIds = workerJobs.keySet();
		Iterator<String> jobKeys = jobIds.iterator();
		for (VmStruct vmStruct : villein.getVmStructs()) {
        	String key = jobKeys.next();
			villein.sendSubmitJob(vmStruct, workerJobs.get(key ), key);
        	}
		
		Collection<Job> jobs = synch.waitForJobs(jobIds, timeout, sleepTime);
		return jobs;
	}

	public void sendGetBindings() {
		for(VmStruct vm : villein.getVmStructs()) {
			villein.sendGetBindings(vm);
		}
	}

	public XmppVillein getVillein() {
		return villein;
	}



}

