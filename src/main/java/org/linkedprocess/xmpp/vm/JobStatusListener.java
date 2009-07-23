package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.JobNotFoundException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.ErrorIq;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:11 PM
 */
public class JobStatusListener extends LopListener {

    public JobStatusListener(XmppVirtualMachine xmppVirtualMachine) {
        super(xmppVirtualMachine);
    }

    public void processPacket(Packet packet) {
        try {
            processJobStatusPacket((JobStatus) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processJobStatusPacket(JobStatus jobStatus) {

        XmppVirtualMachine.LOGGER.fine("Arrived " + JobStatusListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(jobStatus.toXML());

        String jobId = jobStatus.getJobId();
        String vmPassword = jobStatus.getVmPassword();

        if (null == vmPassword || null == jobId) {
            String errorMessage = "";
            if (null == vmPassword) {
                errorMessage = "job_status XML packet is missing the vm_password attribute";
            }
            if (null == jobId) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "job_status XML packet is missing the job_id attribute";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;

            this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), jobStatus.getFrom(), jobStatus.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage);
        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), jobStatus.getFrom(), jobStatus.getPacketID(), XMPPError.Type.AUTH, XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null);

        } else {
            try {
                JobStatus returnJobStatus = new JobStatus();
                returnJobStatus.setValue(((XmppVirtualMachine) this.xmppClient).getJobStatus(jobId));
                returnJobStatus.setTo(jobStatus.getFrom());
                returnJobStatus.setFrom(this.xmppClient.getFullJid());
                returnJobStatus.setPacketID(jobStatus.getPacketID());
                returnJobStatus.setType(IQ.Type.RESULT);

                XmppVirtualMachine.LOGGER.fine("Sent " + JobStatusListener.class.getName());
                XmppVirtualMachine.LOGGER.fine(returnJobStatus.toXML());
                xmppClient.getConnection().sendPacket(returnJobStatus);

            } catch (VMWorkerNotFoundException e) {
                this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), jobStatus.getFrom(), jobStatus.getPacketID(), XMPPError.Type.CANCEL, XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage());
            } catch (JobNotFoundException e) {
                this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), jobStatus.getFrom(), jobStatus.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.item_not_found, LinkedProcess.LopErrorType.JOB_NOT_FOUND, e.getMessage());
            }
        }


    }
}
