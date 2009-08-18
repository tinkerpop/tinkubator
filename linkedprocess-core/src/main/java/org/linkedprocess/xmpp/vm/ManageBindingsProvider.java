/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.InvalidValueException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:48:05 PM
 */
public class ManageBindingsProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        ManageBindings manageBindings = new ManageBindings();
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if (null != vmPassword) {
            manageBindings.setVmPassword(vmPassword);
        }

        while (parser.next() == XmlPullParser.START_TAG && parser.getName().equals(LinkedProcess.BINDING_TAG)) {
            String name = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.NAME_ATTRIBUTE);
            String value = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VALUE_ATTRIBUTE);
            String datatype = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.DATATYPE_ATTRIBUTE);

            try {
                manageBindings.addBinding(name, value, datatype);
            } catch (InvalidValueException e) {
                String msg = "Invalid value for datatype " + datatype + ": " + value;
                if (null == manageBindings.getInvalidValueMessage()) {
                    manageBindings.setInvalidValueMessage(msg);
                } else {
                    manageBindings.setInvalidValueMessage(manageBindings.getInvalidValueMessage() + "\n" + msg);
                }
            } catch (IllegalArgumentException e) {
                if (manageBindings.getBadDatatypeMessage() == null) {
                    manageBindings.setBadDatatypeMessage("No such datatype " + datatype);
                } else {
                    manageBindings.setBadDatatypeMessage(manageBindings.getBadDatatypeMessage() + "\nNo such datatype " + datatype);
                }
            }
            parser.next();
        }

        //parser.next();
        return manageBindings;
    }
}