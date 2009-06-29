package gov.lanl.cnls.linkedprocess.os;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:34:20 PM
 */
public class Job {
    private final String iqId;
    private final String vmJid;
    private final String appJid;
    private final String expression;

    public Job(final String vmJid,
               final String appJid,
               final String iqId,
               final String expression) {
        this.vmJid = vmJid;
        this.appJid = appJid;
        this.iqId = iqId;
        this.expression = expression;
    }

    public String getAppJid() {
        return appJid;
    }
    
    public String getIqId() {
        return iqId;
    }

    public String getVmJid() {
        return vmJid;
    }

    public String getExpression() {
        return expression;
    }
}
