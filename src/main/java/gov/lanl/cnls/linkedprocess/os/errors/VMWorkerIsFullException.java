package gov.lanl.cnls.linkedprocess.os.errors;

/**
 * Author: josh
* Date: Jun 30, 2009
* Time: 3:44:43 PM
*/
public class VMWorkerIsFullException extends /*Scheduler*/Exception {
    public VMWorkerIsFullException(final String vmJID) {
        super("virtual machine '" + vmJID + "' is not accepting new jobs");
    }
}
