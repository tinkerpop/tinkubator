package gov.lanl.cnls.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import gov.lanl.cnls.linkedprocess.xmpp.vm.SubmitJob;
import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;

/**
 * User: marko
 * Date: Jul 12, 2009
 * Time: 7:46:14 PM
 */
public class EvaluateGuiListener implements PacketListener {

    protected VilleinGui villeinGui;

    public EvaluateGuiListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        SubmitJob submitJob = (SubmitJob)packet;

        VmFrame vmFrame = this.villeinGui.getVmFrame(submitJob.getFrom());
        if(vmFrame != null) {
            vmFrame.handleIncomingEvaluate(submitJob);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + submitJob.toXML());
        }
    }
}
