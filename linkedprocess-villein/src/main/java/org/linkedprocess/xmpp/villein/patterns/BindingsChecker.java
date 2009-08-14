/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.os.VMBindings;

/**
 * A BindingsChecker allows a user to define a bindings equivalence relationship that is utilized by the PollBindingsPattern.
 *
 * User: marko
 * Date: Aug 6, 2009
 * Time: 12:39:07 AM
 */
public interface BindingsChecker {

    public boolean areEquivalent(VMBindings actualBindings, VMBindings desiredBindings);
}
