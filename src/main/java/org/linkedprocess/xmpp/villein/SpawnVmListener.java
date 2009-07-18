package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 8:52:55 AM
 */
public class SpawnVmListener implements PacketListener {

    protected XmppVillein xmppVillein;

    public SpawnVmListener(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

    public void processPacket(Packet packet) {
        try {
            processSpawnVmPacket((SpawnVm)packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processSpawnVmPacket(SpawnVm spawnVm) {
        
        String farmJid = spawnVm.getFrom();
        if(spawnVm.getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = new VmStruct();
            vmStruct.setFullJid(spawnVm.getVmJid());
            vmStruct.setVmPassword(spawnVm.getVmPassword());
            vmStruct.setVmSpecies(spawnVm.getVmSpecies());
            xmppVillein.addVmStruct(farmJid, vmStruct);
        } else {
            XmppVillein.LOGGER.severe("Error: " + spawnVm.getErrorMessage());
        }
    }
}
