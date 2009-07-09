package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

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

        if (packet.getFrom().contains("LoPFarm")) {
            FarmStruct checkStruct = this.villein.getFarmStruct(packet.getFrom());
            if (checkStruct == null) {
                FarmStruct farmStruct = new FarmStruct();
                farmStruct.setFullJid(packet.getFrom());
                farmStruct.setPresence(presence);
                this.villein.addFarmStruct(farmStruct);
            } else {
                checkStruct.setPresence(presence);
            }

        } else if (packet.getFrom().contains("LoPVM")) {

            VmStruct checkStruct = this.villein.getVmStruct(packet.getFrom());
            if (checkStruct == null) {
                VmStruct vmStruct = new VmStruct();
                vmStruct.setFullJid(packet.getFrom());
                //this.villein.addVmStruct()
            } else {
                checkStruct.setPresence(presence);
            }
        }
    }
}
