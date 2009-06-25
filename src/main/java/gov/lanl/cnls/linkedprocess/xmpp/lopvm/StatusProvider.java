package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:19 PM
 */
public class StatusProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Status status = new Status();
        status.setJobId(parser.getAttributeValue(LinkedProcess.LOP_VM_NAMESPACE, "id"));
        return status;
    }
}
