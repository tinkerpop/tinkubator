package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.vm.SubmitJob;
import org.linkedprocess.xmpp.villein.XmppVillein;

/**
 * User: marko
 * Date: Jul 12, 2009
 * Time: 7:46:14 PM
 */
public class SubmitJobListener implements PacketListener {

    protected VilleinGui villeinGui;

    public SubmitJobListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        SubmitJob submitJob = (SubmitJob)packet;

        VmFrame vmFrame = this.villeinGui.getVmFrame(submitJob.getFrom());
         
        if(vmFrame != null) {
            vmFrame.handleIncomingSubmitJob(submitJob);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + submitJob.toXML());
        }
    }
}
