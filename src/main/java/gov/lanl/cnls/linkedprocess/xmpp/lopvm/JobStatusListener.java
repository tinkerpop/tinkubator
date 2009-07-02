package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:11 PM
 */
public class JobStatusListener implements PacketListener {
     private XmppVirtualMachine vm;

    public JobStatusListener(XmppVirtualMachine vm) {
        this.vm = vm;
    }

    public void processPacket(Packet jobStatus) {

            XmppVirtualMachine.LOGGER.fine("Arrived " + JobStatusListener.class.getName());
            XmppVirtualMachine.LOGGER.fine(jobStatus.toXML());

            JobStatus returnJobStatus = new JobStatus();
            returnJobStatus.setTo(jobStatus.getFrom());
            returnJobStatus.setPacketID(jobStatus.getPacketID());
            String vmPassword = ((JobStatus)jobStatus).getVmPassword();

            if(null == vmPassword) {
                returnJobStatus.setErrorType(LinkedProcess.Errortype.MALFORMED_PACKET);
                returnJobStatus.setErrorMessage("job_status XML packet is missing the vm_password attribute");
                returnJobStatus.setType(IQ.Type.ERROR);
            } else if(!this.vm.checkVmPassword(vmPassword)) {
                returnJobStatus.setErrorType(LinkedProcess.Errortype.WRONG_VM_PASSWORD);
                returnJobStatus.setType(IQ.Type.ERROR);
            } else {
                try {
                    returnJobStatus.setValue(this.vm.getJobStatus(((JobStatus)jobStatus).getJobId()));
                    returnJobStatus.setType(IQ.Type.RESULT);
                } catch(VMWorkerNotFoundException e) {
                    returnJobStatus.setErrorType(LinkedProcess.Errortype.INTERNAL_ERROR);
                    returnJobStatus.setErrorMessage(e.getMessage());
                    returnJobStatus.setType(IQ.Type.ERROR);
                }
            }

            XmppVirtualMachine.LOGGER.fine("Sent " + JobStatusListener.class.getName());
            XmppVirtualMachine.LOGGER.fine(returnJobStatus.toXML());
            vm.getConnection().sendPacket(returnJobStatus);


    }
}
