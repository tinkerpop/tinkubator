/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.security;

import org.linkedprocess.LinkedProcess;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jul 7, 2009
 * Time: 11:39:50 AM
 */
public class VmSecurityManager extends SecurityManager {
    private static final Logger LOGGER = LinkedProcess.getLogger(VmSecurityManager.class);

    // TODO (maybe): make this configurable by turning it into a permission type.
    private static final boolean PERMIT_READ_TO_CLASSPATH = true;

    private final Set<PermissionType> permittedTypes;

    private PathPermissions
            readPermissions,
            writePermissions,
            deletePermissions,
            execPermissions,
            linkPermissions,
            httpGetPermissions,
            httpPutPermissions,
            httpPostPermissions;

    private final List<VmSecurityManagerListener> listeners;

    private boolean isVMWorkerThread() {
        // This is weird, but the below (commented out) results in a ClassCircularityError due to permissions associated with class comparison.
        return Thread.currentThread().toString() == VmSandboxedThread.SPECIAL_TOSTRING_VALUE;

        //return Thread.currentThread().getClass() == VMSandboxedThread.class;
    }

    private void alertListeners(final SecurityException e,
                                final PermissionType type,
                                final String path) {
        if (0 < listeners.size()) {
            for (VmSecurityManagerListener l : listeners) {
                if (null == path) {
                    if (null == type) {
                        l.notPermitted(e);
                    } else {
                        l.notPermittedByType(e, type);
                    }
                } else {
                    l.notPermittedByTypeAndPath(e, type, path);
                }
            }
        }
    }

    private void permissionDenied() {
        LOGGER.info("denying permission");
        SecurityException e = new SecurityException("operation is not allowed in VM worker threads");
        alertListeners(e, null, null);
        throw e;
    }

    private void permissionDenied(final PermissionType type) {
        LOGGER.info("denying permission '" + type.getSpecName() + "'");
        SecurityException e = new SecurityException("operation type is not allowed in VM worker threads: " + type);
        alertListeners(e, type, null);
        throw e;
    }

    private void permissionDenied(final PermissionType type, final String resource) {
        LOGGER.info("denying permission '" + type.getSpecName() + "' to resource '" + resource + "'");
//new Exception().printStackTrace();

        SecurityException e = new SecurityException("permission '" + type + "' is not granted for resource: " + resource);
        alertListeners(e, type, resource);
        throw e;
    }

    private void checkPermissionType(final PermissionType type) {
        if (!permittedTypes.contains(type)) {
            permissionDenied(type);
        }
    }

    public VmSecurityManager(final Properties props) {
        permittedTypes = PermissionType.createSet(props);

        setReadPermissions(findPermittedPaths(props, PermissionType.read));
        setWritePermissions(findPermittedPaths(props, PermissionType.write));
        setDeletePermissions(findPermittedPaths(props, PermissionType.delete));
        setExecPermissions(findPermittedPaths(props, PermissionType.exec));
        setLinkPermissions(findPermittedPaths(props, PermissionType.link));

        listeners = new LinkedList<VmSecurityManagerListener>();
    }

    public void addListener(final VmSecurityManagerListener listener) {
        listeners.add(listener);
    }

    private PathPermissions findPermittedPaths(final Properties props,
                                               final PermissionType type) {
        PathPermissions p = new PathPermissions();
        String prefix = type.getPropertyName() + ".permitted";

        for (Object key : props.keySet()) {
            if (key instanceof String
                    && ((String) key).startsWith(prefix)) {
                String value = props.get(key).toString().trim();
                if (value.length() > 0) {
                    p.addPermitRule(value);
                }
            }
        }

        return p;
    }

