package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:48:05 PM
 */
public class ManageBindingsProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        ManageBindings manageBindings = new ManageBindings();
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            manageBindings.setVmPassword(vmPassword);
        }
        String errorType = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.ERROR_TYPE_ATTRIBUTE);
        if(null != errorType) {
            manageBindings.setErrorType(LinkedProcess.ErrorType.getErrorType(errorType));
        }

        while(parser.next() == XmlPullParser.START_TAG && parser.getName().equals(LinkedProcess.BINDING_TAG)) {
            String name = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.NAME_ATTRIBUTE);
            String value = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VALUE_ATTRIBUTE);
            String datatype = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.DATATYPE_ATTRIBUTE);

            try {
                manageBindings.addBinding(name, value, datatype);
            } catch(IllegalArgumentException e) {
                if(manageBindings.getBadDatatypeMessage() == null)
                    manageBindings.setBadDatatypeMessage("No such datatype " + datatype);
                else
                    manageBindings.setBadDatatypeMessage(manageBindings.getBadDatatypeMessage() + "\nNo such datatype " + datatype);
            }
            parser.next();
        }

        //parser.next();
        return manageBindings;
    }
}