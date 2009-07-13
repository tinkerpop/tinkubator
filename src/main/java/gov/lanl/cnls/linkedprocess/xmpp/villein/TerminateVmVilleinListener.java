package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 8:52:41 AM
 */
public class TerminateVmVilleinListener implements PacketListener {

    protected XmppVillein xmppVillein;

    public TerminateVmVilleinListener(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

    public void processPacket(Packet terminateVm) {

    }
}
