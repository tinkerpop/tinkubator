package gov.lanl.cnls.linkedprocess.os.errors;

/**
 * An exception representing an error condition which is the reponsibility of the client application.
 *
 * Author: josh
 * Date: Jun 26, 2009
 * Time: 10:43:40 AM
 */
public class SchedulerException extends Exception {
    public SchedulerException() {
        super();
    }

    public SchedulerException(final String msg) {
        super(msg);
    }
}
