package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VmScheduler;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.xmpp.vm.XmppVm;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 4:23:32 PM
 */
public class VMJobResultHandler implements VmScheduler.VmResultHandler {

    XmppFarm xmppFarm;

    public VMJobResultHandler(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
    }

    public void handleResult(JobResult result) {
        try {
            XmppVm vm = xmppFarm.getVirtualMachine(result.getJob().getVmJid());
            IQ returnSubmitJob = result.generateReturnEvalulate();
            vm.getConnection().sendPacket(returnSubmitJob);

            XmppVm.LOGGER.info("Sent " + VMJobResultHandler.class.getName());
            XmppVm.LOGGER.info(returnSubmitJob.toXML());

        } catch (VmWorkerNotFoundException e) {
            XmppVm.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");
        }

    }
}
