package gov.lanl.cnls.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VMSchedulerIsFullException extends /*Scheduler*/Exception {
    public VMSchedulerIsFullException() {
        super("scheduler cannot instantiate new virtual machines");
    }
}