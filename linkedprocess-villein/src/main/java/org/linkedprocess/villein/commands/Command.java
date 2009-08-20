/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.linkedprocess.villein.LopVillein;

/**
 * The base class of all LoP command proxies.
 * Note that the command proxies are best utilized through the supported LoP proxies and patterns.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class Command {

    protected final LopVillein xmppVillein;

    public Command(final LopVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

}
