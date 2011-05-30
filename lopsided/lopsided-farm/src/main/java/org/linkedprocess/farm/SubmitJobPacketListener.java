package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LopError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.Job;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.JobAlreadyExistsException;
import org.linkedprocess.farm.os.errors.VmIsFullException;
import org.linkedprocess.farm.os.errors.VmNotFoundException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class SubmitJobPacketListener extends FarmPacketListener {


    public SubmitJobPacketListener(Farm farm) {
        super(farm);
    }

    public void processPacket(Packet packet) {

        try {
            processSubmitJobPacket((SubmitJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processSubmitJobPacket(SubmitJob submitJob) {
        Farm.LOGGER.info("Arrived " + SubmitJobPacketListener.class.getName());
        Farm.LOGGER.info(submitJob.toXML());

        String expression = submitJob.getExpression();
        String iqId = submitJob.getPacketID();
        String villeinJid = submitJob.getFrom();
        String vmId = submitJob.getVmId();

        SubmitJob returnSubmitJob = new SubmitJob();
        returnSubmitJob.setPacketID(submitJob.getPacketID());
        returnSubmitJob.setFrom(this.getFarm().getJid().toString());
        returnSubmitJob.setTo(submitJob.getFrom());
        returnSubmitJob.setVmId(vmId);

        if (null == vmId || null == expression) {
            String errorMessage = "";
            if (null == vmId) {
                errorMessage = "submit_job XML packet is missing the vm_password attribute";
            }
            if (null == expression) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "submit_job XML packet is missing the expression text body";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;

            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage, submitJob.getPacketID()));
        } else {
            Job job = new Job(vmId, villeinJid, iqId, expression);
            try {
                Vm vm = this.getFarm().getVm(vmId);
                vm.scheduleJob(job);
                submitJob = null;
            } catch (VmNotFoundException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setLopError(new LopError(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.VM_NOT_FOUND, e.getMessage(), submitJob.getPacketID()));
            } catch (VmIsFullException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setLopError(new LopError(XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.VM_IS_BUSY, e.getMessage(), submitJob.getPacketID()));
            } catch (JobAlreadyExistsException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setLopError(new LopError(XMPPError.Condition.conflict, LinkedProcess.LopErrorType.JOB_ALREADY_EXISTS, e.getMessage(), submitJob.getPacketID()));
            }
        }

        if (submitJob != null) {
            Farm.LOGGER.fine("Sent " + PingJobPacketListener.class.getName());
            Farm.LOGGER.fine(returnSubmitJob.toXML());
            this.getFarm().getConnection().sendPacket(returnSubmitJob);
        }

    }
}
