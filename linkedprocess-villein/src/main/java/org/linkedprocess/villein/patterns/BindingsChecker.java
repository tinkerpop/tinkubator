/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.patterns;

import org.linkedprocess.farm.os.VmBindings;

/**
 * A BindingsChecker allows a user to define a bindings equivalence relationship that is utilized by the PollBindingsPattern.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public interface BindingsChecker {

    public boolean areEquivalent(VmBindings actualBindings, VmBindings desiredBindings);
}
