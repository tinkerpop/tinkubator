package org.linkedprocess.xmpp;

import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.IQ;
import org.jdom.output.XMLOutputter;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.io.IOException;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 10:14:25 AM
 */
public abstract class LopIq extends IQ {

    protected LinkedProcess.ErrorType errorType;
    protected String errorMessage;
    protected String vmPassword;

    public void setErrorType(LinkedProcess.ErrorType errorType) {
        this.errorType = errorType;
    }

    public LinkedProcess.ErrorType getErrorType() {
        return this.errorType;
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
