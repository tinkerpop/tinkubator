package gov.lanl.cnls.linkedprocess;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 18, 2009
 * Time: 9:02:29 PM
 * Smack utilizes listeners that respond when packets with certain properties (tags/attributes) are received.
 */
public class GenericPacketListener implements PacketListener {

        public void processPacket(Packet packet) {
            // The JDOM package is used to turn the raw XML into a DOM object for ease of manipulation.
            SAXBuilder builder = new SAXBuilder();
            try {
                System.out.println("From GenericPacketListener:");
                Document domPacket = builder.build(new StringReader(packet.toXML()));
                XMLOutputter out = new XMLOutputter();
                // print the XML packet/stanza to the command line
                out.output(domPacket, System.out);
            } catch(Exception e) {
                e.printStackTrace();
            }
            
        }
}
