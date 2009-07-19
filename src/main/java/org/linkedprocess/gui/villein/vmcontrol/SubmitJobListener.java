package org.linkedprocess.gui.villein.vmcontrol;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.vm.SubmitJob;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.gui.villein.VilleinGui;

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

        VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(submitJob.getFrom());
         
        if(vmControlFrame != null) {
            vmControlFrame.handleIncomingSubmitJob(submitJob);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + submitJob.toXML());
        }
    }
}
