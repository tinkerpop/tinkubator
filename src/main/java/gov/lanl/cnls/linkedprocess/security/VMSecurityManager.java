package gov.lanl.cnls.linkedprocess.security;

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
    public void checkPermission(java.security.Permission permission) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPermission(java.security.Permission permission, java.lang.Object o) {
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
    public void checkAccess(java.lang.Thread thread) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkAccess(java.lang.ThreadGroup threadGroup) {
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
    public void checkExec(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkLink(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkRead(java.io.FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkRead(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkRead(java.lang.String s, java.lang.Object o) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkWrite(java.io.FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkWrite(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkDelete(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkConnect(java.lang.String s, int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkConnect(java.lang.String s, int i, java.lang.Object o) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkListen(int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkAccept(java.lang.String s, int i) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkMulticast(java.net.InetAddress inetAddress) {
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
    public void checkPackageAccess(java.lang.String s) {
        if (isVMWorkerThread()) {
            throw new SecurityException(NOT_ALLOWED_IN_VM);
        }
    }

    @Override
    public void checkPackageDefinition(java.lang.String s) {
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
