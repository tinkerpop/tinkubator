package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:52 PM
 */
public class TerminateVm extends FarmIq {

    public static final String TERMINATE_VM_TAGNAME = "terminate_vm";

    public String getChildElementXML() {

        Element destroyElement = new Element(TERMINATE_VM_TAGNAME, LinkedProcess.LOP_FARM_NAMESPACE);
        if(this.vmJid != null) {
            destroyElement.setAttribute(VM_JID_ATTRIBUTE, this.vmJid);
        }

        return LinkedProcess.xmlOut.outputString(destroyElement);
    }
}