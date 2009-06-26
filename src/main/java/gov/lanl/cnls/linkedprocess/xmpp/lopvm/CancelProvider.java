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
public class CancelProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Cancel cancel = new Cancel();
        cancel.setJobId(parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, "id"));
        return cancel;
    }
}