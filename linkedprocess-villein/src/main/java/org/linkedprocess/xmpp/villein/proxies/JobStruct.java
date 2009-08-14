/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopError;

/**
 * A JobStruct is a data structure representing a job. A job is submitted to and returned by a virtual machine.
 * A submitted job does not have a result or error. However, a submitted job should have an expression.
 * The expression is a code fragement that is to be executed by the virtual machine and must be in the language of the virtual machine species.
 * When a JobStruct is returned by a submit_job call, the result or error may be set along with the complete flag.
 * 
 * User: marko
 * Date: Jul 28, 2009
 * Time: 12:59:40 PM
 */
public class JobStruct implements Comparable {
    protected String jobId;
    protected String result;
    protected String expression;
    protected LopError lopError;
    protected boolean complete = false;

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

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean wasAborted() {
        return (null != lopError && LinkedProcess.LopErrorType.JOB_ABORTED != lopError.getLopErrorType());
    }

    public boolean wasSuccessful() {
        return (complete && null == lopError);
    }

    public int compareTo(Object job) {
        if (job instanceof JobStruct) {
            return this.jobId.compareTo(((JobStruct) job).getJobId());
        } else {
            throw new ClassCastException();
        }
    }

    public String toString() {
        if(null == lopError)
            return "job id[" + jobId + "], complete[" + complete + "], result[" + result + "]";
        else
            return "job id[" + jobId + "], complete[" + complete + "], error[" + lopError.getLopErrorType().toString() + "]";
    }


}
