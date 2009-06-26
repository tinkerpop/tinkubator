package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 4:23:32 PM
 */
public class VMJobResultHandler implements VMScheduler.VMResultHandler {

    XmppFarm farm;

    public VMJobResultHandler(XmppFarm farm) {
        this.farm = farm;
    }

    public void handleResult(JobResult result) {
        XmppVirtualMachine vm = farm.getVirtualMachine(result.getJob().getVmJid());
        vm.getConnection().sendPacket(result.generateReturnEvalulate());
    }
}
