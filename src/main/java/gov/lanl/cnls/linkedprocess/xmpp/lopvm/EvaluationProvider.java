package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.IQProvider;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 24, 2009
 * Time: 11:29:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationProvider implements IQProvider {

    public IQ parseIQ(org.xmlpull.v1.XmlPullParser parser) throws Exception {
        Evaluation eval = new Evaluation();
        parser.next();
        eval.setCode(parser.getText());
        parser.next();
        return eval;
    }
}
