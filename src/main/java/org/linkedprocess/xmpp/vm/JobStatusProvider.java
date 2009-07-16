package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:19 PM
 */
public class JobStatusProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        JobStatus jobStatus = new JobStatus();
        String jobId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.JOB_ID_ATTRIBUTE);
        if(null != jobId) {
            jobStatus.setJobId(jobId);
        }
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            jobStatus.setVmPassword(vmPassword);
        }
        parser.next();
        return jobStatus;
    }
}
