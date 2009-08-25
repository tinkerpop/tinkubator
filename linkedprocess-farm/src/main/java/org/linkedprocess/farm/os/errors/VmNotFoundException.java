package org.linkedprocess.farm.os.errors;

import org.linkedprocess.farm.os.errors.SchedulerException;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VmNotFoundException extends SchedulerException {
    public VmNotFoundException(final String vmId) {
        super("virtual machine '" + vmId + "' does not exist");
    }
}
