package org.linkedprocess.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopError;
import org.linkedprocess.vm.ManageBindings;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class ManageBindingsPacketListener extends VmPacketListener {

    public ManageBindingsPacketListener(LopVm lopVm) {
        super(lopVm);
    }

    public void processPacket(Packet packet) {

        try {
            processManageBindingsPacket((ManageBindings) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processManageBindingsPacket(ManageBindings manageBindings) {

        LopVm.LOGGER.info("Arrived " + ManageBindingsPacketListener.class.getName());
        LopVm.LOGGER.info(manageBindings.toXML());

        ManageBindings returnManageBindings = new ManageBindings();
        returnManageBindings.setTo(manageBindings.getFrom());
        returnManageBindings.setFrom(this.getLopVm().getFullJid());
        returnManageBindings.setPacketID(manageBindings.getPacketID());


        String vmPassword = manageBindings.getVmPassword();

        if (null == vmPassword) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "manage_bindings XML packet is missing the vm_password attribute", LOP_CLIENT_TYPE, manageBindings.getPacketID()));
        } else if (null != manageBindings.getBadDatatypeMessage()) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.UNKNOWN_DATATYPE, manageBindings.getBadDatatypeMessage(), LOP_CLIENT_TYPE, manageBindings.getPacketID()));
        } else if (null != manageBindings.getInvalidValueMessage()) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.INVALID_VALUE, manageBindings.getInvalidValueMessage(), LOP_CLIENT_TYPE, manageBindings.getPacketID()));
        } else if (!((LopVm) this.lopClient).checkVmPassword(vmPassword)) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new LopError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null, LOP_CLIENT_TYPE, manageBindings.getPacketID()));
        } else {
            try {

                returnManageBindings.setType(IQ.Type.RESULT);
                if (manageBindings.getType() == IQ.Type.GET) {
                    returnManageBindings.setBindings(this.getLopVm().getBindings(manageBindings.getBindings().keySet()));
                } else if (manageBindings.getType() == IQ.Type.SET) {
                    this.getLopVm().setBindings(manageBindings.getBindings());
                }


            } catch (VmWorkerNotFoundException e) {
                returnManageBindings.setType(IQ.Type.ERROR);
                returnManageBindings.setLopError(new LopError(XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), LOP_CLIENT_TYPE, manageBindings.getPacketID()));
            }
        }

        LopVm.LOGGER.info("Sent " + ManageBindingsPacketListener.class.getName());
        LopVm.LOGGER.info(returnManageBindings.toXML());
        this.getLopVm().getConnection().sendPacket(returnManageBindings);


    }
}
