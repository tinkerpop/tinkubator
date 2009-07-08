package gov.lanl.cnls.linkedprocess.xmpp.farm;

import gov.lanl.cnls.linkedprocess.xmpp.vm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.xmpp.vm.Evaluate;
import gov.lanl.cnls.linkedprocess.os.JobResult;
import gov.lanl.cnls.linkedprocess.os.VMScheduler;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.IQ;

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
        try {
            XmppVirtualMachine vm = farm.getVirtualMachine(result.getJob().getVMJID());
            Evaluate returnEvaluate = result.generateReturnEvalulate();
            vm.getConnection().sendPacket(returnEvaluate);

            XmppVirtualMachine.LOGGER.info("Sent " + VMJobResultHandler.class.getName());
            XmppVirtualMachine.LOGGER.info(returnEvaluate.toXML());

        } catch(VMWorkerNotFoundException e) {
            Evaluate x = result.generateReturnEvalulate();
            x.setErrorType(LinkedProcess.Errortype.INTERNAL_ERROR);
            x.setType(IQ.Type.ERROR);
            XmppVirtualMachine.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");   
        }

    }
}
