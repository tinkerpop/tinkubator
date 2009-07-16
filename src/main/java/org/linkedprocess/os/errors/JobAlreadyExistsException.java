package gov.lanl.cnls.linkedprocess.os.errors;

import gov.lanl.cnls.linkedprocess.os.Job;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class JobAlreadyExistsException extends SchedulerException {
    public JobAlreadyExistsException(final Job job) {
        super("job with id '" + job.getJobId() + "' already exists on virtual machine with JID '" + job.getVMJID() + "'");
    }
}