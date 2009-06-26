package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:52 PM
 */
public class Destroy extends IQ {

    public static final String DESTROY_TAGNAME = "destroy";
    public String vmJid;

    public void setVmJid(String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
    }


    public String getChildElementXML() {
        StringBuilder builder = new StringBuilder("\n  <" + DESTROY_TAGNAME + " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE +"\" jid=\"" + vmJid + "\" >");

        builder.append("</"+ DESTROY_TAGNAME +">\n");
        return builder.toString();
    }
}