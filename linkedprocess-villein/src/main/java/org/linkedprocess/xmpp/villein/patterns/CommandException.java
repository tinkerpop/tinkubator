/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.LopError;

/**
 * User: marko
 * Date: Aug 11, 2009
 * Time: 10:17:19 AM
 */
public class CommandException extends Exception {
    protected LopError lopError;

    public CommandException(LopError lopError) {
        this.lopError = lopError;
    }

    public LopError getLopError() {
        return this.lopError;
    }

    public String getMessage() {
        return this.lopError.toString();
    }
}