    private void addClassPath(final PathPermissions perms) {
        String classpath = System.getProperty("java.class.path")
                + ":" + System.getProperty("java.library.path");
        LOGGER.info("adding elements of classpath/library paths as read-permitted paths: " + classpath);
        String[] paths = classpath.split(":");
        for (String p : paths) {
            perms.addPermitRule(p);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public Set<PermissionType> getPermittedTypes() {
        return permittedTypes;
    }

    public PathPermissions getReadPermissions() {
        return readPermissions;
    }

    public PathPermissions getWritePermissions() {
        return writePermissions;
    }

    public PathPermissions getDeletePermittedPaths() {
        return deletePermissions;
    }

    public PathPermissions getExecPermissions() {
        return execPermissions;
    }

    public PathPermissions getLinkPermissions() {
        return linkPermissions;
    }

    ////////////////////////////////////////////////////////////////////////////

    public void setReadPermissions(final PathPermissions p) {
        readPermissions = p;

        if (PERMIT_READ_TO_CLASSPATH) {
            addClassPath(readPermissions);
        }
    }

    public void setWritePermissions(final PathPermissions p) {
        writePermissions = p;
    }

    public void setDeletePermissions(final PathPermissions p) {
        deletePermissions = p;
    }

    public void setExecPermissions(final PathPermissions p) {
        execPermissions = p;
    }

    public void setLinkPermissions(final PathPermissions p) {
        linkPermissions = p;
    }

    public void setHttpGetPermissions(final PathPermissions p) {
        httpGetPermissions = p;
    }

    public void setHttpPutPermissions(final PathPermissions p) {
        httpPutPermissions = p;
    }

    public void setHttpPostPermissions(final PathPermissions p) {
        httpPostPermissions = p;
    }

    @Override
    public void checkPermission(final Permission permission) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.permission);
        }
    }

    @Override
    public void checkPermission(final Permission permission,
                                final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.permission);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.createClassLoader);
        }
    }

    @Override
    public void checkAccess(final Thread thread) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.access);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup threadGroup) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.access);
        }
    }

    @Override
    public void checkExit(final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.exit);
        }
    }

    @Override
    public void checkExec(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.exec);
            if (null == execPermissions || !execPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.exec, s);
            }
        }
    }

    @Override
    public void checkLink(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.link);
            if (null == linkPermissions || !linkPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.link, s);
            }
        }
    }

    @Override
    public void checkRead(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.read);
            // Deny anyway...
            permissionDenied();
        }
    }

    @Override
    public void checkRead(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.read);
            if (null == readPermissions || !readPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.read, s);
            }
        }
    }

    @Override
    public void checkRead(final String s,
                          final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.read);
            // Deny anyway...
            permissionDenied();
        }
    }

    @Override
    public void checkWrite(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.write);
            // Deny anyway...
            permissionDenied();
        }
    }

    @Override
    public void checkWrite(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.write);
            if (null == writePermissions || !writePermissions.isPermitted(s)) {
                permissionDenied(PermissionType.write, s);
            }
        }
    }

    @Override
    public void checkDelete(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.delete);
            if (null == deletePermissions || !deletePermissions.isPermitted(s)) {
                permissionDenied(PermissionType.delete, s);
            }
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.connect);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i,
                             final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.connect);
        }
    }

    @Override
    public void checkListen(final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.listen);
        }
    }

    @Override
    public void checkAccept(final String s,
                            final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accept);
        }
    }

    @Override
    public void checkMulticast(final InetAddress inetAddress) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.multicast);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.propertiesAccess);
        }
    }

    @Override
    public void checkPropertyAccess(java.lang.String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.propertyAccess);
        }
    }

    //public boolean checkTopLevelWindow(java.lang.Object o) {  }

    @Override
    public void checkPrintJobAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.printJobAccess);
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.systemClipboardAccess);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.awtEventQueueAccess);
        }
    }

    @Override
    public void checkPackageAccess(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.packageAccess);
        }
    }

    @Override
    public void checkPackageDefinition(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.packageDefinition);
        }
    }

    @Override
    public void checkSetFactory() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.setFactory);
        }
    }

    @Override
    public void checkMemberAccess(final Class<?> aClass,
                                  final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.memberAccess);
        }
    }

    @Override
    public void checkSecurityAccess(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.securityAccess);
        }
    }
}
