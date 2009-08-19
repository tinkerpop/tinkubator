/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
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