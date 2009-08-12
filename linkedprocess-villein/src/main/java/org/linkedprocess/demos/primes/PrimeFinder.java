package org.linkedprocess.demos.primes;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.patterns.SynchronousPattern;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.XmppClient;

/**
 * User: marko Date: Jul 28, 2009 Time: 11:35:49 AM
 */
public class PrimeFinder {


	public PrimeFinder(int startInteger, int endInteger, String username, String password, String server, int port) throws Exception {

		XmppVillein villein = new XmppVillein(server, port, username, password);
		villein.createLopCloudFromRoster();
        SynchronousPattern sp = new SynchronousPattern();
        sp.waitForFarms(villein.getLopCloud(), 2, 10000);
        for (FarmProxy farm : villein.getLopCloud().getFarmProxies()) {
			System.out.println("found farm: " + farm.getFullJid());
		}

        int intevalInteger = Math.round((endInteger - startInteger) / villein.getLopCloud().getFarmProxies().size());
        for(FarmProxy farmProxy : villein.getLopCloud().getFarmProxies()) {
            System.out.println("Spawing a groovy virtual machine on: " + farmProxy.getFullJid());
            VmProxy vmProxy = sp.spawnVm(farmProxy, "groovy", 10000);
            System.out.println("Submitting prime code to: " + vmProxy.getFullJid());
            JobStruct job = new JobStruct();
            job.setExpression(XmppClient.convertStreamToString(PrimeFinder.class.getResourceAsStream("findPrimes.groovy")));
            sp.submitJob(vmProxy, job, -1);
        }     

    }

    public static void main(String[] args) throws Exception {
        new PrimeFinder(1, 1000, "linked.process.1", "linked12", "lanl.linkedprocess.org", 5222);
    }


}
