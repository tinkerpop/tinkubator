package org.linkedprocess.security;

import org.linkedprocess.LinkedProcess;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jul 7, 2009
 * Time: 11:39:50 AM
 */
public class VMSecurityManager extends SecurityManager {
    private static final Logger LOGGER = LinkedProcess.getLogger(VMSecurityManager.class);

    // TODO (maybe): make this configurable by turning it into a permission type.
    private static final boolean PERMIT_READ_TO_CLASSPATH = true;

    public enum PermissionType {
        permission("permission", "exercise a permission"),
        createClassLoader("create_class_loader", "create a Java class loader"),
        access("access", "modify a Java thread or thread group"),
        exit("exit", "exit the farm process"),
        exec("exec", "execute a program"),
        link("link", "create a file system link"),
        read("read", "read from a file"),
        write("write", "write to a file"),
        delete("delete", "delete a file"),
        connect("connect", "open a socket connection"),
        listen("listen", "wait for a connection request"),
        accept("accept", "accept a socket connection"),
        multicast("multicast", "use IP multicast"),
        propertiesAccess("properties_access", "access or modify system properties"),
        propertyAccess("property_access", "access or modify a system property"),
        printJobAccess("print_job_access", "initiate a print job request"),
        systemClipboardAccess("system_clipboard_access", "access the system clipboard"),
        awtEventQueueAccess("awt_event_queue_access", "access the Java AWT event queue"),
        packageAccess("package_access", "access a Java package"),
        packageDefinition("package_definition", "define classes in a package"),
        setFactory("set_factory", "set a socket factory"),
        memberAccess("member_access", "access Java members"),
        securityAccess("security_access", "exercise a permission");

        private final String specName;
        private final String label;

        private PermissionType(final String specName,
                               final String label) {
            this.specName = specName;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public boolean isPermitted(final Properties props) {
            return Boolean.valueOf(props.getProperty(getPropertyName()));
        }

        public static Set<PermissionType> createSet(final Properties props) {
            Set<PermissionType> set = new HashSet<PermissionType>();

            for (PermissionType pt : PermissionType.values()) {
                if (pt.isPermitted(props)) {
                    set.add(pt);
                }
            }

            return set;
        }

        public String getPropertyName() {
            return "org.linkedprocess.security." + this;
        }

        public String getSpecName() {
            return specName;
        }
    }

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

    private boolean isVMWorkerThread() {
        // This is weird, but the below (commented out) results in a ClassCircularityError due to permissions associated with class comparison.
        return Thread.currentThread().toString() == VMSandboxedThread.SPECIAL_TOSTRING_VALUE;

        //return Thread.currentThread().getClass() == VMSandboxedThread.class;
    }

    private void permissionDenied() {
        LOGGER.info("denying permission");
        throw new SecurityException("operation is not allowed in VM worker threads");
    }

    private void permissionDenied(final PermissionType type) {
        LOGGER.info("denying permission '" + type.getSpecName() + "'");
        throw new SecurityException("operation type is not allowed in VM worker threads: " + type);
    }

    private void permissionDenied(final PermissionType type, final String resource) {
        LOGGER.info("denying permission '" + type.getSpecName() + "' to resource '" + resource + "'");
        throw new SecurityException("permission '" + type + "' is not granted for resource: " + resource);
    }

    private void checkPermissionType(final PermissionType type) {
        if (!permittedTypes.contains(type)) {
            permissionDenied(type);
        }
    }

    public VMSecurityManager(final Properties props) {
        permittedTypes = PermissionType.createSet(props);

        setReadPermissions(findPermittedPaths(props, PermissionType.read));
        setWritePermissions(findPermittedPaths(props, PermissionType.write));
        setDeletePermissions(findPermittedPaths(props, PermissionType.delete));
        setExecPermissions(findPermittedPaths(props, PermissionType.exec));
        setLinkPermissions(findPermittedPaths(props, PermissionType.link));
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
        String[] paths = System.getProperty("java.class.path").split(":");
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
