package gov.lanl.cnls.linkedprocess.security;

/**
 * This is merely a marker class which allows VMSecurityManager to distinguish VM worker threads from all others.
 */
public class VMSandboxedThread extends Thread {
    public VMSandboxedThread(final Runnable r,
                             final String name) {
        super(r, name);
    }
}
