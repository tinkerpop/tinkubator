package gov.lanl.cnls.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class VMAlreadyExistsException extends /*Scheduler*/Exception {
    public VMAlreadyExistsException(final String vmJID) {
        super("virtual machine with JID '" + vmJID + "' already exists");
    }
}