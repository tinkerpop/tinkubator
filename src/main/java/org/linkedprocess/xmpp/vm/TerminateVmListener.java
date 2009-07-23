package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.ErrorIq;
import org.linkedprocess.xmpp.LopListener;
import org.linkedprocess.xmpp.farm.XmppFarm;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:59 PM
 */
public class TerminateVmListener extends LopListener {

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

        String vmPassword = terminateVm.getVmPassword();
        boolean terminate = false;
        if (null == vmPassword) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, xmppClient.getFullJid(), terminateVm.getFrom(), terminateVm.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "terminate_vm XML packet is missing the vm_password attribute");
        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, xmppClient.getFullJid(), terminateVm.getFrom(), terminateVm.getPacketID(), XMPPError.Type.AUTH, XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null);

        } else {
            terminate = true;
            TerminateVm returnTerminateVm = new TerminateVm();
            returnTerminateVm.setTo(terminateVm.getFrom());
            returnTerminateVm.setFrom(this.xmppClient.getFullJid());
            returnTerminateVm.setPacketID(terminateVm.getPacketID());
            returnTerminateVm.setType(IQ.Type.RESULT);

            XmppFarm.LOGGER.info("Sent " + TerminateVmListener.class.getName());
            XmppFarm.LOGGER.info(returnTerminateVm.toXML());
            this.xmppClient.getConnection().sendPacket(returnTerminateVm);
        }

        if (terminate) {
            try {
                ((XmppVirtualMachine) this.xmppClient).getFarm().getVmScheduler().terminateVirtualMachine(xmppClient.getFullJid());
            } catch (VMWorkerNotFoundException e) {
                ((XmppVirtualMachine) this.xmppClient).shutDown();
            }
        }
    }
}
