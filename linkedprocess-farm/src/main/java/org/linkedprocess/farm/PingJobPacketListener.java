package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LopError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.JobNotFoundException;
import org.linkedprocess.farm.os.errors.VmNotFoundException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class PingJobPacketListener extends FarmPacketListener {

    public PingJobPacketListener(Farm farm) {
        super(farm);
    }

    public void processPacket(Packet packet) {
        try {
            processJobStatusPacket((PingJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processJobStatusPacket(PingJob pingJob) {

        Farm.LOGGER.fine("Arrived " + PingJobPacketListener.class.getName());
        Farm.LOGGER.fine(pingJob.toXML());

        PingJob returnPingJob = new PingJob();
        returnPingJob.setTo(pingJob.getFrom());
        returnPingJob.setFrom(this.getFarm().getJid().toString());
        returnPingJob.setPacketID(pingJob.getPacketID());
        returnPingJob.setVmId(pingJob.getVmId());


        String jobId = pingJob.getJobId();
        String vmId = pingJob.getVmId();

        if (null == vmId || null == jobId) {
            String errorMessage = "";
            if (null == vmId) {
                errorMessage = "ping_job XML packet is missing the vm_id attribute";
            }
            if (null == jobId) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "ping_job XML packet is missing the job_id attribute";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;

            returnPingJob.setType(IQ.Type.ERROR);
            returnPingJob.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage, pingJob.getPacketID()));
        } else {
            try {
                Vm vm = this.getFarm().getVm(vmId);
                returnPingJob.setStatus(vm.getJobStatus(jobId));
                returnPingJob.setType(IQ.Type.RESULT);
            } catch (VmNotFoundException e) {
                returnPingJob.setType(IQ.Type.ERROR);
                returnPingJob.setLopError(new LopError(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.VM_NOT_FOUND, e.getMessage(), pingJob.getPacketID()));
            } catch (JobNotFoundException e) {
                returnPingJob.setType(IQ.Type.ERROR);
                returnPingJob.setLopError(new LopError(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.JOB_NOT_FOUND, e.getMessage(), pingJob.getPacketID()));
            }
        }

        Farm.LOGGER.fine("Sent " + PingJobPacketListener.class.getName());
        Farm.LOGGER.fine(returnPingJob.toXML());
        this.getFarm().getConnection().sendPacket(returnPingJob);


    }
}
