package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:31 PM
 */
public class AbortJob extends VirtualMachineIq {

    protected String errorMessage;

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getChildElementXML() {

        Element abandonJobElement = new Element(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE);
        if(this.jobId != null) {
            abandonJobElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }
        if(this.errorMessage != null) {
            abandonJobElement.setText(this.errorMessage);
        }
        return LinkedProcess.xmlOut.outputString(abandonJobElement);
    }
}
