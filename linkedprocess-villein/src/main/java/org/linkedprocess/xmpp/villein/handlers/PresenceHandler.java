package org.linkedprocess.xmpp.villein.handlers;

import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.xmpp.villein.Struct;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 11:02:03 PM
 */
public interface PresenceHandler {

    public void handlePresenceUpdate(Struct struct, Presence.Type presenceType);
}
