package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:02 PM
 */
public class Status extends IQ {

    public static final String STATUS_TAGNAME = "status";

    private String jobId;
    private String value;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
       return this.jobId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String getChildElementXML() {
        return("\n  <" + STATUS_TAGNAME + " xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE +"\" id=\"" + jobId + "\" value=\"" + value + "\" />");
    }
}
