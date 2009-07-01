package gov.lanl.cnls.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 10:14:25 AM
 */
public abstract class LopIq extends IQ {

    protected LinkedProcess.Errortype errorType;

        public void setErrorType(LinkedProcess.Errortype errorType) {
            this.errorType = errorType;
        }

        public LinkedProcess.Errortype getErrorType() {
            return this.errorType;
        }

}
