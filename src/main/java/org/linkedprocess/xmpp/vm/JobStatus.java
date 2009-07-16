package gov.lanl.cnls.linkedprocess.xmpp.vm;

import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:02 PM
 */
public class JobStatus extends VirtualMachineIq {

    protected LinkedProcess.JobStatus value;

    public void setValue(LinkedProcess.JobStatus status) {
        this.value = status;
    }

    public String getValue() {
        return this.value.toString();
    }

    public String getChildElementXML() {

        Element jobStatusElement = new Element(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE);
        if(this.vmPassword != null) {
            jobStatusElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if(this.jobId != null) {
            jobStatusElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }
        if(this.value != null) {
            jobStatusElement.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, this.value.toString());
        }
        if(this.errorType != null) {
            jobStatusElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                jobStatusElement.setText(this.errorMessage);
            }
        }

        return LinkedProcess.xmlOut.outputString(jobStatusElement);
    }
}
