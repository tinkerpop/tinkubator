package gov.lanl.cnls.linkedprocess.xmpp;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 10:14:25 AM
 */
public abstract class LopIq extends IQ {

    protected LinkedProcess.Errortype errorType;
    protected String errorMessage;

    public void setErrorType(LinkedProcess.Errortype errorType) {
        this.errorType = errorType;
    }

    public LinkedProcess.Errortype getErrorType() {
        return this.errorType;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

}
