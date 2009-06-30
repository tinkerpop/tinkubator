package gov.lanl.cnls.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class JobNotFoundException extends /*Scheduler*/Exception {
    public JobNotFoundException(final String jobID) {
        super("job '" + jobID + "' does not exist");
    }
}