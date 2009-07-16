package org.linkedprocess.xmpp.farm;

import org.linkedprocess.xmpp.vm.XmppVirtualMachine;
import org.linkedprocess.xmpp.vm.SubmitJob;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VMScheduler;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.IQ;

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
            XmppVirtualMachine vm = xmppFarm.getVirtualMachine(result.getJob().getVMJID());
            SubmitJob returnSubmitJob = result.generateReturnEvalulate();
            vm.getConnection().sendPacket(returnSubmitJob);

            XmppVirtualMachine.LOGGER.info("Sent " + VMJobResultHandler.class.getName());
            XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

        } catch(VMWorkerNotFoundException e) {
            SubmitJob x = result.generateReturnEvalulate();
            x.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
            x.setType(IQ.Type.ERROR);
            XmppVirtualMachine.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");   
        }

    }
}
