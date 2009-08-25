package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VmIsFullException extends SchedulerException {
    public VmIsFullException(final String vmId) {
        super("virtual machine '" + vmId + "' is not accepting new jobs");
    }
}
