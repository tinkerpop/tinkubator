package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.LopXmppError;
import org.linkedprocess.xmpp.farm.XmppFarm;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:59 PM
 */
public class TerminateVmListener extends LopVmListener {

    public TerminateVmListener(XmppVirtualMachine xmppVirtualMachine) {
        super(xmppVirtualMachine);
    }

    public void processPacket(Packet terminateVm) {

        try {
            processTerminateVmPacket((TerminateVm) terminateVm);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processTerminateVmPacket(TerminateVm terminateVm) {
        XmppFarm.LOGGER.info("Arrived " + TerminateVmListener.class.getName());
        XmppFarm.LOGGER.info(terminateVm.toXML());

        TerminateVm returnTerminateVm = new TerminateVm();
        returnTerminateVm.setTo(terminateVm.getFrom());
        returnTerminateVm.setFrom(this.getXmppVm().getFullJid());
        returnTerminateVm.setPacketID(terminateVm.getPacketID());

        String vmPassword = terminateVm.getVmPassword();
        boolean terminate = false;

        if (null == vmPassword) {
            returnTerminateVm.setType(IQ.Type.ERROR);
            returnTerminateVm.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "terminate_vm XML packet is missing the vm_password attribute"));
        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            returnTerminateVm.setType(IQ.Type.ERROR);
            returnTerminateVm.setError(new LopXmppError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null));
        } else {
            terminate = true;
            returnTerminateVm.setType(IQ.Type.RESULT);
        }

        XmppFarm.LOGGER.info("Sent " + TerminateVmListener.class.getName());
        XmppFarm.LOGGER.info(returnTerminateVm.toXML());
        this.getXmppVm().getConnection().sendPacket(returnTerminateVm);

        if (terminate) {
            try {
                this.getXmppVm().getFarm().getVmScheduler().terminateVirtualMachine(xmppClient.getFullJid());
            } catch (VMWorkerNotFoundException e) {
                this.getXmppVm().shutDown();
            }
        }
    }
}
