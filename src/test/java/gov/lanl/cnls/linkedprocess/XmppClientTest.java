package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;

import java.util.Set;
import java.util.HashSet;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 5:52:06 PM
 */
public class XmppClientTest extends TestCase {

    private static String username = "linked.process.1@gmail.com";
    private static String password = "linked12";
    private static String server = "talk1.l.google.com";
    private static int port = 5222;

    public void testJidConversion() {
        assertEquals(XmppClient.generateBareJid("linked.process.1@gmail.com/test/12345"), username);
        assertEquals(XmppClient.generateBareJid("lop1@xmpp.linkedprocess.org/test/" + XmppClient.generateRandomID()), "lop1@xmpp.linkedprocess.org");

    }

    public void testRandomIDGenerator() {
        // 1,000,000 random IDs are generated without conflict.
        Set<String> uniques = new HashSet<String>();
        for(int i=0; i<1000000; i++) {
            //System.out.println(XmppClient.generateRandomID());
            uniques.add(XmppClient.generateRandomID());
        }
        assertEquals(uniques.size(), 1000000);
    }
}
