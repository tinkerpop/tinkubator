package gov.lanl.cnls.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.xmpp.farm.SpawnVm;
import gov.lanl.cnls.linkedprocess.xmpp.villein.VmStruct;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:09:17 PM
 */
public class SpawnVmGuiListener implements PacketListener {

    protected VilleinGui villeinGui;

    public SpawnVmGuiListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;    
    }

    public void processPacket(Packet packet) {
        SpawnVm spawnVm = (SpawnVm)packet;
        if(spawnVm.getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = villeinGui.getXmppVillein().getVmStruct(spawnVm.getVmJid());
            villeinGui.updateTree(vmStruct);
        }
    }
}
