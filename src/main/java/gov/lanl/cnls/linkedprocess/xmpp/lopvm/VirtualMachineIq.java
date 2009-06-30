package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:56:48 AM
 */
public abstract class VirtualMachineIq extends IQ {

    public static final String JOB_ID_ATTRIBUTE = "job_id";

    protected String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
       return this.jobId;
    }
}
