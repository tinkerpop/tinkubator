package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

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
        parser.next();
        return abortJob;
    }
}