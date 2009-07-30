package org.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 12:29:28 PM
 */
public class ProbePresence extends Presence {

    private static final String OLD_PRESENCE_PREFIX = "<presence";
    private static final String NEW_PRESENCE_PREFIX = "<presence type=\"probe\"";

    public ProbePresence() {
        super(Presence.Type.available);

    }

    public String toXML() {
        return super.toXML().replace(OLD_PRESENCE_PREFIX, NEW_PRESENCE_PREFIX);
    }
}
