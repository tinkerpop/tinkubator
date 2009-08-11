package org.linkedprocess.xmpp.villein.proxies;

import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopError;

/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 12:59:40 PM
 */
public class JobStruct implements Comparable {
    protected String jobId;
    protected String result;
    protected String expression;
    protected LopError lopError;

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

    public LopError getLopError() {
        return lopError;
    }

    public void setLopError(LopError lopError) {
        this.lopError = lopError;
    }

    public String getExpression()  {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isComplete() {
        return (null != lopError || null != result);
    }

    public boolean wasAborted() {
        return(null != lopError && LinkedProcess.LopErrorType.JOB_ABORTED != lopError.getLopErrorType());
    }

    public int compareTo(Object job) {
        if (job instanceof JobStruct) {
            return this.jobId.compareTo(((JobStruct) job).getJobId());
        } else {
            throw new ClassCastException();
        }
    }


}
