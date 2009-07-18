package org.linkedprocess.xmpp.vm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.os.errors.JobNotFoundException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:11 PM
 */
public class JobStatusListener implements PacketListener {
    private XmppVirtualMachine xmppVirtualMachine;

    public JobStatusListener(XmppVirtualMachine xmppVirtualMachine) {
        this.xmppVirtualMachine = xmppVirtualMachine;
    }

    public void processPacket(Packet packet) {
        try {
            processJobStatusPacket((JobStatus)packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processJobStatusPacket(JobStatus jobStatus) {

        XmppVirtualMachine.LOGGER.fine("Arrived " + JobStatusListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(jobStatus.toXML());

        JobStatus returnJobStatus = new JobStatus();
        returnJobStatus.setTo(jobStatus.getFrom());
        returnJobStatus.setPacketID(jobStatus.getPacketID());
        String jobId = jobStatus.getJobId();
        returnJobStatus.setJobId(jobId);
        String vmPassword = jobStatus.getVmPassword();

        if (null == vmPassword || null == jobId) {
            returnJobStatus.setErrorType(LinkedProcess.ErrorType.MALFORMED_PACKET);
            String errorMessage = "";
            if (null == vmPassword) {
                errorMessage = "job_status XML packet is missing the vm_password attribute";
            }
            if (null == jobId) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "job_status XML packet is missing the job_id attribute";
            }
            if (errorMessage.length() > 0)
                returnJobStatus.setErrorMessage(errorMessage);
            returnJobStatus.setType(IQ.Type.ERROR);
        } else if (!this.xmppVirtualMachine.checkVmPassword(vmPassword)) {
            returnJobStatus.setErrorType(LinkedProcess.ErrorType.WRONG_VM_PASSWORD);
            returnJobStatus.setType(IQ.Type.ERROR);
        } else {
            try {
                returnJobStatus.setValue(this.xmppVirtualMachine.getJobStatus(jobId));
                returnJobStatus.setType(IQ.Type.RESULT);
            } catch (VMWorkerNotFoundException e) {
                returnJobStatus.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
                returnJobStatus.setErrorMessage(e.getMessage());
                returnJobStatus.setType(IQ.Type.ERROR);
            } catch (JobNotFoundException e) {
                returnJobStatus.setErrorType(LinkedProcess.ErrorType.JOB_NOT_FOUND);
                returnJobStatus.setErrorMessage(e.getMessage());
                returnJobStatus.setType(IQ.Type.ERROR);
            }
        }

        XmppVirtualMachine.LOGGER.fine("Sent " + JobStatusListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(returnJobStatus.toXML());
        xmppVirtualMachine.getConnection().sendPacket(returnJobStatus);


    }
}
