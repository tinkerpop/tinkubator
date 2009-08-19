/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein;

/**
 * A general purpose handler. Used extensively by the proxies, commands, and patterns.
 * <p/>
 * User: josh
 * Date: Aug 6, 2009
 * Time: 4:09:18 PM
 */
public interface Handler<T> {
    void handle(T t);
}

