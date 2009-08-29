/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.errors.InvalidValueException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A manage_binding parser that creates a ManageBindings object.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ManageBindingsProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        ManageBindings manageBindings = new ManageBindings();

        String vmId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_ID_ATTRIBUTE);
        if (null != vmId) {
            manageBindings.setVmId(vmId);
        }

        while (parser.next() == XmlPullParser.START_TAG && parser.getName().equals(LinkedProcess.BINDING_TAG)) {
            String name = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.NAME_ATTRIBUTE);
            String value = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.STATUS_ATTRIBUTE);
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