package org.linkedprocess.os;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.vm.SubmitJob;

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

    public IQ generateReturnEvalulate() {

        SubmitJob returnSubmitJob = new SubmitJob();
        returnSubmitJob.setFrom(job.getVmJid());
        returnSubmitJob.setTo(job.getVilleinJid());
        returnSubmitJob.setPacketID(job.getJobId());

        if (this.type == ResultType.ABORTED) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setError(new LopError(XMPPError.Condition.not_allowed, LinkedProcess.LopErrorType.JOB_ABORTED, null, LinkedProcess.ClientType.VM));
            return returnSubmitJob;
        } else if (this.type == ResultType.ERROR) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.EVALUATION_ERROR, exception.getMessage(), LinkedProcess.ClientType.VM));
            return returnSubmitJob;
        } else if(this.type == ResultType.PERMISSION_DENIED) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setError(new LopError(XMPPError.Condition.forbidden, LinkedProcess.LopErrorType.PERMISSION_DENIED, exception.getMessage(), LinkedProcess.ClientType.VM));
            return returnSubmitJob;
        } else if (this.type == ResultType.NORMAL_RESULT) {
            returnSubmitJob.setType(IQ.Type.RESULT);
            returnSubmitJob.setExpression(expression);
            return returnSubmitJob;
        } else if (this.type == ResultType.TIMED_OUT) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setError(new LopError(XMPPError.Condition.request_timeout, LinkedProcess.LopErrorType.JOB_TIMED_OUT, "execution of job timed out after " + job.getTimeSpent() + "ms of execution", LinkedProcess.ClientType.VM));
        }
        return returnSubmitJob;

    }
}
