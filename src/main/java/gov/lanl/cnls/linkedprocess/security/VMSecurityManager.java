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

    public VMSecurityManager() {
    }

    @Override
    public void checkPermission(final Permission permission) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPermission(final Permission permission,
                                final Object o) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkAccess(final Thread thread) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup threadGroup) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkExit(final int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkExec(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkLink(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkRead(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkRead(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkRead(final String s,
                          final Object o) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkWrite(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkWrite(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkDelete(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i,
                             final Object o) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkListen(final int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkAccept(final String s,
                            final int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkMulticast(final InetAddress inetAddress) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPropertyAccess(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    //public boolean checkTopLevelWindow(java.lang.Object o) {  }

    @Override
    public void checkPrintJobAccess() {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPackageAccess(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPackageDefinition(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkSetFactory() {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkMemberAccess(final Class<?> aClass,
                                  final int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkSecurityAccess(final String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    private boolean isVMWorkerThread() {
        return Thread.currentThread() instanceof VMSandboxedThread;
    }
}
