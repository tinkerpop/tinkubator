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

    public void processPacket(Packet spawnVm) {
        System.out.println(spawnVm.toXML());
        String iqId = spawnVm.getPacketID();
        String farmJid = spawnVm.getFrom();
        if(((SpawnVm)spawnVm).getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = new VmStruct();
            vmStruct.setVmJid(((SpawnVm)spawnVm).getVmJid());
            vmStruct.setVmPassword(((SpawnVm)spawnVm).getVmPassword());
            vmStruct.setVmSpecies(((SpawnVm)spawnVm).getVmSpecies());
            xmppVillein.addVmStruct(farmJid, vmStruct);
        } else {
            XmppVillein.LOGGER.severe("Error: " + ((SpawnVm)spawnVm).getErrorMessage());
        }
    }
}
