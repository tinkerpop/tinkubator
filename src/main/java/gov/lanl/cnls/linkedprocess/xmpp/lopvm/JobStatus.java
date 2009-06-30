package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.VMScheduler;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:02 PM
 */
public class JobStatus extends VirtualMachineIq {

    public static final String JOB_STATUS_TAGNAME = "job_status";
 
    private VMScheduler.JobStatus value;

    public void setValue(VMScheduler.JobStatus status) {
        this.value = status;
    }

    public String getValue() {
        return this.value.toString();
    }

    public String getChildElementXML() {

        Element jobStatusElement = new Element(JOB_STATUS_TAGNAME, LinkedProcess.LOP_VM_NAMESPACE);
        if(this.jobId != null) {
            jobStatusElement.setAttribute(JOB_ID_ATTRIBUTE, this.jobId);
        }
        if(this.value != null) {
            jobStatusElement.setAttribute("value", this.value.toString());
        }
        return LinkedProcess.xmlOut.outputString(jobStatusElement);
    }
}
