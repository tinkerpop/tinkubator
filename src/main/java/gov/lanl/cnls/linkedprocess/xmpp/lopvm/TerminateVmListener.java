package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:59 PM
 */
public class TerminateVmListener implements PacketListener {

    private XmppVirtualMachine vm;

    public TerminateVmListener(XmppVirtualMachine vm) {
        this.vm = vm;
    }

    public void processPacket(Packet terminateVm) {

        try {
            processPacketTemp(terminateVm);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processPacketTemp(Packet terminateVm) {
        XmppFarm.LOGGER.info("Arrived " + TerminateVmListener.class.getName());
        XmppFarm.LOGGER.info(terminateVm.toXML());

        TerminateVm returnTerminateVm = new TerminateVm();
        returnTerminateVm.setTo(terminateVm.getFrom());
        returnTerminateVm.setPacketID(terminateVm.getPacketID());

        String vmPassword = ((TerminateVm) terminateVm).getVmPassword();
        boolean terminate = false;
        if(null == vmPassword) {
            returnTerminateVm.setErrorType(LinkedProcess.Errortype.MALFORMED_PACKET);
            returnTerminateVm.setErrorMessage("terminate_vm XML packet is missing the vm_password attribute");
            returnTerminateVm.setType(IQ.Type.ERROR);
        } else if(!this.vm.checkVmPassword(vmPassword)) {
            returnTerminateVm.setErrorType(LinkedProcess.Errortype.WRONG_VM_PASSWORD);
            returnTerminateVm.setType(IQ.Type.ERROR);
        } else {
            terminate = true;
            try {
                this.vm.terminateSelf();
                returnTerminateVm.setType(IQ.Type.RESULT);
            } catch (VMWorkerNotFoundException e) {
                returnTerminateVm.setErrorType(LinkedProcess.Errortype.INTERNAL_ERROR);
                returnTerminateVm.setErrorMessage(e.getMessage());
                returnTerminateVm.setType(IQ.Type.ERROR);
            }
        }

        XmppFarm.LOGGER.info("Sent " + TerminateVmListener.class.getName());
        XmppFarm.LOGGER.info(returnTerminateVm.toXML());
        vm.getConnection().sendPacket(returnTerminateVm);

        if(terminate) {
            vm.shutDown();
        }
    }
}