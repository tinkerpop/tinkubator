/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import org.linkedprocess.LopPacketListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class RegistryPacketListener extends LopPacketListener {

    public RegistryPacketListener(Registry registry) {
        super(registry);
    }

    public Registry getRegistry() {
        return (Registry) this.lopClient;
    }

}
