package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:31 PM
 */
public class Cancel extends IQ {

    public static final String CANCEL_TAGNAME = "cancel";
    public static final String JOB_ID_ATTRIBUTE = "job_id";

    private String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
       return this.jobId;
    }

    public String getChildElementXML() {

        Element cancelElement = new Element(CANCEL_TAGNAME, LinkedProcess.LOP_VM_NAMESPACE);
        if(this.jobId != null) {
            cancelElement.setAttribute(JOB_ID_ATTRIBUTE, this.jobId);
        }
        return LinkedProcess.xmlOut.outputString(cancelElement);
    }
}
