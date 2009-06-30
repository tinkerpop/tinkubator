package gov.lanl.cnls.linkedprocess.os;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:34:20 PM
 */
public class Job {
    private final String jobID;
    private final String vmJID;
    private final String appJID;
    private final String expression;

    public Job(final String vmJID,
               final String appJID,
               final String jobID,
               final String expression) {
        this.vmJID = vmJID;
        this.appJID = appJID;
        this.jobID = jobID;
        this.expression = expression;
    }

    public String getAppJID() {
        return appJID;
    }
    
    public String getJobID() {
        return jobID;
    }

    public String getVMJID() {
        return vmJID;
    }

    public String getExpression() {
        return expression;
    }

    public String toString() {
        return "Job("
                + "jobID:'" + jobID + "'"
                + ", vmJID:'" + vmJID + "'"
                + ", appJID:'" + appJID + "'"
                + ", expression:'" + expression + "')";

    }
}
