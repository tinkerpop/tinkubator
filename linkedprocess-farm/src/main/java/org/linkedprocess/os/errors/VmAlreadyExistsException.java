package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VmAlreadyExistsException extends SchedulerException {
    public VmAlreadyExistsException(final String vmJID) {
        super("virtual machine with JID '" + vmJID + "' already exists");
    }
}
