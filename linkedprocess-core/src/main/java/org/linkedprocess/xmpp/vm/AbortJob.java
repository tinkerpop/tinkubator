package org.linkedprocess.xmpp.vm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:31 PM
 */
public class AbortJob extends VirtualMachineIq {

    protected String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return this.jobId;
    }

    public String getChildElementXML() {

        Element abandonJobElement = new Element(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE);
        if (this.vmPassword != null) {
            abandonJobElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if (this.jobId != null) {
            abandonJobElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }

        return LinkedProcess.xmlOut.outputString(abandonJobElement);
    }
}
