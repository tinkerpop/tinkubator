/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.security;

/**
 * This is merely a marker class which allows VMSecurityManager to distinguish VM worker threads from all others.
 */
public class VmSandboxedThread extends Thread {
    // Definitely a hack...
    public static final String SPECIAL_TOSTRING_VALUE = "!";

    public VmSandboxedThread(final Runnable r,
                             final String name) {
        super(r, name);
    }

    public String toString() {
        return SPECIAL_TOSTRING_VALUE;
    }
}
