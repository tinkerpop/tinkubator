package org.linkedprocess.gui.villein.vmcontrol;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.vm.ManageBindings;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.gui.villein.VilleinGui;

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
        VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(manageBindings.getFrom());

        if(vmControlFrame != null) {
            vmControlFrame.handleIncomingManageBindings(manageBindings);
        } else {
            XmppVillein.LOGGER.severe("Could not find vmframe for " + manageBindings.toXML());
        }
    }
}
