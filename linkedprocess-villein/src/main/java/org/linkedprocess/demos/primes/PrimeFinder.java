package org.linkedprocess.demos.primes;

import java.util.Collection;
import java.util.HashMap;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LinkedProcess.VilleinStatus;
import org.linkedprocess.os.Job;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.xmpp.villein.proxies.CountrysideProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.vm.SubmitJob;

/**
 * User: marko Date: Jul 28, 2009 Time: 11:35:49 AM
 */
public class PrimeFinder {

	private static final String PROGRESS = "progress";
	private static final long TIMEOUT = 1000000;
	private static final long POLL_INTERVALL = 200;
	private String jobId = "groovyPrimeFinderFunctionDef";
	private XmppVillein villein;

	public PrimeFinder(int startInteger, int endInteger, String vm_type,
			int nrOfFarms, int nrOfVMs, String username, String password,
			int port, String server) throws Exception {

		villein = new XmppVillein(server, port, username, password);
		villein.createCountrysideProxiesFromRoster();

		for (FarmProxy farm : villein.getFarmProxies()) {
			System.out.println("found farm: " + farm.getFullJid());
		}
		// CountrysideProxy countrysideStruct = new CountrysideProxy(villein
		// .getDispatcher());
		// countrysideStruct.setFullJid(LinkedProcess.generateBareJid(villein
		// .getFullJid()));

		// scatter = new ScatterGatherPattern(vm_type, nrOfFarms, nrOfVMs,
		// TIMEOUT,username, password, port, server);
		// VMBindings bindings = new VMBindings();
		// bindings.putTyped(PROGRESS, new TypedValue(XMLSchemaDatatype.INTEGER,
		// "0"));
		// scatter.setBinding(bindings);
		// printProgress();
		// scatter.deployJob(jobId,
		// XmppClient.convertStreamToString(PrimeFinder.class.getResourceAsStream("findPrimes.groovy")));
		// HashMap<String, String> workerJobs =
		// partitionParameters(startInteger, endInteger, nrOfVMs);
		// Collection<Job> deployWorkerJobsAndWait =
		// scatter.deployWorkerJobsAndWait(workerJobs, TIMEOUT, POLL_INTERVALL);
		// scatter.sendGetBindings();
		// Thread.sleep(2000);
		// printProgress();
		// printJobResults(deployWorkerJobsAndWait);
		// scatter.shutdown();
	}

	// private void printJobResults(Collection<SubmitJob>
	// deployWorkerJobsAndWait) {
	// for (Job job : deployWorkerJobsAndWait) {
	// System.out.println(job.getJobId() + ":" + job.getResult());
	// }
	// }
	//
	// private void printProgress() {
	// for(VmStruct vm : scatter.getVillein().getVmProxies()) {
	// System.out.println(vm.getFullJid() + "\t" +
	// vm.getBindings().get(PROGRESS) + "%");
	// }
	// }

	public static void main(String args[]) throws Exception {
		new PrimeFinder(1, 100, "groovy", 1, 1, "linked.process.1", "linked12",
				5222, "sweden.linkedprocess.org");
		// new PrimeFinder(1, 10000, "groovy", 4,
		// 4,"linked.process.2@fortytwo.linkedprocess.org", "linked23",
		// 5222,"fortytwo.linkedprocess.org");
	}

	private HashMap<String, String> partitionParameters(int startInteger,
			int endInteger, int numberOfVms) {
		int interval = Math.round((endInteger - startInteger) / numberOfVms);
		int startValue = startInteger;
		HashMap<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < numberOfVms; i++) {
			int endValue = interval + startValue;
			if (endValue > endInteger)
				endValue = endInteger;
			String jobId = "job-" + startValue + "-" + endValue;
			// villein.submitJob(vmStruct, "findPrimes(" + startValue + "," +
			// endValue + ")", jobId);
			result
					.put(jobId, "findPrimes(" + startValue + "," + endValue
							+ ")");
			// this.jobIds.add(jobId);
			startValue = interval + startValue + 1;
		}
		return result;
	}

}
