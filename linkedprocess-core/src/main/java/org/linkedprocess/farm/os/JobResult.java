/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.os;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LopError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.SubmitJob;

import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:35:11 PM
 */
public class JobResult {
    private static final Logger LOGGER = LinkedProcess.getLogger(JobResult.class);

    public enum ResultType {
        NORMAL_RESULT, ERROR, PERMISSION_DENIED, ABORTED, TIMED_OUT
    }

    private final Job job;
    private final ResultType type;
    private final String expression;
    private final Throwable exception;
    private final long timeout;

    public JobResult(final Job job,
                     final String expression) {
        this.job = job;
        this.expression = expression;
        this.exception = null;
        this.timeout = 0;
        type = ResultType.NORMAL_RESULT;
        LOGGER.info("normal job result");
    }

    public JobResult(final Job job,
                     final Throwable exception) {
        this.job = job;
        this.expression = null;
        //this.expression = exception.getMessage();
        this.exception = exception;
        this.timeout = 0;
        this.type = ResultType.ERROR;
        LOGGER.info("error job result");
    }

    public JobResult(final Job job) {
        this.job = job;
        this.expression = null;
        this.exception = null;
        this.timeout = 0;
        this.type = ResultType.ABORTED;
        LOGGER.info("aborted job result");
    }

    public JobResult(final Job job,
                     final long timeout) {
        this.job = job;
        this.timeout = timeout;
        this.expression = null;
        this.exception = null;
        this.type = ResultType.TIMED_OUT;
        LOGGER.info("timed-out job result");
    }

    public Job getJob() {
        return this.job;
    }

    public ResultType getType() {
        return type;
    }

    public String getExpression() {
        return expression;
    }

    public Throwable getException() {
        return exception;
    }

    public SubmitJob generateReturnSubmitJob() {

        SubmitJob returnSubmitJob = new SubmitJob();
        //TODO: returnSubmitJob.setFrom(job.getVmId());
        returnSubmitJob.setTo(job.getVilleinJid());
        returnSubmitJob.setPacketID(job.getJobId());
        returnSubmitJob.setVmId(job.getVmId());

        if (this.type == ResultType.ABORTED) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setLopError(new LopError(XMPPError.Condition.not_allowed, LinkedProcess.LopErrorType.JOB_ABORTED, null, this.job.getJobId()));
            return returnSubmitJob;
        } else if (this.type == ResultType.ERROR) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            if (this.exception instanceof SecurityException) {
                // SecurityExceptions are handled differently than all other errors.
                returnSubmitJob.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.PERMISSION_DENIED, exception.getMessage(), this.job.getJobId()));
            } else {
                returnSubmitJob.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.EVALUATION_ERROR, exception.getMessage(), this.job.getJobId()));
            }
            return returnSubmitJob;
        } else if (this.type == ResultType.PERMISSION_DENIED) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setLopError(new LopError(XMPPError.Condition.forbidden, LinkedProcess.LopErrorType.PERMISSION_DENIED, exception.getMessage(), this.job.getJobId()));
            return returnSubmitJob;
        } else if (this.type == ResultType.NORMAL_RESULT) {
            returnSubmitJob.setType(IQ.Type.RESULT);
            returnSubmitJob.setExpression(expression);
            return returnSubmitJob;
        } else if (this.type == ResultType.TIMED_OUT) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setLopError(new LopError(XMPPError.Condition.request_timeout, LinkedProcess.LopErrorType.JOB_TIMED_OUT, "execution of job timed out after " + job.getTimeSpent() + "ms of execution", this.job.getJobId()));
        }
        return returnSubmitJob;

    }
}
