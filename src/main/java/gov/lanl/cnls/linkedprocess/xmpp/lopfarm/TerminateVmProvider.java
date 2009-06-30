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
public class TerminateVmProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {

        TerminateVm terminateVm = new TerminateVm();
        String vmJid = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, TerminateVm.VM_JID_ATTRIBUTE);
        if(null != vmJid) {
            terminateVm.setVmJid(vmJid);
        }
        parser.next();
        return terminateVm;
    }
}