package org.linkedprocess.xmpp.registry;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.packet.DiscoverItems;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 3:14:37 PM
 */
public class DiscoItemsListener extends LopRegistryListener {

    public DiscoItemsListener(XmppRegistry xmppRegistry) {
        super(xmppRegistry);
    }

    public void processPacket(Packet packet) {
        DiscoverItems discoItems = (DiscoverItems) packet;
        if(discoItems.getType() == IQ.Type.GET) {
            DiscoverItems returnDiscoItems = this.getXmppRegistry().createDiscoItems(discoItems.getFrom());
            returnDiscoItems.setPacketID(discoItems.getPacketID());
            this.getXmppRegistry().getConnection().sendPacket(returnDiscoItems);
            System.out.println(returnDiscoItems.toXML());
        }

    }
}
