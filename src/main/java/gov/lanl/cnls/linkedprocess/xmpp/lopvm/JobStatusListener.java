package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

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

        try {
            XmppVirtualMachine.LOGGER.debug("Arrived StatusListener:");
            XmppVirtualMachine.LOGGER.debug(jobStatus.toXML());

            JobStatus returnJobStatus = new JobStatus();
            returnJobStatus.setTo(jobStatus.getFrom());
            returnJobStatus.setPacketID(jobStatus.getPacketID());

            returnJobStatus.setType(IQ.Type.RESULT);
            returnJobStatus.setValue(this.vm.getJobStatus(((JobStatus)jobStatus).getJobId()));

            XmppVirtualMachine.LOGGER.debug("Sent StatusListener:");
            XmppVirtualMachine.LOGGER.debug(returnJobStatus.toXML());
            vm.getConnection().sendPacket(returnJobStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
