/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jul 23, 2009
 * Time: 1:01:08 PM
 */
public class NoSuchDatatypeException extends Exception {
    public NoSuchDatatypeException(final String msg) {
        super(msg);
    }
}
