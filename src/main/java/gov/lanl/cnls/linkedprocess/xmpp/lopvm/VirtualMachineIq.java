package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.os.VMScheduler;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:56:48 AM
 */
public abstract class VirtualMachineIq extends IQ {

    protected String jobId;
    protected VMScheduler.JobStatus value;

    public void setValue(VMScheduler.JobStatus status) {
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
