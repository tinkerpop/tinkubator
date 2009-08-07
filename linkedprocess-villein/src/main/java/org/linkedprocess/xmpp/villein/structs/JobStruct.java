package org.linkedprocess.xmpp.villein.structs;

import org.jivesoftware.smack.packet.XMPPError;

/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 12:59:40 PM
 */
public class JobStruct implements Comparable {
    protected String jobId;
    protected String result;
    protected String expression;
    protected XMPPError error;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public XMPPError getError() {
        return error;
    }

    public void setError(XMPPError error) {
        this.error = error;
    }

    public String getExpression()  {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isComplete() {
        return (null != error || null != result);
    }

    public int compareTo(Object job) {
        if (job instanceof JobStruct) {
            return this.jobId.compareTo(((JobStruct) job).getJobId());
        } else {
            throw new ClassCastException();
        }
    }


}
