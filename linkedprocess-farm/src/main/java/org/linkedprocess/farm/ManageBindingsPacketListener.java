package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.Error;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.VmNotFoundException;
import org.linkedprocess.farm.ManageBindings;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class ManageBindingsPacketListener extends FarmPacketListener {

    public ManageBindingsPacketListener(Farm farm) {
        super(farm);
    }

    public void processPacket(Packet packet) {

        try {
            processManageBindingsPacket((ManageBindings) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processManageBindingsPacket(ManageBindings manageBindings) {

        Vm.LOGGER.info("Arrived " + ManageBindingsPacketListener.class.getName());
        Vm.LOGGER.info(manageBindings.toXML());

        ManageBindings returnManageBindings = new ManageBindings();
        returnManageBindings.setTo(manageBindings.getFrom());
        returnManageBindings.setFrom(this.getFarm().getFullJid());
        returnManageBindings.setPacketID(manageBindings.getPacketID());
        returnManageBindings.setVmId(manageBindings.getVmId());


        String vmId = manageBindings.getVmId();

        if (null == vmId) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new org.linkedprocess.Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "manage_bindings XML packet is missing the vm_id attribute", manageBindings.getPacketID()));
        } else if (null != manageBindings.getBadDatatypeMessage()) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.UNKNOWN_DATATYPE, manageBindings.getBadDatatypeMessage(),  manageBindings.getPacketID()));
        } else if (null != manageBindings.getInvalidValueMessage()) {
            returnManageBindings.setType(IQ.Type.ERROR);
            returnManageBindings.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.INVALID_VALUE, manageBindings.getInvalidValueMessage(),  manageBindings.getPacketID()));
        } else {
            try {
                Vm vm = this.getFarm().getVm(vmId);
                returnManageBindings.setType(IQ.Type.RESULT);
                if (manageBindings.getType() == IQ.Type.GET) {
                    returnManageBindings.setBindings(vm.getBindings(manageBindings.getBindings().keySet()));
                } else if (manageBindings.getType() == IQ.Type.SET) {
                    vm.setBindings(manageBindings.getBindings());
                }
            } catch (VmNotFoundException e) {
                returnManageBindings.setType(IQ.Type.ERROR);
                returnManageBindings.setLopError(new Error(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.VM_NOT_FOUND, e.getMessage(),  manageBindings.getPacketID()));
            }
        }

        Vm.LOGGER.info("Sent " + ManageBindingsPacketListener.class.getName());
        Vm.LOGGER.info(returnManageBindings.toXML());
        this.getFarm().getConnection().sendPacket(returnManageBindings);


    }
}
