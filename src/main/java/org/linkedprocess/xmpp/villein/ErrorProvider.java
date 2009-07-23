package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.ErrorIq;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * User: marko
 * Date: Jul 23, 2009
 * Time: 3:23:29 PM
 */
public class ErrorProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        System.out.println("I AM PARSING!");
        ErrorIq errorIq = new ErrorIq();
        String type = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.TYPE_ATTRIBUTE);
        if (null != type) {
            errorIq.setXmppErrorType(XMPPError.Type.valueOf(type));
        }

        while (parser.next() == XmlPullParser.START_TAG && parser.getName().equals(LinkedProcess.TEXT_TAG)) {
            errorIq.setErrorMessage(parser.getText());
        }
        parser.next();
        return errorIq;
    }
}