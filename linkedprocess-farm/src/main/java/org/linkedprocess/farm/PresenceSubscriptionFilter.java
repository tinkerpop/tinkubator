package org.linkedprocess.farm;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
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
