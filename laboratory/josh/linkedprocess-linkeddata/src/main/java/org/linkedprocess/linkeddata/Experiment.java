package org.linkedprocess.linkeddata;

import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.VmProxy;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.villein.patterns.SynchronousPattern;
import org.linkedprocess.Jid;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Experiment {

    private static final int AMOUNT = 20;
    private static final Jid GROOVY_FARM_JID = new Jid("lanl_countryside@lanl.linkedprocess.org/LoPFarm/IRAXDFP5");
    private long clock = -1l;

    private long timer() {
        if(clock == -1l) {
            clock = System.currentTimeMillis();
            return -1l;
        }
        else {
            long x = System.currentTimeMillis() - clock;
            clock = -1l;
            return x;
        }
    }

    public void lopExperiment(int resolutions) throws Exception {

        Properties props = new Properties();
        props.load(Experiment.class.getResourceAsStream("experiment.properties"));
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        String server = props.getProperty("server");
        int port = Integer.valueOf(props.getProperty("port"));

        Villein villein = new Villein(server, port, username, password);
        villein.createCloudFromRoster();
        FarmProxy farmProxy = ResourceAllocationPattern.allocateFarm(villein.getCloudProxy(), GROOVY_FARM_JID, 10000);
        ResultHolder<VmProxy> result = SynchronousPattern.spawnVm(farmProxy, "groovy", 2500);
        if(!result.isSuccessful())
            throw new Exception();
        VmProxy vmProxy = result.getSuccess();
        JobProxy jobProxy = new JobProxy();
        jobProxy.setExpression(Experiment.class.getResourceAsStream("SingleUriResolution.groovy"));
        SynchronousPattern.submitJob(vmProxy, jobProxy, 10000);
        jobProxy = new JobProxy();
        jobProxy.setExpression("SingleUriResolution.doBuckshotExperiment(" + resolutions + ")");
        SynchronousPattern.submitJob(vmProxy, jobProxy, -1);
        vmProxy.terminateVm(null, null);

    }

    public void baseExperiment(int resolutions) throws Exception {
        SingleUriResolution.doBuckshotExperiment(resolutions);
    }

    public static void main(String args[]) throws Exception {

        int totalNumberOfExperiments = 10;
        Experiment e = new Experiment();
        List<Double> lopTimes = new ArrayList<Double>();
        List<Double> baseTimes = new ArrayList<Double>();

        for(int t=1; t<100; t=t+10) {
            e.timer();
            for(int i=0; i<totalNumberOfExperiments; i++) {
                e.lopExperiment(t);
            }
            long lopTime = e.timer();
            e.timer();
            for(int i=0; i<totalNumberOfExperiments; i++) {
                e.baseExperiment(t);
            }
            long baseTime = e.timer();

            double avgLopTime = (double)lopTime / (double)totalNumberOfExperiments;
            double avgBaseTime = (double)baseTime / (double)totalNumberOfExperiments;
            lopTimes.add(avgLopTime);
            lopTimes.add(avgBaseTime);
            System.out.println("Average LoP time for " + t + ": " + avgLopTime);
            System.out.println("Average base time for " + t + ": " + avgBaseTime);
        }
        System.out.println("\n\n");
        System.out.println("Average LoP times: " + lopTimes);
        System.out.println("Average Base times: " + baseTimes);

    }


}
