package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 10:04:14 AM
 */
public class PresenceSubscriptionListener implements PacketListener {

    XMPPConnection connection;
    Roster roster;

    public PresenceSubscriptionListener(XMPPConnection connection, Roster roster) {
        this.connection = connection;
        this.roster = roster;
    }

    public void processPacket(Packet packet) {
        Presence presence = ((Presence)packet);
        Presence.Type type = presence.getType();
        if(type == Presence.Type.subscribe) {
            XmppFarm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setFrom(packet.getTo());
            subscribe.setFrom(packet.getTo());
            connection.sendPacket(subscribed);
            connection.sendPacket(subscribe);
            connection.sendPacket(XmppFarm.createFarmPresence(XmppFarm.FarmPresence.AVAILABLE));
            return;

        } else if(type == Presence.Type.unsubscribe) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setFrom(packet.getTo());
            unsubscribe.setFrom(packet.getTo());
            connection.sendPacket(unsubscribed);
            connection.sendPacket(unsubscribe);
            connection.sendPacket(XmppFarm.createFarmPresence(XmppFarm.FarmPresence.UNAVAILABLE));
            try {
            roster.removeEntry(roster.getEntry(packet.getFrom()));
            } catch(XMPPException e) {
                e.printStackTrace();
            }
            return;
        }
        XmppFarm.LOGGER.info("This shouldn't have happened.");
    }
}
