/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.proxies;

import java.util.Collection;

/**
 *
 * User: marko
 * Date: Aug 14, 2009
 * Time: 12:37:36 PM
 */
public interface FarmHolder {

    public Collection<FarmProxy> getFarmProxies();
}
