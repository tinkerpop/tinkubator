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
public class AbandonJobListener implements PacketListener {
    private XmppVirtualMachine vm;

    public AbandonJobListener(XmppVirtualMachine vm) {
        this.vm = vm;
    }

    public void processPacket(Packet abandon) {


        XmppVirtualMachine.LOGGER.debug("Arrived CancelListener:");
        XmppVirtualMachine.LOGGER.debug(abandon.toXML());

        AbandonJob returnAbandonJob = new AbandonJob();
        returnAbandonJob.setTo(abandon.getFrom());
        returnAbandonJob.setPacketID(abandon.getPacketID());

        try {
            this.vm.getFarm().getScheduler().removeJob(vm.getFullJid(), ((AbandonJob) abandon).getJobId());
            returnAbandonJob.setType(IQ.Type.RESULT);
        } catch (ServiceRefusedException e) {
            returnAbandonJob.setType(IQ.Type.ERROR);
            ///TODO:make method returnAbandonJob.
        }



        XmppVirtualMachine.LOGGER.debug("Sent CancelListener:");
        XmppVirtualMachine.LOGGER.debug(returnAbandonJob.toXML());
        vm.getConnection().sendPacket(returnAbandonJob);


    }
}