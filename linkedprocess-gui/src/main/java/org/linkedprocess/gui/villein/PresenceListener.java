package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.xmpp.villein.Struct;

/**
 * User: marko
 * Date: Jul 9, 2009
 * Time: 4:19:23 PM
 */
public class PresenceListener implements PacketListener {

    VilleinGui villeinGui;

    public PresenceListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        Presence presence = ((Presence) packet);

        if (presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed) {
            this.villeinGui.updateHostAreaTree(packet.getFrom(), true);
        } else if (presence.getType() == Presence.Type.error && presence.getError().getCode() == 404) {
            this.villeinGui.updateHostAreaTree(packet.getFrom(), true);
        } else {
            this.villeinGui.updateHostAreaTree(packet.getFrom(), false);
        }


    }
}

