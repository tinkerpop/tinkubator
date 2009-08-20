/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopPacketListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class RegistryPacketListener extends LopPacketListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.REGISTRY;

    public RegistryPacketListener(LopRegistry lopRegistry) {
        super(lopRegistry);
    }

    public LopRegistry getXmppRegistry() {
        return (LopRegistry) this.lopClient;
    }

}
