package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

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
public class AbortJobListener implements PacketListener {
    private XmppVirtualMachine vm;

    public AbortJobListener(XmppVirtualMachine vm) {
        this.vm = vm;
    }

    public void processPacket(Packet abortJob) {


        XmppVirtualMachine.LOGGER.fine("Arrived CancelListener:");
        XmppVirtualMachine.LOGGER.fine(abortJob.toXML());

        AbortJob returnAbortJob = new AbortJob();
        returnAbortJob.setTo(abortJob.getFrom());
        returnAbortJob.setPacketID(abortJob.getPacketID());
        String jobId = ((AbortJob) abortJob).getJobId();

        if(null == jobId) {
            returnAbortJob.setErrorType(LinkedProcess.Errortype.MALFORMED_PACKET);
            returnAbortJob.setErrorMessage("abort_job XML packet is missing the job_id attribute");
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

        XmppVirtualMachine.LOGGER.fine("Sent CancelListener:");
        XmppVirtualMachine.LOGGER.fine(returnAbortJob.toXML());
        vm.getConnection().sendPacket(returnAbortJob);
    }
}