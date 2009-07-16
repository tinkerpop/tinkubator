package org.linkedprocess.security;

/**
 * This is merely a marker class which allows VMSecurityManager to distinguish VM worker threads from all others.
 */
public class VMSandboxedThread extends Thread {
    // Definitely a hack...
    public static final String SPECIAL_TOSTRING_VALUE = "!";

    public VMSandboxedThread(final Runnable r,
                             final String name) {
        super(r, name);
    }

    public String toString() {
        return SPECIAL_TOSTRING_VALUE;
    }
}
