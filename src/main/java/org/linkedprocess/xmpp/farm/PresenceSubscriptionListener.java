package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 10:04:14 AM
 */
public class PresenceSubscriptionListener extends LopListener {


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
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setTo(presence.getFrom());
            subscribe.setTo(presence.getFrom());

            Presence available = ((XmppFarm) this.xmppClient).createPresence(((XmppFarm) this.xmppClient).getVmScheduler().getSchedulerStatus());
            available.setTo(presence.getFrom());
            available.setPacketID(presence.getPacketID());

            this.xmppClient.getConnection().sendPacket(subscribed);
            this.xmppClient.getConnection().sendPacket(subscribe);
            this.xmppClient.getConnection().sendPacket(available);

            try {
                this.xmppClient.getRoster().createEntry(presence.getFrom(), presence.getFrom(), null);
            } catch (XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }

            return;

        } else
        if (type == Presence.Type.unsubscribe && !presence.getFrom().equals(this.xmppClient.getBareJid()) && !presence.getFrom().equals(this.xmppClient.getFullJid())) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribe.setTo(presence.getFrom());

            Presence unavailable = ((XmppFarm) this.xmppClient).createPresence(((XmppFarm) this.xmppClient).getVmScheduler().getSchedulerStatus());
            unavailable.setTo(presence.getFrom());

            this.xmppClient.getConnection().sendPacket(unsubscribed);
            this.xmppClient.getConnection().sendPacket(unsubscribe);
            this.xmppClient.getConnection().sendPacket(unavailable);

            try {
                this.xmppClient.getRoster().removeEntry(this.xmppClient.getRoster().getEntry(presence.getFrom()));
            } catch (XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }
            return;
        }
        XmppFarm.LOGGER.severe("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}
