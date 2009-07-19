package org.linkedprocess.gui.villein.vmcontrol;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.gui.villein.VilleinGui;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.PacketListener;

/**
 * User: marko
 * Date: Jul 16, 2009
 * Time: 4:53:39 PM
 */
public class AbortJobListener implements PacketListener {

    protected VilleinGui villeinGui;

    public AbortJobListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        AbortJob abortJob = (AbortJob) packet;

        VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(abortJob.getFrom());
        if (vmControlFrame != null) {
            vmControlFrame.handleIncomingAbortJob(abortJob);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + abortJob.toXML());
        }
    }
}


