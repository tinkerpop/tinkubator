package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.JobNotFoundException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.LopError;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:11 PM
 */
public class PingJobListener extends LopVmListener {

    public PingJobListener(XmppVirtualMachine xmppVirtualMachine) {
        super(xmppVirtualMachine);
    }

    public void processPacket(Packet packet) {
        try {
            processJobStatusPacket((PingJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processJobStatusPacket(PingJob pingJob) {

        XmppVirtualMachine.LOGGER.fine("Arrived " + PingJobListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(pingJob.toXML());

        PingJob returnPingJob = new PingJob();
        returnPingJob.setTo(pingJob.getFrom());
        returnPingJob.setFrom(this.getXmppVm().getFullJid());
        returnPingJob.setPacketID(pingJob.getPacketID());


        String jobId = pingJob.getJobId();
        String vmPassword = pingJob.getVmPassword();

        if (null == vmPassword || null == jobId) {
            String errorMessage = "";
            if (null == vmPassword) {
                errorMessage = "ping_job XML packet is missing the vm_password attribute";
            }
            if (null == jobId) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "ping_job XML packet is missing the job_id attribute";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;

            returnPingJob.setType(IQ.Type.ERROR);
            returnPingJob.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage, LOP_CLIENT_TYPE));
        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            returnPingJob.setType(IQ.Type.ERROR);
            returnPingJob.setLopError(new LopError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null, LOP_CLIENT_TYPE));
        } else {
            try {
                returnPingJob.setValue(((XmppVirtualMachine) this.xmppClient).getJobStatus(jobId));
                returnPingJob.setType(IQ.Type.RESULT);
            } catch (VMWorkerNotFoundException e) {
                returnPingJob.setType(IQ.Type.ERROR);
                returnPingJob.setLopError(new LopError(XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), LOP_CLIENT_TYPE));
            } catch (JobNotFoundException e) {
                returnPingJob.setType(IQ.Type.ERROR);
                returnPingJob.setLopError(new LopError(XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.JOB_NOT_FOUND, e.getMessage(), LOP_CLIENT_TYPE));
            }
        }

        XmppVirtualMachine.LOGGER.fine("Sent " + PingJobListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(returnPingJob.toXML());
        this.getXmppVm().getConnection().sendPacket(returnPingJob);


    }
}
