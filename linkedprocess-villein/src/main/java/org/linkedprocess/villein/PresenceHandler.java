/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein;

import org.linkedprocess.villein.proxies.XmppProxy;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.Jid;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public interface PresenceHandler {

    public void handlePresenceUpdate(Jid jid, LinkedProcess.Status status);
}
