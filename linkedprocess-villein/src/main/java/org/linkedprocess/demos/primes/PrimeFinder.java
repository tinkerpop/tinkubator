package org.linkedprocess.demos.primes;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.xmpp.villein.FarmStruct;
import org.linkedprocess.xmpp.villein.Job;
import org.linkedprocess.xmpp.villein.VmStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;

import java.io.IOException;
import java.util.*;


/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 11:35:49 AM
 */
public class PrimeFinder extends XmppVillein {

    protected final static int DESIRED_FARMS = 4;
    protected final static int DESIRED_VMS = 4;
    protected Set<String> jobIds = new HashSet<String>();

    public PrimeFinder(int startInteger, int endInteger) throws Exception {
        super("fortytwo.linkedprocess.org", 5222, "linked.process.2@fortytwo.linkedprocess.org", "linked23");
        this.createFarmlandStructsFromRoster();
        this.waitFromFarms(DESIRED_FARMS, 500);

        for (FarmStruct farmStruct : this.getFarmStructs()) {
            this.spawnVirtualMachine(farmStruct.getFullJid(), "groovy");
        }
        this.waitFromVms(DESIRED_VMS, 500);
        int numberOfVms = this.getVmStructs().size();
        System.out.println("Number of virtual machines spawned: " + numberOfVms);

        long startTime = System.currentTimeMillis();
        for (VmStruct vmStruct : this.getVmStructs()) {
            this.submitJob(vmStruct, PrimeFinder.getGroovyFindPrimesMethod(), "prime1234");
        }

        int interval = Math.round((endInteger - startInteger) / numberOfVms);
        int startValue = startInteger;
        for (VmStruct vmStruct : this.getVmStructs()) {
            int endValue = interval + startValue;
            if (endValue > endInteger)
                endValue = endInteger;
            String jobId = "job-" + new Random().nextInt();
            this.submitJob(vmStruct, "findPrimes(" + startValue + "," + endValue + ")", jobId);
            System.out.println("Submitted job " + startValue + " to " + endValue + " to " + vmStruct.getFullJid());
            this.jobIds.add(jobId);
            startValue = interval + startValue + 1;
        }

        Collection<Job> jobs = this.waitForJobs(this.jobIds, 500);
        List<Integer> primes = new ArrayList<Integer>();
        for (Job job : jobs) {
            if (job.getError() == null) {
                String x = job.getResult().replace("[", "").replace("]", "");
                String[] xs = x.split(", ");
                for (String y : xs) {
                    primes.add(new Integer(y));
                }
            }
        }
        Integer[] temp = new Integer[primes.size()];
        temp = primes.toArray(temp);
        Arrays.sort(temp);
        System.out.println("Result: " + Arrays.asList(temp));
        System.out.println("Running time: " + ((System.currentTimeMillis() - startTime) / 1000.0f) + " seconds.");

        for (VmStruct vmStruct : this.getVmStructs()) {
            this.terminateVirtualMachine(vmStruct);
        }
        this.clearJobs();
        this.shutDown(this.createPresence(LinkedProcess.VilleinStatus.INACTIVE));

        startTime = System.currentTimeMillis();
        System.out.println("\nResult: " + this.findPrimes(startInteger, endInteger));
        System.out.println("Running time: " + ((System.currentTimeMillis() - startTime) / 1000.0f) + " seconds.");

    }

    public ArrayList<Integer> findPrimes(int startInt, int endInt) {
        ArrayList<Integer> primes = new ArrayList<Integer>();
        for (int n = startInt; n < endInt; n++) {
            boolean prime = true;
            for (int i = 3; i < n - 1; i++) {
                if (n % i == 0) {
                    prime = false;
                    break;
                }
            }
            if ((n % 2 != 0 && prime && n > 2) || n == 2) {
                primes.add(n);
            }
        }
        return primes;
    }

    public static String getGroovyFindPrimesMethod() {
        try {
            return XmppClient.convertStreamToString(PrimeFinder.class.getResourceAsStream("findPrimes.groovy"));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String args[]) throws Exception {
        new PrimeFinder(1, 10000);
    }

}
