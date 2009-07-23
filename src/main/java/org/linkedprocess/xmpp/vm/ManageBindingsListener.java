package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.ErrorIq;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:51:27 PM
 */
public class ManageBindingsListener extends LopListener {

    public ManageBindingsListener(XmppVirtualMachine xmppVirtualMachine) {
        super(xmppVirtualMachine);
    }

    public void processPacket(Packet packet) {

        try {
            processManageBindingsPacket((ManageBindings) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processManageBindingsPacket(ManageBindings manageBindings) {

        XmppVirtualMachine.LOGGER.info("Arrived " + ManageBindingsListener.class.getName());
        XmppVirtualMachine.LOGGER.info(manageBindings.toXML());

        String vmPassword = manageBindings.getVmPassword();

        if (null == vmPassword) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, xmppClient.getFullJid(), manageBindings.getFrom(), manageBindings.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "manage_bindings XML packet is missing the vm_password attribute");
        } else if (null != manageBindings.getBadDatatypeMessage()) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, xmppClient.getFullJid(), manageBindings.getFrom(), manageBindings.getPacketID(), XMPPError.Type.CANCEL, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.UNKNOWN_DATATYPE, manageBindings.getBadDatatypeMessage());
        } else if (null != manageBindings.getInvalidValueMessage()) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, xmppClient.getFullJid(), manageBindings.getFrom(), manageBindings.getPacketID(), XMPPError.Type.CANCEL, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.INVALID_VALUE, manageBindings.getInvalidValueMessage());
        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, xmppClient.getFullJid(), manageBindings.getFrom(), manageBindings.getPacketID(), XMPPError.Type.AUTH, XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null);
        } else {
            try {
                ManageBindings returnManageBindings = new ManageBindings();
                returnManageBindings.setTo(manageBindings.getFrom());
                returnManageBindings.setFrom(this.xmppClient.getFullJid());
                returnManageBindings.setPacketID(manageBindings.getPacketID());
                returnManageBindings.setType(IQ.Type.RESULT);
                if (manageBindings.getType() == IQ.Type.GET) {
                    returnManageBindings.setBindings(((XmppVirtualMachine) this.xmppClient).getBindings(manageBindings.getBindings().keySet()));
                } else if (manageBindings.getType() == IQ.Type.SET) {
                    ((XmppVirtualMachine) this.xmppClient).setBindings(manageBindings.getBindings());
                }
                this.xmppClient.getConnection().sendPacket(returnManageBindings);
                XmppVirtualMachine.LOGGER.info("Sent " + ManageBindingsListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnManageBindings.toXML());

            } catch (VMWorkerNotFoundException e) {
                this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), manageBindings.getFrom(), manageBindings.getPacketID(), XMPPError.Type.CANCEL, XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage());
            }
        }

    }
}
