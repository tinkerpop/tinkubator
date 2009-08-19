package org.linkedprocess.farm;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 6:47:07 PM
 */
public class PresenceSubscriptionFilter implements PacketFilter {

    public boolean accept(Packet packet) {
        try {
            if (!packet.toXML().trim().startsWith("<presence")) {
                return false;
            }
            Presence presence = (Presence) packet;
            Presence.Type type = presence.getType();
            if (type == Presence.Type.subscribe || type == Presence.Type.unsubscribe)
                return true;
            else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
