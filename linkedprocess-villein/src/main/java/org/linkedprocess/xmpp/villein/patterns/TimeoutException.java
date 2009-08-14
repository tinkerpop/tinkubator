/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

/**
 * The TimeoutException is used by most synchronous methods. With synchronous methods a timeout value is provided.
 * If the method takes longer than this timeout value to execute, then a TimeoutException is thrown.
 *
 * User: marko
 * Date: Aug 4, 2009
 * Time: 2:04:27 PM
 */
public class TimeoutException extends Exception {
    public TimeoutException(final String message) {
        super(message);
    }
}
