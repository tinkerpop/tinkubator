package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 6:47:07 PM
 */
public class PresenceTypeFilter implements PacketFilter {

    private static final SAXBuilder builder = new SAXBuilder();

    public boolean accept(Packet packet) {
        try {
            Document doc = builder.build(packet.toXML());
            Element root = doc.getRootElement();
            if (root.getName().equalsIgnoreCase("presence")) {
                return false;
            }
            if (root.getAttribute("type").getValue().equalsIgnoreCase("subscibe")) {
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
