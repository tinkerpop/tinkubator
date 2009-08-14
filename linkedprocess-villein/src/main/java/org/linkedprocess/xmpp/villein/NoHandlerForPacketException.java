/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein;

/**
 * If no handler was registered for a packet, then this exception is thrown.
 * For many situations, a null handler is passed into a command and thus, this is a common exception.
 *
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:58:39 PM
 */
public class NoHandlerForPacketException extends Exception {
}
