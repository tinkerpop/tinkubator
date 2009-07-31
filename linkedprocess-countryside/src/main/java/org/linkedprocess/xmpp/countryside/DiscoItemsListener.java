package org.linkedprocess.xmpp.countryside;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.packet.DiscoverItems;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 3:14:37 PM
 */
public class DiscoItemsListener extends LopCountrysideListener {

    public DiscoItemsListener(XmppCountryside xmppCountryside) {
        super(xmppCountryside);
    }

    public void processPacket(Packet packet) {
        DiscoverItems discoItems = (DiscoverItems) packet;
        if(discoItems.getType() == IQ.Type.GET) {
            DiscoverItems returnDiscoItems = this.getXmppCountryside().createDiscoItems(discoItems.getFrom());
            returnDiscoItems.setPacketID(discoItems.getPacketID());
            this.getXmppCountryside().getConnection().sendPacket(returnDiscoItems);
            System.out.println(returnDiscoItems.toXML());
        }

    }
}
