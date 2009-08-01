package org.linkedprocess.xmpp.countryside;


import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 11:59:59 AM
 */
public class PresenceListener extends LopCountrysideListener {


    public PresenceListener(XmppCountryside xmppCountryside) {
        super(xmppCountryside);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppCountryside.LOGGER.info("Arrived " + PresenceListener.class.getName());
        XmppCountryside.LOGGER.info(presence.toXML());

        if (presence.isAvailable()) {
            DiscoverInfo discoInfo = this.getDiscoInfo(packet.getFrom());
            if (isFarm(discoInfo)) {
                System.out.println("Registering Farm resource: " + packet.getFrom());
                this.getXmppCountryside().addFarmland(LinkedProcess.generateBareJid(packet.getFrom()));
            }
        } else {
            System.out.println("Unregistering resource: " + packet.getFrom());
            this.getXmppCountryside().removeFarmland(packet.getFrom());
        }
    }
}
