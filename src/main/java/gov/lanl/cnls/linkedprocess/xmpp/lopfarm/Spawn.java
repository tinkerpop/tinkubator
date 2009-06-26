package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:20 AM
 */
public class Spawn extends IQ {

    public static final String SPAWN_TAGNAME = "spawn";
    public String vmJid;

    public void setVmJid(String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
    }

    public String getChildElementXML() {
        if(null != vmJid)
            return "<" + SPAWN_TAGNAME + " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE +"\" jid=\"" + this.vmJid + "\" />";
        else
            return "<" + SPAWN_TAGNAME + " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE +"\" />";
    }
}