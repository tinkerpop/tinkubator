package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VmWorkerIsFullException extends SchedulerException {
    public VmWorkerIsFullException(final String vmJID) {
        super("virtual machine '" + vmJID + "' is not accepting new jobs");
    }
}
