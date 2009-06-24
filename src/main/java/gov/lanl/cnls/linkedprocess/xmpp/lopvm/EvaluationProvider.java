package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 24, 2009
 * Time: 11:29:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationProvider implements IQProvider {


    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Evaluation eval = new Evaluation();
        int v = parser.next();
        if(v == XmlPullParser.TEXT) {
            eval.setCode(parser.getText());
            parser.next();
        }
        return eval;
    }
}
