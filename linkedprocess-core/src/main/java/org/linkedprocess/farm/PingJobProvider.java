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
import org.linkedprocess.farm.PingJob;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PingJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        PingJob pingJob = new PingJob();
        String jobId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.JOB_ID_ATTRIBUTE);
        if (null != jobId) {
            pingJob.setJobId(jobId);
        }
        String vmId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_ID_ATTRIBUTE);
        if (null != vmId) {
            pingJob.setVmId(vmId);
        }
        parser.next();
        return pingJob;
    }
}
