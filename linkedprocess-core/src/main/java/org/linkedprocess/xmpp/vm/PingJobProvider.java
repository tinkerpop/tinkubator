package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.xmlpull.v1.XmlPullParser;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:19 PM
 */
public class PingJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        PingJob pingJob = new PingJob();
        String jobId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.JOB_ID_ATTRIBUTE);
        if (null != jobId) {
            pingJob.setJobId(jobId);
        }
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if (null != vmPassword) {
            pingJob.setVmPassword(vmPassword);
        }
        parser.next();
        return pingJob;
    }
}
