package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 10:04:14 AM
 */
public class PresenceSubscriptionListener extends LopFarmListener {


    public PresenceSubscriptionListener(XmppFarm xmppFarm) {
        super(xmppFarm);
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
            XmppFarm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(presence.getFrom());
            subscribed.setFrom(this.getXmppFarm().getFullJid());
            this.getXmppFarm().getConnection().sendPacket(subscribed);

            return;

        } else if (type == Presence.Type.unsubscribe && !presence.getFrom().equals(this.getXmppFarm().getBareJid()) && !presence.getFrom().equals(this.getXmppFarm().getFullJid())) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribed.setFrom(this.getXmppFarm().getFullJid());
            unsubscribe.setTo(presence.getFrom());
            unsubscribe.setFrom(this.getXmppFarm().getFullJid());

            this.getXmppFarm().getConnection().sendPacket(unsubscribed);
            this.getXmppFarm().getConnection().sendPacket(unsubscribe);
            return;
        }
        XmppFarm.LOGGER.severe("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}
