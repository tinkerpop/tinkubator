package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:02:49 AM
 */
public abstract class FarmIq extends IQ {


    public static final String VM_JID_ATTRIBUTE = "vm_jid";
    protected String vmJid;

    public void setVmJid(String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
    }
}
