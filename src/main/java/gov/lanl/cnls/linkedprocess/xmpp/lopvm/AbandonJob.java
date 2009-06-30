package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:31 PM
 */
public class AbandonJob extends VirtualMachineIq {

    public static final String ABANDON_JOB_TAGNAME = "abandon_job";

    public String getChildElementXML() {

        Element abandonElement = new Element(ABANDON_JOB_TAGNAME, LinkedProcess.LOP_VM_NAMESPACE);
        if(this.jobId != null) {
            abandonElement.setAttribute(JOB_ID_ATTRIBUTE, this.jobId);
        }
        return LinkedProcess.xmlOut.outputString(abandonElement);
    }
}
