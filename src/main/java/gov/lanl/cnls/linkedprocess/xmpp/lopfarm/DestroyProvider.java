package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:26:06 PM
 */
public class DestroyProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Destroy destroy = new Destroy();
        String jid = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, "jid");
        destroy.setVmJid(jid);
        parser.next();
        return destroy;
    }
}