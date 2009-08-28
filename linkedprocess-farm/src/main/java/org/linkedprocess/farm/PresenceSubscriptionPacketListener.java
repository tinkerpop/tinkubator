package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PresenceSubscriptionPacketListener extends FarmPacketListener {


    public PresenceSubscriptionPacketListener(Farm farm) {
        super(farm);
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
            Farm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(presence.getFrom());
            subscribed.setFrom(this.getFarm().getJid().toString());
            this.getFarm().getConnection().sendPacket(subscribed);

            return;

        } else if (type == Presence.Type.unsubscribe && !presence.getFrom().equals(this.getFarm().getJid().getBareJid().toString()) && !presence.getFrom().equals(this.getFarm().getJid().toString())) {
            Farm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribed.setFrom(this.getFarm().getJid().toString());
            unsubscribe.setTo(presence.getFrom());
            unsubscribe.setFrom(this.getFarm().getJid().toString());

            this.getFarm().getConnection().sendPacket(unsubscribed);
            this.getFarm().getConnection().sendPacket(unsubscribe);
            return;
        }
        throw new IllegalStateException();
    }
}
