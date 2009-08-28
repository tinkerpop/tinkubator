package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.Error;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.errors.VmNotFoundException;
import org.linkedprocess.farm.TerminateVm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class TerminateVmPacketListener extends FarmPacketListener {

    public TerminateVmPacketListener(Farm farm) {
        super(farm);
    }

    public void processPacket(Packet terminateVm) {

        try {
            processTerminateVmPacket((TerminateVm) terminateVm);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processTerminateVmPacket(TerminateVm terminateVm) {
        Farm.LOGGER.info("Arrived " + TerminateVmPacketListener.class.getName());
        Farm.LOGGER.info(terminateVm.toXML());

        TerminateVm returnTerminateVm = new TerminateVm();
        returnTerminateVm.setTo(terminateVm.getFrom());
        returnTerminateVm.setFrom(this.getFarm().getJid().toString());
        returnTerminateVm.setPacketID(terminateVm.getPacketID());
        returnTerminateVm.setVmId(terminateVm.getVmId());

        String vmId = terminateVm.getVmId();

        if (null == vmId) {
            returnTerminateVm.setType(IQ.Type.ERROR);
            returnTerminateVm.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "terminate_vm XML packet is missing the vm_id attribute",  terminateVm.getPacketID()));
        } else {
            try {
                this.getFarm().getVmScheduler().terminateVm(vmId);
                this.getFarm().terminateVm(vmId);
                returnTerminateVm.setType(IQ.Type.RESULT);
            } catch (VmNotFoundException e) {
                returnTerminateVm.setType(IQ.Type.ERROR);
                returnTerminateVm.setLopError(new Error(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.VM_NOT_FOUND, e.getMessage(),  terminateVm.getPacketID()));
            }
        }

        Farm.LOGGER.info("Sent " + TerminateVmPacketListener.class.getName());
        Farm.LOGGER.info(returnTerminateVm.toXML());
        this.getFarm().getConnection().sendPacket(returnTerminateVm);

    }
}
