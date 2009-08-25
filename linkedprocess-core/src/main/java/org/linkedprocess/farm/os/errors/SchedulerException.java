/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.os.errors;

/**
 * An exception representing an error condition which is the reponsibility of the client application.
 * <p/>
 * Author: josh
 * Date: Jun 26, 2009
 * Time: 10:43:40 AM
 */
public class SchedulerException extends Exception {
    public SchedulerException() {
        super();
    }

    public SchedulerException(final String msg) {
        super(msg);
    }
}
