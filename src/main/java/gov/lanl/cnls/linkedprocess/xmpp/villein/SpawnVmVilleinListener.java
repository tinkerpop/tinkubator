package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.xmpp.farm.SpawnVm;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 8:52:55 AM
 */
public class SpawnVmVilleinListener implements PacketListener {

    protected XmppVillein xmppVillein;

    public SpawnVmVilleinListener(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

    public void processPacket(Packet packet) {
        SpawnVm spawnVm = (SpawnVm)packet;

        System.out.println(spawnVm.toXML());
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
