package gov.lanl.cnls.linkedprocess.security;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * Author: josh
 * Date: Jul 7, 2009
 * Time: 11:39:50 AM
 */
public class VMSecurityManager extends SecurityManager {
    private static final String NOT_ALLOWED_IN_VM = "operation is not allowed for VM worker threads";

    private static final String
            PERMISSION = "permission",
            CREATECLASSLOADER = "createClassLoader",
            ACCESS = "access",
            EXIT = "exit",
            EXEC = "exec",
            LINK = "link",
            READ = "read",
            WRITE = "write",
            DELETE = "delete",
            CONNECT = "connect",
            LISTEN = "listen",
            ACCEPT = "accept",
            MULTICAST = "multicast",
            PROPERTIESACCESS = "propertiesAccess",
            PROPERTYACCESS = "propertyAccess",
            PRINTJOBACCESS = "printJobAccess",
            CLIPBOARDACCESS = "clipboardAccess",
            AWTEVENTQUEUEACCESS = "awtEventQueueAccess",
            PACKAGEACCESS = "packageAccess",
            PACKAGEDEFINITION = "packageDefinition",
            SETFACTORY = "setFactory",
            MEMBERACCESS = "memberAccess",
            SECURITYACCESS = "securityAccess";


    public VMSecurityManager() {
    }

    private void permissionDenied(final String permissionType) {
        throw new SecurityException("operation is not allowed for VM worker threads: " + permissionType);
    }

    @Override
    public void checkPermission(final Permission permission) {
        if (isVMWorkerThread()) {
            permissionDenied(PERMISSION);
        }
    }

    @Override
    public void checkPermission(final Permission permission,
                                final Object o) {
        if (isVMWorkerThread()) {
            permissionDenied(PERMISSION);
        }
    }

    @Override
    public void checkCreateClassLoader() {
    //    if (isVMWorkerThread()) {
    //    permissionDenied(CREATECLASSLOADER);
    //    }
    }

    @Override
    public void checkAccess(final Thread thread) {
        if (isVMWorkerThread()) {
            permissionDenied(ACCESS);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup threadGroup) {
        if (isVMWorkerThread()) {
            permissionDenied(ACCESS);
        }
    }

    @Override
    public void checkExit(final int i) {
        if (isVMWorkerThread()) {
            permissionDenied(EXIT);
        }
    }

    @Override
    public void checkExec(final String s) {
        if (isVMWorkerThread()) {
            permissionDenied(EXEC);
        }
    }

    @Override
    public void checkLink(final String s) {
        if (isVMWorkerThread()) {
            permissionDenied(LINK);
        }
    }

    @Override
    public void checkRead(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            permissionDenied(READ);
        }
    }

    @Override
    public void checkRead(final String s) {
        if (isVMWorkerThread()) {
            permissionDenied(READ);
        }
    }

    @Override
    public void checkRead(final String s,
                          final Object o) {
        if (isVMWorkerThread()) {
            permissionDenied(READ);
        }
    }

    @Override
    public void checkWrite(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            permissionDenied(WRITE);
        }
    }

    @Override
    public void checkWrite(final String s) {
        if (isVMWorkerThread()) {
            permissionDenied(WRITE);
        }
    }

    @Override
    public void checkDelete(final String s) {
        if (isVMWorkerThread()) {
            permissionDenied(DELETE);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i) {
        if (isVMWorkerThread()) {
            permissionDenied(CONNECT);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i,
                             final Object o) {
        if (isVMWorkerThread()) {
            permissionDenied(CONNECT);
        }
    }

    @Override
    public void checkListen(final int i) {
        if (isVMWorkerThread()) {
            permissionDenied(LISTEN);
        }
    }

    @Override
    public void checkAccept(final String s,
                            final int i) {
        if (isVMWorkerThread()) {
            permissionDenied(ACCEPT);
        }
    }

    @Override
    public void checkMulticast(final InetAddress inetAddress) {
        if (isVMWorkerThread()) {
            permissionDenied(MULTICAST);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (isVMWorkerThread()) {
            permissionDenied(PROPERTIESACCESS);
        }
    }

    @Override
    public void checkPropertyAccess(java.lang.String s) {
        if (isVMWorkerThread()) {
            permissionDenied(PROPERTYACCESS);
        }
    }

    //public boolean checkTopLevelWindow(java.lang.Object o) {  }

    @Override
    public void checkPrintJobAccess() {
        if (isVMWorkerThread()) {
            permissionDenied(PRINTJOBACCESS);
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (isVMWorkerThread()) {
            permissionDenied(CLIPBOARDACCESS);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (isVMWorkerThread()) {
            permissionDenied(AWTEVENTQUEUEACCESS);
        }
    }

    @Override
    public void checkPackageAccess(final String s) {
    //    if (isVMWorkerThread()) {
    //    permissionDenied(PACKAGEACCESS);
    //    }
    }

    @Override
    public void checkPackageDefinition(final String s) {
    //    if (isVMWorkerThread()) {
    //    permissionDenied(PACKAGEDEFINITION);
    //    }
    }

    @Override
    public void checkSetFactory() {
        if (isVMWorkerThread()) {
            permissionDenied(SETFACTORY);
        }
    }

    @Override
    public void checkMemberAccess(final Class<?> aClass,
                                  final int i) {
    //    if (isVMWorkerThread()) {
    //    permissionDenied(MEMBERACCESS);
    //    }
    }

    @Override
    public void checkSecurityAccess(final String s) {
        if (isVMWorkerThread()) {
            permissionDenied(SECURITYACCESS);
        }
    }

    private boolean isVMWorkerThread() {
        // This is weird, but the below (commented out) results in a ClassCircularityError due to permissions associated with class comparison.
        return Thread.currentThread().toString() == VMSandboxedThread.SPECIAL_TOSTRING_VALUE;

        //return Thread.currentThread().getClass() == VMSandboxedThread.class;
    }
}
