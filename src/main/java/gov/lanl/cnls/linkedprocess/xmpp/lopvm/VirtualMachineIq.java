package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:56:48 AM
 */
public abstract class VirtualMachineIq extends IQ {

    protected String jobId;
    protected LinkedProcess.JobStatus value;

    public void setValue(LinkedProcess.JobStatus status) {
        this.value = status;
    }

    public String getValue() {
        return this.value.toString();
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
       return this.jobId;
    }
}
