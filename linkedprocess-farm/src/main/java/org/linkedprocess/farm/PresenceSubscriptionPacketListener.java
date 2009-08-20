package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 10:04:14 AM
 */
public class PresenceSubscriptionPacketListener extends FarmPacketListener {


    public PresenceSubscriptionPacketListener(LopFarm lopFarm) {
        super(lopFarm);
    }

    public void processPacket(Packet packet) {
        try {
            processPresencePacket((Presence) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processPresencePacket(Presence presence) {

        Presence.Type type = presence.getType();
        if (type == Presence.Type.subscribe) {
            LopFarm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(presence.getFrom());
            subscribed.setFrom(this.getLopFarm().getFullJid());
            this.getLopFarm().getConnection().sendPacket(subscribed);

            return;

        } else if (type == Presence.Type.unsubscribe && !presence.getFrom().equals(this.getLopFarm().getBareJid()) && !presence.getFrom().equals(this.getLopFarm().getFullJid())) {
            LopFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribed.setFrom(this.getLopFarm().getFullJid());
            unsubscribe.setTo(presence.getFrom());
            unsubscribe.setFrom(this.getLopFarm().getFullJid());

            this.getLopFarm().getConnection().sendPacket(unsubscribed);
            this.getLopFarm().getConnection().sendPacket(unsubscribe);
            return;
        }
        throw new IllegalStateException();
    }
}
