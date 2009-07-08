package gov.lanl.cnls.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 12:29:28 PM
 */
public class ProbePresence extends Presence {

    public ProbePresence() {
        super(Presence.Type.available);

    }

    public String toXML() {
        return super.toXML().replace("<presence", "<presence type=\"probe\"");
    }
}
