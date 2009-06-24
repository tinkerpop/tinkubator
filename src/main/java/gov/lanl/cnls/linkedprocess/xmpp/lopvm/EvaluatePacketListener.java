package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;

import javax.script.ScriptEngine;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluatePacketListener implements PacketListener {

    private XMLOutputter out = new XMLOutputter();
    private ScriptEngine engine;
    private XMPPConnection connection;

    public EvaluatePacketListener(ScriptEngine engine, XMPPConnection connection) {
        this.engine = engine;
        this.connection = connection;
    }

    public void processPacket(Packet eval) {

        try {
            LopVirtualMachine.logger.debug("\nArrived EvaluationPacketListener:");
            LopVirtualMachine.logger.debug(eval.toXML());

            Evaluate returnEval = new Evaluate();
            returnEval.setTo(eval.getFrom());
            if (eval.getPacketID() != null) {
                returnEval.setPacketID(eval.getPacketID());
            }

            String returnValue = null;
            try {

                String code = ((Evaluate) eval).getCode();
                Object returnObject = engine.eval(code);
                
                if(null == returnObject)
                    returnValue = "";
                else
                    returnValue = returnObject.toString();
                
                returnEval.setType(IQ.Type.RESULT);
            } catch (Exception e) {
                returnValue = e.toString();
                returnEval.setType(IQ.Type.ERROR);
            }

            // this makes sure the XML characters are set appropriately as to not create faulty XML.
            Text returnText = new Text(returnValue);
            returnValue = out.outputString(returnText);

            returnEval.setCode(returnValue);
            LopVirtualMachine.logger.debug("\nSent EvaluationPacketListener:");
            LopVirtualMachine.logger.debug(returnEval.toXML());
            connection.sendPacket(returnEval);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
