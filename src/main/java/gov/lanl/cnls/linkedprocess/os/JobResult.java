package gov.lanl.cnls.linkedprocess.os;

import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.IQ;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:35:11 PM
 */
public class JobResult {

    public enum ResultType {
        NORMAL_RESULT, ERROR, CANCELLED
    }

    private final Job job;
    private final ResultType type;
    private final String expression;
    private final Exception exception;

    public JobResult(final Job job,
                     final String expression) {
        this.job = job;
        this.expression = expression;
        this.exception = null;
        type = ResultType.NORMAL_RESULT;
    }

    public JobResult(final Job job,
                     final Exception exception) {
        this.job = job;
        this.expression = null;
        this.exception = exception;
        this.type = ResultType.ERROR;
    }

    public JobResult(final Job job) {
        this.job = job;
        this.expression = null;
        this.exception = null;
        this.type = ResultType.CANCELLED;
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
    
    public Evaluate generateReturnEvalulate() {
        Evaluate returnEval = new Evaluate();
        returnEval.setTo(job.getAppJid());
        returnEval.setPacketID(job.getJobId());

        switch (type) {
            case CANCELLED:
                returnEval.setType(IQ.Type.ERROR);
                returnEval.setErrorType(LinkedProcess.Errortype.JOB_ABORTED);
                break;
            case ERROR:
                returnEval.setType(IQ.Type.ERROR);
                returnEval.setErrorType(LinkedProcess.Errortype.EVALUATION_ERROR);
                String msg = exception.getMessage();
                if (null != msg) {
                    returnEval.setExpression(msg);
                }
                break;
            case NORMAL_RESULT:
                returnEval.setType(IQ.Type.RESULT);
                returnEval.setExpression(expression);
                break;
        }

        return returnEval;
    }
}
