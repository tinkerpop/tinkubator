package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.IQ;
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

    public EvaluationPacketListener(ScriptEngine engine, XMPPConnection connection) {
        this.engine = engine;
        this.connection = connection;
    }

    public void processPacket(Packet eval) {
           
            try {
                System.out.println("Arrived EvaluationPacketListener:");
                System.out.println(eval.toXML());

                String returnValue = null;
                try {
                        returnValue = engine.eval(((Evaluation)eval).getCode()).toString();
                    } catch(Exception e) {
                        returnValue = e.toString();
                    }


                Evaluation returnEval = new Evaluation();
                returnEval.setTo(eval.getFrom());
                returnEval.setType(IQ.Type.RESULT);
                if(eval.getPacketID() != null) {
                    returnEval.setPacketID(eval.getPacketID());
                }
                returnEval.setCode(returnValue);
                System.out.println("\nSent EvaluationPacketListener:");
                System.out.println(returnEval.toXML());
                connection.sendPacket(returnEval);

            } catch(Exception e) {
                e.printStackTrace();
            }

        }
}
