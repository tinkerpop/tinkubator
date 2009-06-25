package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:28 AM
 */
public class SpawnProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Spawn spawn = new Spawn();
        int v = parser.next();
        return spawn;
    }
}
