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
