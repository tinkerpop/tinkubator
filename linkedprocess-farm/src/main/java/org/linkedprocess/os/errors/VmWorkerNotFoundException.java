package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VmWorkerNotFoundException extends SchedulerException {
    public VmWorkerNotFoundException(final String vmJID) {
        super("virtual machine '" + vmJID + "' does not exist");
    }
}
