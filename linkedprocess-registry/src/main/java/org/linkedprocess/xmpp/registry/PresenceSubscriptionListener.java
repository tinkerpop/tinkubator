package org.linkedprocess.xmpp.registry;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.XMPPException;

/**
 * User: marko
 * Date: Aug 1, 2009
 * Time: 2:30:06 PM
 */
public class PresenceSubscriptionListener extends LopRegistryListener {


    public PresenceSubscriptionListener(XmppRegistry xmppRegistry) {
        super(xmppRegistry);
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
            XmppRegistry.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setTo(presence.getFrom());
            subscribed.setFrom(this.getXmppRegistry().getFullJid());
            subscribe.setTo(presence.getFrom());
            subscribe.setFrom(this.getXmppRegistry().getFullJid());

            this.getXmppRegistry().getConnection().sendPacket(subscribed);
            this.getXmppRegistry().getConnection().sendPacket(subscribe);

            return;

        } else
        if (type == Presence.Type.unsubscribe && !presence.getFrom().equals(this.getXmppRegistry().getBareJid()) && !presence.getFrom().equals(this.getXmppRegistry().getFullJid())) {
            XmppRegistry.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribed.setFrom(this.getXmppRegistry().getFullJid());
            unsubscribe.setTo(presence.getFrom());
            unsubscribe.setFrom(this.getXmppRegistry().getFullJid());

            this.getXmppRegistry().getConnection().sendPacket(unsubscribed);
            this.getXmppRegistry().getConnection().sendPacket(unsubscribe);


            return;
        }
        XmppRegistry.LOGGER.severe("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}