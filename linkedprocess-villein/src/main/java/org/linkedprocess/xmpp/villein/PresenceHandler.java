package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.xmpp.villein.proxies.Proxy;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 11:02:03 PM
 */
public interface PresenceHandler {

    public void handlePresenceUpdate(Proxy proxy, Presence.Type presenceType);
}
