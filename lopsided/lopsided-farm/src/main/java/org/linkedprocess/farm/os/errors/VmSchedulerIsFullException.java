package org.linkedprocess.farm.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VmSchedulerIsFullException extends SchedulerException {
    public VmSchedulerIsFullException() {
        super("scheduler cannot instantiate new virtual machines");
    }
}
