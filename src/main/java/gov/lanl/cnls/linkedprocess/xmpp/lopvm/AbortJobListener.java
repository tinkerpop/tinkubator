package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import gov.lanl.cnls.linkedprocess.os.ServiceRefusedException;

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


        XmppVirtualMachine.LOGGER.debug("Arrived CancelListener:");
        XmppVirtualMachine.LOGGER.debug(abortJob.toXML());

        AbortJob returnAbortJob = new AbortJob();
        returnAbortJob.setTo(abortJob.getFrom());
        returnAbortJob.setPacketID(abortJob.getPacketID());

        try {
            this.vm.abortJob(((AbortJob)abortJob).getJobId());
            returnAbortJob.setType(IQ.Type.RESULT);
        } catch (ServiceRefusedException e) { 
            returnAbortJob.setAbortError();
            returnAbortJob.setType(IQ.Type.ERROR);
        }



        XmppVirtualMachine.LOGGER.debug("Sent CancelListener:");
        XmppVirtualMachine.LOGGER.debug(returnAbortJob.toXML());
        vm.getConnection().sendPacket(returnAbortJob);


    }
}