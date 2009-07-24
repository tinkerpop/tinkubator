package org.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 10:14:25 AM
 */
public abstract class LopIq extends IQ {

    protected String errorMessage;
    protected String vmPassword;


    public void setVmPassword(String vmPassword) {
        this.vmPassword = vmPassword;
    }

    public String getVmPassword() {
        return this.vmPassword;
    }

}
