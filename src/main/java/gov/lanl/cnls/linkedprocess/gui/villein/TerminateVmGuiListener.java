package gov.lanl.cnls.linkedprocess.gui.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.xmpp.vm.TerminateVm;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:21:56 PM
 */
public class TerminateVmGuiListener implements PacketListener {

    protected VilleinGui villeinGui;

    public TerminateVmGuiListener(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
    }

    public void processPacket(Packet packet) {
        TerminateVm terminateVm = (TerminateVm)packet;
        if(terminateVm.getType() == IQ.Type.RESULT) {
            DefaultMutableTreeNode node = villeinGui.buddyArea.treeMap.get(terminateVm.getFrom());
            node.removeFromParent();
            villeinGui.buddyArea.treeMap.remove(terminateVm.getFrom());
        }
    }
}
