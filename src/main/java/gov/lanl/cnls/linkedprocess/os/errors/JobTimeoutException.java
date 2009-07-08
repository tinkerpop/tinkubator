package gov.lanl.cnls.linkedprocess.os.errors;

import gov.lanl.cnls.linkedprocess.os.Job;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class JobTimeoutException extends SchedulerException {
    public JobTimeoutException(final Job job) {
        super("execution of job with id '" + job.getJobId() + "' timed out after " + job.getTimeSpent() + "ms of execution");
    }
}