package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.XMPPError;

/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 12:59:40 PM
 */
public class Job implements Comparable {
    protected String jobId;
    protected String result;
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

    public int compareTo(Object job) {
        if (job instanceof Job) {
            return this.jobId.compareTo(((Job) job).getJobId());
        } else {
            throw new ClassCastException();
        }
    }


}
