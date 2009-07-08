package gov.lanl.cnls.linkedprocess.xmpp.vm;

import gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:45 PM
 */
public class AbortJobVmListener implements PacketListener {
    private XmppVirtualMachine vm;

    public AbortJobVmListener(XmppVirtualMachine vm) {
        this.vm = vm;
    }

    public void processPacket(Packet abortJob) {


        XmppVirtualMachine.LOGGER.fine("Arrived " + AbortJobVmListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(abortJob.toXML());

        AbortJob returnAbortJob = new AbortJob();
        returnAbortJob.setTo(abortJob.getFrom());
        returnAbortJob.setPacketID(abortJob.getPacketID());
        String jobId = ((AbortJob) abortJob).getJobId();
        String vmPassword = ((AbortJob) abortJob).getVmPassword();

        if(null == vmPassword || null == jobId) {
            returnAbortJob.setErrorType(LinkedProcess.Errortype.MALFORMED_PACKET);
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
        } else if(!this.vm.checkVmPassword(vmPassword)) {
            returnAbortJob.setErrorType(LinkedProcess.Errortype.WRONG_VM_PASSWORD);
            returnAbortJob.setType(IQ.Type.ERROR);
        } else {
            try {
                this.vm.abortJob(jobId);
                returnAbortJob.setType(IQ.Type.RESULT);
            } catch (VMWorkerNotFoundException e) {
                returnAbortJob.setErrorType(LinkedProcess.Errortype.INTERNAL_ERROR);
                returnAbortJob.setErrorMessage(e.getMessage());
                returnAbortJob.setType(IQ.Type.ERROR);
            } catch (JobNotFoundException e) {
                returnAbortJob.setErrorType(LinkedProcess.Errortype.JOB_NOT_FOUND);
                returnAbortJob.setErrorMessage(e.getMessage());
                returnAbortJob.setType(IQ.Type.ERROR);
            }
        }

        XmppVirtualMachine.LOGGER.fine("Sent " + AbortJobVmListener.class.getName());
        XmppVirtualMachine.LOGGER.fine(returnAbortJob.toXML());
        vm.getConnection().sendPacket(returnAbortJob);
    }
}