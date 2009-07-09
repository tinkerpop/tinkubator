package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

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
        Presence presence = ((Presence) packet);

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());

        if(presence.getType() == Presence.Type.unavailable) {
            this.villein.removeStruct(packet.getFrom());
            return;
        }

        if (packet.getFrom().contains("LoPFarm")) {
            Struct checkStruct = this.villein.getStruct(packet.getFrom(), XmppVillein.StructType.FARM);
            if (checkStruct == null) {
                FarmStruct farmStruct = new FarmStruct();
                farmStruct.setFullJid(packet.getFrom());
                farmStruct.setPresence(presence);
                this.villein.addFarmStruct(LinkedProcess.generateBareJid(packet.getFrom()), farmStruct);
            } else {
                checkStruct.setPresence(presence);
            }

        } else {
            Struct checkStruct = this.villein.getStruct(packet.getFrom());
            if (checkStruct != null) {
                checkStruct.setPresence(presence);
            }
        }
    }
}
