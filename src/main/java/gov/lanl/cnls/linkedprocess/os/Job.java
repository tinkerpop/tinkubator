package gov.lanl.cnls.linkedprocess.os;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:34:20 PM
 */
public class Job {
    private final String iqID;
    private final String vmJID;
    private final String appJID;
    private final String expression;

    public Job(final String vmJID,
               final String appJID,
               final String iqID,
               final String expression) {
        this.vmJID = vmJID;
        this.appJID = appJID;
        this.iqID = iqID;
        this.expression = expression;
    }

    public String getAppJID() {
        return appJID;
    }
    
    public String getIQID() {
        return iqID;
    }

    public String getVMJID() {
        return vmJID;
    }

    public String getExpression() {
        return expression;
    }

    public String toString() {
        return "Job("
                + "iqID:'" + iqID + "'"
                + ", vmJID:'" + vmJID + "'"
                + ", appJID:'" + appJID + "'"
                + ", expresison:'" + expression + "')";

    }
}
