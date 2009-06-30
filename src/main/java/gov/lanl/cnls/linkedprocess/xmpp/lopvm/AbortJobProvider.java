package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:38 PM
 */
public class AbortJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        AbortJob abortJob = new AbortJob();
        abortJob.setJobId(parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.JOB_ID_ATTRIBUTE));
        return abortJob;
    }
}