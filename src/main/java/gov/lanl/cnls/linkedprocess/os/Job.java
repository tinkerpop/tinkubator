package gov.lanl.cnls.linkedprocess.os;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:34:20 PM
 */
public class Job {
    private final String jobId;
    private final String vmJid;
    private final String appJid;
    private final String expression;
    private long timeSpent;

    public Job(final String vmJid,
               final String appJid,
               final String jobId,
               final String expression) {
        this.vmJid = vmJid;
        this.appJid = appJid;
        this.jobId = jobId;
        this.expression = expression;

        timeSpent = 0;
    }

    public String getAppJid() {
        return appJid;
    }
    
    public String getJobId() {
        return jobId;
    }

    public String getVMJID() {
        return vmJid;
    }

    public String getExpression() {
        return expression;
    }

    public void increaseTimeSpent(final long time) {
        timeSpent += time;
    }
    
    public long getTimeSpent() {
        return timeSpent;
    }

    public String toString() {
        return "Job("
                + "jobID:'" + jobId + "'"
                + ", vmJID:'" + vmJid + "'"
                + ", appJID:'" + appJid + "'"
                + ", expression:'" + expression + "')";

    }
}
