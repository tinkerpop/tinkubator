/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jivesoftware.smack.packet.Presence;

/**
 * The Smack API does not support a <presence type="probe"/>. This class is provided to fill in this hole in their API.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
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
