package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.xmpp.villein.handlers.SpawnVmHandler;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.villein.structs.ParentStructNotFoundException;

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
            vmStruct.setPresence(new Presence(Presence.Type.available));
            try {
                this.getXmppVillein().addVmStruct(spawnVm.getFrom(), vmStruct);
                for (SpawnVmHandler spawnListener : this.getXmppVillein().getSpawnVmHandlers()) {
                    spawnListener.handleSuccessfulSpawnVm(vmStruct);
                }

            } catch (ParentStructNotFoundException e) {
                XmppVillein.LOGGER.severe(e.getMessage());
            }
        } else {
            XmppVillein.LOGGER.severe("Error: " + spawnVm.getError().toXML());
        }
    }
}
