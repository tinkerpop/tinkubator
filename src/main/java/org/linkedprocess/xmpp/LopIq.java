package org.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 10:14:25 AM
 */
public abstract class LopIq extends IQ {

    protected LinkedProcess.LopErrorType lopErrorType;
    protected String errorMessage;
    protected String vmPassword;

    public void setErrorType(LinkedProcess.LopErrorType lopErrorType) {
        this.lopErrorType = lopErrorType;
    }

    public LinkedProcess.LopErrorType getErrorType() {
        return this.lopErrorType;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setVmPassword(String vmPassword) {
        this.vmPassword = vmPassword;
    }

    public String getVmPassword() {
        return this.vmPassword;
    }

}
