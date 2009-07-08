package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 8:52:28 AM
 */
public class EvaluateVilleinListener implements PacketListener {

    protected XmppVillein villein;

    public EvaluateVilleinListener(XmppVillein villein) {
        this.villein = villein;
    }

    public void processPacket(Packet evaluate) {

    }
}
