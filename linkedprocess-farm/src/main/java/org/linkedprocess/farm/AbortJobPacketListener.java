package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.Error;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.JobNotFoundException;
import org.linkedprocess.farm.os.errors.VmNotFoundException;
import org.linkedprocess.farm.AbortJob;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class AbortJobPacketListener extends FarmPacketListener {

    public AbortJobPacketListener(Farm farm) {
        super(farm);
    }

    public void processPacket(Packet packet) {
        try {
            processAbortJobPacket((AbortJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processAbortJobPacket(AbortJob abortJob) {


        Farm.LOGGER.fine("Arrived " + AbortJobPacketListener.class.getName());
        Farm.LOGGER.fine(abortJob.toXML());

        AbortJob returnAbortJob = new AbortJob();

        returnAbortJob.setTo(abortJob.getFrom());
        returnAbortJob.setFrom(this.getFarm().getJid().toString());
        returnAbortJob.setPacketID(abortJob.getPacketID());
        returnAbortJob.setVmId(abortJob.getVmId());

        String jobId = abortJob.getJobId();
        String vmId = abortJob.getVmId();

        if (null == vmId || null == jobId) {
            String errorMessage = "";
            if (null == vmId) {
                errorMessage = "abort_job XML packet is missing the vm_id attribute";
            }
            if (null == jobId) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "abort_job XML packet is missing the job_id attribute";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;
            returnAbortJob.setType(IQ.Type.ERROR);
            returnAbortJob.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage,  abortJob.getPacketID()));
        } else {
            try {
                Vm vm = this.getFarm().getVm(vmId);
                vm.abortJob(jobId);
                returnAbortJob.setType(IQ.Type.RESULT);
            } catch (VmNotFoundException e) {
                returnAbortJob.setType(IQ.Type.ERROR);
                returnAbortJob.setLopError(new org.linkedprocess.Error(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.VM_NOT_FOUND, e.getMessage(),  abortJob.getPacketID()));
            } catch (JobNotFoundException e) {
                returnAbortJob.setType(IQ.Type.ERROR);
                returnAbortJob.setLopError(new Error(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.JOB_NOT_FOUND, e.getMessage(),  abortJob.getPacketID()));
            }
        }

        Farm.LOGGER.fine("Sent " + AbortJobPacketListener.class.getName());
        Farm.LOGGER.fine(returnAbortJob.toXML());
        this.getFarm().getConnection().sendPacket(returnAbortJob);


    }
}
