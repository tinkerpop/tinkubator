/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.security;

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

    // TODO (maybe): make this configurable by turning it into a exercisePermission type.
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

    private void permissionDenied(final String msg) {
        LOGGER.info("denying exercisePermission (" + msg + ")");
        SecurityException e = new SecurityException("operation is not allowed in VM worker threads");
        alertListeners(e, null, null);
        throw e;
    }

    private void permissionDenied(final PermissionType type) {
        LOGGER.info("denying exercisePermission '" + type.getSpecName() + "'");
        SecurityException e = new SecurityException("operation type is not allowed in VM worker threads: " + type);
        alertListeners(e, type, null);
        throw e;
    }

    private void permissionDenied(final PermissionType type, final String resource) {
        LOGGER.info("denying exercisePermission '" + type.getSpecName() + "' to resource '" + resource + "'");
//new Exception().printStackTrace();

        SecurityException e = new SecurityException("exercisePermission '" + type + "' is not granted for resource: " + resource);
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

        setReadPermissions(findPermittedPaths(props, PermissionType.readFile));
        setWritePermissions(findPermittedPaths(props, PermissionType.writeFile));
        setDeletePermissions(findPermittedPaths(props, PermissionType.deleteFile));
        setExecPermissions(findPermittedPaths(props, PermissionType.executeProgram));
        setLinkPermissions(findPermittedPaths(props, PermissionType.createFileLink));

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
        LOGGER.info("adding elements of classpath/library paths as readFile-permitted paths: " + classpath);
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
            checkPermissionType(PermissionType.exercisePermission);
        }
    }

    @Override
    public void checkPermission(final Permission permission,
                                final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.exercisePermission);
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
            checkPermissionType(PermissionType.modifyThread);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup threadGroup) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.modifyThread);
        }
    }

    @Override
    public void checkExit(final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.shutdownFarm);
        }
    }

    @Override
    public void checkExec(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.executeProgram);
            if (null == execPermissions || !execPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.executeProgram, s);
            }
        }
    }

    @Override
    public void checkLink(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.createFileLink);
            if (null == linkPermissions || !linkPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.createFileLink, s);
            }
        }
    }

    @Override
    public void checkRead(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.readFile);

            // Deny anyway...
            // FIXME: allowing readFile modifyThread with FileDescriptors represents a major security vulnerability.
            //        We are allowing it here only for the sake of a demo, and need a better solution.
            //permissionDenied("readFile exercisePermission to file by descriptor: " + fileDescriptor);
        }
    }

    @Override
    public void checkRead(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.readFile);
            if (null == readPermissions || !readPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.readFile, s);
            }
        }
    }

    @Override
    public void checkRead(final String s,
                          final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.readFile);
            // Deny anyway...
            permissionDenied("readFile exercisePermission to file: " + s + " in context: " + o);
        }
    }

    @Override
    public void checkWrite(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.writeFile);
            // Deny anyway...
            // FIXME: allowing readFile modifyThread with FileDescriptors represents a major security vulnerability.
            //        We are allowing it here only for the sake of a demo, and need a better solution.
            //permissionDenied("writeFile exercisePermission to file descriptor: " + fileDescriptor);
        }
    }

    @Override
    public void checkWrite(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.writeFile);
            if (null == writePermissions || !writePermissions.isPermitted(s)) {
                permissionDenied(PermissionType.writeFile, s);
            }
        }
    }

    @Override
    public void checkDelete(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.deleteFile);
            if (null == deletePermissions || !deletePermissions.isPermitted(s)) {
                permissionDenied(PermissionType.deleteFile, s);
            }
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.openConnection);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i,
                             final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.openConnection);
        }
    }

    @Override
    public void checkListen(final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.listenForConnection);
        }
    }

    @Override
    public void checkAccept(final String s,
                            final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.acceptConnection);
        }
    }

    @Override
    public void checkMulticast(final InetAddress inetAddress) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.performMulticast);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessProperties);
        }
    }

    @Override
    public void checkPropertyAccess(java.lang.String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessProperty);
        }
    }

    //public boolean checkTopLevelWindow(java.lang.Object o) {  }

    @Override
    public void checkPrintJobAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessPrintJob);
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessSystemClipboard);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessAWTEventQueue);
        }
    }

    @Override
    public void checkPackageAccess(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessPackage);
        }
    }

    @Override
    public void checkPackageDefinition(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.defineClass);
        }
    }

    @Override
    public void checkSetFactory() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.setSocketFactory);
        }
    }

    @Override
    public void checkMemberAccess(final Class<?> aClass,
                                  final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accessMember);
        }
    }

    @Override
    public void checkSecurityAccess(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.exerciseNamedPermission);
        }
    }
}
