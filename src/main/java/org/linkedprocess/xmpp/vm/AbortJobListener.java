package org.linkedprocess.xmpp.vm;

import org.linkedprocess.os.errors.JobNotFoundException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:45 PM
 */
public class AbortJobListener implements PacketListener {
    private XmppVirtualMachine xmppVirtualMachine;

    public AbortJobListener(XmppVirtualMachine xmppVirtualMachine) {
        this.xmppVirtualMachine = xmppVirtualMachine;
    }

    public void processPacket(Packet abortJob) {


        XmppVirtualMachine.LOGGER.fine("Arrived " + AbortJobListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(abortJob.toXML());

        AbortJob returnAbortJob = new AbortJob();
        returnAbortJob.setTo(abortJob.getFrom());
        returnAbortJob.setPacketID(abortJob.getPacketID());
        String jobId = ((AbortJob) abortJob).getJobId();
        returnAbortJob.setJobId(jobId);
        String vmPassword = ((AbortJob) abortJob).getVmPassword();

        if(null == vmPassword || null == jobId) {
            returnAbortJob.setErrorType(LinkedProcess.ErrorType.MALFORMED_PACKET);
            String errorMessage = "";
            if(null == vmPassword) {
                errorMessage = "abort_job XML packet is missing the vm_password attribute";
            }
            if(null == jobId) {
                if(errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "abort_job XML packet is missing the job_id attribute";
            }
            if(errorMessage.length() > 0)
                returnAbortJob.setErrorMessage(errorMessage);
            returnAbortJob.setType(IQ.Type.ERROR);
        } else if(!this.xmppVirtualMachine.checkVmPassword(vmPassword)) {
            returnAbortJob.setErrorType(LinkedProcess.ErrorType.WRONG_VM_PASSWORD);
            returnAbortJob.setType(IQ.Type.ERROR);
        } else {
            try {
                this.xmppVirtualMachine.abortJob(jobId);
                returnAbortJob.setType(IQ.Type.RESULT);
            } catch (VMWorkerNotFoundException e) {
                returnAbortJob.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
                returnAbortJob.setErrorMessage(e.getMessage());
                returnAbortJob.setType(IQ.Type.ERROR);
            } catch (JobNotFoundException e) {
                returnAbortJob.setErrorType(LinkedProcess.ErrorType.JOB_NOT_FOUND);
                returnAbortJob.setErrorMessage(e.getMessage());
                returnAbortJob.setType(IQ.Type.ERROR);
            }
        }

        XmppVirtualMachine.LOGGER.fine("Sent " + AbortJobListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(returnAbortJob.toXML());
        xmppVirtualMachine.getConnection().sendPacket(returnAbortJob);
    }
}
