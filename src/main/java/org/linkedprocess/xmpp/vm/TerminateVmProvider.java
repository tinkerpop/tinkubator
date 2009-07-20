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
 * Time: 2:26:06 PM
 */
public class TerminateVmProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {

        TerminateVm terminateVm = new TerminateVm();
 
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            terminateVm.setVmPassword(vmPassword);
        }
        String errorType = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.ERROR_TYPE_ATTRIBUTE);
        if(null != errorType) {
            terminateVm.setErrorType(LinkedProcess.ErrorType.getErrorType(errorType));
        }

        parser.next();
        return terminateVm;
    }
}
