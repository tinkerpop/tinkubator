package org.linkedprocess.demos.primes;

import org.linkedprocess.xmpp.villein.*;
import org.linkedprocess.LinkedProcess;
import java.util.*;


/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 11:35:49 AM
 */
public class PrimeFinder extends XmppVillein {

    protected final static int DESIRED_FARMS = 2;
    protected final static int DESIRED_VMS = 2;
    protected Set<String> jobIds = new HashSet<String>();

    public PrimeFinder(int startInteger, int endInteger) throws Exception {
        super("xmpp42.linkedprocess.org", 5222, "linked.process.2@xmpp42.linkedprocess.org", "linked23");
        this.createHostStructsFromRoster();
        this.waitFromFarms(DESIRED_FARMS, 1000);
        for(FarmStruct farmStruct : this.getFarmStructs()) {
            this.spawnVirtualMachine(farmStruct.getFullJid(), "groovy");
        }
        this.waitFromVms(DESIRED_VMS, 1000);
        int numberOfVms = this.getVmStructs().size();
        System.out.println("Number of virtual machines spawned: " + numberOfVms);


        for(VmStruct vmStruct : this.getVmStructs()) {
            this.submitJob(vmStruct, PrimeFinder.getIsPrimeMethod(), "prime1234");
        }

        int interval = Math.round((endInteger - startInteger)/numberOfVms);
        int startValue = startInteger;
        for(VmStruct vmStruct : this.getVmStructs()) {
            int endValue = interval + startValue;
            if(endValue > endInteger)
                endValue = endInteger;
            String jobId = "job-" + new Random().nextInt();
            this.submitJob(vmStruct, "findPrimes(" + startValue + "," + endValue + ")", jobId);
            System.out.println("Submitted job " + startValue + " to " + endValue + " to " + vmStruct.getFullJid());
            this.jobIds.add(jobId);
            startValue = interval + startValue + 1;
        }

        Collection<Job> jobs = this.waitForJobs(this.jobIds, 1000);
        List<Integer> primes = new ArrayList<Integer>();
        for(Job job : jobs) {
            String x = job.getResult().replace("[","").replace("]","");
            String[] xs = x.split(", ");
            for(String y : xs) {
                primes.add(new Integer(y));
            }
        }
        Integer[] temp = new Integer[primes.size()];
        temp = primes.toArray(temp);
        Arrays.sort(temp);
        System.out.println("Result: " + Arrays.asList(temp));


        for(VmStruct vmStruct : this.getVmStructs()) {
            this.terminateVirtualMachine(vmStruct);
        }
        this.clearJobs();
        this.shutDown(this.createPresence(LinkedProcess.VilleinStatus.INACTIVE));
    }

    public static String getIsPrimeMethod() {
        return "def findPrimes(startInt, endInt) {\n" +
                "  x = [];\n" +
                "  for(n in startInt..endInt) {\n" +
                "    prime = true;\n" +
                "    for (i in 3..n-1) {\n" +
                "      if (n % i == 0) {\n" +
                "        prime = false;\n" +
                "        break;\n" +
                "      }\n" +
                "    }\n" +
                "    if (( n%2 !=0 && prime && n > 2) || n == 2) {\n" +
                "      x.add(n);\n" +
                "    } \n" +
                "  }\n" +
                "  return x;\n" +
                "}";
    }

    public static void main(String args[]) throws Exception {
        new PrimeFinder(1, 100);
    }

}
