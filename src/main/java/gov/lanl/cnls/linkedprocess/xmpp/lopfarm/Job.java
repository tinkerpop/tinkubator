package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:34:20 PM
 */
public class Job {
    private final String iqID;
    private final String clientJID;
    private final String expression;

    public Job(final String clientJID,
               final String iqID,
               final String expression) {
        this.clientJID = clientJID;
        this.iqID = iqID;
        this.expression = expression;
    }

    public String getClientJID() {
        return clientJID;
    }
    
    public String getIQID() {
        return iqID;
    }

    public String getExpression() {
        return expression;
    }
}
