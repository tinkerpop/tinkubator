package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.linkedprocess.LinkedProcess;

import java.io.IOException;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:38 PM
 */
public class AbortJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        AbortJob abortJob = new AbortJob();
        String jobId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.JOB_ID_ATTRIBUTE);
        if(null != jobId) {
            abortJob.setJobId(jobId);
        }
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            abortJob.setVmPassword(vmPassword);
        }
        String errorType = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.ERROR_TYPE_ATTRIBUTE);
        if(null != errorType) {
            abortJob.setErrorType(LinkedProcess.ErrorType.getErrorType(errorType));
        }
        parser.next();
        return abortJob;
    }
}
