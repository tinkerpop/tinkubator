package gov.lanl.cnls.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 23, 2009
 * Time: 4:05:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationPacket implements PacketExtension {

    private String code;


    public void setCode(String code) {
        this.code = code;
    }

    public String getElementName() {
        return "eval";
    }

    public String getNamespace() {
        return LOPClient.LOP_NAMESPACE;
    }

    public String toXML() {
        Namespace lop = Namespace.getNamespace(LOPClient.LOP_NAMESPACE);
        Element evalElement = new Element("eval", lop);
        Element codeElement = new Element("code", lop);
        codeElement.setText(code);
        evalElement.addContent(codeElement);
        XMLOutputter out = new XMLOutputter();
        return out.outputString(evalElement);

    }
}
