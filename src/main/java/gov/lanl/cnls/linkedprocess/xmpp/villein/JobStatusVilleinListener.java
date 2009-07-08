package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 8:56:19 AM
 */
public class JobStatusVilleinListener implements PacketListener {

    protected XmppVillein villein;

    public JobStatusVilleinListener(XmppVillein villein) {
        this.villein = villein;
    }

    public void processPacket(Packet jobStatus) {

    }
}
