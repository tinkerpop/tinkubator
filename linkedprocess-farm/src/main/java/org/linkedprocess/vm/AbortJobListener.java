package org.linkedprocess.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopError;
import org.linkedprocess.vm.AbortJob;
import org.linkedprocess.os.errors.JobNotFoundException;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class AbortJobListener extends LopVmListener {

    public AbortJobListener(LopVm lopVm) {
        super(lopVm);
    }

    public void processPacket(Packet packet) {
        try {
            processAbortJobPacket((AbortJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processAbortJobPacket(AbortJob abortJob) {


        LopVm.LOGGER.fine("Arrived " + AbortJobListener.class.getName());
        LopVm.LOGGER.fine(abortJob.toXML());

        AbortJob returnAbortJob = new AbortJob();

        returnAbortJob.setTo(abortJob.getFrom());
        returnAbortJob.setFrom(this.getXmppVm().getFullJid());
        returnAbortJob.setPacketID(abortJob.getPacketID());

        String jobId = abortJob.getJobId();
        String vmPassword = abortJob.getVmPassword();

        if (null == vmPassword || null == jobId) {
            String errorMessage = "";
            if (null == vmPassword) {
                errorMessage = "abort_job XML packet is missing the vm_password attribute";
            }
            if (null == jobId) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "abort_job XML packet is missing the job_id attribute";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;
            returnAbortJob.setType(IQ.Type.ERROR);
            returnAbortJob.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage, LOP_CLIENT_TYPE, abortJob.getPacketID()));
        } else if (!((LopVm) this.lopClient).checkVmPassword(vmPassword)) {
            returnAbortJob.setType(IQ.Type.ERROR);
            returnAbortJob.setLopError(new LopError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null, LOP_CLIENT_TYPE, abortJob.getPacketID()));
        } else {
            try {
                ((LopVm) this.lopClient).abortJob(jobId);
                returnAbortJob.setType(IQ.Type.RESULT);
            } catch (VmWorkerNotFoundException e) {
                returnAbortJob.setType(IQ.Type.ERROR);
                returnAbortJob.setLopError(new LopError(XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), LOP_CLIENT_TYPE, abortJob.getPacketID()));
            } catch (JobNotFoundException e) {
                returnAbortJob.setType(IQ.Type.ERROR);
                returnAbortJob.setLopError(new LopError(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.JOB_NOT_FOUND, e.getMessage(), LOP_CLIENT_TYPE, abortJob.getPacketID()));
            }
        }

        LopVm.LOGGER.fine("Sent " + AbortJobListener.class.getName());
        LopVm.LOGGER.fine(returnAbortJob.toXML());
        this.getXmppVm().getConnection().sendPacket(returnAbortJob);


    }
}
