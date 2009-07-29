package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.XMPPException;
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
            //Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribed.setTo(presence.getFrom());
            subscribed.setFrom(this.getXmppFarm().getFullJid());
            //subscribe.setTo(presence.getFrom());

            Presence available = this.getXmppFarm().createPresence(this.getXmppFarm().getVmScheduler().getSchedulerStatus());
            available.setFrom(this.getXmppFarm().getFullJid());
            available.setTo(presence.getFrom());
            available.setPacketID(presence.getPacketID());

            this.getXmppFarm().getConnection().sendPacket(subscribed);
            //this.getXmppFarm().getConnection().sendPacket(subscribe);
            this.getXmppFarm().getConnection().sendPacket(available);

            /*try {
                this.getXmppFarm().getRoster().createEntry(presence.getFrom(), presence.getFrom(), null);
            } catch (XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }*/

            return;

        } else
        if (type == Presence.Type.unsubscribe && !presence.getFrom().equals(this.getXmppFarm().getBareJid()) && !presence.getFrom().equals(this.getXmppFarm().getFullJid())) {
            XmppFarm.LOGGER.info("Unsubscribing from " + presence.getFrom());
            Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
            unsubscribed.setTo(presence.getFrom());
            unsubscribe.setTo(presence.getFrom());


            this.getXmppFarm().getConnection().sendPacket(unsubscribed);
            this.getXmppFarm().getConnection().sendPacket(unsubscribe);

            try {
                this.getXmppFarm().getRoster().removeEntry(this.getXmppFarm().getRoster().getEntry(presence.getFrom()));
            } catch (XMPPException e) {
                XmppFarm.LOGGER.severe(e.getMessage());
            }
            return;
        }
        XmppFarm.LOGGER.severe("This shouldn't have happened.");  // TODO: make this an exception or something -- however, this has yet to happen. Perhaps just remove.
    }
}
