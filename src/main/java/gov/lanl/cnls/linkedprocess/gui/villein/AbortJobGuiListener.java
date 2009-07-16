package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;
import gov.lanl.cnls.linkedprocess.xmpp.vm.AbortJob;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.PacketListener;

/**
 * User: marko
 * Date: Jul 16, 2009
 * Time: 4:53:39 PM
 */
public class AbortJobGuiListener implements PacketListener {

    protected VilleinGui villeinGui;

    public AbortJobGuiListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        AbortJob abortJob = (AbortJob) packet;

        VmFrame vmFrame = this.villeinGui.getVmFrame(abortJob.getFrom());
        if (vmFrame != null) {
            vmFrame.handleIncomingAbortJob(abortJob);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + abortJob.toXML());
        }
    }
}


