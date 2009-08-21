package org.linkedprocess.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.vm.SubmitJob;
import org.linkedprocess.os.Job;
import org.linkedprocess.os.errors.JobAlreadyExistsException;
import org.linkedprocess.os.errors.VmWorkerIsFullException;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.Error;
import org.linkedprocess.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class SubmitJobPacketListener extends VmPacketListener {


    public SubmitJobPacketListener(LopVm lopVm) {
        super(lopVm);
    }

    public void processPacket(Packet packet) {

        try {
            processSubmitJobPacket((SubmitJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processSubmitJobPacket(SubmitJob submitJob) {
        LopVm.LOGGER.info("Arrived " + SubmitJobPacketListener.class.getName());
        LopVm.LOGGER.info(submitJob.toXML());

        String expression = submitJob.getExpression();
        String iqId = submitJob.getPacketID();
        String villeinJid = submitJob.getFrom();
        String vmPassword = submitJob.getVmPassword();

        SubmitJob returnSubmitJob = new SubmitJob();
        returnSubmitJob.setPacketID(submitJob.getPacketID());
        returnSubmitJob.setFrom(this.getLopVm().getFullJid());
        returnSubmitJob.setTo(submitJob.getFrom());

        if (null == vmPassword || null == expression) {
            String errorMessage = "";
            if (null == vmPassword) {
                errorMessage = "submit_job XML packet is missing the vm_password attribute";
            }
            if (null == expression) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "submit_job XML stanza is missing the expression text body";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;

            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage, LOP_CLIENT_TYPE, submitJob.getPacketID()));


        } else if (!((LopVm) this.lopClient).checkVmPassword(vmPassword)) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setLopError(new Error(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null, LOP_CLIENT_TYPE, submitJob.getPacketID()));
        } else {
            Job job = new Job(this.lopClient.getFullJid(), villeinJid, iqId, expression);
            try {
                ((LopVm) lopClient).scheduleJob(job);
                submitJob = null;
            } catch (VmWorkerNotFoundException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setLopError(new Error(XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), LOP_CLIENT_TYPE, submitJob.getPacketID()));
            } catch (VmWorkerIsFullException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setLopError(new Error(XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.VM_IS_BUSY, e.getMessage(), LOP_CLIENT_TYPE, submitJob.getPacketID()));
            } catch (JobAlreadyExistsException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setLopError(new org.linkedprocess.Error(XMPPError.Condition.conflict, LinkedProcess.LopErrorType.JOB_ALREADY_EXISTS, e.getMessage(), LOP_CLIENT_TYPE, submitJob.getPacketID()));
            }
        }

        if (submitJob != null) {
            LopVm.LOGGER.fine("Sent " + PingJobPacketListener.class.getName());
            LopVm.LOGGER.fine(returnSubmitJob.toXML());
            this.getLopVm().getConnection().sendPacket(returnSubmitJob);
        }

    }
}
