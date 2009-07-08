package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:57:44 AM
 */
public class PresenceListener implements PacketListener {

    XmppVillein villein;

    public PresenceListener(XmppVillein villein) {
        this.villein = villein;
    }

    public void processPacket(Packet packet) {
        Presence presence = ((Presence)packet);

            XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());


                if(packet.getFrom().contains("LoPFarm")) {
                    System.out.println("here");
                    //if(!villein.getUserStructs().containsKey(packet.getFrom())) {
                        System.out.println("here2");
                        FarmStruct farmStruct = new FarmStruct();
                        farmStruct.setFarmJid(packet.getFrom());
                        villein.addFarmStruct(farmStruct);
                //}

             

    }
    }
}
