package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.farm.SpawnVm;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 8:52:55 AM
 */
public class SpawnVmListener extends LopVilleinListener {

    public SpawnVmListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        try {
            processSpawnVmPacket((SpawnVm) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processSpawnVmPacket(SpawnVm spawnVm) {

        XmppVillein.LOGGER.info("Arrived " + SpawnVmListener.class.getName());
        XmppVillein.LOGGER.info(spawnVm.toXML());

        if (spawnVm.getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = new VmStruct();
            vmStruct.setFullJid(spawnVm.getVmJid());
            vmStruct.setVmPassword(spawnVm.getVmPassword());
            vmStruct.setVmSpecies(spawnVm.getVmSpecies());
            this.getXmppVillein().addVmStruct(spawnVm.getFrom(), vmStruct);
        } else {
            XmppVillein.LOGGER.severe("Error: " + spawnVm.getError().toXML());
        }
    }
}
