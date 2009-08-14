/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

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
