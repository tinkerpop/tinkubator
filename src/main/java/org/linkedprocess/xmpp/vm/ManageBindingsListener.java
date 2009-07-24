package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.LopXmppError;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:51:27 PM
 */
public class ManageBindingsListener extends LopVmListener {

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

        ManageBindings returnManageBindings = new ManageBindings();
        returnManageBindings.setTo(manageBindings.getFrom());
        returnManageBindings.setFrom(this.getXmppVm().getFullJid());
        returnManageBindings.setPacketID(manageBindings.getPacketID());


        String vmPassword = manageBindings.getVmPassword();

        if (null == vmPassword) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "manage_bindings XML packet is missing the vm_password attribute"));
        } else if (null != manageBindings.getBadDatatypeMessage()) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.UNKNOWN_DATATYPE, manageBindings.getBadDatatypeMessage()));
        } else if (null != manageBindings.getInvalidValueMessage()) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.INVALID_VALUE, manageBindings.getInvalidValueMessage()));
        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setError(new LopXmppError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null));
        } else {
            try {

                returnManageBindings.setType(IQ.Type.RESULT);
                if (manageBindings.getType() == IQ.Type.GET) {
                    returnManageBindings.setBindings(this.getXmppVm().getBindings(manageBindings.getBindings().keySet()));
                } else if (manageBindings.getType() == IQ.Type.SET) {
                    this.getXmppVm().setBindings(manageBindings.getBindings());
                }


            } catch (VMWorkerNotFoundException e) {
                returnManageBindings.setType(IQ.Type.ERROR);
                returnManageBindings.setError(new LopXmppError(XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage()));
            }
        }

        XmppVirtualMachine.LOGGER.info("Sent " + ManageBindingsListener.class.getName());
        XmppVirtualMachine.LOGGER.info(returnManageBindings.toXML());
        this.getXmppVm().getConnection().sendPacket(returnManageBindings);


    }
}
