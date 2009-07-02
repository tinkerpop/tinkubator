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
 * Time: 2:26:06 PM
 */
public class TerminateVmProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {

        TerminateVm terminateVm = new TerminateVm();
 
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            terminateVm.setVmPassword(vmPassword);
        }

        parser.next();
        return terminateVm;
    }
}