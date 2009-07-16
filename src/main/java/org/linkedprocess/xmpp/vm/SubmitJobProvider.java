package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 11:29:58 AM
 */
public class SubmitJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        SubmitJob submitJob = new SubmitJob();

        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            submitJob.setVmPassword(vmPassword);
        }

        int v = parser.next();
        if(v == XmlPullParser.TEXT) {
            submitJob.setExpression(parser.getText());
            parser.next();
        }
        return submitJob;
    }
}
