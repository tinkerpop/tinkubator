/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.xmlpull.v1.XmlPullParser;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 11:29:58 AM
 */
public class SubmitJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        SubmitJob submitJob = new SubmitJob();

        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if (null != vmPassword) {
            submitJob.setVmPassword(vmPassword);
        }

        int v = parser.next();
        if (v == XmlPullParser.TEXT) {
            String textBody = parser.getText();
            if (textBody != null) {
                submitJob.setExpression(textBody);
            }
            parser.next();
        }
        return submitJob;
    }
}
