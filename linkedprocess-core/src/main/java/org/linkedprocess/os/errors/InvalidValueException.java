/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jul 22, 2009
 * Time: 2:20:47 PM
 */
public class InvalidValueException extends SchedulerException {
    public InvalidValueException(final String msg) {
        super(msg);
    }
}
