package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:51:27 PM
 */
public class ManageBindingsListener implements PacketListener {

    private XmppVirtualMachine xmppVirtualMachine;

    public ManageBindingsListener(XmppVirtualMachine xmppVirtualMachine) {
        this.xmppVirtualMachine = xmppVirtualMachine;
    }

    public void processPacket(Packet packet) {

        try {
            processManageBindingsPacket((ManageBindings)packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processManageBindingsPacket(ManageBindings manageBindings) {

        XmppVirtualMachine.LOGGER.info("Arrived " + ManageBindingsListener.class.getName());
        XmppVirtualMachine.LOGGER.info(manageBindings.toXML());

        String iqId = manageBindings.getPacketID();
        String villeinJid = manageBindings.getFrom();
        String vmPassword = manageBindings.getVmPassword();

        if(null == vmPassword) {
            ManageBindings returnManageBindings = new ManageBindings();
            returnManageBindings.setTo(villeinJid);
            returnManageBindings.setFrom(this.xmppVirtualMachine.getFullJid());
            returnManageBindings.setPacketID(iqId);
            returnManageBindings.setErrorType(LinkedProcess.ErrorType.MALFORMED_PACKET);
            String errorMessage = "";
            errorMessage = "manage_bindings XML packet is missing the vm_password attribute";
            returnManageBindings.setErrorMessage(errorMessage);
            returnManageBindings.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnManageBindings);

            XmppVirtualMachine.LOGGER.info("Sent " + ManageBindingsListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnManageBindings.toXML());

        } else if(!this.xmppVirtualMachine.checkVmPassword(vmPassword)) {
            ManageBindings returnManageBindings = new ManageBindings();
            returnManageBindings.setTo(villeinJid);
            returnManageBindings.setFrom(this.xmppVirtualMachine.getFullJid());
            returnManageBindings.setPacketID(iqId);
            returnManageBindings.setErrorType(LinkedProcess.ErrorType.WRONG_VM_PASSWORD);
            returnManageBindings.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnManageBindings);

            XmppVirtualMachine.LOGGER.info("Sent " + ManageBindingsListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnManageBindings.toXML());

        } else {
            ManageBindings returnManageBindings = new ManageBindings();
            returnManageBindings.setTo(villeinJid);
            returnManageBindings.setFrom(this.xmppVirtualMachine.getFullJid());
            returnManageBindings.setPacketID(iqId);

            try {
                returnManageBindings.setType(IQ.Type.RESULT);
                if(manageBindings.getType() == IQ.Type.GET) {
                    returnManageBindings.setBindings(this.xmppVirtualMachine.getBindings(manageBindings.getBindings().keySet()));
                } else if(manageBindings.getType() == IQ.Type.SET) {
                    this.xmppVirtualMachine.setBindings(manageBindings.getBindings());
                }
            } catch(VMWorkerNotFoundException e) {
                returnManageBindings.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
                returnManageBindings.setType(IQ.Type.ERROR);
            }

            xmppVirtualMachine.getConnection().sendPacket(returnManageBindings);
            XmppVirtualMachine.LOGGER.info("Sent " + ManageBindingsListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnManageBindings.toXML());
        }

    }
}
