package org.linkedprocess.xmpp.registry;


import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 11:59:59 AM
 */
public class PresenceListener extends LopRegistryListener {


    public PresenceListener(XmppRegistry xmppRegistry) {
        super(xmppRegistry);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppRegistry.LOGGER.info("Arrived " + PresenceListener.class.getName());
        XmppRegistry.LOGGER.info(presence.toXML());

        if (presence.isAvailable()) {
            DiscoverInfo discoInfo = this.getDiscoInfo(packet.getFrom());
            if (isFarm(discoInfo)) {
                System.out.println("Registering farm: " + packet.getFrom());
                this.getXmppRegistry().addActiveFarm(packet.getFrom());
            }
        } else {
            System.out.println("Unregistering resource: " + packet.getFrom());
            this.getXmppRegistry().removeActiveFarm(packet.getFrom());
        }
    }
}
