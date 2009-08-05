package org.linkedprocess;

import junit.framework.TestCase;
import org.linkedprocess.xmpp.XmppClient;

import java.util.HashSet;
import java.util.Set;

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
        assertEquals(LinkedProcess.generateBareJid("linked.process.1@gmail.com/test/12345"), username);
        assertEquals(LinkedProcess.generateBareJid("lop1@xmpp.linkedprocess.org/test/" + XmppClient.generateRandomResourceId()), "lop1@xmpp.linkedprocess.org");
        assertEquals(LinkedProcess.generateResource("lop1@xmpp.linkedprocess.org/test/"), "test/");
    }

    public void testRandomIDGenerator() {
        // 1,000,000 random IDs are generated without conflict.
        Set<String> uniques = new HashSet<String>();
        for (int i = 0; i < 100000; i++) {
            //System.out.println(XmppClient.generateRandomResourceId());
            uniques.add(XmppClient.generateRandomResourceId());
        }
        assertEquals(uniques.size(), 100000);
    }
}
