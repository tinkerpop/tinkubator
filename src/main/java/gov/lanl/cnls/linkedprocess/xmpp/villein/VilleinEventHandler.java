package gov.lanl.cnls.linkedprocess.xmpp.villein;

import gov.lanl.cnls.linkedprocess.xmpp.vm.AbortJob;
import gov.lanl.cnls.linkedprocess.xmpp.vm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.vm.JobStatus;
import gov.lanl.cnls.linkedprocess.xmpp.vm.TerminateVm;
import gov.lanl.cnls.linkedprocess.xmpp.farm.SpawnVm;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:00:46 PM
 */
public interface VilleinEventHandler {

    public void abortJobReturned(AbortJob abortJob);
    public void evaluateReturned(Evaluate evaluate);
    public void jobStatusReturned(JobStatus jobStatus);
    public void terminateVmReturned(TerminateVm terminateVm);
    public void spawnVmReturned(SpawnVm spawnVm);

}
