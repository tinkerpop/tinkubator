package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message;
import org.jdom.input.SAXBuilder;
import org.jdom.Namespace;

import javax.script.ScriptEngine;

import gov.lanl.cnls.linkedprocess.xmpp.lopvm.LopVirtualMachine;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationPacketListener implements PacketListener {

    private ScriptEngine engine;
    private XMPPConnection connection;
    private Namespace LOP_NS = Namespace.getNamespace(LopVirtualMachine.LOP_NAMESPACE);

    public EvaluationPacketListener(ScriptEngine engine, XMPPConnection connection) {
        this.engine = engine;
        this.connection = connection;
    }

    public void processPacket(Packet packet) {
            // The JDOM package is used to turn the raw XML into a DOM object for ease of manipulation.
            SAXBuilder builder = new SAXBuilder();
            try {
                System.out.println("From EvaluationPacketListener:");
                System.out.println(packet.toXML());
                // TODO: XML string values are being converted weird
                //Document domPacket = builder.build(new StringReader(packet.toXML()));
                //String code = domPacket.getRootElement().getChild("eval", LOP_NS).getChild("code", LOP_NS).getTextTrim();
                String packetString = packet.toXML();
                String returnValue = null;
                if(packetString.contains("<code>") && packetString.contains("</code>")) {
                 String code = packetString.substring(packetString.indexOf("<code>")+6, packetString.indexOf("</code>"));
                    try {
                        returnValue = engine.eval(code).toString();
                    } catch(Exception e) {
                        returnValue = e.toString();
                    }
                } else {
                    returnValue = "error";
                }


                Message returnMessage = new Message();
                returnMessage.setTo(packet.getFrom());
                returnMessage.setBody(returnValue);
                if(packet.getPacketID() != null) {
                    returnMessage.setPacketID(packet.getPacketID());
                }
                connection.sendPacket(returnMessage);
                System.out.println(returnMessage.toXML());

                //XMLOutputter out = new XMLOutputter();
                // print the XML packet/stanza to the command line
                //out.output(temp, System.out);
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
}
