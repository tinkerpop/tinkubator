package org.linkedprocess.linkeddata;

import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.VmProxy;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.patterns.ResourceAllocationPattern;
import org.linkedprocess.villein.patterns.SynchronousPattern;
import org.linkedprocess.Jid;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Experiment {

    private static final Jid GROOVY_FARM_JID = new Jid("lanl_countryside@lanl.linkedprocess.org/LoPFarm/PXWDDXW9");
    private static final String RESULTS_FILE = "/Users/marko/Desktop/lop-experiment.txt";
    private static String USERNAME = "test_countryside";
    private static String PASSWORD = "test";
    private static String SERVER = "lanl.linkedprocess.org";
    private static int PORT = 5222;

    private long clock = -1l;

    private long timer() {
        if(clock == -1l) {
            clock = System.currentTimeMillis();
            return -1l;
        } else {
            long x = System.currentTimeMillis() - clock;
            clock = -1l;
            return x;
        }
    }

    public void lopBuckshotExperiment(int resolutions) throws Exception {
        Villein villein = new Villein(SERVER, PORT, USERNAME, PASSWORD);
        villein.createCloudFromRoster();
        FarmProxy farmProxy = ResourceAllocationPattern.allocateFarm(villein.getCloudProxy(), GROOVY_FARM_JID, 10000);
        ResultHolder<VmProxy> result = SynchronousPattern.spawnVm(farmProxy, "groovy", 10000);
        if(!result.isSuccessful())
            throw new Exception("spawn error.");
        VmProxy vmProxy = result.getSuccess();
        JobProxy jobProxy = new JobProxy();
        jobProxy.setExpression(Experiment.class.getResourceAsStream("SingleUriResolution.groovy"));
        vmProxy.submitJob(jobProxy, null, null);
        jobProxy = new JobProxy();
        jobProxy.setExpression("SingleUriResolution.doBuckshotExperiment(" + resolutions + ")");
        SynchronousPattern.submitJob(vmProxy, jobProxy, -1);
        vmProxy.terminateVm(null, null);
        villein.shutdown();
    }

    public void lopOneshotExperiment() throws Exception {
        Villein villein = new Villein(SERVER, PORT, USERNAME, PASSWORD);
        villein.createCloudFromRoster();
        FarmProxy farmProxy = ResourceAllocationPattern.allocateFarm(villein.getCloudProxy(), GROOVY_FARM_JID, 10000);
        ResultHolder<VmProxy> result = SynchronousPattern.spawnVm(farmProxy, "groovy", 10000);
        if(!result.isSuccessful())
            throw new Exception("spawn error.");
        VmProxy vmProxy = result.getSuccess();
        JobProxy jobProxy = new JobProxy();
        jobProxy.setExpression(Experiment.class.getResourceAsStream("SingleUriResolution.groovy"));
        vmProxy.submitJob(jobProxy, null, null);
        jobProxy = new JobProxy();
        jobProxy.setExpression("SingleUriResolution.doOneshotExperiment()");
        ResultHolder<JobProxy> resultJob = SynchronousPattern.submitJob(vmProxy, jobProxy, -1);
        System.out.println("lop oneshot: " + resultJob.getSuccess().getResult());
        vmProxy.terminateVm(null, null);
        villein.shutdown();
    }

    public void baseBuckshotExperiment(int resolutions) throws Exception {
        SingleUriResolution.doBuckshotExperiment(resolutions);
    }

    public void baseOneshotExperiment() throws Exception {
        System.out.println("base oneshot: " + SingleUriResolution.doOneshotExperiment());
    }

    public static void main(String args[]) throws Exception {

        Experiment e = new Experiment();
        e.lopOneshotExperiment();
        e.baseOneshotExperiment();

        /*int maxUriResolutions = 102;
        int uriResolutionInterval = 10;
        int totalNumberOfExperimentsPerInterval = 5;*/
        /*File file = new File(RESULTS_FILE);
        PrintWriter writer = new PrintWriter(file);
        int minUriResolutions = 1;
        int maxUriResolutions = 1000;
        int uriResolutionInterval = 50;
        int totalNumberOfExperimentsPerInterval = 10;
        writer.println("experiment: " + Experiment.class.getName());
        writer.println("min uri resolutions: " + minUriResolutions);
        writer.println("max uri resolutions: " + maxUriResolutions);
        writer.println("uri resolution interval: " + uriResolutionInterval);
        writer.println("total number of experiments per interval: " + totalNumberOfExperimentsPerInterval);
        writer.println("\n");

        Experiment e = new Experiment();
        List<Double> lopTimes = new ArrayList<Double>();
        List<Double> baseTimes = new ArrayList<Double>();

        for(int t=minUriResolutions; t<=maxUriResolutions; t=t+uriResolutionInterval) {
            e.timer();
            for(int i=0; i<totalNumberOfExperimentsPerInterval; i++) {
                e.lopBuckshotExperiment(t);
            }
            long lopTime = e.timer();
            e.timer();
            for(int i=0; i<totalNumberOfExperimentsPerInterval; i++) {
                e.baseBuckshotExperiment(t);
            }
            long baseTime = e.timer();

            double avgLopTime = (double)lopTime / (double)totalNumberOfExperimentsPerInterval;
            double avgBaseTime = (double)baseTime / (double)totalNumberOfExperimentsPerInterval;

            lopTimes.add(avgLopTime);
            baseTimes.add(avgBaseTime);
            
            writer.println("LoP " + t + " : " + lopTimes);
            writer.println("Base " + t + " : " + baseTimes);
            writer.println("");
            writer.flush();

            // so the intervals are clean after 1
            if(t==1) {
                t=0;
            }

        }
        writer.close(); */
    }


}
