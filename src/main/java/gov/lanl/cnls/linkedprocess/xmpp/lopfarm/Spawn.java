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

    public String getChildElementXML() {
        StringBuilder builder = new StringBuilder("\n  <" + SPAWN_TAGNAME + " xmlns=\"" + LinkedProcess.LOP_NAMESPACE +"\">");

        builder.append("</"+ SPAWN_TAGNAME +">\n");
        return builder.toString();
    }
}