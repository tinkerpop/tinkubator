package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 11:29:58 AM
 */
public class EvaluateProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Evaluate eval = new Evaluate();
        int v = parser.next();
        if(v == XmlPullParser.TEXT) {
            eval.setExpression(parser.getText());
            parser.next();
        }
        return eval;
    }
}
