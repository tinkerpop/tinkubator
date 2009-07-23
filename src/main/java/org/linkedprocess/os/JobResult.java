package org.linkedprocess.os;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.ErrorIq;
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
        NORMAL_RESULT, ERROR, ABORTED, TIMED_OUT
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

        switch (type) {
            case ABORTED:
                ErrorIq errorIq = new ErrorIq();
                errorIq.setTo(this.job.getVilleinJid());
                errorIq.setFrom(this.job.getVmJid());
                errorIq.setPacketID(job.getJobId());
                errorIq.setType(IQ.Type.ERROR);
                errorIq.setXmppErrorType(XMPPError.Type.CANCEL);
                errorIq.setLopErrorType(LinkedProcess.LopErrorType.JOB_ABORTED);
                errorIq.setErrorMessage(exception.getMessage());
                errorIq.setCondition(XMPPError.Condition.not_allowed);
                errorIq.setClientType(ErrorIq.ClientType.VM);
                return errorIq;
            case ERROR:
                errorIq = new ErrorIq();
                errorIq.setTo(this.job.getVilleinJid());
                errorIq.setFrom(this.job.getVmJid());
                errorIq.setPacketID(job.getJobId());
                errorIq.setType(IQ.Type.ERROR);
                errorIq.setXmppErrorType(XMPPError.Type.MODIFY);
                errorIq.setLopErrorType(LinkedProcess.LopErrorType.EVALUATION_ERROR);
                errorIq.setErrorMessage(exception.getMessage());
                errorIq.setCondition(XMPPError.Condition.bad_request);
                errorIq.setClientType(ErrorIq.ClientType.VM);
                return errorIq;
            case NORMAL_RESULT:
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setFrom(job.getVmJid());
                returnSubmitJob.setTo(job.getVilleinJid());
                returnSubmitJob.setPacketID(job.getJobId());
                returnSubmitJob.setType(IQ.Type.RESULT);
                returnSubmitJob.setExpression(expression);
                return returnSubmitJob;
            case TIMED_OUT:
                errorIq = new ErrorIq();
                errorIq.setTo(this.job.getVilleinJid());
                errorIq.setFrom(this.job.getVmJid());
                errorIq.setPacketID(job.getJobId());
                errorIq.setType(IQ.Type.ERROR);
                errorIq.setXmppErrorType(XMPPError.Type.CANCEL);
                errorIq.setLopErrorType(LinkedProcess.LopErrorType.JOB_TIMED_OUT);
                errorIq.setErrorMessage("execution of job timed out after " + job.getTimeSpent() + "ms of execution");
                errorIq.setCondition(XMPPError.Condition.request_timeout);
                errorIq.setClientType(ErrorIq.ClientType.VM);
                return errorIq;
        }
        return null;
    }
}
