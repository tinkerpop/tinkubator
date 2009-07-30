package org.linkedprocess.xmpp.countryside;


import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;

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
            System.out.println(discoInfo.toXML());
            if (isFarm(discoInfo)) {
                System.out.println("Farm discovered: " + packet.getFrom());
                ServiceDiscoveryManager discoManager = this.getXmppCountryside().getDiscoManager();
                DiscoverItems.Item item = new DiscoverItems.Item(packet.getFrom());
                item.setAction(DiscoverItems.Item.UPDATE_ACTION);
                DiscoverItems items = new DiscoverItems();
                items.addItem(item);
                try {
                    discoManager.publishItems(packet.getFrom(), items);
                } catch (XMPPException e) {
                    XmppCountryside.LOGGER.severe("XMPP Discovery Manager error: " + e.getMessage());
                }
                XmppCountryside.LOGGER.info(items.toXML());
            }
        } else {
            ServiceDiscoveryManager discoManager = this.getXmppCountryside().getDiscoManager();
            DiscoverItems.Item item = new DiscoverItems.Item(packet.getFrom());
            item.setAction(DiscoverItems.Item.REMOVE_ACTION);
            DiscoverItems items = new DiscoverItems();
            items.addItem(item);
            try {
                discoManager.publishItems(packet.getFrom(), items);
            } catch (XMPPException e) {
                XmppCountryside.LOGGER.severe("XMPP Discovery Manager error: " + e.getMessage());
            }
            XmppCountryside.LOGGER.info(items.toXML());
        }
    }
}
