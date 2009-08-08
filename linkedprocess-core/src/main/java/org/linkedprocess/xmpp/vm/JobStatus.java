package org.linkedprocess.xmpp.vm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:02 PM
 */
public class JobStatus extends VirtualMachineIq {

    protected LinkedProcess.JobStatus value;
    protected String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return this.jobId;
    }

    public void setValue(LinkedProcess.JobStatus status) {
        this.value = status;
    }

    public String getValue() {
        return this.value.toString();
    }

    public String getChildElementXML() {

        Element jobStatusElement = new Element(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE);
        if (this.vmPassword != null) {
            jobStatusElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if (this.jobId != null) {
            jobStatusElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }
        if (this.value != null) {
            jobStatusElement.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, this.value.toString());
        }
        return LinkedProcess.xmlOut.outputString(jobStatusElement);
    }
}
