/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.security;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 3, 2009
 * Time: 5:19:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VMSecurityManagerListener {
    void notPermitted(SecurityException exception);

    void notPermittedByType(SecurityException exception,
                            PermissionType type);

    void notPermittedByTypeAndPath(SecurityException exception,
                                   PermissionType type,
                                   String path);
}
