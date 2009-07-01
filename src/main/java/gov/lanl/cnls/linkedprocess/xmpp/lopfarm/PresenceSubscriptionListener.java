package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.PacketListener;
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
        Presence presence = ((Presence)packet);
        Presence.Type type = presence.getType();
        if(type == Presence.Type.subscribe) {
            XmppFarm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setTo(packet.getFrom());
            subscribe.setTo(packet.getFrom());

            Presence available = farm.createPresence(farm.getScheduler().getSchedulerStatus());
            available.setTo(packet.getFrom());
            available.setPacketID(packet.getPacketID());

            farm.getConnection().sendPacket(subscribed);
            farm.getConnection().sendPacket(subscribe);
            farm.getConnection().sendPacket(available);

            try {
                farm.getRoster().createEntry(packet.getFrom(), packet.getFrom(), null);
            } catch(XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }

            return;

        } else if(type == Presence.Type.unsubscribe) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(packet.getFrom());
            unsubscribe.setTo(packet.getFrom());

            Presence unavailable = farm.createPresence(farm.getScheduler().getSchedulerStatus());
            unavailable.setTo(packet.getFrom());

            farm.getConnection().sendPacket(unsubscribed);
            farm.getConnection().sendPacket(unsubscribe);
            farm.getConnection().sendPacket(unavailable);

            try {
                farm.getRoster().removeEntry(farm.getRoster().getEntry(packet.getFrom()));
            } catch(XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }
            return;
        }
        XmppFarm.LOGGER.info("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}
