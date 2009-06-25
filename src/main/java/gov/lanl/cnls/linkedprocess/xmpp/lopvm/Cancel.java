package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:31 PM
 */
public class Cancel extends IQ {

    public static final String CANCEL_TAGNAME = "cancel";

    private String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
       return this.jobId;
    }

    public String getChildElementXML() {
        return("\n  <" + CANCEL_TAGNAME + " xmlns=\"" + LinkedProcess.LOP_NAMESPACE +"\" id=\"" + jobId + "\" />");
    }
}
