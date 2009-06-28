package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:52 PM
 */
public class Destroy extends IQ {

    public static final String DESTROY_TAGNAME = "destroy";
    public static final String VM_JID_ATTRIBUTE = "vm_jid";
    public String vmJid;

    public void setVmJid(String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
    }


    public String getChildElementXML() {

        Element destroyElement = new Element(DESTROY_TAGNAME, LinkedProcess.LOP_FARM_NAMESPACE);
        if(this.vmJid != null) {
            destroyElement.setAttribute(VM_JID_ATTRIBUTE, this.vmJid);
        }

        return LinkedProcess.xmlOut.outputString(destroyElement);
    }
}