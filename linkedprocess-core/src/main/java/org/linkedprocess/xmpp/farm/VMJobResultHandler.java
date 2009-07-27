package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VMScheduler;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 4:23:32 PM
 */
public class VMJobResultHandler implements VMScheduler.VMResultHandler {

    XmppFarm xmppFarm;

    public VMJobResultHandler(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
    }

    public void handleResult(JobResult result) {
        try {
            XmppVirtualMachine vm = xmppFarm.getVirtualMachine(result.getJob().getVmJid());
            IQ returnSubmitJob = result.generateReturnEvalulate();
            vm.getConnection().sendPacket(returnSubmitJob);

            XmppVirtualMachine.LOGGER.info("Sent " + VMJobResultHandler.class.getName());
            XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

        } catch (VMWorkerNotFoundException e) {
            XmppVirtualMachine.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");
        }

    }
}
