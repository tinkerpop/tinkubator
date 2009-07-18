package org.linkedprocess.xmpp.farm;

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

    XmppFarm xmppFarm;

    public PresenceSubscriptionListener(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
    }

    public void processPacket(Packet packet) {
        try {
            processPresencePacket((Presence)packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processPresencePacket(Presence presence) {

        Presence.Type type = presence.getType();
        if(type == Presence.Type.subscribe) {
            XmppFarm.LOGGER.info("Subscribing to " + presence.getFrom());
            Presence subscribed = new Presence(Presence.Type.subscribed);
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setTo(presence.getFrom());
            subscribe.setTo(presence.getFrom());

            Presence available = xmppFarm.createPresence(xmppFarm.getVmScheduler().getSchedulerStatus());
            available.setTo(presence.getFrom());
            available.setPacketID(presence.getPacketID());

            xmppFarm.getConnection().sendPacket(subscribed);
            xmppFarm.getConnection().sendPacket(subscribe);
            xmppFarm.getConnection().sendPacket(available);

            try {
                xmppFarm.getRoster().createEntry(presence.getFrom(), presence.getFrom(), null);
            } catch(XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }

            return;

        } else if(type == Presence.Type.unsubscribe && !presence.getFrom().equals(xmppFarm.getBareJid()) && !presence.getFrom().equals(xmppFarm.getFullJid())) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribe.setTo(presence.getFrom());

            Presence unavailable = xmppFarm.createPresence(xmppFarm.getVmScheduler().getSchedulerStatus());
            unavailable.setTo(presence.getFrom());

            xmppFarm.getConnection().sendPacket(unsubscribed);
            xmppFarm.getConnection().sendPacket(unsubscribe);
            xmppFarm.getConnection().sendPacket(unavailable);

            try {
                xmppFarm.getRoster().removeEntry(xmppFarm.getRoster().getEntry(presence.getFrom()));
            } catch(XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }
            return;
        }
        XmppFarm.LOGGER.severe("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}
