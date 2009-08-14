/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.proxies;

/**
 * An internal exception thrown when the proxies data structure is in an inconsistent state.
 *
 * User: marko
 * Date: Aug 4, 2009
 * Time: 2:30:56 PM
 */
public class ParentProxyNotFoundException extends RuntimeException {
    public ParentProxyNotFoundException(final String message) {
        super(message);
    }
}
