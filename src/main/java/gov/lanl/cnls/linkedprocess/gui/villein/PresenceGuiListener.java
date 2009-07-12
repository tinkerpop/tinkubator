package gov.lanl.cnls.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 9, 2009
 * Time: 4:19:23 PM
 */
public class PresenceGuiListener implements PacketListener {

    VilleinGui villeinGui;

    public PresenceGuiListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        Presence presence = ((Presence) packet);

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());

        if(presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed) {
            this.villeinGui.updateTree(packet.getFrom(), true);
        } else {
            this.villeinGui.updateTree(packet.getFrom(), false);
        }


    }
}

