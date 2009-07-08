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

    protected XmppVillein villein;

    public SpawnVmVilleinListener(XmppVillein villein) {
        this.villein = villein;
    }

    public void processPacket(Packet spawnVm) {

        String iqId = spawnVm.getPacketID();
        String farmJid = spawnVm.getFrom();
        if(((SpawnVm)spawnVm).getType() == IQ.Type.RESULT) {
            VmStruct vm = new VmStruct();
            vm.setVmJid(((SpawnVm)spawnVm).getVmJid());
            vm.setVmPassword(((SpawnVm)spawnVm).getVmPassword());
            vm.setVmSpecies(((SpawnVm)spawnVm).getVmSpecies());
        } else {
            XmppVillein.LOGGER.severe("Error: " + ((SpawnVm)spawnVm).getErrorMessage());
        }
    }
}
