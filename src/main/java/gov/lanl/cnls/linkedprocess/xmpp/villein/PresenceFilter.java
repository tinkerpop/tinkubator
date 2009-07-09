package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:57:34 AM
 */
public class PresenceFilter implements PacketFilter {

    public boolean accept(Packet packet) {
        try {
            if(!packet.toXML().trim().startsWith("<presence")) {
                return false;
            }
            Presence presence = (Presence)packet;
            Presence.Type type = presence.getType();
            if(type != Presence.Type.subscribe && type != Presence.Type.unsubscribe &&
                    type != Presence.Type.subscribed && type != Presence.Type.unsubscribed)
                return true;
            else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
