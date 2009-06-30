package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.VMScheduler;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:02 PM
 */
public class JobStatus extends VirtualMachineIq {

    public String getChildElementXML() {

        Element jobStatusElement = new Element(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE);
        if(this.jobId != null) {
            jobStatusElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }
        if(this.value != null) {
            jobStatusElement.setAttribute("value", this.value.toString());
        }
        return LinkedProcess.xmlOut.outputString(jobStatusElement);
    }
}
