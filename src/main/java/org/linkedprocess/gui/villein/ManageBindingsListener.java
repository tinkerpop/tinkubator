package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.vm.ManageBindings;
import org.linkedprocess.xmpp.villein.XmppVillein;

/**
 * User: marko
 * Date: Jul 18, 2009
 * Time: 12:19:16 PM
 */
public class ManageBindingsListener implements PacketListener {

    protected VilleinGui villeinGui;

    public ManageBindingsListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        ManageBindings manageBindings = (ManageBindings)packet;
        VmFrame vmFrame = this.villeinGui.getVmFrame(manageBindings.getFrom());

        if(vmFrame != null) {
            vmFrame.handleIncomingManageBindings(manageBindings);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + manageBindings.toXML());
        }
    }
}
