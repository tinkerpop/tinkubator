package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.vm.SubmitJob;
import org.jivesoftware.smack.packet.IQ;

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
    private final Exception exception;
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
                     final Exception exception) {
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

    public Exception getException() {
        return exception;
    }

    public SubmitJob generateReturnEvalulate() {
        SubmitJob returnEval = new SubmitJob();
        returnEval.setTo(job.getAppJid());
        returnEval.setPacketID(job.getJobId());
        String msg = "";
        if (exception != null) {
            msg = exception.getMessage();
        }

        switch (type) {
            case ABORTED:
                returnEval.setType(IQ.Type.ERROR);
                returnEval.setErrorType(LinkedProcess.ErrorType.JOB_ABORTED);
                if (null != msg) {
                    returnEval.setExpression(null);
                    returnEval.setErrorMessage(msg);
                }
                break;
            case ERROR:
                returnEval.setType(IQ.Type.ERROR);
                returnEval.setErrorType(LinkedProcess.ErrorType.EVALUATION_ERROR);
                if (null != msg) {
                    returnEval.setExpression(null);
                    returnEval.setErrorMessage(msg);
                }
                break;
            case NORMAL_RESULT:
                returnEval.setType(IQ.Type.RESULT);
                returnEval.setExpression(expression);
                break;
            case TIMED_OUT:
                returnEval.setType(IQ.Type.ERROR);
                returnEval.setErrorType(LinkedProcess.ErrorType.JOB_TIMED_OUT);
                returnEval.setExpression(null);
                returnEval.setErrorMessage("execution of job timed out after " + job.getTimeSpent() + "ms of execution");
        }

        return returnEval;
    }
}
