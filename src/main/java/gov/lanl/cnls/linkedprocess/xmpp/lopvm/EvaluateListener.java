package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;

import javax.script.ScriptEngine;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 */
public class EvaluateListener implements PacketListener {

    private XMLOutputter out = new XMLOutputter();
    private ScriptEngine engine;
    private XMPPConnection connection;

    public EvaluateListener(ScriptEngine engine, XMPPConnection connection) {
        this.engine = engine;
        this.connection = connection;
    }

    public void processPacket(Packet eval) {

        try {
            XmppVirtualMachine.LOGGER.debug("Arrived EvaluationPacketListener:");
            XmppVirtualMachine.LOGGER.debug(eval.toXML());

            Evaluate returnEval = new Evaluate();
            returnEval.setTo(eval.getFrom());
            if (eval.getPacketID() != null) {
                returnEval.setPacketID(eval.getPacketID());
            }

            String returnValue = null;
            try {

                String code = ((Evaluate) eval).getExpression();
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

            returnEval.setExpression(returnValue);
            XmppVirtualMachine.LOGGER.debug("Sent EvaluationPacketListener:");
            XmppVirtualMachine.LOGGER.debug(returnEval.toXML());
            connection.sendPacket(returnEval);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
