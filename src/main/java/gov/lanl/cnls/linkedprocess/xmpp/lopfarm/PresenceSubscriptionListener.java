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

    XmppFarm farm;

    public PresenceSubscriptionListener(XmppFarm farm) {
        this.farm = farm;
    }

    public void processPacket(Packet packet) {
        //System.out.println("here" + packet.toXML());
        Presence presence = ((Presence)packet);
        Presence.Type type = presence.getType();
        if(type == Presence.Type.subscribe) {
            XmppFarm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setFrom(packet.getTo());
            subscribe.setFrom(packet.getTo());
            farm.getConnection().sendPacket(subscribed);
            farm.getConnection().sendPacket(subscribe);
            Presence available = farm.createFarmPresence(XmppFarm.FarmPresence.AVAILABLE);
            available.setTo(packet.getFrom());
            farm.getConnection().sendPacket(available);
            return;

        } else if(type == Presence.Type.unsubscribe) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setFrom(packet.getTo());
            unsubscribe.setFrom(packet.getTo());
            farm.getConnection().sendPacket(unsubscribed);
            farm.getConnection().sendPacket(unsubscribe);
            Presence unavailable = farm.createFarmPresence(XmppFarm.FarmPresence.UNAVAILABLE);
            unavailable.setTo(packet.getFrom());
            farm.getConnection().sendPacket(unavailable);
            try {
                farm.getRoster().removeEntry(farm.getRoster().getEntry(packet.getFrom()));
            } catch(XMPPException e) {
                e.printStackTrace();
            }
            return;
        }
        XmppFarm.LOGGER.info("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}
